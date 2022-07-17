package util.lit;

import java.lang.reflect.Array;
import java.util.Arrays;

//Extremely simple open-addressed linked hash map for Sparse Symmetry
public class IntLinkedHashMap<T> {
	public static class IntEntry<T> {
		private int key;
		private T value;
		private IntEntry<T> next;
		private IntEntry<T> prev;
		private boolean deleted = false;
		
		
		public IntEntry() {}
		
		public IntEntry(int key, T value) {
			super();
			this.key = key;
			this.value = value;
		}

		public int getKey() {
			return key;
		}

		public T getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "[key=" + key + ", value=" + value + "]";
		}
		
		
	}
	
	public class EntryIter {
		private IntEntry<T> cur;
		
		public EntryIter(IntLinkedHashMap<T> map) {
			cur = map.head;
		}
		
		public boolean hasNext() {
			return cur.next != tail;
		}
		
		public IntEntry<T> next() {
			if(cur.next == tail) return null;

			cur = cur.next;
			
			return cur;
		}
	}
	
	private IntEntry<T> head = new IntEntry<T>();
	private IntEntry<T> tail = new IntEntry<T>();
	private IntEntry<T>[] entries;
	
	private int size;
	private int removed = 0;
	
	private float factor = 1.5f;
	
	static final int MAXIMUM_CAPACITY = 1 << 30;
	
	private static long numFinds = 0;
	private static long totalFindLen = 0;
	
	@SuppressWarnings("unchecked")
	public IntLinkedHashMap(int size) {
		entries = (IntEntry[])Array.newInstance(IntEntry.class,roundUpToPowerOf2((int)(factor*size+1)));
		head.next = tail;
		tail.prev = head;
	}
	
	public IntLinkedHashMap() {
		this(16);
	}
	
    private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }
	
	public int size() {
		return size;
	}
	
	//returns if new val
	public boolean put(int key, T value) {
		int index = find(key);
		
		if(!exists(index)) {
			add(key, value, index);
			return true;
		} else {
			entries[index].value = value;
			return false;
		}
	}
	
	public T get(int key) {
		int index = find(key);
		return exists(index) ? entries[index].value : null;
	}

	private void add(int key, T value, int index) {
		size++;
		
		if(entries[index] != null) {
			//was deleted, now being put back
			removed--;
		}
		
		if((size+removed)*factor >= entries.length) {
			rebuild(true);
			index = find(key);
		}
		
		IntEntry<T> entry = new IntEntry<T>(key,value);
		entries[index] = entry;
		
		IntEntry<T> behind = tail.prev;
		entry.next = tail;
		entry.prev = behind;
		behind.next = entry;
		tail.prev = entry;
	}
	
	public boolean contains(int key) {
		int index = find(key);
		
		return exists(index);
	}
	

	public void remove(int i) {
		int index = find(i);
		
		if(exists(index)) {
			IntEntry<T> entry = entries[index];
			entry.deleted = true;
			entry.prev.next = entry.next;
			entry.next.prev = entry.prev;
			
			removed++;
			size--;
			
			if((size+removed)*factor >= entries.length) {
				rebuild(false);
				index = find(i);
			}
		}
	}
	
	private boolean exists(int index) {
		return entries[index] != null && !entries[index].deleted; 
	}
	
	@SuppressWarnings("unchecked")
	private void rebuild(boolean increase) {
		if(increase) {
			int next = Math.min(MAXIMUM_CAPACITY,roundUpToPowerOf2(entries.length+1));
			entries = (IntEntry[])Array.newInstance(IntEntry.class,next);
		} else {
			Arrays.fill(entries,null);
		}
		removed = 0;
		
		EntryIter iter = getIter();
		
		while(iter.hasNext()) {
			IntEntry<T> next = iter.next();
			entries[find(next.key)] = next;
		}
	}

//	static long num = 0;
//	static long times = 0;
//	static long max = 0;
	private int find(int key) {
		int index = index(key);
//		times++;
		int tempNum = 0;
		while(entries[index] != null && entries[index].key != key) {
			index = (index+1)&(entries.length-1);
			tempNum++;
		}
//		max = Math.max(max,tempNum);
//		num += tempNum;
//		if(times == 100000) {
//			System.out.println();
//			System.out.println(num/(double)times);
//			System.out.println(max);
//			times = 0;
//			num = 0;
//		}
		return index;
	}
	
	public EntryIter getIter() {
		return new EntryIter(this);
	}
	
	private int hash(int key) {
		return Math.abs((key*1327217909));
	}
	
	private int index(int key) {
		return hash(key)&(entries.length-1);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		EntryIter iter = getIter();
		
		while(iter.hasNext()) {
			if(sb.length() != 0) {
				sb.append(',');
			}
			sb.append(iter.next());
		}
		
		return sb.toString();
	}

	
}
