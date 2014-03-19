package workflow.graph;

import graph.PossiblyDenseGraph;

import java.util.List;

public class MakeEquivEdgesSmallDistances extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
		float min = Float.MAX_VALUE;
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				float dist = g.getEdgeWeight(k,i);
				if(dist > 0) {
					min = Math.min(min,dist);
				}
			}
		}
		
		float equivDist = min/2F;
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				float dist = g.getEdgeWeight(k,i);
				if(dist <= 0) {
					g.setEdgeWeight(k,i,equivDist);
				}
			}
		}
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}

}
