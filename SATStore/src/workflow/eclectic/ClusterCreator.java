package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import task.clustering.AgglomerativeClustering;
import task.clustering.ClusterHierarchy;
import task.clustering.ModelDistance;
import task.clustering.PrecomputedDistance;

public class ClusterCreator extends EclecSetCoverCreator {
	public ClusterCreator() {
	}

	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		PrecomputedDistance dist = new PrecomputedDistance(pdg);
		AgglomerativeClustering cl = new AgglomerativeClustering(dist);
		
		List<int[]> models = new LinkedList<int[]>();
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			models.add(pdg.getElt(k));
		}
		
		ClusterHierarchy hier = cl.cluster(models);
		
		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();
		
		for(int k = 1; k <= hier.getMaxLevel(); k++) { //ignore unclustered and cluster of 1
			System.out.println("LEVEL " + k);
			List<Set<int[]>> curClust = hier.getClusterAtLevel(k);
			List<Integer> toAdd = new LinkedList<Integer>();
			
			for(int i = 0; i < curClust.size(); i++) {
				Set<int[]> clust = curClust.get(i);
				
				int[] maxMin = getPointFurthestFromAllOtherOutsidePoints(dist, curClust, clust);//getRandPoint(clust); //
				toAdd.add(dist.getIndex(maxMin));
			}
			
			ret.add(toAdd);
		}
		return ret;
	}
	
	public int[] getRandPoint(Set<int[]> clust) {
		int taken = rand.nextInt(clust.size());
		
		for(int[] i : clust) {
			if(taken == 0) {
				return i;
			} else {
				taken--;
			}
		}
		return null; //should never happen
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

	@Override
	public List<Integer> getRandomEclecticSet(
			PossiblyDenseGraph<int[]> pdg) {
		throw new NotImplementedException();
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}

	@Override
	public double getEclecticSetScore(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}

}
