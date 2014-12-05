package task.symmetry.sparse;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.InvalidPermutationException;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import task.symmetry.FoundSymmetryAction;
import task.symmetry.OrderedPartitionPair;
import task.symmetry.SemiPermutableClauseList;
import task.symmetry.SymmetryStatistics;
import task.symmetry.sparse.PermCheckingClauseList.CheckDouble;
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
public class SparseSymFinder {
	private static final boolean debug = false;
	private int numTotalVars;
	public int numIters = 0;
	private PermCheckingClauseList pcl;
	private ArrayList<LiteralPermutation> knownPerms;
	int curKnownInd = -1;
	private BigInteger groupOrder = BigInteger.ZERO;
	private int gensFound = 0;

	private int maxSyms = Integer.MAX_VALUE;

	//	private Set<int[]> checkClauses;

	private SparseSymmetryStatistics stats;
	private SparseOrderedPartitionPair part;

	private boolean keepGoing = true; //setting to false stops the symmetry finding process
	private boolean doStrongRefine = false; //Doesn't seem to be worthwhile

	private int virtToRealVars[];
	private int realToVirtVars[];

	private ClauseList cl;
	private boolean doStrongValidation = false;;
	
	
	public SparseSymFinder(ClauseList cl) {
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

		pcl = new PermCheckingClauseList(virtList);
		stats = new SparseSymmetryStatistics(pcl);
	}

	
	public LiteralGroup getSymGroup() {
		List<LiteralPermutation> perms = getSyms();

		if(perms.size() == 0) {
			//So bad stopped on first refinement
			perms.add(new LiteralPermutation(realToVirtVars.length));
			groupOrder = BigInteger.ONE;
		}

		NaiveLiteralGroup group = new NaiveLiteralGroup(perms);

		return group;
	}

	public List<LiteralPermutation> getSyms() {
		final ArrayList<LiteralPermutation> ret = new ArrayList<LiteralPermutation>();
		FoundSymmetryAction act = new FoundSymmetryAction() {
			@Override
			public boolean foundSymmetry(int[] perm) {
				LiteralPermutation toAdd = new LiteralPermutation(false,perm);
				ret.add(toAdd);
				if(debug) System.out.println(ret.size());
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
		SparseOrderedPartitionPair part = initializeForSearch();

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


	private SparseOrderedPartitionPair initializeForSearch() {
		return initializeForSearch(false);
	}
	private SparseOrderedPartitionPair initializeForSearch(boolean posAndNeg) {		
		keepGoing = true;
		numIters = 0;
		groupOrder = BigInteger.ZERO;
		gensFound = 0;
		curKnownInd = knownPerms == null ? 0 : knownPerms.size()-1;

		pcl.reset();
		List<IntList> refinements = initialRefine(posAndNeg);

		int numVars = pcl.getContext().size();

		SparseOrderedPartitionPair part = new SparseOrderedPartitionPair(refinements);
		this.part = part;
		part.setNum(numVars); //initial refinement removes unused variables, so we need to make sure it outputs a valid permutation
		part.post();
		//when the time comes
		boolean ok = part.refine(stats,true);
		part.setAsBasePoint();

		if(!ok) {
			part = null;
		}


		//Be sure to permute things we've seen permuted
		//		for(int k = 0; k < part.topParts(); k++) {
		//			if(part.topPartSize(k) == 1 && !pcl.isPermuted(part.getTopElt(k,0))) {
		//				if(!pcl.permuteAndCheck(part.getTopElt(k,0),part.getBottomElt(k,0))) {
		//					return null;
		//				}
		//			}
		//		}

		return part;
	}

	private void addPerm(SparseOrderedPartitionPair part, FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit) {
		int[] permutation = part.getPermutation();


		addPerm(act, litOrbits, firstInOrbit, permutation);
	}


	private void addPerm(FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit,
			int[] permutation) {

		gensFound++;

		for(int i = 0; i < permutation.length; i++) {
			if(permutation[i] != i) {
				int image = permutation[i];

				int prevPosSmallestI = litOrbits.getLeastEltInSet(i);
				int prevNegSmallestI = litOrbits.getLeastEltInSet(-i);

				int prevPosSmallestImage = litOrbits.getLeastEltInSet(image);
				int prevNegSmallestImage = litOrbits.getLeastEltInSet(-image);

				litOrbits.join(i,image);
				litOrbits.join(-i,-image);

				int posSmallest = litOrbits.getLeastEltInSet(i);
				int negSmallest = litOrbits.getLeastEltInSet(-i);

				if(prevPosSmallestI != posSmallest) {
					firstInOrbit.remove(prevPosSmallestI);
				}

				if(prevPosSmallestImage != posSmallest) {
					firstInOrbit.remove(prevPosSmallestImage);
				}

				if(prevNegSmallestI != negSmallest) {
					firstInOrbit.remove(prevNegSmallestI);
				}

				if(prevNegSmallestImage != negSmallest) {
					firstInOrbit.remove(prevNegSmallestImage);
				}


				//				int smallest = litOrbits.getLeastEltInSet(i);
				//
				//				if(i != smallest) {
				//					firstInOrbit.remove(i);
				//				}
				//				if(image != smallest) {
				//					firstInOrbit.remove(image);
				//				}
				//				
				//				smallest = litOrbits.getLeastEltInSet(-i);
				//				
				//				if(-i != smallest) {
				//					firstInOrbit.remove(-i);
				//				}
				//				if(-image != smallest) {
				//					firstInOrbit.remove(-image);
				//				}
			}
		}


		int[] ret = getRealPerm(permutation);
		LiteralPermutation debug = new LiteralPermutation(false, ret);

		//			System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(ret),false));

		if(act != null) {
			keepGoing = act.foundSymmetry(ret);
		}
	}

	//Not thread-safe
	//Returns true if found a symmetry
	protected void symSearchWithOrbitPruning(SparseOrderedPartitionPair part,FoundSymmetryAction act, IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit, int g_k) {

		//next stabilizer chain rep
		int k = part.getLeastABSNonUnitPart(); //Get the literals in order
		if(k == -1) {
			addPerm(part,act,litOrbits,firstInOrbit); //add ID
			return;
		}
		int elt = part.getTopElt(k,0);

		int topSize = part.topPartSize(k);

		post();
		//This will usually not return null because identity is always an automorphism
		boolean unified = performUnification(part,k,0,0,topSize);

		if(unified) {
			int[] partialPerm = part.getPartialPermutation();
			boolean ok = pcl.checkPartialPerm(partialPerm);
			//			nextPart will typically not be null, but it can happen if part's top and bottom are not identical
			//			E.g. when searching for a symmetry under certain conditions
			if(ok) {
				symSearchWithOrbitPruning(part,act,litOrbits,firstInOrbit,elt);
			}
		} else {
			if(debug) System.out.print("-");
		}
		pop();

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

		boolean found = generate(part,act,litOrbits, firstInOrbit,elt);

		if(found && groupOrder.equals(BigInteger.ZERO)) {
			groupOrder = BigInteger.valueOf(litOrbits.getSetWith(elt).size());
		} else if(found) {
			//use orbit-stabilizer theorem to calc group size
			int orbSize = litOrbits.getSetWith(elt).size();
			groupOrder = groupOrder.multiply(BigInteger.valueOf(orbSize));
			if(debug && orbSize > 1) {
				System.out.println();
				System.out.println(new BigDecimal(groupOrder).round(MathContext.DECIMAL128));
			}
			
		}

		if(!keepGoing) return;

	}

	private void pop() {
		part.pop();
		pcl.pop();

	}


	private void post() {
		part.post();
		pcl.post();

	}

	public BigInteger getGroupOrder() {
		return groupOrder;
	}


	public int getGensFound() {
		return gensFound;
	}



	//True if done, otherwise false
	static int numSkipped = 0;
	private boolean generate(SparseOrderedPartitionPair part, FoundSymmetryAction act,
			IntegralDisjointSet litOrbits, TreeSet<Integer> firstInOrbit, int g_k) {
		//		System.out.println(part);
		if(!keepGoing) return true;
		numIters++;
		if(numIters%100000 == 0) {
			//System.out.println(numIters);
		}
		
		boolean found = false;

		int k = part.getLeastABSNonUnitPart(); //Get the literals in order

		if(k == -1) {
			int[] perm = part.getPermutation();
			//When all parititions are unit paritions, we have a single permutation
			if(pcl.checkPerm(perm)) {
				addPerm(act, litOrbits, firstInOrbit,perm);
				return true;
			} else {
				if(debug) System.out.println("Not ok!");
				return false;
			}
		} else {
			int topSize = part.topPartSize(k);
			int bottomSize = part.topPartSize(k);

			if(topSize != bottomSize) return false; //non-isomporphic

			int topElt = part.getTopElt(k,0);

			for(int j = 0; j < bottomSize; j++) {
				if(!keepGoing) return false;

				int botElt = part.getBottomElt(k,j);

				if(topElt == g_k) {
					if(topElt == botElt
							|| litOrbits.sameSet(topElt,botElt)
							//If pos and neg lits in same orbit, then neg will be the first
							|| (!firstInOrbit.contains(botElt) && !firstInOrbit.contains(-botElt)))
					{
						if(debug) System.out.print("/");
						continue;
					}
				} 

				post();
				boolean ok = performUnification(part,k,0,j,topSize);
				boolean hasPerm = false; 
				if(ok) {

					int[] partialPerm = part.getPartialPermutation();
					ok = pcl.checkPartialPerm(partialPerm);


					
					if(ok) {
						int[] match = part.getMatch();

						if(match != null && (
								!(debug || doStrongValidation) || pcl.checkPerm(match))) {
							if(debug) System.out.print("M");
							hasPerm = true;
							addPerm(act, litOrbits, firstInOrbit,match);
						} else if(match != null) {
							System.out.print("N");
						}



						if(!hasPerm && match != null) {
							
							//should be unnecessary to check. Simultaneous refinement
							//guarantees that if the match does not work, it's because
							//a unit partition is wrong (e.g. we made a wrong choice)
							if(debug) System.out.print("K");
						}
					} else {
						if(debug) System.out.print("+");
					}

					
					if((topElt == g_k || j != 0 || numIters%10 == 0) &&  ok && !hasPerm) {
						int[] shortCutPerm = part.getShortcutPermutation();
						
						if(pcl.checkPerm(shortCutPerm)) {
							if(debug)  System.out.print("S");
							hasPerm = true;
							addPerm(act, litOrbits, firstInOrbit,shortCutPerm);
						} else {
							if(debug)  System.out.print("C");
						}
					}

					if(ok && !hasPerm) {
						if(debug) System.out.print('^');
						hasPerm = generate(part,act,litOrbits,firstInOrbit,g_k);
					}
				} else {
					if(debug) System.out.print("-");
				}

				pop();

				if(hasPerm) {
					if(topElt == g_k) {
						found = true;
					} else {
						return true;
					}
				}

			}
		}
		if(debug && !found) System.out.print('X');
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

		SparseOrderedPartitionPair nextPart = part;
		nextPart.assignIndeciesToUnitPart(partIndex,topIndex, botIndex);
		boolean ok = nextPart.assignEltsToUnitPart(-topElt,-botElt);
		if(ok == false) return false; //Cannot make dual permutation work


		ok = nextPart.refine(stats); 	//**Important line**//
		//		System.out.println(nextPart);
		//		System.out.println();

		return ok;
	}

	public void addKnownSubgroup(LiteralGroup g) {
		knownPerms = new ArrayList<LiteralPermutation>();

		for(LiteralPermutation p : g.getGenerators()) {
			int[] array = p.asArray();
			int[] internal = getInternalPerm(array);
			try {
				knownPerms.add(new LiteralPermutation(internal));
			} catch(InvalidPermutationException pe) {
				new SparseSymFinder(cl);
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
	protected List<IntList> initialRefine() {
		return initialRefine(false);
	}

	//If posAndNeg true, add both pos and neg lits if one exists
	protected List<IntList> initialRefine(boolean posAndNeg) {
		if(doStrongRefine) {
			return getStrongInitialRefine(posAndNeg);
		} else {
			return getWeakInitialRefine(posAndNeg);
		}
	}


	private List<IntList> getStrongInitialRefine(boolean posAndNeg) {
		List<IntList> ret = new ArrayList<IntList>();

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
				IntList list = ret.get(i);
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
				ArrayIntList newPart = new ArrayIntList();
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
				ArrayIntList newPart = new ArrayIntList();
				newPart.add(-k);
				ret.add(newPart);
			}
		}

		return ret;
	}


	private List<IntList> getWeakInitialRefine(boolean posAndNeg) {
		List<IntList> ret = new ArrayList<IntList>();

		int numVars = pcl.getContext().size();
		int[] posFreq = new int[numVars+1];
		int[] negFreq = new int[numVars+1];

		for(int[] clause : pcl.getClauses()) {
			int len = clause.length;
			for(int i : clause) {
				int[] freqArray;
				if(i > 0) {
					freqArray = posFreq;
				} else {
					freqArray = negFreq;
				}

				freqArray[Math.abs(i)]++; // += (len);
			}
		}



		for(int k = 1; k < posFreq.length; k++) {
			int posIndex = -1;
			int negIndex = -1;

			boolean varExistsAndThatMatters = posAndNeg && (posFreq[k] != 0 || negFreq[k] != 0);

			for(int i = 0; i < ret.size() && (posIndex == -1 || negIndex == -1); i++) {
				IntList list = ret.get(i);
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
				ArrayIntList newPart = new ArrayIntList();
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
				ArrayIntList newPart = new ArrayIntList();
				newPart.add(-k);
				ret.add(newPart);
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

	private SparseOrderedPartitionPair breakByOrbits(SparseOrderedPartitionPair part, SchreierVector vec) {
		List<IntList> ret = new ArrayList<IntList>();		
		for(int k = 0; k < part.topParts(); k++) {
			ArrayList<IntList> cur = new ArrayList<IntList>();
			cur.add(new ArrayIntList());

			cur.get(0).add(part.getTopElt(k,0));

			for(int i = 1; i < part.topPartSize(k); i++) {
				int elt = part.getTopElt(k,i);
				boolean added = false;
				for(IntList newSlice : cur) {
					if(vec.sameOrbit(newSlice.get(0),elt)) {
						newSlice.add(elt);
						added = true;
						break;
					}
				}

				if(!added) {
					IntList split = new ArrayIntList();
					split.add(elt);
					cur.add(split);
				}

			}

			ret.addAll(cur);
		}

		return new SparseOrderedPartitionPair(ret);
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
	
	public boolean isDoStrongValidation() {
		return doStrongValidation;
	}

	public void setDoStrongValidation(boolean doStrongValidation) {
		this.doStrongValidation = doStrongValidation;
	}



}
