package task.formula;

import java.io.File;
import java.io.IOException;

import formula.VariableContext;
import formula.simple.CNF;
import graph.LineCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;
import io.GraphColorIO;
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class LineColoringCreator implements CNFCreator, FileDecodable, ConsoleDecodeable {
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
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < model.length; k++) {
			if(model[k] > 0) {
				if(sb.length() > 0) sb.append(',');
				sb.append((model[k]-1)%numColors);
			}
		}
		return sb.toString();
	}
	

	@Override
	public CNF generateCNF(VariableContext context) {
		LineCreator creat = new LineCreator();
		Node[] graph = creat.getLine(numNodes); //creat.getLineSkips(numNodes,2,3);//
		prevGraph = graph;
		CNF cnf = GraphToColorProblem.coloringAsCNF(graph,numColors);
		
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
