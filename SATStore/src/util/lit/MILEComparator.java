package util.lit;

import java.util.Arrays;
import java.util.Comparator;

//Orders clauses
public class MILEComparator implements Comparator<int[]> {

	@Override
	public int compare(int[] a1, int[] a2) {
		if(Arrays.equals(a1,a2)) return 0;
		int size = Math.min(a1.length,a2.length);
		
		for(int k = 0; k < size; k++) {
			int diff = a1[k]-a2[k];
			int absDiff = Math.abs(a1[k])- Math.abs(a2[k]);
			
			if(diff != 0) {
				if(absDiff == 0) {
					return -a1[k];
				} else {
					return absDiff;
				}
			}
		}

		int lenDiff = a1.length-a2.length;
		return lenDiff; //longer part interps are "smaller" 
	}

}
