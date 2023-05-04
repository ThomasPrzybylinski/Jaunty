package util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * 
 * @author Thomas Przybylinski
 *
 * 
 *TODO: Why a HashMap? Why not a LitsMap?
 */
public class IntegralDisjointSet {
	private class SetItem implements Comparable<SetItem> {
		private int item;
		private SetItem parent;
		private Set<SetItem> children = new LinkedHashSet<SetItem>();
		private int rank;


		public SetItem(int item) {
			this.item = item;
			parent = null;
		}

		//		public SetItem getParent() {
		//			return parent;
		//		}
		//
		//		public void setParent(SetItem parent) {
		//			this.parent = parent;
		//		}

		public int getItem() {
			return item;
		}

		//		public int getRank() {
		//			return rank;
		//		}
		//
		//		public void setRank(int rank) {
		//			this.rank = rank;
		//		}



		public String toString() {
			return item + ":" + rank;
		}

		@Override
		public int compareTo(SetItem o) {
			return this.item-o.item;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SetItem) {
				return item == ((SetItem)obj).item;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return item;
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

	public IntegralDisjointSet(IntegralDisjointSet old) {
		items = new SetItem[old.items.length];

		this.min = old.min;

		for(int k = 0; k < items.length; k++) {
			items[k] = new SetItem(k+min);

			for(int i = 0; i < k; i++) {
				if(old.sameSet(k+min,i+min)) {
					this.join(k+min,i+min);
				}
			}
		}
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return items.length + min - 1;
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
		//curItem becomes root
		while(!items.isEmpty()) {
			SetItem item = items.poll();
			item.parent.children.remove(item);
			item.parent = curItem;
			curItem.children.add(item);
		}


		return curItem;
	}

	public void join(int item1, int item2) {
		SetItem root1 = findRoot(item1);
		SetItem root2 = findRoot(item2);

		if(root1 == root2) return;

		boolean root1Parent = false;

		if(root1.rank > root2.rank) {
			root1Parent = true;
			root2.parent = root1;
			root1.children.add(root2);
		} else if(root1.rank < root2.rank) {
			root1.parent = root2;
			root2.children.add(root1);
		} else {
			root1.parent = root2;
			root2.children.add(root1);
			root2.rank++;
		}

		//Make the root the least elt
		SetItem root;
		SetItem other;
		root = root1Parent ? root1 : root2;
		other = root1Parent ? root2 : root1;

		boolean rootGrtr = Math.abs(root.item) - Math.abs(other.item) > 0;
		rootGrtr = rootGrtr || (root.item > 0 && (root.item-other.item == 0));


		if(rootGrtr) {
			root.children.remove(other);
			int rootInt = root.item;
			int otherInt = other.item;

			root.item = otherInt;
			other.item = rootInt;

			items[(rootInt-min)] = other;
			items[(otherInt-min)] = root;
			root.children.add(other);
		}
	}

	public List<Integer> getSetWith(int elt) {
		SetItem root = findRoot(elt);

		if(root == null) return null;

		LinkedList<Integer> ret = new LinkedList<Integer>();

		fillList(ret,root);

		return ret;
	}

	private void fillList(LinkedList<Integer> ret, SetItem root) {
		ret.add(root.item);

		for(SetItem item : root.children) {
			fillList(ret,item);
		}

	}
	//This is also the root
	public int getLeastEltInSet(int elt) {
		SetItem root = findRoot(elt);

		return root.item;
	}

	public Set<Integer> getRoots() {
		HashSet<Integer> ret = new HashSet<Integer>();
		for(SetItem st : items) {
			ret.add(findRoot(st).getItem());
		}

		return ret;
	}

	public String toString() {
		boolean[] seenRoot = new boolean[items.length];
		StringBuilder sb = new StringBuilder();

		LinkedList<Integer> orbit = new LinkedList<Integer>();
		for(SetItem item : items) {
			SetItem r = findRoot(item);

			if(!seenRoot[r.item-min]) {
				seenRoot[r.item-min] = true;

				fillList(orbit,r);
				sb.append(orbit.toString());
				sb.append(' ');
				orbit.clear();
			}
		}

		return sb.toString();
	}


	//	public int getSmallest(SetItem root, int curSmallest) {
	//		curSmallest = Math.min(root.item,curSmallest);
	//		
	//		for(SetItem item : root.children) {
	//			curSmallest = getSmallest(item,curSmallest);
	//		}
	//		
	//		return curSmallest;
	//	}



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
