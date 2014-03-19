package util.lit;

import java.util.Comparator;

public class SetLitCompare  implements Comparator<Integer> {

	public SetLitCompare() {
	}

	//Positives are "less" than negatives
	@Override
	public int compare(Integer o1, Integer o2) {
		int comp = Math.abs(o1) - Math.abs(o2);
		
		if(comp == 0 && (o1-o2) != 0) {
			comp = o2 > o1 ? 1 : -1;
		}
		return comp;
	}
}
