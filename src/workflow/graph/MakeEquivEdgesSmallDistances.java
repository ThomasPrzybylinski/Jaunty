package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

public class MakeEquivEdgesSmallDistances extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		float min = Float.MAX_VALUE;
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				float dist = g.getEdgeWeight(k,i);
				if(dist > 0) {
					min = Math.min(min,dist);
				}
			}
		}
		if(min == Float.MAX_VALUE) {
			min = 2;
		}
		
		float equivDist = min/2F;
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				float dist = g.getEdgeWeight(k,i);
				if(dist <= 0 && dist != Float.NEGATIVE_INFINITY) {
					g.setEdgeWeight(k,i,equivDist);
				}
			}
		}
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
	
	public String toString() {
		return "EquivMadeSmall";
	}

}
