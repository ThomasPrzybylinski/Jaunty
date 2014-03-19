package task.sat;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import formula.simple.CNF;

public class SATUtil {
	
	public static List<int[]> getAllModels(CNF cnf) throws ContradictionException, TimeoutException {
		ISolver solver = cnf.getSolverForCNF();
		ModelIterator iter = new ModelIterator(solver);
		
		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		while(iter.isSatisfiable()) {
			ret.add(iter.model());
		}
		
		return ret;
	}

}
