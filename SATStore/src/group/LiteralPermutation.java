package group;

import java.util.Arrays;

import util.PermutationUtil;


//A representation of a permutation on literals.
//Since if  a -> b, then -a -> -b, we only store the mappings
//to positive literals.
public class LiteralPermutation implements Comparable<LiteralPermutation>{
	private int[] perm;
	
	//Creates an identity permutation
	public LiteralPermutation(int size) {
		perm = new int[size+1];
		for(int k = 1; k < perm.length;k++) {
			perm[k] = k;
		}
	}
	
	
	//Permutations work on numbers starting at 1
	// initial[k] = j means that the permutation maps k to j
	// The 0 index is ignored
	public LiteralPermutation(int... initial) {
		this.perm = new int[initial.length];
		
		boolean[] seen = new boolean[initial.length];
		seen[0] = true;
		perm[0] = 0;
		
		for(int k = 1; k < initial.length; k++) {
			int lit = initial[k];
			int var = Math.abs(lit);
			if(var == 0 || var >= initial.length || seen[var] == true) {
				throw new InvalidPermutationException();
			} else {
				perm[k] = lit;
				seen[var] = true;
			}
		}
	}
	
	//The number of literals this permutation permutes
	//Is also the value of the largest literal
	public int size() {
		return perm.length-1;
	}
	
	public final int imageOf(int k) {
		int abs = Math.abs(k);
		int sign = k/abs;
		return sign*perm[abs];
	}
	
	public int[] apply(int[] array) {
		return PermutationUtil.permute(array,perm);
	}
	
	public int[] applySort(int[] array) {
		return PermutationUtil.permuteClause(array,perm);
	}
	
	public int[] asArray() {
		return perm.clone();
	}
	
	//returns this composed with perm
	public LiteralPermutation compose(LiteralPermutation perm) {
		//This exceptions may be too broad, but making it more targeted would be too much trouble
		if(this.perm.length != perm.perm.length) throw new InvalidPermutationException();
		return new LiteralPermutation(PermutationUtil.permute(this.perm,perm.perm));
	}
	
	public LiteralPermutation inverse() {
		return new LiteralPermutation(PermutationUtil.getInverse(this.perm));
	}
	
	public LiteralPermutation iso(LiteralPermutation litPerm) {
		int[] newPerm = new int[perm.length];
		
		for(int k = 0; k < perm.length; k++) {
			int newSource = litPerm.imageOf(k);
			int newDest = litPerm.imageOf(perm[k]); 
			
			if(newSource < 0) {
				newSource = -newSource;
				newDest = -newDest;
			}
			
			newPerm[newSource] = newDest;
		}
		
		return new LiteralPermutation(newPerm);
	}
	
	
	public boolean isId() {
		for(int k = 1; k < perm.length; k++) {
			if(perm[k] != k) return false;
		}
		
		return true;
	}
	
	//Returns first var that does not map to itself, otherwise 0
	public int getFirstUnstableVar() {
		for(int k = 1; k < perm.length; k++) {
			if(perm[k] != k) return k;
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		return PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(this.perm),false);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(perm);
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
		if (!(obj instanceof LiteralPermutation)) {
			return false;
		}
		LiteralPermutation other = (LiteralPermutation) obj;
		if (compareArrays(perm, other.perm) != 0) {
			return false;
		}
		return true;
	}


	@Override
	public int compareTo(LiteralPermutation o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return 1;
		}
		
		return compareArrays(this.perm,o.perm);
	}
	
	private int compareArrays(int[] perm1, int[] perm2) {
		if(perm1.length > perm2.length) {
			return 1;
		} else if(perm1.length < perm2.length) {
			return -1;
		}

		for(int k = 1; k < perm1.length; k++) {
			if(perm1[k] > perm2[k]) {
				return 1;
			} else if(perm1[k] < perm2[k]) {
				return -1;
			}
		}
		return 0;
	}
	
	
}
