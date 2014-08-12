package task.sat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
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

	public static int[] getRejection(int[] firstModel) {
		int[] i = new int[firstModel.length];

		for(int k = 0; k < i.length; k++) {
			i[k] = -firstModel[k];
		}

		return i;
	}

	//	From:
	//	Near-Uniform Sampling of Combinatorial Spaces
	//	Using XOR Constraints
	//	Carla P . Gomes Ashish Sabharwal Bart Selman
	public static int[] getRandomModel(CNF cnf, Random rand, int s) throws TimeoutException {
//		solver.setDBSimplificationAllowed(false);
		int curNumVars = cnf.getContext().size();//solver.nVars();
		List<IConstr> addedConstraints = new ArrayList<IConstr>();
		boolean singleModel = false;
		int[] model = null;

		while(!singleModel) {
			ISolver solver = null;
			try {
				solver = cnf.getSolverForCNFEnsureVariableUIDsMatch();
				for(int k = 0; k < s; k++) {

					addXORConstraint(solver,curNumVars,rand,addedConstraints);

				}
			} catch(ContradictionException ce) {
				break;
			}
			model = solver.findModel();

			if(model != null) {
				try {
					addedConstraints.add(solver.addClause(new VecInt(getRejection(model))));
				} catch(ContradictionException ce) {
					singleModel = true;
				}

				if(!singleModel) {
					int[] otherModel = solver.findModel();//null;//
					singleModel = (otherModel == null);

					if(!singleModel) {
						//						System.out.println(Arrays.toString(model));
						//						System.out.println(Arrays.toString(otherModel));
						//						System.out.println();
					}
				}
			}

			if(!singleModel) model = null;

//			for(IConstr c : addedConstraints) {
//				solver.removeConstr(c);
//			}
//			solver.newVar(curNumVars);
			addedConstraints.clear();
		}

		int[] ret = new int[curNumVars];
		System.arraycopy(model,0,ret,0,ret.length); //Don't want to include added ones
		
		return ret;
	}

	private static int num = 0;
	private static void addXORConstraint(ISolver solver, int curNumVars,
			Random rand, List<IConstr> addedConstraints) throws ContradictionException {
		List<Integer> chosen = new ArrayList<Integer>(curNumVars/2 + 10);
		num++;
		while(chosen.size() == 0) {
			for(int k = 0; k < curNumVars; k++) {
				if(rand.nextBoolean()) {
					chosen.add(k+1);
				}
			}
			if(chosen.size() == 1) {
				chosen.clear(); //Can rarely happen, and does not work well for SAT4J
			}
		}

		boolean odd = rand.nextBoolean(); //Do we want odd or even parity?
		List<Integer> curVars = chosen;

		int varBase = solver.nVars();

		while(curVars.size() > 3) {
			solver.newVar(solver.nVars()+(curVars.size()/2));
			List<Integer> nextVars = new ArrayList<Integer>(chosen.size()/2 + 1);
			for(int k = 0; k < curVars.size()/2; k++) {
				int v1 = curVars.get(2*k);
				int v2 = curVars.get(2*k+1);
				int resVar = varBase+(k+1); //Encodes the result of the XOR

				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,-v2,-resVar})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,v2,resVar})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,-v2,resVar})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,v2,-resVar})));

				nextVars.add(resVar);
			}

			if(curVars.size()%2 == 1) {
				nextVars.add(curVars.size()-1);
			}

			varBase = solver.nVars();

			curVars = nextVars;
		}

		//I need to avoid unit clauses so SAT4J can remove the constraints
		//(For some reason can't remove unit clauses, according to the documentation)
		if(curVars.size() == 3) {
			int v1 = curVars.get(0);
			int v2 = curVars.get(1);
			int v3 = curVars.get(2);

			if(odd) {
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,-v2,-v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,v2,-v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,-v2,v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,v2,v3})));
			}
			else {
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,v2,v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,-v2,v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,v2,-v3})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,-v2,-v3})));
			}
		} else if(curVars.size() == 2) {
			int v1 = curVars.get(0);
			int v2 = curVars.get(1);
			if(odd) {
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,-v2})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,v2})));
			} else {
				addedConstraints.add(solver.addClause(new VecInt(new int[]{v1,-v2})));
				addedConstraints.add(solver.addClause(new VecInt(new int[]{-v1,v2})));
			}
		} else {
			throw new RuntimeException("Incorrect XOR addition");
		}
	}

}
