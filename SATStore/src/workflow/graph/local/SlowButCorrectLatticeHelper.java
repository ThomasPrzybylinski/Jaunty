package workflow.graph.local;

import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import util.IntPair;
import util.PermutationUtil;
import util.lit.LitsMap;

public class SlowButCorrectLatticeHelper extends LatticeHelper {
	private ArrayList<List<LatticePart>> latticeLevels;
	private LitsMap<LatticePart> map;

	public SlowButCorrectLatticeHelper(int numVars) {
		map = new LitsMap<LatticePart>(numVars);

		latticeLevels = new ArrayList<List<LatticePart>>(numVars+1);

		for(int k = 0; k < numVars+1; k++) {
			latticeLevels.add(new ArrayList<LatticePart>());//new LinkedList<LatticePart>());
		}
	}

	//Note: the ith generator of litSyms must be the ith generator of modelSyms
	@Override
	public void add(int[] filter, int[] full, LiteralGroup litSyms, LiteralGroup modelSyms) {
		LatticePart lp = new LatticePart(filter, litSyms, modelSyms);
		map.put(filter,lp);
		latticeLevels.get(filter.length).add(lp);
		
		LatticePart lp2 = new LatticePart(full, litSyms, modelSyms);
		map.put(full,lp2);
		latticeLevels.get(full.length).add(lp2);

		if(filter.length > 0) {
			int[] parent = getFilterExcluding(filter,filter.length-1);
			LatticePart parentLP = map.get(parent);
			lp.addParent(parentLP);
			lp2.addParent(parentLP);
			
			parentLP.addChild(lp);
			parentLP.addChild(lp2);
		}

		SchreierVector vec = new SchreierVector(modelSyms.reduce());

		for(int k = 1; k <= vec.getNumVars(); k++) {
			for(int i = k+1; i <= vec.getNumVars(); i++) {
				if(vec.sameOrbit(k,i)) {
					IntPair newPair = new IntPair(k,i);
					lp.pairs.add(newPair);
					lp2.pairs.add(newPair);
				}
			}
		}

	}

	private int[] getFilterExcluding(int[] filter, int excl) {
		int[] ret = new int[filter.length-1];

		int index = 0;

		for(int k = 0; k < filter.length; k++) {
			if(k == excl) continue;

			ret[index] = filter[k];
			index++;
		}

		return ret;
	}

	@Override
	public Set<IntPair> getAllEdges() {
		setupLattice();

		for(int k = latticeLevels.size() - 1; k >= 0; k--) {
			for(LatticePart lp : latticeLevels.get(k)) {
				computeIso(lp);
			}

			if(k != 0) {
				latticeLevels.get(k).clear();
			}
		}

		return latticeLevels.get(0).get(0).getPairs();
	}



	private void computeIso(LatticePart lp) {
		LitsMap<Object> seen = new LitsMap<Object>(lp.litGroup.getId().size());
		computeIso(lp,lp.filter,lp.pairs,seen);

		if(lp != latticeLevels.get(0).get(0)) {
			lp.getPairs().clear();//For memory purposes
		}
	}

	private class ToCompute {
		int[] filter;
		Set<IntPair> pairs;

		public ToCompute(int[] filter, Set<IntPair> pairs) {
			super();
			this.filter = filter;
			this.pairs = pairs;
		}
	}

	private void computeIso(LatticePart lp, int[] curFilter, Set<IntPair> curPairs, LitsMap<Object> seen) {
		List<LiteralPermutation> litSyms = lp.litGroup.getGenerators();
		List<LiteralPermutation> modSyms = lp.autoGroup.getGenerators();

		LinkedList<ToCompute> toCompute = new LinkedList<ToCompute>();
		LitsMap<Object> localSeen = new LitsMap<Object>(seen.getNumVars());

		toCompute.add(new ToCompute(curFilter,curPairs));

		while(!toCompute.isEmpty()) {
			ToCompute curComp = toCompute.poll();
			int[] compFilter = curComp.filter;

			for(int k = 0; k < litSyms.size(); k++) {
				LiteralPermutation litPerm = litSyms.get(k);
				LiteralPermutation modPerm = modSyms.get(k);

				int[] newFilter = PermutationUtil.permuteClause(compFilter,litPerm.asArray());

				Set<IntPair> newPairs = null;
				
				boolean neverSeen = !seen.contains(newFilter);

				if(lp.filter.length > 0 && neverSeen) {

					if(newPairs == null) {
						newPairs = getPairs(curComp,modPerm);
					}
					
					for(LatticePart par : lp.getParents()) {
						computeIso(par,newFilter,newPairs,seen);
					}
				}

				if(neverSeen) {

					if(newPairs == null) {
						newPairs = getPairs(curComp, modPerm);
					}

					seen.put(newFilter,null);//newPairs);
					localSeen.put(newFilter,null);
					toCompute.add(new ToCompute(newFilter,newPairs));
					
					distributePairs(newFilter,newPairs,seen);

//					for(int i = 0; i < newFilter.length; i++) {
//						int[] parentFilter = getFilterExcluding(newFilter,i);
//
//						if(map.contains(parentFilter)) {
//							map.get(parentFilter).pairs.addAll(newPairs);
//						}
//					}
				}
			}
		}
	}

	private void distributePairs(int[] newFilter, Set<IntPair> newPairs,
			LitsMap<Object> seen) {
		Set<Integer> parts = new TreeSet<Integer>();
		
		for(int i : newFilter) {
			parts.add(i);
		}
		
		distributePairs(getRoot(),parts,newFilter,newPairs,seen);
		
	}

	private void distributePairs(LatticePart root, Set<Integer> parts,
			int[] newFilter, Set<IntPair> newPairs, LitsMap<Object> seen) {
		boolean addPairsHere = true;
		
		for(LatticePart lp : root.children) {
			boolean valid = true;
			
			for(int i : lp.filter) {
				if(!parts.contains(i)) {
					valid = false;
					break;
				}
			}
			
			if(valid && !Arrays.equals(lp.filter,newFilter)) {
				distributePairs(lp,parts,newFilter,newPairs,seen);
				addPairsHere = false;
			}
		}
		
		if(addPairsHere) {
			root.pairs.addAll(newPairs);
		}
		
	}

	private Set<IntPair> getPairs(ToCompute curComp, LiteralPermutation modPerm) {
		Set<IntPair> newPairs;
		newPairs = new TreeSet<IntPair>();

		for(IntPair ip : curComp.pairs) {
			newPairs.add(ip.applySort(modPerm));
		}
		return newPairs;
	}


	@Override
	public void clear() {
		latticeLevels.clear();
		map.clear();
	}


	private void setupLattice() {
		for(int k = 0; k < latticeLevels.size(); k++) {
			for(LatticePart p : latticeLevels.get(k)) {
				//								for(int i = k+1; i < latticeLevels.size(); i++) {
				int i = k+1;
				for(LatticePart p2 : latticeLevels.get(i)) {
					if(isSubset(p,p2)) {
						p2.addParent(p);
					}
				}
				//							}
			}
		}
	}

	//True is p is a subset of p2
	private boolean isSubset(LatticePart p, LatticePart p2) {
		int p2Ind = 0;

		for(int k = 0; k < p.filter.length; k++) {
			int testLit = p.filter[k];
			int testVar = Math.abs(testLit);
			for(; p2Ind < p2.filter.length; p2Ind++) {
				int compareLit = p2.filter[p2Ind];
				int compareVar = Math.abs(compareLit);

				if(testVar < compareVar) {
					return false;
				} else if(testVar == compareVar) {
					if(testLit == compareLit) {
						break;
					} else {
						return false;
					}
				}

				if(p2Ind == p2.filter.length - 1) return false; //Did not find the kth literal
			}
		}

		return true;
	}

	
	public LatticePart getRoot() {
		return latticeLevels.get(0).get(0);
	}

	private	class LatticePart {
		private int[] filter;
		private List<LatticePart> parents;
		private List<LatticePart> children;
		private LiteralGroup autoGroup;
		private LiteralGroup litGroup;
		private Set<IntPair> pairs;
		public LiteralGroup varGroup; //for Debugging
		public LiteralGroup fullGroup; //for Debugging

		public LatticePart(int[] filter, LiteralGroup litGroup,  LiteralGroup autoGroup) {
			super();
			this.filter = filter;
			this.autoGroup = autoGroup;
			this.litGroup = litGroup;
			parents = new LinkedList<LatticePart>();
			children = new LinkedList<LatticePart>();
			pairs = new TreeSet<IntPair>();
		}

		public void addParent(LatticePart parent) {
			this.parents.add(parent);
		}

		public List<LatticePart> getParents() {
			return parents;
		}
		
		public void addChild(LatticePart parent) {
			this.children.add(parent);
		}

		public List<LatticePart> getChildren() {
			return children;
		}

		public void addPair(IntPair p) {
			pairs.add(p);
		}

		public LiteralGroup getAutoGroup() {
			return autoGroup;
		}

		public Set<IntPair> getPairs() {
			return pairs;
		}

		public String toString() {
			return "Lat: " + Arrays.toString(filter);
		}
	}

}
