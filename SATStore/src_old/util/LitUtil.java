package util;


//Methods for using literals in arrays;
public class LitUtil {

	public static int getIndex(int lit, int numVars) {
		return lit + numVars;
	}
	
	public static int getLit(int index, int numVars) {
		return index - numVars;
	}
}
