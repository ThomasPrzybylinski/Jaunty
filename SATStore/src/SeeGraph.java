import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import task.formula.AllRectangles;
import task.formula.QueensToSAT;
import util.IntPair;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;


public class SeeGraph {
	public static void main(String[] args) throws Exception {
		ModelGiver mg =  new CNFCreatorModelGiver(new QueensToSAT(8));
		ClauseList models = new ClauseList(VariableContext.defaultContext);
		models.addAll(mg.getAllModels(VariableContext.defaultContext));
		
		EdgeManipulator e1 = new AgreementLocalSymAdder();
		
		PossiblyDenseGraph<int[]> g1 = new PossiblyDenseGraph<int[]>(models.getClauses());
		
		e1.addEdges(g1,models);

		System.out.print(" \t ");
		for(int k = 0; k < models.size(); k++) {
			System.out.print(k+ " ");
		}
		
		System.out.println();
		
		for(int k = 0; k < models.size(); k++) {
			System.out.print(k+ "\t ");
			for(int i = 0; i < models.size(); i++) {
				if(g1.areAdjacent(k,i)) {
					System.out.print(1+ " ");
				} else {
					System.out.print(0+ " ");
				}
			}
			System.out.println();
		}
		
		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				if(g1.areAdjacent(k,i)) continue;
				for(int j = i+1; j < models.size(); j++) {
					if(!g1.areAdjacent(k,j) && !g1.areAdjacent(i,j)) {
						System.out.println(k + " " + i + " " + j);
					}
				}
			}
		}
	}
}
