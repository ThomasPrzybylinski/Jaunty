package workflow.graph.local;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import util.IntPair;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class AllLocalSymAdder_NEW_OLD extends EdgeManipulator {

	private class AdditionalState {
		LatticePart prev;
		Set<Integer> validLits;
	}

	private int numIters = 0;
	private int numComp = 0;
	private int numModels;
	private LinkedList<Integer> filter = new LinkedList<Integer>();
	private ArrayList<LinkedList<LatticePart>> latticeLevels = new ArrayList<LinkedList<LatticePart>>();
	
	public ArrayList<int[]> computed = new ArrayList<int[]>();

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		long start = System.currentTimeMillis();
		numIters = 1; //At least global
		numComp = 1;
		numModels = representatives.size();

		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();

		while(vc.size() < rep.length) {
			vc.createNextDefaultVar();
		}

		latticeLevels = new ArrayList<LinkedList<LatticePart>>(rep.length+1);

		for(int k = 0; k < rep.length+1; k++) {
			latticeLevels.add(new LinkedList<LatticePart>());
		}

		ClauseList cl = new ClauseList(new VariableContext());
		cl.fastAddAll(representatives);

		LocalModelSymClauses cls = new LocalModelSymClauses(cl);

		LatticePart glob = addGlobalSyms(g,cls); //find global symmetries
		LiteralGroup varGlob = getVarGroup(cls,glob.getAutoGroup());
		glob.varGroup = varGlob;
		LiteralGroup origGroup = glob.getAutoGroup();


		glob.autoGroup = getModelGroup(cls,glob.getAutoGroup());
//		System.out.println(glob.autoGroup);
		
		latticeLevels.get(0).add(glob);

		AdditionalState state = new AdditionalState();
		state.prev = glob;
		state.validLits = cls.curValidLits();

		localize(g,cls, rep.length, 0, varGlob, origGroup,state); //find local symmetries

		long endLocal = System.currentTimeMillis();

		setupLattice();

		addAllIsoEdges(g);

		long fullEnd = System.currentTimeMillis();

		System.out.println("Time: " + (fullEnd-start));
		System.out.println("Iso Time: " + (endLocal-start));
		System.out.println("Generate Time: " + (fullEnd-endLocal));

		//allSeen = getIsomorphicPairs(g,allSeen,glob);

		System.out.println(numIters);
		System.out.println(numComp);
		System.out.print("1N: ");
		for(int k = 1; k < g.getNumNodes(); k++) {
			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		}
		
		System.out.println();

	}

	private void localize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives, int numVars, int curFilterVar,
			LiteralGroup curGroup, LiteralGroup knownGroup, AdditionalState state) {
		TreeSet<IntPair> seen = new TreeSet<IntPair>();
		if(curFilterVar >= numVars) return;
		int nextFilterVar = curFilterVar+1;

		SchreierVector vec = new SchreierVector(curGroup);
		Set<Integer> existantLits = representatives.curValidLits();

		AdditionalState newState = new AdditionalState();
		newState.prev = state.prev;
		newState.validLits = new TreeSet<Integer>();
		newState.validLits.addAll(state.validLits);

		Iterator<Integer> iter = newState.validLits.iterator();
		while(iter.hasNext()) {
			int i = iter.next();
			if(!existantLits.contains(i)) {
				iter.remove();
				//				newState.validLits.remove(i);
			}
		}

		for(int k = nextFilterVar; k <= numVars; k++) {
			if(state.validLits.contains(k) && vec.getRep(k) == k) {
				numIters++;
				filter.add(k);

				for(int i : existantLits) {
					if(Math.abs(vec.getRep(i)) < k) {
						newState.validLits.remove(i);
					}
				}

				doLocalize(g,representatives,numVars,k,k,
						knownGroup.getStabSubGroup(representatives.litToDualVar(k)).reduce(),
						newState);
				filter.removeLast();
			}
			if(state.validLits.contains(-k) && vec.getRep(-k) == -k) {
				numIters++;
				filter.add(-k);
				
				for(int i : existantLits) {
					int rep = vec.getRep(i);
					if(rep == k) {
						newState.validLits.remove(i);
					}
				}

				if(Arrays.equals(getFilterArray(),new int[]{-1,-10})) {
					System.out.print("");
				}
				
				doLocalize(g,representatives,numVars,k,-k,
						knownGroup.getStabSubGroup(representatives.litToDualVar(-k)).reduce(), //-k and k should have same stab sub groups
						newState); 
				filter.removeLast();
			}
		}
	}

	private TreeSet<IntPair> doLocalize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives,
			int numVars, int curFilterVar, int filter,
			LiteralGroup isoGroup, AdditionalState state) {

		//new recursive level
		representatives.post();

		int size = representatives.curValidModels();
		representatives.addCondition(filter);
		int newSize = representatives.curValidModels();


		LiteralGroup group = null;
		TreeSet<IntPair> pairs = new TreeSet<IntPair>();

//		System.out.println(this.filter);

		if(newSize < size && newSize > 2) {
			numComp++;
			
			computed.add(getFilterArray());
			
			
			ClauseList cl = representatives.getCurList(true);
			TreeSet<Integer> existantVars = getExistantGroup(cl);

			group = getSymGroup(cl,isoGroup);
			LiteralGroup modelGroup = getModelGroup(representatives, group).reduce();
			LiteralGroup varGroup = getVarGroup(representatives,group).reduce();

			int[] curFilter = getFilterArray();


			LatticePart lat = new LatticePart(curFilter,modelGroup);
			latticeLevels.get(curFilter.length).add(lat);
			lat.parents.add(state.prev);
			
//			System.out.println(modelGroup);
			//			lat.varGroup = varGroup;
			//			lat.fullGroup = group;

			//			System.out.println();
//						System.out.println(varGroup);

			SchreierVector vec = new SchreierVector(group);

			addEdges(g, lat, existantVars, vec);

			AdditionalState newState = new AdditionalState();
			newState.prev = lat;
			newState.validLits = new TreeSet<Integer>();
			newState.validLits.addAll(state.validLits);

			localize(g,representatives,numVars,curFilterVar, varGroup, group,newState);
		} else if(newSize == 2) {
			numComp++;
			
			computed.add(getFilterArray());
			
			getSyms2Models(g, representatives);
		} else if(newSize >= size || newSize == 0 || newSize == 1) {
			numIters--;
		}
//		else if(newSize > 2) {
//			AdditionalState newState = new AdditionalState();
//			newState.prev = state.prev;
//			newState.validLits = new TreeSet<Integer>();
//			newState.validLits.addAll(state.validLits);
//
//			LiteralGroup varGroup = getVarGroup(representatives,isoGroup).reduce();
//			TreeSet<IntPair> childrenSeen = localize(g,representatives,numVars,curFilterVar, varGroup, isoGroup,newState);
//			pairs.addAll(getIsomorphicPairs(g,childrenSeen,group));
//		}

		representatives.pop();

		return pairs;
	}

	private void addAllIsoEdges(PossiblyDenseGraph<int[]> g) {
		for(int k = latticeLevels.size() - 1; k >= 0; k--) {
			for(LatticePart lp : latticeLevels.get(k)) {
				computeIso(lp);
			}
		}

		LatticePart head = latticeLevels.get(0).getFirst();

		for(IntPair pairs : getPairs(head)) {
			g.setEdgeWeight(pairs.getI1()-1,pairs.getI2()-1,0);
		}

	}



	private void computeIso(LatticePart lp) {
		TreeSet<IntPair> seen = getPairs(lp);

		seen.addAll(lp.getPairs());

		if(lp != latticeLevels.get(0).getFirst()) {
			lp.getPairs().clear();
			lp.getComputePairs().clear(); //For memory purposes
		}

		for(LatticePart p : lp.getParents()) {
			p.getComputePairs().addAll(seen);
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
				IntPair newP = pair.apply(p);

				if(!seen.contains(newP)) {
					seen.add(newP);
					toCompute.push(newP);
				}
			}
		}
		return seen;
	}



	private void setupLattice() {
		for(int k = 0; k < latticeLevels.size(); k++) {
			for(LatticePart p : latticeLevels.get(k)) {
				//				for(int i = k+1; i < latticeLevels.size(); i++) {
				int i = k+1;
				for(LatticePart p2 : latticeLevels.get(i)) {
					if(isSubset(p,p2)) {
						p2.addParent(p);
					}
				}
				//				}
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







	private int[] getFilterArray() {
		int[] curFilter = new int[this.filter.size()];
		int index = 0;

		for(int i : this.filter) {
			curFilter[index] = i;
			index++;
		}

		return curFilter;
	}



	private TreeSet<IntPair> getIsomorphicPairs(
			PossiblyDenseGraph<int[]> g, TreeSet<IntPair> childrenSeen,
			LiteralGroup group) {
		TreeSet<IntPair> seen = new TreeSet<IntPair>();

		LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
		toCompute.addAll(childrenSeen);
		seen.addAll(childrenSeen);

		while(!toCompute.isEmpty()) {
			IntPair pair = toCompute.poll();

			for(LiteralPermutation p : group.getGenerators()) {
				IntPair newP = pair.apply(p);

				if(!seen.contains(newP)) {
					seen.add(newP);
					g.setEdgeWeight(newP.getI1()-1,newP.getI2()-1,0);
					toCompute.push(newP);
				}
			}
		}

		return seen;
	}



	private boolean addEdges(PossiblyDenseGraph<int[]> g,  LatticePart part,
			TreeSet<Integer> existantVars, SchreierVector vec) {
		boolean continueSym;
		continueSym = false;
		for(int j = 0; j < numModels; j++) {
			int var1 = j+1;
			if(!existantVars.contains(var1)) continue;

			for(int h = j+1; h < numModels; h++) {
				int var2 = h+1;
				if(!existantVars.contains(var2)) continue;

				if(vec.sameOrbit(var1,var2)) { 
					//if(ds.sameSet(j+1,h+1)) {
					part.addPair(new IntPair(var1,var2));
					g.setEdgeWeight(j,h,0);	
				}

//				if(!g.areAdjacent(j,h) || g.getEdgeWeight(j,h) != 0) {
//					continueSym = true;
//				}
			}
		}
		return continueSym;
	}



	private TreeSet<Integer> getExistantGroup(ClauseList cl) {
		TreeSet<Integer> existantVars = new TreeSet<Integer>();

		for(int[] c : cl.getClauses()) {
			for(int i : c) {
				existantVars.add(Math.abs(i));
			}
		}
		return existantVars;
	}



	private NaiveLiteralGroup getSymGroup(ClauseList cl) {
		return getSymGroup(cl,null);
	}

	private NaiveLiteralGroup getSymGroup(ClauseList cl, LiteralGroup isoGroup) {
		RealSymFinder finder = new RealSymFinder(cl);

		if(isoGroup != null) {
			finder.addKnownSubgroup(isoGroup);
		}

		List<int[]> genList = finder.getSyms();
		LinkedList<LiteralPermutation> perms = new LinkedList<LiteralPermutation>();

		for(int[] i : genList) {
			perms.add(new LiteralPermutation(i));
		}

		NaiveLiteralGroup group = new NaiveLiteralGroup(perms);

		//		System.out.println();
		//		System.out.println(group);


		return group;
	}

	private LatticePart addGlobalSyms(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives) {

		ClauseList cl = representatives.getCurList(true);

		NaiveLiteralGroup group = getSymGroup(cl);

		SchreierVector vec = new SchreierVector(group);

		LatticePart ret = new LatticePart(new int[]{}, group);

		for(int j = 0; j < numModels; j++) {
			int var1 = j+1;

			for(int h = j+1; h < numModels; h++) {
				int var2 = h+1;

				if(vec.sameOrbit(var1,var2)) {
					ret.addPair(new IntPair(var1,var2));
					g.setEdgeWeight(var1-1,var2-1,0);
				}
			}
		}

		return ret;
	}

	private LiteralGroup getVarGroup(LocalModelSymClauses representatives,
			LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			gens.add(representatives.getVarPart(perm));
		}

		return group.getNewInstance(gens);

	}

	private LiteralGroup getModelGroup(LocalModelSymClauses representatives, LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			gens.add(representatives.getModelPart(perm));
		}

		return group.getNewInstance(gens);

	}



	private void getSyms2Models(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives) {
		ClauseList cl = representatives.getCurList();

		int[] existantVars = new int[2];

		full: for(int[] c : cl.getClauses()) {
			for(int i : c) {
				if(existantVars[0] == 0) {
					existantVars[0] = i;
				} else if (existantVars[0] != i) {
					existantVars[1] = i;
					break full;
				}
			}
		}
		LinkedList<LiteralPermutation> dummy = new LinkedList<LiteralPermutation>();
		dummy.add(new LiteralPermutation(representatives.numTotalModels()));
		//Just so it isn't null. This is a leaf so the group doesn't matter
		LatticePart l = new LatticePart(getFilterArray(),new NaiveLiteralGroup(dummy));
		l.addPair(new IntPair(existantVars[0],existantVars[1]));
		latticeLevels.get(getFilterArray().length).add(l);

		g.setEdgeWeight(existantVars[0]-1,existantVars[1]-1,0);
	}

	//Assumes agreers is a sublist of representatives
	private int[] getLocalToGlobalIndecies(List<int[]> agreers,	List<int[]> representatives) {
		int[] ret = new int[agreers.size()];
		int repIndex = 0;

		for(int k = 0; k < agreers.size(); k++) {
			boolean found = false;
			while(!found) {
				if(Arrays.equals(agreers.get(k),representatives.get(repIndex))) {
					found = true;
					ret[k] = repIndex;
				}
				repIndex++;
			}
		}

		return ret;
	}



	@Override
	public boolean isSimple() {
		return true;
	}

	private class LatticePart {
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
