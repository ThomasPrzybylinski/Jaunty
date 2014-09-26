package graph;


public class SparseGraph {
	private Node[] nodes;
	
	public SparseGraph(int numNodes) {
		nodes = new Node[numNodes];
		
		for(int k = 0; k < nodes.length; k++) {
			nodes[k] = new Node(k);
		}
	}
	
	public Node[] getNodes() {
		return nodes;
	}
}
