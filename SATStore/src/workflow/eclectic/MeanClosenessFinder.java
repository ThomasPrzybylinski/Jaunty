package workflow.eclectic;


public class MeanClosenessFinder extends ClosenessFinder {
	private double cutoff = 0;
	
	@Override
	public boolean areTooClose(int i, int k) {
		if(!pdg.areAdjacent(k,i)) {
			return false;
		}
		if(pdg.getEdgeWeight(i,k) <= 0) { //equiv
			return true;
		}
		if(pdg.areAdjacent(i,k)) {
			return pdg.getEdgeWeight(i,k) <= cutoff;
		}
		return false;
	}

	@Override
	public void initialize() {
		double total = 0;
		int num = 0;
		float val = Float.NEGATIVE_INFINITY;
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = k+1; i < pdg.getNumNodes(); i++) {
				if(pdg.getEdgeWeight(k,i) > 0) { //will return false if NAN (nonadj)
					float weight = pdg.getEdgeWeight(k,i);
					
					if(val == Float.NEGATIVE_INFINITY) {
						val = weight;
					} else if(val != Float.POSITIVE_INFINITY) {
						if(val != weight) {
							val = Float.POSITIVE_INFINITY;
						}
					}
					
					total += weight;
					num++;
				}
			}
		}
		
		double mean = total/(double)num;
		cutoff = mean;
		if(val != Float.POSITIVE_INFINITY) {
			cutoff = cutoff/2;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	
}
