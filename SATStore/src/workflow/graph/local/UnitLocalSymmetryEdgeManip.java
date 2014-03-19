package workflow.graph.local;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import workflow.graph.EdgeManipulator;

public class UnitLocalSymmetryEdgeManip extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		throw new NotImplementedException();
	}

	
	@Override
	public boolean isSimple() {
		return true;
	}
}
