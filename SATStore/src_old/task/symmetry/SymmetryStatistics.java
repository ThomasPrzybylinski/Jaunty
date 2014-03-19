package task.symmetry;

import java.util.ArrayList;
import java.util.List;

import util.LitUtil;
import formula.simple.ClauseList;

public class SymmetryStatistics {
	//Given two literals, how frequently are they in the same clause?
	//private Map<Integer,Map<Integer,Integer>> clauseFreqs = new HashMap<Integer,Map<Integer,Integer>>();
	private int[][] clauseFreqs;
	//private VariableContext context;
	private int numVars;
	
	//Holds statistics necessary for the SimpleSymFinder to do its job properly
	public SymmetryStatistics(ClauseList toSym) {
		numVars = toSym.getContext().getNumVarsMade();
		
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
				int[] cur = clauseFreqs[getIndex(lit1,numVars)];
				for(int i = 0; i < clause.length; i++) {
					if(i == k) continue;
					int lit2 = clause[i];
					//cur.put(lit2,cur.get(lit2)+1);
					cur[getIndex(lit2,numVars)]++;
				}
			}
		}
	}
	
	public int getIndex(int lit, int numVars) {
		return lit + numVars;
	}
	
	public int[][] getPartFreqs(List<List<Integer>> toRefine) {
		//Map<Integer,int[]> ret = new HashMap<Integer,int[]>(2*numVars+1,1);
		int[][] ret = new int[2*numVars+1][];
		
		//Map<Integer,Integer> litToPart = new HashMap<Integer,Integer>();
		int[] litToPart = new int[numVars*2 +1];
		List<Integer> validLits = new ArrayList<Integer>();
		
		for(int k = 0; k < toRefine.size(); k++) {
			List<Integer> part = toRefine.get(k);
			for(int i = 0; i < part.size(); i++) {
				int lit = part.get(i);
				//litToPart.put(lit,k);
				litToPart[getIndex(lit,numVars)] = k;
				validLits.add(lit);
			}
		}
		
		
//		for(Entry<Integer,Map<Integer,Integer>> entry : clauseFreqs.entrySet()) {
//			int[] toAdd = new int[toRefine.size()];
//			
//			int curLit = entry.getKey();
//			Map<Integer,Integer> freqs = entry.getValue();
//			
//			for(int lit : validLits) {
//				if(curLit == lit) continue;
//				int freq = freqs.get(lit);
//				int part = litToPart.get(lit);
//				
//				toAdd[part] += freq;
//			}
//			
//			ret.put(entry.getKey(),toAdd);
//		}
		
		
		for(int k = 0; k < clauseFreqs.length; k++) {
			int[] toAdd = new int[toRefine.size()];
			int curLit = k-numVars;
			
			int[] freqs = clauseFreqs[k];
			
			for(int lit : validLits) {
				if(curLit == lit) continue;
				int freq = freqs[getIndex(lit,numVars)];
				int part = litToPart[getIndex(lit,numVars)];
				
				toAdd[part] += freq;
			}
			
			//ret.put(curLit,toAdd);
			ret[LitUtil.getIndex(curLit,numVars)] = toAdd;
		}
		
		
		
		
		return ret;
	}

}
