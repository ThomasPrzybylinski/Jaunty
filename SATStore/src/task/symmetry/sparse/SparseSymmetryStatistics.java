package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;

import task.symmetry.SemiPermutableClauseList;
import util.lit.LitUtil;
import formula.simple.ClauseList;

public class SparseSymmetryStatistics {
	//Given two literals, how frequently are they in the same clause?
	//private Map<Integer,Map<Integer,Integer>> clauseFreqs = new HashMap<Integer,Map<Integer,Integer>>();
//	private int[][] clauseFreqs;
	private List<int[]> cl;
	//private VariableContext context;
	private final int numVars;
	
	//Holds statistics necessary for the SimpleSymFinder to do its job properly
	public SparseSymmetryStatistics(PermCheckingClauseList toSym) {
		numVars = toSym.getContext().size();
		varToPart = new int[2*numVars+1];
		this.cl = toSym.getClauses();
	}
	
	
	private int[] varToPart;
	public int[][] getPartFreqs(List<List<Integer>> toRefine) {
		
		Arrays.fill(varToPart,-1); 
		for(int k = 0; k < toRefine.size(); k++) {
			List<Integer> cl = toRefine.get(k);
			for(int j = 0; j < cl.size(); j++) {
				int i = cl.get(j);
				varToPart[LitUtil.getIndex(i,numVars)] = k;
			}
		}
		int[][] ret = new int[2*numVars+1][toRefine.size()];
		
		for(int l = 0; l < cl.size(); l++) {
			int[] i = cl.get(l);
			for(int k = 0; k < i.length; k++) {
				int kLit = i[k];
				int kIndex = LitUtil.getIndex(kLit,numVars);
				if(varToPart[kIndex] == -1) continue;
				for(int j = k+1; j < i.length; j++) {
					int jLit = i[j];
					int jIndex = LitUtil.getIndex(jLit,numVars);
					if(varToPart[jIndex] == -1) continue;
					ret[kIndex][varToPart[jIndex]]++;
					ret[jIndex][varToPart[kIndex]]++;
				}
			}
		}
		
		return ret;
	}

}
