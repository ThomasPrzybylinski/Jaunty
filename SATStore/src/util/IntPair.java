package util;

import group.LiteralPermutation;

public class IntPair implements Comparable<IntPair>{
	private int i1;
	private int i2;
	
	public IntPair(int i1, int i2) {
		super();
		this.i1 = i1;
		this.i2 = i2;
	}

	public int getI1() {
		return i1;
	}

	public void setI1(int i1) {
		this.i1 = i1;
	}

	public int getI2() {
		return i2;
	}

	public void setI2(int i2) {
		this.i2 = i2;
	}
	
	public IntPair apply(LiteralPermutation perm) {
		return new IntPair(perm.imageOf(i1),perm.imageOf(i2));
	}
	
	public IntPair apply(LiteralPermutation perm, int index) {
		int n1;
		if(index == 0) {
			n1 = perm.imageOf(i1);
		} else {
			n1 = i1;
		}
		
		int n2;
		if(index == 1) {
			n2 = perm.imageOf(i2);
		} else {
			n2 = i2;
		}
		return new IntPair(n1,n2);
	}
	
	public IntPair applySort(LiteralPermutation perm) {
		int n1 = perm.imageOf(i1);
		int n2 = perm.imageOf(i2);
		return new IntPair(n1 < n2 ? n1 : n2,n1 < n2 ? n2 : n1);
	}
	
	public IntPair applySort(LiteralPermutation perm, int index) {
		int n1;
		if(index == 0) {
			n1 = perm.imageOf(i1);
		} else {
			n1 = i1;
		}
		
		int n2;
		if(index == 1) {
			n2 = perm.imageOf(i2);
		} else {
			n2 = i2;
		}
		return new IntPair(n1 < n2 ? n1 : n2,n1 < n2 ? n2 : n1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i1;
		result = prime * result + i2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IntPair)) {
			return false;
		}
		IntPair other = (IntPair) obj;
		if (i1 != other.i1) {
			return false;
		}
		if (i2 != other.i2) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(IntPair o) {
		int comp1 = i1-o.i1;
		
		if(comp1 != 0) {
			return comp1;
		}
		
		return i2 - o.i2;
	}

	@Override
	public String toString() {
		return "(" + i1 + "," + i2 + ")";
	}
	
	
	
	
	
}
