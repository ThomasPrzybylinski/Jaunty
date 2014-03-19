package workflow.eclectic;

import graph.PossiblyDenseGraph;

public abstract class ClosenessFinder {
	protected PossiblyDenseGraph<int[]> pdg;
	
	public ClosenessFinder() {}
	
	public ClosenessFinder(PossiblyDenseGraph<int[]> pdg) {
		this.pdg = pdg;
	}
	
	public PossiblyDenseGraph<int[]> getPdg() {
		return pdg;
	}

	public void setPdg(PossiblyDenseGraph<int[]> pdg) {
		this.pdg = pdg;
	}


	public abstract boolean areTooClose(int i, int k);
	public abstract void initialize();

}
