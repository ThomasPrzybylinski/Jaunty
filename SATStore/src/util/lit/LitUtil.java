package util.lit;


//Methods for using literals in arrays;
public class LitUtil {

	public static int getIndex(int lit, int numVars) {
		return lit + numVars;
	}

	public static int getLit(int index, int numVars) {
		return index - numVars;
	}

	//Assumes ordered
	public static boolean isSubset(int[] s1, int[] s2) {
		int s2Ind = 0;

		for(int k = 0; k < s1.length; k++) {
			int testLit = s1[k];
			int testVar = Math.abs(testLit);
			for(; s2Ind < s2.length; s2Ind++) {
				int compareLit = s2[s2Ind];
				int compareVar = Math.abs(compareLit);

				if(testVar < compareVar) {
					return false;
				} else if(testVar == compareVar) {
					if(testLit == compareLit) {
						break;
					} else {
						return false;
					}
				}

				if(s2Ind == s2.length - 1) return false; //Did not find the kth literal
			}
		}

		return true;
	}
}
