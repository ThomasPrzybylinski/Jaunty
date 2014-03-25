package util.lit;

import java.util.Arrays;
import java.util.Comparator;

//Orders clauses
public class MILEComparator implements Comparator<int[]> {

	@Override
	public int compare(int[] a1, int[] a2) {
		if(Arrays.equals(a1,a2)) return 0;
		int size = Math.min(a1.length,a2.length);
		
		//First by variable name
		for(int k = 0; k < size; k++) {
			int diff = Math.abs(a1[k])-Math.abs(a2[k]);
			
			if(diff > 0) {
				return -1;
			} else if(diff < 0) {
				return 1;
			}
		}
		
		//Then distinguish by pos/neg
		for(int k = 0; k < size; k++) {
			int diff = a1[k]-a2[k];
			
			if(diff > 0) {
				return -1;
			} else if(diff < 0) {
				return 1;
			}
		}
		
		//if all else fails, use the length
		int diff = a1.length-a2.length;
		return diff;
	}

}
