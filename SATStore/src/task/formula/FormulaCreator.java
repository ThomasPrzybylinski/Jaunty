package task.formula;

import formula.BoolFormula;
import formula.Variable;
import formula.VariableContext;

public abstract class FormulaCreator {

	protected Variable[] vars;
	protected VariableContext context = new VariableContext();
	
	public FormulaCreator(int numVars) {
		vars = new Variable[numVars];
		for(int i = 0; i < vars.length; i++) {
			Variable b = context.getOrCreateVar(""+(i+1));
			vars[i] = b;
		}
	}
	
	public VariableContext getContext() {
		return context;
	}
	
	public BoolFormula nextFormula() {
		BoolFormula creat = nextFormulaImpl();
		creat.setCurContext(context);
		
		return creat;
	}
	
	public abstract BoolFormula nextFormulaImpl();
	
}
