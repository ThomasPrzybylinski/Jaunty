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
import util.lit.LitSorter;
import util.lit.LitsMap;
import workflow.graph.EdgeManipulator;

@Deprecated
//Looks at all symmetries from agreement, not just of the two models
public class BFS_AllLocalSymAdder extends EdgeManipulator {
	private int numIters = 0;
	private int numModels;
	private ArrayList<LinkedList<LatticePart>> latticeLevels = new ArrayList<LinkedList<LatticePart>>();

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		numIters = 0;
		numModels = representatives.size();

		//		(new AgreementLocalSymAdder()).addEdges(g,representatives);

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

		bfs(g,cls,rep.length);

		setupLattice();

		addAllIsoEdges(g);

		System.out.println(numIters);
		System.out.print("1N: ");
		for(int k = 1; k < g.getNumNodes(); k++) {
			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		}
		System.out.println();

	}

	private void bfs(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives, int numVars) {
		TreeSet<Integer> validReps = new TreeSet<Integer>();

		ClauseList globList = representatives.getCurList(true);
		TreeSet<Integer> globVars = getExistantGroup(globList);
		LiteralGroup glob = getSymGroup(globList);

		//		testStuff(g, numVars, glob);

//		System.out.println(getVarGroup(representatives, glob).reduce());

		SchreierVector varVec = new SchreierVector(getVarGroup(representatives,glob));

		for(int k = 1; k <= numVars; k++) {
			if(k == 0) continue;

			validReps.add(k);
			if(varVec.getRep(k) == k) {
				latticeLevels.get(1).add(new LatticePart(new int[]{k}));
			}

			validReps.add(-k);
			if(varVec.getRep(-k) == -k) {
				latticeLevels.get(1).add(new LatticePart(new int[]{-k}));
			}
		}
		LatticePart globL = new LatticePart(new int[]{});
		globL.setAutoGroup(glob);
		globL.varVec = varVec;
		globL.numModels = representatives.curValidModels();
		addEdges(g,globL,globVars);

		latticeLevels.get(0).add(globL);

		representatives.post();
		for(int k = 1; k < latticeLevels.size(); k++) {
			lattice: for(LatticePart nextP : latticeLevels.get(k)) {
				numIters++;
				int[] next = nextP.filter;

				representatives.pop();
				representatives.post();

				for(int i : next) {
					representatives.addCondition(i);
				}

				ClauseList cl = representatives.getCurList(true);

				nextP.numModels = representatives.curValidModels();

				
//				System.out.println(Arrays.toString(next));
				
				
				if(cl.getClauses().size() > 0 && nextP.numModels >= 2) {

					for(LatticePart parent : nextP.parents) {
						if(parent.numModels == nextP.numModels)  {
							//No difference now, may have differences later
							nextP.setAutoGroup(parent.autoGroup);
							nextP.addMore = true;
							nextP.varVec = parent.varVec;
							nextP.numModels = parent.numModels;
							continue lattice;
						}
					}

					TreeSet<Integer> existantVars = getExistantGroup(cl);

					LiteralGroup curGroup = getSymGroup(cl);

					
//					System.out.println(getModelGroup(representatives, curGroup).reduce());
//					System.out.println(getVarGroup(representatives, curGroup).reduce());

					nextP.setAutoGroup(curGroup);
					nextP.varVec = new SchreierVector(getVarGroup(representatives,curGroup));
					nextP.numModels = representatives.curValidModels();

					addEdges(g,nextP,existantVars);
					//I have commented this out because it's not clear that I can just stop because all the edges have been filled out
					//since I can't say for certain that all isomorphic edges have been filled out (e.g going lower may connected models
					//with more subsets
					//nextP.addMore = addNew;
				}

			}

		LitsMap<Object> filters = new LitsMap<Object>(numVars);
		for(LatticePart p : latticeLevels.get(k)) {
			int[] newRep = new int[p.filter.length+1];
			if(p.numModels <= 2) continue;
			for(int rep : validReps) {
				

				boolean add = true;
				for(int i = 0; i < p.filter.length; i++) {
					newRep[i] = p.filter[i];
					if(newRep[i] == rep
							|| newRep[i] == -rep) add = false;
				}

				if(add) {

					newRep[newRep.length-1] = rep;

					LitSorter.inPlaceSort(newRep);
					if(!filters.contains(newRep)) {
						filters.put(newRep,null);
						LatticePart toAdd =	new LatticePart(newRep);
						if(isValidRep(toAdd,p,rep)) {
							latticeLevels.get(k+1).add(toAdd);
							newRep = new int[p.filter.length+1]; //Only create new when added, to reduce object creation times
						} else {
							//						System.out.print("("+(k+1)+","+Arrays.toString(newRep)+")");
						}
					}
				}
			}
		}
		//			System.out.println();

		}
	}



	private boolean isValidRep(LatticePart toAdd, LatticePart builtFrom, int newRep) {
		//int newRep = toAdd.filter[toAdd.filter.length-1];
		//The smallest element must be the one built from global symmetry
		if(Math.abs(newRep) <= Math.abs(builtFrom.filter[0])) return false;
		if(builtFrom.varVec.getRep(newRep) != newRep) return false;
		//		if(part.varVec.getRep(newRep) != newRep) {
		//			return false;
		//		}

		//		for(LatticePart p : part.parents) {
		//			if(p.varVec.getRep(newRep) != newRep || p.numModels == part.numModels) {
		//				return false;
		//			}
		//		}

		int level = builtFrom.filter.length;

		for(LatticePart p : latticeLevels.get(level)) {
			if(isSubset(p,toAdd)) {
				toAdd.addParent(p);
				int rep2 = getDifference(p,toAdd);

				if (p.numModels <= 2 || p.varVec.getRep(rep2) != rep2 || !p.addMore) {
					if(p.numModels > 2 && p.varVec.getRep(rep2) != rep2) {
//						System.out.print("#");
					}
					return false;
				}
			}
		}

		return true;
	}

	//get an elt of the filter of toAdd not in p
	//p must be a subset of toAdd in the same order
	private int getDifference(LatticePart p, LatticePart toAdd) {
		int k = 0;
		for(k = 0; k < p.filter.length; k++) {
			if(p.filter[k] != toAdd.filter[k]) break;
		}
		return toAdd.filter[k];
	}

	private boolean addEdges(PossiblyDenseGraph<int[]> g,  LatticePart part,
			TreeSet<Integer> existantVars) {
		boolean continueSym;
		continueSym = false;
		for(int j = 0; j < numModels; j++) {
			int var1 = j+1;
			if(!existantVars.contains(var1)) continue;

			for(int h = j+1; h < numModels; h++) {
				int var2 = h+1;
				if(!existantVars.contains(var2)) continue;

				if(part.vec.sameOrbit(var1,var2)) { 
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

	//True is p is a subset of p2
	private boolean isSubset(LatticePart p, LatticePart p2) {
		return isSubset(p.filter,p2.filter);
	}

	private boolean isSubset(int[] f1, int[] f2) {
		int f2Ind = 0;

		for(int k = 0; k < f1.length; k++) {
			int testLit = f1[k];
			int testVar = Math.abs(testLit);
			for(; f2Ind < f2.length; f2Ind++) {
				int compareLit = f2[f2Ind];
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

				if(f2Ind == f2.length - 1) return false; //Did not find the kth literal
			}
		}

		return true;
	}

	private void setupLattice() {
		for(int k = 0; k < latticeLevels.size(); k++) {
			for(LatticePart p : latticeLevels.get(k)) {
				for(int i = k+1; i < latticeLevels.size(); i++) {
					//					int i = k+1;
					for(LatticePart p2 : latticeLevels.get(i)) {
						if(isSubset(p,p2)) {
							p2.addParent(p);
						}
					}
				}
			}
		}
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

	private NaiveLiteralGroup getSymGroup(ClauseList cl) {
		RealSymFinder finder = new RealSymFinder(cl);
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

	private LiteralGroup getVarGroup(LocalModelSymClauses representatives,
			LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			LiteralPermutation vperm = representatives.getVarPart(perm);
			if(vperm == null) {
				vperm = representatives.getVarPart(perm);
				return null;
			}
			gens.add(vperm);
		}

		return group.getNewInstance(gens);

	}

	private class LatticePart {
		private int[] filter;
		private List<LatticePart> parents;
		private LiteralGroup autoGroup;
		private SchreierVector vec;
		private SchreierVector varVec;
		private Set<IntPair> pairs;
		private Set<IntPair> toCompute;
		private int numModels;
		private boolean addMore = true;

		public LatticePart(int[] filter) {
			super();
			this.filter = filter;

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

		public void setAutoGroup(LiteralGroup autoGroup) {
			this.autoGroup = autoGroup;
			this.autoGroup = autoGroup;
			vec = new SchreierVector(autoGroup);
		}

		public LiteralGroup getAutoGroup() {
			return autoGroup;
		}

//		public void addComputePair(IntPair p) {
//			toCompute.add(p);
//		}

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



	@Override
	public boolean isSimple() {
		return true;
	}

}
