package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpanningCycleGraphCreator {

	private Random rand;
	
	public SpanningCycleGraphCreator() {
		rand = new Random();
	}
	
	public SpanningCycleGraphCreator(Random rand) {
		this.rand = rand;
	}
	
	//Right now only good for relatively sparse graphs
	//NumEdges may be somewhat less than what you have given, to ensure this program halts
	public Node[] getSpanningCycleGraph(int numNodes, int numEdges, int numCyclesEnsured) {
		assert numCyclesEnsured > 0;
		assert numNodes > 0;
		assert numEdges > 0;
		
		int maxEdges = (numNodes*(numNodes-1))/2;
		numCyclesEnsured = Math.min(numNodes/3,numCyclesEnsured);
		
		numEdges = Math.min(numEdges,maxEdges);
		
		
		Node[] nodes = new Node[numNodes];
		Node.reset();
		for(int k = 0; k < numNodes; k++) {
			nodes[k] = new Node();
		}
		
		
		//Distribute into spanning cycles
		List<List<Node>> cycles = new ArrayList<List<Node>>(numCyclesEnsured);
		
		List<Node> curNodes = new ArrayList<Node>();
		for(Node n : nodes) {
			curNodes.add(n);
		}
		for(int k = 0; k < numCyclesEnsured; k++) {
			List<Node> cycle = new ArrayList<Node>(3);
			
			for(int i = 0; i < 3; i++) {
				int index = rand.nextInt(curNodes.size());
				Node toAdd = curNodes.remove(index);
				cycle.add(toAdd);
			}
			cycles.add(cycle);
		}

		while(curNodes.size() > 0) {
			Node toAdd = curNodes.remove(0);
			List<Node> cycle = cycles.get(rand.nextInt(cycles.size()));
			cycle.add(toAdd);
		}

		
		
		List<List<Node>> connection = new ArrayList<List<Node>>();
		for(List<Node> cycle : cycles) {
			
			for(int k = 1; k < cycle.size(); k++) {
				cycle.get(k-1).addEdge(cycle.get(k));
				numEdges--;
				
				if(k == cycle.size()-1) {
					cycle.get(k).addEdge(cycle.get(0));
					numEdges--;
				}
			}
			
			
			List<Node> newList = new ArrayList<Node>(cycle.size());
			for(Node n : cycle) {
				newList.add(n);
			}
			connection.add(newList);
		}
		
		while(connection.size() > 1) {
			int index1 = rand.nextInt(connection.size());
			int index2 = rand.nextInt(connection.size()-1);
			
			if(index2 >= index1) {
				index2++;
			}
			
			List<Node> c1 = connection.get(index1);
			List<Node> c2 = connection.get(index2);
			
			Node n1 = c1.get(rand.nextInt(c1.size()));
			Node n2 = c2.get(rand.nextInt(c2.size()));
			
			n1.addEdge(n2);
			numEdges--;
			c1.addAll(c2);
			connection.remove(index2);
		}
		
		for(int k = 0; k < numEdges; k++) {
			int index1 = rand.nextInt(numNodes);
			int index2;
			int counter = 0;
			final int maxTries = 2*numNodes;
			do {
				index2 = rand.nextInt(numNodes);
				counter++;
			} while(counter < 2*numNodes && nodes[index1].adjacentTo(nodes[index2])); 
			
			if(counter != maxTries) {
				nodes[index1].addEdge(nodes[index2]);
			}
		}
		
		return nodes;
	}
}
