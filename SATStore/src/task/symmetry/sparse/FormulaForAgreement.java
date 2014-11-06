package task.symmetry.sparse;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import util.ArrayIntersectionHelper;
import util.lit.LitUtil;
import formula.simple.CNF;

public class FormulaForAgreement {
	private IntersectionHelper clausesForRemoval;
	private CNF cnf;
	private int[][] clausesToRemoveForLit;
	private boolean doSubsumption = true;
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
				return Arrays.toString(cnf.getClauses().get(index));
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
	
	public FormulaForAgreement(CNF cnf) {
		int numVars = cnf.getContext().size();
		this.cnf = cnf;
		clausesForRemoval = new IntersectionHelper(cnf.size());
		
		int[] freq = new int[2*numVars+1];
		
		for(int[] cl : cnf.getClauses()) {
			for(int i : cl) {
				freq[LitUtil.getIndex(i,numVars)]++;
			}
		}
		
		clausesToRemoveForLit = new int[2*numVars+1][];
		
		for(int k = 0; k < freq.length; k++) {
			clausesToRemoveForLit[k] = new int[freq[k]];
			Arrays.fill(clausesToRemoveForLit[k],-1);
		}
		freq = null;
		
		int index = 0;
		for(int[] cl : cnf.getClauses()) {
			for(int i : cl) {
				int litInd = LitUtil.getIndex(i,numVars);
				for(int k = 0; k < clausesToRemoveForLit[litInd].length; k++) {
					if(clausesToRemoveForLit[litInd][k] == -1) {
						clausesToRemoveForLit[litInd][k] = index;
						break;
					}
				}
			}
			index++;
		}
		
	}
	
	public CNF getCNFFromAgreement(int[] agreement) {
		clausesForRemoval.reset();
		
		int numVars = cnf.getContext().size();
		
		BitSet bs = new BitSet(2*numVars+1);
		for(int i : agreement) {
			bs.set(LitUtil.getIndex(i,numVars));
		}
		
		for(int i : agreement) {
			int[] toRemove = clausesToRemoveForLit[LitUtil.getIndex(i,numVars)];
			
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
		
		CNF ret = new CNF(cnf.getContext());
		List<int[]> clauses = cnf.getClauses();
		
		if(doSubsumption) {
			doSubsumption(clauses,bs, numVars);
		}
		

		populateCNF(numVars, bs, ret, clauses);
		
		return ret;
	}

	private void populateCNF(int numVars, BitSet bs, CNF ret,
			List<int[]> clauses) {
		clausesForRemoval.startIter();
		while(clausesForRemoval.hasNext()) {
			int i = clausesForRemoval.next();
			int[] origClase = clauses.get(i);
			int realSize = origClase.length;
			
			for(int lit : origClase) {
				if(bs.get(LitUtil.getIndex(-lit,numVars))) {
					realSize--;
				}
			}
			
			int[] toAdd = new int[realSize];
			int curIndex = 0;
			
			for(int lit : origClase) {
				if(!bs.get(LitUtil.getIndex(-lit,numVars))) {
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
		
		int numVars = cnf.getContext().size();
		
		BitSet bs = new BitSet(2*numVars+1);
		
		doSubsumption(cnf.getClauses(),bs,numVars);
		
		CNF ret = new CNF(cnf.getContext());
		
		populateCNF(numVars,bs,ret,cnf.getClauses());
		
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
							clausesToRemoveForLit[LitUtil.getIndex(lit,numVars)]);
					
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
	
}
