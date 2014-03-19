package util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * 
 * @author Thomas Przybylinski
 * 
 * Given a parition of subset sizes m1,m2,m3,...mn, iterate through all possible ways to
 * pick one from each subset by giving an array that says to pick index i1,i2,i3,..,in from the corresponding partitions
 *
 * This will go on infinitely if all indecies are 1
 */
public class PartitionIterator implements Iterator<int[]>{
	private int[] cur;
	private int[] sizes;
	private boolean hasNext = true;
	
	public PartitionIterator(int... sizes) {
		cur = new int[sizes.length];
		this.sizes = new int[sizes.length];
		System.arraycopy(sizes,0,this.sizes,0,sizes.length);
	}
	
	public PartitionIterator(int parts, int partSizes) {
		cur = new int[parts];
		this.sizes = new int[parts];

		Arrays.fill(this.sizes,partSizes);
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public int[] next() {
		int[] ret = cur;
		cur = new int[ret.length];
		
		boolean overFlow = true;
		hasNext = false;
		for(int k = 0; k < ret.length; k++) {
			if(ret[k] == 0 && sizes[k] > 1) hasNext = true;
			if(overFlow) {
				overFlow = false;
				cur[k] = ret[k]+1;
				
				if(cur[k] >= sizes[k]) {
					overFlow = true;
					cur[k] = 0;
				}
			} else {
				cur[k] = ret[k];
			}
		}
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
