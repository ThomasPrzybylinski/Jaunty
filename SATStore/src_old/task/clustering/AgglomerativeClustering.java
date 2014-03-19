package task.clustering;

import java.util.List;
import java.util.Set;

//Hierarchical Clustering
public class AgglomerativeClustering {
	private ModelDistance measure;

	public AgglomerativeClustering() {
		measure = new SimpleDifference();
	}

	public AgglomerativeClustering(ModelDistance measure) {
		this.measure = measure;
	}

	public ClusterHierarchy cluster(List<int[]> models) {
		MyClusterHierarchy hier = new MyClusterHierarchy(models);
		doClustering(hier);
		
		return hier;
	}
	
	public ClusterHierarchy continueCluster(List<Set<int[]>> initalClusters) {
		MyClusterHierarchy hier = new MyClusterHierarchy(initalClusters,null);
		doClustering(hier);
		
		return hier;
		
	}

	private void doClustering(MyClusterHierarchy hier) {
		List<Set<int[]>> curCluster = hier.getClusterAtLevel(hier.getMaxLevel());
		
		while(curCluster.size() > 1) {
			double minDist = getMinDistances(curCluster);
		
			clusterMinDist(curCluster,hier,minDist);
			
			hier.startNextLevel();
			
			curCluster = hier.getClusterAtLevel(hier.getMaxLevel());
		}
	}

	private void clusterMinDist(List<Set<int[]>> curCluster,
			MyClusterHierarchy hier, double minDist) {
		
		for(int k = 0; k < curCluster.size(); k++) {
			Set<int[]> c1 = curCluster.get(k);
			for(int i = k+1; i < curCluster.size(); i++) {
				Set<int[]> c2 = curCluster.get(i);

				boolean join = false;
				compare: for(int[] m1 : c1) {
					for(int[] m2 : c2) {
						if(measure.distance(m1,m2) == minDist) {
							join = true;
							break compare;
						}
					}
				}
				if(join) {
					hier.join(k,i);
				}
			}
		}
		
	}

	private double getMinDistances(List<Set<int[]>> curCluster) {
		double minDistance = Double.MAX_VALUE;

		for(int k = 0; k < curCluster.size(); k++) {
			Set<int[]> c1 = curCluster.get(k);
			for(int i = k+1; i < curCluster.size(); i++) {
				Set<int[]> c2 = curCluster.get(i);

				for(int[] m1 : c1) {
					for(int[] m2 : c2) {
						minDistance = Math.min(minDistance, measure.distance(m1,m2));
					}
				}
			}
		}
		return minDistance;
	}
}
