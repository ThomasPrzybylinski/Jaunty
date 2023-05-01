package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class OppositeOfIndependentSetCreator extends EclecSetCoverCreator {
	private  ClosenessFinder closeFinder;

	public OppositeOfIndependentSetCreator(ClosenessFinder closeEdgeFinder) {
		this.closeFinder = closeEdgeFinder;
	}

	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		boolean[] used = new boolean[pdg.getNumNodes()];
		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();

		for(int k = 0; k < used.length; k++) {
//			if(!used[k]) {
				boolean[] adj = new boolean[pdg.getNumNodes()];
				List<Integer> toAdd = getAllAdj(pdg,used,adj,k,false);
				ret.add(toAdd);
//			}
		}

		return ret;
	}

	private List<Integer> getAllAdj(PossiblyDenseGraph<int[]> pdg, boolean[] used, boolean[] adj, int k, boolean random) {
		ArrayList<Integer> toAdd = new ArrayList<Integer>();
		used[k] = true;
		toAdd.add(k);
		
		for(int i = 0; i < pdg.getNumNodes(); i++) {
			if(i == k) continue;
			if(closeFinder.areTooClose(k,i)) {
				used[i] = true;
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
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		return getAllAdj(pdg,new boolean[pdg.getNumNodes()],new boolean[pdg.getNumNodes()],rand.nextInt(pdg.getNumNodes()),true);
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		for(int k = 0; k < list.size(); k++) {
			int m1 = list.get(k);
			for(int i = k+1; i < list.size(); i++) {
				int m2 = list.get(i);
				if(!closeFinder.areTooClose(m1,m2)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public double getEclecticSetScore(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		int numOk = 0;
		int numTotal = 0;
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		for(int k = 0; k < list.size(); k++) {
			int m1 = list.get(k);
			for(int i = k+1; i < list.size(); i++) {
				numTotal++;
				int m2 = list.get(i);
				if(closeFinder.areTooClose(m1,m2)) {
					numOk++;
				}
			}
		}
		
		return numOk/(double)numTotal;
	}

	@Override
	public boolean verifyEclecticPair(PossiblyDenseGraph<int[]> pdg, int v1, int v2) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		return closeFinder.areTooClose(v1,v2);
	}

	@Override
	public boolean displayUnitSets() {
		return true;
	}
	
	
	
}
