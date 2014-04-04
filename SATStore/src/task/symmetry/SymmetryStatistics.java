package task.symmetry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;

import util.lit.LitUtil;
import formula.simple.ClauseList;

public class SymmetryStatistics {
	//Given two literals, how frequently are they in the same clause?
	//private Map<Integer,Map<Integer,Integer>> clauseFreqs = new HashMap<Integer,Map<Integer,Integer>>();
	private int[][] clauseFreqs;
	//private VariableContext context;
	private final int numVars;
	
	//Holds statistics necessary for the SimpleSymFinder to do its job properly
	public SymmetryStatistics(ClauseList toSym) {
		numVars = toSym.getContext().size();
		
		clauseFreqs = new int[numVars*2+1][numVars*2+1];
		
//		for(int k = -numVars; k <= numVars; k++) {
//			if(k == 0) continue;
//			int[] start = new int[numVars*2 +1];
//			HashMap<Integer,Integer> start = new HashMap<Integer,Integer>();
//			for(int i = -numVars; i <= numVars; i++) {
//				if(i == 0 || i == k) continue;
//				start.put(i,0);
//			}
//			
//			clauseFreqs.put(k,start);
//			
//		}
		
		for(int[] clause : toSym.getClauses()) {
			for(int k = 0; k < clause.length; k++) {
				int lit1 = clause[k];
				//Map<Integer,Integer> cur = clauseFreqs.get(lit1);
				int[] cur = clauseFreqs[getIndex(lit1)];
				for(int i = 0; i < clause.length; i++) {
//					if(i == k) continue;
					int lit2 = clause[i];
					//cur.put(lit2,cur.get(lit2)+1);
					cur[getIndex(lit2)]++;
				}
			}
		}
	}
	
	private final int getIndex(int lit) {
		return lit + numVars;
	}

	public int[][] getPartFreqs(List<List<Integer>> toRefine) {
//		System.out.println(toRefine.size());
//		for(List<Integer> l : toRefine) {
//			System.out.print(l.size() + " ");
//		}
//		System.out.println();
//		System.out.println();
		//Map<Integer,int[]> ret = new HashMap<Integer,int[]>(2*numVars+1,1);
		
		
		//Map<Integer,Integer> litToPart = new HashMap<Integer,Integer>();
		int[] litToPart = new int[numVars*2 +1];
		List<Integer> validLits = new ArrayList<Integer>(2*numVars+1);
		
		for(int k = 0; k < toRefine.size(); k++) {
			List<Integer> part = toRefine.get(k);
			for(int i = 0; i < part.size(); i++) {
//				iters++;
				Integer lit = part.get(i);
				//litToPart.put(lit,k);
				litToPart[getIndex(lit)] = k;
				validLits.add(lit);
			}
		}
		
		
		//This function will be called very often, so minimizing
		//boxing/unboxing is worthwhile.
		final int size = validLits.size();
		int[] valLits = new int[size];
		
		for(int k = 0; k < size; k++) {
			valLits[k] = LitUtil.getIndex(validLits.get(k),numVars);
		}
		
		int[][] ret = new int[2*numVars+1][toRefine.size()];
		
		for(int k = 0; k < clauseFreqs.length; k++) {
			int curLit = k-numVars;
			int[] toMod = ret[LitUtil.getIndex(curLit,numVars)];
			
			int[] freqs = clauseFreqs[k];
			
			for(int lIndex : valLits) {
//				iters++;
//				if(curLit == lit) continue;
				int freq = freqs[lIndex];
				int part = litToPart[lIndex];
				
				toMod[part] += freq;
			}
			
			//ret.put(curLit,toAdd);

		}
		
		
//		System.out.println(iters);
		
		return ret;
	}

}
