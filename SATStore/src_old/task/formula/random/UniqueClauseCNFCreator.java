package task.formula.random;

import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.util.MathUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.FormulaCreator;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class UniqueClauseCNFCreator extends FormulaCreator {

	private int numClauses;
	private int clauseSize;
	private BinomialDistribution binDis;
	private static Random rand = new Random();
	private ISolver solver;

	public UniqueClauseCNFCreator(int numVars, int numClauses, int clauseSize) {
		super(numVars);
		this.numClauses = numClauses;
		this.clauseSize = clauseSize;
		binDis = new BinomialDistributionImpl(numClauses,(clauseSize/(double)numVars));
	}

	@Override
	public BoolFormula nextFormulaImpl() {
		Conjunctions sat = new Conjunctions();
		if(solver == null) {
			solver = SolverFactory.newLight();
		} else {
			solver.reset();
		}
		solver.newVar((int)vars.length);

		try {
			populateSAT(sat,solver);
		}catch(MathException me) {
			me.printStackTrace();
		}

		return sat;
	}

	private void populateSAT(Conjunctions threeSat, ISolver satSolve) throws MathException {
		int numVars = vars.length;
		addUniqueDisjunctions(threeSat,satSolve, numVars,0, numClauses);
		
		
		
		for(int i = 0; i < numClauses; i++) {
			int[] clauseForSolve = new int[clauseSize];
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
				satSolve.addClause(new VecInt(clauseForSolve));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}
	}

	private void addUniqueDisjunctions(Conjunctions threeSat, ISolver satSolve,
			int numVars, int varsChosen, int numClauses) throws MathException {
		int numCurVar = binDis.inverseCumulativeProbability(rand.nextDouble());
		double maxClausesWithVar = Math.pow(2,(numVars-varsChosen));
		double maxClausesNoVar = Math.pow(2,(numVars-varsChosen)-1);
		long maxClauses = MathUtils.binomialCoefficient((numVars-varsChosen),numClauses); 
		
	}
}
