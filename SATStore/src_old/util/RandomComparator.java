package util;

import java.util.Comparator;
import java.util.Random;

public class RandomComparator<T> implements Comparator<T> {
	Random rand;
	
	public RandomComparator() {
		rand = new Random();
	}
	
	public RandomComparator(long seed) {
		rand = new Random(seed);
	}
	
	@Override
	public int compare(T o1, T o2) {
		if(o1 == o2) {
			return 0;
		} else {
			return rand.nextBoolean() ? -1 : 1;
		}
	}

}
