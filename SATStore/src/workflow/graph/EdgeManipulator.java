package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public abstract class EdgeManipulator {
	//By convention negative edges are "weak equivalence" edges
	public abstract void addEdges(PossiblyDenseGraph<int[]> g, ClauseList representatives);
	//A simple EdgeManipulator only "adds" new edges.
	//That is, we may replace any existing edges with the minimum of the two
	public abstract boolean isSimple();
}
