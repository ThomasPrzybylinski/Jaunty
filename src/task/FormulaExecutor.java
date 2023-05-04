package task;

import java.util.ArrayList;

import task.formula.FormulaCreator;
import formula.BoolFormula;

public class FormulaExecutor {
	private FormulaCreator fc;
	private ArrayList<FormulaTask> tasks = new ArrayList<FormulaTask>();
	private int iters;
	
	public FormulaExecutor(FormulaCreator fc, FormulaTask ft, int iters) {
		this.fc = fc;
		this.tasks.add(ft);
		this.iters = iters;
	}
	
	public void addTask(FormulaTask ft) {
		tasks.add(ft);
	}
	
	public void run() {
		for(int k = 0; k < iters; k++) {
			BoolFormula formula = fc.nextFormula();
			for(FormulaTask ft : tasks) {
				ft.executeTask(formula);
				//System.out.println(ft.executeReport());
			}
		}
		for(FormulaTask ft : tasks) {
			System.out.println(ft.aggregateReport());
		}
		
	}
}
