package task;

import org.sat4j.specs.ISolver;

import formula.BoolFormula;
import task.formula.random.Simple3SATCreator;

public class SATTimeTask implements FormulaTask {

	private Simple3SATCreator fc;
	
	private long totalMiilis = 0;
	private long totalNum = 0;
	
	private long lastTime = 0;
	private boolean lastDone = false;
	
	public SATTimeTask(Simple3SATCreator fc) {
		this.fc = fc;
	
	}
	@Override
	public String aggregateReport() {
		return totalNum + " in " + totalMiilis;
	}

	@Override
	public String executeReport() {
		return lastDone + " in " + lastTime;
	}

	@Override
	public void executeTask(BoolFormula formula) {
		ISolver solv = fc.getSATSolverForFormula();
		
		long start = System.currentTimeMillis();
		boolean solved  = false;
		try {
			solved = solv.isSatisfiable();
		} catch(Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		
		totalNum++;
		//if(solved) {
		
			totalMiilis += (end-start);
		//}
		lastDone = solved;
		lastTime = (end-start);
		
		

	}

}
