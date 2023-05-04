package task.formula;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class DoubleChainCreator extends FormulaCreator {


	private int chainLen = 1;

	public DoubleChainCreator(int chainLen) {
		super(2*chainLen+4);
		this.chainLen = chainLen;
	}

	@Override
	public BoolFormula nextFormulaImpl() {
		Conjunctions c = new Conjunctions(new Disjunctions(context.getVar(1).getPosLit()),new Disjunctions(context.getVar(2).getPosLit()));

		for(int k = 3; k <= chainLen; k++) {
			Literal var1 = context.getVar(k-1).getNegLit();
			Literal var2 = context.getVar(k-2).getNegLit();
			Literal var3 = context.getVar(k).getPosLit();
			c.add(new Disjunctions(var1,var2,var3));
		}

		c.add(new Disjunctions(context.getVar(chainLen+1).getNegLit()));
		c.add(new Disjunctions(context.getVar(chainLen+2).getNegLit()));
		for(int k = chainLen+3; k <= 2*chainLen; k++) {
			Literal var1 = context.getVar(k-1).getPosLit();
			Literal var2 = context.getVar(k-2).getPosLit();
			Literal var3 = context.getVar(k).getNegLit();
			c.add(new Disjunctions(var1,var2,var3));
		}

		return c;
	}

}
