package util.lit;

import group.LiteralGroup;
import group.LiteralPermutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.math.util.MathUtils;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import util.IntPair;

public class SymBreaker {
	private HashMap<IntPair, Integer> equalVars = new HashMap<IntPair,Integer>();

	public void addSmallSymBreakClause(int[] condition, ISolver solver,
			LiteralPermutation perm) throws ContradictionException {
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

			solver.addClause(new VecInt(clause));
		}
	}

	public void addFullSymBreakClauses(LiteralGroup globalGroup, int[] condition,
			ISolver solver) throws ContradictionException {
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
			addFullBreakingClauseForPerm(condition, solver, perm);

		}
	}

	public void addFullBreakingClauseForPerm(int[] condition,
			ISolver solver, LiteralPermutation perm)
					throws ContradictionException {
		LinkedList<Integer> unstableVarsSeenSoFar = new LinkedList<Integer>();
		HashSet<IntPair> pairsSeen = new HashSet<IntPair>();
		for(int k = 1; k <= perm.size(); k++) {
			int map = perm.imageOf(k);

			if(map == k) continue;

			if(map == -k && unstableVarsSeenSoFar.size() != 0) {
				break; //Everything will be tautalogous since map will never be equal to k
			}

			IntPair oppPair = new IntPair(map,k);
			if(pairsSeen.contains(oppPair)) continue; //Represents the end of a cycle.

			IntPair truePair = new IntPair(k,map);

			int equalVar = 0;
			if(k < perm.size() && equalVars.containsKey(truePair)) {
				equalVar = equalVars.get(truePair);
			} else {
				equalVar = solver.newVar(solver.nVars()+1);
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

				solver.addClause(new VecInt(newClause1));
				solver.addClause(new VecInt(newClause2));
				solver.addClause(new VecInt(newClause3));
				solver.addClause(new VecInt(newClause4));
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

			solver.addClause(new VecInt(clause));
		}
	}

	public void addAlternateSymBreakClauses(LiteralGroup globalGroup, int[] condition,
			ISolver solver) throws ContradictionException {
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
			addAltBreakingClauseForPerm(condition, solver, perm);

		}
	}

	public void addAltBreakingClauseForPerm(int[] condition, ISolver solver,
			LiteralPermutation perm) throws ContradictionException {
		int unstable = perm.getFirstUnstableVar();
		IntList cycLens = new ArrayIntList();
		while(unstable != 0) {
			IntList cycle = perm.getCycleWith(unstable);
			
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
				addCycleAltBreakingClauses(condition, solver, unstable, cycle);
				
				///TEMPORARY (or permanent for speed);
				break;
			}
			
			unstable = perm.getUnstableVarAfter(unstable);
		}

	}

	//Either unstable is true, or everyone is false
	private void addCycleAltBreakingClauses(int[] condition, ISolver solver,
			int unstable, IntList cycle) throws ContradictionException {
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
					toAdd[j] = unstable;
					j++;
				}

				if(!otherAdded) {
					toAdd[j] = -other;
				}
			}

			solver.addClause(new VecInt(toAdd));
		}
	}
}
