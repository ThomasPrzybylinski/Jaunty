package util.formula;

import java.util.Comparator;

import util.lit.MILEComparator;

public class BySizeComp implements Comparator<int[]> {
	MILEComparator comp = new MILEComparator();
	
	@Override
	public int compare(int[] o1, int[] o2) {
		if(o1.length != o2.length) {
			return o1.length-o2.length;
		} else {
			return comp.compare(o1,o2);
		}
	}

	

}
