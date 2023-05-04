package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

import task.clustering.ModelDistance;

public class DistanceEdges extends EdgeManipulator {
	private ModelDistance distance;
	
	public DistanceEdges(ModelDistance measure) {
		distance = measure;
	}

	//Warning: if used incorrectly can overwrite previous edges.
	//The EclecWorkflow does the edges is such a way that it will not overwrite smaller edges
	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);
				g.setEdgeWeight(k,i,(float)distance.distance(rep1,rep2));
			}
		}

	}
	
	@Override
	public boolean isSimple() {
		return true;
	}
	
	public String toString() {
		return "Distance:"+distance.toString();
	}

}
