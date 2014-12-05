package task.formula.random;

import io.GraphColorIO;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import task.translate.FileDecodable;
import formula.Conjunctions;
import formula.VariableContext;
import formula.simple.CNF;
import graph.ColorableGraphCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;

public class ApproxColorableGraphCNF implements CNFCreator, FileDecodable {
	private Random rand;
	private int numNodes;
	private int numEdges;
	private int numColors;
	
	private Node[] prevGraph;
	
	
	
	public ApproxColorableGraphCNF(int numNodes, int numEdges, int numColors) {
		super();
		this.numNodes = numNodes;
		this.numEdges = numEdges;
		this.numColors = numColors;
		rand = new Random();
	}


	public ApproxColorableGraphCNF(int numNodes, int numEdges, int numColors,int seed) {
		this.numNodes = numNodes;
		this.numEdges = numEdges;
		this.numColors = numColors;
		rand = new Random(seed);
	}
	
	
	@Override
	public CNF generateCNF(VariableContext context) {
		ColorableGraphCreator creat = new ColorableGraphCreator(rand);
		prevGraph = creat.getColorableGraph(numNodes,numEdges,numColors);
		Conjunctions color = GraphToColorProblem.coloringAsConjunction(prevGraph,numColors);
		return new CNF(color);
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

}
