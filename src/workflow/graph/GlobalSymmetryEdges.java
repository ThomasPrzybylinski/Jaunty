package workflow.graph;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import workflow.graph.local.PositiveChoices;

public class GlobalSymmetryEdges extends ReportableEdgeAddr {
	private static boolean PRINT = true;
	
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
		
		if(PRINT) {
			PositiveChoices pc = new PositiveChoices();
			
			System.out.println("Models  : " + pc.getList(orig));
			System.out.println("Var Syms: " + lg);
			System.out.println("Mod Syms: " + models.reduce());
			System.out.println((new SchreierVector(lg)).transcribeOrbits());
			System.out.println(symOrbits.transcribeOrbits(false));
		}
		
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public int getIters() {
		return 1;
	}

	@Override
	public long getNumUsefulModelSyms() {
		return -1;
	}
	
	public String toString() {
		return "GlobalSym";
	}

}
