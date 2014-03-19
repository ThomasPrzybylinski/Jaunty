package task.clustering;

import graph.PossiblyDenseGraph;
import util.lit.LitsMap;

public class PrecomputedDistance extends ModelDistance {
	private LitsMap<Integer> distIndex;
	private PossiblyDenseGraph<int[]> distance;
	
	//Distances is NOT copied over so changes to that will change how this class functions
	public PrecomputedDistance(PossiblyDenseGraph<int[]> distances) {
		this.distance = distances;
		int numVars = distances.getElt(0).length;
		
		distIndex = new LitsMap<Integer>(numVars);
		for(int k = 0; k < distances.getNumNodes(); k++) {
			distIndex.put(distances.getElt(k),k);
		}
	}
	
	public int getIndex(int[] model) {
		return distIndex.get(model);
	}

	@Override
	public double distance(int[] m1, int[] m2) {
		Integer i1 = distIndex.get(m1);
		Integer i2 = distIndex.get(m2);
		
		if(i1 == null || i2 == null) throw new UnsupportedOperationException("Either m1 or m2 are not models with precomputed distances");
		
		return distance.getEdgeWeight(i1,i2);
	}

}
