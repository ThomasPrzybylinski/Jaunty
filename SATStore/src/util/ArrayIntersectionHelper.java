package util;

import java.util.Arrays;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class ArrayIntersectionHelper {
	
	//Intersect two arrays with regular integer order
	public static int[] intersectOrdered(int[] l1, int[] l2) {
		int minSize = Math.min(l1.length,l2.length);
		int maxSize = Math.max(l1.length,l2.length);
		
		IntList ret = new ArrayIntList(minSize/2);
		
		boolean doLinear = minSize >= maxSize/((Math.log(maxSize)/Math.log(2))-1); //approx the point where binary search isn't helpful
		
		if(doLinear) {
			int l1Ind = 0;
			int l1Val = l1[0];
			int l2Ind = 0;
			int l2Val = l2[0];
			
			while(l1Ind < l1.length && l2Ind < l2.length) {
				
				if(l1Val < l2Val) {
					l1Ind++;
					if(l1Ind < l1.length) {
						l1Val = l1[l1Ind];
					}
				} else if (l2Val < l1Val) {
					l2Ind++;
					if(l2Ind < l2.length) {
						l2Val = l2[l2Ind];
					}
				} else {
					ret.add(l1Val);
					
					l1Ind++;
					if(l1Ind < l1.length) {
						l1Val = l1[l1Ind];
					}
					
					l2Ind++;
					if(l2Ind < l2.length) {
						l2Val = l2[l2Ind];
					}
				}
			}
		} else {
			int[] min = l1.length == minSize ? l1 : l2;
			int[] max = l1.length == maxSize ? l1 : l2;
			
			if(l1.length == l2.length) {
				//Just in case
				min = l1;
				max = l2;
			}
			
			int lastIndex = 0;
			for(int test : min) {
				int res = Arrays.binarySearch(max,lastIndex,max.length,test);
				
				if(res >= 0) {
					ret.add(test);
				} else {
					lastIndex = -(res+1);
				}
			}
		}
		
		return ret.toArray();
	}
}
