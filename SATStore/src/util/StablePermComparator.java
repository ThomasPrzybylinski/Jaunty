package util;

import group.LiteralPermutation;

import java.util.Comparator;

public class StablePermComparator implements Comparator<LiteralPermutation> {

	public StablePermComparator() {
	}

	@Override
	public int compare(LiteralPermutation arg0, LiteralPermutation arg1) {
		return arg0.getFirstUnstableVar() - arg1.getFirstUnstableVar();
	}

}
