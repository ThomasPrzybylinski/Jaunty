package workflow.graph.local;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.sat4j.minisat.core.IntQueue;

import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import task.symmetry.sparse.SparseSymFinder;
import util.formula.FormulaForAgreement;
import util.lit.IntArrayKey;
import util.lit.LitsMap;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class AgreementLocalSymAdder extends EdgeManipulator {
	public int skipped = 0;
	public int iters = 0;
	private static boolean PRINT = true;
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		long addedSize = 0;
		List<int[]> representatives = orig.getClauses();
		Map<IntArrayKey,Object> map = new WeakHashMap<IntArrayKey,Object>();//new LitsMap<Object>(orig.getContext().size());
//		LocalSymClauses rep = new LocalSymClauses(orig);
		FormulaForAgreement rep = new FormulaForAgreement(orig);
//		LocalSymClauses Rep = new LocalSymClauses(orig);
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);
//
				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
//
//				iters++;
//				
//				if(map.contains(agreement)) {
//					skipped++;
//					continue;
//				} else {
//					map.put(agreement,null);
//				}
//				rep.post();
//
//				for(int j = 0; j < agreement.length; j++) {
//					int var = Math.abs(agreement[j]);
//					if(agreement[j] > 0) {
//						rep.addCondition(var);
//					} else if(agreement[j] < 0) {
//						rep.addCondition(-(var));
//					}
//				}
//
//				
//				
//				ClauseList cl = rep.getCurList(false);
//				
//				RealSymFinder syms = new RealSymFinder(cl);
//				LiteralGroup group = syms.getSymGroup();
//
//				LiteralGroup modelGroup = rep.getModelGroup(group);
//
//				populateEdges(g, orig, modelGroup);
//
//				rep.pop();
				
//				int[] rep2 = representatives.get(i);
//
//				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
//
//				iters++;
//				
//				if(map.contains(agreement)) {
//					skipped++;
//					continue;
//				} else {
//					map.put(agreement,null);
//				}
				
				
//			int[] rep2 = representatives.get(i);
//
//				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
//
				iters++;

				IntArrayKey agreeKey = new IntArrayKey(agreement);
				if(map.containsKey(agreeKey)) {
					skipped++;
					continue;
				} else {
					map.put(agreeKey,null);
				}
				
//				Rep.post();
//				for(int j = 0; j < agreement.length; j++) {
//					int var = Math.abs(agreement[j]);
//					if(agreement[j] > 0) {
//						Rep.addCondition(var);
//					} else if(agreement[j] < 0) {
//						Rep.addCondition(-(var));
//					}
//				}
//				
//				ClauseList cl2 = Rep.getCurList(false);
//				Rep.pop();
				
				ClauseList cl = rep.getCLFromModels(agreement);

				
//				System.out.println(Arrays.toString(agreement));
//				System.out.println(cl);
//				System.out.println(cl2);
//				System.out.println();
			
//				RealSymFinder syms = new RealSymFinder(cl);
				
				
				if(PRINT) {
					System.out.println("Models   : ("+k+","+i+"), (" + skipped +" skipped, mapsize " + map.size() + ")");
					//System.out.println("Agreement: " + Arrays.toString(agreement));
				}
				
				if(cl.size() > 2) {
					SparseSymFinder syms = new SparseSymFinder(cl);
					LiteralGroup group = syms.getSymGroup();
					FormulaForAgreement form = new FormulaForAgreement(cl);
					int[] exist = rep.getExistantClauses();
					LiteralGroup modelGroup = form.getModelGroup(group);
					SchreierVector vec = new SchreierVector(modelGroup);
					for(int j = 1; j <= vec.getNumVars(); j++) {
						for(int h = j+1; h <= vec.getNumVars(); h++) {
							if(vec.sameOrbit(j,h)) {
								g.setEdgeWeight(exist[j-1],exist[h-1],0);
							}
						}
					}
					
					if(PRINT) {
					//	System.out.println("Syms     : " + group);
					}
					
//					LiteralGroup modelGroup = rep.getModelGroup(group,rep.getExistantClauses());
				
//					populateEdges(g, orig, modelGroup);
				} else {
					g.setEdgeWeight(k,i,0);

				}
				
				
//				
			}
		}
	}
	private void populateEdges(PossiblyDenseGraph<int[]> g,
			ClauseList orig, LiteralGroup modelGroup) {
		IntQueue toCompute = new IntQueue();
		toCompute.ensure(modelGroup.size()+1);
		int[] orbits = new int[modelGroup.size()+1];
		int[] localOrbit = new int[modelGroup.size()+1];
		
		for(int k = 1; k < orbits.length; k++) {
			if(orbits[k] != 0) continue;
			
//			int rep = modVec.getRep(k);

			toCompute.insert(k);
			orbits[k] = k;
			localOrbit[0] = k;
			int localOrbitIndex = 1;
		
			//Compute orbit of k
			while(toCompute.size() > 0) {
				int i = toCompute.dequeue();
				for(LiteralPermutation perm : modelGroup.getGenerators()) {
					int image = perm.imageOf(i);
					if(orbits[image] == 0) {
						orbits[image] = k;
						localOrbit[localOrbitIndex] = image;
						localOrbitIndex++;
						toCompute.insert(image);
					}
				}
			}
			
			//use the orbit to create edges
			Arrays.sort(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex));
			for(int i = 0; i < localOrbitIndex; i++) {
				for(int j = i+1; j < localOrbitIndex; j++) {
					g.setEdgeWeight(localOrbit[i]-1,localOrbit[j]-1,0);
				}
			}
			Arrays.fill(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex),0);
		}
			
		
//		SchreierVector vec = new SchreierVector(modelGroup);
//
//		for(int j = 0; j < orig.size(); j++) {
//			for(int h = j+1; h < orig.size(); h++) {
//				if(vec.sameOrbit(j+1,h+1)) {
//					g.setEdgeWeight(j,h,0);
//				}
//			}
//		}
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
	
	public String toString() {
		return "AgreeSym";
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}
}
