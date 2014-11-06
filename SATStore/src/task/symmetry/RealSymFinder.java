package task.symmetry;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.InvalidPermutationException;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import util.IntegralDisjointSet;
import util.PermutationUtil;
import util.StablePermComparator;
import util.lit.LitSorter;
import util.lit.LitUtil;
import util.lit.SetLitCompare;

/*
	Created so that SimpleSymFinder remains backwards compatible.
	This one always does orbit pruning, is the basis for trying to find local symmetries, eventually
 */
public class RealSymFinder {
	private int numTotalVars;
	public int numIters = 0;
	private SemiPermutableClauseList pcl;
	private ArrayList<LiteralPermutation> knownPerms;
	int curKnownInd = -1;
	
	private int maxSyms = Integer.MAX_VALUE;
	
	//	private Set<int[]> checkClauses;

	private SymmetryStatistics stats;

	private boolean keepGoing = true; //setting to false stops the symmetry finding process
	private boolean doStrongRefine = false; //Doesn't seem to be worthwhile

	private int virtToRealVars[];
	private int realToVirtVars[];

	private ClauseList cl;
	public RealSymFinder(ClauseList cl) {
		this.cl = cl;
		this.numTotalVars = cl.getContext().size();

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


	public LiteralGroup getSymGroup() {
		List<LiteralPermutation> perms = getSyms();
		
		if(perms.size() == 0) {
			//So bad stopped on first refinement
			perms.add(new LiteralPermutation(pcl.getContext().size()));
		}
		
		NaiveLiteralGroup group = new NaiveLiteralGroup(perms);

		return group;
	}

	public List<LiteralPermutation> getSyms() {
		final ArrayList<LiteralPermutation> ret = new ArrayList<LiteralPermutation>();
		FoundSymmetryAction act = new FoundSymmetryAction() {
			@Override
			public boolean foundSymmetry(int[] perm) {
				LiteralPermutation toAdd = new LiteralPermutation(perm);
				ret.add(toAdd);
				return ret.size() != maxSyms;
			}
		};

		getSymOrbits(act);

		return ret;
	}

	public IntegralDisjointSet getSymOrbits() {
		return getSymOrbits(null);
	}

	public IntegralDisjointSet getSymOrbits(FoundSymmetryAction act) {
		OrderedPartitionPair part = initializeForSearch();

		int numVars = pcl.getContext().size();

		IntegralDisjointSet ret = new IntegralDisjointSet(-numVars,numVars);
		//LitsSet firstInOrbit = new LitsSet(numVars);

		TreeSet<Integer> firstInOrbit = setupFirstInOrbit(numVars);

		if(part != null) {		
			symSearchWithOrbitPruning(part,act,ret,firstInOrbit,0);
		}
		//Else exception?
		return ret;
	}


	private TreeSet<Integer> setupFirstInOrbit(int numVars) {
		TreeSet<Integer> firstInOrbit = new TreeSet<Integer>();

		for(int k = -numVars; k <= numVars; k++) {
			firstInOrbit.add(k);
		}
		return firstInOrbit;
	}


	private OrderedPartitionPair initializeForSearch() {
		return initializeForSearch(false);
	}
	private OrderedPartitionPair initializeForSearch(boolean posAndNeg) {		
		keepGoing = true;
		pcl.reset();
		pcl.post();
		numIters = 0;
		curKnownInd = knownPerms == null ? 0 : knownPerms.size()-1;

		List<List<Integer>> refinements = initialRefine(posAndNeg);

		int numVars = pcl.getContext().size();

		OrderedPartitionPair part = new OrderedPartitionPair(refinements);
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
		//when the time comes
		part = part.refine(stats);
		
		
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

	private void addPerm(OrderedPartitionPair part, FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit) {
		int[] permutation = part.getPermutation();


		addPerm(act, litOrbits, firstInOrbit, permutation);
	}


	private void addPerm(FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit,
			int[] permutation) {
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
				
				smallest = litOrbits.getLeastEltInSet(-i);
				
				if(-i != smallest) {
					firstInOrbit.remove(-i);
				}
				if(-image != smallest) {
					firstInOrbit.remove(-image);
				}
			}
		}

		int[] ret = getRealPerm(permutation);
		LiteralPermutation debug = new LiteralPermutation(ret);

		//			System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(ret),false));

		if(act != null) {
			keepGoing = act.foundSymmetry(ret);
		}
	}

	//Not thread-safe
	//Returns true if found a symmetry
	protected void symSearchWithOrbitPruning(OrderedPartitionPair part,FoundSymmetryAction act, IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit, int g_k) {

		//next stabilizer chain rep
		int k = part.getLeastABSNonUnitPart(); //Get the literals in order
		if(k == -1) {
			addPerm(part,act,litOrbits,firstInOrbit); //add ID
			return;
		}
		int elt = part.getTopElt(k,0);
		pcl.post();

		int topSize = part.topPartSize(k);
		//This will usually not return null because identity is always an automorphism
		OrderedPartitionPair nextPart = performUnification(part,k,0,0,topSize);

		if(nextPart != null) {
			//			nextPart will typically not be null, but it can happen if part's top and bottom are not identical
			//			E.g. when searching for a symmetry under certain conditions
			symSearchWithOrbitPruning(nextPart,act,litOrbits,firstInOrbit,elt);
		}

		pcl.pop();

		if(knownPerms != null && knownPerms.size() > 0 && curKnownInd >= 0) {
			LiteralPermutation cur = knownPerms.get(curKnownInd);

			while(cur.getFirstUnstableVar() >= Math.abs(elt) && curKnownInd >= 0) {
				addPerm(act,litOrbits,firstInOrbit,cur.asArray());
				curKnownInd--;
				if(curKnownInd >= 0) {
					cur = knownPerms.get(curKnownInd);
				}
			}
		}


		generate(part,act,litOrbits, firstInOrbit,elt);

		if(!keepGoing) return;

	}

	//True if done, otherwise false
	private boolean generate(OrderedPartitionPair part, FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit, int g_k) {
		
		if(!keepGoing) return true;
		boolean found = false;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}

		int k = part.getLeastABSNonUnitPart(); //Get the literals in order

		if(k == -1) {
			//When all parititions are unit paritions, we have a single permutation
			addPerm(part, act, litOrbits, firstInOrbit);
			return true;
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			int topElt = part.getTopElt(k,0);

			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;

				int botElt = part.getBottomElt(k,j);

				if(topElt == g_k) {
					if(topElt == botElt) continue;
					if(litOrbits.sameSet(topElt,botElt)) continue;
					if(!firstInOrbit.contains(botElt)) continue;
				}

				pcl.post();
				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(nextPart != null) {
					hasPerm = generate(nextPart,act,litOrbits,firstInOrbit,g_k);
				}

				pcl.pop();//undo any permutations before next iteration
				if(hasPerm && topElt != g_k) {
					return true;
				}

			}
		}

		return found;
	}

	private int[] getRealPerm(int[] permutation) {
		return translate(permutation,realToVirtVars,virtToRealVars);
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

	private int[] getInternalPerm(int[] permutation) {
		int[] perm = new int[virtToRealVars.length];

		for(int k = 1; k < virtToRealVars.length; k++) {
			int transVar = virtToRealVars[k];
			int image = permutation[transVar];
			int imageVar = Math.abs(image);
			perm[k] = (image/imageVar)*realToVirtVars[Math.abs(image)];
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

	public void addKnownSubgroup(LiteralGroup g) {
		knownPerms = new ArrayList<LiteralPermutation>();

		for(LiteralPermutation p : g.getGenerators()) {
			int[] array = p.asArray();
			int[] internal = getInternalPerm(array);
			try {
				knownPerms.add(new LiteralPermutation(internal));
			} catch(InvalidPermutationException pe) {
				new RealSymFinder(cl);
				internal = getInternalPerm(array);
			}
		}
		Collections.sort(knownPerms, new StablePermComparator());

		if(knownPerms.get(0).isId()) {
			knownPerms.remove(0);
		}
		//		System.out.println(knownPerms);
	}


	//TODO:Gah. I should use the stats variable for this.
	//This is a little different from most refinements because
	//We are guaranteed the result will make an isomorphic OOP
	protected List<List<Integer>> initialRefine() {
		return initialRefine(false);
	}

	//If posAndNeg true, add both pos and neg lits if one exists
	protected List<List<Integer>> initialRefine(boolean posAndNeg) {
		if(doStrongRefine) {
			return getStrongInitialRefine(posAndNeg);
		} else {
			return getWeakInitialRefine(posAndNeg);
		}
	}


	private List<List<Integer>> getStrongInitialRefine(boolean posAndNeg) {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();

		int numVars = pcl.getContext().size();
		int[] litFreq = new int[2*numVars+1];

		int[][] adjFreq = new int[2*numVars+1][2*numVars+1];
		
		for(int[] clause : pcl.getClauses()) {
			for(int i : clause) {
				int index = LitUtil.getIndex(i,numVars);
				litFreq[index]++;
				for(int j : clause) {
					adjFreq[index][LitUtil.getIndex(j,numVars)]++;
				}
			}
		}
		
		for(int k = 0; k < adjFreq.length; k++) {
			Arrays.sort(adjFreq[k]); //Since lits can change via symmetries, all that matters is that the
									//distribution is the same
		}
		
		for(int k = 1; k <= numVars; k++) {
			int posIndex = LitUtil.getIndex(k,numVars);
			int negIndex = LitUtil.getIndex(-k,numVars);;
			
			boolean varExistsAndThatMatters = posAndNeg && (litFreq[posIndex] != 0 || litFreq[negIndex] != 0);
			
			int posPart = -1;
			int negPart = -1;
			for(int i = 0; i < ret.size() && (posPart == -1 || negPart == -1); i++) {
				List<Integer> list = ret.get(i);
				int partRep = list.get(0);
				int repIndex = LitUtil.getIndex(partRep,numVars);
				int negRepIndex = LitUtil.getIndex(-partRep,numVars);

				//If var a goes to var b, then var -a must go to var -b
				//which means that both frequencies have to match
				if((litFreq[posIndex] != 0 || varExistsAndThatMatters)) {
					
					if(Arrays.equals(adjFreq[posIndex],adjFreq[repIndex])
							&& Arrays.equals(adjFreq[negIndex],adjFreq[negRepIndex])) {
						list.add(k);
						posPart = i;
					}
				}
				
				if((litFreq[negIndex] != 0 || varExistsAndThatMatters)) {
					
					if(Arrays.equals(adjFreq[negIndex],adjFreq[repIndex])
							&& Arrays.equals(adjFreq[posIndex],adjFreq[negRepIndex])) {
						list.add(-k);
						negPart = i;
					}
				}
			}

			
			boolean addPos = posPart == -1 && (litFreq[posIndex] != 0 || varExistsAndThatMatters);

			//Will also add negative lit, if freqs are the same
			if(addPos) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(k);
				if(negPart == -1 && Arrays.equals(adjFreq[posIndex],adjFreq[negIndex])) {
					//Add neg since its the same partition
					negPart = ret.size();
					newPart.add(-k);
				}
				ret.add(newPart);
			}

			boolean addNeg = negPart == -1 && (litFreq[negIndex] != 0 || varExistsAndThatMatters);
			if(addNeg) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(-k);
				ret.add(newPart);
			}
		}
		
		return ret;
	}


	private List<List<Integer>> getWeakInitialRefine(boolean posAndNeg) {
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
			
			boolean varExistsAndThatMatters = posAndNeg && (posFreq[k] != 0 || negFreq[k] != 0);
			
			for(int i = 0; i < ret.size() && (posIndex == -1 || negIndex == -1); i++) {
				List<Integer> list = ret.get(i);
				int varNum = list.get(0);

				//If var a goes to var b, then var -a must go to var -b
				//which means that both frequencies have to match
				if((posFreq[k] != 0 || varExistsAndThatMatters) &&
						posFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& negFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(k);
					posIndex = i;
				}

				if((negFreq[k] != 0 || varExistsAndThatMatters) &&
						negFreq[k] == getFreq(varNum,posFreq,negFreq)
						&& posFreq[k] == getFreq(varNum,negFreq,posFreq)) {
					list.add(-k);
					negIndex = i;
				}

			}

			
			boolean addPos = posIndex == -1 && (posFreq[k] != 0 || varExistsAndThatMatters);

			//Will also add negative lit, if freqs are the same
			if(addPos) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(k);
				if(negIndex == -1 && posFreq[k] == negFreq[k]) {
					//Add neg since its the same partition
					negIndex = ret.size();
					newPart.add(-k);
				}
				ret.add(newPart);
			}


			boolean addNeg = negIndex == -1 && (negFreq[k] != 0 || varExistsAndThatMatters);
			if(addNeg) {
				ArrayList<Integer> newPart = new ArrayList<Integer>();
				newPart.add(-k);
				ret.add(newPart);
			}
		}

		return ret;
	}

	@Deprecated
	//TODO: I need to translate input into internal representations
	public int[] getSmallerSubsetIfPossible(int[] curSubset, LiteralGroup curGroup) {
		LiteralPermutation perm = getPermForSmallerIfPossible(curSubset, curGroup);
		
		if(perm != null) {
			int[] ret = PermutationUtil.permute(curSubset,perm.asArray());
			LitSorter.inPlaceSort(ret);
			return ret;
		}
		
		return null;
	}
	
	@Deprecated
	public LiteralPermutation getPermForSmallerIfPossible(int[] curSubset, LiteralGroup curGroup) {
		if(curGroup == null) {
			curGroup = getSymGroup();
		}

		//Translate curGroup into an internal group
		//At the same time translate curSubset into an internal subset
		LiteralGroup interalGroup = translateGroup(curGroup);
		SchreierVector orbits = new SchreierVector(interalGroup);
		
		int[] internalSubset = translateCurSubset(curSubset);

		OrderedPartitionPair part = initializeForSearch(true);
		part = breakByOrbits(part,orbits);

		int numVars = pcl.getContext().size();

		IntegralDisjointSet orbit = new IntegralDisjointSet(-numVars,numVars); //orbits.transcribeOrbits();


		Comparator<Integer> litComp = new SetLitCompare();

		LiteralPermutation perm = null;
		if(part != null) {
			//The permutation returned is a permutation ont he original literals
			perm = getSmallerPerm(part,orbit,internalSubset,litComp,0);
		}

		
		return perm;
	}



	//Algo:
	//For each element n of the subset
	//	Take each element elt of the parition in (n-1,n] (min 1, max numVars)
	//	Try to map elt to elements of curSubset in increasing order, and then try to find
	//	a permutation that corresponds to that mapping.
	//		If we succeed, we have found a permutation the gives us a lexicographically better set
	//	When we reach n, we try every possiblity that does not include n.
	//	If still no perm, we know that n maps to n, so change partition to reflect that
	private LiteralPermutation getSmallerPerm(OrderedPartitionPair part,
			IntegralDisjointSet litOrbits, int[] curSubset,Comparator<Integer> litComp, int eltInd) {
		if(eltInd >= curSubset.length) return null;

		TreeSet<Integer> setElts = new TreeSet<Integer>();
		for(int curElt : curSubset) {
			setElts.add(curElt);
		}

		
		int curElt = curSubset[eltInd];
		int curMapping = eltInd == 0 ? 1 : nextMapping(curSubset[eltInd-1]);
		//Try to find set that is mapped to something less than curElt
		for(; litComp.compare(curMapping,curElt) < 0; curMapping = nextMapping(curMapping)) {
			int curPart = part.getBottomPartWithElt(curMapping);
			int partSize = part.topPartSize(curPart);

			//Var exists, and not mapped from previous decisions
			if(curPart != -1 && partSize != 1) {
				int partInd = part.getBottomPartIndexOfElt(curPart,curMapping);

				for(int k = 0; k < partSize; k++) {
					int topElt = part.getTopElt(curPart,k);

					if(setElts.contains(topElt)) {
						pcl.post();

						//Require setting topElt to k
						OrderedPartitionPair nextPart = performUnification(part,curPart,k,partInd,partSize);

						if(nextPart != null) {

							//Now see if a permutation exists that allows this
							final ArrayList<int[]> ret = new ArrayList<int[]>();
							FoundSymmetryAction act = new FoundSymmetryAction() {
								@Override
								public boolean foundSymmetry(int[] perm) {
									ret.add(perm);
									return false; //only need 1 perm
								}
							};

							symSearchWithOrbitPruning(nextPart,act,
									new IntegralDisjointSet(litOrbits)
							,setupFirstInOrbit(litOrbits.getMax()),
							topElt);

							//if there is a permutation, we are done
							if(ret.size() > 0) {
								pcl.pop();
								return new LiteralPermutation(ret.get(0));
							}
						}
						
						pcl.pop();
					}
				}
			}
		}

		//If cannot find such a set, we know that someone maps to elt, so we search for the cases where it
		//is mapped to another elt, unless the cur elt is the final elt of the set
		int to = curElt;
		int toPart = part.getBottomPartWithElt(to);
		int toInd = part.getBottomPartIndexOfElt(toPart,to);
		int toSize = part.bottomPartSize(toPart);

		if(toSize != 1) {
			for(int k = 0; k < curSubset.length; k++) {
				int from = curSubset[k];
				int fromPart = part.getTopPartWithElt(from);

				//If we can map them
				if(fromPart == toPart) {
					int fromInd = part.getTopPartIndexOfElt(fromPart,from);
					
					pcl.post();

					//Require setting topElt to k
					OrderedPartitionPair nextPart = performUnification(part,toPart,fromInd,toInd,toSize);
					
					if(nextPart != null) {
						LiteralPermutation perm = getSmallerPerm(nextPart,litOrbits,curSubset,litComp,eltInd+1);
						
						if(perm != null) {
							pcl.pop();
							return perm;
						}
					}
					
					pcl.pop();

				}
			}
		}

		return null;
	}

	private int nextMapping(int curMapping) {
		return curMapping > 0 ? -curMapping : -curMapping +1;
	}

	private LiteralGroup translateGroup(LiteralGroup curGroup) {
		ArrayList<LiteralPermutation> newGens = new ArrayList<LiteralPermutation>(curGroup.getGenerators().size());
		for(LiteralPermutation perm : curGroup.getGenerators()) {
			newGens.add(new LiteralPermutation(getInternalPerm(perm.asArray())));
		}
		
		LiteralGroup ret = new NaiveLiteralGroup(newGens);
		return ret;
	}
	
	private int[] translateCurSubset(int[] curSubset) {
		int size = 0;
		
		for(int i : curSubset) {
			if(realToVirtVars[Math.abs(i)] != -1) {
				size++;
			}
		}
		
		int[] ret = new int[size];
		int index = 0;
		
		for(int i : curSubset) {
			if(realToVirtVars[Math.abs(i)] != -1) {
				ret[index] = (i/Math.abs(i))*realToVirtVars[Math.abs(i)];
				index++;
			}
		}
		
		return ret;
	}


	//	private LiteralPermutation getSmallerPerm(OrderedPartitionPair part,
	//			IntegralDisjointSet litOrbits, int[] curSubset,Comparator<Integer> litComp, int eltInd) {
	//		//next stabilizer chain rep
	//		//		int k = part.getLeastABSNonUnitPart(); //Get the literals in order
	//		if(eltInd >= curSubset.length) {
	//			return null;
	//		}
	//
	//		int elt = curSubset[eltInd];
	//		int partK = part.getBottomPartWithElt(elt);
	////		if(partK == -1) { //sometimes that elt doesn't exist
	////			elt = -elt;
	////			partK = part.getBottomPartWithElt(elt);
	////		}
	//		
	//
	//
	//		LiteralPermutation perm = null;
	//		if(partK != -1) {
	//
	//			int partInd = part.getBottomPartIndexOfElt(partK,elt);
	//
	//			int topSize = part.topPartSize(partK);
	//
	//			for(int k = 0; k < topSize; k++) {
	//				int topElt = part.getTopElt(partK,k);
	//
	//				if(litComp.compare(elt,topElt) < 0) {
	//					pcl.post();
	//					
	//					//This will never return null because identity is always an automorphism
	//					OrderedPartitionPair nextPart = performUnification(part,partK,partInd,partInd,topSize);
	//
	//					final ArrayList<int[]> ret = new ArrayList<int[]>();
	//					FoundSymmetryAction act = new FoundSymmetryAction() {
	//						@Override
	//						public boolean foundSymmetry(int[] perm) {
	//							ret.add(perm);
	//							return false; //only need 1 perm
	//						}
	//					};
	//
	//					symSearchWithOrbitPruning(nextPart,act,
	//												new IntegralDisjointSet(litOrbits)
	//												,setupFirstInOrbit(litOrbits.getMax()),
	//												topElt);
	//
	//					pcl.pop();
	//
	//					if(ret.size() > 0) {
	//						perm = new LiteralPermutation(ret.get(0));
	//						return perm;
	//					}				
	//				}
	//				
	//				if(perm == null) {
	//					pcl.post();
	//					int part2 = part.getTopPartWithElt(elt);
	//					
	//					if(part2 == partK) {
	//						int topInd =  part.getTopPartIndexOfElt(partK,elt);
	//						OrderedPartitionPair nextPart = part;
	//						
	//						if(part.topPartSize(partK) > 1) {
	//							nextPart = performUnification(part,partK,topInd,partInd,topSize);
	//						}
	//						
	//						if(nextPart != null) {
	//							perm = getSmallerPerm(nextPart,litOrbits,curSubset,litComp,eltInd+1);
	//						}
	//					}
	//					
	//					pcl.pop();
	//				}
	//			}
	//		}
	//
	//		return perm;
	//	}




	//True if done, otherwise false
//	private int[] generateSmallerSym(OrderedPartitionPair part, IntegralDisjointSet litOrbits, int g_k, int[] curSubset) {
//		if(!keepGoing) return null;
//
//		numIters++;
//		if(numIters%100000 == 0) {
//			//System.out.println(numIters);
//		}
//
//		int k = part.getLeastABSNonUnitPart(); //Get the literals in order
//
//		if(k == -1) {
//			//When all parititions are unit paritions, we have a single permutation
//			int[] perm = part.getPermutation();
//			int[] candidate = PermutationUtil.permute(curSubset,perm);
//
//			if((new ModelComparator()).compare(candidate,curSubset) < 0) {
//				return perm;
//			}
//		} else {
//			int topSize = part.topPartSize(k);
//			int bottomSize = part.topPartSize(k);
//
//			if(topSize != bottomSize) return null; //non-isomporphic
//
//			int topElt = part.getTopElt(k,0);
//
//			for(int j = 0; j < bottomSize; j++) {
//				if(!keepGoing) return null;
//
//				int botElt = part.getBottomElt(k,j);
//
//				if(topElt == g_k) {
//					if(topElt == botElt) continue;
//					if(topElt > botElt) continue;
//					if(litOrbits.sameSet(topElt,botElt)) continue;
//				}
//
//				pcl.post();
//				OrderedPartitionPair nextPart = performUnification(part,k,0,j,topSize);
//				int[] perm = null;
//				if(nextPart != null) {
//					perm = generateSmallerSym(nextPart,litOrbits,g_k,curSubset);
//				}
//
//				pcl.pop();//undo any permutations before next iteration
//				if(perm != null) {
//					return perm;
//				}
//
//			}
//		}
//
//		return null;
//	}

	private OrderedPartitionPair breakByOrbits(OrderedPartitionPair part, SchreierVector vec) {
		List<List<Integer>> ret = new ArrayList<List<Integer>>();		
		for(int k = 0; k < part.topParts(); k++) {
			ArrayList<ArrayList<Integer>> cur = new ArrayList<ArrayList<Integer>>();
			cur.add(new ArrayList<Integer>());

			cur.get(0).add(part.getTopElt(k,0));

			for(int i = 1; i < part.topPartSize(k); i++) {
				int elt = part.getTopElt(k,i);
				boolean added = false;
				for(ArrayList<Integer> newSlice : cur) {
					if(vec.sameOrbit(newSlice.get(0),elt)) {
						newSlice.add(elt);
						added = true;
						break;
					}
				}

				if(!added) {
					ArrayList<Integer> split = new ArrayList<Integer>();
					split.add(elt);
					cur.add(split);
				}

			}

			ret.addAll(cur);
		}

		return new OrderedPartitionPair(ret);
	}

	private int getFreq(int varNum, int[] posFreq, int[] negFreq) {
		return varNum > 0 ? posFreq[varNum] : negFreq[-varNum];
	}


	public boolean isDoStrongRefine() {
		return doStrongRefine;
	}


	public void setDoStrongRefine(boolean doStrongRefine) {
		this.doStrongRefine = doStrongRefine;
	}


	public int getMaxSyms() {
		return maxSyms;
	}


	public void setMaxSyms(int maxSyms) {
		this.maxSyms = maxSyms;
	}
	
	
}
