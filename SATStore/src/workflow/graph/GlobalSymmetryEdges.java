package workflow.graph;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;

import java.util.List;

import task.symmetry.RealSymFinder;
import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.BetterSymFinder;
import task.symmetry.local.LocalSymClauses;
import util.DisjointSet;
import util.IntegralDisjointSet;

public class GlobalSymmetryEdges extends EdgeManipulator {

	//Adds an edge of weight -1 if models are globally symmetric
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		LocalSymClauses repr = new LocalSymClauses(orig,false);

		RealSymFinder sym = new RealSymFinder(orig);
		
		LiteralGroup lg = sym.getSymGroup();
		LiteralGroup models = repr.getModelGroup(lg);
		SchreierVector symOrbits = new SchreierVector(models);
		
		
//		BetterSymFinder ssf2 = new BetterSymFinder(dual);
//		
//		IntegralDisjointSet symOrbits2 = ssf2.getSymOrbits();
		
		for(int k = 0; k < orig.size(); k++) {
			for(int i = k+1; i < orig.size(); i++) {
				if(symOrbits.sameOrbit(k+1,i+1)) {
					g.setEdgeWeight(k,i,-0);
				}
			}
		}
		
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

}
