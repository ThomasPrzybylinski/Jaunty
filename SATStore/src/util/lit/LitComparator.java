package util.lit;

import java.util.Comparator;

public class LitComparator implements Comparator<Integer> {

	public LitComparator() {
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		int comp = Math.abs(o1) - Math.abs(o2);
		
		if(comp == 0 && (o1-o2) != 0) {
			comp = o1 > o2 ? 1 : -1;
		}
		return comp;
	}
}
