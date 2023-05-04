package task.formula;

import formula.BoolFormula;
import formula.Not;

public class InverseCreator extends FormulaCreator {
	private FormulaCreator toInvert = null;
	
	public InverseCreator(FormulaCreator toInvert) {
		super(0);
		this.toInvert = toInvert;
		this.vars = toInvert.vars;
	}
	
	@Override
	public BoolFormula nextFormulaImpl() {
		return new Not(toInvert.nextFormula());
	}

}
