import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.AllRectangles;
import util.IntPair;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;


public class SeeDifferences {
	public static void main(String[] args) throws Exception {
		ModelGiver mg =  new AllRectangles(5); //new CNFCreatorModelGiver(new LineColoringCreator(7,3));
		ClauseList models = new ClauseList(VariableContext.defaultContext);
		models.addAll(mg.getAllModels(VariableContext.defaultContext));
		
		EdgeManipulator e1 = new GlobalPruningAllLocalSymAdder(false); // new RealAllLocalSymAddr(true,false,true,false);
		EdgeManipulator e2 = new AgreementLocalSymAdder();
		
		PossiblyDenseGraph<int[]> g1 = new PossiblyDenseGraph<int[]>(models.getClauses());
		PossiblyDenseGraph<int[]> g2 = new PossiblyDenseGraph<int[]>(models.getClauses());
		
		e1.addEdges(g1,models);
		e2.addEdges(g2,models);		
		(new GlobalSymmetryEdges()).addEdges(g2,models);
		
		List<IntPair> notIn2 = new LinkedList<IntPair>();
		
		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				if(g1.areAdjacent(k,i) && !g2.areAdjacent(k,i)) {
					notIn2.add(new IntPair(k,i));
					System.out.println("(" + k + "," + i + ")");
					System.out.println(Arrays.toString(models.getClauses().get(k)));
					System.out.println(Arrays.toString(models.getClauses().get(i)));
					System.out.println();
				}
			}
		}
	}
}
