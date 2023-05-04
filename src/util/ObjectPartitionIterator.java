package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ObjectPartitionIterator<T> implements Iterator<List<T>> {
	private List<T> items;
	private PartitionIterator iter;
	
	public ObjectPartitionIterator(List<T> items) {
		ArrayList<T> myItems = new ArrayList<T>(items.size());
		myItems.addAll(items);
		this.items= myItems;
		
		setup();
	}

	private void setup() {
		int[] selection = new int[this.items.size()];
		for(int k = 0; k < selection.length; k++) {
			selection[k] = 2;
		}
		
		iter = new PartitionIterator(selection);
	}
	
	public ObjectPartitionIterator(T[] items) {
		ArrayList<T> myItems = new ArrayList<T>(items.length);
		
		for(T t: items) {
			myItems.add(t);
		}

		this.items= myItems;
		
		setup();
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public List<T> next() {
		int[] curSelection = iter.next();
		LinkedList<T> ret = new LinkedList<T>();
		
		for(int k = 0; k < curSelection.length; k++) {
			if(curSelection[k] == 1) {
				ret.add(items.get(k));
			}
		}
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
