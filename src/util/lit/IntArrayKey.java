package util.lit;

import java.util.Arrays;

public class IntArrayKey implements Comparable<int[]> {
	private int[] array;
	
	public IntArrayKey(int[] array) {
		this.array=array;
	}
	
	@Override
	public int compareTo(int[] o) {
		return Arrays.compare(this.array,o);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IntArrayKey) {
			obj = ((IntArrayKey)obj).array;
		}
		if(obj instanceof int[]) {
			return Arrays.equals(this.array,(int[])obj);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(array);
	}

}
