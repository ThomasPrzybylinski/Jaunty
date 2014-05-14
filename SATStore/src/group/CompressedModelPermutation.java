package group;

import util.lit.LitSorter;

public abstract class CompressedModelPermutation {

	private static final double cutoff = 0;//.33;

	public static CompressedModelPermutation ID = new CompressedModelPermutationImpl();

	public static CompressedModelPermutation getPerm(LiteralPermutation perm) {
		int numUnstable = 0;
		for(int k = 1; k <= perm.size(); k++) {
			if(perm.imageOf(k) != k) {
				numUnstable++;
			}
		}

		if(numUnstable == 0) {
			return ID;
		}
		else if((numUnstable/(double)perm.size()) < cutoff) {
			return new CompressedModelPermutationImpl(perm,numUnstable);
		} 
		else {
			return new CompressedModelPermutationWrapper(perm);
		}
	}
	
	protected static CompressedModelPermutation getPerm(int[] perms, int[] translation, int size) {
		if(perms.length <= 2) return ID;
		
		double comp = perms.length/(double)size;
		
		if(comp < 1.5*cutoff) {
			return new CompressedModelPermutationImpl(perms,translation,size);
		} else {
			int[] perm = new int[size+1];
			int transIndex = 1;
			for(int k = 1; k < perm.length; k++) {
				if(transIndex < translation.length && translation[transIndex] == k) {
					perm[k] = translation[perms[transIndex]];
					transIndex++;
				} else {
					perm[k] = k;
				}
			}
			
			return new CompressedModelPermutationWrapper(new LiteralPermutation(perm));
		}
	}

	protected CompressedModelPermutation() {}

	//The number of literals this permutation permutes
	//Is also the value of the largest literal
	public abstract int size();

	public abstract int imageOf(int k);

	public int[] apply(int[] input) {
		int[] ret = new int[input.length];

		for(int k = 0; k < input.length; k++) {
			ret[k] = this.imageOf(input[k]);
		}
		return ret;
	}

	public int[] applySort(int[] input) {
		int[] ret = apply(input);
		LitSorter.inPlaceSort(input);
		return ret;
	}

	public abstract int[] asArray();

	//returns this composed with perm
	public abstract CompressedModelPermutation compose(CompressedModelPermutation perm);

	public boolean isId() {
		return this == ID;
	}
	
	public abstract CompressedModelPermutation inverse();

	//Returns first var that does not map to itself, otherwise 0
	public abstract int getFirstUnstableVar();

}
