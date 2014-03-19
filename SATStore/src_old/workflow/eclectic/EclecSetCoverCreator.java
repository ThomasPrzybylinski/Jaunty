package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.List;


public abstract class EclecSetCoverCreator {
	
	public abstract List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg);

	public String getDirLabel() {
		return this.getClass().getSimpleName();
	}

}
