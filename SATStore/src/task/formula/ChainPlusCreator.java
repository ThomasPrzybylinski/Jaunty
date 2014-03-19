package task.formula;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class ChainPlusCreator extends FormulaCreator {
	private int chainLen = 1;
	
	public ChainPlusCreator(int chainLen) {
		super(chainLen+6);
		this.chainLen = chainLen;
	}
	
	@Override
	public BoolFormula nextFormulaImpl() {
		Literal v1 = context.getVar(1).getPosLit();
		Literal v2 = context.getVar(2).getPosLit();
		
		
		Conjunctions c = new Conjunctions(new Disjunctions(v1),new Disjunctions(v2));
		
		for(int k = 3; k <= chainLen; k++) {
			Literal var1 = context.getVar(k-1).getNegLit();
			Literal var2 = context.getVar(k-2).getNegLit();
			Literal var3 = context.getVar(k).getPosLit();
			c.add(new Disjunctions(var1,var2,var3));
		}
		
		
		for(int k = 3; k <= chainLen-1; k++) {
			Literal var1 = context.getVar(k-1).getPosLit();
			Literal var2 = context.getVar(k-2).getPosLit();
			Literal var3 = context.getVar(k).getPosLit();
			c.add(new Disjunctions(var1,var2,var3));
		}
		
		
		return c;
	}

}
