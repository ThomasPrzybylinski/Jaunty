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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import util.IntPair;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class GlobalPruningAllLocalSymAdder_Old extends EdgeManipulator {
	private class AdditionalState {
		LatticePart prev;
		Set<Integer> validLits;
	}

	private int numIters = 0;
	private int numModels;
	private LinkedList<Integer> filter = new LinkedList<Integer>();
	private ArrayList<LinkedList<LatticePart>> latticeLevels = new ArrayList<LinkedList<LatticePart>>();

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		long start = System.currentTimeMillis();
		numIters = 0;
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

		RealSymFinder pruner = new RealSymFinder(cl);

		LocalModelSymClauses cls = new LocalModelSymClauses(cl);

		LatticePart glob = addGlobalSyms(g,cls); //find global symmetries
		LiteralGroup varGlob = getVarGroup(cls,glob.getAutoGroup());
		glob.varGroup = varGlob;
		
		
//		System.out.println(varGlob);
//		System.out.println((new SchreierVector(varGlob)).transcribeOrbits());
		
		LiteralGroup origGroup = glob.getAutoGroup();


		glob.autoGroup = getModelGroup(cls,glob.getAutoGroup());
		latticeLevels.get(0).add(glob);

		AdditionalState state = new AdditionalState();
		state.prev = glob;
		state.validLits = cls.curValidLits();

		localize(g,cls, rep.length, 0, varGlob, pruner); //find local symmetries

		long endLocal = System.currentTimeMillis();

		addAllIsoEdges(g);

		long fullEnd = System.currentTimeMillis();
//		System.out.println(varGlob);
//		System.out.println((new SchreierVector(varGlob)).transcribeOrbits());
		
		System.out.println("Time: " + (fullEnd-start));
		System.out.println("Iso Time: " + (endLocal-start));
		System.out.println("Generate Time: " + (fullEnd-endLocal));

		//allSeen = getIsomorphicPairs(g,allSeen,glob);

		System.out.println(numIters);
		System.out.print("1GP: ");
		for(int k = 1; k < g.getNumNodes(); k++) {
			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		}
		System.out.println();

	}

	private void localize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives, int numVars, int curFilterVar,
			LiteralGroup varGroup, RealSymFinder pruner) {
		TreeSet<IntPair> seen = new TreeSet<IntPair>();
		if(curFilterVar > numVars) return;
		int nextFilterVar = curFilterVar+1;

		for(int k = nextFilterVar; k <= numVars; k++) {
			filter.add(k);
			int[] toTest = getFilterArray();

			if(pruner.getSmallerSubsetIfPossible(toTest,varGroup) == null) {
				doLocalize(g,representatives,numVars,k,k,varGroup, pruner);
				numIters++;
			}
			filter.removeLast();
			


			filter.add(-k);
			toTest = getFilterArray();

			if(pruner.getSmallerSubsetIfPossible(toTest,varGroup) == null) {
				doLocalize(g,representatives,numVars,k,-k,
						varGroup, //-k and k should have same stab sub groups
						pruner);
				numIters++;
			}
			filter.removeLast();

		}
	}

	private void doLocalize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives,
			int numVars, int curFilterVar, int filter,
			LiteralGroup varGroup, RealSymFinder pruner) {

		//new recursive level
		representatives.post();

		int size = representatives.curValidModels();
		representatives.addCondition(filter);
		int newSize = representatives.curValidModels();


		LiteralGroup group = null;

//		System.out.println(this.filter);

		if(newSize < size && newSize > 2) {
			ClauseList cl = representatives.getCurList(true);
			TreeSet<Integer> existantVars = getExistantGroup(cl);

			group = getSymGroup(cl);
			LiteralGroup modelGroup = getModelGroup(representatives, group).reduce();

			int[] curFilter = getFilterArray();


			LatticePart lat = new LatticePart(curFilter,modelGroup);
			latticeLevels.get(curFilter.length).add(lat);

			SchreierVector vec = new SchreierVector(group);

			addEdges(g, lat, existantVars, vec);

			AdditionalState newState = new AdditionalState();
			newState.prev = lat;
			newState.validLits = new TreeSet<Integer>();

			localize(g,representatives,numVars,curFilterVar, varGroup,pruner);
		} else if(newSize == 2) {
			getSyms2Models(g, representatives);
		}

		representatives.pop();
	}

	private void addAllIsoEdges(PossiblyDenseGraph<int[]> g) {
		LatticePart lp = latticeLevels.get(0).get(0);
		LiteralGroup glob = lp.getAutoGroup();

		LinkedList<IntPair> toCompute = new LinkedList<IntPair>();

		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i < g.getNumNodes(); i++) {
				if(g.areAdjacent(k,i)) {
					toCompute.add(new IntPair(k+1,i+1));
				}
			}
		}

		TreeSet<IntPair> seen = new TreeSet<IntPair>();

		seen.addAll(toCompute);

		while(!toCompute.isEmpty()) {
			IntPair pair = toCompute.poll();

			for(LiteralPermutation p : glob.getGenerators()) {
				IntPair newP = pair.apply(p);

				if(!seen.contains(newP)) {
					seen.add(newP);
					toCompute.push(newP);
					g.setAdjacent(newP.getI1()-1,newP.getI2()-1);
				}
			}
		}
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

				if(!g.areAdjacent(j,h) || g.getEdgeWeight(j,h) != 0) {
					continueSym = true;
				}
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
