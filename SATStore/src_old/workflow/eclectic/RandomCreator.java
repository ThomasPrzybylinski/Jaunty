package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


//Finds random sets for comparison purposese
public class RandomCreator extends EclecSetCoverCreator {

	private static Random rand = new Random();
	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		
		boolean[] used = new boolean[pdg.getNumNodes()];
		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();

		for(int k = 0; k < used.length; k++) {
			if(!used[k]) {
				List<Integer> toAdd = findSet(pdg,used,k);
				ret.add(toAdd);
			}
		}

		return ret;
	}
	
	private List<Integer> findSet(PossiblyDenseGraph<int[]> pdg, boolean[] used, int k) {
		int num = (int)Math.max(2, rand.nextInt((int)Math.sqrt(pdg.getNumNodes())));
		
		Set<Integer> actualNums = new TreeSet<Integer>();
		
		for(int i = 0; i < num; i++) {
			actualNums.add(rand.nextInt(pdg.getNumNodes()));
		}
		
		List<Integer> ret = new ArrayList<Integer>(actualNums.size());
		ret.add(k);
		for(int i : actualNums) {
			used[i] = true;
			ret.add(i);
		}
		
		return ret;
		
	}

}
