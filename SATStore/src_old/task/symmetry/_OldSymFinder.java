package task.symmetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import util.LitsSet;
import util.MultilevelLitSet;
import formula.simple.CNF;



//TODO: Change it so that pos and neg literals are in the OPP separately. Change refinement to reflect this
//TODO: Build a Trie data structure for variables denoted by integers (probably Set interface)
//This will allow me to denote formula as tries for contains operations
//TODO: At each iteration of the sym finder, return those clauses which have been "fully permuted"
//by the current permutation. If one of the clauses is not part of the original formula, stop going through that tree

//Lesser priority (unless needed) is do better refinement techniques

public class _OldSymFinder {
	private CNF cnf;
	private int numIters = 0;
	private SemiPermutableClauseList pcnf;
	Set<int[]> checkClauses;

	public _OldSymFinder(CNF cnf) {
		this.cnf = cnf;
		pcnf = new SemiPermutableClauseList(cnf);

		checkClauses = new LitsSet(cnf.getContext().getNumVarsMade()); //new HashSet<int[]>();
		checkClauses.addAll(cnf.getClauses());
	}

	public List<int[]> getSyms() {
		numIters = 0;
		List<List<Integer>> refinements = initialRefine();

		OrderedPartitionPair part = new OrderedPartitionPair(refinements);
		MultilevelLitSet exploredPairs = new MultilevelLitSet(cnf.getContext().getNumVarsMade());
		List<int[]> syms = getSyms(part,exploredPairs);

//		for(int k = 0; k < syms.size(); k++) {
//			for(int i = k+1; i < syms.size(); i++) {
//				if(Arrays.equals(syms.get(k),syms.get(i))) {
//					syms.remove(i);
//					i--;
//				}
//			}
//		}

		return syms;

	}


	private List<int[]> getSyms(OrderedPartitionPair part,MultilevelLitSet exploredPairs) {
		numIters++;
		if(numIters%10000 == 0) {
			System.out.println(numIters);
		}

		exploredPairs.post();
		ArrayList<int[]> ret = new ArrayList<int[]>();

		int k = part.getFirstNonUnitPart();

		if(k == -1) {
			ret.add(part.getPermutation());
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return ret; //non-isomporphic

			for(int i = 0; i < topSize; i++) {
				for(int j = 0; j < bottomSize; j++) {
					if(topSize > 1) {
						OrderedPartitionPair nextPart = part;

						int topElt = part.getTopElt(k,i);
						int botElt = part.getBottomElt(k,j);

						if(exploredPairs.contains(new int[]{topElt,botElt})) continue;

						nextPart = nextPart.assignIndeciesToUnitPart(k,i,j);
						nextPart = nextPart.assignEltsToUnitPart(-topElt,-botElt);

						boolean checkMore = true;
						if(nextPart == null) checkMore = false; //Cannot make dual permutation work

						int otherTopElt = 0;
						int otherBotElt = 0;
						if(checkMore) {
							List<int[]> finishedClauses = pcnf.partialPermute(topElt,botElt);
							
							if(topSize == 2) {
								//permute side-effect
								otherTopElt = part.getTopElt(k,1-i);
								otherBotElt = part.getBottomElt(k,1-j);
								
								if(exploredPairs.contains(new int[]{otherTopElt,otherBotElt})) checkMore = false;
								
								finishedClauses.addAll(pcnf.partialPermute(otherTopElt,otherBotElt));
							}

							if(checkMore) {
								for(int[] cl : finishedClauses) {
									if(!checkClauses.contains(cl)) {
										checkMore = false;
										break;
									}
								}
							}

							if(checkMore) {
								ret.addAll(getSyms(nextPart,exploredPairs));
								exploredPairs.add(new int[]{topElt,botElt});
								
								if(topSize == 2) {
									exploredPairs.add(new int[]{otherTopElt,otherBotElt});
								}
							}
				
							if(topSize == 2) {
								//unpermute the side-effects
								pcnf.undoLastPerm();
							}
							pcnf.undoLastPerm();
						}
					}
				}
			}
		}
		exploredPairs.pop();
		return ret;
	}

	//	private List<int[]> getSyms(PairwiseOrderedPartition part) {
	//		numIters++;
	//		if(numIters%10000 == 0) {
	//			System.out.println(numIters);
	//		}
	//
	//		ArrayList<int[]> ret = new ArrayList<int[]>();
	//		int[] perm = part.getPermutation();
	//
	//		if(perm == null) {
	//			int k = part.getFirstNonUnitPart();
	//			
	//			int topSize = part.topPartSize(k);
	//			int bottomSize = part.topPartSize(k);
	//
	//			if(topSize != bottomSize) return ret; //non-isomporphic
	//
	//			for(int i = 0; i < topSize; i++) {
	//				for(int j = 0; j < bottomSize; j++) {
	//					if(topSize > 1) {
	//						PairwiseOrderedPartition nextPart = part;
	//
	//						int topElt = part.getTopElt(k,i);
	//						int botElt = part.getBottomElt(k,j);
	//
	//						nextPart = nextPart.assignIndeciesToSinglePart(k,i,j);
	//
	//						nextPart = nextPart.assignEltsToSinglePart(-topElt,-botElt);
	//						
	//						if(nextPart == null) return ret; //Cannot make dual permutation work
	//
	//						ret.addAll(getSyms(nextPart));
	//					}
	//					//						if(part.getBottomElt(k,j) > 0) {
	//						//							//Still want to phase shift, don't want to double-phase shift
	//					//							ret.addAll(getSyms(part.assignEltsToSinglePart(k,i,j,true)));
	//					//						}
	//				}
	//
	//			}
	//		} else {
	//			CNF toCompare = cnf.permute(perm);
	//			if(toCompare.equals(cnf)) {
	//				ret.add(perm);
	//			}
	//		}
	//
	//		return ret;
	//	}

	private List<List<Integer>> initialRefine() {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();

		int numVars = cnf.getContext().getNumVarsMade();
		int[] posFreq = new int[numVars+1];
		int[] negFreq = new int[numVars+1];

		for(int[] clause : cnf.getClauses()) {
			for(int i : clause) {
				int[] freqArray;
				if(i > 0) {
					freqArray = posFreq;
				} else {
					freqArray = negFreq;
				}

				freqArray[Math.abs(i)]++;
			}
		}

		for(int k = 1; k < posFreq.length; k++) {
			int posIndex = -1;
			int negIndex = -1;
			for(int i = 0; i < ret.size() && (posIndex == -1 || negIndex == -1); i++) {
				List<Integer> list = ret.get(i);
				int varNum = list.get(0);

				//If var a goes to var b, then var -a must go to var -b
				//which means that both frequencies have to match
				if(posFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& negFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(k);
					posIndex = i;
				}

				if(negFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& posFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(-k);
					negIndex = i;
				}

			}

			if(posIndex == -1) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(k);
				if(negIndex == -1 && posFreq[k] == negFreq[k]) {
					newPart.add(-k);
				}
				ret.add(newPart);
			}

			if(negIndex == -1 && (posIndex != -1 || posFreq[k] != negFreq[k])) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(-k);
				ret.add(newPart);
			}
		}
		
		for(int k = 0; k < ret.size(); k++) {
			int elt = ret.get(k).get(0);
			
			int[] freqArray;
			if(elt > 0) {
				freqArray = posFreq;
			} else {
				freqArray = negFreq;
			}

			if(freqArray[Math.abs(elt)] == 0) {
				ret.remove(k);
				k--;
			}
			
		}


		return ret;
	}

	private int getFreq(int varNum, int[] posFreq, int[] negFreq) {
		return varNum > 0 ? posFreq[varNum] : negFreq[-varNum];
	}
}