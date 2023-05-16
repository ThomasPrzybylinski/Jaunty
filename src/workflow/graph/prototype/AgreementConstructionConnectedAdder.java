package workflow.graph.prototype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;
import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import util.IntPair;
import util.IntegralDisjointSet;
import util.formula.FormulaForAgreement;
import util.lit.LitUtil;
import util.lit.LitsMap;
import util.lit.ModelComparator;
import util.lit.ModelMeasure;
import workflow.graph.ReportableEdgeAddr;

//Tells us the level at which the model graph becomes connected
public class AgreementConstructionConnectedAdder extends ReportableEdgeAddr {
	public int skipped = 0;
	public int iters = 0;
	private boolean doGlob;
	private int minModels=-1;
	private static boolean PRINT = false;
	private int minConnectedSize = -1;

	private IntPair[][] cachedPairs;

	private class AgreementLattice implements Comparable<AgreementLattice> {
		int[] agreement;
		LiteralGroup modelGroup;

		HashSet<IntPair> seenEdges = new HashSet<>();
		TreeSet<IntPair> edgesToCompute = new TreeSet<>();

		List<AgreementLattice> parents = new LinkedList<AgreementLattice>();

		public AgreementLattice(int[] agreement) {
			super();
			this.agreement = agreement;
		}
		@Override
		public int compareTo(AgreementLattice o) {
			return (new ModelMeasure()).compare(agreement,o.agreement); //So sorted root to leaves
		}
		@Override
		public String toString() {
			return Arrays.toString(agreement);// + " " + modelGroup.toString();
		}

		public void addEdgeToCompute(IntPair edge) {
			if(!seenEdges.contains(edge)) {
				edgesToCompute.add(edge);
			}
		}


		//		public void setEdges(List<IntPair> edges) {
		//			this.edges = edges;
		//		}
	}


	public AgreementConstructionConnectedAdder() {
		this(false);
	}

	public AgreementConstructionConnectedAdder(boolean doGlob) {
		this.doGlob = doGlob;
	}


	public AgreementConstructionConnectedAdder(boolean doGlob, int minModels) {
		this.doGlob = doGlob;
		this.minModels=minModels;
	}

	public int getMinConnectedSize() {
		return minConnectedSize;
	}

	//Tries to only find edge necessary for 
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		minConnectedSize=0;
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		FormulaForAgreement rep = new FormulaForAgreement(orig);

		List<AgreementLattice> agreements = new ArrayList<>();

		cachedPairs = new IntPair[representatives.size()][representatives.size()];

		if(doGlob) {
			iters++;
			//Do glob sym if we haven't already
			ClauseList cl = orig;

			RealSymFinder syms = new RealSymFinder(cl);
			LiteralGroup group = syms.getSymGroup();

			LiteralGroup modelGroup = rep.getModelGroup(group);

			AgreementLattice partSym = new AgreementLattice(new int[]{});
			partSym.modelGroup=modelGroup;
			agreements.add(partSym);

			SchreierVector vec = new SchreierVector(modelGroup);

			for(int j = 0; j < orig.size(); j++) {
				for(int h = j+1; h < orig.size(); h++) {
					if(vec.sameOrbit(j+1,h+1)) {
						g.setEdgeWeight(j,h,0);
					}
				}
			}

			if(PRINT) {
				System.out.println(group); 	

			}

			map.put(new int[]{}, null);

			if(g.getNumConnectedComponents() == 1) return;

		}

		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);

				int[] agreement = SymmetryUtil.getAgreementLits(rep1,rep2);

				iters++;

				if(map.contains(agreement)) {
					skipped++;
					iters--;
					continue;
				} else {
					map.put(agreement,null);
				}

				agreements.add(new AgreementLattice(agreement));
			}
		}


		//Now that we have the reps, combine them
		//Lowest to highest
		Collections.sort(agreements);

		for(int k = 0; k < agreements.size(); k++) {
			AgreementLattice node = agreements.get(k);
			int[] partInterp = node.agreement;
			for(int i = k-1; i >= 0; i--) {
				AgreementLattice maybeParent = agreements.get(i);
				boolean isParent = false;
				if(LitUtil.isSubset(maybeParent.agreement,partInterp)) {
					isParent = true;
					for(AgreementLattice trueParent : node.parents) {
						if(LitUtil.isSubset(maybeParent.agreement,trueParent.agreement)) {
							isParent = false; 
							break;
						}
					}
				}

				if(isParent) {
					node.parents.add(maybeParent);
				}
			}
		}

		minConnectedSize = 0;
		for(AgreementLattice agreement : agreements) {
			ClauseList cl = rep.getCLFromModels(agreement.agreement);

			if(cl.size() < minModels) continue;

			RealSymFinder syms = new RealSymFinder(cl);
			LiteralGroup group = syms.getSymGroup();

			FormulaForAgreement form = new FormulaForAgreement(cl);
			int[] exist = rep.getExistantClauses();
			LiteralGroup modelGroup = form.getModelGroup(group).reduce();


			LiteralGroup modToAdd = rep.getModelGroup(group,exist).reduce();
			agreement.modelGroup = modToAdd;

			SchreierVector vec = new SchreierVector(modelGroup);

			minConnectedSize = agreement.agreement.length;

			TreeSet<AgreementLattice> toCompute = new TreeSet<AgreementLattice>();
			toCompute.add(agreement);

			for(int j = 1; j <= vec.getNumVars(); j++) {
				for(int h = j+1; h <= vec.getNumVars(); h++) {
					if(vec.sameOrbit(j,h)) {
						int realJ = exist[j-1];
						int realH = exist[h-1];
						g.setEdgeWeight(realJ,realH,0);
						IntPair modelPair = getCachedPair(j, h);
						agreement.seenEdges.add(modelPair);

						addComputeForAllParents(agreement,modelPair);

					}
				}
			}

			for(AgreementLattice p : agreement.parents) {
				if(p.edgesToCompute.size() > 0) {
					toCompute.add(p);
				}
			}

			HashSet<IntPair> edgesToAdd = new HashSet<>();
			while(!toCompute.isEmpty()) {
				AgreementLattice node = toCompute.pollLast();

				while(node.edgesToCompute.size() > 0) {
					IntPair modelPair = node.edgesToCompute.pollLast();
					node.seenEdges.add(modelPair);
					addComputeForAllParents(node,modelPair);

					for(LiteralPermutation p : node.modelGroup.getGenerators()) {
						IntPair newP;

						newP = getCachedPair(modelPair.applySort(p,0));

						if(!node.seenEdges.contains(newP)) {
							node.edgesToCompute.add(newP);
							node.seenEdges.add(newP);
							addComputeForAllParents(node,newP);
							edgesToAdd.add(newP);
						}

						newP = getCachedPair(modelPair.applySort(p,1));

						if(!node.seenEdges.contains(newP)) {
							node.edgesToCompute.add(newP);
							node.seenEdges.add(newP);
							addComputeForAllParents(node,newP);
							edgesToAdd.add(newP);
						}
					}
				}
			}

			if(g.getNumConnectedComponents() == 1) {
				break;
			}
		}

		cachedPairs = null;
	}

	private void addComputeForAllParents(AgreementLattice agree, IntPair modelPair) {
		for(AgreementLattice p : agree.parents) {
			p.addEdgeToCompute(modelPair);
		}
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
