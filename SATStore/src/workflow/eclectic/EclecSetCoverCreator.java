package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.List;
import java.util.Random;


public abstract class EclecSetCoverCreator {
	protected static Random rand = new Random();
	
	public abstract List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg);
	public abstract List<Integer> getRandomEclecticSet(PossiblyDenseGraph<int[]> pdg);
	public abstract boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg, List<Integer> list);

	public String getDirLabel() {
		return this.getClass().getSimpleName();
	}

}
