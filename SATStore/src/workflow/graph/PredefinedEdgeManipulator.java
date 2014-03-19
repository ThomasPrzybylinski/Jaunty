package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class PredefinedEdgeManipulator extends EdgeManipulator {
	private PossiblyDenseGraph<int[]> predefined;

	public PredefinedEdgeManipulator(PossiblyDenseGraph<int[]> predefined) {
		this.predefined = predefined;
	}


	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g,
			ClauseList orig) {
		assert(g.getNumNodes() == predefined.getNumNodes());

		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i< g.getNumNodes(); i++) {
				if(predefined.areAdjacent(k,i)) {
					float val;
					if(g.areAdjacent(k,i)) {
						val = Math.min(g.getEdgeWeight(k,i),predefined.getEdgeWeight(k,i));
					} else { 
						val = predefined.getEdgeWeight(k,i);
					}
					g.setEdgeWeight(k,i,val);
				}
			}
		}
	}

	@Override
	public boolean isSimple() {
		return true;
	}
}