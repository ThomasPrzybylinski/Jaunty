package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;

public class GlobalSymmetryEdges extends EdgeManipulator {

	//Adds an edge of weight -1 if models are globally symmetric
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
		ClauseList dual = SymmetryUtil.getInverseList(representatives,representatives.get(0).length);
		SimpleSymFinder ssf = new SimpleSymFinder(dual);
		
		DisjointSet<Integer> symOrbits = ssf.getSymOrbits();
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				if(symOrbits.sameSet(k+1,i+1)) {
					g.setEdgeWeight(k,i,-0);
				}
			}
		}
		
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

}
