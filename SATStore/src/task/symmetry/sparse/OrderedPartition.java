package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.IntList;

import util.lit.LitUtil;

class MyEntry implements StatsInfo {
	public static final int PTR = -1;

	public static final int UNDO = -1;
	public static final int MOD = -2;
	
	
	HeadPointer list = null;
	int index;
	MyEntry next = null;
	MyEntry prev = null;
	boolean saved = false;
	OrderedPartition that;

	public MyEntry(int item, OrderedPartition that) {
		this.index = item;
		this.that = that;
	}

	public MyEntry(int item, HeadPointer list) {
		this(item,list.that);
		this.list = list;
	}

	public void copyFrom(MyEntry entry) {
		assert(entry.index == this.index);

		this.list = entry.list;
		this.next = entry.next;
		this.prev = entry.prev;
		this.that = entry.that;
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
		return LitUtil.getLit(index,that.numVars);
	}

	public String toString() {
		return "("+LitUtil.getLit(this.index,that.numVars)+" "+this.index + (this.list == null ? "" : (" " + this.list.list))+")";  
	}
}

class HeadPointer extends MyEntry implements Comparable<HeadPointer> {
	int size = 0;
	int list;
	boolean eltModded = false;

	HeadPointer nextModded;

	MyEntry tail;

	public HeadPointer(int list, OrderedPartition that) {
		super(PTR,that);
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


		setModded();
	}
	
	public void add(MyEntry entry, OrderedPartition that) {
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


		setModded();
	}

	void setModded() {
		if(!eltModded) {
			eltModded = true;
			HeadPointer prevHead = that.moddedHead.nextModded;

			this.nextModded = prevHead;

			that.moddedHead.nextModded = this;
			that.moddedHead.size++;
		}
	}
	

	void setUnModded() {
		this.eltModded = false;
		this.nextModded = null;
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

	@Override
	public int compareTo(HeadPointer o) {
		return this.list - o.list;
	}

}

class Undo extends MyEntry {
	public Undo(int item, OrderedPartition that) {
		super(item,that);
	}

	Undo undoPtr;
	boolean head;
}


public class OrderedPartition {
	

	

	ArrayList<HeadPointer> headPointers;
	MyEntry[] partition;

	LinkedList<HeadPointer> undos;
	HeadPointer curUndo;
	HeadPointer moddedHead;
	MyEntry availHead = null;

	int numVars;

	private OrderedPartition() {
		headPointers = new ArrayList<HeadPointer>();
		undos = new LinkedList<HeadPointer>();
		curUndo = new HeadPointer(MyEntry.UNDO,this);
		moddedHead = new HeadPointer(MyEntry.MOD,this);
	}

	public OrderedPartition(int num) {
		this(num,true);
	}

	@SuppressWarnings("unchecked")
	protected OrderedPartition(int num, boolean addNums) {
		this();

		this.numVars = num;

		partition = new MyEntry[2*num+1];

		HeadPointer headPointer = new HeadPointer(0,this);
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
				partition[k] = new MyEntry(k,this);
			}
		}

		headPointers.ensureCapacity(initial.size());

		for(int k = 0; k < initial.size(); k++) {
			HeadPointer curPointer = new HeadPointer(k,this);
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

	public boolean isIsomorphicWith(OrderedPartition other, int offset) {
		if(this.headPointers.size() != other.headPointers.size()) return false;

		for(int k = offset; k < headPointers.size(); k++) {
			if(this.headPointers.get(k).size != other.headPointers.get(k).size) return false;
		}
		return true;
	}
	
	public boolean matches(OrderedPartition other) {
		if(this.headPointers.size() != other.headPointers.size()) return false;

		for(int k = 0; k < headPointers.size(); k++) {
			HeadPointer topP = this.headPointers.get(k);
			HeadPointer botP = other.headPointers.get(k);
			if(topP.size != botP.size) return false;
			
			if(topP.size > 1) {
				MyEntry topE = topP.next;
				MyEntry botE = botP.next;
				
				while(topE != null) {
					if(topE.index != botE.index) {
						return false;
					}
					topE = topE.next;
					botE = botE.next;
				}
			}
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
		HeadPointer newPointer = new HeadPointer(headPointers.size(),this);
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

	public int[] getShortcutPermutation(OrderedPartition bottom, int num) {
		assert(this.numVars == bottom.numVars && this.headPointers.size() == bottom.headPointers.size());
		int[] perm = new int[num+1];
		perm[0] = 0;

		for(int k = 0; k < num+1; k++) {
			perm[k] = k;
		}

		for(int k = 0; k < this.headPointers.size(); k++) {
			HeadPointer topPt = this.headPointers.get(k);
			HeadPointer botPt = bottom.headPointers.get(k);

			if(topPt.size != botPt.size) {
				return null; //cannot denote any permutation
			}

			MyEntry topEntry = topPt.next;
			MyEntry botEntry = botPt.next;

			while(topEntry != null) {
				int topLit = LitUtil.getLit(topEntry.index,numVars);
				int botLit = LitUtil.getLit(botEntry.index,numVars);

				int topVar = Math.abs(topLit);
				perm[topVar] =  (topVar/topLit)*botLit;

				topEntry = topEntry.next;
				botEntry = botEntry.next;
			}
		}

		return perm;
	}

	public int[] getPartialPermutation(OrderedPartition bottom, int num) {
		assert(this.numVars == bottom.numVars && this.headPointers.size() == bottom.headPointers.size());
		int[] perm = new int[num+1];
		perm[0] = 0;

		for(int k = 0; k < num+1; k++) {
			perm[k] = 0;
		}

		for(int k = 0; k < this.headPointers.size(); k++) {
			HeadPointer topPt = this.headPointers.get(k);
			HeadPointer botPt = bottom.headPointers.get(k);

			if(topPt.size == 1 && botPt.size == 1) {
				int topLit = LitUtil.getLit(topPt.next.index,numVars);
				int botLit = LitUtil.getLit(botPt.next.index,numVars);

				int topVar = Math.abs(topLit);
				perm[topVar] =  (topVar/topLit)*botLit;
			}
		}

		return perm;
	}
	
	public int[] matchOf(OrderedPartition bottom, int num) {
		if(this.numVars != bottom.numVars || this.headPointers.size() != bottom.headPointers.size()) {
			return null;
		}
		int[] perm = new int[num+1];
		perm[0] = 0;

		for(int k = 0; k < num+1; k++) {
			perm[k] = k;
		}

		for(int k = 0; k < this.headPointers.size(); k++) {
			HeadPointer topPt = this.headPointers.get(k);
			HeadPointer botPt = bottom.headPointers.get(k);

			if(topPt.size != botPt.size) {
				return null; //cannot denote any permutation
			}

			MyEntry topEntry = topPt.next;
			MyEntry botEntry = botPt.next;

			while(topEntry != null) {
				int topLit = LitUtil.getLit(topEntry.index,numVars);
				int botLit = LitUtil.getLit(botEntry.index,numVars);
				
				if(topPt.size > 1 && topLit != botLit) {
					return null; //not a matching perm
				}

				int topVar = Math.abs(topLit);
				perm[topVar] =  (topVar/topLit)*botLit;

				topEntry = topEntry.next;
				botEntry = botEntry.next;
			}
		}

		return perm;
	}


	private void remove(MyEntry entry) {
		save(entry);
		entry.list.setModded();
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

	public void setBasePoint() {
		undos.clear();
		curUndo = new HeadPointer(MyEntry.UNDO,this);
	}

	public void save(MyEntry entry) {
		if(entry.index == -1) throw new RuntimeException();

		//		if(entry.saved) return; //the undo will already restore it to its proper place

		Undo prevHead = (Undo)curUndo.next;

		Undo toAdd = new Undo(entry.index,this);//allocate(entry.index);
		toAdd.copyFrom(entry);

		if(entry.prev == entry.list) {
			toAdd.head = true;
		}

		if(prevHead != null) {
			toAdd.undoPtr = prevHead; 
		}

		curUndo.next = toAdd;
	}
	
	private void add(HeadPointer hp, MyEntry entry, OrderedPartition bot) {
		hp.add(entry,bot);
	}

	public void post() {
		remModded(this);
		
		if(curUndo != null) {
			undos.push(curUndo);
		}
		curUndo = new HeadPointer(MyEntry.UNDO,this);
		
		
		checkSizes();

	}



	public void pop() {
		checkSizes();
		remModded(this);
		
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

		}

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
	
	private void remModded(OrderedPartition that) {
		HeadPointer prevModded = that.moddedHead;
		while(prevModded != null) {
			HeadPointer next = prevModded.nextModded;
			prevModded.setUnModded();
			prevModded = next;
		}
		that.moddedHead.size = 0;
	}
	
	public HeadPointer[] modded(OrderedPartition that) {
		HeadPointer[] modded = new HeadPointer[that.moddedHead.size];
		
		HeadPointer cur = that.moddedHead.nextModded;
		int index = 0;
		while(cur != null) {
			modded[index] = cur;
			index++;
			cur = cur.nextModded;
		}
		
		remModded(that);
		
		Arrays.sort(modded);
		
		return modded;
		
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
		Undo prevHeadUndo = null;
		boolean ok = false;

		do {
			headUndo = (Undo)curUndo.next;
			ok = refine(bot,stats,prevHeadUndo,initial);

			if(!ok) break;

			initial = false;

			prevHeadUndo = headUndo;

			ppoIso = this.isIsomorphicWith(bot);

		} while(curUndo.next != null && headUndo != curUndo.next && ppoIso);



		if(ppoIso && ok) {
			return true;
		} else {
			return false;
		}
	}


	//varToClause is 1 if clause of index k contains integer i

	private TreeSet<Integer> modded = new TreeSet<Integer>();
	private boolean refine(OrderedPartition bot, SparseSymmetryStatistics stats, Undo prevHeadUndo, boolean initial) {
//		modded.clear();
//
//		if(initial) {
//			for(int k = 0; k < headPointers.size(); k++) {
//				modded.add(k);
//			}
//		} else {
//			Undo entry = (Undo)curUndo.next;
//
//			while(entry != null && entry != prevHeadUndo) {
//				modded.add(entry.list.list);
//
//				MyEntry undidEntry = partition[entry.index];
//				modded.add(undidEntry.list.list);
//
//				if(entry == prevHeadUndo) break;
//
//				entry = entry.undoPtr;
//			}
//		}
//
//		HeadPointer[] topToUse = new HeadPointer[modded.size()];
//		HeadPointer[] botToUse = new HeadPointer[modded.size()];
//
//		int index = 0;
//		for(Integer headIndex : modded) {
//			topToUse[index] = headPointers.get(headIndex);
//			botToUse[index] = bot.headPointers.get(headIndex);
//			index++;
//		}
		
		HeadPointer[] topToUse;
		HeadPointer[] botToUse;
		
		if(initial) {
			topToUse = new HeadPointer[headPointers.size()];
			botToUse = new HeadPointer[headPointers.size()];
			for(int k = 0; k < headPointers.size(); k++) {
				topToUse[k] = headPointers.get(k);
				botToUse[k] = bot.headPointers.get(k);
			}
		} else {
			topToUse = this.modded(this);
			botToUse = bot.modded(bot);
		}
		

		int[][] topFreqs = null;
		int[][] botFreqs = null;

		long[][] hash = stats.getPartHashes(topToUse,botToUse);
		long[] topHash = hash[0];
		long[] botHash =hash[1];

//		int[][][] freqs = stats.getPartFreqs(topToUse,botToUse);
//		topFreqs = freqs[0];
//		botFreqs = freqs[1];
		
//				int[][][] freqs = stats.getPartFreqs(topToUse,botToUse);
//				int[][] topFreqs = freqs[0];//stats.getPartFreqs(topToUse);
//				int[][] botFreqs = freqs[1];//stats.getPartFreqs(botToUse);

		//		int[] topHash = new int[topFreqs.length];
		//		int[] botHash = new int[botFreqs.length];

		//		for(int k = 0; k < topFreqs.length; k++) {
		//			topHash[k] = Arrays.hashCode(topFreqs[k]);
		//			botHash[k] = Arrays.hashCode(botFreqs[k]);
		//		}

		for(int p = 0; p < headPointers.size(); p++) {
			HeadPointer topPart = this.headPointers.get(p);
			HeadPointer bottomPart = bot.headPointers.get(p);

			boolean correct = true;
			if(topPart.size == 1) {
				continue;
			} else {
				 correct = doRefine(bot,topPart, bottomPart,topFreqs,botFreqs,topHash,botHash, topToUse,botToUse);
			}

			if(!correct) {
				return false;
			}
		}

		return true;//this.isIsomorphicWith(bot);

	}

	private boolean doRefine(OrderedPartition bot, HeadPointer topPart, HeadPointer bottomPart,
			int[][] topFreqs, int[][] botFreqs, long[] topHash, long[] botHash, HeadPointer[] topToUse, HeadPointer[] botToUse) {
		int offset = this.headPointers.size();

		//Given an index for a new refined cell, give an example index
		//Is the last elt added, to make sure we can always unify
		//the top and bottom
		int[] topAssn = new int[topPart.size];

		Arrays.fill(topAssn,-1);

		MyEntry topPtr = topPart.next;

		int origPosInd = LitUtil.getIndex(topPtr.getLit(),numVars);
		int origNegInd = LitUtil.getIndex(-topPtr.getLit(),numVars);
		int k = 0;
		while(topPtr.hasNext()) {
			//First elt always stays in the list
			k++;
			topPtr = topPtr.next;

			int topIndex = LitUtil.getIndex(topPtr.getLit(),numVars);
			int topNegIndex = LitUtil.getIndex(-topPtr.getLit(),numVars);


			//			int top = topPtr.getLit();
			//			int bot = botPtr.getLit();

			if(!areEqv(topIndex, topNegIndex,origPosInd,origNegInd, topHash, topFreqs)) {
				boolean added = false;
				for(int i = offset; !added && i < headPointers.size(); i++) {
					int example = topAssn[i-offset];

					int exInd = LitUtil.getIndex(example,numVars);
					int exNegInd = LitUtil.getIndex(-example,numVars);

					if(areEqv(topIndex,topNegIndex,exInd,exNegInd, topHash, topFreqs)) {
						MyEntry toAdd = topPtr;
						topPtr = topPtr.prev;

						remove(toAdd);
						headPointers.get(i).add(toAdd);
						added = true;
					}
				}

				if(!added) {
					HeadPointer newPointer = new HeadPointer(headPointers.size(),this);
					topAssn[headPointers.size()-offset] = topPtr.getLit();
					headPointers.add(newPointer);

					MyEntry toAdd = topPtr;
					topPtr = topPtr.prev;

					remove(toAdd);
					newPointer.add(toAdd);

				}
			}
		}

//		for(HeadPointer hp : headPointers) {
//			if(hp.next == null) {
//				throw new RuntimeException();
//			}
//		}

		while(this.headPointers.size() > bot.headPointers.size()) {
			HeadPointer newPointer = new HeadPointer(bot.headPointers.size(),bot);
			bot.headPointers.add(newPointer);
		}

		topPtr = null;

		MyEntry botPtr = bottomPart.next;
		k = 0;
		while(botPtr != null) {
			int botIndex = LitUtil.getIndex(botPtr.getLit(),numVars);
			int botNegIndex = LitUtil.getIndex(-botPtr.getLit(),numVars);


			if(!areEqv(origPosInd,origNegInd,botIndex,botNegIndex,
					topHash,  botHash, topFreqs, botFreqs)) {
				boolean added = false;
				for(int i = offset; !added && i < bot.headPointers.size(); i++) {
					int example = topAssn[i-offset];

					int exInd = LitUtil.getIndex(example,numVars);
					int exNegInd = LitUtil.getIndex(-example,numVars);

					if(areEqv(exInd,exNegInd, botIndex,botNegIndex,
							topHash,  botHash, topFreqs, botFreqs)) {

						MyEntry toAdd = botPtr;
						botPtr = toAdd.prev;

						bot.remove(toAdd);
						bot.add(bot.headPointers.get(i),toAdd,bot); //for strange scoping rules
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

		return this.isIsomorphicWith(bot,offset);
	}

	private boolean areEqv(int topIndex, int topNegIndex, int origPosInd,
			int origNegInd, long[] topHash, int[][] topFreqs) {
		return areEqv(topIndex,topNegIndex,origPosInd,origNegInd,topHash,topHash,topFreqs,topFreqs);
	}

	private boolean areEqv(int topInd, int topNegInd, int botInd, int botNegInd,
			long[] topHash, long[] botHash, int[][] topFreqs, int[][] botFreqs) {
		return topHash[topInd] == botHash[botInd] &&
				topHash[topNegInd] == botHash[botNegInd]
						;
		//								&&
		//						Arrays.equals(topFreqs[topInd],botFreqs[botInd]) &&
		//						Arrays.equals(topFreqs[topNegInd],botFreqs[botNegInd]);
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
