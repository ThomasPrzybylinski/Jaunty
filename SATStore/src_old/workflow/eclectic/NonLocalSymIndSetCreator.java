package workflow.eclectic;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.LinkedList;
import java.util.List;

import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import util.LitsMap;

public class NonLocalSymIndSetCreator extends EclecSetCoverCreator {
	private  IndependentSetCreator actual;

	public NonLocalSymIndSetCreator(ClosenessFinder closeEdgeFinder) {
		this.actual = new IndependentSetCreator(closeEdgeFinder);
	}
	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		List<List<Integer>> toPrune = actual.getEclecticSetCover(pdg);

		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();

		for(List<Integer> set : toPrune) {
			List<Integer> newSet = prune(set,pdg);
			
			if(//newSet.size() != set.size() && 
					newSet.size() > 1) {
				newSet.addAll(set);
				//ret.add(newSet);	//For visualization purposes
			}

		}

		return ret;
	}

	
	private List<Integer> prune(List<Integer> cur, PossiblyDenseGraph<int[]> pdg) {
		int numVars = pdg.getElt(0).length;
		ClauseList cl = new ClauseList(new VariableContext());

		LitsMap<Integer> intRep = new LitsMap<Integer>(numVars);
		LitsMap<Integer> listInd = new LitsMap<Integer>(numVars);

		int index = 0;
		for(int k : cur) {
			int[] elt = pdg.getElt(k);
			intRep.put(elt,k);
			listInd.put(elt,index);
			cl.addClause(elt);
			index++;
		}

		boolean[] toRemove = new boolean[cur.size()];
		boolean removed = true;

		while(removed && cl.getClauses().size() > 1) {
			removed = false;
			DisjointSet<int[]> sets = SymmetryUtil.findSymmetryOrbitsNEW(cl);


			for(int k = 0; k < cl.getClauses().size(); k++) {
				int[] elt1  = cl.getClauses().get(k);
				for(int i = k+1; i < cl.getClauses().size(); i++) {
					int[] elt2  = cl.getClauses().get(i);

					if(sets.sameSet(elt1,elt2)) {
						toRemove[listInd.get(elt2)] = true;
						removed = true;
					}
				}
			}

			ClauseList next = new ClauseList(cl.getContext());
			for(int k = 0; k < cur.size(); k++) {
				if(!toRemove[k]) {
					next.addClause(pdg.getElt(cur.get(k)));
				}
			}

			cl = next;
		}
		
		LinkedList<Integer> ret = new LinkedList<Integer>();
		
		for(int[] i : cl.getClauses()) {
			ret.add(intRep.get(i));
		}
		
		return ret;
	}
}
