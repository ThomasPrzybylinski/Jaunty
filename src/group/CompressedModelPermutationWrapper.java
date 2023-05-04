package group;

public class CompressedModelPermutationWrapper extends
CompressedModelPermutation {
	LiteralPermutation perm;

	protected CompressedModelPermutationWrapper(LiteralPermutation perm) {
		this.perm = perm;
	}

	@Override
	public int size() {
		return perm.size();
	}

	@Override
	public int imageOf(int k) {
		return perm.imageOf(k);
	}

	@Override
	public int[] asArray() {
		return perm.asArray();
	}

	@Override
	public CompressedModelPermutation compose(CompressedModelPermutation perm) {
		if(perm instanceof CompressedModelPermutationWrapper) {
			return CompressedModelPermutation.getPerm(this.perm.compose(((CompressedModelPermutationWrapper)perm).perm));
		}
		else {
			if(this.size() != perm.size() && !(this.isId() || perm.isId())) {
				throw new InvalidPermutationException();
			}
			int[] ret = new int[this.size()+1];

			for(int k = 1; k < ret.length;k++) {
				ret[k] = perm.imageOf(this.imageOf(k));
			}

			return CompressedModelPermutation.getPerm(new LiteralPermutation(ret));
		}

	}
	
	@Override
	public CompressedModelPermutation inverse() {
		return new CompressedModelPermutationWrapper(perm.inverse());
	}

	@Override
	public int getFirstUnstableVar() {
		return perm.getFirstUnstableVar();
	}

	@Override
	public String toString() {
		return perm.toString();
	}
	
	

}
