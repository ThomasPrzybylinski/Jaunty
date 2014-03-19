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

public class SomewhatFlawedLatticeHelper extends LatticeHelper {

	private ArrayList<List<LatticePart>> latticeLevels;

	public SomewhatFlawedLatticeHelper(int numVars) {
		latticeLevels = new ArrayList<List<LatticePart>>(numVars+1);

		for(int k = 0; k < numVars+1; k++) {
			latticeLevels.add(new ArrayList<LatticePart>());//new LinkedList<LatticePart>());
		}
	}

	//Note: the ith generator of litSyms must be the ith generator of modelSyms
	@Override
	public void add(int[] filter, int[] full, LiteralGroup litSyms, LiteralGroup modelSyms) {
		LatticePart lp = new LatticePart(filter,modelSyms);
		latticeLevels.get(filter.length).add(lp);

		SchreierVector vec = new SchreierVector(modelSyms.reduce());

		for(int k = 1; k <= vec.getNumVars(); k++) {
			for(int i = k+1; i <= vec.getNumVars(); i++) {
				if(vec.sameOrbit(k,i)) {
					lp.pairs.add(new IntPair(k,i));
				}
			}
		}

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

	private void computeIso(LatticePart lp) {
		TreeSet<IntPair> seen = getPairs(lp);

		seen.addAll(lp.getPairs());


		lp.getPairs().clear();
		lp.getComputePairs().clear(); //For memory purposes

		if(lp == latticeLevels.get(0).get(0)) {
			lp.pairs = seen;
		} else {
			for(LatticePart p : lp.getParents()) {
				p.getComputePairs().addAll(seen);
			}
		}

	}

	private TreeSet<IntPair> getPairs(LatticePart lp) {
		Set<IntPair> compPairs = lp.getComputePairs();

		LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
		toCompute.addAll(compPairs);

		TreeSet<IntPair> seen = new TreeSet<IntPair>();

		seen.addAll(toCompute);

		while(!toCompute.isEmpty()) {
			IntPair pair = toCompute.poll();

			for(LiteralPermutation p : lp.getAutoGroup().getGenerators()) {
				IntPair newP = pair.applySort(p);

				if(!seen.contains(newP)) {
					seen.add(newP);
					toCompute.push(newP);
				}
			}
		}
		return seen;
	}


	@Override
	public void clear() {
		latticeLevels.clear();
	}




	private	class LatticePart {
		private int[] filter;
		private List<LatticePart> parents;
		private LiteralGroup autoGroup;
		private Set<IntPair> pairs;
		private Set<IntPair> toCompute;
		public LiteralGroup varGroup; //for Debugging
		public LiteralGroup fullGroup; //for Debugging

		public LatticePart(int[] filter, LiteralGroup autoGroup) {
			super();
			this.filter = filter;
			this.autoGroup = autoGroup;
			parents = new LinkedList<LatticePart>();
			pairs = new TreeSet<IntPair>();
			toCompute = new TreeSet<IntPair>();
		}

		public void addParent(LatticePart parent) {
			this.parents.add(parent);
		}

		public List<LatticePart> getParents() {
			return parents;
		}

		public void addPair(IntPair p) {
			pairs.add(p);
		}

		public LiteralGroup getAutoGroup() {
			return autoGroup;
		}

		public void addComputePair(IntPair p) {
			toCompute.add(p);
		}

		public Set<IntPair> getComputePairs() {
			return toCompute;
		}

		public Set<IntPair> getPairs() {
			return pairs;
		}

		public String toString() {
			return "Lat: " + Arrays.toString(filter);
		}
	}

}
