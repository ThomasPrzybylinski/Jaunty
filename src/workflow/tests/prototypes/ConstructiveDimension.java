package workflow.tests.prototypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
import task.formula.AllTrees;
import task.formula.CycleColoringCreator;
import task.formula.MonotonicPath;
import task.formula.QueensToSAT;
import task.formula.SimpleLatinSquareCreator;
import task.formula.SomeFilledRectangles;
import task.formula.SpaceFillingCycles;
import task.formula.plan.BlocksWorldDeconstruct;
import task.formula.random.RandLitFreqBoolFormula;
import task.symmetry.local.LocalSymClauses;
import util.IntPair;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.CNFCreatorNonModelGiver;
import workflow.ModelGiver;
import workflow.graph.prototype.AgreementConstructionConnectedAdder;

public class ConstructiveDimension {

	public static void main(String[] args) throws TimeoutException {
		int start = 2;
		int end = 50;
		
		AgreementConstructionConnectedAdder canConn = new AgreementConstructionConnectedAdder(true);
		List<IntPair> modelDim = new ArrayList<>(end-start);
		
		for(int val = start; val <=end; val++) {

			VariableContext curContext = new VariableContext();
			//ModelGiver modelGiver = new AllFilledSquares(val);
			//ModelGiver modelGiver = new SomeFilledRectangles(val, 3, 1, val, 1, 1);
			//ModelGiver modelGiver = new RandLitFreqBoolFormula(val, val, 1);
			int[][] bw = new int[2][];
			bw[0] = new int[val];
			bw[1] = new int[2];
			Arrays.setAll(bw[0], IntUnaryOperator.identity());
			Arrays.setAll(bw[1], IntUnaryOperator.identity());
			ModelGiver modelGiver = new CNFCreatorModelGiver(new BlocksWorldDeconstruct(bw));
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(val));
		
			List<int[]> curModels = modelGiver.getAllModels(curContext); 

			Collections.sort(curModels, new MILEComparator());
			
			ClauseList models = new ClauseList(curContext);
			models.fastAddAll(curModels);
			
			PossiblyDenseGraph<int[]> graph = new PossiblyDenseGraph<>(curModels);

			canConn.addEdges(graph, models);
			
			modelDim.add(new IntPair(curContext.size(), canConn.getMinConnectedSize()));
			
			double ratio = canConn.getMinConnectedSize()/(double)curContext.size();
			System.out.println(val + ", " + models.size() + ", " + ", " + curContext.size() + canConn.getMinConnectedSize()  + ", " + ratio);
		}
		
		System.out.println();
		for(IntPair ip : modelDim) {
			System.out.println(ip);
		}

	}

}
