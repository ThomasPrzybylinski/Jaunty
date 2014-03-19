package util.lit;

import java.util.Arrays;

public class LitSorter {
	
	public static int[] newSortedClause(int[] lits) {
		int[] ret = lits.clone();
		inPlaceSort(ret);
		return ret;
	}
	
	public static void inPlaceSort(int[] vars) {
		Arrays.sort(vars);
		
		int posPoint = vars.length; //first index that is positive
		
		for(int k = 0; k < vars.length; k++) {
			if(vars[k] > 0) {
				posPoint = k;
				break;
			}
		}
		
		int[] temp = new int[vars.length];
		//k= index of neg part. i = index of pos part
		for(int count = 0, k = posPoint-1, i = posPoint; count < vars.length; count++) {
			if(k < 0) {
				temp[count] = vars[i];
				i++;
			} else if(i == vars.length) {
				temp[count] = vars[k];
				k--;
			} else if(Math.abs(vars[k]) < vars[i]) {
				temp[count] = vars[k];
				k--;
			} else {
				temp[count] = vars[i];
				i++;
			}
		}
		
		System.arraycopy(temp,0,vars,0,vars.length);
	
	}

}
