package graph;


public class LineCreator {
	public LineCreator() {
	}

	public Node[] getLine(int numNodes) {
		if(numNodes < 1) return null;
		Node.reset();
		Node[] nodes = new Node[numNodes];
		nodes[0] = new Node();
		
		for(int k = 1; k < nodes.length; k++) {
			nodes[k] = new Node();
			nodes[k-1].addEdge(nodes[k]);
		}
		
		return nodes;
	}
	
	///Every skipNum nodes has an edge skipSize nodes forward
	public Node[] getLineSkips(int numNodes, int skipNum, int skipSize) {
		if(numNodes < 1) return null;

		Node[] nodes = new Node[numNodes];
		nodes[0] = new Node();
		
		for(int k = 1; k < nodes.length; k++) {
			nodes[k] = new Node();
			nodes[k-1].addEdge(nodes[k]);
		}
		
		for(int k = 0; k+skipSize < nodes.length; k += skipNum) {
			nodes[k].addEdge(nodes[k+skipSize]);
		}
		
		return nodes;
	}
}
