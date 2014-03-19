package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

public class AllEdgeAdder extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i < g.getNumNodes(); i++) {
				g.setAdjacent(k,i);
			}
		}
		
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

}
