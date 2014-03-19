package workflow.graph.local;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;

import java.util.List;

import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import util.lit.LitsMap;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class AgreementLocalSymAdder extends EdgeManipulator {
	public int skipped = 0;
	public int iters = 0;
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		LocalSymClauses rep = new LocalSymClauses(orig);
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);

				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);

				iters++;
				
				if(map.contains(agreement)) {
					skipped++;
					continue;
				} else {
					map.put(agreement,null);
				}
				
				rep.post();

				for(int j = 0; j < agreement.length; j++) {
					int var = Math.abs(agreement[j]);
					if(agreement[j] > 0) {
						rep.addCondition(var);
					} else if(agreement[j] < 0) {
						rep.addCondition(-(var));
					}
				}

				
				
				ClauseList cl = rep.getCurList(true);
				
				RealSymFinder syms = new RealSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();

				LiteralGroup modelGroup = rep.getModelGroup(group);

				SchreierVector vec = new SchreierVector(modelGroup);

				for(int j = 0; j < orig.size(); j++) {
					for(int h = j+1; h < orig.size(); h++) {
						if(vec.sameOrbit(j+1,h+1)) {
							g.setEdgeWeight(j,h,0);
						}
					}
				}

				rep.pop();
				
			}
		}
	}
	//		int[] rep = representatives.get(0);
	//		VariableContext vc = new VariableContext();
	//		
	//		while(vc.size() < rep.length) {
	//			vc.createNextDefaultVar();
	//		}
	//		
	//		for(int k = 0; k < representatives.size(); k++) {
	//			int[] rep1 = representatives.get(k);
	//			for(int i = k+1; i < representatives.size(); i++) {
	//				int[] rep2 = representatives.get(i);
	//				
	//				int[] agree = SymmetryUtil.getAgreement(rep1,rep2);
	//				
	//				List<int[]> agreers = SymmetryUtil.filterModels(representatives,agree);
	//				
	//				int[] localToGlobalIndecies = getLocalToGlobalIndecies(agreers,representatives);
	//				
	//				ClauseList cl = SymmetryUtil.getInverseList(agreers,rep.length);
	//				
	//				RealSymFinder finder = new RealSymFinder(cl);
	//				IntegralDisjointSet ds = finder.getSymOrbits();
	//				
	//				for(int j = 0; j < agreers.size(); j++) {
	//					for(int h = j+1; h < agreers.size(); h++) {
	//						if(ds.sameSet(j+1,h+1)) {
	//							g.setEdgeWeight(localToGlobalIndecies[j],localToGlobalIndecies[h],0);	
	//						}
	//					}
	//				}
	//			}
	//		}
	//		
	////		System.out.print("1A: ");
	////		for(int k = 1; k < g.getNumNodes(); k++) {
	////			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
	////		}
	////		System.out.println();
	//	}
	//
	//	//Assumes agreers is a sublist of representatives
	//	private int[] getLocalToGlobalIndecies(List<int[]> agreers,	List<int[]> representatives) {
	//		int[] ret = new int[agreers.size()];
	//		int repIndex = 0;
	//		
	//		for(int k = 0; k < agreers.size(); k++) {
	//			boolean found = false;
	//			while(!found) {
	//				if(Arrays.equals(agreers.get(k),representatives.get(repIndex))) {
	//					found = true;
	//					ret[k] = repIndex;
	//				}
	//				repIndex++;
	//			}
	//		}
	//		
	//		return ret;
	//	}
	//
	//
	//
	@Override
	public boolean isSimple() {
		return true;
	}
}
