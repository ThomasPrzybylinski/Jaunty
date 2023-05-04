import java.util.List;

import org.sat4j.specs.ContradictionException;

import formula.simple.CNF;
import task.formula.PigeonHoleCreator;
import task.sat.ClausalIntersectionSolver;


public class ClausalIntersectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//		CNF cnf = new CNF(VariableContext.defaultContext);
		//
		//		cnf.addClause(1,-2,-3);
		//		cnf.addClause(1,-2,3);
		//		cnf.addClause(-1,-2,4);
		//		cnf.addClause(1,-2,5);
		//		
		//		cnf.addClause(1,3,4);
		//		cnf.addClause(-1,3,5);
		//		cnf.addClause(-1,-4);
		//		cnf.addClause(1,4,5);
		//		cnf.addClause(-1,-5);
		//		
		//		cnf.addClause(2,-3,-4);
		//		cnf.addClause(2,-3,4);
		//		cnf.addClause(-2,3,-5);
		//		cnf.addClause(2,-3,-5);
		//		cnf.addClause(2,4,-5);
		//		cnf.addClause(2,4,5);
		//		cnf.addClause(3,4,-5);




//		SimpleCNFCreator creat = new SimpleCNFCreator(8,5,3);
//		for(int k = 0; k < 1000; k++) {
			//CNF cnf = new CNF(creat.nextFormulaImpl()).reduce();
			CNF cnf = PigeonHoleCreator.createPigeonHole(4,3);

			List<int[]> cover = ClausalIntersectionSolver.getCover(cnf);
			CNF cnf2 = new CNF(cnf.getContext());
			cnf2.addAll(cover);
			cnf = cnf2.trySubsumption();
			
//			System.out.println(cnf.reduce().toNumString());
//
//			System.out.println(ClausalIntersectionSolver.isSat(cnf));
//			System.out.println(cnf.getSolverForCNF().isSatisfiable());
			
			boolean sat1 = ClausalIntersectionSolver.isSat(cnf);
			boolean sat2 = false;
			
			try {
				sat2 = cnf.getSolverForCNF().isSatisfiable();
			} catch(ContradictionException e) {
				sat2 = false;
			}
			
			if(sat1 != sat2) {
				ClausalIntersectionSolver.isSat(cnf);
				System.out.println(cnf.toNumString());
				System.out.println(sat1);
				System.out.println(sat2);
				System.out.println();
			}
			
		}
	

//	}

}
