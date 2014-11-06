package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

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
	private int[][][] litClauses; //litInd to list of pairs (lit, freq)

	//Holds statistics necessary for the SimpleSymFinder to do its job properly
	@SuppressWarnings("unchecked")
	public SparseSymmetryStatistics(PermCheckingClauseList toSym) {
		numVars = toSym.getContext().size();
		varToPart = new int[2*numVars+1];
		varToIndex = new int[2*numVars+1];
		this.cl = toSym.getClauses();

		Map<Integer,Integer>[] tempLitClauses = new Map[2*numVars+1];

		for(int[] i : cl) {

			for(int lit : i) {
				int index = LitUtil.getIndex(lit,numVars);
				if(tempLitClauses[index] == null) {
					tempLitClauses[index] = new LinkedHashMap<Integer, Integer>();
				}
			}

			for(int k = 0; k < i.length; k++) {
				int index = LitUtil.getIndex(i[k],numVars);

				for(int j = 0; j < i.length; j++) {
					if(k == j) continue;
					if(tempLitClauses[index].containsKey(i[j])) {
						tempLitClauses[index].put(i[j],tempLitClauses[index].get(i[j])+1);
					} else {
						tempLitClauses[index].put(i[j],1);
					}
				}
			}
		}

		litClauses = new int[2*numVars+1][][];
		for(int k = 0; k < litClauses.length; k++) {
			if(tempLitClauses[k] == null) continue;

			litClauses[k] = new int[tempLitClauses[k].size()][2];

			int index = 0;
			for(Entry<Integer,Integer> other : tempLitClauses[k].entrySet()) {
				litClauses[k][index][0] = other.getKey();
				litClauses[k][index][1] = other.getValue();
				index++;
			}
		}

	}


	//Returns freqs based on indecies
	private int[] varToPart;
	private int[] varToIndex;
	public int[][] getPartFreqs(List<IntList> toRefine) {
		int total  = 0;

		for(IntList l: toRefine) {
			total += l.size();
		}


		Arrays.fill(varToIndex,-1);
		Arrays.fill(varToPart,-1);
		int[] lits = new int[total];

		int index = 0;
		int part = 0;
		for(IntList l: toRefine) {
			for(int k = 0; k < l.size(); k++) {
				Integer i = l.get(k);
				varToIndex[LitUtil.getIndex(i,numVars)] = index;
				varToPart[LitUtil.getIndex(i,numVars)] = part;
				lits[index] = i;
				index++;
			}
			part++;
		}

		int[][] ret = new int[total][toRefine.size()];

		index = 0;

		for(int toFill : lits) {
			int toFillLitIndex = LitUtil.getIndex(toFill,numVars);

			int[][] freqs = litClauses[toFillLitIndex];

			for(int[] other : freqs) {
				int resInd = LitUtil.getIndex(other[0],numVars);

				if(varToPart[resInd] != -1) {
					ret[index][varToPart[resInd]]+=other[1];
				}
			}

			toFillLitIndex = LitUtil.getIndex(-toFill,numVars);

			freqs = litClauses[toFillLitIndex];

			for(int[] other : freqs) {
				int resInd = LitUtil.getIndex(other[0],numVars);

				if(varToPart[resInd] != -1) {
					ret[index][varToPart[resInd]]+=other[1];
				}
			}

			index++;
		}

		return ret;
	}

	//Returns pos and neg freqs
	public int[][][] getPartFreqs(IntList toRefine, List<IntList> toUse) {
		int[][][] ret = new int[2][][];

		int total  = toRefine.size();

		Arrays.fill(varToPart,-1);

		int index = 0;
		int part = 0;

		for(int j = 0; j < toUse.size(); j++) {
			IntList l = toUse.get(j);
			for(int k = 0; k < l.size(); k++) {
				int i = l.get(k);
				varToPart[LitUtil.getIndex(i,numVars)] = part;
				index++;
			}
			part++;
		}

		ret[0] = new int[total][toUse.size()];
		ret[1] = new int[total][toUse.size()];

		index = 0;

		for(int k = 0; k < toRefine.size(); k++) {
			int toFill = toRefine.get(k);

			int toFillLitIndex = LitUtil.getIndex(toFill,numVars);

			int[][] freqs = litClauses[toFillLitIndex];

			if(freqs != null) { //Can occur if toFill not in formula
				for(int[] other : freqs) {
					int resInd = LitUtil.getIndex(other[0],numVars);

					if(varToPart[resInd] != -1) {
						ret[0][index][varToPart[resInd]]+=other[1];
					}
				}
			}

			toFillLitIndex = LitUtil.getIndex(-toFill,numVars);

			freqs = litClauses[toFillLitIndex];

			if(freqs != null) { //Can occur if -toFill not in formula
				for(int[] other : freqs) {
					int resInd = LitUtil.getIndex(other[0],numVars);

					if(varToPart[resInd] != -1) {
						ret[1][index][varToPart[resInd]]+=other[1];
					}
				}
			}
			index++;
		}

		return ret;
	}

}
