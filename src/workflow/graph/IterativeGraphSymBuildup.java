package workflow.graph;

import java.util.HashSet;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import util.IntPair;

public class IterativeGraphSymBuildup extends EdgeManipulator {
	EdgeManipulator initial;
	
	public IterativeGraphSymBuildup() {
		
	}
	
	public IterativeGraphSymBuildup(EdgeManipulator initial) {
		this.initial=initial;
	}
	
	
	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g, ClauseList cl) {
		if(initial != null) {
			initial.addEdges(g,cl);
		}
		boolean addedEdges = true;
		int times = 0;
		while(addedEdges) {
			times++;
			System.out.println("Round "+times);
			addedEdges = false;

			ClauseList graphRep = new ClauseList(cl.getContext());
			for(int k = 0; k < cl.size(); k++) {
				for(int i = k+1; i < cl.size(); i++) {
					if(g.areAdjacent(k,i)) {
						addedEdges = true;
						graphRep.addClause(k+1,i+1);
					}
				}
			}
			
			if(!addedEdges) return; //empty graph. Error Condition
			addedEdges=false;
			
			RealSymFinder finder = new RealSymFinder(graphRep);
			LiteralGroup lg = finder.getSymGroup();
//			System.out.println(lg);
			SchreierVector sv = new SchreierVector(lg);
			HashSet<IntPair> newEdges = new HashSet<IntPair>();
			
			for(int k = 0; k < cl.size(); k++) {
				for(int i = k+1; i < cl.size(); i++) {
					if(!g.areAdjacent(k,i) && sv.sameOrbit(k+1,i+1)) {
						addedEdges=true;
						
						newEdges.add(new IntPair(k,i));
						
//						for(int j = 0; j < cl.size(); j++) {
//							if(g.areAdjacent(j,k)) {
//								int small = Math.min(j,i);
//								int large = Math.max(j,i);
//								newEdges.add(new IntPair(small,large));
//							}
//							if(g.areAdjacent(i,j)) {
//								int small = Math.min(j,k);
//								int large = Math.max(j,k);
//								newEdges.add(new IntPair(small,large));
//							}
//						}
					}
				}
			}
			
			for(IntPair ip : newEdges) {
				g.setAdjacent(ip.getI1(),ip.getI2());
			}
		}
	}

	@Override
	public boolean isSimple() {
		return false;
	}

}
