package task.formula.random;

import java.util.HashSet;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.FormulaCreator;
import task.sat.SATSolvable;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class SimpleUnique3SATCreator extends FormulaCreator implements SATSolvable{
	private static Random rand = new Random();
	private double clauseVarRatio;
	private ISolver solver = null;
	
	public SimpleUnique3SATCreator(int numVars, double clauseVarRatio) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;

	}
	
	@Override
	public BoolFormula nextFormulaImpl() {
		Conjunctions threeSat = new Conjunctions();
		ISolver satSolve = SolverFactory.newLight();
		satSolve.newVar((int)vars.length);
		
		populateSAT(clauseVarRatio,threeSat,satSolve);
		
		return threeSat;
	}
	
	private void populateSAT(double clauseVarRatio, Conjunctions threeSat, ISolver satSolve) {
		double numVars = (double)vars.length;
		for(int i = 0; i < numVars*clauseVarRatio; i++) {
			int[] clauseForSolve = new int[3];
			HashSet<Integer> prevSeen = new HashSet<Integer>();
			Disjunctions clause = new Disjunctions();

			do {
				for(int j = 0; j < 3; j++) {
					int varInd = -1;
					do {
						varInd = rand.nextInt(vars.length);
					} while(prevSeen.contains(varInd));

					prevSeen.add(varInd);
					clauseForSolve[j] = varInd+1;
					Literal l = context.getVar(varInd+1).getPosLit();
					if(rand.nextInt(2) == 1) {
						clauseForSolve[j] *= -1;
						l = l.negate();
					}

					clause.add(l);
				}
			} while(seenClause(clause,threeSat));
			
			threeSat.add(clause);
			try {
				satSolve.addClause(new VecInt(clauseForSolve));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}
		solver = satSolve;
	}

	
	private boolean seenClause(Disjunctions clause, Conjunctions threeSat) {
		
		for(BoolFormula bf : threeSat.getFormulas()) {
			Disjunctions d = (Disjunctions) bf;
			if(clausesSame(clause, d)) {
				return true;
			}
		}
		return false;
	}

	private boolean clausesSame(Disjunctions clause, Disjunctions d) {
		for(int k = 0; k < clause.getFormulas().size(); k++) {
			if(!d.getFormulas().contains(clause.getFormulas().get(k))) {
				return false;
			}
		}
		return true;
	}

	public ISolver getSATSolverForFormula() {
		return solver;
	}

}
