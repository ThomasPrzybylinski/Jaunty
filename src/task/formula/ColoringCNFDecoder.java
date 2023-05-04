package task.formula;

import java.io.File;
import java.io.IOException;

import formula.VariableContext;
import formula.simple.CNF;
import graph.Node;
import graph.SparseGraph;
import io.GraphColorIO;
import task.formula.random.CNFCreator;
import task.translate.FileDecodable;

public class ColoringCNFDecoder implements CNFCreator, FileDecodable {
	private CNFCreator coloringCreator;
	
	private int numNodes;
	private int numColors;
	private SparseGraph prevGraph;
	
	public ColoringCNFDecoder(CNFCreator coloringCreator) {
		this.coloringCreator = coloringCreator;
	}
	
	
	@Override
	public CNF generateCNF(VariableContext context) {
		CNF coloringTheory = coloringCreator.generateCNF(context);
		int numVars = context.size();
		
		for(int[] clause : coloringTheory.getClauses()) {
			boolean fullPositive = true;
			
			for(int i : clause) {
				if(i < 0) {
					fullPositive = false;
					break;
				}
			}
			
			if(fullPositive) {
				numColors = clause.length;
				numNodes = numVars/numColors;
				prevGraph = new SparseGraph(numNodes);
				break;
			}
		}
		
		Node[] nodes = prevGraph.getNodes();
		
		for(int[] clause : coloringTheory.getClauses()) {
			boolean fullPositive = true;
			
			for(int i : clause) {
				if(i < 0) {
					fullPositive = false;
					break;
				}
			}
			
			if(!fullPositive) {
				int node1 = (-clause[0]-1)/numColors;
				int node2 = (-clause[1]-1)/numColors;
				
				if(node1 != node2 && !nodes[node1].adjacentTo(nodes[node2])) {
					nodes[node1].addEdge(nodes[node2]);
				}
			}
		}
		
		return coloringTheory;
	}

	
	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		(new GraphColorIO(prevGraph.getNodes(),numColors)).fileDecoding(dir,filePrefix,model);
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);

	}
	
	@Override
	public String toString() {
		return "Coloring_"+coloringCreator.toString();
	}
}
