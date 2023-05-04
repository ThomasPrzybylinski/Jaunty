package util;

import java.util.Arrays;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import util.lit.IntToIntLinkedHashMap;

public class ArrayIntersectionHelper {
	
	//Intersect two arrays with regular integer order
	public static int[] intersectOrdered(int[] l1, int[] l2) {

		
		int minSize = Math.min(l1.length,l2.length);
		int maxSize = Math.max(l1.length,l2.length);
		
		if(minSize == 0) return new int[0];
		
		int[] min = l1.length == minSize ? l1 : l2;
		int[] max = l1.length == maxSize ? l1 : l2;
		
		int lastMax = Arrays.binarySearch(max,min[min.length-1]);
		lastMax = lastMax >= 0 ? lastMax : -(lastMax+1);
		int maxMax = Math.min(lastMax+1,max.length);
//		
//		if(maxMax < minSize) {
//			maxSize = minSize;
//			minSize = maxMax;
//			int[] temp = min;
//			min = max;
//			max = temp;
//		}
		
		IntList ret = new ArrayIntList(minSize/2);
		
		boolean doLinear = getUseLinear(minSize, maxSize);
		
//		System.out.println(doLinear);
//		System.out.println(Arrays.toString(min));
//		System.out.println(Arrays.toString(max));
//		System.out.println();
		
		if(doLinear) {
			linearIntersection(l1, l2, ret);
		} else {
			binarySearchIntersection(l1, l2, min, max, maxMax, ret);
		}
		
		return ret.toArray();
	}

	private static void linearIntersection(int[] l1, int[] l2, IntList ret) {
		int l1Ind = 0;
		int l1Val = l1[0];
		int l2Ind = 0;
		int l2Val = l2[0];
		
		while(l1Ind < l1.length && l2Ind < l2.length) {
			l1Val = l1[l1Ind];
			l2Val = l2[l2Ind];
			
			if(l1Val < l2Val) {
				l1Ind++;
			} else if (l2Val < l1Val) {
				l2Ind++;
			} else {
				ret.add(l1Val);
				l1Ind++;
				l2Ind++;
			}
		}
	}
	
	private static void binarySearchIntersection(int[] l1, int[] l2, int[] min,
			int[] max, int maxMax, IntList ret) {
		if(l1.length == l2.length) {
			//Just in case
			min = l1;
			max = l2;
		}
		
		int lastIndex = 0;
		for(int test : min) {
			int res = Arrays.binarySearch(max,lastIndex,maxMax,test);
			
			if(res >= 0) {
				ret.add(test);
				lastIndex = res;
			} else {
				lastIndex = -(res+1);
			}
		}
	}

	
	
	public static int intersectSize(int[] l1, int[] l2) {
		if(l1 == null || l2 == null) return 0;
		
		int ret = 0;
		
		int minSize = Math.min(l1.length,l2.length);
		int maxSize = Math.max(l1.length,l2.length);
		
		boolean doLinear = getUseLinear(minSize,maxSize);
		
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
					ret++;
					
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
					ret++;
				} else {
					lastIndex = -(res+1);
				}
			}
		}
		
		return ret;
	}
	
	public static int[] setDifference(int[] orig, int[] toRem) {
		int minSize = Math.min(orig.length,toRem.length);
		int maxSize = Math.max(orig.length,toRem.length);
		
		if(minSize == 0) {
			return orig; //either nothing to remove, or nothing is removed
		}
		
		int[] min = orig.length == minSize ? orig : toRem;
		int[] max = orig.length == maxSize ? orig : toRem;
		
		int lastMax = Arrays.binarySearch(max,min[min.length-1]);
		lastMax = lastMax >= 0 ? lastMax : -(lastMax+1);
		int maxMax = Math.min(lastMax+1,max.length);
//		
//		if(maxMax < minSize) {
//			maxSize = minSize;
//			minSize = maxMax;
//			int[] temp = min;
//			min = max;
//			max = temp;
//		}
		
		IntList ret = new ArrayIntList(orig.length);
		
		boolean doLinear = getUseLinear(minSize, maxSize);
		
//		System.out.println(doLinear);
//		System.out.println(Arrays.toString(min));
//		System.out.println(Arrays.toString(max));
//		System.out.println();
		
		if(doLinear) {
			linearDifference(orig, toRem, ret);
		} else {
			binaryDifference(orig, toRem, min, maxMax, ret);
		}
		
		return ret.toArray();
	}

	private static boolean getUseLinear(int minSize, int maxSize) {
		boolean doLinear = minSize >= 3*maxSize/((Math.log(maxSize)/Math.log(2))-1); //approx the point where binary search isn't helpful
		return doLinear;
	}

	private static void binaryDifference(int[] orig, int[] toRem, int[] min,
			int maxMax, IntList ret) {
		if(orig.length == min.length) {
			int lastIndex = 0;
			for(int test : orig) {
				int res = Arrays.binarySearch(toRem,lastIndex,maxMax,test);
				
				if(res >= 0) {
					lastIndex = res;
				} else {
					ret.add(test);
					lastIndex = -(res+1);
				}
			}
		} else {
			int lastIndex = 0;
			boolean remove = false;
			for(int test : toRem) {
				int res = Arrays.binarySearch(orig,lastIndex,maxMax,test);
				int startIndex = lastIndex + (remove ? 1 : 0);
				
				if(res >= 0) {
					lastIndex = res;
					remove = true;
				} else {
					lastIndex = -(res+1);
					remove = false;
				}
				
				for(int k = startIndex; k < lastIndex; k++) {
					ret.add(orig[k]);
				}
			}
			int startIndex = lastIndex + (remove ? 1 : 0);
			for(int k = startIndex; k < orig.length; k++) {
				ret.add(orig[k]);
			}
				
		}
	}

	private static void linearDifference(int[] orig, int[] toRem, IntList ret) {
		int l1Ind = 0;
		int l1Val = orig[0];
		int l2Ind = 0;
		int l2Val = toRem[0];
		
		while(l1Ind < orig.length && l2Ind < toRem.length) {
			l1Val = orig[l1Ind];
			l2Val = toRem[l2Ind];
			
			if(l1Val < l2Val) {
				ret.add(l1Val);
				l1Ind++;
			} else if (l2Val < l1Val) {
				l2Ind++;
			} else {
				l1Ind++;
				l2Ind++;
			}
		}
		
		for(int k = l1Ind; k < orig.length; k++) {
			ret.add(orig[k]);
		}
	}

	//removes all in possible that are NOT in complRem
	public static int[] unionOrdered(int[] l1, int[] l2) {
		IntList ret = new ArrayIntList(l1.length+l2.length);
		
		int l1Ind = 0;
		int l1Val = l1[0];
		int l2Ind = 0;
		int l2Val = l2[0];
		
		while(l1Ind < l1.length && l2Ind < l2.length) {
			l1Val = l1[l1Ind];
			l2Val = l2[l2Ind];
			
			if(l1Val < l2Val) {
				ret.add(l1Val);
				l1Ind++;
			} else if (l2Val < l1Val) {
				ret.add(l2Val);
				l2Ind++;
			} else {
				ret.add(l1Val);
				l1Ind++;
				l2Ind++;
			}
		}
		
		return ret.toArray();
	}
}
