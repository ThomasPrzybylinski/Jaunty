package task.symmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralPermutation;
import util.IntPair;

//checks to see if you can map one set of literals into another using the symmetries of Clauselist
public class ModelMapper {
	public int numIters = 0;
	private SemiPermutableClauseList pcl;
	int curKnownInd = -1;

	//	private Set<int[]> checkClauses;

	private SymmetryStatistics stats;

	private boolean keepGoing = true; //setting to false stops the symmetry finding process

	private int virtToRealVars[];
	private int realToVirtVars[];
	
	private LiteralPermutation foundPerm = null;
	private int numTotalVars = 0;
	
	public ModelMapper(ClauseList list) {
		this.numTotalVars = list.getContext().size();
		boolean[] varFound = new boolean[list.getContext().size()+1];

		int totalVars = 0;

		for(int[] c : list.getClauses()) {
			for(int i : c) {
				int var = Math.abs(i);

				if(!varFound[var]) {
					varFound[Math.abs(i)] = true;
					totalVars++;
				}
			}
		}

		virtToRealVars = new int[totalVars+1];
		realToVirtVars = new int[varFound.length];
		Arrays.fill(realToVirtVars,-1);

		int index = 1;
		for(int i = 1; i < varFound.length; i++) {
			if(varFound[i]) {
				virtToRealVars[index] = i;
				realToVirtVars[i] = index;
				index++;
			}
		}

		//		System.out.println(index);

		VariableContext virtContext = new VariableContext();
		while(virtContext.size() < totalVars) {
			virtContext.createNextDefaultVar();
		}

		ClauseList virtList = new ClauseList(virtContext);

		for(int[] c : list.getClauses()) {
			int[] virt = new int[c.length];

			for(int k = 0; k < c.length; k++) {
				int lit = c[k];
				int var = Math.abs(lit);
				int sign = lit/var;

				virt[k] = sign*realToVirtVars[var];
			}

			virtList.fastAddClause(virt);
		}

		pcl = new SemiPermutableClauseList(virtList);
		stats = new SymmetryStatistics(pcl);
	}

	public boolean canMap(int[] from, int[] to) {
		int[] transFrom = translate(from);
		int[] transTo = translate(to);

		OrderedPartitionPair part = initializeForSearch(transFrom, transTo);

		if(part == null || !part.checkIsomorphic()) {
			return false;
		}

		return generate(part);
	}

	private boolean generate(OrderedPartitionPair part) {
		// TODO Auto-generated method stub
		if(!keepGoing) return true;
		boolean found = false;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}

		int k = part.getLeastABSNonUnitPart(); //Get the literals in order

		if(k == -1) {
			foundPerm = getRealPerm(part.getPermutation());
			//When all parititions are unit paritions, we have a single permutation
			return true;
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;

				pcl.post();
				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(nextPart != null) {
					hasPerm = generate(nextPart);
				}

				pcl.pop();//undo any permutations before next iteration
				if(hasPerm) {
					return true;
				}

			}
		}

		return found;
	}

	//Returns null if pruned
	protected OrderedPartitionPair performUnification(OrderedPartitionPair part, int partIndex, int topIndex, int botIndex, int topSize) {
		int topElt = part.getTopElt(partIndex,topIndex);
		int botElt = part.getBottomElt(partIndex,botIndex);

		int otherTopElt = 0;
		int otherBotElt = 0;

		if(topSize == 2) {
			//have to take into account side-effects
			otherTopElt = part.getTopElt(partIndex,1-topIndex);
			otherBotElt = part.getBottomElt(partIndex,1-botIndex);
		}

		if(!pcl.permuteAndCheck(topElt,botElt)) {
			return null;
		}

		if(topSize == 2) {
			if(Math.abs(topElt) != Math.abs(otherTopElt)) {
				//permute side-effect
				if(!pcl.permuteAndCheck(otherTopElt,otherBotElt)) {
					return null;
				}
			}
		}

		OrderedPartitionPair nextPart = part;
		nextPart = nextPart.assignIndeciesToUnitPart(partIndex,topIndex, botIndex);
		nextPart = nextPart.assignEltsToUnitPart(-topElt,-botElt);
		if(nextPart == null) return null; //Cannot make dual permutation work

		//Will contain all new unit partitions made by the refinement
		OrderedPartitionPair newPairs = new OrderedPartitionPair(); 

		OrderedPartitionPair lastPart = nextPart;
		nextPart = lastPart.refine(stats,newPairs); 	//**Important line**//

		if(nextPart != null) { //null if nonisomorphic refinement
			for(int i = 0; i < newPairs.topParts(); i++) {
				if(!pcl.permuteAndCheck(newPairs.getTopElt(i,0),newPairs.getBottomElt(i,0))) {
					return null;
				}
			}
		} else {
			//			System.out.println("%"+topElt+"."+botElt);
		}

		return nextPart;
	}

//	private OrderedPartitionPair applyIsoMapping(OrderedPartitionPair nextPart,
//			int topElt, int botElt) {
////		int toppart = nextPart.getTopPartWithElt(topElt);
////		int botpart = nextPart.getBottomPartWithElt(botElt);
////		
////		if(toppart != botpart) return null; //Cannot map sufficiently
//		
//		
//		return null;
//	}

	private int[] translate(int[] set) {
		int[] ret = new int[set.length];

		int retInd = 0;
		for(int k = 0; k < set.length; k++,retInd++) {
			int image = set[k];
			int imageVar = Math.abs(image);
			int virVar = realToVirtVars[imageVar];
			if(virVar == -1) { //If somehow var does not exist
				int[] ret2 = new int[ret.length-1];
				System.arraycopy(ret,0,ret2,0,k-(k-retInd));
				ret = ret2;
				retInd--;
			} else {
				ret[retInd] = (image/imageVar)*virVar;
			}
		}

		return ret;
	}


	private OrderedPartitionPair initializeForSearch(int[] from, int[] to) {
		foundPerm = null;
		keepGoing = true;
		pcl.reset();
		pcl.post();
		numIters = 0;

		OrderedPartitionPair part = getInitial(from,to); //getWeakInitialRefine();
//		OrderedPartitionPair part2 =  part.refine(stats); //getWeakInitialRefine();
//		if(part2 != null) {
//			System.out.println("NOTNULL");
//		}
		part = initialRefine(part);
		
		if(part == null) return null;

		int numVars = pcl.getContext().size();
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
								//when the time comes

		part = part.refine(stats);

//		part = getRefineOnModels(part, from,to);

//		part = part.refine(stats);
		
		if(part == null) return null;
		
		//make sure variables are mapped consistently
		for(int k = 0; k < part.topParts(); k++) {
			if(part.topPartSize(k) == 1 && !pcl.isPermuted(part.getTopElt(k,0))) {
				part = part.assignEltsToUnitPart(-part.getTopElt(k,0),-part.getBottomElt(k,0));
			}
		}
		
		//Be sure to permute things we've seen permuted
		for(int k = 0; k < part.topParts(); k++) {
			if(part.topPartSize(k) == 1 && !pcl.isPermuted(part.getTopElt(k,0))) {
				if(!pcl.permuteAndCheck(part.getTopElt(k,0),part.getBottomElt(k,0))) {
					return null;
				}
			}
		}

		return part;
	}
	


	private OrderedPartitionPair getInitial(int[] from, int[] to) {
		ArrayList<List<Integer>> newTop = new ArrayList<List<Integer>>();
		ArrayList<List<Integer>> newBot = new ArrayList<List<Integer>>();
		
		ArrayList<Integer> top1 = new ArrayList<Integer>();
		ArrayList<Integer> top2 = new ArrayList<Integer>();
		ArrayList<Integer> bot1 = new ArrayList<Integer>();
		ArrayList<Integer> bot2 = new ArrayList<Integer>();
		
		for(int i : from) {
			top1.add(i);
			top2.add(-i);
		}
		for(int i : to) {
			bot1.add(i);
			bot2.add(-i);
		}
	
		newTop.add(top1);
		newTop.add(top2);
		newBot.add(bot1);
		newBot.add(bot2);
		
		return new OrderedPartitionPair(newTop,newBot);
		
	}
	
	private OrderedPartitionPair initialRefine(OrderedPartitionPair part) {
		int numVars = pcl.getContext().size();
		int[] posFreq = new int[numVars+1];
		int[] negFreq = new int[numVars+1];
		
		for(int[] clause : pcl.getClauses()) {
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
		
		ArrayList<List<Integer>> newTop = new ArrayList<List<Integer>>();
		ArrayList<List<Integer>> newBot = new ArrayList<List<Integer>>();
				
		for(int k = 0; k < part.topParts(); k++) {
			ArrayList<List<Integer>> splitTopPart = new ArrayList<List<Integer>>();
			HashMap<IntPair,Integer> seenFreqsToIndex = new HashMap<IntPair,Integer>();
			
			for(int i = 0; i < part.topPartSize(k); i++) {
				int lit = part.getTopElt(k,i);
				int var = Math.abs(lit);
				
				int freq1 = lit > 0 ? posFreq[var] : negFreq[var];
				int freq2 = lit > 0 ? negFreq[var] : posFreq[var];
				IntPair freqPair = new IntPair(freq1,freq2);
				
				Integer index = seenFreqsToIndex.get(freqPair);
				
				if(index == null) {
					ArrayList<Integer> newTopPart = new ArrayList<Integer>();
					seenFreqsToIndex.put(freqPair,splitTopPart.size());
					splitTopPart.add(newTopPart);
					
					newTopPart.add(lit);
					
				} else {
					splitTopPart.get(index).add(lit);
				}
			}
			
			ArrayList<List<Integer>> splitBotPart = new ArrayList<List<Integer>>();
			for(int i = 0; i < splitTopPart.size(); i++) {
				splitBotPart.add(new ArrayList<Integer>(splitTopPart.get(i).size()));
			}
			
			for(int i = 0; i < part.bottomPartSize(k); i++) {
				int lit = part.getBottomElt(k,i);
				int var = Math.abs(lit);
				
				int freq1 = lit > 0 ? posFreq[var] : negFreq[var];
				int freq2 = lit > 0 ? negFreq[var] : posFreq[var];
				IntPair freqPair = new IntPair(freq1,freq2);
				
				Integer index = seenFreqsToIndex.get(freqPair);
				
				if(index == null) {
					return null; //invalid OPP
				} else {
					splitBotPart.get(index).add(lit);
				}
			}
			
			for(int i = 0; i < splitTopPart.size(); i++) {
				if(splitBotPart.get(i).size() != splitTopPart.get(i).size()) {
					return null;  //invalid OPP
				}
			}
			
			newTop.addAll(splitTopPart);
			newBot.addAll(splitBotPart);
		}
		
		OrderedPartitionPair ret = new OrderedPartitionPair(newTop,newBot);
		return ret;
	}

	private LiteralPermutation getRealPerm(int[] permutation) {
		return new LiteralPermutation(translate(permutation,realToVirtVars,virtToRealVars));
	}

	private int[] translate(int[] permutation, int[] varTrans, int[] invTrans) {
		int[] perm = new int[numTotalVars+1];

		for(int k = 1; k < perm.length; k++) {
			if(varTrans[k] == -1) {
				perm[k] = k;
			} else {
				int transVar = varTrans[k];
				int image = permutation[transVar];
				int imageVar = Math.abs(image);

				perm[k] = (image/imageVar)*invTrans[Math.abs(permutation[transVar])];
			}
		}

		return perm;
	}

	public LiteralPermutation getFoundPerm() {
		return foundPerm;
	}

	public int[] greedyFindLessRestrictiveConditionForPerm(int[] agree,
			LiteralPermutation perm) {
		return pcl.greedyFindLessRestrictiveConditionForPerm(agree,perm);
	}

}
