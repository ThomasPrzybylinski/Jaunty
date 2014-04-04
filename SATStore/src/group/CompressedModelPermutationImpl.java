package group;

import java.util.Arrays;

import util.PermutationUtil;
import util.lit.LitSorter;

public class CompressedModelPermutationImpl extends CompressedModelPermutation {
	private int[] translation;
	private int[] perm;
	private int size;
	
	//Creates an identity permutation
	protected CompressedModelPermutationImpl() {
		perm = new int[0];
		translation = new int[0];
		size = 1;
	}
	
	//Permutations work on numbers starting at 1
	// initial[k] = j means that the permutation maps k to j
	// The 0 index is ignored
	protected CompressedModelPermutationImpl(LiteralPermutation perm, int numUnstable) {
		this.perm = new int[numUnstable+1];
		this.perm[0] = -1;
		this.translation = new int[numUnstable+1];
		this.translation[0] = -1;
		this.size = perm.size(); 

		int transIndex = 1;

		for(int k = 1; k <= perm.size(); k++) {
			int image = perm.imageOf(k);

			if(image != k) {
				translation[transIndex] = k;
				this.perm[transIndex] = image;
				transIndex++;
			}
		}

		for(int k = 0; k < this.perm.length; k++) {
			int index = Arrays.binarySearch(translation,this.perm[k]);
			this.perm[k] = index;
		}
	}
	
	protected CompressedModelPermutationImpl(int[] perms, int[] translation, int size) {
		this.perm = perms;
		this.translation = translation;
		this.size = size;
	}
	
	//The number of literals this permutation permutes
	//Is also the value of the largest literal
	public int size() {
		return size;
	}

	public final int imageOf(int k) {
		int index = Arrays.binarySearch(translation,k);
		if(index > 0) {
			return translation[perm[index]];
		} else {
			return k;
		}
	}


	public int[] asArray() {
		return perm.clone();
	}

	//returns this composed with perm
	public CompressedModelPermutation compose(CompressedModelPermutation permAbst) {
		//This exceptions may be too broad, but making it more targeted would be too much trouble
		if(this.size() != permAbst.size() && !(this.isId() || permAbst.isId())) {
			throw new InvalidPermutationException();
		}
		int trueSize = Math.max(this.size(),permAbst.size());
		
		if(permAbst instanceof CompressedModelPermutationWrapper) {
			int[] ret = new int[trueSize+1];
			
			for(int k = 1; k < ret.length;k++) {
				ret[k] = permAbst.imageOf(this.imageOf(k));
			}
			
			return CompressedModelPermutation.getPerm(new LiteralPermutation(ret));
//			CompressedModelPermutation perm1 = CompressedModelPermutation.getPerm(new LiteralPermutation(ret));
		}
		
		//Otherwise
		
		CompressedModelPermutationImpl perm = (CompressedModelPermutationImpl)permAbst;
		
		int[] trans = new int[this.translation.length+perm.translation.length+1];
		int[] newPerm = new int[this.translation.length+perm.translation.length+1];
		int t1 = 0;
		int t2 = 0;
		int t = 1;
		
		while(t1 < this.translation.length || t2 <  perm.translation.length) {
			if(t1 == translation.length) {
				trans[t] = perm.translation[t2];
				t2++;
			} else if(t2 == perm.translation.length) {
				trans[t] = this.translation[t1];
				t1++;
			} else {
				int diff = this.translation[t1]-perm.translation[t2];
				if(diff == 0) {
					trans[t] = this.translation[t1];
					t1++;
					t2++;
				} else if(diff < 0) {
					trans[t] = this.translation[t1];
					t1++;
				} else {
					trans[t] = perm.translation[t2];
					t2++;
				}
			}
			
			int image = perm.imageOf(this.imageOf(trans[t])) ; 
			if(image != trans[t]) {
				newPerm[t] = image; 
				t++;
			}
		}
		
		int[] realTrans = new int[t];
		System.arraycopy(trans,0,realTrans,0,realTrans.length);
		int[] realPerm = new int[t];
		System.arraycopy(newPerm,0,realPerm,0,realPerm.length);
		
		for(int k = 1; k < realPerm.length; k++) {
			realPerm[k] = Arrays.binarySearch(realTrans,realPerm[k]);
		}
		
		if(realTrans.length <= 1) {
			return ID;
		}
//		return CompressedModelPermutation.getPerm(realPerm,realTrans,trueSize);
		CompressedModelPermutation perm2 = CompressedModelPermutation.getPerm(realPerm,realTrans,trueSize);
		
//		for(int k = 1; k <= trueSize; k++) {
//			if(perm1.imageOf(k) != perm2.imageOf(k)) {
//				System.out.println("AH!");
//				perm2 = CompressedModelPermutation.getPerm(realPerm,realTrans,trueSize);
//			}
//		}
		
		return perm2;
		
	}

	public CompressedModelPermutation inverse() {
		if(this.isId()) {
			return ID;
		}
		return new CompressedModelPermutationImpl(PermutationUtil.getInverse(this.perm),translation,this.size);
	}

	//Returns first var that does not map to itself, otherwise 0
	public int getFirstUnstableVar() {
		if(this.isId()) {
			return 0;
		} else {
			return translation[1];
		}
	}

	@Override
	public String toString() {
		int[][] cycles = PermutationUtil.getCycleRepresentation(this.perm);
		
		int[][] trueCycles = new int[cycles.length][];
		
		for(int k = 0; k < cycles.length; k++) {
			trueCycles[k] = new int[cycles[k].length];
			for(int i = 0; i < cycles[k].length; i++) {
				trueCycles[k][i] = translation[cycles[k][i]];
			}
		}
		
		return PermutationUtil.getPrettyCycles(trueCycles,false);
	}

}
