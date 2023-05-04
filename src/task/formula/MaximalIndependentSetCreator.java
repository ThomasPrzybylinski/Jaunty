package task.formula;

import formula.VariableContext;
import formula.simple.CNF;
import graph.PossiblyDenseGraph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import task.formula.random.CNFCreator;
import task.translate.FileDecodable;
import workflow.eclectic.ClosenessFinder;

public class MaximalIndependentSetCreator implements CNFCreator, FileDecodable {
	private PossiblyDenseGraph<?> graph;
	private ClosenessFinder finderForGraph = null;
	
	public MaximalIndependentSetCreator(PossiblyDenseGraph<?> graph) {
		super();
		this.graph = graph;
	}
	
	public MaximalIndependentSetCreator(PossiblyDenseGraph<?> graph, ClosenessFinder finder) {
		this(graph);
		finderForGraph = finder;
	}

	@Override
	public formula.simple.CNF generateCNF(VariableContext context) {
		CNF ret = new CNF(context);
		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			List<Integer> neighbors = new LinkedList<Integer>();
			for(int i = 0; i < graph.getNumNodes(); i++) {
				if(i == k) continue;
				if(areTooClose(k,i)) {
					neighbors.add(i);
					if(i > k) {
						ret.addClause(-(k+1),-(i+1)); //no two neighbors can be chosen
					}
				}
			}
			
			int[] clause = new int[neighbors.size()+1]; //One of k or its neighbors must be chosen
			clause[0] = k+1;
			int index = 1;
			for(int i : neighbors) {
				clause[index] = i+1;
				index++;
			}
			ret.addClause(clause);
		}
		
		return ret.reduce();
	}
	
	private boolean areTooClose(int k, int i) {
		return finderForGraph == null ? graph.areAdjacent(k,i) : finderForGraph.areTooClose(k,i);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		throw new NotImplementedException();
	}

}
