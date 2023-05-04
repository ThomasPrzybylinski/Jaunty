package task.symmetry.sparse;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import util.lit.IntToIntLinkedHashMap;
import util.lit.IntToIntLinkedHashMap.EntryIter;
import util.lit.IntToIntLinkedHashMap.IntEntry;
import util.lit.LitUtil;

public class SparseSymmetryStatistics {
	//Given two literals, how frequently are they in the same clause?
	//private Map<Integer,Map<Integer,Integer>> clauseFreqs = new HashMap<Integer,Map<Integer,Integer>>();
	//	private int[][] clauseFreqs;
	private List<int[]> cl;
	//private VariableContext context;
	private final int numVars;
	private IntToIntLinkedHashMap[] litClauses; //litInd to list of pairs (lit, freq)
	//Returns freqs based on indecies
	private int[] varToPart;
	private int[] botVarToPart;

	private class LitFreq {
		int lit;
		int freq;

		public LitFreq(int lit, int freq) {
			this.lit = lit;
			this.freq = freq;
		}
	}

	//Holds statistics necessary for the SimpleSymFinder to do its job properly
	@SuppressWarnings("unchecked")
	public SparseSymmetryStatistics(PermCheckingClauseList toSym) {
		numVars = toSym.getContext().size();
		varToPart = new int[2*numVars+1];
		botVarToPart = new int[2*numVars+1];
		this.cl = toSym.getClauses();
		int size = toSym.getDeepSize();


		litClauses = new IntToIntLinkedHashMap[2*numVars+1];

		for(int k = 0; k < litClauses.length; k++) {
			litClauses[k] = new IntToIntLinkedHashMap(32,.5f,true);
		}

		for(int[] clause : cl) {
			for(int k = 0; k < clause.length; k++) {
				int lit1 = clause[k];
				int ind1 = LitUtil.getIndex(lit1,numVars);
				IntToIntLinkedHashMap map1 = litClauses[ind1];

				for(int i = k+1; i < clause.length; i++) {
					int lit2 = clause[i];
					int ind2 = LitUtil.getIndex(lit2,numVars);
					IntToIntLinkedHashMap map2 = litClauses[ind2];

					map1.increment(lit2);
					map2.increment(lit1);
				}
			}
		}
	}

	public int[][][] getPartFreqs(StatsInfo[] toTopUse, StatsInfo[] toBotUse) {
		final int[][][] ret = new int[2][numVars*2+1][toTopUse.length];

		Arrays.fill(varToPart,-1);
		Arrays.fill(botVarToPart,-1);

		int part = 0;
		for(int j = 0; j < toTopUse.length; j++) {
			StatsInfo l = toTopUse[j];
			while(l.hasNext()) {
				l = l.next();
				int i = l.getLit();
				varToPart[LitUtil.getIndex(i,numVars)] = part;
			}
			part++;
		}

		part = 0;
		for(int j = 0; j < toBotUse.length; j++) {
			StatsInfo l = toBotUse[j];
			while(l.hasNext()) {
				l = l.next();
				int i = l.getLit();
				botVarToPart[LitUtil.getIndex(i,numVars)] = part;
			}
			part++;
		}

		for(int[] clause : cl) {
			for(int j : clause) {
				int indexJ = LitUtil.getIndex(j,numVars);
				int jPart;
				if((jPart = varToPart[indexJ]) != -1) {
					for(int i : clause) {
						int indexI = LitUtil.getIndex(i,numVars);
						ret[0][indexI][jPart] += ((clause.length+3463)^0x98654321);
					}
				}

				if((jPart = botVarToPart[indexJ]) != -1) {
					for(int i : clause) {
						int indexI = LitUtil.getIndex(i,numVars);
						ret[1][indexI][jPart]+= ((clause.length+3463)^0x98654321);
					}
				}
			}
		}

		return ret;
	}

	public long[][] getPartHashes(StatsInfo[] toTopUse, StatsInfo[] toBotUse) {
		final long[][] ret = new long[2][numVars*2+1];
		Random rand = new Random();

		//		Arrays.fill(varToPart,-1);
		//		Arrays.fill(botVarToPart,-1);

		int part = 0;
		for(int j = 0; j < toTopUse.length; j++) {
			StatsInfo lt = toTopUse[j];
			StatsInfo ld = toBotUse[j];
			long partHash = rand.nextLong();
			while(lt.hasNext()) {
				lt = lt.next();
				int i = lt.getLit();
				int index = LitUtil.getIndex(i,numVars);

				EntryIter iter = litClauses[index].getIter();
				while(iter.hasNext()) {
					IntEntry entry = iter.next();
					int otherIndex = LitUtil.getIndex(entry.getKey(),numVars);
					ret[0][otherIndex] += entry.getValue()*(partHash);
				}

				ld = ld.next();
				i = ld.getLit();

				index = LitUtil.getIndex(i,numVars);

				iter = litClauses[index].getIter();
				while(iter.hasNext()) {
					IntEntry entry = iter.next();
					int otherIndex = LitUtil.getIndex(entry.getKey(),numVars);
					ret[1][otherIndex] += entry.getValue()*(partHash);
				}
			}
			part++;
		}

		return ret;
	}

	public int[][] getPartHashes2(StatsInfo[] toTopUse, StatsInfo[] toBotUse) {
		final int[][] ret = new int[2][numVars*2+1];

		Arrays.fill(varToPart,-1);
		Arrays.fill(botVarToPart,-1);

		int part = 0;
		for(int j = 0; j < toTopUse.length; j++) {
			StatsInfo lt = toTopUse[j];
			StatsInfo ld = toBotUse[j];
			while(lt.hasNext()) {
				lt = lt.next();
				int i = lt.getLit();
				varToPart[LitUtil.getIndex(i,numVars)] = part;

				ld = ld.next();
				i = ld.getLit();
				botVarToPart[LitUtil.getIndex(i,numVars)] = part;
			}
			part++;
		}

		for(int[] clause : cl) {
			for(int j : clause) {
				int indexJ = LitUtil.getIndex(j,numVars);
				int jPartT = varToPart[indexJ];
				int jPartB = botVarToPart[indexJ];

				if(jPartT != -1) {
					if(jPartB != -1) {
						for(int i : clause) {
							int indexI = LitUtil.getIndex(i,numVars);
							ret[0][indexI] += hash(jPartT,clause);
							ret[1][indexI] += hash(jPartB,clause);
						}
					} else {
						for(int i : clause) {
							int indexI = LitUtil.getIndex(i,numVars);
							ret[0][indexI] += hash(jPartT,clause);
						}
					}
				} else if(jPartB != -1) {
					for(int i : clause) {
						int indexI = LitUtil.getIndex(i,numVars);
						ret[1][indexI] += hash(jPartB,clause);
					}
				}
			}
		}

		return ret;
	}

	public static final int hash(int part, int[] clause) {
		return (part+31)^clause.length;
	}

	public static int times = 0;
	public int[][] getPartFreqs(StatsInfo[] toUse) {
		final int[][] ret = new int[numVars*2+1][toUse.length];

		Arrays.fill(varToPart,-1);

		int index = 0;
		int part = 0;

		for(int j = 0; j < toUse.length; j++) {
			StatsInfo l = toUse[j];
			while(l.hasNext()) {
				l = l.next();
				int i = l.getLit();
				varToPart[LitUtil.getIndex(i,numVars)] = part;
				index++;
			}
			part++;
		}

		for(int[] clause : cl) {
			for(int j : clause) {
				int indexJ = LitUtil.getIndex(j,numVars);
				int jPart;
				if((jPart = varToPart[indexJ]) != -1) {
					for(int i : clause) {
						int indexI = LitUtil.getIndex(i,numVars);
						ret[indexI][jPart]++;
					}
				}
			}
		}

		//		IntPredicate pred = lit-> (varToPart[LitUtil.getIndex(lit,numVars)] != -1);
		//		
		//		cl.stream().filter(ar -> Arrays.stream(ar).anyMatch(pred))
		//		.forEach(ar-> Arrays.stream(ar).filter(pred).forEach(
		//				lit1 -> {
		//					int ind1 = LitUtil.getIndex(lit1,numVars);
		//					int part1 = varToPart[ind1];
		//					for(int lit2 : ar) {
		//						int ind2 = LitUtil.getIndex(lit2,numVars);
		//						ret[ind2][part1]++;
		//					
		//					}
		//				}
		//				)
		//				);


		//		for(int j = 0; j < toUse.length; j++) {
		//			final int index = j;
		//			StatsInfo l = toUse[j];
		//			while(l.hasNext()) {
		//				l = l.next();
		//				int i = l.getLit();
		//				
		//				cl.stream().filter(ar -> Arrays.stream(ar).anyMatch(lit -> (lit == i))).forEach(
		//						ar -> Arrays.stream(ar).forEach(lit->ret[LitUtil.getIndex(lit,numVars)][index]++)
		//						);
		//			}
		//		}


		//		IntStream.range(0,toUse.length).forEach( 
		//				j -> {
		//
		//					StatsInfo l = toUse[j];
		//					while(l.hasNext()) {
		//						l = l.next();
		//						final int i = l.getLit();
		//						final int lIndex = LitUtil.getIndex(i,numVars);
		//
		//						final int[][] freqs = litClauses[lIndex];
		//
		//						Arrays.stream(freqs).forEach(
		//								other -> {
		//									int otherInd = LitUtil.getIndex(other[0],numVars);
		//									ret[otherInd][j]+=other[1];
		//
		//								}
		//								);
		//
		//					}
		//				}
		//				);

		//		for(int j = 0; j < toUse.length; j++) {
		//			StatsInfo l = toUse[j];
		//			while(l.hasNext()) {
		//				l = l.next();
		//				int i = l.getLit();
		//				int lIndex = LitUtil.getIndex(i,numVars);
		//				
		//				int[][] freqs = litClauses[lIndex];
		//		
		//				for(int[] other : freqs) {
		//					int otherInd = LitUtil.getIndex(other[0],numVars);
		//					ret[otherInd][j]+=other[1];
		//
		//				}
		//			}
		//		}

		return ret;
	}

}
