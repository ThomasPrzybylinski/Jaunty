package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class NULLEdgeAdder extends EdgeManipulator {

	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g, ClauseList representatives) {
		return;

	}

	@Override
	public boolean isSimple() {
		// TODO Auto-generated method stub
		return true;
	}

}
