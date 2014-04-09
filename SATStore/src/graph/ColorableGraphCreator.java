package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import task.formula.random.CNFCreator;

public class ColorableGraphCreator{

	private Random rand;
	
	public ColorableGraphCreator() {
		rand = new Random();
	}
	
	public ColorableGraphCreator(Random rand) {
		this.rand = rand;
	}
	
	//Right now only good for relatively sparse graphs
	//NumEdges may be somewhat less than what you have given, to ensure this program halts
	public Node[] getColorableGraph(int numNodes, int numEdges, int numColors) {
		if(numColors == 1 && numNodes > 1) return null;
		
		int maxEdges = (numNodes*(numNodes-1))/2;
		
		numEdges = Math.min(numEdges,maxEdges);
		
		
		Node[] nodes = new Node[numNodes];
		List<List<Node>> satColors = new ArrayList<List<Node>>(numColors);
		List<Set<Node>> satColorsTester = new ArrayList<Set<Node>>(numColors);
		
		for(int k = 0; k < numColors; k++) {
			satColors.add(new ArrayList<Node>());
			satColorsTester.add(new HashSet<Node>());
		}
		
		//Distribute into colors
		Node.reset();
		for(int k = 0; k < numNodes; k++) {
			nodes[k] = new Node();
			
			if(k < numColors) {
				nodes[k].info = k;
				satColors.get(k).add(nodes[k]);
				satColorsTester.get(k).add(nodes[k]);
			} else {
				int index = rand.nextInt(satColors.size());
				nodes[k].info = index;
				satColors.get(index).add(nodes[k]);
				satColorsTester.get(index).add(nodes[k]);
			}
		}
		
		//Make spanning tree, to ensure connectedness
		for(int k = 1; k < nodes.length; k++) {
			if(k < numColors) {
				nodes[k].addEdge(nodes[rand.nextInt(k)]);
				numEdges--;
			} else {
				int nodeIndex;
				do {
					nodeIndex = rand.nextInt(k);
				} while(nodes[k].info == nodes[nodeIndex].info); //Don't connect nodes of same color
				
				nodes[k].addEdge(nodes[nodeIndex]);
				numEdges--;
			}
		}
		
		for(int k = 0; k < numEdges; k++) {
			int index1 = rand.nextInt(numNodes);
			int index2;
			int counter = 0;
			final int maxTries = 2*numNodes;
			do {
				index2 = rand.nextInt(numNodes);
				counter++;
			} while(counter < 2*numNodes && (nodes[index1].info == nodes[index2].info //Don't connect nodes of same color
					|| nodes[index1].adjacentTo(nodes[index2]))); 
			
			if(counter != maxTries) {
				nodes[index1].addEdge(nodes[index2]);
			}
		}
		
		return nodes;
	}
}
