package task.symmetry.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import task.symmetry.FoundSymmetryAction;
import task.symmetry.OrderedPartitionPair;
import task.symmetry.SemiPermutableClauseList;
import task.symmetry.SymmetryStatistics;
import util.DisjointSet;
import util.IntegralDisjointSet;
import util.PermutationUtil;
import util.lit.LitsSet;
import formula.VariableContext;
import formula.simple.ClauseList;

/*
	Created so that SimpleSymFinder remains backwards compatible.
	This one always does orbit pruning, is the basis for trying to find local symmetries, eventually
 */
public class BetterSymFinder {
	private ClauseList cl;
	public int numIters = 0;
	private SemiPermutableClauseList pcl;
	//	private Set<int[]> checkClauses;

	private SymmetryStatistics stats;

	private boolean keepGoing = true; //setting to false stops the symmetry finding process
	
	private int virtToRealVars[];
	private int realToVirtVars[];

	public BetterSymFinder(ClauseList cl) {
		this.cl = cl;
		
		boolean[] varFound = new boolean[cl.getContext().size()+1];
		
		int totalVars = 0;
		
		for(int[] c : cl.getClauses()) {
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
		
		for(int[] c : cl.getClauses()) {
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


	public List<int[]> getSyms() {
		final ArrayList<int[]> ret = new ArrayList<int[]>();
		FoundSymmetryAction act = new FoundSymmetryAction() {
			@Override
			public boolean foundSymmetry(int[] perm) {
				ret.add(perm);
				return true;
			}
		};

		getSymOrbits(act);

		return ret;
	}

	private IntegralDisjointSet getSymOrbits() {
		return getSymOrbits(null);
	}

	private IntegralDisjointSet getSymOrbits(FoundSymmetryAction act) {
		OrderedPartitionPair part = initializeForSearch();

		int numVars = pcl.getContext().size();

		IntegralDisjointSet ret = new IntegralDisjointSet(-numVars,numVars);
		//LitsSet firstInOrbit = new LitsSet(numVars);
		
		TreeSet<Integer> firstInOrbit = new TreeSet<Integer>();

		for(int k = -numVars; k <= numVars; k++) {
			firstInOrbit.add(k);
		}

		if(part != null) {		
			symSearchWithOrbitPruning(part,act,ret,firstInOrbit);
		}
		//Else exception?
		return ret;
	}


	private OrderedPartitionPair initializeForSearch() {
		keepGoing = true;
		pcl.reset();
		numIters = 0;

		List<List<Integer>> refinements = initialRefine();

		int numVars = pcl.getContext().size();

		OrderedPartitionPair part = new OrderedPartitionPair(refinements);
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
		//when the time comes
		part = part.refine(stats);

		return part;
	}

	//Not thread-safe
	//Returns true if found a symmetry
	protected boolean symSearchWithOrbitPruning(OrderedPartitionPair part,FoundSymmetryAction act, IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit) {
		if(!keepGoing) return false;
		boolean found = false;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}

		int k = part.getLeastABSNonUnitPart(); //Get the literals in order

		if(k == -1) {
			//When all parititions are unit paritions, we have a single permutation
			int[] permutation = part.getPermutation();
			

			for(int i = 0; i < permutation.length; i++) {
				if(permutation[i] != i) {
					int image = permutation[i];
					litOrbits.join(i,image);
					litOrbits.join(-i,-image);

					int smallest = litOrbits.getLeastEltInSet(i);

					if(i != smallest) {
						firstInOrbit.remove(i);
					}
					if(image != smallest) {
						firstInOrbit.remove(image);
					}
				}
			}

			int[] ret = getRealPerm(permutation);
			
//			System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(ret),false));
			
			if(act != null) {
				keepGoing = act.foundSymmetry(ret);
			}
			return true;
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			int topElt = part.getTopElt(k,0);

			LinkedList<Integer> definitelyNotInSameOrbitAsTop = new LinkedList<Integer>();

			//TODO: Don't use a goto, use a function
			searchBot: for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;

				
				
				int botElt = part.getBottomElt(k,j);

				if(topElt != botElt && litOrbits.sameSet(topElt,botElt)) {
					//					System.out.println("_"+topElt+"."+botElt);
					continue;
				} else
					if(topElt != botElt) { //that is, litOrbits.sameSet is false
					
					IntegralDisjointSet orbitSoFar = populateOrbitsSoFar(litOrbits, part);
					
//					if(!orbitSoFar.sameSet(topElt,botElt) && !firstInOrbit.contains(botElt)) {
//						System.out.println(litOrbits);
//						System.out.println(orbitSoFar);
//						System.out.println(part);
//						System.out.println("#"+topElt+"."+botElt);
//						continue;
//					}
					
//					for(int i : definitelyNotInSameOrbitAsTop) {
//						if(litOrbits.sameSet(i,botElt)) {
//							System.out.println(litOrbits.getSetWith(botElt));
//							System.out.println(litOrbits.getSetWith(topElt));
//							System.out.println("@"+topElt+"."+botElt);
//							continue searchBot;
//						}
//					}

//					int large = Math.max(topElt,botElt);
//					int small = Math.min(topElt,botElt);
//					
//					if(!firstInOrbit.contains(small)) {
//						System.out.println(litOrbits);
//						System.out.println("#"+topElt+"."+botElt);
//						System.out.println(litOrbits.getSetWith(botElt));
//						continue searchBot;
//					}

				}

				pcl.post();
				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(nextPart != null) {
					hasPerm = symSearchWithOrbitPruning(nextPart,act,litOrbits,firstInOrbit);
					//found |= hasPerm;
				}

				if(!hasPerm) {
					definitelyNotInSameOrbitAsTop.add(botElt);
				}


				pcl.pop();//undo any permutations before next iteration

			}
		}

		return found;
	}

	//Populates orbits as if all non-unites of the OPP are identity
	private IntegralDisjointSet populateOrbitsSoFar(
			IntegralDisjointSet litOrbits, OrderedPartitionPair part) {
		IntegralDisjointSet ret = new IntegralDisjointSet(litOrbits);
		
		
		for(int k = 0; k < part.topParts(); k++) {
			if(part.topPartSize(k) == 1) {
				int top = part.getTopElt(k,0);
				int bot = part.getBottomElt(k,0);
				if(top != bot) {
					ret.join(top,bot);
				}
			}
		}
		
		return ret;
		
	}


	private int[] getRealPerm(int[] permutation) {
		int[] perm = new int[cl.getContext().size()+1];
		
		for(int k = 1; k < perm.length; k++) {
			if(realToVirtVars[k] == -1) {
				perm[k] = k;
			} else {
				int virtVar = realToVirtVars[k];
				int image = permutation[virtVar];
				int imageVar = Math.abs(image);
				
				perm[k] = (image/imageVar)*virtToRealVars[Math.abs(permutation[virtVar])];
			}
		}
		
		return perm;
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

		nextPart = nextPart.refine(stats,newPairs); 	//**Important line**//

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


	//TODO:Gah. I should use the stats variable for this.
	//This is a little different from most refinements because
	//We are guaranteed the result will make an isomorphic OOP
	protected List<List<Integer>> initialRefine() {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();

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

		for(int k = 1; k < posFreq.length; k++) {
			int posIndex = -1;
			int negIndex = -1;
			for(int i = 0; i < ret.size() && (posIndex == -1 || negIndex == -1); i++) {
				List<Integer> list = ret.get(i);
				int varNum = list.get(0);

				//If var a goes to var b, then var -a must go to var -b
				//which means that both frequencies have to match
				if(posFreq[k] != 0 &&
						posFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& negFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(k);
					posIndex = i;
				}

				if(negFreq[k] != 0 &&
						negFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& posFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(-k);
					negIndex = i;
				}

			}

			if(posIndex == -1 && posFreq[k] != 0) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(k);
				if(negIndex == -1 && posFreq[k] == negFreq[k]) {
					newPart.add(-k);
				}
				ret.add(newPart);
			}

			if(negIndex == -1 && negFreq[k] != 0 && (posIndex != -1 || posFreq[k] != negFreq[k])) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(-k);
				ret.add(newPart);
			}
		}
		
		return ret;
	}

	private int getFreq(int varNum, int[] posFreq, int[] negFreq) {
		return varNum > 0 ? posFreq[varNum] : negFreq[-varNum];
	}
}
