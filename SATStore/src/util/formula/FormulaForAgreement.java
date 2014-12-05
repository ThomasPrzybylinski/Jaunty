package util.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import util.ArrayIntersectionHelper;
import util.IntPair;
import util.IntegralDisjointSet;
import util.PermutationUtil;
import util.lit.IntToIntLinkedHashMap;
import util.lit.IntToIntLinkedHashMap.EntryIter;
import util.lit.IntToIntLinkedHashMap.IntEntry;
import util.lit.LitSorter;
import util.lit.LitUtil;
import formula.Clause;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import formula.simple.DNF;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;

public class FormulaForAgreement {
	IntersectionHelper clausesForRemoval;
	ClauseList cl;
	int[][] clausesWithLit;
	boolean doSubsumption = true;
	int num;

	private class IntersectionHelper {
		private class Entry {
			private boolean set;
			private int index;
			private Entry next;
			private Entry prev;

			public Entry(Entry prev, int index) {
				this.index = index;
				set = false;
				this.prev = prev;
			}

			public String toString() {
				return Arrays.toString(cl.getClauses().get(index));
			}
		}


		private Entry headPointer;
		private Entry iterPointer = headPointer;
		private Entry[] entries;

		public IntersectionHelper(int size) {
			num = size;

			headPointer = new Entry(null,-1);
			entries = new Entry[size];

			entries[0] = new Entry(headPointer,0);
			headPointer.next = entries[0];
			Entry prev = entries[0];
			for(int k = 1; k < size; k++) {
				entries[k] = new Entry(prev,k);
				prev.next = entries[k];
				prev = entries[k];
			}
		}

		public void reset() {
			startIter();

			num = entries.length;

			Entry prev = headPointer;
			for(int k = 0; k < entries.length; k++) {
				entries[k].next = k == entries.length -1 ? null : entries[k+1];
				entries[k].prev = prev;
				entries[k].set = false;
				prev.next = entries[k];
				prev = entries[k];
			}

		}

		public void remove(int index) {
			remove(entries[index]);

		}

		public void startIter() {
			iterPointer = headPointer;
		}

		public int next() {
			iterPointer = iterPointer.next;
			return iterPointer.index;
		}

		public void remove() {
			remove(iterPointer);
		}

		private void remove(Entry entry) {
			if(entry.set) return;

			entry.set = true;
			entry.prev.next = entry.next;

			if(entry.next != null) {
				entry.next.prev = entry.prev;
			}
			num--;

		}

		public boolean hasNext() {
			return iterPointer.next != null;
		}

		public int size() {
			return num;
		}

		public String toString() {
			Entry cur = headPointer;
			StringBuilder sb = new StringBuilder();
			while(cur.next != null) {
				cur = cur.next;
				sb.append(cur +"->");
			}
			return sb.toString();
		}
	}

	public FormulaForAgreement(ClauseList cnf) {
		int numVars = cnf.getContext().size();
		this.cl = cnf;
		clausesForRemoval = new IntersectionHelper(cnf.size());

		int[] freq = new int[2*numVars+1];

		for(int[] cl : cnf.getClauses()) {
			for(int i : cl) {
				freq[LitUtil.getIndex(i,numVars)]++;
			}
		}

		clausesWithLit = new int[2*numVars+1][];
		IntList[] tempClausesWithLit = new IntList[2*numVars+1];

		for(int k = 0; k < freq.length; k++) {
			clausesWithLit[k] = new int[freq[k]];
			tempClausesWithLit[k] = new ArrayIntList(freq[k]);
//			Arrays.fill(clausesWithLit[k],-1);
		}
		freq = null;

		int index = 0;
		for(int[] cl : cnf.getClauses()) {
			for(int i : cl) {
				int litInd = LitUtil.getIndex(i,numVars);
				clausesWithLit[litInd][tempClausesWithLit[litInd].size()] = index;
				tempClausesWithLit[litInd].add(index);
			}
			index++;
		}

	}

	public CNF getCNFFromAgreement(int[] agreement) {
		clausesForRemoval.reset();

		int numVars = cl.getContext().size();

		BitSet bs = new BitSet(2*numVars+1);
		for(int i : agreement) {
			bs.set(LitUtil.getIndex(i,numVars));
		}

		for(int i : agreement) {
			computeRemoval(numVars, i);
		}

		CNF ret = new CNF(cl.getContext());
		List<int[]> clauses = cl.getClauses();

		if(doSubsumption) {
			doSubsumption(clauses,bs, numVars);
		}


		populateClauseList(numVars, bs, ret, clauses);

		return ret;
	}

	private void computeRemoval(int numVars, int i) {
		int[] toRemove = clausesWithLit[LitUtil.getIndex(i,numVars)];

		int minSize = Math.min(toRemove.length,clausesForRemoval.size());
		int maxSize = Math.max(toRemove.length,clausesForRemoval.size());

		boolean doLinear = minSize > maxSize/((Math.log(maxSize)/Math.log(2))-1); //approx the point where binary search isn't helpful

		if(doLinear) {
			clausesForRemoval.startIter();
			int removeIndex = 0;
			while(clausesForRemoval.hasNext()) {
				Integer cur = clausesForRemoval.next();

				while(removeIndex < toRemove.length && toRemove[removeIndex] < cur) {
					removeIndex++;
				}

				if(removeIndex == toRemove.length) break;

				if(toRemove[removeIndex] == cur) {
					clausesForRemoval.remove();
				}
			}
		} else if(maxSize == clausesForRemoval.size()) {
			for(int rem : toRemove) {
				clausesForRemoval.remove(rem);
			}
		} else {
			int lastIndex = 0;
			clausesForRemoval.startIter();
			while(clausesForRemoval.hasNext()) {
				int test = clausesForRemoval.next();
				int res = Arrays.binarySearch(toRemove,lastIndex,toRemove.length,test);

				if(res >= 0) clausesForRemoval.remove();
				else {
					lastIndex = -(res+1);
				}
			}
		}
	}

	public DNF getCLFromModels(int[] agreement) {
		clausesForRemoval.reset();

		int numVars = cl.getContext().size();

		BitSet bs = new BitSet(2*numVars+1);
		for(int i : agreement) {
			bs.set(LitUtil.getIndex(i,numVars));
		}

		for(int i : agreement) {
			computeRemoval(numVars, -i);
		}

		DNF ret = new DNF(cl.getContext());
		List<int[]> clauses = cl.getClauses();

		//Since models, subsumption shouldn't help
		//		if(doSubsumption) {
		//			doSubsumption(clauses,bs, numVars);
		//		}


		populateClauseList(numVars, bs, ret, clauses,true);

		return ret;
	}

	public int[] getExistantClauses() {
		int[] ret = new int[num];
		clausesForRemoval.startIter();
		int index = 0;
		while(clausesForRemoval.hasNext()) {
			int i = clausesForRemoval.next();
			ret[index] = i;
			index++;
		}

		return ret;
	}

	private void populateClauseList(int numVars, BitSet bs, ClauseList ret,
			List<int[]> clauses) {
		populateClauseList(numVars,bs,ret,clauses,false);

	}
	private void populateClauseList(int numVars, BitSet bs, ClauseList ret,
			List<int[]> clauses, boolean dnf) {
		final int mult = dnf ? -1 : 1;
		clausesForRemoval.startIter();
		while(clausesForRemoval.hasNext()) {
			int i = clausesForRemoval.next();
			int[] origClase = clauses.get(i);
			int realSize = origClase.length;

			for(int lit : origClase) {
				if(bs.get(LitUtil.getIndex(mult*-lit,numVars))) {
					realSize--;
				}
			}

			int[] toAdd = new int[realSize];
			int curIndex = 0;

			for(int lit : origClase) {
				if(!bs.get(LitUtil.getIndex(mult*-lit,numVars))) {
					toAdd[curIndex] = lit;
					curIndex++;
				}
			}

			ret.fastAddClause(toAdd);
		}

		ret.sort();
	}

	public CNF doSubsumption() {
		clausesForRemoval.reset();

		int numVars = cl.getContext().size();

		BitSet bs = new BitSet(2*numVars+1);

		doSubsumption(cl.getClauses(),bs,numVars);

		CNF ret = new CNF(cl.getContext());

		populateClauseList(numVars,bs,ret,cl.getClauses());

		return ret;

	}

	private void doSubsumption(List<int[]> clauses, BitSet bs, int numVars) {
		int[] fullClauses = new int[clausesForRemoval.size()];
		int curIndex = 0;

		clausesForRemoval.startIter();
		while(clausesForRemoval.hasNext()) {
			fullClauses[curIndex] = clausesForRemoval.next();
			curIndex++;
		}


		clausesForRemoval.startIter();
		while(clausesForRemoval.hasNext()) {
			int i = clausesForRemoval.next();
			int[] origClause = clauses.get(i);

			int[] curMatches = fullClauses;

			for(int lit : origClause) {
				if(!bs.get(LitUtil.getIndex(-lit,numVars))) {
					curMatches = ArrayIntersectionHelper.intersectOrdered(curMatches,
							clausesWithLit[LitUtil.getIndex(lit,numVars)]);

					if(curMatches.length <= 1) break; //this clauses will always be included
				}
			}

			for(int j : curMatches) {
				if(i != j) {
					clausesForRemoval.remove(j); //This should be iterator safe for us
				}
			}
		}
	}

	public LiteralGroup getModelGroup(LiteralGroup varGroup, int[] valid) {
		LinkedList<LiteralPermutation> newPerms = new LinkedList<LiteralPermutation>();
		if(varGroup.size() > 0) {
			for(LiteralPermutation perm : varGroup.getGenerators()) {
				newPerms.add(getModelPart(perm,valid));
			}
		}

		return new NaiveLiteralGroup(newPerms);
	}

	private LiteralPermutation getModelPart(LiteralPermutation perm,
			int[] validClauses) {
		int[] newPerm = new int[cl.size()+1];

		for(int k = 0; k < newPerm.length; k++) {
			newPerm[k] = k;
		}

		for(int i : validClauses) {
			int[] clause = cl.getClauses().get(i);
			int[] permed = perm.applySort(clause);
			int ind2 = Collections.binarySearch(cl.getClauses(),permed,ClauseList.COMPARE);
			newPerm[i+1] = ind2+1;
		}

		return new LiteralPermutation(newPerm);
	}

	public CNF unitPropagate() {
		clausesForRemoval.reset();

		int numVars = cl.getContext().size();
		List<int[]> clauses = cl.getClauses();

		BitSet propping = new BitSet(2*numVars+1);
		BitSet invalidToProp = new BitSet(2*numVars+1);

		IntList[] watched = new IntList[2*numVars+1];
		IntPair[] watchedLits = new IntPair[cl.size()];

		for(int k = 0 ; k < watched.length; k++) {
			watched[k] = new ArrayIntList();
		}

		LinkedList<Integer> toProp = new LinkedList<Integer>();

		for(int k = 0; k < clauses.size(); k++) {
			int[] clause = clauses.get(k);
			if(clause.length == 1) {
				int lit = clause[0];
				int index = LitUtil.getIndex(lit,numVars);
				int negIndex = LitUtil.getIndex(-lit,numVars);
				if(!propping.get(index)) {
					propping.set(index);
					invalidToProp.set(index);
					invalidToProp.set(negIndex);
					toProp.add(lit);
				}
			} else if(clause.length >= 2) {
				watchedLits[k] = new IntPair(0,1);
				int lit = clause[0];
				int index = LitUtil.getIndex(lit,numVars);
				watched[index].add(k);

				lit = clause[1];
				index = LitUtil.getIndex(lit,numVars);
				watched[index].add(k);
			}
		}

		while(toProp.size() > 0) {
			Integer propLit = toProp.peek();
			int negLit = -propLit;
			int negIndex = LitUtil.getIndex(negLit,numVars);

			if(propping.get(negIndex)) {
				return CNF.contradiction;
			} else {
				propagate(numVars, clauses, propping, invalidToProp, watched, watchedLits, toProp);
			}
		}

		CNF ret = new CNF(cl.getContext());
		for(int[] origClase : clauses) {
			int realSize = origClase.length;

			boolean keep = true;
			for(int lit : origClase) {
				if(invalidToProp.get(LitUtil.getIndex(lit,numVars))) {
					if(propping.get(LitUtil.getIndex(lit,numVars))) {
						keep = false;
						break;
					} else {
						realSize--;
					}
				}
			}

			if(!keep) continue;

			int[] toAdd = new int[realSize];
			int curIndex = 0;

			for(int lit : origClase) {
				if(!invalidToProp.get(LitUtil.getIndex(-lit,numVars))) {
					toAdd[curIndex] = lit;
					curIndex++;
				}
			}

			ret.fastAddClause(toAdd);
		}

		ret.sort();

		return ret;
	}

	private void propagate(int numVars, List<int[]> clauses, BitSet bs,
			BitSet invalidToProp, IntList[] watched, IntPair[] watchedLits, LinkedList<Integer> toProp) {
		Integer propLit = toProp.poll();
		int index = LitUtil.getIndex(propLit,numVars);
		int negIndex = LitUtil.getIndex(-propLit,numVars);
		IntList wList = watched[negIndex];

		for(int k = 0; k < wList.size(); k++) {
			int clauseIndex = wList.get(k);
			int[] cl = clauses.get(clauseIndex);
			IntPair watchedPair = watchedLits[clauseIndex];
			boolean prop = true;
			boolean isTrue = false;

			int assignedInd = -1;
			int nextWatchedindex = -1;
			for(int i = 0; i < cl.length; i++) {
				if(cl[i] == -propLit) {
					assignedInd = i;
				} else if(i != watchedPair.getI1() && i != watchedPair.getI2()) {
					int litInd = LitUtil.getIndex(cl[i],numVars);
					if(!invalidToProp.get(litInd)) {
						nextWatchedindex = i;
						prop = false;
					} else if(bs.get(litInd)) {
						isTrue=true;
						prop = false;
						break;
					}
				}
			}



			if(prop) {
				watchedLits[clauseIndex] = null;
				int litToProp;

				if(watchedPair.getI1() == assignedInd) {
					litToProp = cl[watchedPair.getI2()];
				} else {
					litToProp = cl[watchedPair.getI1()];
				}

				int propIndex= LitUtil.getIndex(litToProp,numVars);
				int negPropIndex= LitUtil.getIndex(-litToProp,numVars);
				if(!invalidToProp.get(propIndex)) {
					bs.set(propIndex);
					invalidToProp.set(propIndex);
					invalidToProp.set(negPropIndex);
					toProp.add(litToProp);
				}

			} else if(!isTrue) {
				int newVarIndex = LitUtil.getIndex(cl[nextWatchedindex],numVars);
				if(watchedPair.getI1() == assignedInd) {
					watchedPair.setI1(nextWatchedindex);
				} else {
					watchedPair.setI2(nextWatchedindex);
				}
				watched[newVarIndex].add(clauseIndex);
			}
		}

		watched[index] = null;
		watched[negIndex] = null;
	}

	public CNF removeObvEqVars() {
		//			orig = orig.unitPropagate();
		int numVars = cl.getContext().size();
		IntegralDisjointSet equive = new IntegralDisjointSet(-numVars,numVars);
		//			HashSet<IntPair> seenPairs = new HashSet<IntPair>();
		IntToIntLinkedHashMap[] sparse = new IntToIntLinkedHashMap[2*numVars+1];

		for(int k = 0; k < sparse.length; k++) {
			sparse[k] = new IntToIntLinkedHashMap();
		}

		for(int[] i : cl.getClauses()) {
			if(i.length == 2) {
				int i0 = LitUtil.getIndex(i[0],numVars);
				int i1 = LitUtil.getIndex(i[1],numVars);

				sparse[i0].put(i[1],0);
				sparse[i1].put(i[0],0);
			}
		}

		final int[] placeholder = new int[]{};
		for(int k = 1; k < numVars; k++) {
			if(equive.getRootOf(k) == k) {
				//Not equivalent yet
				int[] connK = getConnectedTo(k,sparse,numVars);

				int[] connNegK = placeholder;
				if(connK.length != 0) {
					connNegK = getConnectedTo(-k,sparse,numVars);
				}

				if(connK.length > 0 && connNegK.length > 0) {
					for(int i = 0; i < connNegK.length; i++) {
						connNegK[i] = -connNegK[i];
					}

					Arrays.sort(connK);
					Arrays.sort(connNegK);

					int[] commonVars = ArrayIntersectionHelper.intersectOrdered(connK,connNegK);
					connNegK = null;
					connK = null;

					for(int lit : commonVars) {
						equive.join(k,-lit);
						equive.join(-k,lit);
					}
				}
			}
		}

		//			for(int[] i : orig.getClauses()) {
		//				if(i.length == 2) {
		//					seenPairs.add(new IntPair(i[0],i[1]));
		//					int[] nextPair = new int[]{-i[0],-i[1]};
		//					LitSorter.inPlaceSort(nextPair);
		//					if(seenPairs.contains(new IntPair(nextPair[0],nextPair[1]))) {
		//						equive.join(i[0],-i[1]);
		//						equive.join(-i[0],i[1]);
		//					}
		//				}
		//			}

		List<int[]> newCl = new ArrayList<int[]>(cl.size());
		CNF ret = new CNF(cl.getContext());
		boolean[] added = new boolean[2*numVars+1];

		for(int[] i : cl.getClauses()) {
			Arrays.fill(added,false);

			boolean[] toAddMask = new boolean[i.length];
			int len = 0;
			boolean modded = false;
			for(int k = 0; k < i.length; k++) {
				int rep = equive.getRootOf(i[k]);
				int index = LitUtil.getIndex(rep,numVars);
				modded |= (rep != i[k]);

				if(!added[index]) {
					toAddMask[k] = true;
					added[index] = true;
					len++;
				}
			}

			int[] toAdd;

			if(modded) {
				toAdd= new int[len];
				int index = 0;

				for(int k = 0; k < i.length; k++) {
					if(toAddMask[k]) {
						int rep = equive.getRootOf(i[k]);
						toAdd[index] = rep;
						index++;
					}
				}

				LitSorter.inPlaceSort(toAdd);
			} else {
				toAdd = i;
			}
			newCl.add(toAdd);
		}

		ret.fastAddAll(newCl);
		ret.sort();
		return ret;
	}

	private int[] getConnectedTo(int k, IntToIntLinkedHashMap[] sparse, int numVars) {
		IntToIntLinkedHashMap seen = new IntToIntLinkedHashMap();
		seen.put(k,1);

		LinkedList<Integer> next = new LinkedList<Integer>();
		next.add(k);

		while(!next.isEmpty()) {
			int lit = next.poll();
			int index = LitUtil.getIndex(lit,numVars);

			IntToIntLinkedHashMap neigh = sparse[index];

			EntryIter iter = neigh.getIter();

			while(iter.hasNext()) {
				IntEntry toExplore = iter.next();
				int clauseLit = toExplore.getKey();
				int searchLit = -clauseLit;


				//implicit binary resolution
				if(seen.put(clauseLit,1)) {
					next.push(searchLit);
				}
			}
		}

		int[] ret = new int[seen.size()-1];

		EntryIter iter = seen.getIter();

		int index = 0;
		while(iter.hasNext()) {
			IntEntry entry = iter.next();
			if(entry.getKey() != k) {
				ret[index] = entry.getKey();
				index++;
			}
		}

		return ret;
	}
}
