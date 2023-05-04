package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import graph.Node;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.IntPair;
import workflow.ModelGiver;

public class SpanningTree implements ModelGiver, FileDecodable {
	private Node[] graph;
	HashMap<IntPair,Integer> edgeToLit;
	
	//Assumes graph is already connected
	public SpanningTree(Node[] graph) {
		this.graph = graph;
		
		int lit = 1;
		for(int k = 0; k < graph.length; k++) {
			Node n = graph[k];
			
			for(Node n2 : n.getNeighbors()) {
				if(n.nodeNum < n2.nodeNum) {
					edgeToLit.put(new IntPair(n.nodeNum,n2.nodeNum),lit);
					lit++;
				}
			}
		}
		
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		context.ensureSize(edgeToLit.size());
		List<int[]> ret = new ArrayList<int[]>();
		boolean[] seen = new boolean[graph.length];
		List<IntPair> edges = new ArrayList<IntPair>();
		seen[0] = true;
		
		getTrees(ret,seen,edges,0);
			
		return null;
	}
	
	
	
	private void getTrees(List<int[]> ret, boolean[] seen, List<IntPair> edges,
			int prev) {
		
		Node n = graph[prev];
		
		for(Node n2 : n.getNeighbors()) {
			if(!seen[n2.nodeNum]) {
				
			}
		}
		
		
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return this;
	}

	@Override
	public String getDirName() {
		// TODO Auto-generated method stub
		return null;
	}

}
