package task.formula.random;

import java.util.Random;

import task.formula.FormulaCreator;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class WeakTrueBoolFormula extends FormulaCreator {
	private int numTrue;
	private static final Random rand = new Random();
	
	
	public WeakTrueBoolFormula(int numVars, int numTrue) {
		super(numVars);
		this.numTrue = numTrue;
	}

	@Override
	public Disjunctions nextFormulaImpl() {
		Disjunctions dnf = new Disjunctions();
		for(int k = 0; k < numTrue; k++) {
			Conjunctions clause = new Conjunctions();
			for(int i = 0; i < vars.length; i++) {
				Literal toAdd;
				if(rand.nextInt(2) == 0) {
					toAdd = vars[i].getNegLit();
				} else {
					toAdd = vars[i].getPosLit();
				}
				clause.add(toAdd);
			}
			dnf.add(clause);
		}
		return dnf;
	}

	

}
