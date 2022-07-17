package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.List;
import java.util.Random;


public abstract class EclecSetCoverCreator {
	private static Random fixedRand = new Random();
	protected Random rand = fixedRand;
	
	public abstract List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg);
	public abstract List<Integer> getRandomEclecticSet(PossiblyDenseGraph<int[]> pdg);
	public abstract boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg, List<Integer> list);
	public abstract boolean verifyEclecticPair(PossiblyDenseGraph<int[]> pdg, int v1, int v2);
	public abstract double getEclecticSetScore(PossiblyDenseGraph<int[]> pdg, List<Integer> list);

	public void setRand(Random rand) {
		this.rand = rand;
	}
	
	public String getDirLabel() {
		return this.getClass().getSimpleName();
	}
	
	public boolean displayUnitSets() {
		return false;
	}

}
