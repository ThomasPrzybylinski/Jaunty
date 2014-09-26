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
		
//		//Put negatives in positive order
//		for(int k = 0; k < (posPoint)/2; k++) {
//			swap(vars,k,(posPoint)-(k+1));
//		}
//		
//		int otherStart = posPoint;
//		int otherIndex = 0;
//		for(int k = 0; k < vars.length && !(otherStart == posPoint && posPoint == vars.length); k++) {
//			if(otherStart == posPoint) {
//				int curComp = Math.abs(vars[k]);
//				if(curComp > vars[posPoint]) {
//					swap(vars,k,posPoint);
//					posPoint++;
//				}
//			} else if(posPoint != vars.length) {
//				int curComp = Math.abs(vars[otherStart]);
//				
//				if(curComp > vars[posPoint]) {
//					swap(vars,k,posPoint);
//					posPoint++;
//				} else {
//					swap(vars,k,otherStart+otherIndex);
//					if(otherIndex > 0) {
//						otherStart++;
//						otherIndex--;
//					}
//
//					otherIndex = (otherIndex+1)%(posPoint-otherStart);
//				}
//			} else {
//				swap(vars,k,otherStart+otherIndex);
//				if(otherIndex > 0) {
//					otherStart++;
//					otherIndex--;
//				}
//			}
//		}
		
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
	
	private static void swap(int[] i, int i1, int i2) {
		int temp = i[i1];
		i[i1] = i[i2];
		i[i2] = temp;
	}

}
