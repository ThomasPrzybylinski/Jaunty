package util.lit;

import java.util.Arrays;

//Extremely simple open-addressed linked hash map for Sparse Symmetry
public class IntToIntLinkedHashMap {
	public class IntEntry {
		private int key;
		private int value;
		private IntEntry next;
		private IntEntry prev;
		
		
		public IntEntry() {}
		
		public IntEntry(int key, int value) {
			super();
			this.key = key;
			this.value = value;
		}

		public int getKey() {
			return key;
		}

		public int getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "[key=" + key + ", value=" + value + "]";
		}
		
		
	}
	
	public class EntryIter {
		private IntEntry cur;
		
		public EntryIter(IntToIntLinkedHashMap map) {
			cur = map.head;
		}
		
		public boolean hasNext() {
			return cur.next != tail;
		}
		
		public IntEntry next() {
			if(cur.next == tail) return null;

			cur = cur.next;
			
			return cur;
		}
	}
	
	private IntEntry head = new IntEntry();
	private IntEntry tail = new IntEntry();
	private IntEntry[] entries;
	
	private int size;
	private float factor;
	private static final float DEFAULT_FACTOR = .666666666666666666f;
	private final boolean quadratic;
	
	static final int MAXIMUM_CAPACITY = 1 << 30;
	
	
	public IntToIntLinkedHashMap(int size, float factor, boolean quadratic) {
		entries = new IntEntry[roundUpToPowerOf2((int)(factor*size+1))];
		this.factor = factor;
		this.quadratic = quadratic;
		
		head.next = tail;
		tail.prev = head;
	}
	
	public IntToIntLinkedHashMap(int size) {
		this(size,DEFAULT_FACTOR,false);
	}
	
	public IntToIntLinkedHashMap() {
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
	public boolean put(int key, int value) {
		int index = find(key);
		
		if(entries[index] == null) {
			add(key, value, index);
			return true;
		} else {
			entries[index].value = value;
			return false;
		}
	}

	private void add(int key, int value, int index) {
		size++;
		
		if(size >= entries.length*factor) {
			rebuild();
			index = find(key);
		}
		
		IntEntry entry = new IntEntry(key,value);
		entries[index] = entry;
		
		IntEntry behind = tail.prev;
		entry.next = tail;
		entry.prev = behind;
		behind.next = entry;
		tail.prev = entry;
	}
	
	public boolean contains(int key) {
		int index = find(key);
		
		return entries[index] != null;
	}
	
	//Starting at 0
	public void increment(int key) {
		int index = find(key);
		
		if(entries[index] == null) {
			add(key, 1, index);
		} else {
			entries[index].value++;
		}
	}
	
	public boolean addValue(int key, int value) {
		int index = find(key);
		
		if(entries[index] == null) {
			return false;
		} else {
			entries[index].value += value;
			return true;
		}
	}
	
	private void rebuild() {
		entries = new IntEntry[Math.min(MAXIMUM_CAPACITY,roundUpToPowerOf2(entries.length+1))];
		
		EntryIter iter = getIter();
		
		while(iter.hasNext()) {
			IntEntry next = iter.next();
			entries[find(next.key)] = next;
		}
	}

	public int find(int key) {
		int index = index(key);
		int num = 0;
		while(entries[index] != null && entries[index].key != key) {
			int incr = quadratic ? num+1 : 1;
			index = (index+incr)&(entries.length-1);
			num++;
		}
//		if(num > 10) {
//			System.out.println(num);
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
	
	public void clear() {
		size = 0;
		Arrays.fill(entries,null);
		head.next = tail;
		tail.prev = head;
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
