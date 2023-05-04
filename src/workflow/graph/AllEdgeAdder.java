package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class AllEdgeAdder extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
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
