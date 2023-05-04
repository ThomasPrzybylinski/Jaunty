package workflow.eclectic;

import java.util.PriorityQueue;


//Two are two close if the distance between the two is <= the distance from one to the furthest of its k nearest neighbors
public class KNNClosenessFinder extends ClosenessFinder {
	private int knn;
	private double[] kDist;
	
	public KNNClosenessFinder(int knn) {
		this.knn = knn;
	}

	@Override
	public boolean areTooClose(int i, int k) {
		if(!pdg.areAdjacent(k,i)) {
			return false;
		}
		float weight = pdg.getEdgeWeight(k,i);
		return weight <= kDist[i] || weight <= kDist[k];
	}

	@Override
	public void initialize() {
		kDist = new double[pdg.getNumNodes()];
		
		PriorityQueue<Float> queue = new PriorityQueue<Float>(kDist.length);
		for(int k = 0; k < kDist.length; k++) {
			queue.clear();
			for(int i = 0; i < kDist.length; i++) {
				if(k != i && pdg.areAdjacent(k,i)) {
					queue.add(pdg.getEdgeWeight(k,i));
				}
			}
			
			float cur = 0;
			for(int i = 0; i < knn && !queue.isEmpty(); i++) {
				cur = queue.poll();
			}
			
			kDist[k] = cur;
		}

	}

}
