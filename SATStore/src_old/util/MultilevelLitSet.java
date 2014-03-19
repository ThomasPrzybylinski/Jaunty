package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class MultilevelLitSet implements Set<int[]>{
	private LitsSet master;
	LinkedList<LitsSet> undoSet;
	
	public MultilevelLitSet(int numVars) {
		master = new LitsSet(numVars);
		undoSet = new LinkedList<LitsSet>();
	}
	
	public void post() {
		undoSet.push(new LitsSet(master.getNumVars()));
	}
	
	public LitsSet pop() {
		LitsSet curSet = undoSet.pop();
		master.removeAll(curSet);
		return curSet;
	}
	
	@Override
	public boolean add(int[] arg0) {
		master.add(arg0);
		undoSet.peek().add(arg0);
		
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends int[]> arg0) {
		for(int[] i : arg0) {
			this.add(i);
		}
		return true;
	}

	@Override
	public void clear() {
		master.clear();
		undoSet.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return master.contains(arg0);
	}
	
	public boolean contains(int[] arg0) {
		return master.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return master.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return master.isEmpty();
	}

	@Override
	public Iterator<int[]> iterator() {
		return master.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		boolean ret = master.remove(arg0);
		
		for(LitsSet ls : undoSet) {
			ls.remove(arg0);
		}
		
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean ret = master.removeAll(arg0);
		for(LitsSet ls : undoSet) {
			ls.removeAll(arg0);
		}
		
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean ret = master.retainAll(arg0);
		for(LitsSet ls : undoSet) {
			ls.retainAll(arg0);
		}
		
		return ret;
	}

	@Override
	public int size() {
		return master.size();
	}

	@Override
	public Object[] toArray() {
		return master.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return master.toArray(arg0);
	}

}
