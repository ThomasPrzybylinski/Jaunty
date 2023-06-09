package task.formula.random;

import java.util.HashSet;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;
import formula.simple.CNF;
import task.formula.FormulaCreator;
import task.sat.SATSolvable;

//Not thread safe
public class SimpleCNFCreator extends FormulaCreator implements SATSolvable, CNFCreator {
	private static Random rand = new Random();
	private double clauseVarRatio;
	private int clauseSize;
	
	private ISolver solver = null;
	public  int[][] thing = null;
	
	public SimpleCNFCreator(int numVars, double clauseVarRatio, int clauseSize) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;
		this.clauseSize = clauseSize;
	}
	
	public SimpleCNFCreator(int numVars, double clauseVarRatio, int clauseSize, int seed) {
		super(numVars);
		this.clauseVarRatio = clauseVarRatio;
		this.clauseSize = clauseSize;
		rand.setSeed(seed);
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
		solver.newVar((int)vars.length);
		
		populateSAT(clauseVarRatio,sat,solver);
		sat.setCurContext(this.context);
		//System.out.println();
		return sat;
	}
	
	private void populateSAT(double clauseVarRatio, Conjunctions threeSat, ISolver satSolve) {
		double numVars = (double)vars.length;
		
		thing = new int[(int)Math.ceil((numVars*clauseVarRatio))][clauseSize];
		
		for(int i = 0; i < numVars*clauseVarRatio; i++) {
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

	@Override
	public CNF generateCNF(VariableContext context) {
		Conjunctions conj = nextFormulaImpl();
		CNF ret = new CNF(conj);
		while(context.size() < conj.getCurContext().size()) {
			context.createNextDefaultVar();
		}
		ret.setContext(context);
		return ret;
	}

}
