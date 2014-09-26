package workflow.eclectic;

import formula.VariableContext;
import formula.simple.CNF;
import graph.PossiblyDenseGraph;

import java.util.LinkedList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import task.formula.MaximalIndependentSetCreator;

public class MaximumIndependentSetCreator extends EclecSetCoverCreator {

	private  ClosenessFinder closeFinder;

	public MaximumIndependentSetCreator(ClosenessFinder closeEdgeFinder) {
		this.closeFinder = closeEdgeFinder;
	}

	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();

		CNF maximal = (new MaximalIndependentSetCreator(pdg,closeFinder)).generateCNF(VariableContext.defaultContext);
		boolean[] used = new boolean[pdg.getNumNodes()];
		LinkedList<List<Integer>> ret = new LinkedList<List<Integer>>();

		for(int k = 0; k < used.length; k++) {
			if(!used[k]) {
				CNF maximalWithK = new CNF(maximal.getContext());
				maximalWithK.addAll(maximal.getClauses());
				maximalWithK.addClause((k+1));
				List<Integer> toAdd = findIndependentSet(maximalWithK);
				if(toAdd != null) {
					ret.add(toAdd);
				}
			}
		}

		return ret;
	}

	private List<Integer> findIndependentSet(CNF maximalWithK) {
		int[] maxModel = null;
		int maxNumVars = -1;

		try {

			int[] vars = new int[maximalWithK.getContext().size()];

			for(int k = 0; k < vars.length; k++) {
				vars[k] = k+1;
			}

			int topNum = vars.length;
			int botNum = 0;

			while(topNum != botNum) {
				IVecInt allVars = new VecInt(vars);

				ISolver solver = maximalWithK.getSolverForCNF();
				int num =(topNum + botNum)/2;
				solver.addAtLeast(allVars,num);

				if(solver.isSatisfiable()) {
					int[] m = solver.model();
					int curNumVars = 0;
					for(int i : m) {
						if(i > 0) maxNumVars++;
					}

					if(curNumVars > maxNumVars) {
						maxNumVars = curNumVars;
						botNum = curNumVars;
						maxModel = m;
					}

				} else {
					topNum = num-1;
				}
			}
		}catch(Exception e) {
			return null;
		}

		if(maxModel != null) {
			List<Integer> ret = new LinkedList<Integer>();
			for(int k = 0; k < maxModel.length; k++) {
				if(maxModel[k] > 0) {
					ret.add(k);
				}
			}

			return ret;
		}

		return null;
	}
	
	@Override
	public List<Integer> getRandomEclecticSet(
			PossiblyDenseGraph<int[]> pdg) {
		throw new NotImplementedException();
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}
	
	@Override
	public double getEclecticSetScore(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}

}
