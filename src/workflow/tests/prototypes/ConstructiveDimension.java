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
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(val));
			//ModelGiver modelGiver = new CNFCreatorModelGiver(new SimpleCNFCreator(val, 4.3, 3, 1));
			ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(3, val));  //The ratio is consistent for all but val=3 which seems to converge to 1/3 as line size increases
		
			List<int[]> curModels = modelGiver.getAllModels(curContext); 
			if(curModels.size() == 0) {
				//System.out.println(val + ", " + 0 + ", " + curContext.size() + ", - , " + "-");
				continue;
			}

			System.out.print(val + ", " + curModels.size() + ", " + curContext.size());
			Collections.sort(curModels, new MILEComparator());
			
			ClauseList models = new ClauseList(curContext);
			models.fastAddAll(curModels);
			
			PossiblyDenseGraph<int[]> graph = new PossiblyDenseGraph<>(curModels);

			canConn.addEdges(graph, models);
			
			modelDim.add(new IntPair(curContext.size(), canConn.getMinConnectedSize()));
			
			double ratio = canConn.getMinConnectedSize()/(double)curContext.size();
			System.out.println(", " + canConn.getMinConnectedSize()  + ", " + ratio);
		}
		
		System.out.println();
		for(IntPair ip : modelDim) {
			System.out.println(ip);
		}

	}

}
