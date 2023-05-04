package util.lit;

//Extremely simple open-addressed linked hash map for Sparse Symmetry
public class IntHashMap<T> {
	int[] keys;
	T[] values;
	boolean[] exists;
	
	public class ValueIter {
		private int curIndex;
		private int curKey = 0;
		private IntHashMap<T> map;
		
		public ValueIter(IntHashMap<T> map) {
			curIndex = 0;
			this.map = map;
			
			findNext();
		}
		
		public boolean hasNext() {
			return curIndex < exists.length && exists[curIndex];
		}
		
		public T next() {
			if(curIndex >= exists.length) return null;

			T val = values[curIndex];
			curKey = keys[curIndex];
			curIndex++;
			
			findNext();
			
			return val;
		}
		
		public int curKey() {
			return curKey;
		}

		private void findNext() {
			while(curIndex < exists.length && !exists[curIndex]) {
				curIndex++;
			}
		}
	}
	
	private int size;

	private float factor = 1.5f;
	
	static final int MAXIMUM_CAPACITY = 1 << 30;
	
	private static long numFinds = 0;
	private static long totalFindLen = 0;
	
	@SuppressWarnings("unchecked")
	public IntHashMap(int size) {
		int realSize = roundUpToPowerOf2((int)(factor*size+1));
		values = (T[])(new Object[realSize]);
		keys = new int[realSize];
		exists = new boolean[realSize];
	}
	
	public IntHashMap() {
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
			values[index] = value;
			return false;
		}
	}
	
	public T get(int key) {
		int index = find(key);
		return exists(index) ? values[index] : null;
	}

	private void add(int key, T value, int index) {
		size++;
		
		if((size)*factor >= values.length) {
			rebuild();
			index = find(key);
		}
		
		keys[index] = key;
		values[index] = value;
		exists[index] = true;

	}
	
	public boolean contains(int key) {
		int index = find(key);
		
		return exists(index);
	}
	

	public void remove(int i) {
		int index = find(i);
		int hash = hash(i);
		
		if(exists(index)) {
			int k = index+1;
			for(;k < values.length && exists(k) && hash(keys[k]) == hash; k++) {
				keys[k-1] = keys[k];
				values[k-1] = values[k];
				exists[k-1] = true;
				exists[k] = false;
			}
			if(k < values.length) {
				values[k] = null;
			}
			size--;
		}
	}
	
	public int[] getKeys() {
		int size = 0;
		for(int k = 0; k < exists.length; k++) {
			if(exists(k)) size++;
		}
		int[] ret = new int[size];
		
		int index = 0;
		for(int k = 0; k < exists.length; k++) {
			if(exists[k]) {
				ret[index]=keys[k];
				index++;
			}
		}
		
		return ret;
	}
	
	private boolean exists(int index) {
		return exists[index]; 
	}
	
	@SuppressWarnings("unchecked")
	private void rebuild() {
		int[] prevKeys = keys;
		T[] prevValues = values;
		boolean[] prevExists = exists;

		int realSize = Math.min(MAXIMUM_CAPACITY,roundUpToPowerOf2(values.length+1));
		values = (T[])(new Object[realSize]);
		keys = new int[realSize];
		exists = new boolean[realSize];

		
		for(int k = 0; k < prevValues.length; k++) {
			if(prevExists[k]) {
				this.put(prevKeys[k],prevValues[k]);
			}
		}
	}

//	static long num = 0;
//	static long times = 0;
//	static long max = 0;
	private int find(int key) {
		int index = index(key);
//		times++;
		int tempNum = 0;
		while(exists[index] && keys[index] != key) {
			index = (index+1)&(values.length-1);
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
	
	public ValueIter getIter() {
		return new ValueIter(this);
	}
	
	private int hash(int key) {
		return Math.abs((key*1327217909));
	}
	
	private int index(int key) {
		return hash(key)&(values.length-1);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ValueIter iter = getIter();
		
		while(iter.hasNext()) {
			if(sb.length() != 0) {
				sb.append(',');
			}
			sb.append("["+iter.next()+";"+iter.curKey()+"]");
		}
		
		return sb.toString();
	}

	
}
