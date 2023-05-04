package workflow.graph;

import java.util.ArrayList;
import java.util.List;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import util.IntPair;
import util.IntegralDisjointSet;

public class CollapseGlobalPartitions extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		RealSymFinder globalSymFinder = new RealSymFinder(orig);
		LiteralGroup group =  globalSymFinder.getSymGroup();
		LocalSymClauses clauses = new LocalSymClauses(orig);
		LiteralGroup modelGroup = clauses.getModelGroup(group);
		SchreierVector vec = new SchreierVector(modelGroup);
		IntegralDisjointSet modelOrbits = vec.transcribeOrbits(false);
		
		List<IntPair> pairsToDelete = new ArrayList<IntPair>();
		
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = 0; i < g.getNumNodes(); i++) {
				if(k == i) continue;
				if(!g.areAdjacent(k,i)) continue;
				if(modelOrbits.getRootOf(k+1)==(k+1) && modelOrbits.getRootOf(i+1)==(i+1)) continue;
				
				pairsToDelete.add(new IntPair(k,i));
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
		return "RemoveNonGlobalRepEdges";
	}

}
