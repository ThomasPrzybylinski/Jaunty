package workflow.tests.prototypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntUnaryOperator;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
import task.formula.AllTrees;
import task.formula.CycleColoringCreator;
import task.formula.DistinctModelsCNF;
import task.formula.LineColoringCreator;
import task.formula.MonotonicPath;
import task.formula.PigeonHoleCreator;
import task.formula.QueensToSAT;
import task.formula.ReducedLatinSquareCreator;
import task.formula.RelaxedPigeonHoleCreator;
import task.formula.SimpleLatinSquareCreator;
import task.formula.SomeFilledRectangles;
import task.formula.SpaceFillingCycles;
import task.formula.plan.BlocksWorldDeconstruct;
import task.formula.random.RandLitFreqBoolFormula;
import task.formula.random.Simple3SATCreator;
import task.formula.random.SimpleCNFCreator;
import task.sat.SATUtil;
import task.symmetry.local.LocalSymClauses;
import util.IntPair;
import util.lit.LitUtil;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.CNFCreatorNonModelGiver;
import workflow.ModelGiver;
import workflow.graph.prototype.AgreementConstructionConnectedAdder;

public class ConstructiveDimension {

	public static void main(String[] args) throws TimeoutException,ContradictionException {
		int start = 8;
		int end = 8;
		
		AgreementConstructionConnectedAdder canConn = new AgreementConstructionConnectedAdder(true);
		List<IntPair> modelDim = new ArrayList<>(end-start);
		
		System.out.println("Value, NumModels, numVars, minConnectedCompLevel, ratio");
		
		for(int val = start; val <=end; val++) {

			VariableContext curContext = new VariableContext();
			//ModelGiver modelGiver = new AllFilledSquares(val);
			//ModelGiver modelGiver = new SomeFilledRectangles(val, 1, 1, val, 1, 1);
			//ModelGiver modelGiver = new RandLitFreqBoolFormula(val, val, 1);
			/*int[][] bw = new int[2][];
			bw[0] = new int[val];
			bw[1] = new int[2];
			Arrays.setAll(bw[0], IntUnaryOperator.identity());
			Arrays.setAll(bw[1], IntUnaryOperator.identity());
			ModelGiver modelGiver = new CNFCreatorModelGiver(new BlocksWorldDeconstruct(bw));*/
			ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(val));
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new SimpleCNFCreator(val, 4.3, 3, 1));
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(3, val));  //The ratio is consistent for all but val=3 which seems to converge to 1/3 as line size increases
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(val, 3));
		
			List<int[]> curModels = modelGiver.getAllModels(curContext); 
			if(curModels.size() == 0) {
				//System.out.println(val + ", " + 0 + ", " + curContext.size() + ", - , " + "-");
				continue;
			}

			
			Collections.sort(curModels, new MILEComparator());
			
			ClauseList models = new ClauseList(curContext);
			models.fastAddAll(curModels);
			
			getChoices(models);
			
			PossiblyDenseGraph<int[]> graph = new PossiblyDenseGraph<>(curModels);

			canConn.addEdges(graph, models);
			
			modelDim.add(new IntPair(curContext.size(), canConn.getMinConnectedSize()));
			
			double ratio = canConn.getMinConnectedSize()/(double)curContext.size();
			System.out.println("------------------------");
			System.out.print(val + ", " + curModels.size() + ", " + curContext.size());
			System.out.println(", " + canConn.getMinConnectedSize()  + ", " + ratio);
			System.out.println("------------------------");
		}
		
		System.out.println();
		for(IntPair ip : modelDim) {
			System.out.println(ip);
		}

	}

	private static void getChoices(ClauseList models) throws ContradictionException, TimeoutException {
		if(models.size() == 0) return;
		System.out.println(models);
		
		int numOrigVars = models.getClauses().get(0).length;
		DistinctModelsCNF distinct = new DistinctModelsCNF(models);
		CNF dCNF = distinct.generateCNF(new VariableContext());
		
		//System.out.println(dCNF);
		
		ISolver solver = dCNF.getSolverForCNF();
		
		int[] allLits = new int[2*numOrigVars];
		
		for(int k = 1; k <= 2*numOrigVars; k++) {
			allLits[k-1] = k;
		}
		
		VecInt newLits = new VecInt(allLits);
		
		int max = numOrigVars; //All positive lits always uniquely identifies a model
		int min = (int)Math.ceil(Math.log(models.size())/Math.log(2));
		boolean sat = false;
		boolean solveAgain = true;
		
		while(min < max || solveAgain) {
			int pivot = max-1;//(min+max+1)/2;
			System.out.println(min+","+pivot+","+max);
			IConstr litConstr = solver.addAtMost(newLits, pivot);
			
			int numPositive = Integer.MAX_VALUE;
			if(solver.isSatisfiable()) {
				System.out.println("sat");
				int[] model = solver.model();

				numPositive = 0;
				for(int i = 0; i < 2*numOrigVars; i++) {
					if(model[i] > 0) numPositive++;
				}
				
				if(min != max) max = Math.min(pivot-1, numPositive);
				sat = true;
				
				solveAgain = min < pivot;
			} else {
				System.out.println("unsat");
				min = pivot+1;
				sat = false;
				solveAgain = true;
			}
			
			if(min != max || solveAgain) {
				solver.removeConstr(litConstr);
			}
		}
		
		ModelIterator iter = new ModelIterator(solver);
		if(iter.isSatisfiable()) { //Just using iter since easy to change to while if I want
			int[] choices = iter.model();
			
			System.out.println(Arrays.toString(choices));
			int[] varsWeCareAbout = new int[2*numOrigVars];
			for(int i = 0; i < varsWeCareAbout.length; i++) {
				varsWeCareAbout[i] = choices[i];
			}
			System.out.println(Arrays.toString(varsWeCareAbout));
			
			ClauseList cl = new ClauseList(dCNF.getContext());
			cl.fastAddClause(varsWeCareAbout);
			System.out.println(cl);
			LinkedList<Integer> theChoices = new LinkedList<>();
			for(int k = 0; k < varsWeCareAbout.length; k++) {
				if(varsWeCareAbout[k] > 0) {
					theChoices.add(distinct.getOriginalLitFromCNF(varsWeCareAbout[k]));
				}
			}
			System.out.println(theChoices);
		}

		System.out.println();
	}
}
