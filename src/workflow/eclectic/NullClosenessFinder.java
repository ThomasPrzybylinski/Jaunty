package workflow.eclectic;


public class NullClosenessFinder extends ClosenessFinder {

	@Override
	public boolean areTooClose(int i, int k) {
		return pdg.areAdjacent(i,k);
	}

	@Override
	public void initialize() {


	}

}
