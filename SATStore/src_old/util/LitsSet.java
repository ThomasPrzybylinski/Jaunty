package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


//TODO: Low priority: Implement using LitsMap because they work almost exactly the same 

//Uses a trie-like structure to contain lists of literals (1 to n and -1 to -n)
//It assumes that they have already been put in some order 
//to facilitate using this structure to store permutation arrays 

public class LitsSet implements Set<int[]> {
	private class LitNode {

		LitNode[] children;
		boolean endPoint; 
		LitNode prev; //for when I implement the iterator

		public LitNode(int numVars) {
			children = new LitNode[2*numVars+1];
			endPoint = false;
		}

		public LitNode(LitNode parent) {
			children = new LitNode[2*numVars+1];
			endPoint = false;
			prev = parent;
		}

		public boolean isLeaf() {
			for(LitNode ln : children) {
				if(ln != null) return false;
			}
			return true;
		}
	}

	private class LitSetIterator implements Iterator<int[]> {
		private LitsSet ls;
		private LinkedList<Integer> curList;
		private LitNode cur;
		private int[] toRet;

		public LitSetIterator(LitsSet ls) {
			this.ls = ls;
			this.cur = ls.root;
			curList = new LinkedList<Integer>();

			toRet = new int[0];
			if(!ls.root.endPoint) {
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
				toRet = null;
			} else {
				toRet = new int[curList.size()];
				int index = 0;
				for(int i : curList) {
					toRet[index] = i - numVars; //Remember that actual int is lit# + numvars so that negatives can be positive
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

		@Override
		public boolean hasNext() {
			return toRet != null;
		}

		@Override
		public int[] next() {
			int[] ret = toRet;
			getNext();
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	private LitNode root;
	private int numVars;

	public LitsSet(int numVars) {
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
	public boolean add(int[] e) {
		LitNode cur = root;

		for(int k = 0; k < e.length; k++) {
			int index = getIndex(e[k]);
			if(cur.children[index] == null) {
				cur.children[index] = new LitNode(cur);
			}
			cur = cur.children[index];
		}
		cur.endPoint = true;

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends int[]> c) {
		for(int[] toAdd : c) {
			add(toAdd);
		}
		return true;
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
	public boolean contains(Object o) {
		if(o instanceof int[]) {
			int[] e = (int[])o;
			return contains(e);
		}
		return false;
	}

	public boolean contains(int[] e) {
		LitNode node = traverseTo(e);
		return node == null ? false : node.endPoint;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o : c) {
			if(!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		for(LitNode n : root.children) {
			if(n != null) return false;
		}
		return true;
	}

	@Override
	public Iterator<int[]> iterator() {
		return new LitSetIterator(this);
	}

	@Override
	public boolean remove(Object o) {
		if(o instanceof int[]) {
			int[] e = (int[])o;
			return remove(e);
		}

		return false;
	}

	public boolean remove(int[] e) {
		LitNode node = traverseTo(e);

		if(node == null || !node.endPoint) {
			return false;
		}

		node.endPoint = false;
		int index = e.length-1;
		while(node.isLeaf() && !node.endPoint && node.prev != null) {
			LitNode parent = node.prev;
			parent.children[getIndex(e[index])] = null;

			index--;
			node = parent;
		}

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for(Object o : c) {
			this.remove(o);
		}
		return true;
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		LitsSet temp = new LitsSet(numVars);

		for(Object o : c) {
			if(this.contains(o)) {
				temp.add((int[])o);
			}
		}
		this.root = temp.root;
		return true;
	}

	//Inefficient
	@Override
	public int size() {
		Iterator<int[]> iter = this.iterator();
		int size = 0;
		while(iter.hasNext()) {
			size++;
			iter.next();
		}

		return size;
	}

	@Override
	public Object[] toArray() {
		int len = this.size();
		Object[] ret = new Object[len];

		int index = 0;
		for(int[] toAdd : this) {
			ret[index] = toAdd;
			index++;
		}
		return ret;
	}

	public int[][] toArray(int[][] a) {
		int len = this.size();
		if(a.length < len) {
			a = new int[len][];
		}
		int index = 0;
		for(int[] toAdd : this) {
			a[index] = toAdd;
			index++;
		}
		return a;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("Can only give an array of ints arrays");
	}

}
