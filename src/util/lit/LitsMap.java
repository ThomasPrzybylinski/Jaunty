package util.lit;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import util.lit.IntLinkedHashMap.IntEntry;


//TODO: Improve set returns?
//TODO: Implement LitsSet using LitsMap
//TODO: Do lazy LitNode.children array initialization to decrease space requirement
//Uses a trie-like structure to contain lists of literals (1 to n and -1 to -n)
//It assumes that they have already been put in some order 
//to facilitate using this structure to store permutation arrays 

public class LitsMap<T> implements Map<int[],T> {
	public class LitNode {

		private IntHashMap<LitNode> children;  //variable sized array that's smallest possible that fits all possible ranges
									//For memory efficiency
		boolean endPoint; 
		LitNode prev; //for when I implement the iterator
		T value;

		public LitNode(int numVars) {
			children = new IntHashMap<LitNode>(2);
			endPoint = false;
		}

		public LitNode(LitNode parent) {
			children = new IntHashMap<LitNode>(2);
			endPoint = false;
			prev = parent;
		}
		
		public LitNode getChild(int literal) {
			return children.get(literal);
		}
		
		public void setChild(int literal, LitNode n) {
			children.put(literal,n);
		}

		public T getValue() {
			return value;
		}

		public boolean isLeaf() {
			return children.size() == 0;
		}

		public void remove(int i) {
			children.remove(i);
		}
	}

	private class DefaultIterator implements Iterator<LitNode> {
//		private LitsMap<T> lm;
		private LinkedList<IntHashMap<LitNode>.ValueIter> stack;
		private IntList curKey;
		private IntList retKey;
		private LitNode cur;

		public DefaultIterator(LitsMap<T> lm) {
//			this.lm = lm;
			curKey = new ArrayIntList();
			retKey = new ArrayIntList();
			this.cur = lm.root;
			
			stack = new LinkedList<IntHashMap<LitNode>.ValueIter>();
			stack.addLast(cur.children.getIter());
			
			if(!lm.root.endPoint) {
				getNext();
			}
		}

		public void getNext() {
			if(!hasNext()) return;

			
			do {
				while(!stack.isEmpty() && !stack.getLast().hasNext()) {
					stack.removeLast();
					if(!stack.isEmpty()) {
						//They are always off by one
						curKey.removeElementAt(curKey.size()-1);
					}
				}
				
				if(!stack.isEmpty()) {
					cur = stack.getLast().next(); 
					curKey.add(stack.getLast().curKey());
					stack.addLast(cur.children.getIter());
				}
				
			} while(!stack.isEmpty() && cur != null && !cur.endPoint);
			if(stack.isEmpty()) cur = null;
		}
		
		public int[] getKey() {
			return retKey.toArray();
		}

		public boolean hasNext() {
			return cur != null;
		}

		public LitNode next() {
			LitNode ret = cur;
			retKey.clear();
			retKey.addAll(curKey);
			getNext();
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	private LitNode root;
	private int numVars;

	public LitsMap(int numVars) {
		this.numVars = numVars;
		root = new LitNode(numVars);
	}

	public int getNumVars() {
		return numVars;
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
			ls.add(iter.getKey());
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

		@SuppressWarnings("unchecked")
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

		private LitsMap<T> getOuterType() {
			return LitsMap.this;
		}
	}
	
	@Override
	public Set<java.util.Map.Entry<int[], T>> entrySet() {
		Set<Entry<int[],T>> ret = new HashSet<Entry<int[],T>>();
		

		DefaultIterator iter = new DefaultIterator(this);
		while(iter.hasNext()) {
			LitNode toAdd = iter.next();
			
			ret.add(new LitsMapEntry(iter.getKey(),toAdd));
		}
		
		return ret;
	}
	
	@Override
	public T put(int[] key, T value) {
		LitNode prev = root;

		for(int k = 0; k < key.length; k++) {
			LitNode cur = prev.getChild(key[k]);
			if(cur == null) {
				LitNode temp = new LitNode(prev);
				prev.setChild(key[k],temp);
				cur = temp;
			}
			prev = cur;
		}
		prev.endPoint = true;
		
		T ret = prev.value;
		prev.value = value;

		return ret;
	}

	@Override
	public void clear() {
		root = new LitNode(numVars);
	}

	private LitNode traverseTo(int[] e) {
		LitNode prev = root;

		for(int k = 0; k < e.length; k++) {
			LitNode cur = prev.getChild(e[k]); 
			if(cur == null) {
				return null;
			}
			prev = cur;
		}
		return prev;
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
		if(root.children == null) return true;
		
		return root.children.size() == 0;
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
			parent.remove(e[index]);

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

	///Maybe I should normally obsolete this, since it's normally a very bad idea to use this
	public Iterator<LitNode> getRawIterator() {
		return new DefaultIterator(this);
	}


	

}
