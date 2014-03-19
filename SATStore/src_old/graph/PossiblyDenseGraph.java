package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Weighted Graph implementation. Uses an upper-triangular matrix to store values
	//tries to minimize memory usage for very dense graphs
//Uses shorts for weights, because the graph can be big
public class PossiblyDenseGraph<T> {
	private float[][] graph; //0 means nonadjacent, 1 means adjacent
	private int numNodes;
	private T[] objs;
	
	public PossiblyDenseGraph(int numNodes) {
		graph = new float[numNodes-1][];
		this.numNodes = numNodes;
		for(int k = 0; k < graph.length; k++) {
			graph[k] = new float[numNodes-k-1];
			for(int i = 0; i < graph[k].length; i++) {
				graph[k][i] = Float.NEGATIVE_INFINITY;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public PossiblyDenseGraph(Collection<T> objs) {
		this.numNodes = objs.size();
		this.objs =  (T[]) new Object[numNodes];
		
		int index = 0;
		
		for(T obj : objs) {
			this.objs[index] = obj;
			index++;
		}
		
		graph = new float[numNodes-1][];

		for(int k = 0; k < graph.length; k++) {
			graph[k] = new float[numNodes-k-1];
			for(int i = 0; i < graph[k].length; i++) {
				graph[k][i] = Float.NEGATIVE_INFINITY;
			}
		}
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public T getElt(int index) {
		return objs[index];
	}
	
	public List<T> getObjs() {
		 List<T> ret = new ArrayList<T>(objs.length);
		 
		 for(T obj : objs) {
			 ret.add(obj);
		 }
		 
		 return ret;
	}

	public void setAdjacent(int k, int i) {
		setAdjacent(k,i,true);
	}
	
	public void setAdjacent(int k, int i, boolean adj) {
		if(k == i) return;
		int small = Math.min(k,i);
		int large = Math.max(k,i);
		graph[small][getSecondIndex(large,small)] = adj ? 0 : Float.NEGATIVE_INFINITY;
	}
	
	private int getSecondIndex(int large, int small) {
		return large-small-1;
	}
	
	public boolean areAdjacent(int k, int i) {
		if(k == i) return true;
		int small = Math.min(k,i);
		int large = Math.max(k,i);
		return graph[small][getSecondIndex(large,small)] > Float.NEGATIVE_INFINITY;
	}
	
	public void setEdgeWeight(int k, int i, float weight) {
		if(k == i) return;
		int small = Math.min(k,i);
		int large = Math.max(k,i);
		graph[small][getSecondIndex(large,small)] = weight;
	}
		
	public float getEdgeWeight(int k, int i) {
		if(k == i) return 0;
		int small = Math.min(k,i);
		int large = Math.max(k,i);
		return graph[small][getSecondIndex(large,small)];
	}

}
