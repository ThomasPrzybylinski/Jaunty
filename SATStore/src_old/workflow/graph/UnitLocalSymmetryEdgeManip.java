package workflow.graph;

import graph.PossiblyDenseGraph;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class UnitLocalSymmetryEdgeManip extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
		throw new NotImplementedException();
	}

	
	@Override
	public boolean isSimple() {
		return true;
	}
}
