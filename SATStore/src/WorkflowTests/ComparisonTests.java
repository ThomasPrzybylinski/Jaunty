package WorkflowTests;

import graph.PossiblyDenseGraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import task.clustering.SimpleDifference;
import task.formula.AllFilledRectangles;
import task.formula.AllRectangles;
import task.formula.AllSquares;
import task.formula.CycleMatching;
import task.formula.LineColoringCreator;
import task.formula.MonotonicPath;
import task.formula.QueensToSAT;
import task.formula.RestrictedLineColoringCreator;
import task.formula.SpaceFillingCycles;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.EclecWorkflowData;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.RandomCreator;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.ShortestPathCreator;
import workflow.graph.local.AgreementConstructionAdder;
import workflow.graph.local.AgreementLocalSymAdder;

public class ComparisonTests {
	private static PrintStream out = System.out;
	private static final int iters = 10000;
	
	public static void main(String[] args) throws Exception {
//		out = new PrintStream("Summary Stats");
		
		
		
		EdgeManipulator[][] processes = //Need at least 1
				new EdgeManipulator[][]{
				{new DistanceEdges(new SimpleDifference())},
//				{new GlobalSymmetryEdges()},
				{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges()},
				{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),new ShortestPathCreator()},
				{new AgreementLocalSymAdder()},
//				{new GlobalSymmetryEdges(),new AgreementLocalSymAdder(),},
//				{new DistanceEdges(new SimpleDifference()),new AgreementLocalSymAdder(),},
				{new DistanceEdges(new SimpleDifference()),new GlobalSymmetryEdges(),new AgreementLocalSymAdder()},
//				{new DistanceEdges(new SimpleDifference()),new GlobalSymmetryEdges(),new AgreementLocalSymAdder(), new DistanceEdges(new SimpleDifference()),new ShortestPathCreator()},
				{new DistanceEdges(new SimpleDifference()),new GlobalSymmetryEdges(),new ShortestPathCreator(),new AgreementLocalSymAdder()},
				
				{new AgreementConstructionAdder(false)},
				{new AgreementConstructionAdder(true)},
				{new DistanceEdges(new SimpleDifference()),new AgreementConstructionAdder()},
				//Add Global edges first even though we add them again just so shortest path creator can do its thing
				{new DistanceEdges(new SimpleDifference()),new GlobalSymmetryEdges(),new ShortestPathCreator(),new AgreementConstructionAdder(true)},
			};
		EclecSetCoverCreator creator =  new IndependentSetCreator(new MeanClosenessFinder());
				//new IndependentSetCreator(new NVarsClosenessFinder(.33)),
				//new NonLocalSymIndSetCreator(new MeanClosenessFinder()),
				//new IndependentSetCreator(new FunctionalNClosenessFinder(new HalfFunction())),
		//, new DBScanEclecCreator(new KNNClosenessFinder(1)),
		//	new ClusterCreator(),
			//new MaximumIndependentSetCreator(new MeanClosenessFinder()),
		//	new RandomCreator()

		ModelGiver[] modelCreators = new ModelGiver[]{
				
				new CNFCreatorModelGiver(new QueensToSAT(8)), 
				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
				new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
				new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)), //THIS IS CURRENTLY THE ONE IN THE THESIS
				new CNFCreatorModelGiver(new MonotonicPath(6,6)),
				new CNFCreatorModelGiver(new CycleMatching(11)),
				new AllSquares(7),
				new AllFilledRectangles(5),
				new AllRectangles(5),
//				new CNFCreatorModelGiver(new SimpleCNFCreator(24,3,3)),
//				new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
//				new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
//				//new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
		};
		
		
		LinkedList<EclecWorkflowData> data = new LinkedList<EclecWorkflowData>();
		
		for(EdgeManipulator[] process : processes) {
			EclecWorkflowData dataInstance = new EclecWorkflowData();
			dataInstance.addEdgeAdder(new CompoundEdgeManipulator(process));
			dataInstance.setCreator(creator);
			data.add(dataInstance);
		}
		
		for(int k = 0; k < processes.length; k++) {
			out.print(k + " ");
			for(EdgeManipulator em : processes[k]) {
				out.print(em.getClass().getSimpleName() +",");
			}
			out.println();
		}
		out.println();
		
		
		for(ModelGiver mg : modelCreators) {
			EclecWorkflow workflow = new EclecWorkflow(data,mg,null);
			PossiblyDenseGraph<int[]>[] graphs = workflow.getDissimilarityGraphs();
			out.println(mg.getDirName());
			List<Integer>[][] sets = getTestSets(data,graphs,iters);
			out.println("Min Dist Avg");
			printAvgMinDistance(sets,graphs);
			out.println("Set size");
			printSizes(sets);
			out.println("Exclustions");
			int[][] excl = printExclusions(data,graphs,sets);
			out.println("Random Exclustions");
			int[][] randExcl = printRandomExclusions(data,graphs,sets);
			out.println("Excl vs Rand excl");
			printExclComparison(excl,randExcl);
//			out.println("Kulc");
//			printExclKulc(excl,randExcl);
//			out.println("Cosine Correlation");
//			printExclCosine(excl,randExcl);
			out.println();
		}
		
		out.flush();
		out.close();
	}
	
	private static void printSizes(List<Integer>[][] sets) {
		for(int i = 0; i < sets.length; i++) {
			out.format("%0$6d ",i);
		}
		out.println();
		
		for(int k = 0; k < sets.length; k++) {
			long total = 0;
			for(int i = 0; i < sets[k].length; i++) {
				total += sets[k][i].size();
			}
			out.format("%0$6.2f ",total/(double)sets[k].length);
		}
		out.println();
		out.println();
	}
	
	private static void printAvgMinDistance(List<Integer>[][] sets,PossiblyDenseGraph<int[]>[] graphs) {
		for(int i = 0; i < sets.length; i++) {
			out.format("%0$6d ",i);
		}
		out.println();
		
		for(int k = 0; k < sets.length; k++) {
			double total = 0;
			int len = sets[k].length;
			for(int i = 0; i < sets[k].length; i++) {
				int min = getMinDist(sets[k][i],graphs[k]);
				if(min != -1) {
					total += min;
				} else {
					len--;
				}
			}
			out.format("%0$6.2f ",total/len);
		}
		out.println();
		out.println();
	}
	
	private static int getMinDist(List<Integer> list,
			PossiblyDenseGraph<int[]> graph) {
		List<int[]> models = graph.getObjs();
		SimpleDifference diff = new SimpleDifference();
		
		int dif = Integer.MAX_VALUE;
		
		for(int k = 0; k < list.size(); k++) {
			for(int i = k+1; i < list.size(); i++) {
				dif = Math.min(dif, (int)diff.distance(models.get(k),models.get(i)));
			}
			
		}
		return dif == Integer.MAX_VALUE ? -1 : dif;
	}

//	private static void printAvgAvgEntropy(List<Integer>[][] sets,PossiblyDenseGraph<int[]>[] graphs) {
//		for(int i = 0; i < sets.length; i++) {
//			out.format("%0$6d ",i);
//		}
//		out.println();
//		
//		for(int k = 0; k < sets.length; k++) {
//			double total = 0;
//			for(int i = 0; i < sets[k].length; i++) {
//				total += getAvgEntropy(sets[k][i],graphs[k]);
//			}
//			out.format("%0$6.2f ",total/(double)sets[k].length);
//		}
//		out.println();
//		out.println();
//	}
	
//	private static double getAvgEntropy(List<Integer> collec, PossiblyDenseGraph<int[]> graph) {
//		List<int[]> models = graph.getObjs();
//		int[] varFreq = new int[models.get(0).length];
//		double[] perChance = new double[models.get(0).length];
//
//		for(int[] m : models) {
//			for(int k = 0; k < m.length; k++) {
//				if(m[k] > 0) {
//					varFreq[k]++;
//				}
//			}
//		}
//
//		for(int k = 0; k < varFreq.length; k++) {
//			perChance[k] = varFreq[k]/(double)models.size();
//		}
//
//		varFreq = null;
//
//		double totalEntropy = 0;
//
//		for(int i : collec) {
//			totalEntropy += getEntropy(models.get(i),perChance);
//		}
//
//		return totalEntropy/(double)collec.size();
//	}


//	private static double getEntropy(int[] model, double[] perChance) {
//		double ret = 0;
//		for(int k = 0; k < model.length; k++) {
//			if(perChance[k] != 1. && perChance[k] != 0) {
//				double prob = model[k] == 1 ? perChance[k] : 1 - perChance[k];
//				ret += prob * (Math.log(prob)/Math.log(2));
//			}
//		}
//
//		return -ret;
//	}
	
	@SuppressWarnings("unchecked")
	private static List<Integer>[][] getTestSets(List<EclecWorkflowData> data,
			PossiblyDenseGraph<int[]>[] graphs, int num) {
		List<Integer>[][] sets = new List[data.size()][num];
		
		for(int k = 0; k < data.size(); k++) {
			sets[k] = new List[num];
			
			for(int i = 0; i < sets[k].length; i++) {
				sets[k][i] = data.get(k).getCreator().getRandomEclecticSet(graphs[k]);
			}
		}
		return sets;
	}

	//Rows are creators, Columns are testers.
	private static int[][] printExclusions(List<EclecWorkflowData> data,
			PossiblyDenseGraph<int[]>[] graphs,List<Integer>[][] sets) {
		int[][] exclusions = new int[data.size()][data.size()];
		
		for(int k = 0; k < data.size(); k++) {
			for(int i = 0; i < data.size(); i++) {
				exclusions[k][i] = 0;
//				if(k == i) {
//					continue;
//				}
				
				for(int j = 0; j < sets[k].length; j++) {
					if(!data.get(i).getCreator().verifyEclecticSet(graphs[i], sets[k][j])) {
						exclusions[k][i]++;
					}
				}
			}
		}
		
		out.print("      ");
		for(int i = 0; i < data.size(); i++) {
			out.format("%0$6d ",i);
		}
		out.println();
		
		for(int k = 0; k < data.size(); k++) {
			out.format("%0$6d ",k);
			for(int i = 0; i < data.size(); i++) {
				out.format("%0$6d ",exclusions[k][i]);
			}
			out.println();
		}
		
		return exclusions;
	}
	
	private static int[][] printRandomExclusions(List<EclecWorkflowData> data,
			PossiblyDenseGraph<int[]>[] graphs,List<Integer>[][] sets) {
		int[][] exclusions = new int[data.size()][data.size()];
		RandomCreator creat = new RandomCreator();
		
		for(int k = 0; k < data.size(); k++) {
			for(int i = 0; i < data.size(); i++) {
				exclusions[k][i] = 0;
//				if(k == i) {
//					continue;
//				}
				
				for(int j = 0; j < sets[k].length; j++) {
					int size = sets[k][j].size();
					List<Integer> randSetOfSameSize = creat.getRandomEclecticSet(graphs[k],size);
					if(!data.get(i).getCreator().verifyEclecticSet(graphs[i], randSetOfSameSize)) {
						exclusions[k][i]++;
					}
				}
			}
		}
		
		out.print("      ");
		for(int i = 0; i < data.size(); i++) {
			out.format("%0$6d ",i);
		}
		out.println();
		
		for(int k = 0; k < data.size(); k++) {
			out.format("%0$6d ",k);
			for(int i = 0; i < data.size(); i++) {
				out.format("%0$6d ",exclusions[k][i]);
			}
			out.println();
		}
		
		return exclusions;
	}
	
	private static void printExclComparison(int[][] excl, int[][] randExcl) {
		out.print("       ");
		for(int i = 0; i < excl.length; i++) {
			out.format("%0$6d ",i);
		}
		out.println();
		
		for(int k = 0; k < excl.length; k++) {
			out.format("%0$6d ",k);
			for(int i = 0; i < excl[k].length; i++) {
				double num = 0;
				num = (excl[k][i] + 1)/((double)randExcl[k][i] + 1);
				out.format("%0$6.2f ",num);
			}
			out.println();
		}
		
	}

//	private static void printExclKulc(int[][] excl, int[][] randExcl) {
//		out.print("       ");
//		for(int i = 0; i < excl.length; i++) {
//			out.format("%0$6d ",i);
//		}
//		out.println();
//		
//		for(int k = 0; k < excl.length; k++) {
//			out.format("%0$6d ",k);
//			for(int i = 0; i < excl[k].length; i++) {
//				double num = 0;
//				double pAGivB = 1 - (excl[k][i] + 1)/((double)randExcl[k][i] + 1);
//				double pBGivA = 1 - (excl[i][k] + 1)/((double)randExcl[i][k] + 1);
//				out.format("%0$6.2f ",.5*(pAGivB + pBGivA));
//			}
//			out.println();
//		}
//		
//	}
//	
//	private static void printExclCosine(int[][] excl, int[][] randExcl) {
//		out.print("       ");
//		for(int i = 0; i < excl.length; i++) {
//			out.format("%0$6d ",i);
//		}
//		out.println();
//		
//		for(int k = 0; k < excl.length; k++) {
//			out.format("%0$6d ",k);
//			for(int i = 0; i < excl[k].length; i++) {
//				double num = 0;
//				num = (excl[k][i] + 1)/((double)randExcl[k][i] + 1);
//				double pAGivB = 1 - (excl[k][i] + 1)/((double)randExcl[k][i] + 1);
//				double pBGivA = 1 - (excl[i][k] + 1)/((double)randExcl[i][k] + 1);
//				out.format("%0$6.2f ",Math.sqrt(pAGivB*pBGivA));
//			}
//			out.println();
//		}
//		
//	}
}
