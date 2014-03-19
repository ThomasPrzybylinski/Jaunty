package util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

//TODO: Improve set returns?
//TODO: Implement LitsSet using LitsMap
//TODO: Do lazy LitNode.children array initialization to decrease space requirement
//Uses a trie-like structure to contain lists of literals (1 to n and -1 to -n)
//It assumes that they have already been put in some order 
//to facilitate using this structure to store permutation arrays 

public class LitsMap_backup<T> implements Map<int[],T> {
	private class LitNode {

		LitNode[] children;
		boolean endPoint; 
		LitNode prev; //for when I implement the iterator
		T value;

		@SuppressWarnings("unchecked")
		public LitNode(int numVars) {
			children = (LitNode[])Array.newInstance(this.getClass(),2*numVars+1);
			endPoint = false;
		}

		@SuppressWarnings("unchecked")
		public LitNode(LitNode parent) {
			children = (LitNode[])Array.newInstance(this.getClass(),2*numVars+1);
			endPoint = false;
			prev = parent;
		}

		public T getValue() {
			return value;
		}

		public boolean isLeaf() {
			for(LitNode ln : children) {
				if(ln != null) return false;
			}
			return true;
		}
	}

	private class DefaultIterator implements Iterator<LitNode> {
		private LitsMap_backup<T> lm;
		private LinkedList<Integer> curList;
		private LitNode cur;
		private int[] curKey;

		public DefaultIterator(LitsMap_backup<T> lm) {
			this.lm = lm;
			this.cur = lm.root;
			curList = new LinkedList<Integer>();

			curKey = new int[0];
			if(!lm.root.endPoint) {
				getNext();
			}
		}

		public void getNext() {
			if(!hasNext()) return;

			boolean ok = false;
			Integer last = 0; //Since we ended on this node, we have not explored any children of cur

			do {
				int next = getNextNonEmpty(cur, last!= null ? last : -1);

				if(next == -1) {
					last = curList.pollLast();
					cur = cur.prev;
				} else {
					last = -1;
					curList.add(next);
					cur = cur.children[next];
					ok = true;
				}

			} while(cur != null && (!ok || !cur.endPoint));

			if(cur == null) {
				curKey = null;
			} else {
				curKey = new int[curList.size()];
				int index = 0;
				for(int i : curList) {
					curKey[index] = i - numVars; //Remember that actual int is lit# + numvars so that negatives can be positive
					index++;
				}
			}
		}

		private int getNextNonEmpty(LitNode curNode, int last) {
			for(int k = last+1; k < curNode.children.length; k++) {
				if(curNode.children[k] != null) {
					return k;
				}
			}
			return -1;
		}

		public boolean hasNext() {
			return curKey != null;
		}

		public LitNode next() {
			LitNode ret = cur;
			getNext();
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	private LitNode root;
	private int numVars;

	public LitsMap_backup(int numVars) {
		this.numVars = numVars;
		root = new LitNode(numVars);
	}

	public int getNumVars() {
		return numVars;
	}

	private int getIndex(int e) {
		return e + numVars;
	}

	@Override
	public boolean containsValue(Object value) {
		DefaultIterator iter = new DefaultIterator(this);
		
		while(iter.hasNext()) {
			LitNode node = iter.next();
			if((value == null ? value == node.value : value.equals(node.value))) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public T get(Object key) {
		if(key instanceof int[]) {
			return get((int[])key);
		}
		return null;
	}
	
	public T get(int[] key) {
		LitNode node = traverseTo((int[])key);
		return  node == null ? null : node.value;
	}

	@Override
	public void putAll(Map<? extends int[], ? extends T> m) {
		for(Entry<? extends int[], ? extends T> entry : m.entrySet()) {
			this.put(entry.getKey(),entry.getValue());
		}
	}

	@Override
	public Set<int[]> keySet() {
		LitsSet ls = new LitsSet(this.numVars);
		DefaultIterator iter = new DefaultIterator(this);
		while(iter.hasNext()) {
			iter.next();
			ls.add(iter.curKey);
		}
		return ls;
	}

	@Override
	public Collection<T> values() {
		LinkedList<T> ret = new LinkedList<T>();
		
		DefaultIterator iter = new DefaultIterator(this);
		while(iter.hasNext()) {
			ret.add(iter.next().value);
		}
		
		return null;
	}

	
	private class LitsMapEntry implements Entry<int[],T> {
		private int[] key;
		private LitNode node;
		
		public LitsMapEntry(int[] key, LitNode node) {
			this.key = key;
			this.node = node;
		}
		
		@Override
		public int[] getKey() {
			return key;
		}

		@Override
		public T getValue() {
			return node.value;
		}

		@Override
		public T setValue(T value) {
			T ret = node.value;
			node.value = value;
			return ret;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(key);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LitsMapEntry other = (LitsMapEntry) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (!Arrays.equals(key, other.key)) {
				return false;
			}
			return true;
		}

		private LitsMap_backup getOuterType() {
			return LitsMap_backup.this;
		}
	}
	
	@Override
	public Set<java.util.Map.Entry<int[], T>> entrySet() {
		Set<Entry<int[],T>> ret = new HashSet<Entry<int[],T>>();
		

		DefaultIterator iter = new DefaultIterator(this);
		while(iter.hasNext()) {
			LitNode toAdd = iter.next();
			
			ret.add(new LitsMapEntry(iter.curKey,toAdd));
		}
		
		return ret;
	}
	
	@Override
	public T put(int[] key, T value) {
		LitNode cur = root;

		for(int k = 0; k < key.length; k++) {
			int index = getIndex(key[k]);
			if(cur.children[index] == null) {
				cur.children[index] = new LitNode(cur);
			}
			cur = cur.children[index];
		}
		cur.endPoint = true;
		
		T ret = cur.value;
		cur.value = value;

		return ret;
	}

	@Override
	public void clear() {
		root = new LitNode(numVars);
	}

	private LitNode traverseTo(int[] e) {
		LitNode cur = root;

		for(int k = 0; k < e.length; k++) {
			int index = getIndex(e[k]);
			if(cur.children[index] == null) {
				return null;
			}
			cur = cur.children[index];
		}
		return cur;
	}

	
	@Override
	public boolean containsKey(Object key) {
		if(key instanceof int[]) {
			int[] e = (int[])key;
			return contains(e);
		}
		return false;
	}

	public boolean contains(int[] e) {
		LitNode node = traverseTo(e);
		return node == null ? false : node.endPoint;
	}

	@Override
	public boolean isEmpty() {
		for(LitNode n : root.children) {
			if(n != null) return false;
		}
		return true;
	}

	@Override
	public T remove(Object key) {
		if(key instanceof int[]) {
			return remove((int[])key);
		}
		return null;
	}
	
	public T remove(int[] e) {
		LitNode node = traverseTo(e);
		LitNode ret = node;
		
		if(node == null || !node.endPoint) {
			return null;
		}

		
		node.endPoint = false;
		int index = e.length-1;
		while(node.isLeaf() && !node.endPoint && node.prev != null) {
			LitNode parent = node.prev;
			parent.children[getIndex(e[index])] = null;

			index--;
			node = parent;
		}

		return ret.value;
	}

	//Inefficient
	@Override
	public int size() {
		DefaultIterator iter = new DefaultIterator(this);
		int size = 0;
		while(iter.hasNext()) {
			size++;
			iter.next();
		}

		return size;
	}

	
	public Iterator<LitNode> getRawIterator() {
		return new DefaultIterator(this);
	}


	

}
