package DistanceTests;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import task.clustering.AgglomerativeClustering;
import task.clustering.ClusterHierarchy;
import task.clustering.ModelDistance;
import task.clustering.SimpleDifference;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.DisjointSet;
import formula.simple.CNF;

public class GeneralizedClusterTest {
	public static ClusterHierarchy getHierarchy(CNF formula) throws ContradictionException, TimeoutException {
		List<int[]> models = SATUtil.getAllModels(formula);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);

//		int[] modelExample = models.get(0);
//		FastVector attrs = new FastVector(modelExample.length);
//		FastVector attrVals = new FastVector(2);
//		attrVals.addElement(""+0);
//		attrVals.addElement(""+1);
//
//		for(int i = 0; i < modelExample.length; i++) {
//			attrs.addElement(new Attribute(""+i,attrVals));
//		}
//
//		Instances inst = new Instances("Queens",attrs,models.size());
//
//		for(int[] i : models) {
//			double[] data = new double[i.length];
//			for(int k = 0; k < data.length; k++) {
//				data[k] = i[k] > 0 ? 1 : 0;
//			}
//			inst.add(new Instance(1,data));
//		}

		return getHierarchy(models);
	}

	public static ClusterHierarchy getHierarchy(List<int[]> models) {
		AgglomerativeClustering ac = new AgglomerativeClustering();
		return ac.cluster(models);
	}
	
	public static ClusterHierarchy getHierarchy(DisjointSet<int[]> models) {
		AgglomerativeClustering ac = new AgglomerativeClustering();
		return ac.continueCluster(models.getSets());
	}
	
	public static void printReps(ClusterHierarchy hier, ConsoleDecodeable decoder) throws IOException {
		printReps(hier,1,hier.getMaxLevel(),decoder);
	}
	
	public static void printReps(ClusterHierarchy hier, int start, int end, ConsoleDecodeable decoder) {
		ModelDistance dist = new SimpleDifference();
		for(int k = start; k < end; k++) { //ignore unclustered and cluster of 1
			System.out.println("LEVEL " + k);
			List<Set<int[]>> curClust = hier.getClusterAtLevel(k);

			for(int i = 0; i < curClust.size(); i++) {
				Set<int[]> clust = curClust.get(i);
				
				int[] maxMin = getPointFurthestFromAllOtherOutsidePoints(dist, curClust, clust);
				
				System.out.println(decoder.consoleDecoding(maxMin));
			}
		}
	}
	
	public static void saveReps(String prefix, ClusterHierarchy hier, FileDecodable decoder) throws IOException {
		saveReps(prefix,hier,1,hier.getMaxLevel(),decoder);
	}
	
	public static void saveReps(String prefix, ClusterHierarchy hier, int start, int end, FileDecodable decoder) throws IOException {
		ModelDistance dist = new SimpleDifference();
		for(int k = start; k < end; k++) { //ignore unclustered and cluster of 1
			System.out.println("LEVEL " + k);
			List<Set<int[]>> curClust = hier.getClusterAtLevel(k);

			for(int i = 0; i < curClust.size(); i++) {
				Set<int[]> clust = curClust.get(i);
				
				int[] maxMin = getPointFurthestFromAllOtherOutsidePoints(dist, curClust, clust);
				
				decoder.fileDecoding(prefix+"_Level_"+k+"Cl_"+ i,  maxMin);
			}
		}
	}


	public static int[] getPointFurthestFromAllOtherOutsidePoints(
			ModelDistance dist, List<Set<int[]>> curClust, Set<int[]> clust) {
		int[] maxMin = null;
		double maxMinDist = Integer.MIN_VALUE;
		for(int[] model : clust) {
			double minDist = Integer.MAX_VALUE;
			for(int j = 0; j < curClust.size(); j++) {
				Set<int[]> clust2 = curClust.get(j);
				if(clust == clust2) continue;
				
				for(int[] mod2 : clust2) {
					if(dist.distance(model,mod2) < minDist) {
						minDist = dist.distance(model,mod2); 
					}
				}
			}
			if(minDist > maxMinDist) {
				maxMinDist = minDist;
				maxMin = model;
			}
		}
		return maxMin;
	}
}
