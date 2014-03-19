package workflow.graph;

import graph.PossiblyDenseGraph;

import java.util.List;

import task.clustering.ModelDistance;

public class DistanceEdges extends EdgeManipulator {
	private ModelDistance distance;
	
	public DistanceEdges(ModelDistance measure) {
		distance = measure;
	}

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
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

}
