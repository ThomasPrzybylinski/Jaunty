package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralPermutation;
import util.IntPair;

//checks to see if you can map one set of literals into another using the symmetries of Clauselist
public class SparseModelMapper {
	public int numIters = 0;
	private PermCheckingClauseList pcl;
	int curKnownInd = -1;

	//	private Set<int[]> checkClauses;

	private SparseSymmetryStatistics stats;

	private boolean keepGoing = true; //setting to false stops the symmetry finding process

	private int virtToRealVars[];
	private int realToVirtVars[];

	private LiteralPermutation foundPerm = null;
	private int numTotalVars = 0;

	public SparseModelMapper(ClauseList list) {
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

		pcl = new PermCheckingClauseList(virtList);
		stats = new SparseSymmetryStatistics(pcl);
	}

	public boolean canMap(int[] from, int[] to) {
		int[] transFrom = translate(from);
		int[] transTo = translate(to);

		SparseOrderedPartitionPair part = initializeForSearch(transFrom, transTo);

		if(part == null || !part.checkIsomorphic()) {
			return false;
		}

		return generate(part);
	}

	private boolean generate(SparseOrderedPartitionPair part) {
		// TODO Auto-generated method stub
		if(!keepGoing) return true;
		boolean found = false;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}

		int k = part.getLeastABSNonUnitPart(); //Get the literals in order

		if(k == -1) {
			int[] perm = part.getPermutation();
			if(pcl.checkPerm(perm)) {
				foundPerm = getRealPerm(perm);
			}
			//When all parititions are unit paritions, we have a single permutation
			return true;
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;

				part.post();
				boolean unified = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(unified) {

					if(j != 0) {
						int[] shortCutPerm = part.getShortcutPermutation();
						hasPerm = pcl.checkPerm(shortCutPerm);
					}

					if(!hasPerm) {
						hasPerm = generate(part);
					}
				}

				part.pop();

				if(hasPerm) {
					return true;
				}

			}
		}

		return found;
	}

	//Returns null if pruned
	protected boolean performUnification(SparseOrderedPartitionPair part, int partIndex, int topIndex, int botIndex, int topSize) {
		int topElt = part.getTopElt(partIndex,topIndex);
		int botElt = part.getBottomElt(partIndex,botIndex);

		int otherTopElt = 0;
		int otherBotElt = 0;

		if(topSize == 2) {
			//have to take into account side-effects
			otherTopElt = part.getTopElt(partIndex,1-topIndex);
			otherBotElt = part.getBottomElt(partIndex,1-botIndex);
		}

		//		if(!pcl.permuteAndCheck(topElt,botElt)) {
		//			return null;
		//		}

		//		if(topSize == 2) {
		//			if(Math.abs(topElt) != Math.abs(otherTopElt)) {
		//				//permute side-effect
		//				if(!pcl.permuteAndCheck(otherTopElt,otherBotElt)) {
		//					return null;
		//				}
		//			}
		//		}

		SparseOrderedPartitionPair nextPart = part;
		nextPart.assignIndeciesToUnitPart(partIndex,topIndex, botIndex);
		boolean ok = nextPart.assignEltsToUnitPart(-topElt,-botElt);
		if(!ok) return false; //Cannot make dual permutation work

		ok = part.refine(stats); 	//**Important line**//

		return ok;
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


	private SparseOrderedPartitionPair initializeForSearch(int[] from, int[] to) {
		foundPerm = null;
		keepGoing = true;
		numIters = 0;

		InitialReturn ret = getInitial(from,to); //getWeakInitialRefine();
		//		OrderedPartitionPair part2 =  part.refine(stats); //getWeakInitialRefine();
		//		if(part2 != null) {
		//			System.out.println("NOTNULL");
		//		}

		SparseOrderedPartitionPair part = initialRefine(ret);

		if(part == null) return null;
		part.post();

		int numVars = pcl.getContext().size();
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
		//when the time comes

		SparseOrderedPartitionPair oldPart = part;
		part.post();
		boolean ok = part.refine(stats,true);

		//		part = getRefineOnModels(part, from,to);

		//		part = part.refine(stats);

		if(!ok) return null;

		//make sure variables are mapped consistently
		for(int k = 0; k < part.topParts(); k++) {
			if(part.topPartSize(k) == 1) {
				//				oldPart = part;
				ok = part.assignEltsToUnitPart(-part.getTopElt(k,0),-part.getBottomElt(k,0));

				if(!ok) {
					ok = part.assignEltsToUnitPart(-part.getTopElt(k,0),-part.getBottomElt(k,0));

					return null;
				}
			}
		}

		//		//Be sure to permute things we've seen permuted
		//		for(int k = 0; k < part.topParts(); k++) {
		//			if(part.topPartSize(k) == 1 && !pcl.isPermuted(part.getTopElt(k,0))) {
		//				if(!pcl.permuteAndCheck(part.getTopElt(k,0),part.getBottomElt(k,0))) {
		//					return null;
		//				}
		//			}
		//		}

		return part;
	}

	private class InitialReturn {
		ArrayList<IntList> newTop;
		ArrayList<IntList> newBot;
		public InitialReturn(ArrayList<IntList> newTop,
				ArrayList<IntList> newBot) {
			super();
			this.newTop = newTop;
			this.newBot = newBot;
		}


	}

	private InitialReturn getInitial(int[] from, int[] to) {
		ArrayList<IntList> newTop = new ArrayList<IntList>();
		ArrayList<IntList> newBot = new ArrayList<IntList>();

		ArrayIntList top1 = new ArrayIntList();
		ArrayIntList top2 = new ArrayIntList();
		ArrayIntList top3 = new ArrayIntList();
		ArrayIntList bot1 = new ArrayIntList();
		ArrayIntList bot2 = new ArrayIntList();
		ArrayIntList bot3 = new ArrayIntList();

		boolean[] topSeen = new boolean[virtToRealVars.length];
		boolean[] botSeen = new boolean[virtToRealVars.length];
		int topNumSeen = 0;
		int botNumSeen = 0;

		for(int i : from) {
			top1.add(i);
			top2.add(-i);
			topSeen[Math.abs(i)] = true;
			topNumSeen++;
		}
		for(int i : to) {
			bot1.add(i);
			bot2.add(-i);
			botSeen[Math.abs(i)] = true;
			botNumSeen++;
		}

		newTop.add(top1);
		newTop.add(top2);
		newBot.add(bot1);
		newBot.add(bot2);

		if(topNumSeen != virtToRealVars.length-1 || botNumSeen != virtToRealVars.length-1) {
			for(int k = 1; k < topSeen.length; k++) {
				if(!topSeen[k]) {
					top3.add(k);
					top3.add(-k);
				}
				if(!botSeen[k]) {
					bot3.add(k);
					bot3.add(-k);
				}
			}
			newTop.add(top3);
			newBot.add(bot3);
		}




		return new InitialReturn(newTop,newBot);

	}

	private SparseOrderedPartitionPair initialRefine(InitialReturn part) {
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

		ArrayList<IntList> newTop = new ArrayList<IntList>();
		ArrayList<IntList> newBot = new ArrayList<IntList>();

		for(int k = 0; k < part.newTop.size(); k++) {
			ArrayList<IntList> splitTopPart = new ArrayList<IntList>();
			HashMap<IntPair,Integer> seenFreqsToIndex = new HashMap<IntPair,Integer>();

			IntList topPart = part.newTop.get(k);
			IntList botPart = part.newBot.get(k);

			for(int i = 0; i < topPart.size(); i++) {
				int lit = topPart.get(i);
				int var = Math.abs(lit);

				int freq1 = lit > 0 ? posFreq[var] : negFreq[var];
				int freq2 = lit > 0 ? negFreq[var] : posFreq[var];
				IntPair freqPair = new IntPair(freq1,freq2);

				Integer index = seenFreqsToIndex.get(freqPair);

				if(index == null) {
					ArrayIntList newTopPart = new ArrayIntList();
					seenFreqsToIndex.put(freqPair,splitTopPart.size());
					splitTopPart.add(newTopPart);

					newTopPart.add(lit);

				} else {
					splitTopPart.get(index).add(lit);
				}
			}

			ArrayList<IntList> splitBotPart = new ArrayList<IntList>();
			for(int i = 0; i < splitTopPart.size(); i++) {
				splitBotPart.add(new ArrayIntList(splitTopPart.get(i).size()));
			}

			for(int i = 0; i < botPart.size(); i++) {
				int lit = botPart.get(i);
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

		SparseOrderedPartitionPair ret = new SparseOrderedPartitionPair(newTop,newBot);
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

}
