package workflow.graph.local;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

import java.util.List;

import task.symmetry.SymmetryUtil;
import workflow.graph.EdgeManipulator;

@Deprecated
public class AgreementSymAdder extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();
		
		while(vc.size() < rep.length) {
			vc.createNextDefaultVar();
		}
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);
				if(SymmetryUtil.doModelsAgreeSym(vc,representatives,rep1,rep2)) {
					g.setEdgeWeight(k,i,0);
				}
			}
		}
	}

	@Override
	public boolean isSimple() {
		return true;
	}

}
