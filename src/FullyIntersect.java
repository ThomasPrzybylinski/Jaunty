import java.util.HashSet;
import java.util.List;

import org.sat4j.specs.ContradictionException;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.PigeonHoleCreator;
import task.formula.random.SimpleCNFCreator;
import task.sat.ClausalIntersectionSolver;


public class FullyIntersect {
	//Symmetry
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SimpleCNFCreator creat = new SimpleCNFCreator(8,4.6,3);
		CNF cnf = PigeonHoleCreator.createPigeonHole(4,3);

		//CNF cnf = new CNF(creat.nextFormulaImpl()).reduce();
		
		CNF toBeCnf = new CNF(cnf.getContext());
//		List<int[]> laTemp = ClausalIntersectionSolver.getCover(cnf);
//		toBeCnf.addAll(laTemp);
//		cnf = toBeCnf.reduce().trySubsumption();
		System.out.println(cnf.getSolverForCNF().isSatisfiable());
		System.out.println(cnf.toNumString());
		
		List<int[]> clauses = cnf.getClauses();
		
		CNF allIntersections = new CNF(cnf.getContext());

		while(true) {
			clauses = cnf.getClauses();
			CNF newList = new CNF(cnf.getContext());

			for(int k = 0; k < clauses.size(); k++) {
				int[] clause1 = clauses.get(k);
				for(int i = k+1; i < clauses.size(); i++) { 
					int[] clause2 = clauses.get(i);
					int[] intersect = ClausalIntersectionSolver.getIntersection(clause1,clause2);

					if(intersect != null) {
						allIntersections.addClause(intersect);
						newList.addClause(intersect);
					}
				}
			}

			CNF trueNewClauses = new CNF(newList.getContext());

			for(int[] intersection : newList.getClauses()) {
				if(intersection.length == 0) continue;
				CNF nextTest = getIntersectedClauses(intersection,clauses,cnf.getContext());
				trueNewClauses.fastAddAll(nextTest.getClauses());
			}

			trueNewClauses = trueNewClauses.reduce().trySubsumption();
//			CNF temp = new CNF(trueNewClauses.getContext());
//			List<int[]> temp2 = ClausalIntersectionSolver.getCover(trueNewClauses);
//			temp.addAll(temp2);
//			trueNewClauses = temp.reduce().trySubsumption();
			System.out.println(trueNewClauses.getClauses().size());
			System.out.println(trueNewClauses.toNumString());
			if(!cnf.equals(trueNewClauses)) {
				cnf = trueNewClauses;
			} else {
				break;
			}
		}
		System.out.println();
		CNF temp = new CNF(cnf.getContext());
		List<int[]> temp2 = ClausalIntersectionSolver.getCover(cnf);
		temp.addAll(temp2);
		cnf = temp.reduce().trySubsumption();
		System.out.println(cnf.toNumString());
		try {
			System.out.println(cnf.getSolverForCNF().isSatisfiable());
		} catch(ContradictionException ce) {
			System.out.println(false);
		}
		
		temp = new CNF(cnf.getContext());
		temp2 = ClausalIntersectionSolver.getCover(allIntersections);
		temp.addAll(temp2);
		allIntersections = temp.reduce().trySubsumption();
		
		System.out.println(allIntersections.toNumString());
		//System.out.println(allIntersections.getSolverForCNF().isSatisfiable());

	}

	private static CNF getIntersectedClauses(int[] intersection,
			List<int[]> clauses, VariableContext context) {

		CNF ret = new CNF(context);
		HashSet<Integer> lits = new HashSet<Integer>();

		for(int i : intersection) {
			lits.add(i);
		}

		for(int[] cl : clauses) {
			int[] newClause = ClausalIntersectionSolver.getAgreement(lits,cl);


			if(newClause != null) {


				int[] realNewClause = new int[newClause.length+intersection.length];

				int k = 0;
				for(; k < intersection.length; k++) {
					realNewClause[k]= intersection[k];
				}

				for(; k < realNewClause.length; k++) {
					realNewClause[k]= newClause[k-intersection.length];
				}

				ret.addClause(realNewClause);
			}
		}

		return ret;

	}

}
