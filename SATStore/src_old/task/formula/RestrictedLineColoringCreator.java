package task.formula;

import formula.Conjunctions;
import formula.VariableContext;
import formula.simple.CNF;
import graph.LineCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;
import io.GraphColorIO;

import java.io.File;
import java.io.IOException;

import task.formula.random.CNFCreator;
import task.translate.FileDecodable;

public class RestrictedLineColoringCreator implements CNFCreator, FileDecodable {
	private int numNodes;
	private int numColors;
	private Node[] prevGraph;
	
	public RestrictedLineColoringCreator(int numNodes, int numColors) {
		this.numNodes = numNodes;
		this.numColors = numColors;
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		(new GraphColorIO(prevGraph,numColors)).fileDecoding(dir,filePrefix,model);
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);

	}

	@Override
	public CNF generateCNF(VariableContext context) {
		LineCreator creat = new LineCreator();
		Node[] graph = creat.getLine(numNodes); //creat.getLineSkips(numNodes,2,3);//
//		Node[] graph2 = new Node[graph.length+3];
//		
//		System.arraycopy(graph,0,graph2,0,graph.length);
//		
//		graph2[graph.length] = new Node();
//		graph2[graph.length+1] = new Node();
//		graph2[graph.length+2] = new Node();
//		
//		graph2[graph.length].addEdge(graph2[graph.length+1]);
//		graph2[graph.length].addEdge(graph2[graph.length+2]);
//		graph2[graph.length+1].addEdge(graph2[graph.length+2]);
////		
////		for(int k = 1; k < graph.length; k++) {
////			graph2[k].addEdge(graph2[graph.length+(k%3)]);
////		}
//		
//		graph = graph2;
		
		prevGraph = graph;
		Conjunctions color = GraphToColorProblem.coloringAsCNF(graph,numColors);
		
		CNF cnf = new CNF(color);
		
		for(int k = 0; k <= 0; k++) {
			cnf.addClause(k + (k*numColors) + 1); //kth node has kth color
		}
		
		while(context.getNumVarsMade() < cnf.getContext().getNumVarsMade()) {
			context.createNextDefaultVar();
		}
		
		cnf.setContext(context);
		
		return cnf;
	}

	
	
}
