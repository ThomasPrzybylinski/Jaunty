package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

//DOES NOT ACTUALLY FIND GOOD ECLECTIC SETS. THE OPPOSITE ACTUALLY
public class ConnectedCreator extends EclecSetCoverCreator {
	private  ClosenessFinder closeFinder;

	public ConnectedCreator(ClosenessFinder closeEdgeFinder) {
		this.closeFinder = closeEdgeFinder;
	}

	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		boolean[] used = new boolean[pdg.getNumNodes()];
		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();

		for(int k = 0; k < used.length; k++) {
			if(!used[k]) {
				List<Integer> toAdd = findNeighbors(pdg,used,k);
				ret.add(toAdd);
			}
		}

		return ret;
	}

	private List<Integer> findNeighbors(PossiblyDenseGraph<int[]> pdg, boolean[] used, int k) {
		ArrayList<Integer> toAdd = new ArrayList<Integer>();

		toAdd.add(k);
		used[k] = true;
			
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(i != k && closeFinder.areTooClose(k,i)) {
					toAdd.add(i);
				}
			}
		return toAdd;
	}

	@Override
	public String getDirLabel() {
		return super.getDirLabel() + "["+ closeFinder.toString() + "]";
	}

	@Override
	public List<Integer> getRandomEclecticSet(
			PossiblyDenseGraph<int[]> pdg) {
		throw new NotImplementedException();
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}
	
	
}
