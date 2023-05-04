package util.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import formula.simple.DNF;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import util.ArrayIntersectionHelper;
import util.IntPair;
import util.IntegralDisjointSet;
import util.lit.IntToIntLinkedHashMap;
import util.lit.IntToIntLinkedHashMap.EntryIter;
import util.lit.IntToIntLinkedHashMap.IntEntry;
import util.lit.LitSorter;
import util.lit.LitUtil;
import util.lit.LitsMap;

public class FormulaForAgreement {
	public static final CNF EMPTY = new CNF(VariableContext.defaultContext);
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
		
		if(clausesForRemoval.size() == 0) {
			return this.EMPTY;
		}
		
		
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
				int cur = clausesForRemoval.next();

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
		final int[] initMatch = new int[0];
		while(clausesForRemoval.hasNext()) {
			int i = clausesForRemoval.next();
			int[] origClause = clauses.get(i);

			int[] curMatches = null;//initMatch;

			if(origClause.length == 0) {
				curMatches = fullClauses;
			}

			int matchLit = 0;
			for(int lit : origClause) {
				if(!bs.get(LitUtil.getIndex(-lit,numVars))) {
					if(curMatches == null) {
						curMatches = clausesWithLit[LitUtil.getIndex(lit,numVars)];
//						curMatches = keepOnlyAtLeastAsLarge(curMatches,origClause.length);
						matchLit = lit;
					} else {
						int[] otherMatch = clausesWithLit[LitUtil.getIndex(lit,numVars)];
//						otherMatch = keepOnlyAtLeastAsLarge(otherMatch,origClause.length);
						if(otherMatch.length < curMatches.length) {
							matchLit = lit;
							curMatches = otherMatch;
						}

					}
					if(curMatches.length <= 1) break; //this clauses will always be included
				}
			}
			curMatches = keepOnlyAtLeastAsLarge(curMatches,origClause.length);
			

			if(curMatches.length > 1) {
				for(int lit : origClause) {
					if(!bs.get(LitUtil.getIndex(-lit,numVars))) {
						if(lit != matchLit) {
							curMatches = ArrayIntersectionHelper.intersectOrdered(curMatches,
									clausesWithLit[LitUtil.getIndex(lit,numVars)]);
						}
						if(curMatches.length <= 1) break; //this clauses will always be included
					}
				}
				for(int j : curMatches) {
					if(i != j) {
						clausesForRemoval.remove(j);
					}
				}
			}



		}
	}

	private int[] keepOnlyAtLeastAsLarge(int[] curMatches, int clLen) {
		int[] tempRet = new int[curMatches.length];
		int len = 0;
		for(int i : curMatches) {
			if(cl.getClauses().get(i).length >= clLen) {
				tempRet[len] = i;
				len++;
			}
		}
		int[] ret = new int[len];
		System.arraycopy(tempRet,0,ret,0,len);
		
		return ret;
	}

	public boolean isSubsumed(int[] clause) {
		if(clause.length == 0) return false; //Maybe bad idea
		int numVars = cl.getContext().size();
		int[] possible = null;

		for(int lit : clause) {
			if(possible == null) {
				possible = clausesWithLit[LitUtil.getIndex(lit,numVars)];
			} else {
				possible = ArrayIntersectionHelper.unionOrdered(possible,clausesWithLit[LitUtil.getIndex(lit,numVars)]);
			}
		}

		for(int lit : clause) {
			possible = ArrayIntersectionHelper.setDifference(possible,clausesWithLit[LitUtil.getIndex(-lit,numVars)]);
		}


		if(possible.length == 0) return false;

		IntToIntLinkedHashMap map = new IntToIntLinkedHashMap(clause.length);
		for(int lit : clause) {
			map.put(lit,0);
		}

		for(int ind : possible) {
			boolean subbed = true;
			for(int lit : cl.getClauses().get(ind)) {
				if(!map.contains(lit)) {
					subbed = false;
					break;
				}
			}

			if(subbed) return true;
		}

		return false;
	}
	
	public LiteralGroup getModelGroup(LiteralGroup varGroup) {
		int[] valid = new int[cl.getClauses().size()];
		for(int k = 0; k < valid.length; k++) {
			valid[k] = k;
		}
		return getModelGroup(varGroup,valid);
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
	@Deprecated //Currently does not work if forms a contradiction!
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
//					invalidToProp.set(negPropIndex);
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
		for(int k = 1; k <= numVars; k++) {
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
			boolean add = true;
			for(int k = 0; k < i.length; k++) {
				int rep = equive.getRootOf(i[k]);
				int index = LitUtil.getIndex(rep,numVars);
				modded |= (rep != i[k]);

				if(!added[index]) {
					toAddMask[k] = true;
					added[index] = true;
					
					if(added[LitUtil.getIndex(-rep,numVars)]) {
						//tautologous clause
						add = false;
						break;
					}
					
					len++;
				}
			}

			int[] toAdd;

			if(modded && add) {
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
			
			if(add) {
				newCl.add(toAdd);
			}
		}

		ret.fastAddAll(newCl);
		ret.sort();
		return ret;
	}

	private int[] getConnectedTo(int k, IntToIntLinkedHashMap[] sparse, int numVars) {
		return getConnectedTo(k,sparse,numVars,false);
	}
	private int[] getConnectedTo(int k, IntToIntLinkedHashMap[] sparse, int numVars, boolean includOrigIfNewClause) {
		IntToIntLinkedHashMap seen = new IntToIntLinkedHashMap();
		seen.put(k,1);

		LinkedList<Integer> next = new LinkedList<Integer>();
		next.add(k);

		boolean foundAgain = false;
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
				} else if(clauseLit == k) {
					foundAgain = true;
				}
			}
		}

		int sizeChange = (foundAgain && includOrigIfNewClause) ? 0 : -1;
		
		int[] ret = new int[seen.size()+sizeChange];

		EntryIter iter = seen.getIter();

		int index = 0;
		while(iter.hasNext()) {
			IntEntry entry = iter.next();
			if(entry.getKey() != k || (foundAgain && includOrigIfNewClause)) {
				ret[index] = entry.getKey();
				index++;
			}
		}

		return ret;
	}
	
	public CNF binaryResolveToUnitClauses() {
		CNF ret = new CNF(cl.getContext());
		ret.addAll(cl.getClauses());
		int numVars = cl.getContext().size();
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
		

		
		for(int k = 1; k <= numVars; k++) {
			int[] connK = getConnectedTo(k,sparse,numVars,true);
			
			boolean unit = false;
			for(int i : connK) {
				if(i == k) {
					ret.addClause(i);
					unit = true;
					break;
				}
			}
			
			if(!unit) {
				connK = getConnectedTo(-k,sparse,numVars,true);
				
				for(int i : connK) {
					if(i == -k) {
						ret.addClause(i);
						unit = true;
						break;
					}
				}
			}
		}
		
		return ret;
	}

	public List<int[]> getResolvants() {
		return getResolvants(null);
	}
	public List<int[]> getResolvants(LitsMap<?> seen) {
		//		Set<String> curClauses = new HashSet<String>();
		int numVars = cl.getContext().size();

		LitsMap all = new LitsMap(cl.getContext().size());

		List<int[]> totalClauses = new ArrayList<int[]>();
		List<int[]> resolvants = new ArrayList<int[]>();
		for(int[] cl1 : cl.getClauses()) {
			if(seen == null || !seen.contains(cl1)) {
				totalClauses.add(cl1);
			} 
//			all.put(cl1,null);
		}

		for(int k = 0; k < totalClauses.size(); k++) {
			int[] cl1 = totalClauses.get(k);
 
			boolean old = false;
			if(seen != null) {
				if(seen.contains(cl1)) {
					old = true;
				} else {
//					seen.put(cl1,null);
				}
			}
			
			for(int lit : cl1) {
				int[] compatClauses = getCompatibleClauses(numVars, k, cl1, lit);

				for(int ind : compatClauses) {
					if(ind < k) continue;

					int[] cl2 = cl.getClauses().get(ind);
//					boolean old2 = (seen != null && seen.contains(cl2));
//					
//					if(old && old2) {
//						System.out.print("+");
//						continue; //did this before
//					}
					
					int[] res = LitUtil.mergeForResolve(cl1,cl2);
					if(res != null) {
						if(seen == null || !seen.contains(res)) {// && !this.isSubsumed(res)) {
//							all.put(res,null);
							totalClauses.add(res);
							resolvants.add(res);
							if(seen != null) {
								seen.put(res,null);
							}
//							System.out.println("-");
						} else {
//							System.out.println("+");
						}
						
					}
				}
			}
		}
		return resolvants;
	}

	public List<int[]> linearResolveClauseFrom(int index) {
		//		Set<String> curClauses = new HashSet<String>();
		int numVars = cl.getContext().size();

		LitsMap all = new LitsMap(cl.getContext().size());

		List<int[]> totalClauses = new ArrayList<int[]>();
		List<int[]> resolvants = new ArrayList<int[]>();
		for(int[] cl : cl.getClauses()) {
			totalClauses.add(cl);
			all.put(cl,null);
		}

		boolean resolved = false;
		for(int k = index; !resolved && k < totalClauses.size(); k++) {
			int[] cl1 = totalClauses.get(k);

			for(int lit : cl1) {
				int[] compatClauses = getCompatibleClauses(numVars, k, cl1, lit);

				for(int ind : compatClauses) {
					if(ind < k) continue;

					int[] res = LitUtil.mergeForResolve(cl1,cl.getClauses().get(ind));
					if(res != null) {
						if(!all.contains(res) && !this.isSubsumed(res)) {
							resolved = true;
							all.put(res,null);
							totalClauses.add(res);
							resolvants.add(res);
							break;
						}
					}
				}
			}
		}

		while(resolved) {
			resolved = false;
			int[] cur = resolvants.get(resolvants.size()-1);

			for(int lit : cur) {
				int[] compatClauses = getCompatibleClauses(numVars, -1, cur, lit);

				for(int ind : compatClauses) {
					int[] res = LitUtil.mergeForResolve(cur,cl.getClauses().get(ind));
					if(res != null) {
						if(!all.contains(res)) {
							resolved = true;
							all.put(res,null);
							totalClauses.add(res);
							resolvants.add(res);
							break;
						}
					}
				}
			}

		}

		return resolvants;
	}

	private int[] getCompatibleClauses(int numVars, int k, int[] cl1, int lit) {
		int[] compatClauses = clausesWithLit[LitUtil.getIndex(-lit,numVars)];

		for(int lit2 : cl1) {
			if(lit2 != lit) {
				compatClauses = ArrayIntersectionHelper.setDifference(
						compatClauses,clausesWithLit[LitUtil.getIndex(-lit2,numVars)]);
			}
			if(compatClauses.length == 0) {
				break;
			}
			if(compatClauses[compatClauses.length-1] < k) {
				compatClauses = new int[0];
				break;
			}
		}
		return compatClauses;
	}

	public int getPairFreq(int lit1, int lit2) {
		int numVars = cl.getContext().size();
		int ind1 = LitUtil.getIndex(lit1,numVars);
		int ind2 = LitUtil.getIndex(lit2,numVars);
		
		int[] cl1 = clausesWithLit[ind1];
		int[] cl2 = clausesWithLit[ind2];
		
		return ArrayIntersectionHelper.intersectSize(cl1,cl2);
	}
}
