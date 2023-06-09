package workflow;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import io.GraphIO;
import task.clustering.SimpleDifference;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.lit.MILEComparator;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.PredefinedEdgeManipulator;
import workflow.graph.ShortestPathCreator;

public class EclecWorkflow {
	private int testIters = 100;

	private LinkedList<EclecWorkflowData> data;
	private ModelGiver creat;
	private ConsoleDecodeable consoleDecoder;
	private FileDecodable fileDecoder;
	private File modelsDir;
	
	private boolean doStats = true;
	private boolean sortPics = true;
	private boolean sortModels = true;
	

	PossiblyDenseGraph<int[]> distanceGraph = null;
	PossiblyDenseGraph<int[]> symGraph = null;
	PossiblyDenseGraph<int[]> shortPathSymDistGraph = null;

	ClauseList models;
	ClauseList usableModels;

	//the list data will be modified for memory reasons
	public EclecWorkflow(LinkedList<EclecWorkflowData> data, ModelGiver creat, File modelsDir) {
		this.data = data;
		this.creat = creat;
		this.modelsDir = modelsDir;

		if(creat.getConsoleDecoder() != null) {
			consoleDecoder = creat.getConsoleDecoder();
		}
		if(creat.getFileDecodabler() != null) {
			fileDecoder = creat.getFileDecodabler();
		}
	}


	public ConsoleDecodeable getConsoleDecoder() {
		return consoleDecoder;
	}


	public void setConsoleDecoder(ConsoleDecodeable consoleDecoder) {
		this.consoleDecoder = consoleDecoder;
	}


	public FileDecodable getFileDecoder() {
		return fileDecoder;
	}


	public void setFileDecoder(FileDecodable fileDecoder) {
		this.fileDecoder = fileDecoder;
	}
	
	public boolean isDoStats() {
		return doStats;
	}


	public void setDoStats(boolean doStats) {
		this.doStats = doStats;
	}

	public boolean isSortPics() {
		return sortPics;
	}


	public void setSortPics(boolean sortPics) {
		this.sortPics = sortPics;
	}

	public boolean isSortModels() {
		return sortModels;
	}


	public void setSortModels(boolean sortModels) {
		this.sortModels = sortModels;
	}


	public PossiblyDenseGraph<int[]>[] getDissimilarityGraphs() throws IOException, TimeoutException {
		@SuppressWarnings("unchecked")
		PossiblyDenseGraph<int[]>[] ret = new PossiblyDenseGraph[data.size()];
		HashMap<EdgeManipulator,PredefinedEdgeManipulator> alreadyDone = new HashMap<EdgeManipulator,PredefinedEdgeManipulator>();
		setupModels();

		if(models.size() == 0) return null;

		if(doStats) {
			setupStatsGraphs(alreadyDone);
		}

		int index = 0;
		while(!data.isEmpty()) {
			EclecWorkflowData eclecData = data.poll();
			PossiblyDenseGraph<int[]> graph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
			addEdges(eclecData, alreadyDone, graph);
			ret[index] = graph;
			index++;
		}

		return ret;

	}


	public void executeWorkflow() throws TimeoutException, IOException {
		if(consoleDecoder == null) {
			consoleDecoder = new DefaultConsoleDecoder();
		}

		HashMap<EdgeManipulator,PredefinedEdgeManipulator> alreadyDone = new HashMap<EdgeManipulator,PredefinedEdgeManipulator>();
		setupModels();

		if(models.size() == 0) return;

		if(doStats) {
			setupStatsGraphs(alreadyDone);
		}

		File modelsParent = modelsDir.getParentFile();

		File index = new File(modelsParent,modelsDir.getName() + "_index.html"); 
		PrintWriter indexBuilder = new PrintWriter(index);
		appendHead(indexBuilder);


		for(EclecWorkflowData eclecData: data) {
			performSingleWorkflow(eclecData,alreadyDone);

			Path modelPath = new File(eclecData.getDirectory(),"index.html").toPath();
			Path thisPath = modelsParent.toPath();
			String relativePath = thisPath.relativize(modelPath).toString();
			relativePath = relativePath.replaceAll("\\\\",File.separator);

			indexBuilder.append("<p><a href="+relativePath+">");
			indexBuilder.append("Eclectic Set " + eclecData.getDirectory().getName());
			indexBuilder.println("</a></p>");
		}

		appendEnd(indexBuilder);
		indexBuilder.close();
	}


	private void setupStatsGraphs(
			HashMap<EdgeManipulator, PredefinedEdgeManipulator> alreadyDone) {
		distanceGraph =  null; //new PossiblyDenseGraph<int[]>(usableModels);
		symGraph = null; 

		top: for(EclecWorkflowData eclecData: data) {
			for(EdgeManipulator em : eclecData.getDistanceMaker().getManipulators()) {
				if(em instanceof DistanceEdges) {
					distanceGraph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
					PossiblyDenseGraph<int[]> intermediate = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
					//em.addEdges(intermediate,models);
					em.addEdges(intermediate,usableModels);

					PredefinedEdgeManipulator toAdd = new PredefinedEdgeManipulator(intermediate);
					alreadyDone.put(em,toAdd);

					toAdd.addEdges(distanceGraph,usableModels);
					break top;
				}
			}
		}

		if(distanceGraph == null) {
			distanceGraph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
			(new DistanceEdges(new SimpleDifference())).addEdges(distanceGraph,usableModels);
		}


		top: for(EclecWorkflowData eclecData: data) {
			for(EdgeManipulator em : eclecData.getDistanceMaker().getManipulators()) {
				if(em instanceof GlobalSymmetryEdges) {
					symGraph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
					PossiblyDenseGraph<int[]> intermediate = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
					//em.addEdges(intermediate,models);
					em.addEdges(intermediate,usableModels);

					PredefinedEdgeManipulator toAdd = new PredefinedEdgeManipulator(intermediate);
					alreadyDone.put(em,toAdd);

					toAdd.addEdges(symGraph,usableModels);
					break top;
				}
			}
		}

		if(symGraph == null) {
			symGraph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
			(new GlobalSymmetryEdges()).addEdges(symGraph,usableModels);
		}

		shortPathSymDistGraph  = new PossiblyDenseGraph<int[]>(models.getClauses());
		PredefinedEdgeManipulator manip = new PredefinedEdgeManipulator(distanceGraph);
		manip.addEdges(shortPathSymDistGraph,usableModels);
		manip = new PredefinedEdgeManipulator(symGraph);
		manip.addEdges(shortPathSymDistGraph,usableModels);

		ShortestPathCreator shortPath = new ShortestPathCreator();
		shortPath.addEdges(shortPathSymDistGraph,usableModels);
	}

	private void setupModels() throws TimeoutException, IOException{
		models = null;
		usableModels = null;

		VariableContext curContext = new VariableContext();



		List<int[]> curModels = creat.getAllModels(curContext); 

		if(sortModels) {
			Collections.sort(curModels, new MILEComparator());
		}

		curModels = Collections.unmodifiableList(curModels);
		
		models = new ClauseList(curContext);
		models.fastAddAll(curModels);

		if(modelsDir != null) {
			modelsDir.mkdirs();

			for(File f : modelsDir.listFiles()) {
				f.delete();
			}


			for(int k = 0; k < models.size(); k++) {
				if(fileDecoder != null) {
					fileDecoder.fileDecoding(modelsDir, creat.getDirName() + "_"+k ,models.getClauses().get(k));
				}
				File text = new File(modelsDir, creat.getDirName() + "_"+k+".txt");
				PrintWriter pw = new PrintWriter(text);
				String outString = consoleDecoder.consoleDecoding(models.getClauses().get(k));
				pw.println(outString);
				pw.close();
			}
		}

		if(models.size() == 0) return;


		//TODO: Fix preProcessing!!!!
		//usableModels = preProcess(models);


		///TODO: Fix preProcessing!!!
//		ClauseList cl = new ClauseList(curContext);
//		cl.addAll(models.getClauses());
		usableModels = models;
	}

	public void performSingleWorkflow(EclecWorkflowData eclecData, HashMap<EdgeManipulator, PredefinedEdgeManipulator> alreadyDone) throws TimeoutException, IOException {
		File dir = eclecData.getDirectory();

		if(dir.getCanonicalPath().equals(new File(".").getCanonicalPath())
				||!(dir.getCanonicalPath().startsWith(new File(".").getCanonicalPath()))) {
			throw new IOException("NO!!! NOT THAT DIRECTORY");
		}

		dir.mkdirs();
		for(File f : dir.listFiles()) {
			f.delete();
		}

		PossiblyDenseGraph<int[]> graph = new PossiblyDenseGraph<int[]>(usableModels.getClauses());


		addEdges(eclecData, alreadyDone, graph);

		EclecSetCoverCreator eclecCreat = eclecData.getCreator();
		List<List<Integer>> eclecs = eclecCreat.getEclecticSetCover(graph);


		File fullGraph = new File(dir,"graph.csv");
		File fullImage = new File(dir,"bits.png");
		PrintWriter graphWriter = new PrintWriter(fullGraph);
		
		PossiblyDenseGraph<int[]> closeGraph = new PossiblyDenseGraph<int[]>(graph.getObjs());
		BufferedImage image = new BufferedImage(closeGraph.getNumNodes(),closeGraph.getNumNodes(),BufferedImage.TYPE_4BYTE_ABGR);
		for(int k = 0; k<closeGraph.getNumNodes(); k++) {
			image.setRGB(k,k,Color.BLACK.getRGB());
			image.setRGB(k,k,Color.BLACK.getRGB());
			for(int i = k+1; i < closeGraph.getNumNodes(); i++) {
				if(eclecCreat.verifyEclecticPair(graph,k,i)) {
					closeGraph.setAdjacent(k,i);
					
					image.setRGB(k,i,Color.WHITE.getRGB());
					image.setRGB(i,k,Color.WHITE.getRGB());
				} else {
					image.setRGB(k,i,Color.BLACK.getRGB());
					image.setRGB(i,k,Color.BLACK.getRGB());
				}
			}
		}

		graphWriter.write(GraphIO.graphtoPrimativeCSV(closeGraph));
		graphWriter.close();
		ImageIO.write(image,"png",fullImage);
		
		File index = new File(dir,"index.html"); 
		PrintWriter indexBuilder = new PrintWriter(index);
		appendHead(indexBuilder);

		int eclecNum = -1;
		for(List<Integer> collec: eclecs) {
			if(!eclecData.getCreator().displayUnitSets() && collec.size() == 1) continue;
			eclecNum++;

			if(sortPics) {
				Collections.sort(collec);
			}

			String fileName = "eclecSet_"+eclecNum+".html";

			File eclecFile = new File(dir,fileName);
			indexBuilder.append("<p><a href="+fileName+">");
			indexBuilder.append("Eclectic Set " + eclecNum+"["+collec.size()+"]");
			indexBuilder.println("</a></p>");

			PrintWriter fileBuilder = new PrintWriter(eclecFile);
			appendHead(fileBuilder);
			
			if(doStats) {
				appendStats(collec,fileBuilder);
			}

			fileBuilder.println("<table border=1>");
			fileBuilder.println("\t<tr>");
			int curCol = 0;
			int numMod = 0;
			for(Integer i : collec) {
				if(curCol == 4) {
					curCol = 0;
					fileBuilder.println("\t</tr>");
					fileBuilder.println("\t<tr>");
				}
				curCol++;
				numMod++;

				fileBuilder.append("\t\t<td>");
				fileBuilder.println("\t\t\t<p>"+numMod+"</p>");
				if(fileDecoder == null) {
					fileBuilder.append("<pre>");
					fileBuilder.append(consoleDecoder.consoleDecoding(models.getClauses().get(i)));
					fileBuilder.append("</pre>");
				} else {
					Path modelPath = modelsDir.toPath();
					Path thisPath = dir.toPath();
					String relativePath = thisPath.relativize(modelPath).toString();
					relativePath = relativePath.replaceAll("\\\\","/");
					fileBuilder.append("<img src="+relativePath+"/"+creat.getDirName()+"_"+i+".png>");
				}
				fileBuilder.println("</td>");
			}
			fileBuilder.println("</table>");
			appendEnd(fileBuilder);
			fileBuilder.close();
			indexBuilder.flush(); //so we don't have to wait while it's running
		}
		indexBuilder.close();
	}


	private void appendStats(List<Integer> collec, PrintWriter fileBuilder) {
		int size = collec.size();
		double avgPairwiseDistance = getAvgPairwiseDistance(collec,distanceGraph);
		double minDist = getMinPairwiseDistance(collec,distanceGraph);
		double maxDist = getMaxPairwiseDistance(collec,distanceGraph);

		double numGlobSym = getNumGlobSymmetric(collec,symGraph);
		double avgEnt = getAvgEntropy(collec);
		double avgPairwiseDistanceWSym = getAvgPairwiseDistance(collec,shortPathSymDistGraph);
		double minDistSym = getMinPairwiseDistance(collec,shortPathSymDistGraph);
		double maxDistSym = getMaxPairwiseDistance(collec,shortPathSymDistGraph);


		List<Integer> randomSample = new ArrayList<Integer>(usableModels.size());
		for(int k = 0; k < usableModels.size(); k++) {
			randomSample.add(k);
		}

		double testAvgPairwiseDistance = 0;
		double testNumGlobSym = 0;
		double testAvgEnt = 0;
		double testAvgPairwiseDistanceWSym = 0;
		double testMinDist = 0;
		double testMaxDist = 0;
		double testMinDistSym = 0;
		double testMaxDistSym = 0;

		List<Integer> testList = new ArrayList<Integer>(collec.size());
		int numIters = 0;
		while(numIters < testIters) {
			Collections.shuffle(randomSample);
			for(int i = 0; i + size <= usableModels.size(); i += size,numIters++) { //Note numIters++
				testList.clear();
				for(int j = 0; j < size; j++) {
					testList.add(randomSample.get(i+j));
				}
				testAvgPairwiseDistance += getAvgPairwiseDistance(testList,distanceGraph);
				testMinDist += getMinPairwiseDistance(testList,distanceGraph);
				testMaxDist += getMaxPairwiseDistance(testList,distanceGraph);

				testNumGlobSym += getNumGlobSymmetric(testList,symGraph);
				testAvgEnt += getAvgEntropy(testList);

				testAvgPairwiseDistanceWSym += getAvgPairwiseDistance(testList,shortPathSymDistGraph);
				testMinDistSym += getMinPairwiseDistance(testList,shortPathSymDistGraph);
				testMaxDistSym += getMaxPairwiseDistance(testList,shortPathSymDistGraph);
			}
			if(testIters == 0) return; //Something is wrong
		}

		printStats(fileBuilder, avgPairwiseDistance,numGlobSym,avgEnt, avgPairwiseDistanceWSym,
				minDist,maxDist,minDistSym,maxDistSym,
				"This Set Stats");

		testAvgPairwiseDistance = testAvgPairwiseDistance/(double)testIters;
		testNumGlobSym = testNumGlobSym/(double)testIters;
		testAvgEnt = testAvgEnt/(double)testIters;
		testAvgPairwiseDistanceWSym = testAvgPairwiseDistanceWSym/(double)testIters;
		testMinDist = testMinDist/(double)testIters;
		testMaxDist = testMaxDist/(double)testIters;
		testMinDistSym = testMinDistSym/(double)testIters;
		testMaxDistSym = testMaxDistSym/(double)testIters;

		printStats(fileBuilder, testAvgPairwiseDistance,testNumGlobSym, testAvgEnt, testAvgPairwiseDistanceWSym,
				testMinDist,testMaxDist,testMinDistSym,testMaxDistSym,
				"Random Set Stats");

	}


	private void printStats(PrintWriter fileBuilder,
			double avgPairwiseDistance, double numGlobSym, double ent,
			double avgPairwiseDistanceWSym, 
			double minDist, double maxDist, double minDistSym, double maxDistSym, String title) {
		fileBuilder.println("<h2>"+title+"</h2>");
		fileBuilder.print("<p><b>Average Pairwise Distance</b>:");
		fileBuilder.printf("%0$2.3f</p>",avgPairwiseDistance);

		fileBuilder.print("<p><b>Min Pairwise Distance</b>:");
		fileBuilder.printf("%0$2.3f</p>",minDist);

		fileBuilder.print("<p><b>Max Pairwise Distance</b>:");
		fileBuilder.printf("%0$2.3f</p>",maxDist);

		fileBuilder.print("<p><b>Number Global Symmetries</b>:");
		fileBuilder.printf("%0$2.2f</p>",numGlobSym);

		fileBuilder.print("<p><b>Average Entropy</b>:");
		fileBuilder.printf("%0$2.3f</p>",ent);

		fileBuilder.print("<p><b>Average Pairwise Distance w/ Symmetry</b>:");
		fileBuilder.printf("%0$2.3f</p>",avgPairwiseDistanceWSym);

		fileBuilder.print("<p><b>Min Pairwise Distance w/ Symmetry</b>:");
		fileBuilder.printf("%0$2.3f</p>",minDistSym);

		fileBuilder.print("<p><b>Max Pairwise Distance w/ Symmetry</b>:");
		fileBuilder.printf("%0$2.3f</p>",maxDistSym);

	}


	private int getNumGlobSymmetric(List<Integer> collec,
			PossiblyDenseGraph<int[]> symGraph2) {
		int num = 0;
		for(int k = 0; k < collec.size(); k++) {
			int item1 = collec.get(k);
			for(int i = k+1; i < collec.size(); i++) {
				int item2 = collec.get(i);	
				if(symGraph2.areAdjacent(item1,item2)) {
					float dist = symGraph2.getEdgeWeight(item1,item2);
					if(dist <= 0) {
						num++;
					}
				}
			}
		}

		return num;
	}


	private double getAvgPairwiseDistance(List<Integer> collec,
			PossiblyDenseGraph<int[]> distanceGraph2) {
		double total = 0;
		long num = 0;
		for(int k = 0; k < collec.size(); k++) {
			int item1 = collec.get(k);
			for(int i = k+1; i < collec.size(); i++) {
				int item2 = collec.get(i);	
				if(distanceGraph2.areAdjacent(item1,item2)) {
					num++;
					float dist = distanceGraph2.getEdgeWeight(item1,item2);
					total += dist > 0 ? dist : 0;
				}
			}
		}

		return num == 0 ? 0 : total/(double)num;
	}

	private double getMinPairwiseDistance(List<Integer> collec,
			PossiblyDenseGraph<int[]> distanceGraph2) {
		double min = Double.MAX_VALUE;
		for(int k = 0; k < collec.size(); k++) {
			int item1 = collec.get(k);
			for(int i = k+1; i < collec.size(); i++) {
				int item2 = collec.get(i);	
				if(distanceGraph2.areAdjacent(item1,item2)) {
					float dist = distanceGraph2.getEdgeWeight(item1,item2);
					if(dist > 0) {
						min = Math.min(min,dist);
					}
				}
			}
		}

		if(min == Double.MAX_VALUE) {
			min = Double.NaN;
		}

		return min;
	}

	private double getMaxPairwiseDistance(List<Integer> collec,
			PossiblyDenseGraph<int[]> distanceGraph2) {
		double max = -1;
		for(int k = 0; k < collec.size(); k++) {
			int item1 = collec.get(k);
			for(int i = k+1; i < collec.size(); i++) {
				int item2 = collec.get(i);	
				if(distanceGraph2.areAdjacent(item1,item2)) {
					float dist = distanceGraph2.getEdgeWeight(item1,item2);
					if(dist > 0) {
						max = Math.max(max,dist);
					}
				}
			}
		}

		if(max == -1) {
			max = Double.NaN;
		}

		return max;
	}

	private double getAvgEntropy(List<Integer> collec) {
		int[] varFreq = new int[models.getClauses().get(0).length];
		double[] perChance = new double[models.getClauses().get(0).length];

		for(int[] m : models.getClauses()) {
			for(int k = 0; k < m.length; k++) {
				if(m[k] > 0) {
					varFreq[k]++;
				}
			}
		}

		for(int k = 0; k < varFreq.length; k++) {
			perChance[k] = varFreq[k]/(double)models.size();
		}

		varFreq = null;

		double totalEntropy = 0;

		for(int i : collec) {
			totalEntropy += getEntropy(models.getClauses().get(i),perChance);
		}

		return totalEntropy/(double)collec.size();
	}


	private double getEntropy(int[] model, double[] perChance) {
		double ret = 0;
		for(int k = 0; k < model.length; k++) {
			if(perChance[k] != 1. && perChance[k] != 0) {
				double prob = model[k] == 1 ? perChance[k] : 1 - perChance[k];
				ret += prob * (Math.log(prob)/Math.log(2));
			}
		}

		return -ret;
	}


	private void addEdges(EclecWorkflowData eclecData,
			HashMap<EdgeManipulator, PredefinedEdgeManipulator> alreadyDone,
			PossiblyDenseGraph<int[]> graph) {
		for(EdgeManipulator em : eclecData.getDistanceMaker().getManipulators()) {
			if(alreadyDone.containsKey(em)) {
				alreadyDone.get(em).addEdges(graph,usableModels);
			} else if(em.isSimple()) {
				PossiblyDenseGraph<int[]> intermediate = new PossiblyDenseGraph<int[]>(usableModels.getClauses());
				em.addEdges(intermediate,usableModels);

				PredefinedEdgeManipulator toAdd = new PredefinedEdgeManipulator(intermediate);
				alreadyDone.put(em,toAdd);

				toAdd.addEdges(graph,usableModels);
			} else {
				em.addEdges(graph,usableModels);
			}
		}
	}

	private void appendHead(PrintWriter index) {
		index.println("<!DOCTYPE html>");
		index.println("<html>");
		index.println("<body>");
	}

	private void appendEnd(PrintWriter index) {
		index.println("</body>");
		index.println("</html>");
	}

	//Remove equivalent vars
//	private List<int[]> preProcess(List<int[]> models) {
//		int[] template = models.get(0);
//
//		List<Integer> equiveList = new ArrayList<Integer>(template.length);
//		for(int k = 0; k < template.length; k++) {
//			equiveList.add(k+1);
//		}
//
//		DisjointSet<Integer> equivalentVars = new DisjointSet<Integer>(equiveList);
//		equiveList.clear();
//
//		for(int k = 0; k < template.length; k++) {
//			if(equivalentVars.getRootOf(k+1) != k+1) continue;
//			for(int i = k+1; i < template.length; i++) {
//				if(equivalentVars.sameSet(k+1,i+1)) continue;
//				boolean equive = true;
//				boolean negEquive = true;
//
//				for(int[] model : models) {
//					if(model[k]/Math.abs(model[k]) == model[i]/Math.abs(model[i])) {
//						negEquive = false;
//					} else {
//						equive = false;
//					}
//
//					if(!equive && !negEquive) {
//						break;
//					}
//				}
//
//				if(equive || negEquive) {
//					equivalentVars.join(k+1,i+1);
//				}
//
//				//				if(negEquive) {
//				//					for(int[] model : models) {
//				//						model[i] = model[k];
//				//					}
//				//				}
//			}
//		}
//
//		int[] roots = new int[equivalentVars.getRoots().size()];
//		int index = 0;
//		for(Integer i : equivalentVars.getRoots()) {
//			roots[index] = i;
//			index++;
//		}
//		Arrays.sort(roots);
//		List<int[]> ret = new ArrayList<int[]>(models.size());
//		for(int[] model : models) {
//			int[] toAdd = new int[roots.length];
//
//			for(int k = 0; k < roots.length; k++) {
//				toAdd[k] = model[roots[k]-1];
//			}
//			ret.add(toAdd);
//		}
//		return ret;
//	}
}
