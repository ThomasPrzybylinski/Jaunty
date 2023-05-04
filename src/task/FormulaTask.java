package task;

import formula.BoolFormula;

public interface FormulaTask {
	public void executeTask(BoolFormula formula);
	public String executeReport(); //Information about the last executeTask
	public String aggregateReport();  //Aggregate information about all of the tasks executed so far
}
