package workflow.graph.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import util.IntPair;
import util.formula.FormulaForAgreement;
import util.lit.LitUtil;
import util.lit.LitsMap;
import util.lit.ModelMeasure;
import workflow.graph.ReportableEdgeAddr;


public class AgreementConstructionAdder extends ReportableEdgeAddr {
	public int skipped = 0;
	public int iters = 0;
	private boolean doGlob;
	private int minModels=-1;
	private static boolean PRINT = true;
	
	private IntPair[][] cachedPairs;

	private class PartialSym implements Comparable<PartialSym> {
		int[] agreement;
		LiteralGroup modelGroup;

		List<IntPair> edges = new LinkedList<IntPair>();
		
		List<PartialSym> children = new LinkedList<PartialSym>();

		public PartialSym(int[] agreement, LiteralGroup modelGroup) {
			super();
			this.agreement = agreement;
			this.modelGroup = modelGroup;
		}
		@Override
		public int compareTo(PartialSym o) {
			return (new ModelMeasure()).compare(agreement,o.agreement); //So sorted root to leaves
		}
		@Override
		public String toString() {
			return Arrays.toString(agreement);// + " " + modelGroup.toString();
		}
		public List<IntPair> getEdges() {
			return edges;
		}
//		public void setEdges(List<IntPair> edges) {
//			this.edges = edges;
//		}
	}

	
	public AgreementConstructionAdder() {
		this(false);
	}
	
	public AgreementConstructionAdder(boolean doGlob) {
		this.doGlob = doGlob;
	}

	
	public AgreementConstructionAdder(boolean doGlob, int minModels) {
		this.doGlob = doGlob;
		this.minModels=minModels;
	}
	
	/**TODO:
	 *  1) Create tree: In order of size, if A subset of B, and no current child of A is a subset of B, then make B a child of A
	 *  2) Then use partitions to find edges at each level. Then take the leaf nodes and use those to fill in the graph
	 */

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		FormulaForAgreement rep = new FormulaForAgreement(orig);

		List<PartialSym> results = new ArrayList<PartialSym>();
		
		boolean globAgr = false;
		
		cachedPairs = new IntPair[representatives.size()][representatives.size()];

		if(!globAgr && doGlob) {
			iters++;
			//Do glob sym if we haven't already
			ClauseList cl = orig;

			RealSymFinder syms = new RealSymFinder(cl);
			LiteralGroup group = syms.getSymGroup();

			LiteralGroup modelGroup = rep.getModelGroup(group);

			PartialSym partSym = new PartialSym(new int[]{},modelGroup);
			results.add(partSym);

			SchreierVector vec = new SchreierVector(modelGroup);

			for(int j = 0; j < orig.size(); j++) {
				for(int h = j+1; h < orig.size(); h++) {
					if(vec.sameOrbit(j+1,h+1)) {
						g.setEdgeWeight(j,h,0);
						partSym.getEdges().add(new IntPair(j+1,h+1));
					}
				}
			}
			
			if(PRINT) {
			  System.out.println(group); 	
			
			}
			
		}
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);

				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);

				iters++;

				if(map.contains(agreement)) {
					skipped++;
					iters--;
					continue;
				} else {
					map.put(agreement,null);
				}

				List<Integer> partList = new LinkedList<Integer>();

				for(int j = 0; j < agreement.length; j++) {
					int var = Math.abs(agreement[j]);
					if(agreement[j] > 0) {
						partList.add(var);
					} else if(agreement[j] < 0) {
						partList.add(-var);
					}
				}
				
				if(partList.size() == 0) {
					globAgr = true;
				}

				int[] part = new int[partList.size()]; 

				int ind = 0;
				for(int pLit : partList) {
					part[ind] = pLit;
					ind++;
				}

				ClauseList cl = rep.getCLFromModels(part);
				
				if(cl.size() < minModels) continue;

				RealSymFinder syms = new RealSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();

				FormulaForAgreement form = new FormulaForAgreement(cl);
				int[] exist = rep.getExistantClauses();
				LiteralGroup modelGroup = form.getModelGroup(group).reduce();
				
				
				LiteralGroup modToAdd = rep.getModelGroup(group,exist).reduce();
				PartialSym partSym = new PartialSym(part,modToAdd);

				SchreierVector vec = new SchreierVector(modelGroup);

				for(int j = 1; j <= vec.getNumVars(); j++) {
					for(int h = j+1; h <= vec.getNumVars(); h++) {
						if(vec.sameOrbit(j,h)) {
							int realJ = exist[j-1];
							int realH = exist[h-1];
							g.setEdgeWeight(realJ,realH,0);
							partSym.getEdges().add(getCachedPair(realJ+1,realH+1));
						}
					}
				}
				
				if(partSym.getEdges().size() > 0) {
					results.add(partSym);
				}
				
				if(PRINT) {
					System.out.println("Models   : ("+k+","+i+")");
					System.out.println("Agreement: " + Arrays.toString(agreement));
					System.out.println("Syms     : " + group);
				}
			}
		}
		
		

		//Now that we have the reps, combine them
		//Lowest to highest
		Collections.sort(results);

		for(int k = 0; k < results.size(); k++) {
			PartialSym node = results.get(k);
			int[] partInterp = node.agreement;
			for(int i = k+1; i < results.size(); i++) {
				PartialSym maybeChild = results.get(i);
				boolean isChild = false;
				if(LitUtil.isSubset(partInterp,maybeChild.agreement)) {
					isChild = true;
					for(PartialSym trueChild : node.children) {
						if(LitUtil.isSubset(trueChild.agreement,maybeChild.agreement)) {
							isChild = false; 
							break;
						}
					}
				}
				
				if(isChild) {
					node.children.add(maybeChild);
				}
			}
		}
		
		for(int k = results.size() - 1; k >= 0; k--) {
			PartialSym node = results.get(k);
			
			LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
			HashSet<IntPair> seen = new HashSet<IntPair>();
			
			for(PartialSym child : node.children) {
				for(IntPair pair : child.edges) {
					if(!seen.contains(pair)) {
						toCompute.add(pair);
						seen.add(pair);
						node.edges.add(pair);
					}
				}
			}
			
			while(!toCompute.isEmpty()) {
				IntPair pair = toCompute.poll();

				for(LiteralPermutation p : node.modelGroup.getGenerators()) {
					IntPair newP;// = pair.applySort(p);

//					if(!lp.pairs.contains(newP)) {
//						lp.pairs.add(newP);
//						toCompute.push(newP);
//					}
					
					newP = getCachedPair(pair.applySort(p,0));
					
					if(!seen.contains(newP)) {
						seen.add(newP);
						node.edges.add(newP);
						toCompute.push(newP);
					}
					
					newP = getCachedPair(pair.applySort(p,1));
					
					if(!seen.contains(newP)) {
						seen.add(newP);
						node.edges.add(newP);
						toCompute.push(newP);
					}
				}
			}
			
			node.children = null;
			node.modelGroup = null;
		}
		
		for(IntPair ip : results.get(0).edges) {
			g.setEdgeWeight(ip.getI1()-1,ip.getI2()-1,0);
		}
		
//		System.out.println(results);

//		//TODO: Not exactly correct yet. Only gets a subset of edges it should
//		for(int k = 0; k < results.size(); k++) {
//			PartialSym smallerPart = results.get(k);
//			smallerPart.edges = null;
//			LiteralGroup modelGroup = smallerPart.modelGroup;
//			int[] partInterp = smallerPart.agreement;
//
//			for(int i = k+1; i < results.size(); i++) {
//				PartialSym largerPart = results.get(i);
//
//				if(LitUtil.isSubset(partInterp,largerPart.agreement)) {
//					HashSet<IntPair> seen = new HashSet<IntPair>();
//					LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
//
//					toCompute.addAll(largerPart.edges);
//					seen.addAll(largerPart.edges);
//
//					while(!toCompute.isEmpty()) {
//						IntPair pair = toCompute.poll();
//						for(LiteralPermutation p : modelGroup.getGenerators()) {
//							IntPair newP;// = pair.applySort(p,0);
////							if(!seen.contains(newP)) {
////								g.setEdgeWeight(newP.getI1()-1,newP.getI2()-1,0);
////								seen.add(newP);
////								toCompute.push(newP);
////							}
//
//							newP = pair.apply(p,0);
//							if(!seen.contains(newP)) {
//								g.setEdgeWeight(newP.getI1()-1,newP.getI2()-1,0);
//								seen.add(newP);
//								toCompute.push(newP);
//							}
//
//
//						}
//					}
//
//				}
//			}
//			
//			smallerPart.agreement = null;
//			smallerPart.modelGroup = null;
//
//		}
		
		cachedPairs = null;
	}
	
	protected IntPair getCachedPair(int i1, int i2) {
		IntPair ret = cachedPairs[i1-1][i2-1];
		
		if(ret == null) {
			ret = new IntPair(i1,i2);
			cachedPairs[i1-1][i2-1] = ret;
		}
		return ret;
	}
	
	protected IntPair getCachedPair(IntPair pair) {
		IntPair ret = cachedPairs[pair.getI1()-1][pair.getI2()-1];
		if(ret == null) {
			ret = new IntPair(pair.getI1(),pair.getI2());
			cachedPairs[pair.getI1()-1][pair.getI2()-1] = ret;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "AttrAgr("+doGlob+")";
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
