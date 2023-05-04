package workflow.graph;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;


//Replaces the distance between two nodes with the shortest path distance
public class ShortestPathCreator extends EdgeManipulator {
	
	private class NodeDistPair implements Comparable<NodeDistPair> {
		public int node;
		public float dist;
		
		public NodeDistPair(int node, float dist) {
			this.node = node;
			this.dist = dist;
		}
		
		@Override
		public int compareTo(NodeDistPair o) {
			float distDiff = this.dist - o.dist;
			
			if(distDiff == 0) {
				return this.node - o.node;
			}
			
			int ret = (int) Math.signum(distDiff);
			
			return ret;
		}

		@Override
		public String toString() {
			return "[n=" + node + ", d=" + dist + "]";
		}
		
		
		
	}

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		boolean[] visited = new boolean[g.getNumNodes()];
		PossiblyDenseGraph<int[]> newWeights = new PossiblyDenseGraph<int[]>(representatives);
		
		
		for(int k = 0; k < visited.length; k++) {
			Arrays.fill(visited,false);
			PriorityQueue<NodeDistPair> modelQueue = new PriorityQueue<NodeDistPair>();
			modelQueue.add(new NodeDistPair(k,0));
			int from = k;
			
			while(!modelQueue.isEmpty()) {
				NodeDistPair pair = modelQueue.poll();
				if(visited[pair.node]) continue;
				int to = pair.node;
				float len = pair.dist;
				visited[to] = true;

				float val = len;//Math.min(len,length[k][mod]);
				float addition = g.getEdgeWeight(to,from);
				addition = addition > 0 ? addition : 0;
				newWeights.setEdgeWeight(from,to,Math.min(addition,val));

				for(int i = 0; i < g.getNumNodes(); i++) {
					if(g.areAdjacent(to,i) && !visited[i]) {
						addition = g.getEdgeWeight(to,i);
//						addition = addition > 0 ? addition : .9f; //We want a small cost to equivalence edges
						NodeDistPair toAdd = new NodeDistPair(i,len+addition);
						modelQueue.add(toAdd);
					}
				}
			}
		}
		
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i < g.getNumNodes(); i++) {
				if(newWeights.areAdjacent(k,i)) { //We don't want to change eq. edge weights since we use the mean
					g.setEdgeWeight(k,i,newWeights.getEdgeWeight(k,i));
				}
			}
		}
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
	
	public String toString() {
		return "MakeShortpath";
	}

}
