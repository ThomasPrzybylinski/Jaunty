package util.lit;

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

public class LitsMap_old<T> implements Map<int[],T> {
	public class LitNode {

		private LitNode[] children; //variable sized array that's smallest possible that fits all possible ranges
									//For memory efficiency
		int translation;
		
		boolean endPoint; 
		LitNode prev; //for when I implement the iterator
		T value;

		public LitNode(int numVars) {
			translation = 0;
			children = null;//(LitNode[])Array.newInstance(this.getClass(),0);
			endPoint = false;
		}

		public LitNode(LitNode parent) {
			translation = 0;
			children = null;//(LitNode[])Array.newInstance(this.getClass(),0);
			endPoint = false;
			prev = parent;
		}
		
		public LitNode getChild(int literal) {
			if(children == null) return null;
			int index = literal + translation;
			if(index >= 0 && index < children.length) {
				return children[index];
			} else {
				return null;
			}
		}
		
		@SuppressWarnings("unchecked")
		public void setChild(int literal, LitNode n) {
			int index = literal + translation;
			if(children == null) {
				children = (LitNode[])Array.newInstance(this.getClass(),1);
				translation = -literal;
				children[0] = n;
			} else if(index < 0) {
				int toTran = -index;
				LitNode[] newChildren = (LitNode[])Array.newInstance(this.getClass(),toTran+children.length);
				newChildren[0] = n;
				
				for(int k = toTran; k < newChildren.length; k++) {
					newChildren[k] = children[k+index];
				}
				translation = translation + toTran;
				children = newChildren;
			} else if(index >= children.length) {
				LitNode[] newChildren = (LitNode[])Array.newInstance(this.getClass(),index+1);
				
				newChildren[index] = n;
				
				for(int k = 0; k < children.length; k++) {
					newChildren[k] = children[k];
				}
				children = newChildren;
			} else {
				children[index] = n;
			}
		}

		public T getValue() {
			return value;
		}

		public boolean isLeaf() {
			if(children == null) return true;
			for(LitNode ln : children) {
				if(ln != null) return false;
			}
			return true;
		}
	}

	private class DefaultIterator implements Iterator<LitNode> {
//		private LitsMap<T> lm;
		private LinkedList<Integer> curList;
		private LitNode cur;
		private int[] curKey;
		private int[] retKey;

		public DefaultIterator(LitsMap_old<T> lm) {
//			this.lm = lm;
			this.cur = lm.root;
			curList = new LinkedList<Integer>();
			curKey = new int[0];
			if(!lm.root.endPoint) {
				getNext();
			}
		}

		private final Integer negOne = -1;
		public void getNext() {
			if(!hasNext()) return;

			boolean ok = false;
			Integer last = negOne; //Since we ended on this node, we have not explored any children of cur

			do {
				int next = getNextNonEmptyIndex(cur, last!= null ? last : -1);

				if(next == -1) {
					last = curList.pollLast(); //gets literal of previous LitNode
					cur = cur.prev;
					if(cur != null) { //prev is root
						last = last + cur.translation; //convert literal to index
					}
				} else {
					last = negOne;
					curList.add(next - cur.translation); //Remember to add as literal
					cur = cur.children[next];
					ok = true;
				}

			} while(cur != null && (!ok || !cur.endPoint));

			retKey = curKey;
			if(cur == null) {
				curKey = null;
			} else {
				curKey = new int[curList.size()];
				int index = 0;
				for(int i : curList) {
					curKey[index] = i;
					index++;
				}
			}
			
		}

		private int getNextNonEmptyIndex(LitNode curNode, int last) {
			if(curNode.children == null) return -1;
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

	public LitsMap_old(int numVars) {
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

		private LitsMap_old<T> getOuterType() {
			return LitsMap_old.this;
		}
	}
	
	@Override
	public Set<java.util.Map.Entry<int[], T>> entrySet() {
		Set<Entry<int[],T>> ret = new HashSet<Entry<int[],T>>();
		

		DefaultIterator iter = new DefaultIterator(this);
		while(iter.hasNext()) {
			LitNode toAdd = iter.next();
			
			ret.add(new LitsMapEntry(iter.retKey,toAdd));
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
			parent.setChild(e[index],null);

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
