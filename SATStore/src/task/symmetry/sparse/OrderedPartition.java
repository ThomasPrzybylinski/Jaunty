package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.IntList;

import util.lit.LitUtil;

public class OrderedPartition {
	private static final int PTR = -1;

	private static final int UNDO = -1;
	private static final int AVAIL = -2;

	private class MyEntry implements StatsInfo {
		HeadPointer list = null;
		int index;
		MyEntry next = null;
		MyEntry prev = null;

		public MyEntry(int item) {
			this.index = item;
		}

		public MyEntry(int item, HeadPointer list) {
			this(item);
			this.list = list;
		}

		public void copyFrom(MyEntry entry) {
			assert(entry.index == this.index);

			this.list = entry.list;
			this.next = entry.next;
			this.prev = entry.prev;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public StatsInfo next() {
			return next;
		}

		@Override
		public int getLit() {
			return LitUtil.getLit(index,numVars);
		}
		
		public String toString() {
			return "("+LitUtil.getLit(this.index,numVars)+" "+this.index + (this.list == null ? "" : (" " + this.list.list))+")";  
		}
	}

	private class HeadPointer extends MyEntry {
		int size = 0;
		int list;

		MyEntry tail;

		public HeadPointer(int list) {
			super(PTR);
			this.list = list;
		}

		public void add(MyEntry entry) {
			entry.list = this;
			entry.next = null;
			entry.prev = this.tail;
			
			
			if(this.size == 0) {
				this.next = entry;
				entry.prev = this;
			} else {
				this.tail.next = entry;
			}
			
			this.tail = entry;
			
			this.size++;
			
			checkSizes();
		}

		public MyEntry get(int index) {
			int curInd = 0;
			MyEntry cur = this.next;

			while(cur != null && curInd != index) {
				cur = cur.next;
				curInd++;
			}

			return curInd == index ? cur : null;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(list).append(' ').append(size).append(']');
			
			MyEntry cur = this.next;
			int index = 0;
			while(cur != null) {
				sb.append(", ").append(cur).append('[').append(index).append(']');
				if(cur instanceof Undo) {
					cur = ((Undo) cur).undoPtr;
				} else {
					cur = cur.next;
				}
				index++;
			}
			
			return sb.toString();

		}

	}
	
	private class Undo extends MyEntry {
		public Undo(int item) {
			super(item);
		}

		Undo undoPtr;
		boolean head;
	}

	ArrayList<HeadPointer> headPointers;
	MyEntry[] partition;

	LinkedList<HeadPointer> undos;
	HeadPointer curUndo;
	MyEntry availHead = null;

	private int numVars;

	private OrderedPartition() {
		headPointers = new ArrayList<HeadPointer>();
		undos = new LinkedList<HeadPointer>();
		curUndo = new HeadPointer(UNDO);
	}

	public OrderedPartition(int num) {
		this(num,true);
	}

	@SuppressWarnings("unchecked")
	protected OrderedPartition(int num, boolean addNums) {
		this();

		this.numVars = num;

		partition = new MyEntry[2*num+1];

		HeadPointer headPointer = new HeadPointer(0);
		headPointer.size = 2*num;
		MyEntry prev = headPointer;
		
		for(int k = 1; k <=num; k++) {
			int index = LitUtil.getIndex(k,num);
			
			partition[index] = new MyEntry(index,headPointer);
			prev.next = partition[index];
			partition[index].prev = prev;

			prev = partition[index];
			
			index = LitUtil.getIndex(-k,num);
			
			partition[index] = new MyEntry(index,headPointer);
			prev.next = partition[index];
			partition[index].prev = prev;

			prev = partition[index];
		}
		
		for(int k = 0; k < partition.length; k++) {
			int lit = LitUtil.getLit(k,num);

			if(lit != 0) {
				partition[k] = new MyEntry(k,headPointer);
				prev.next = partition[k];
				partition[k].prev = prev;

				prev = partition[k];
			}
		}

		headPointer.tail = partition[partition.length-1];
		headPointers.add(headPointer);
		
		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}
	}

	//Won't work if numbers are not contiguous 
	public OrderedPartition(List<IntList> initial) {
		this();

		int num = 0;

		for(IntList part : initial) {
			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
				num = Math.max(num,Math.abs(i));
			}
		}

		this.numVars = num;
		partition = new MyEntry[2*num+1];

		for(int k = 0; k < partition.length; k++) {
			int lit = LitUtil.getLit(k,num);

			if(lit != 0) {
				partition[k] = new MyEntry(k);
			}
		}

		headPointers.ensureCapacity(initial.size());

		for(int k = 0; k < initial.size(); k++) {
			HeadPointer curPointer = new HeadPointer(k);
			headPointers.add(curPointer);

			IntList list = initial.get(k);
			curPointer.size = list.size();

			MyEntry prev = curPointer;
			for(int i = 0; i < list.size(); i++) {
				int toAdd = list.get(i);
				int index = LitUtil.getIndex(toAdd,num);

				MyEntry cur = partition[index];
				cur.list = curPointer;
				cur.prev = prev;
				prev.next = cur;

				prev = cur;
			}
			curPointer.tail = prev;

		}
		
		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}
	}

	public boolean isIsomorphicWith(OrderedPartition other) {
		if(this.headPointers.size() != other.headPointers.size()) return false;

		for(int k = 0; k < headPointers.size(); k++) {
			if(this.headPointers.get(k).size != other.headPointers.get(k).size) return false;
		}
		return true;
	}
	
	public int getNum() {
		return numVars;
	}

	public int parts() {
		return headPointers.size();
	}

	public int partSize(int part) {
		return headPointers.get(part).size;
	}

	public int getElt(int part, int index) {
		MyEntry entry = headPointers.get(part).get(index);
		
		if(entry == null) {
			entry = headPointers.get(part).get(index);
		}
		
		return LitUtil.getLit(entry.index,numVars);
	}

	public int getPartWithElt(int elt) {
		MyEntry entry = partition[LitUtil.getIndex(elt,numVars)];
		
		if(entry != null && entry.list != null) {
			return entry.list.list;
		} else {
			return -1;
		}

	}

	public int getPartIndexOfElt(int elt) {
		int eltIndex = LitUtil.getIndex(elt,numVars);
		MyEntry entry = partition[eltIndex];
		MyEntry cur = entry.list.next;

		int index = 0;
		while(cur != entry) {
			cur = cur.next;
			index++;
		}

		return index;
	}

	public int getFirstNonUnitPart() {
		int index = 0;
		for(HeadPointer pt : headPointers) {
			if(pt.size > 1) {
				return index;
			}
			index++;
		}

		return -1;
	}

	public int getLeastABSNonUnitPart() {
		int index = 0;
		int minIndex = -1;
		int min = Integer.MAX_VALUE;
		for(HeadPointer part : headPointers) {
			if(part.size > 1) {
				int partMin = LitUtil.getLit(part.next.index,numVars);
				int absPartMin = Math.abs(partMin); 
				if(absPartMin < min) {
					min = absPartMin;
					minIndex = index;
				} else if(absPartMin == min && partMin > 0) {
					//If part.get(0) is positive
					minIndex = index;
				}
			}
			index++;
		}
		return minIndex;
	}

	public void assignEltsToUnitPart(int elt) {
		int eltIndex = LitUtil.getIndex(elt,numVars);

		MyEntry entry = partition[eltIndex];

		assignUnit(entry);
	}
	
	public void assignEltsToUnitPart(int part, int index) {

		MyEntry entry = headPointers.get(part).get(index);
		
		assignUnit(entry);
	}


	private void assignUnit(MyEntry entry) {
		//First part is part of an optimization: our refinement algorithm ignores
		//variables that aren't in the formula.
		//no change necessary since isomporphic POP
		if(entry == null | entry.list == null) return; 
		
		if(entry.list.size == 1) {
			return; 
		}


		remove(entry);
		HeadPointer newPointer = new HeadPointer(headPointers.size());
		headPointers.add(newPointer);
		newPointer.add(entry);
		
		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}
	}

	public int[] getPermutation(OrderedPartition bottom, int num) {
		assert(this.numVars == bottom.numVars && this.headPointers.size() == bottom.headPointers.size());
		int[] perm = new int[num+1];
		perm[0] = 0;

		for(int k = 0; k < num+1; k++) {
			perm[k] = k;
		}

		for(int k = 0; k < this.headPointers.size(); k++) {
			HeadPointer topPt = this.headPointers.get(k);
			HeadPointer botPt = bottom.headPointers.get(k);

			if(topPt.size == 1 && botPt.size == 1) {
				int topLit = LitUtil.getLit(topPt.next.index,numVars);
				int botLit = LitUtil.getLit(botPt.next.index,numVars);

				int topVar = Math.abs(topLit);
				perm[topVar] =  (topVar/topLit)*botLit;
			} else {
				return null;
			}
		}

		return perm;
	}

	private void remove(MyEntry entry) {
		save(entry);
		removeNoSave(entry);
	}

	private void removeNoSave(MyEntry entry) {
		

		if(entry.list.tail == entry) {
			entry.list.tail = entry.prev;
		}
		
		if(entry.next != null) {
			entry.next.prev = entry.prev;
		}

		entry.prev.next = entry.next;

		entry.list.size--;
		
		checkSizes();
	}

	public void save(MyEntry entry) {
		if(entry.index == -1) throw new RuntimeException();
		Undo prevHead = (Undo)curUndo.next;

		Undo toAdd = new Undo(entry.index);//allocate(entry.index);
		toAdd.copyFrom(entry);
		
		if(entry.prev == entry.list) {
			toAdd.head = true;
		}

		if(prevHead != null) {
			toAdd.undoPtr = prevHead; 
		}

		curUndo.next = toAdd;
	}

	public void post() {
		if(curUndo != null) {
			undos.push(curUndo);
		}
		curUndo = new HeadPointer(UNDO);
		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}
		
		checkSizes();

	}

	public void pop() {
		checkSizes();
		Undo cur = (Undo)curUndo.next;

		while(cur != null) {
			removeNoSave(partition[cur.index]);
			
			MyEntry trueEntry = partition[cur.index];
			
			trueEntry.copyFrom(cur);
			trueEntry.prev.next = trueEntry;

			if(trueEntry.next == null) {
				//is tail
				trueEntry.list.tail = trueEntry;
			} else {
				trueEntry.next.prev = trueEntry;
			}
			
			trueEntry.list.size++;
			
			if(cur.head) {
				cur.list.next = trueEntry;
			}

			MyEntry toDel = cur;
			cur = cur.undoPtr;

			checkSizes();
			
			deallocate(toDel);
		}
		deallocate(cur);

		for(int k = headPointers.size()-1; k >= 0; k--) {
			if(headPointers.get(k).size == 0) {
				headPointers.remove(k);
			} else {
				break; //Should be removed in order
			}
		}

		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}

		checkSizes();
		
		curUndo = undos.pop();
		

	}

	private MyEntry allocate(int item) {
		if(availHead == null) {
			return new MyEntry(item);
		} else {
			MyEntry ret = availHead;
			availHead = ret.next;
			availHead.index = item;
			return ret;
		}
	}

	private void deallocate(MyEntry entry) {
//		entry.prev = null;
//		entry.next = availHead;
//		entry.list = null;
//		availHead = entry;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(HeadPointer part : headPointers) {
			//			if(part.size == 1) continue;
			sb.append('[');

			MyEntry cur = part;

			while(cur.next != null) {
				cur = cur.next;
				int i = LitUtil.getLit(cur.index,numVars);
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		return sb.toString();
	}

	public boolean refine(OrderedPartition bot, SparseSymmetryStatistics stats) {
		return refine(bot,stats,false);
	}

	@Deprecated //May have an off-by-one error for the undos
	public boolean refine(OrderedPartition bot, SparseSymmetryStatistics stats, boolean initial) {
		boolean ppoIso = true;

		Undo headUndo;
		Undo prevHeadUndo = (Undo)curUndo.next;
		boolean ok = false;

		while(prevHeadUndo != null && prevHeadUndo.undoPtr != null) {
			prevHeadUndo = prevHeadUndo.undoPtr;
		}

		do {
			headUndo = (Undo)curUndo.next;
			ok = refine(bot,stats,prevHeadUndo,initial);
			
			if(!ok) break;
			
			initial = false;

			prevHeadUndo = headUndo;

			ppoIso = this.isIsomorphicWith(bot);

		} while(headUndo != curUndo.next && ppoIso);


		
		if(ppoIso && ok) {
			return true;
		} else {
			return false;
		}
	}


	//varToClause is 1 if clause of index k contains integer i

	private TreeSet<Integer> modded = new TreeSet<Integer>();
	private boolean refine(OrderedPartition bot, SparseSymmetryStatistics stats, Undo prevHeadUndo, boolean initial) {
		modded.clear();

		if(initial || prevHeadUndo == null) {
			for(int k = 0; k < headPointers.size(); k++) {
				modded.add(k);
			}
		} else {
			Undo entry = (Undo)curUndo.next;

			while(entry != null) {
				modded.add(entry.list.list);
				
				MyEntry undidEntry = partition[entry.index];
				modded.add(undidEntry.list.list);
				
				if(entry == prevHeadUndo) break;
				
				entry = entry.undoPtr;
			}
		}

		HeadPointer[] topToUse = new HeadPointer[modded.size()];
		HeadPointer[] botToUse = new HeadPointer[modded.size()];

		int index = 0;
		for(Integer headIndex : modded) {
			topToUse[index] = headPointers.get(headIndex);
			botToUse[index] = bot.headPointers.get(headIndex);
			index++;
		}


		for(int p = 0; p < headPointers.size(); p++) {
			HeadPointer topPart = this.headPointers.get(p);
			HeadPointer bottomPart = bot.headPointers.get(p);

			if(topPart.size == 1) {
				continue;
			}

			boolean correct = doRefine(bot,topPart, bottomPart,stats,topToUse,botToUse);

			if(!correct || !this.isIsomorphicWith(bot)) {
				return false;
			}
		}

		return true;

	}

	private boolean doRefine(OrderedPartition bot, HeadPointer topPart, HeadPointer bottomPart,
			SparseSymmetryStatistics stats, HeadPointer[] topToUse, HeadPointer[] botToUse) {
		// TODO Auto-generated method stub
		int[][][] topFreqs = stats.getPartFreqs(topPart,topToUse,topPart.size);
		int[][][] botFreqs = stats.getPartFreqs(bottomPart,botToUse,bottomPart.size);

		int[] topPosHash = new int[topFreqs[0].length];
		int[] botPosHash = new int[botFreqs[0].length];
		int[] topNegHash = new int[topFreqs[1].length];
		int[] botNegHash = new int[botFreqs[1].length];


		int offset = this.headPointers.size();

		//Given an index for a new refined cell, give an example index
		//Is the last elt added, to make sure we can always unify
		//the top and bottom
		int[] topAssn = new int[topPart.size];
		int[] botAssn = new int[bottomPart.size];

		Arrays.fill(topAssn,-1);
		Arrays.fill(botAssn,-1);

		//Top and bottoms should be isomorphic, so same indecies
		for(int k = 0; k < topFreqs[0].length; k++) {
			topPosHash[k] = Arrays.hashCode(topFreqs[0][k]);
			botPosHash[k] = Arrays.hashCode(botFreqs[0][k]);

			topNegHash[k] = Arrays.hashCode(topFreqs[1][k]);
			botNegHash[k] = Arrays.hashCode(botFreqs[1][k]);
		}

		MyEntry topPtr = topPart.next;

		int k = 0;
		while(topPtr.hasNext()) {
			//First elt always stays in the list
			k++;
			topPtr = topPtr.next;


			//			int top = topPtr.getLit();
			//			int bot = botPtr.getLit();

			if(!areEqv(k,0, topPosHash, topNegHash, topFreqs)) {
				boolean added = false;
				for(int i = offset; !added && i < headPointers.size(); i++) {
					int example = topAssn[i-offset];

					if(areEqv(k,example, topPosHash, topNegHash, topFreqs)) {
						MyEntry toAdd = topPtr;
						topPtr = topPtr.prev;
						
						remove(toAdd);
						headPointers.get(i).add(toAdd);
						added = true;
					}
				}

				if(!added) {
					HeadPointer newPointer = new HeadPointer(headPointers.size());
					topAssn[headPointers.size()-offset] = k;
					headPointers.add(newPointer);
					
					MyEntry toAdd = topPtr;
					topPtr = topPtr.prev;
					
					remove(toAdd);
					newPointer.add(toAdd);

				}
			}
		}
		
		for(HeadPointer hp : headPointers) {
			if(hp.next == null) {
				throw new RuntimeException();
			}
		}

		while(this.headPointers.size() > bot.headPointers.size()) {
			HeadPointer newPointer = new HeadPointer(bot.headPointers.size());
			bot.headPointers.add(newPointer);
		}

		topPtr = null;

		MyEntry botPtr = bottomPart.next;
		k = 0;
		while(botPtr != null) {
			int topIndex = bot.getPartIndexOfElt(topPart.next.getLit());
			if(!areEqv(0,k, topPosHash,  botPosHash,
					topNegHash, botNegHash, topFreqs, botFreqs)) {
				boolean added = false;
				for(int i = offset; !added && i < bot.headPointers.size(); i++) {
					int example = topAssn[i-offset];

					if(areEqv(example, k, topPosHash,  botPosHash,
							topNegHash, botNegHash, topFreqs, botFreqs)) {
						
						MyEntry toAdd = botPtr;
						botPtr = toAdd.prev;
						
						bot.remove(toAdd);
						bot.headPointers.get(i).add(toAdd);
						added = true;
					}
				}

				if(!added) {
					return false;
				}
			}
			
			k++;
			botPtr = botPtr.next;

		}
		
		return this.isIsomorphicWith(bot);
	}


	private boolean areEqv(int k, int i, int[] topPosHash, int[] botPosHash,
			int[] topNegHash, int[] botNegHash, int[][][] topFreqs,
			int[][][] botFreqs) {
		return topPosHash[k] == botPosHash[i] &&
				topNegHash[k] == botNegHash[i]
								&&
						Arrays.equals(topFreqs[0][k],botFreqs[0][i]) &&
						Arrays.equals(topFreqs[1][k],botFreqs[1][i]);
	}

	private boolean areEqv(int k, int[] topPosHash, int[] botPosHash,
			int[] topNegHash, int[] botNegHash, int[][][] topFreqs,
			int[][][] botFreqs) {
		return topPosHash[k] == botPosHash[k] &&
				topNegHash[k] == botNegHash[k]
								&&
						Arrays.equals(topFreqs[0][k],botFreqs[0][k]) &&
						Arrays.equals(topFreqs[1][k],botFreqs[1][k]);
	}

	private boolean areEqv(int k, int i, int[] posHash,int[] negHash, int[][][] freqs) {
		return posHash[k] == posHash[i] &&
				negHash[k] == negHash[i]
								&&
						Arrays.equals(freqs[0][k],freqs[0][i]) &&
						Arrays.equals(freqs[1][k],freqs[1][i]);
	}
	
	private void checkSizes() {
//		for(HeadPointer hp : headPointers) {
//			int size = hp.size;
//			
//			MyEntry cur = hp;
//			
//			while(cur.next != null) {
//				size--;
//				cur = cur.next;
//				
//				if(cur.prev == null) throw new RuntimeException();
//			}
//			
//			if(size != 0) {
//				throw new RuntimeException();
//			}
//		}
	}

}
