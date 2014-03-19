package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompoundEdgeManipulator extends EdgeManipulator {
	private LinkedList<EdgeManipulator> manipulators;
	
	public CompoundEdgeManipulator(EdgeManipulator... manipulators) {
		this.manipulators = new LinkedList<EdgeManipulator>();
		
		for(EdgeManipulator m : manipulators) {
			this.manipulators.add(m);
		}
	}
	
	public CompoundEdgeManipulator(Iterable<EdgeManipulator> manipulators) {
		this.manipulators = new LinkedList<EdgeManipulator>();
		
		for(EdgeManipulator m : manipulators) {
			this.manipulators.add(m);
		}
	}
	
	
	
	public List<EdgeManipulator> getManipulators() {
		return Collections.unmodifiableList(manipulators);
	}

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList representatives) {
		for(EdgeManipulator em : manipulators) {
			em.addEdges(g,representatives);
		}
	}
	
	@Override
	public boolean isSimple() {
		for(EdgeManipulator em : manipulators) {
			if(!em.isSimple()) return false;
		}
		
		return true;
	}

}
