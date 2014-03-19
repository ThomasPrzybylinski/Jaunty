package util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Thomas Przybylinski
 *
 * @param <T> The type of object that we are unioning into sets. This implementation makes use of hashing, so sensible hashCodes and equals
 * 	are advised. 
 * 
 *TODO: Why a HashMap? Why not a LitsMap?
 */
public class IntegralDisjointSet {
	private class SetItem {
		private int item;
		private SetItem parent;
		private int rank;
		
		public SetItem(int item) {
			this.item = item;
			parent = null;
		}

		public SetItem getParent() {
			return parent;
		}

		public void setParent(SetItem parent) {
			this.parent = parent;
		}

		public int getItem() {
			return item;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}
		
	}
	
	private SetItem[] items;
	private int min;
	
	public IntegralDisjointSet(int min, int max) {
		items = new SetItem[(max-min)+1];
		
		this.min = min;
		
		for(int k = 0; k < items.length; k++) {
			items[k] = new SetItem(k+min);
		}
	}
	
	public boolean sameSet(int item1, int item2) {
		return findRoot(item1) == findRoot(item2);
	}
	
	public int getRootOf(int item) {
		return findRoot(item).getItem();
	}
	
	protected SetItem findRoot(int item1) {
		SetItem curItem = items[(item1-min)];
		
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
	
	public void join(int item1, int item2) {
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
	
//	public IntegralDisjointSet getCopy() {
//		IntegralDisjointSet ret = new IntegralDisjointSet(this.min,this.min+items.length-1);
//		
//		for(Entry<T,SetItem> entry : this.itemsMap.entrySet()) {
//			SetItem parentItem = entry.getValue().parent;
//			if(parentItem != null) {
//				T parent = parentItem.item;
//				ret.itemsMap.get(entry.getKey()).setParent(ret.itemsMap.get(parent));
//			}
//		}
//		
//		return ret;
//	}
	
//	public List<Set<Integer>> getSets() {
//		HashMap<SetItem,Integer> rootToIndex = new HashMap<SetItem,Integer>();
//		List<Set<Integer>> ret = new ArrayList<Set<Integer>>();
//		
//		for(SetItem item : itemsMap.values()) {
//			SetItem rootItem = findRoot(item);
//			
//			if(rootToIndex.containsKey(rootItem)) {
//				int index = rootToIndex.get(rootItem);
//				ret.get(index).add(item.getItem());
//			} else {
//				int index = ret.size();
//				Set<T> toAdd = new HashSet<T>();
//				toAdd.add(item.getItem());
//				ret.add(toAdd);
//				rootToIndex.put(rootItem,index);
//			}
//		}
//		
//		return ret;
//		
//	}
	
//	//We make it a HashSet just to show that this implementation uses hashing
//	public HashSet<T> getRoots() {
//		HashSet<T> ret = new HashSet<T>();
//		for(SetItem st : itemsMap.values()) {
//			ret.add(findRoot(st).getItem());
//		}
//		
//		return ret;
//	}
}
