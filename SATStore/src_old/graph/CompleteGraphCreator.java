package graph;


public class CompleteGraphCreator {
	
	public static Node[] getCompleteGraph(int numNodes) {
		Node[] nodes = new Node[numNodes];
		Node.reset();
		for(int k = 0; k < numNodes; k++) {
			nodes[k] = new Node();
		}
		
		for(int k = 0; k < numNodes; k++) {
			for(int i = k+1; i < numNodes; i++) {
				nodes[k].addEdge(nodes[i]);
			}
		}
		
		return nodes;
	}
}
