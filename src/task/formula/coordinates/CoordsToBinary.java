package task.formula.coordinates;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sat4j.MoreThanSAT;
import org.sat4j.core.LiteralsUtils;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import graph.PossiblyDenseGraph;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.lit.LitUtil;
import util.lit.LitUtil.RemovalDecoding;
import workflow.ModelGiver;

public class CoordsToBinary implements ModelGiver, ConsoleDecodeable {//, FileDecodable {
	private CoordinateGenerator gen;
	private CoordSpace space;
	private List<int[]> models;
	private List<HyperPlane> seperatingPlanes;
	
	
	public CoordsToBinary(CoordinateGenerator gen) {
		this.gen=gen;
		models=null;
	}
	
	@Override
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
		if(models != null) return models;
		
		space = gen.generateCoords();
		
		this.models = coordsToModels(space, context);
		return models;
	}

	public static List<int[]> coordsToModels(CoordSpace space, VariableContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("xy=rbind(");
		boolean first = true;
		
		for(double[] pt : space.getPts()) {
			if(first) {
				first=false;
			} else {
				sb.append(",");
			}
			
			sb.append("c(");
			sb.append(pt[0]).append(",").append(pt[1]);
			sb.append(")");
		}
		
		sb.append(")");
		System.out.println(sb);
		
		//return coordsToModelsSplittingPlane(space,context);
		return coordsToModelsBinPartition(space,context);
	}
		
		
	public static List<int[]> coordsToModelsBinPartition(CoordSpace space, VariableContext context) {
		//Partition Method
		double[] minBounds = new double[space.getDim()];
		double[] maxBounds = new double[space.getDim()];
		
		Arrays.fill(minBounds,Double.POSITIVE_INFINITY);
		Arrays.fill(maxBounds,Double.NEGATIVE_INFINITY);
		
		for(double[] point : space.getPts()) {
			for(int k = 0; k < point.length;k++) {
				minBounds[k] = Math.min(minBounds[k],point[k]);
				maxBounds[k] = Math.max(maxBounds[k],point[k]);
			}
		}
		
		System.out.println("plot(xy,col=\"green\",pch=3)");
		
		List<HyperPlane> seperatingPlanes = new ArrayList<HyperPlane>(space.getPts().size());
		ArrayList<Integer>[] models = (ArrayList<Integer>[])(new ArrayList[space.getPts().size()]);
		for(int k = 0; k < models.length; k++) {
			models[k] = new ArrayList<Integer>();
		}
		
		int partition=1;
		while(!endPartition(models)) {
			partition *= 2;
			for(int dim=0; dim < minBounds.length; dim++) {
				double partSize = (maxBounds[dim]-minBounds[dim])/(double)partition;
				
				BigInteger bigNum = BigInteger.valueOf(partition); 
				
				for(int part = 1; part < partition; part++) {
					BigInteger partNum = BigInteger.valueOf(part);
					if(part == 1 || part==partition-1 || bigNum.gcd(partNum).equals(BigInteger.ONE)) {
						double coord = part*partSize + minBounds[dim];
						double[] plane = new double[space.getDim()+1];
						plane[0]=-coord;
						plane[dim+1]=1;
						HyperPlane sepPlane = new HyperPlane(space.getDim(),plane);
						seperatingPlanes.add(sepPlane);
						
						for(int k = 0; k < models.length; k++) {
							int val = sepPlane.value(space.getPts().get(k)) >= 0 ? models[k].size()+1 : -(models[k].size()+1);
							models[k].add(val);
						}
					}
				}
			}
		}
		

		
		List<int[]> ret = new ArrayList<int[]>(models.length);
		for(ArrayList<Integer> m : models) {
			int[] toAdd = new int[m.size()];
			for(int k = 0; k < m.size() ;k++) {
				toAdd[k]=m.get(k);
			}
			ret.add(toAdd);
		}
		
		RemovalDecoding decoder = LitUtil.getSingleValAndEquivVarDecoding(ret,context);
		for(int k = 0; k < seperatingPlanes.size(); k++) {
			if(!decoder.isRemoved[k+1]) {
			String out = seperatingPlanes.get(k).getXYString();
				if(out != null) {
					System.out.println(out);
				}
			}
		}
		ret=LitUtil.removeSingleValAndEquivVars(ret,context,decoder);
		
		System.out.println("points(xy,col=\"green\",pch=3)");
		
		return ret;
	}
	
	
	private static boolean endPartition(ArrayList<Integer>[] models) {
		for(int k = 0; k < models.length; k++) {
			ArrayList<Integer> m1 = models[k];
			
			if(m1.size() == 0) return false;
			
			for(int i = k+1; i < models.length; i++) {
				ArrayList<Integer> m2 = models[i];
				if(m2.size() == 0) return false;
				if(m1.equals(m2)) return false;
			}
		}
		return true;
	}
	
	public static List<int[]> coordsToModelsSplittingPlane(CoordSpace space, VariableContext context) {
		List<double[]> pts = space.getPts();
		
		List<int[]> models = new ArrayList<int[]>(pts.size());
		
		int modVars = (pts.size()*(pts.size()-1))/2;
		context.ensureSize(modVars);
		for(int k = 0; k < pts.size(); k++) {
			models.add(new int[modVars]);
		}
		
		List<HyperPlane> seperatingPlanes = new ArrayList<HyperPlane>(modVars);
		
		int index = -1;
		System.out.println("plot(xy,col=\"green\",pch=3)");
		for(int k = 0; k < pts.size(); k++) {
			double[] pt1 = pts.get(k);
			for(int i = k+1; i < pts.size(); i++) {
				index++;
				
				double[] pt2 = pts.get(i);
				
				double interceptPt = 0;
				double[] slopePt = new double[pt2.length+1];
				double[] testPt = new double[pt2.length];
				
				for(int j = 0; j < testPt.length; j++) {
					interceptPt += (pt1[j]+pt2[j])*(pt1[j]-pt2[j]);
					slopePt[j+1] = (pt1[j]-pt2[j])/2;
					testPt[j]= (pt1[j]+pt2[j])/2;
				}
				interceptPt = -interceptPt/4;
				slopePt[0]=interceptPt;
				HyperPlane hp = new HyperPlane(space.getDim(),slopePt);
				seperatingPlanes.add(hp);
//				System.out.println(Arrays.toString(slopePt));

//				System.out.println(Arrays.toString(pt1));
//				System.out.println(Arrays.toString(pt2));
//				System.out.println("abline(a="+(slopePt[0]/-slopePt[2])+",b="+(slopePt[1]/-slopePt[2])+")");
//				System.out.println(hp.value(pt1));
//				System.out.println(hp.value(pt2));
				
				for(int j = 0; j < models.size(); j++) {
					models.get(j)[index] = hp.value(pts.get(j)) >= 0 ? index+1 : -(index+1);
				}
			}
		}
		RemovalDecoding decoder = LitUtil.getSingleValAndEquivVarDecoding(models,context);
		for(int k = 0; k < seperatingPlanes.size(); k++) {
			if(!decoder.isRemoved[k+1]) {
				System.out.println(seperatingPlanes.get(k).getXYString());
			}
		}
		models=LitUtil.removeSingleValAndEquivVars(models,context,decoder);
		
		System.out.println("points(xy,col=\"green\",pch=3)");
		
		return models;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;
	}

	@Override
	public String getDirName() {
		return "CoordToModel";
	}

//	@Override
//	public void fileDecoding(File dir, String filePrefix, int[] model) throws IOException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void fileDecoding(String filePrefix, int[] model) throws IOException {
//		// TODO Auto-generated method stub
//		
//	}
	
	public String getGDF(PossiblyDenseGraph<int[]> graph) {
		StringBuilder sb = new StringBuilder();
		sb.append("nodedef>name VARCHAR,label VARCHAR,x DOUBLE,y DOUBLE");
		sb.append(ConsoleDecodeable.newline);
		for(int k = 0; k < graph.getObjs().size(); k++) {
			sb.append(k).append(",'").append(Arrays.toString(space.getPts().get(k))).append("'");
			sb.append(","+space.getPts().get(k)[0]+","+space.getPts().get(k)[1]);
			sb.append(ConsoleDecodeable.newline);
		}
		
		sb.append("edgedef>node1 VARCHAR,node2 VARCHAR,weight DOUBLE");
		sb.append(ConsoleDecodeable.newline);
		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = k+1; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
//					sb.append(k+ "--" +i+";");
					sb.append(k+ "," +i+","+"10");
					sb.append(ConsoleDecodeable.newline);
				}
			}
		}
		return sb.substring(0,sb.length()-1);
	}

	@Override
	public String consoleDecoding(int[] model) {
		for(int k = 0; k < models.size(); k++) {
			if(Arrays.equals(model,models.get(k))) {
				return Arrays.toString(space.getPts().get(k));
			}
		}
		return null;
	}

}
