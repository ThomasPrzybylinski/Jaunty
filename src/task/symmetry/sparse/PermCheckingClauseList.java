package task.symmetry.sparse;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;

import formula.Conjunctions;
import formula.VariableContext;
import formula.simple.ClauseList;
import util.PermutationUtil;
import util.lit.LitUtil;
import util.lit.LitsMap;

/*
 *  Ideas to make it more scalable for sym finding:
 *  	Have a "current partition" array. The partial permute functions will only change that array (plus the permuted boolean? Or permutation to 0 means "not permuted")
 *  	We also have a watched literal in each clause (preferably the last element). When it gets permuted, we check the clause
 *  it contains to see if all have been permuted. If yes, we return the fully permuted cause to check up.
 * 
 */

//Mostly unmodifiable
public class PermCheckingClauseList extends ClauseList {
	int[] fullyPermedClauses;
	int level = 0;
	LinkedInts[] checkedClauses;
	LinkedList<Permed> perms;
	int[] permedVars;
//	int[][] clausesWithLit;
	LitsMap<Integer> clauseFreq;
	
	private class LinkedInts {
		private IntLink head = new IntLink(0);
		private IntLink tail = new IntLink(0);
		
		private int size;
		
		public LinkedInts() {
			head.next = tail;
			tail.prev = head;
		}
		
		public void add(int lit) {
			add(new IntLink(lit));
		}
		
		public void add(IntLink link) {
			size++;
			
			IntLink behind = tail.prev;
			link.next = tail;
			link.prev = behind;
			behind.next = link;
			tail.prev = link;
		}
		
		public IntLinkIter getIter() {
			return new IntLinkIter(this);
		}
		
		public class IntLinkIter {
			private IntLink cur;
			private LinkedInts list;
			
			public IntLinkIter(LinkedInts map) {
				cur = map.head;
				list = map;
			}
			
			public boolean hasNext() {
				return cur != list.tail && cur.next != list.tail;
			}
			
			public IntLink next() {
				if(cur == list.tail || cur.next == list.tail) return null;

				cur = cur.next;
				
				return cur;
			}
			
			public void remove() {
				if(cur == list.head || cur == list.tail) return;
				IntLink nextCur = cur.next;
				cur.prev.next = nextCur;
				nextCur.prev = cur.prev;
				cur.next = null;
				cur.prev = null;
				list.size--;
				
				cur = nextCur.prev;
			}
		}
		
	}
	
	
	
	private class IntLink {
		int val;
		IntLink prev;
		IntLink next;
		
		public IntLink(int val) {
			super();
			this.val = val;
		}
		
		
	}

	private class Permed {
		int var;
		int level;
		public Permed(int var, int level) {
			super();
			this.var = var;
			this.level = level;
		}

	}

	public PermCheckingClauseList(Conjunctions cnf) {
		super(cnf);
		setupDataStructures();
	}

	public PermCheckingClauseList(VariableContext context) {
		super(context);
		setupDataStructures();
	}

	//WARNING: USES THE int[] OF CNF, SHOULD NOT BE MODIFIED
	public PermCheckingClauseList(ClauseList cnf) {
		this(cnf,false);
	}

	public PermCheckingClauseList(ClauseList cnf, boolean copy) {
		super(cnf.getContext());
		if(copy) {
			cnf = cnf.getCopy();
		}

		for(int[] clause : cnf.getClauses()) {
			clauses.add(clause.clone());
		}
		setupDataStructures();
	}

	private void setupDataStructures() {
		this.sort();
		int numVars = this.context.size();

		perms = new LinkedList<Permed>();
		permedVars = new int[1+numVars];
		Arrays.fill(permedVars,-1);

		ArrayIntList[] tempClausesWithLit = new ArrayIntList[2*numVars+1];
		for(int k = 0; k < tempClausesWithLit.length; k++) {
			tempClausesWithLit[k] = new ArrayIntList();
		}

		checkedClauses = new LinkedInts[1+numVars];
		clauseFreq = new LitsMap<Integer>(numVars);
		
		for(int k = 1; k < checkedClauses.length; k++) {
			checkedClauses[k] = new LinkedInts();
		}

		for(int k = 0; k < clauses.size(); k++) {
			int[] cl = clauses.get(k);
			
			if(cl.length > 0) { //can be empty when certain choice restrictions occur
				checkedClauses[Math.abs(cl[cl.length-1])].add(k);
			}
			
			Integer freq = clauseFreq.get(cl);
			
			if(freq == null) {
				clauseFreq.put(cl,Integer.valueOf(1));
			} else {
				clauseFreq.put(cl,Integer.valueOf(freq+1));
			}


			for(int lit : cl) {
				tempClausesWithLit[LitUtil.getIndex(lit,numVars)].add(k);
			}
		}

//		clausesWithLit = new int[2*numVars+1][];
//
//		for(int k = 0; k < tempClausesWithLit.length; k++) {
//			clausesWithLit[k] = tempClausesWithLit[k].toArray();
//			tempClausesWithLit[k] = null;
//		}
		


		fullyPermedClauses = new int[clauses.size()];
		Arrays.fill(fullyPermedClauses,-1);
	}

	public void reset() {
		Arrays.fill(fullyPermedClauses,-1);
		Arrays.fill(permedVars,-1);
		perms.clear();
		level = 0;
		
	}

	public void post() {
		level++;
	}

	public void pop() {
		level--;
		for(int k = 0; k < fullyPermedClauses.length; k++) {
			if(fullyPermedClauses[k] > level) {
				fullyPermedClauses[k] = -1;
			}
		}

		while(!perms.isEmpty() && perms.peekLast().level > level) {
			permedVars[perms.pollLast().var] = -1;
		}
	}

	//Assumes ordered
	public boolean checkPerm(int[] perm) {
		int[] oldCl = null;
		int freq = -1;
		int index = -1;
		for(int[] cl : getClauses()) {
			index++;
			if(fullyPermedClauses[index] >= 0) continue;


			if(oldCl == null || !Arrays.equals(cl,oldCl)) {
				if(oldCl != null && freq > 0) return false;
				oldCl = cl;
				int[] permCl = PermutationUtil.permuteClause(cl,perm);
				freq = getClauseFreq(permCl);
			}

			freq--;

			if(freq < 0) return false;
		}

		return true;
	}

	public boolean checkPartialPerm(int[] perm) {
		for(int k = 1; k < perm.length; k++) {
			if(permedVars[k] == -1 && perm[k] != 0) {
				permedVars[k] = level;
				perms.add(new Permed(k,level));
			}
		}
		
		for(int k = 1; k < perm.length; k++) {
			if(permedVars[k] == level && perm[k] != 0) {
				task.symmetry.sparse.PermCheckingClauseList.LinkedInts.IntLinkIter iter = checkedClauses[k].getIter();

				while(iter.hasNext()) {
					IntLink theLink = iter.next();
					int index = theLink.val;
					
					int[] clause = clauses.get(index);

					int[] permCl = PermutationUtil.permuteClauseParital(clause,perm);

					if(permCl == null) {
						iter.remove();

						for(int j = clause.length-1; j >= 0; j--) {
							int i = clause[j];
							int var = Math.abs(i);
							if(permedVars[var] == -1) {
								checkedClauses[var].add(theLink);
								break;
							}
						}
					} else {
						fullyPermedClauses[index] = level;
						int clFreq = getFreqforClauseAtIndex(clause,clauses,index);

						int permedFreq = getClauseFreq(permCl);

						if(clFreq != permedFreq) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public static enum CheckDouble {ValidPerm,ValidPartial,Invalid};

	public CheckDouble checklPermAndPartial(int[] perm, int[] partial) {
		int[] oldCl = null;
		int freq = -1;

		boolean checkingPartial = true;
		boolean isPermValid = true;

		for(int k = 0; k < clauses.size(); k++) {
			if(fullyPermedClauses[k] >= 0) continue;
			int[] cl = clauses.get(k);

			if(oldCl == null || !Arrays.equals(cl,oldCl)) {
				if(oldCl != null && freq > 0) {
					if(checkingPartial) {
						return CheckDouble.Invalid;
					} else {
						isPermValid = false;
					}
				}

				oldCl = cl;
				int[] permCl = PermutationUtil.permuteClauseParital(cl,partial);
				if(permCl == null) {
					checkingPartial = false;
					if(isPermValid) {
						permCl = PermutationUtil.permute(cl,perm);
					}
				} else {
					fullyPermedClauses[k] = level;
				}
				if(permCl != null) {
					freq = getClauseFreq(permCl);
				}
			}

			freq--;

			if(freq < 0) {
				if(checkingPartial) {
					return CheckDouble.Invalid;
				} else {
					isPermValid = false;
				}
			}
		}

		return isPermValid ? CheckDouble.ValidPerm : CheckDouble.ValidPartial;
	}


	public int getClauseFreq(int[] cl) {
//		List<int[]> clauses = super.getClauses();
//		int[] potentialIndecies = clausesWithLit[LitUtil.getIndex(cl[0],this.getContext().size())];
//		int index = specialBinarySearch(clauses,potentialIndecies,cl);//
////		int index2 = Collections.binarySearch(clauses,cl,COMPARE);
//		
//		if(index >= 0) {
//			return getFreqforClauseAtIndex(cl, clauses, index);
//
//		} else {
//			return -1;
//		}
		
		Integer ret = clauseFreq.get(cl);
		
		return ret == null ? -1 : ret;

	}

	//Modified binary search from Java library
	private static int specialBinarySearch(List<int[]> clauses, int[] potentials, int[] cl) {
		int low = 0;
		int high = potentials.length - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
		int midIndex = potentials[mid];

		int compVal = COMPARE.compare(clauses.get(midIndex),cl);
		if (compVal < 0)
			low = mid + 1;
		else if (compVal > 0)
			high = mid - 1;
		else
			return potentials[mid]; // key found
		}
		return -1;  // key not found. Can't tell where it should be (nor do i care in this case)
	}

	private int getFreqforClauseAtIndex(int[] cl, List<int[]> clauses, int index) {
		int num = 1;
		for(int k = -1; k + index >= 0; k--) {
			if(Arrays.equals(cl,clauses.get(k+index))) {
				num++;
			} else {
				break;
			}
		}

		for(int k = 1; k + index < clauses.size(); k++) {
			if(Arrays.equals(cl,clauses.get(k+index))) {
				num++;
			} else {
				break;
			}
		}

		return num;
	}

	@Override
	public void addClause(int... vars) {
		throw new UnsupportedOperationException("SemiPermutableCNFs are meant to be constant at the moment");
		//super.addClause(vars);
	}

	@Override
	public List<int[]> getClauses() {
		return Collections.unmodifiableList(super.getClauses());
	}

}