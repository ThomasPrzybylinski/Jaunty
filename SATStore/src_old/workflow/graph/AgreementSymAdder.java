package workflow.graph;

import formula.VariableContext;
import graph.PossiblyDenseGraph;

import java.util.List;

import task.symmetry.SymmetryUtil;

public class AgreementSymAdder extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, List<int[]> representatives) {
		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();
		
		while(vc.getNumVarsMade() < rep.length) {
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
