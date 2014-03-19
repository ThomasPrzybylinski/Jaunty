package workflow.graph;

import graph.PossiblyDenseGraph;

import java.util.List;

import workflow.eclectic.ClosenessFinder;

public class CreateUneclecGraph extends EdgeManipulator {
	public ClosenessFinder cf;
	
	public CreateUneclecGraph(ClosenessFinder cf) {
		this.cf = cf;
	}

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
		cf.setPdg(g);
		cf.initialize();
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				if(cf.areTooClose(k,i)) {
					g.setAdjacent(k,i,false);
				}
			}
		}
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
}
