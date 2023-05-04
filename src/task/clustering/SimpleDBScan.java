package task.clustering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import util.IntegralDisjointSet;
import util.lit.LitsSet;

//For clustering models. Since there is no such thing as "noise" for SAT models
//the only attribute we set is the radius

//Right now we are using a simple distance metric
public class SimpleDBScan {
	private int radius;
	private double dRadius;
	private ModelDistance measure;

	public SimpleDBScan(int radius) {
		this.radius = radius;
		this.dRadius = (double)radius;
		measure = new SimpleDifference();
	}

	public SimpleDBScan(int radius, ModelDistance measure) {
		this.radius = radius;
		this.dRadius = (double)radius;
		this.measure = measure;
	}

	public List<Set<int[]>> getClustering(List<int[]> models) {

		IntegralDisjointSet clust = new IntegralDisjointSet(0,models.size()-1);
		int numVars = -1;
		for(int k = 0; k < models.size(); k++) {
			int[] curModel = models.get(k);
			numVars = curModel.length;

			for(int i = k+1; i < models.size(); i++) {
				//				if(clust.sameSet(k,i)) continue;
				int[] otherModel = models.get(i);

				if(measure.distance(curModel,otherModel) <= radius) {
					clust.join(k,i);
				}
			}
		}


		Set<Integer> clustRoots = clust.getRoots();
		Map<Integer,Integer> clusNum = new TreeMap<Integer,Integer>();
		List<Set<int[]>> clusters = new ArrayList<Set<int[]>>(clustRoots.size());

		int index = 0;
		for(int clusRoot : clustRoots) {
			clusters.add(new LitsSet(numVars));
			clusNum.put(clusRoot,index);
			index++;
		}

		for(int k = 0; k < models.size(); k++) {
			clusters.get(clusNum.get(clust.getRootOf(k))).add(models.get(k));
		}

		//		List<Set<int[]>> itemCluster = new ArrayList<Set<int[]>>(models.size());
		//
		//		for(int[] m : models) {
		//			LitsSet itemSet = new LitsSet(m.length);
		//			itemSet.add(m);
		//			itemCluster.add(itemSet);
		//		}
		//
		//
		//
		//		for(int k = 0; k < models.size(); k++) {
		//			int[] curModel = models.get(k);
		//
		//			for(int i = k+1; i < models.size(); i++) {
		//				int[] otherModel = models.get(i);
		//
		//				if(measure.distance(curModel,otherModel) <= radius) {
		//					Set<int[]> cluster = itemCluster.get(k);
		//					Set<int[]> otherCluster = itemCluster.get(i);
		//					cluster.addAll(otherCluster);
		//					itemCluster.set(i,cluster);
		//					
		//					for(int j = 0; j < i; j++) {
		//						if(itemCluster.get(j) == otherCluster) {
		//							itemCluster.set(j,cluster);
		//						}
		//					}
		//					
		//				}
		//			}
		//		}
		//
		//		for(int k = 0; k < itemCluster.size(); k++) {
		//			Set<int[]> curCluster = itemCluster.get(k);
		//
		//			boolean add = true;
		//			for(int i = 0; i < clusters.size(); i++) {
		//				if(clusters.get(i) == curCluster) {
		//					add = false;
		//					break;
		//				}
		//			}
		//
		//			if(add) {
		//				clusters.add(curCluster);
		//			}
		//		}
		return clusters;
	}

	public List<List<int[]>> getClusteringList(List<int[]> models) {
		int[] clustNums = new int[models.size()];
		for(int k = 0; k < models.size(); k++) {
			clustNums[k] = k;
		}
		int curClust = 0;
		for(int k = 0; k < models.size(); k++) {
			if(clustNums[k] != k) {
				continue;
			}
			clustNums[k] = curClust;
			LinkedList<Integer> toClust = new LinkedList<Integer>();
			toClust.add(k);
			while(toClust.size() > 0) {
				int c = toClust.poll();
				int[] curModel = models.get(c);
				for(int i = k+1; i < models.size(); i++) {
					if(i == c || clustNums[i] != i) continue;
					int[] otherModel = models.get(i);

					if(measure.lte(curModel,otherModel,dRadius)) {
						clustNums[i] = curClust;
						toClust.add(i);
					}
				}
			}
			curClust++;
		}
		
		List<List<int[]>> clusters = new ArrayList<List<int[]>>(curClust+1);

		for(int k = 0; k < curClust; k++) {
			clusters.add(new ArrayList<int[]>());
		}
		
		for(int k = 0; k < models.size(); k++) {
			clusters.get(clustNums[k]).add(models.get(k));
		}

		return clusters;
	}
	
	public List<List<int[]>> getTighterClustersing(List<List<int[]>> prevClustering) {
		List<List<int[]>> clusters = new ArrayList<List<int[]>>(prevClustering.size());
		
		
		for(int k = 0; k < prevClustering.size(); k++) {
			List<int[]> clust = prevClustering.get(k);
			ArrayList<int[]> newClust = new ArrayList<int[]>(clust.size());
			newClust.addAll(clust);
			clusters.add(newClust);
		}
		
		for(int k = 0; k < clusters.size(); k++) {
			List<int[]> clust1 = clusters.get(k);
			for(int i = k+1; i < clusters.size(); i++) {
				List<int[]> clust2 = clusters.get(i);
				if(withinRadius(clust1,clust2)) {
					clust1.addAll(clust2);
					clusters.remove(i);
					i--;
				}
			}
		}
		
		return clusters;
	}

	private boolean withinRadius(List<int[]> clust1, List<int[]> clust2) {
		for(int k = 0; k < clust1.size(); k++) {
			int[] m1 = clust1.get(k);
			for(int i = 0; i < clust2.size(); i++) {
				int[] m2 = clust2.get(i);
				if(measure.lte(m1,m2,dRadius)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
