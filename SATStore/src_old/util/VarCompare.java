package util;

import formula.Variable;

public class VarCompare implements Comparable<VarCompare> {

	private Variable v;
	private int num;
	
	public VarCompare(Variable v) {
		this.v = v;
		
	}
	
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Variable getV() {
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		VarCompare other = (VarCompare) obj;
		if (v == null) {
			if (other.v != null) {
				return false;
			}
		} else if (!v.equals(other.v)) {
			return false;
		}
		
		if(this.num != other.num) return false;
		return true;
	}

	@Override
	public int compareTo(VarCompare o) {
		int diff = this.num - o.num;
		
		if(diff == 0) {
			return this.v.compareTo(o.v);
		} else {
			return diff;
		}
	}

}
