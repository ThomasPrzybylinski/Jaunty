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

public class LineColoringCreator implements CNFCreator, FileDecodable {
	private int numNodes;
	private int numColors;
	private Node[] prevGraph;
	
	public LineColoringCreator(int numNodes, int numColors) {
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
		prevGraph = graph;
		Conjunctions color = GraphToColorProblem.coloringAsCNF(graph,numColors);
		
		CNF cnf = new CNF(color);
		
		while(context.size() < cnf.getContext().size()) {
			context.createNextDefaultVar();
		}
		
		cnf.setContext(context);
		
		return cnf;
	}

	@Override
	public String toString() {
		return "PathColoring("+"nodes="+numNodes + ",colors="+numColors+")";
	}
	
	

	
	
}
