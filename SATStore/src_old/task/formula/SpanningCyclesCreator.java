package task.formula;

import formula.Conjunctions;
import formula.VariableContext;
import formula.simple.CNF;
import graph.CompleteGraphCreator;
import graph.Node;
import graph.sat.SpanningCycleGraphProblem;
import io.SpanningCyclesIO;

import java.io.File;
import java.io.IOException;

import task.formula.random.CNFCreator;
import task.translate.FileDecodable;

public class SpanningCyclesCreator implements CNFCreator, FileDecodable {
	private int numNodes;
	private Node[] prevGraph;
	
	public SpanningCyclesCreator(int numNodes) {
		this.numNodes = numNodes;
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		(new SpanningCyclesIO(prevGraph)).fileDecoding(dir,filePrefix,model);
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);

	}

	@Override
	public CNF generateCNF(VariableContext context) {
		Node[] graph = CompleteGraphCreator.getCompleteGraph(numNodes); //creat.getLineSkips(numNodes,2,3);//
		prevGraph = graph;
		Conjunctions color = SpanningCycleGraphProblem.cycleAsCNF(graph);
		
		CNF cnf = new CNF(color);
		
		while(context.getNumVarsMade() < cnf.getContext().getNumVarsMade()) {
			context.createNextDefaultVar();
		}
		
		cnf.setContext(context);
		
		return cnf;
	}

}
