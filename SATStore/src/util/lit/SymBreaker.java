package util.lit;

import group.LiteralGroup;
import group.LiteralPermutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.math.util.MathUtils;
import org.sat4j.specs.ContradictionException;

import util.IntPair;

public class SymBreaker {
	private HashMap<IntPair, Integer> equalVars = new HashMap<IntPair,Integer>();
	private int numClausesAdded = 0;
	private long numLitsAdded = 0;

	public List<int[]> getSmallSymBreakClause(int[] condition,
			LiteralPermutation perm) throws ContradictionException {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		int firstCyc = perm.getFirstUnstableVar();
		if(firstCyc != 0) {
			int map = perm.imageOf(firstCyc);

			int[] clause;
			if(map == -firstCyc) {
				clause = new int[condition.length+1];	
			} else {
				clause = new int[condition.length+2];
			}



			for(int k = 0; k < condition.length; k++) {
				clause[k] = -condition[k];
			}

			clause[condition.length] = -firstCyc;
			if(map != -firstCyc) {
				clause[condition.length+1] = map;
			}
			LitSorter.inPlaceSort(clause);

			numClausesAdded++;
			numLitsAdded += clause.length;
			ret.add(clause);
		}
		return ret;
	}

	public List<int[]> getFullSymBreakClauses(LiteralGroup globalGroup, int[] condition)
			throws ContradictionException {
		return getFullSymBreakClauses(globalGroup,condition,Integer.MAX_VALUE);
	}
		
	public List<int[]> getFullSymBreakClauses(LiteralGroup globalGroup, int[] condition,
				int maxCl) throws ContradictionException {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
		 	ret.addAll(getFullBreakingClauseForPerm(condition, perm,maxCl));
		}
		
		return ret;
	}

	public List<int[]> getFullBreakingClauseForPerm(int[] condition,
			LiteralPermutation perm)
					throws ContradictionException {
		return getFullBreakingClauseForPerm(condition,perm,Integer.MAX_VALUE);
	}
	public List<int[]> getFullBreakingClauseForPerm(int[] condition,
			LiteralPermutation perm, int maxCl)  throws ContradictionException {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		LinkedList<Integer> unstableVarsSeenSoFar = new LinkedList<Integer>();
		HashSet<IntPair> pairsSeen = new HashSet<IntPair>();
		int curClAdded = 0;
		for(int k = 1; k <= perm.size() && curClAdded < maxCl; k++) {
			int map = perm.imageOf(k);
			
			if(map == k) continue;

			if(map == -k && unstableVarsSeenSoFar.size() != 0) {
				break; //Everything will be tautalogous since map will never be equal to k
			}
			
			IntPair oppPair;
			if(map < 0 && Math.abs(map) < Math.abs(k)) {
				oppPair = new IntPair(-map,-k);
			} else {
				oppPair = new IntPair(map,k);
			}

			 
			if(pairsSeen.contains(oppPair)) continue; //Represents the end of a cycle.

			IntPair truePair = new IntPair(k,map);
			pairsSeen.add(truePair);

			int equalVar = 0;
			if(k < perm.size() && equalVars.containsKey(truePair)) {
				equalVar = equalVars.get(truePair);
			} else {
				equalVar = perm.size() + equalVars.size()+1;//solver.newVar(solver.nVars()+1);

//				System.out.println(solver.nVars());
				equalVars.put(truePair,equalVar);
				int[] newClause1 = new int[3];
				newClause1[0] = -k;
				newClause1[1] = -map;
				newClause1[2] = equalVar; //(k AND map) -> equal

				int[] newClause2 = new int[3];
				newClause2[0] = k;
				newClause2[1] = map;
				newClause2[2] = equalVar; //(!k AND !map) -> equal

				int[] newClause3 = new int[3];
				newClause3[0] = k;
				newClause3[1] = -map;
				newClause3[2] = -equalVar; //(!k AND map) -> !equal

				int[] newClause4 = new int[3];
				newClause4[0] = -k;
				newClause4[1] = map;
				newClause4[2] = -equalVar; //(k AND !map) -> !equal

				//So equal IFF k == map
				//curClAdded += 4; //Don't count for maxCL
				numClausesAdded += 4;
				numLitsAdded += newClause1.length + newClause2.length + newClause3.length + newClause4.length;
				
				ret.add(newClause1);
				ret.add(newClause2);
				ret.add(newClause3);
				ret.add(newClause4);
			}

			int[] clause;
			if(map == -k) {
				clause = new int[condition.length+unstableVarsSeenSoFar.size()+1];	
			} else {
				clause = new int[condition.length+unstableVarsSeenSoFar.size()+2];
			}

			int curIndex = 0;
			for(int i = 0; i < condition.length; i++) {
				clause[curIndex] = -condition[i];
				curIndex++;
			}

			for(int i : unstableVarsSeenSoFar) {
				clause[curIndex] = -i;
				curIndex++;
			}

			unstableVarsSeenSoFar.add(equalVar);

			clause[curIndex] = -k;
			if(map != -k) {
				curIndex++;
				clause[curIndex] = map;
			}
			LitSorter.inPlaceSort(clause);

			numClausesAdded++;
			numLitsAdded += clause.length;
//			System.out.println(Arrays.toString(clause));
			ret.add(clause);
		}
		return ret;
	}

	public List<int[]> getAlternateSymBreakClauses(LiteralGroup globalGroup, int[] condition)
			throws ContradictionException {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
			ret.addAll(getAltBreakingClauseForPerm(condition, perm));

		}
		return ret;
	}

	public List<int[]> getAltBreakingClauseForPerm(int[] condition,	LiteralPermutation perm)
			throws ContradictionException {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		int unstable = perm.getFirstUnstableVar();
		IntList cycLens = new ArrayIntList();
		boolean[] used = new boolean[perm.size()+1];
		
		while(unstable != 0) {
			IntList cycle = perm.getCycleWith(unstable);
			
			for(int k = 0; k < cycle.size(); k++) {
				used[Math.abs(cycle.get(k))] = true;
			}

			int len = cycle.size();
			boolean ok = true;
			for(int k = 0; k < cycLens.size(); k++) {
				int otherLen = cycLens.get(k);
				if(MathUtils.gcd(len,otherLen) != 1) {
					ok = false;
					break;
				}
			}
			
			
			if(ok) {
				cycLens.add(cycle.size());
				ret.addAll(getCycleAltBreakingClauses(condition, unstable, cycle));
				
				///TEMPORARY (or permanent for speed);
//				break;
			}
			while(used[unstable]) {
				unstable = perm.getUnstableVarAfter(unstable);
			}
		}
		return ret;
	}

	//Either unstable is true, or everyone is false
	private List<int[]> getCycleAltBreakingClauses(int[] condition,
			int unstable, IntList cycle) throws ContradictionException {
		
		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		
		for(int k = 1; k < cycle.size(); k++) {
			int other = cycle.get(k);
			int[] toAdd = new int[condition.length+2];
			int condInd = 0;
			boolean unstabAdded = false;
			boolean otherAdded = false;

			int absUnstab = Math.abs(unstable);
			int absOther = Math.abs(other);

			int j = 0;
			for(; j < toAdd.length && condInd < condition.length; j++) {
				int absCond = Math.abs(condition[condInd]);
				if(!unstabAdded && absUnstab < absCond) {
					toAdd[j] = unstable;
					unstabAdded = true;
				} else if(!otherAdded && absOther < absCond) {
					toAdd[j] = -other;
					otherAdded = true;
				} else {
					toAdd[j] = -condition[condInd];
					condInd++;
				}
			}

			if(j < toAdd.length) {
				if(!unstabAdded) {
					toAdd[j] = -unstable;
					j++;
				}

				if(!otherAdded) {
					toAdd[j] = other;
				}
			}
			
			if(toAdd.length == 2 && toAdd[0] == -toAdd[1]) {
				break;
			}
//			LitSorter.inPlaceSort(toAdd);
			numClausesAdded++;
			numLitsAdded += toAdd.length;
			ret.add(toAdd);
		}
		
		return ret;
	}

	public int getNumClausesAdded() {
		return numClausesAdded;
	}

	public long getNumLitsAdded() {
		return numLitsAdded;
	}
	
	
}
