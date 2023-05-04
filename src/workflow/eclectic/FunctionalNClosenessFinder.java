package workflow.eclectic;

import java.util.PriorityQueue;


//Two are two close if the distance between the two is <= the distance from one to the furthest of its sqrt(n) nearest neighbors
//Sort of adpated from from  page 486 of Data Mining: Concepts and Techniques books (han et al)
public class FunctionalNClosenessFinder extends ClosenessFinder {
	private double[] sqrtNDist;
	private NFunction func;
	
	public FunctionalNClosenessFinder(NFunction nFunc) {
		this.func = nFunc;
	}

	@Override
	public boolean areTooClose(int i, int k) {
		if(!pdg.areAdjacent(k,i)) {
			return false;
		}
		float weight = pdg.getEdgeWeight(k,i);
		return weight <= sqrtNDist[i] || weight <= sqrtNDist[k];
	}

	@Override
	public void initialize() {
		sqrtNDist = new double[pdg.getNumNodes()];
		int n = pdg.getNumNodes();
		PriorityQueue<Float> queue = new PriorityQueue<Float>(sqrtNDist.length);
		for(int k = 0; k < sqrtNDist.length; k++) {
			queue.clear();
			for(int i = 0; i < sqrtNDist.length; i++) {
				if(k != i && pdg.areAdjacent(k,i)) {
					if(pdg.getEdgeWeight(k,i) > 0) {
						queue.add(pdg.getEdgeWeight(k,i));	
					} else {
						n--; //try to ignore symmetric edges
					}

				}
			}

			float cur = 0;
			int numRem = n - func.calcNum(n); 
			for(int i = 0; i < numRem && !queue.isEmpty(); i++) {
				cur = queue.poll();
			}

			sqrtNDist[k] = cur;
		}

	}

	@Override
	public String toString() {
		return "NN(" + func.toString() +")";
	}
	
	

}
