package graph;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Node implements Comparable<Node>{
	private static int num = 0;
	
	public final int nodeNum;
	
	private Set<Node> neighbors;
	
	public int info;
	public String label;
	
	public Node() {
		nodeNum = num;
		num++;
		neighbors = new TreeSet<Node>();
	}
	
	public Node(int num) {
		nodeNum = num;
		neighbors = new TreeSet<Node>();
	}
	
	public static void reset() {
		num = 0;
	}
	
	public void addEdge(Node n) {
		this.neighbors.add(n);
		n.neighbors.add(this);
	}
	
	public boolean adjacentTo(Node n) {
		return neighbors.contains(n);
	}
	
	public Set<Node> getNeighbors() {
		return Collections.unmodifiableSet(neighbors);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeNum;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Node other = (Node) obj;
		if (nodeNum != other.nodeNum) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Node o) {
		return this.nodeNum - o.nodeNum;
	}

	@Override
	public String toString() {
		return label+"_"+nodeNum+"_"+info;
	}
	
	
	
	
}
