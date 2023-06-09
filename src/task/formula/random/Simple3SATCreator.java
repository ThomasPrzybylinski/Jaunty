package task.formula.random;

import java.util.HashSet;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import task.formula.FormulaCreator;
import task.sat.SATSolvable;

//Not thread safe
public class Simple3SATCreator extends FormulaCreator implements SATSolvable{
	private static Random rand = new Random();
	private double clauseVarRatio;
	private ISolver solver = null;
	
	public Simple3SATCreator(int numVars, double clauseVarRatio) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;

	}
	
	public Simple3SATCreator(int numVars, double clauseVarRatio, int seed) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;
		rand.setSeed(seed);

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
		double numVars = vars.length;
		for(int i = 0; i < numVars*clauseVarRatio; i++) {
			int[] clauseForSolve = new int[3];
			HashSet<Integer> prevSeen = new HashSet<Integer>();
			Disjunctions clause = new Disjunctions();

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
			threeSat.add(clause);
			try {
				satSolve.addClause(new VecInt(clauseForSolve));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}
		solver = satSolve;
	}

	
	public ISolver getSATSolverForFormula() {
		return solver;
	}

}
