package workflow.eclectic;


//Only works with counting distance
public class NVarsClosenessFinder extends ClosenessFinder {
	private double ratio; //Max num of agreement between interpretations
	private double cutoff;
	
	public NVarsClosenessFinder(double ratio) {
		super();
		this.ratio = ratio;
	}

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
		cutoff = pdg.getElt(0).length*ratio;

	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
