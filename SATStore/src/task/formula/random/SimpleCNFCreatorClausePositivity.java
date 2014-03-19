package task.formula.random;

import java.util.HashSet;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.FormulaCreator;
import task.sat.SATSolvable;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class SimpleCNFCreatorClausePositivity extends FormulaCreator implements SATSolvable{
	private static Random rand = new Random();
	private double clauseVarRatio;
	private int clauseSize;
	
	private ISolver solver = null;
	public  int[][] thing = null;
	
	public SimpleCNFCreatorClausePositivity(int numVars, double clauseVarRatio, int clauseSize) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;
		this.clauseSize = clauseSize;
	}
	
	@Override
	public Conjunctions nextFormulaImpl() {
		Conjunctions sat = new Conjunctions();
		if(solver == null) {
			//solver = SolverFactory.newLight();
			solver = SolverFactory.newDefault();
		} else {
			solver.reset();
		}
		solver.newVar((int)vars.length/2);
		
		populateSAT(clauseVarRatio,sat,solver);
		//System.out.println();
		return sat;
	}
	
	private void populateSAT(double clauseVarRatio, Conjunctions threeSat, ISolver satSolve) {
		double numVars = (double)vars.length/2;
		
		thing = new int[(int)Math.ceil((numVars*clauseVarRatio))][clauseSize];
		
		for(int i = 0; i < numVars*clauseVarRatio; i++) {
			int[] clauseForSolve = new int[clauseSize];
			int numNeg = rand.nextInt(clauseSize+1);
			
			HashSet<Integer> prevSeen = new HashSet<Integer>();
			Disjunctions clause = new Disjunctions();

			for(int j = 0; j < clauseSize; j++) {
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
				//System.out.println(Arrays.toString(clauseForSolve));
				satSolve.addClause(new VecInt(clauseForSolve));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}
		
	}

	
	public ISolver getSATSolverForFormula() {
		return solver;
	}

}
