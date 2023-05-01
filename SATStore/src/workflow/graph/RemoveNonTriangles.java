package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import util.IntPair;

import java.util.ArrayList;
import java.util.List;

public class RemoveNonTriangles extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<IntPair> pairsToDelete = new ArrayList<IntPair>();
		
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i < g.getNumNodes(); i++) {
				if(!g.areAdjacent(k,i)) continue;
				boolean onlyTriangles=true;
				
				for(int triComplete = 0; (triComplete < g.getNumNodes()) && onlyTriangles; triComplete++) {
					if(triComplete==i || triComplete==k) continue;
					boolean kAdj = g.areAdjacent(k,triComplete);
					boolean iAdj = g.areAdjacent(i,triComplete);
					if((kAdj && !iAdj) || (!kAdj && iAdj)) {
						pairsToDelete.add(new IntPair(k,i));
						onlyTriangles=false;
					}
				}
			}
		}
		
		for(IntPair pairToRemove : pairsToDelete) {
			g.setAdjacent(pairToRemove.getI1(),pairToRemove.getI2(),false);
		}
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
	
	public String toString() {
		return "RemoveNonTriangles";
	}

}
