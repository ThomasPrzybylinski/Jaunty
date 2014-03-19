package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class ImprovingIndependentSetCreator extends EclecSetCoverCreator {
	private  ClosenessFinder closeFinder;

	public ImprovingIndependentSetCreator(ClosenessFinder closeEdgeFinder) {
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
				List<Integer> toAdd = findIndependentSet(pdg,used,adj,k);
				ret.add(toAdd);
			}
		}

		return ret;
	}

	private List<Integer> findIndependentSet(PossiblyDenseGraph<int[]> pdg, boolean[] used, boolean[] adj, int k) {
		ArrayList<Integer> toAdd = new ArrayList<Integer>();
		boolean moreToAdd = true;
		ArrayList<Integer> unused = new ArrayList<Integer>(used.length);
		ArrayList<Integer> unAdj = new ArrayList<Integer>(used.length);
		Random rand = new Random();

		while(moreToAdd) {
			toAdd.add(k);

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
				k = unused.get(rand.nextInt(unused.size()));
			} else if(unAdj.size() > 0) {
				k = unAdj.get(rand.nextInt(unAdj.size()));
			}

		}

		improveMinDist(pdg, adj, k, toAdd);
		
		for(int i : toAdd) {
			used[i] = true;
		}

		return toAdd;
	}

	private void improveMinDist(PossiblyDenseGraph<int[]> pdg, boolean[] adj,
			int k, ArrayList<Integer> toAdd) {
		boolean improving = true;
		while(improving) {
			double min = Double.MAX_VALUE;

			for(int j = 0; j < toAdd.size(); j++) {
				for(int i = k+1; i < toAdd.size(); i++) {
					if(pdg.areAdjacent(i,j)) {
						min = Math.min(pdg.getEdgeWeight(toAdd.get(i), toAdd.get(j)), min);
					}
				}
			}
			
			if(min <= 0) return; //No sensible distance metric used

			double curMaxMin = -1;
			int curMaxMinInd = -1;
			int curSkip = -1;
			for(int skip = 0; skip < toAdd.size(); skip++) {
				int testSkip = toAdd.get(skip);
				for(int i = 0; i < adj.length; i++) {
					if(testSkip == i) continue;
					
					double thisMin = Double.MAX_VALUE;
					for(int j = 0; j < toAdd.size(); j++) {
						int curRep = toAdd.get(j);
						if(curRep == testSkip) continue;
						
						if(toAdd.get(j).equals(i) || closeFinder.areTooClose(toAdd.get(j),i)) {
							thisMin = -1;
							break;
						} else {
							thisMin = Math.min(pdg.getEdgeWeight(i,curRep),thisMin); 
						}
					}
					
					if(thisMin > curMaxMin) {
						curMaxMin = thisMin;
						curMaxMinInd = i;
						curSkip = skip;
					}
				}
			}
			
			if(curMaxMin > min) {
				for(int i = 0; i < toAdd.size(); i++) {
					if(toAdd.get(i) == curSkip) {
						toAdd.remove(i);
						break;
					}
				}
				
				toAdd.add(curMaxMinInd);
			} else {
				improving = false;
			}

		}
	}

	
	
	@Override
	public List<Integer> getRandomEclecticSet(
			PossiblyDenseGraph<int[]> pdg) {
		throw new NotImplementedException();
	}
	//Recommend using regular Ind. Set. Creator for this.
	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}
}
