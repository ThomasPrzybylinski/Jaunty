package task.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.DisjointSet;
//Agglomerative
public class MyClusterHierarchy extends ClusterHierarchy {
	private List<List<Set<int[]>>> clusters;
	private DisjointSet<Integer> intermediateClustering;
	private int curLevel = 0;
	private int curOkLevel = 0;
	
	public MyClusterHierarchy(List<int[]> initial) {
		clusters = new ArrayList<List<Set<int[]>>>();
		ArrayList<Set<int[]>> curCluster = new ArrayList<Set<int[]>>(initial.size());
		for(int[] mod : initial) {
			HashSet<int[]> hs = new HashSet<int[]>(1);
			hs.add(mod);
			curCluster.add(hs);
		}
		clusters.add(curCluster);
		
		ArrayList<Integer> indecies = new ArrayList<Integer>();
		for(int i = 0; i < initial.size(); i++) {
			indecies.add(i);
		}
		intermediateClustering = new DisjointSet<Integer>(indecies);
		curLevel = 1;
		curOkLevel = 0;
	}
	
	public MyClusterHierarchy(List<Set<int[]>> initial, Object dummy) { //dummy because generics not reified
		clusters = new ArrayList<List<Set<int[]>>>();
		ArrayList<Set<int[]>> curCluster = new ArrayList<Set<int[]>>(initial.size());
		for(Set<int[]> clust : initial) {
			HashSet<int[]> hs = new HashSet<int[]>(clust.size());
			hs.addAll(clust);
			curCluster.add(hs);
		}
		clusters.add(curCluster);
		
		ArrayList<Integer> indecies = new ArrayList<Integer>();
		for(int i = 0; i < initial.size(); i++) {
			indecies.add(i);
		}
		intermediateClustering = new DisjointSet<Integer>(indecies);
		curLevel = 1;
		curOkLevel = 0;
	}
	
	

	public int startNextLevel() {
		Set<Integer> roots = intermediateClustering.getRoots();
		
		List<Set<int[]>> prevClusters = clusters.get(clusters.size()-1);
		List<Set<int[]>> newClusters = new ArrayList<Set<int[]>>();
				
		for(Integer r : roots) {
			Set<int[]> clust = new HashSet<int[]>();
			clust.addAll(prevClusters.get(r));
			for(int k = 0; k < prevClusters.size(); k++) {
				if(k == r) continue;
				if(intermediateClustering.sameSet(k,r)) {
					clust.addAll(prevClusters.get(k));
				}
			}
			newClusters.add(clust);
		}
		
		clusters.add(newClusters);
		
		ArrayList<Integer> indecies = new ArrayList<Integer>();
		for(int i = 0; i < newClusters.size(); i++) {
			indecies.add(i);
		}
		intermediateClustering = new DisjointSet<Integer>(indecies);
		
		curOkLevel = curOkLevel >= curLevel ? curOkLevel : curOkLevel+1;
		
		if(roots.size() != 1) {
			curLevel++;
		}
		return curLevel;
	}
	
	@Override
	public int getMaxLevel() {
		return curOkLevel;
	}

	@Override
	public List<Set<int[]>> getClusterAtLevel(int level) {
		return clusters.get(level);
	}

	public void join(int index1, int index2) {
		intermediateClustering.join(index1,index2);
	}
	
	public boolean areJoined(int index1, int index2) {
		return intermediateClustering.sameSet(index1,index2);
	}

}
