package task.clustering;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class AgglomerativeDiameterClustering { 
	private ModelDistance measure;
	private static Random rand = new Random();
	public AgglomerativeDiameterClustering() {
		measure = new SimpleDifference();
	}

	public AgglomerativeDiameterClustering(ModelDistance measure) {
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
			double minDist = getMinDiameter(curCluster);

			clusterMinDiameter(curCluster,hier,minDist);

			hier.startNextLevel();

			curCluster = hier.getClusterAtLevel(hier.getMaxLevel());
		}
	}

	private void clusterMinDiameter(List<Set<int[]>> curCluster,
			MyClusterHierarchy hier, double minDist) {
		boolean joined = false;
		for(int k = 0; k < curCluster.size(); k++) {
			Set<int[]> c1 = curCluster.get(k);
			for(int i = k+1; i < curCluster.size(); i++) {
				Set<int[]> c2 = curCluster.get(i);

				double diameter = -1;
				for(int[] m1 : c1) {
					for(int[] m2 : c2) {
						diameter = Math.max(diameter, measure.distance(m1,m2));
					}
				}
				
				if(diameter == minDist) {
					boolean doJoin = !hier.areJoined(k,i);//!joined || rand.nextBoolean();
					if(doJoin) {
						hier.join(k,i);
						k = 0;
						break;
					}
					
				}
			}
		}
	}

	private double getMinDiameter(List<Set<int[]>> curCluster) {
		double minDiameter = Double.MAX_VALUE;

		for(int k = 0; k < curCluster.size(); k++) {
			Set<int[]> c1 = curCluster.get(k);
			
			for(int i = k+1; i < curCluster.size(); i++) {
				double potentialDiameter = -1;
				Set<int[]> c2 = curCluster.get(i);
				
				for(int[] m1 : c1) {
					for(int[] m2 : c2) {
						potentialDiameter = Math.max(potentialDiameter, measure.distance(m1,m2));
					}
				}
				minDiameter = Math.min(potentialDiameter,minDiameter);
			}
		}
		return minDiameter;
	}
}