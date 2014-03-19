package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author Thomas Przybylinski
 *
 * @param <T> The type of object that we are unioning into sets. This implementation makes use of hashing, so sensible hashCodes and equals
 * 	are advised. 
 * 
 *TODO: Why a HashMap? Why not a LitsMap?
 */
public class DisjointSet<T> {
	private class SetItem {
		private T item;
		private SetItem parent;
		private int rank;
		
		public SetItem(T item) {
			this.item = item;
			parent = null;
		}

		public SetItem getParent() {
			return parent;
		}

		public void setParent(SetItem parent) {
			this.parent = parent;
		}

		public T getItem() {
			return item;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}
		
		public String toString() {
			return (item == null ? "null" : item.toString()) + ":" + rank;
		}
		
	}
	
	private HashMap<T,SetItem> itemsMap = new HashMap<T,SetItem>();
	private Collection<T> items;

	public DisjointSet() {
		this.items = new TreeSet<T>();
	}

	
	public DisjointSet(Collection<? extends T> items) {
		this();

		for(T obj : items) {
			unsafeAdd(obj);
		}
	}
	
	//Returns false if item already present, true if not 
	public boolean add(T item) {
		if(itemsMap.containsKey(item)) return false;
		unsafeAdd(item);
		return true;
	}

	private void unsafeAdd(T item) {
		items.add(item);
		itemsMap.put(item, new SetItem(item));
	}
	
	public boolean sameSet(T item1, T item2) {
		return findRoot(item1) == findRoot(item2);
	}
	
	public T getRootOf(T item) {
		return findRoot(item).getItem();
	}
	
	protected SetItem findRoot(T item1) {
		SetItem curItem = itemsMap.get(item1);
		
		return findRoot(curItem);

	}

	private SetItem findRoot(SetItem curItem) {
		Queue<SetItem> items = new LinkedList<SetItem>();
		
		while(curItem.parent != null) {
			items.add(curItem);
			curItem = curItem.parent;
		}
		
		while(!items.isEmpty()) {
			items.poll().parent = curItem;
		}
				
		return curItem;
	}
	
	public void join(T item1, T item2) {
		SetItem root1 = findRoot(item1);
		SetItem root2 = findRoot(item2);
		
		if(root1 == root2) return;
		
		if(root1.rank > root2.rank) {
			root2.parent = root1;
		} else if(root1.rank < root2.rank) {
			root1.parent = root2;
		} else {
			root1.parent = root2;
			root1.rank++;
		}
	}
	
	public DisjointSet<T> getCopy() {
		DisjointSet<T> ret = new DisjointSet<T>(this.itemsMap.keySet());
		
		for(Entry<T,SetItem> entry : this.itemsMap.entrySet()) {
			SetItem parentItem = entry.getValue().parent;
			if(parentItem != null) {
				T parent = parentItem.item;
				ret.itemsMap.get(entry.getKey()).setParent(ret.itemsMap.get(parent));
			}
		}
		
		return ret;
	}
	
	public List<Set<T>> getSets() {
		HashMap<SetItem,Integer> rootToIndex = new HashMap<SetItem,Integer>();
		List<Set<T>> ret = new ArrayList<Set<T>>();
		
		for(SetItem item : itemsMap.values()) {
			SetItem rootItem = findRoot(item);
			
			if(rootToIndex.containsKey(rootItem)) {
				int index = rootToIndex.get(rootItem);
				ret.get(index).add(item.getItem());
			} else {
				int index = ret.size();
				Set<T> toAdd = new HashSet<T>();
				toAdd.add(item.getItem());
				ret.add(toAdd);
				rootToIndex.put(rootItem,index);
			}
		}
		
		return ret;
		
	}
	
	//We make it a HashSet just to show that this implementation uses hashing
	public HashSet<T> getRoots() {
		HashSet<T> ret = new HashSet<T>();
		for(SetItem st : itemsMap.values()) {
			ret.add(findRoot(st).getItem());
		}
		
		return ret;
	}
	
	public Collection<? extends T> getKeys() {
		return items;
	}
}
