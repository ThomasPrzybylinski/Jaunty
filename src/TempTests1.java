import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import formula.VariableContext;
import formula.simple.CNF;
import group.LiteralGroup;
import subsumptionMain.ResolutionTest;
import subsumptionMain.SATSump;
import task.formula.random.CNFCreator;
import task.formula.random.SimpleCNFCreator;
import task.symmetry.sparse.SparseSymFinder;


public class TempTests1 {

	public TempTests1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int minNumVars = 3;
		int maxNumVars = 100;
		int iters = 1;//Integer.MAX_VALUE;

		for(int i = minNumVars; i <= maxNumVars; i++) {
//			MAX_BRANCHING = (int)Math.max(1,Math.log(i)/Math.log(2));
			CNFCreator creat = new SimpleCNFCreator(i,2.1,3,1);//new TempCNFCreator(i,3);//new IdentityCNFCreator("testcnf\\uf20-02.cnf");//new LineColoringCreator(i,3); //
			System.out.print(i+"\t");

			CNF curBest = null;
			for(int k = 0; k < iters; k++) {
				CNF test = null;
				boolean sat = false;
				while(!sat) {
					test = creat.generateCNF(VariableContext.defaultContext);
//										test = makeAffine(test);
					try {
						ISolver solver = test.getSolverForCNFEnsureVariableUIDsMatch();
						sat = solver.isSatisfiable();
						solver.reset();
					} catch(ContradictionException ce) {}
				}
				test = test.trySubsumption();
				test = SATSump.getPrimify(test);
				System.out.println();
				System.out.println(test);
				test.addAll(ResolutionTest.getResolvants(test));
				test = test.trySubsumption();
				test = SATSump.getPrimify(test);
				SparseSymFinder finder = new SparseSymFinder(test);
				LiteralGroup group = finder.getSymGroup();
				System.out.println(test);
				System.out.println(group);
				System.out.println();
				//				RenameHornUtil.renameToMaximizeTotalNegLits(test);
				//				RenameHornUtil.renameToGreedyMinNonHornPosNumProduct(test);
			}
//			System.out.println(curBest);
//			System.out.println(bestRep);
		}
	}
}
