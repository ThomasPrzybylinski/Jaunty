package workflow.graph;

import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;
import graph.PossiblyDenseGraph;

import java.util.List;

import task.symmetry.SymmetryUtil;

public class MinimalDistanceEdges extends ReportableEdgeAddr {
	private float weight = 0; //The weight to add if two models have minimal distance
	private int iters = 0;
	
	public MinimalDistanceEdges() {}
	
	public MinimalDistanceEdges(float weight) {
		this.weight = weight;
	}

	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g,
			ClauseList orig) {
		iters = 0;
		List<int[]> representatives = orig.getClauses();
		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();
		
		while(vc.size() < rep.length) {
			vc.createNextDefaultVar();
		}
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				iters++;
				int[] rep2 = representatives.get(i);
				
				//Use same functions used to calc agreement symmetry
				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
				
				DNF agreeClauses = new DNF(vc);
				
				SymmetryUtil.filterModelsForAgreement(agreeClauses,representatives,agreement);

				if(agreeClauses.getClauses().size() == 2) {
					if(g.areAdjacent(k,i)) {
						g.setEdgeWeight(k,i,Math.min(weight,g.getEdgeWeight(k,i)));
					} else {
						g.setEdgeWeight(k,i,weight);
					}
				}
			}
		}

	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public int getIters() {
		return iters;
	}

	@Override
	public long getNumUsefulModelSyms() {
		return 0;
	}

}
