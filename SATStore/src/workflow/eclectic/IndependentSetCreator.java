package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class IndependentSetCreator extends EclecSetCoverCreator {
	private  ClosenessFinder closeFinder;

	public IndependentSetCreator(ClosenessFinder closeEdgeFinder) {
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
				boolean[] adj = new boolean[pdg.getNumNodes()];
				List<Integer> toAdd = findIndependentSet(pdg,used,adj,k,false);
				ret.add(toAdd);
			}
		}

		return ret;
	}

	private List<Integer> findIndependentSet(PossiblyDenseGraph<int[]> pdg, boolean[] used, boolean[] adj, int k, boolean random) {
		ArrayList<Integer> toAdd = new ArrayList<Integer>();
		boolean moreToAdd = true;
		ArrayList<Integer> unused = new ArrayList<Integer>(used.length);
		ArrayList<Integer> unAdj = new ArrayList<Integer>(used.length);
		Random rand = new Random();

		while(moreToAdd) {
			toAdd.add(k);
			used[k] = true;
			
			for(int i = 0; i < adj.length; i++) {
				if(i == k || (closeFinder.areTooClose(k,i) && !adj[i])) {
					adj[i] = true;
				}
			}

			moreToAdd = false;
			unAdj.clear();
			unused.clear();
			for(int i = 0; i < adj.length; i++) {
				if(!adj[i]) {
					unAdj.add(i);
					moreToAdd = true;
					if(!used[i]) { //We want to use models that aren't in any set, if possible
						unused.add(i);
					}
				}
			}
			
			if(unused.size() > 0) {
				if(random) {
					k = unused.get(rand.nextInt(unused.size()));
				} else {
					k = unused.get(0);
				}
			} else if(unAdj.size() > 0) {
				if(random) {
					k = unAdj.get(rand.nextInt(unAdj.size()));
				} else {
					k = unAdj.get(0);
				}
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
		
		return findIndependentSet(pdg,new boolean[pdg.getNumNodes()],new boolean[pdg.getNumNodes()],rand.nextInt(pdg.getNumNodes()),true);
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
				if(closeFinder.areTooClose(m1,m2)) {
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
		
		if(list.size() == 1) return 1;
		
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		for(int k = 0; k < list.size(); k++) {
			int m1 = list.get(k);
			for(int i = k+1; i < list.size(); i++) {
				numTotal++;
				int m2 = list.get(i);
				if(!closeFinder.areTooClose(m1,m2)) {
					numOk++;
				}
			}
		}
		
		return (numOk+1)/(double)(numTotal+1);
	}
	
	
}
