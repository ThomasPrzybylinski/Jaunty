package task.formula.random;

import formula.VariableContext;
import formula.simple.CNF;

public interface CNFCreator {
	public CNF generateCNF(VariableContext context);
}
