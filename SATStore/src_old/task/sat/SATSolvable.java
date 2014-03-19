package task.sat;

import org.sat4j.specs.ISolver;

public interface SATSolvable {
	public ISolver getSATSolverForFormula();
}
