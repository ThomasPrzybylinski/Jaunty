import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import util.lit.LitsMap;
import util.lit.LitsSet;


public class TempLitMapTests {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numVars = 10;
		Random rand = new Random();
		LitsMap<Integer> testSet = new LitsMap<Integer>(numVars);
		
		List<int[]> comp = randTest(numVars, rand, testSet);//specTest(numVars,rand,testSet);//

		for(int[] i : comp) {
			System.out.println(Arrays.toString(i) + "\t " + testSet.contains(i));
		}
		System.out.println("_--------------");
		for(Entry<int[],Integer> ent : testSet.entrySet()) {
			System.out.println(Arrays.toString(ent.getKey()));
		}
//		for(int[] i : testSet.keySet()) {
//			System.out.println(Arrays.toString(i));
//		}
		
		System.out.println(testSet.contains(new int[]{1,2,3}));
		System.out.println(testSet.contains(new int[]{}));

	}
	
	private static List<int[]> randTest(int numVars, Random rand, LitsMap<Integer> testSet) {
		List<int[]> comp = new ArrayList<int[]>(); 
		
		for(int k = 0; k < 7; k++) {
			int len = rand.nextInt(numVars);
			int[] toAdd = new int[len];
			
			for(int i = 0; i < toAdd.length; i++) {
				int var = rand.nextInt(numVars-1)+1;
				toAdd[i] = rand.nextBoolean() ? var : -var;
			}
			
			testSet.put(toAdd,0);
			//comp.add(toAdd);
			
			if(rand.nextBoolean()) {
				comp.add(toAdd);
			} else {
				testSet.remove(toAdd);
			}
		}
		return comp;
	}


	private static List<int[]> specTest(int numVars, Random rand, LitsSet testSet) {
		int[] a = new int[]{-2};
		int[] b = new int[0];
		int[] c = new int[]{1};
		int[] d = new int[]{2,-3,-3};
		
		List<int[]> comp = new ArrayList<int[]>(); 
		comp.add(a);
		comp.add(b);
		comp.add(c);
		comp.add(d);
		
		testSet.add(a);
		testSet.add(b);
		testSet.add(c);
		testSet.add(d);
		
		return comp;
		
	}

	
	
}
