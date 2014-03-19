package task.symmetry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import util.DisjointSet;
import formula.simple.ClauseList;

public class SimpleSymFinder {
	private ClauseList cl;
	public int numIters = 0;
	private int numFound = 0;
	private SemiPermutableClauseList pcl;
	//	private Set<int[]> checkClauses;

	private SymmetryStatistics stats;

	List<List<Integer>> prunedVars = new ArrayList<List<Integer>>(); //These vars form a complete symmetry group
	//and were pruned for efficiency
	//Can be used for efficient symmetry breaking

	private boolean keepGoing = true; //setting to false stops the symmetry finding process

	public SimpleSymFinder(ClauseList cl) {
		this.cl = cl;
		pcl = new SemiPermutableClauseList(cl);
		stats = new SymmetryStatistics(pcl);

		//		checkClauses = new LitsSet(cl.getContext().getNumVarsMade()); //new HashSet<int[]>();
		//		checkClauses.addAll(cl.getClauses());
	}


	public List<int[]> getSyms() {
		final ArrayList<int[]> ret = new ArrayList<int[]>();
		FoundSymmetryAction act = new FoundSymmetryAction() {
			@Override
			public boolean foundSymmetry(int[] perm) {
				//				System.out.println(Arrays.toString(perm));
				//				CNF permed = cnf.permute(permutation);
				//				if(!permed.equals(cnf)) {
				//					System.out.println("false");
				//					throw new RuntimeException("SDFSDF");
				//				}

				numFound++;
				if(numFound%100000 == 0) {
					System.out.println("F: " + numFound);
				}
				ret.add(perm);

				return true;
			}
		};

		getSyms(act);

		return ret;
	}

	public void getSyms(FoundSymmetryAction act) {
		getSyms(act,null);
	}

	public void getSyms(FoundSymmetryAction act, int[] requiredPair) {
		OrderedPartitionPair part = initializeForSearch(requiredPair);

		if(part != null) {		
			symSearch(part,act);
		}
		//Else exception?
	}
	
	public DisjointSet<Integer> getSymOrbits() {
		return getSymOrbits(null,null);
	}
	
	public DisjointSet<Integer> getSymOrbits(FoundSymmetryAction act) {
		return getSymOrbits(act,null);
	}

	public DisjointSet<Integer> getSymOrbits(FoundSymmetryAction act, int[] requiredPair) {
		OrderedPartitionPair part = initializeForSearch(requiredPair);

		DisjointSet<Integer> ret = new DisjointSet<Integer>(new ArrayList<Integer>(0));
		for(int k = 1; k <= cl.getContext().size(); k++) {
			ret.add(k);
			ret.add(-k);
		}
		
		if(part != null) {		
			symSearchWithOrbitPruning(part,act,ret);
		}
		//Else exception?
		return ret;
	}


	private OrderedPartitionPair initializeForSearch(int[] requiredPair) {
		keepGoing = true;
		pcl.reset();
		prunedVars.clear();
		numIters = 0;
		numFound = 0;

		List<List<Integer>> refinements = initialRefine();

		int numVars = cl.getContext().size();

		//Initial refine does some extra things such as removing lits that aren't part
		//of the formula, and lits that are in every clause (easy but potentiall massive cases)

		OrderedPartitionPair part = new OrderedPartitionPair(refinements);
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
		//when the time comes
		part = part.refine(stats);

		if(requiredPair != null) {
			//TODO: Sometimes the required pair does not even exist (e.g. in some random cases)
			int topPart = part.getTopPartWithElt(requiredPair[0]);
			int botPart = part.getBottomPartWithElt(requiredPair[1]);

			int topSize = part.topPartSize(topPart);

			if(topPart != botPart) return null;

			int topIndex = part.getTopPartIndexOfElt(topPart,requiredPair[0]);
			int botIndex = part.getBottomPartIndexOfElt(botPart,requiredPair[1]);

			pcl.post(); //unification changes pcl, so should post so that we can reset if used again later
			//Or change so that we don't need to pop later.

			part = performUnification(part,topPart, topIndex,botIndex,topSize);

		}
		return part;
	}

	//Not thread-safe
	protected void symSearch(OrderedPartitionPair part,FoundSymmetryAction act) {
		if(!keepGoing) return;
		numIters++;
		if(numIters%100000 == 0) {
			System.out.println(numIters);
		}

		int k = part.getFirstNonUnitPart();

		if(k == -1) {
			int[] permutation = part.getPermutation();
			keepGoing = act.foundSymmetry(permutation);

			//			permutation = PermutationUtil.getInverse(part.getPermutation());
			//			keepGoing &= act.foundSymmetry(permutation);
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return; //non-isomporphic

			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return;

				pcl.post();
				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);

				if(nextPart != null) {
					symSearch(nextPart,act);
				}

				pcl.pop();//undo any permutations before next iteration

			}
		}
	}

	//Not thread-safe
	//Returns true if found a symmetry
	protected boolean symSearchWithOrbitPruning(OrderedPartitionPair part,FoundSymmetryAction act, DisjointSet<Integer> litOrbits) {
		if(!keepGoing) return false;
		boolean found = false;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}

		int k = part.getFirstNonUnitPart();

		if(k == -1) {
			int[] permutation = part.getPermutation();
			//System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(permutation)));
			for(int i = 0; i < permutation.length; i++) {
				if(permutation[i] != i) {
					litOrbits.join(i,permutation[i]);
				}
			}
			
			if(act != null) {
				keepGoing = act.foundSymmetry(permutation);
			}
			return true;

			//			permutation = PermutationUtil.getInverse(part.getPermutation());
			//			keepGoing &= act.foundSymmetry(permutation);
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			int topElt = part.getTopElt(k,0);
			
			LinkedList<Integer> definitelyNotInSameOrbitAsTop = new LinkedList<Integer>();
			
			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;
				
				
				int botElt = part.getBottomElt(k,j);
				
				if(topElt != botElt && litOrbits.sameSet(topElt,botElt)) {
					//System.out.println("_"+topElt+"."+botElt);
					continue;
				} else if(topElt != botElt) { //that is, litOrbits.sameSet is false
//					for(int i : definitelyNotInSameOrbitAsTop) {
//						if(litOrbits.sameSet(i,botElt)) {
//							System.out.println("#"+topElt+"."+botElt);
//							continue searchBot;
//						}
//					}
				}

				pcl.post();
				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(nextPart != null) {
					hasPerm = symSearchWithOrbitPruning(nextPart,act,litOrbits);
					found |= hasPerm;
					
				}
				
				if(!hasPerm) {
					definitelyNotInSameOrbitAsTop.add(botElt);
				}


				pcl.pop();//undo any permutations before next iteration

			}
		}
		
		return found;
	}

	//Returns null if pruned
	protected OrderedPartitionPair performUnification(OrderedPartitionPair part, int partIndex, int topIndex, int botIndex, int topSize) {
		int topElt = part.getTopElt(partIndex,topIndex);
		int botElt = part.getBottomElt(partIndex,botIndex);

		//if(topElt < botElt) return; //we add inverses along with any permutation we find,
		//which means that if a perm goes from 1 to 2, we already put in one that goes from 2 to 1

		int otherTopElt = 0;
		int otherBotElt = 0;

		if(topSize == 2) {
			//have to take into account side-effects
			otherTopElt = part.getTopElt(partIndex,1-topIndex);
			otherBotElt = part.getBottomElt(partIndex,1-botIndex);
		}


		//List<int[]> finishedClauses = pcl.partialPermute(topElt,botElt);
		if(!pcl.permuteAndCheck(topElt,botElt)) {
			return null;
		}

		if(topSize == 2) {
			if(Math.abs(topElt) != Math.abs(otherTopElt)) {
				//permute side-effect
				//finishedClauses.addAll(pcl.partialPermute(otherTopElt,otherBotElt));
				if(!pcl.permuteAndCheck(otherTopElt,otherBotElt)) {
					return null;
				}
			}
		}

		//		for(int[] cl : finishedClauses) {
		//			if(!checkClauses.contains(cl)) {
		//				return null;
		//			}
		//		}

		OrderedPartitionPair nextPart = part;
		nextPart = nextPart.assignIndeciesToUnitPart(partIndex,topIndex, botIndex);
		nextPart = nextPart.assignEltsToUnitPart(-topElt,-botElt);
		if(nextPart == null) return null; //Cannot make dual permutation work


		OrderedPartitionPair temp = new OrderedPartitionPair(); //Will contain all new unit partitions made by the refinement 

		nextPart = nextPart.refine(stats,temp); 	//**Important line**//

		if(nextPart != null) { //null if nonisomorphic refinement
			//finishedClauses = new ArrayList<int[]>();

			for(int i = 0; i < temp.topParts(); i++) {
				//finishedClauses.addAll(pcl.partialPermute(temp.getTopElt(i,0),temp.getBottomElt(i,0)));
				if(!pcl.permuteAndCheck(temp.getTopElt(i,0),temp.getBottomElt(i,0))) {
					return null;
				}
			}

			//			for(int[] cl : finishedClauses) {
			//				if(!checkClauses.contains(cl)) {
			//					return null;
			//				}
			//			}
		}

		return nextPart;
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

	//TODO:Gah. I should use the stats variable for this.
	//This is a little different from most refinements because
	//We are guaranteed the result will make an isomorphic OOP
	//and because we want to maybe do some efficiency changes
	//(e.g. remove variables that don't exist from consideration)
	protected List<List<Integer>> initialRefine() {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();

		int numVars = cl.getContext().size();
		int[] posFreq = new int[numVars+1];
		int[] negFreq = new int[numVars+1];

		for(int[] clause : cl.getClauses()) {
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


			//Ignore lits that are in 0 /*or all clauses*/
			//Since each group will easily have n! symmetries,
			//which can unnecessarily clutter everything up
			int freq = freqArray[Math.abs(elt)];
			if(freq == 0) {// || freq == cl.getClauses().size()) {
				prunedVars.add(ret.remove(k));
				k--;
			}

		}


		return ret;
	}

	private int getFreq(int varNum, int[] posFreq, int[] negFreq) {
		return varNum > 0 ? posFreq[varNum] : negFreq[-varNum];
	}
}
