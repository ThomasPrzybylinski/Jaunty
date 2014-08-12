import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.sat.SATUtil;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.RealSymFinder;
import task.symmetry.SHATTERSymFinder;
import util.lit.DirectedLitGraph;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;


public class TempTests1 {

	public TempTests1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CNFCreator creator = new IdentityCNFCreator("testcnf\\flat200-1.cnf");
		VariableContext context = new VariableContext();
		CNF function = creator.generateCNF(context);
		ISolver fullSolver = function.getSolverForCNFEnsureVariableUIDsMatch();
		ModelIterator iter = new ModelIterator(fullSolver);
				ArrayList<int[]> allModels = new ArrayList<int[]>();;
		
				while(iter.isSatisfiable()) {
					allModels.add(iter.model());
					System.out.println(allModels.size());
				}
				System.out.println(allModels.size());
		fullSolver.reset();
		fullSolver = null;

	}

}
