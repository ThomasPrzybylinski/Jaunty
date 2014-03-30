package task.symmetry;

import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import util.IntPair;
import util.lit.SetLitCompare;

public class LeftCosetSmallerIsomorphFinder {
	private SetLitCompare comp = new SetLitCompare();
	private boolean checkInterrupt = false;

	public LeftCosetSmallerIsomorphFinder() {
	}

	//group and literalGroup must correspond!
	public LiteralPermutation getSmallerSubsetIfPossible(int[] nextCanon,
			LiteralGroup group, Set<Integer> curUsefulLits, LiteralGroup modelGroup) {
		int newSize = 0;
		//Equiv vars don't effect permutations for nextCanon
		for(int i : nextCanon) {
			if(curUsefulLits.contains(i)) {
				newSize++;
			}
		}
		
		int[] newSet = new int[newSize];
		
		int index = 0;
		for(int i : nextCanon) {
			if(curUsefulLits.contains(i)) {
				newSet[index] = i;
				index++;
			}
		}
		
		//If a literal permutation stabalizes every model, then it also stabalizes nextCanon
		//Which means it is useless
		LinkedList<LiteralPermutation> newGens = new LinkedList<LiteralPermutation>();
		Iterator<LiteralPermutation> litPerms = group.getGenerators().iterator();
		Iterator<LiteralPermutation> modPerms = modelGroup.getGenerators().iterator();
		while(litPerms.hasNext()) {
			LiteralPermutation litP = litPerms.next();
			LiteralPermutation modP = modPerms.next();
			
			if(!modP.isId()) {
				newGens.add(litP);
			}
		}
		
		
		if(newGens.size() == 0) {
			return null;
		}
		
		NaiveLiteralGroup newGroup = new NaiveLiteralGroup(newGens);
		
		return getSmallerSubsetIfPossible(newSet,newGroup);
	}

	public LiteralPermutation getSmallerSubsetIfPossible(int[] curSet, LiteralGroup autoGroup) {
		LiteralPermutation cosetPerm = autoGroup.getId();
		TreeSet<Integer> available = new TreeSet<Integer>(comp);

		for(int i : curSet) {
			available.add(i);
		}

		return getSmallerSubsetIfPossible(curSet, 0, autoGroup, cosetPerm,available);
	}

	private LiteralPermutation getSmallerSubsetIfPossible(int[] curSet, int curIndex,
			LiteralGroup stabGroup, LiteralPermutation cosetPerm, TreeSet<Integer> available) {
		if(checkInterrupt) {
			if(Thread.interrupted()) {
				throw new RuntimeException();
			}
		}

		if(curIndex >= curSet.length) return null;

		int curMapping = curIndex == 0 ? 1 : nextMapping(curSet[curIndex-1]);
		SchreierVector vec = new SchreierVector(stabGroup);

		while(comp.compare(curMapping, curSet[curIndex]) < 0) {
			Set<Integer> mappings = getPossibleMappingsTo(vec,cosetPerm, curMapping);

			for(int i : available) {
				if(mappings.contains(i)) {
					
					//We can map an elt of curSet to a set that was seen earlier.
					return cosetPerm.compose(vec.getPerm(cosetPerm.imageOf(i),curMapping));
				}
			}

			curMapping = nextMapping(curMapping);
		}

		int stabGroupMapping = curMapping;//cosetPerm.inverse().imageOf(curMapping);
		//If we cannot map to a var less than cur, must be equal
		Set<Integer> mappings = getPossibleMappingsTo(vec,cosetPerm,stabGroupMapping);
		LiteralGroup newStabGroup = null;
		
		for(int i : curSet) {
			int cosetImage = cosetPerm.imageOf(i);
			if(mappings.contains(i) && available.contains(i) && vec.sameOrbit(cosetImage,stabGroupMapping)) {
				if(newStabGroup == null) {
					newStabGroup = stabGroup.getStabSubGroup(stabGroupMapping).reduce();
				}
				LiteralPermutation newCosetPerm = cosetPerm.compose(vec.getPerm(cosetImage,stabGroupMapping));

				TreeSet<Integer> newAvailable = new TreeSet<Integer>(comp);
				newAvailable.addAll(available);
				newAvailable.remove(i);

				LiteralPermutation next = getSmallerSubsetIfPossible(curSet,curIndex+1,newStabGroup,newCosetPerm,newAvailable);

				if(next != null) {
					return next;
				}
			}
		}
		return null;

	}

	public static Set<Integer> getPossibleMappingsTo(SchreierVector vec, LiteralPermutation cosetPerm, int num) {
		TreeSet<Integer> nums = new TreeSet<Integer>();
		LiteralPermutation cosetPermInv = cosetPerm.inverse();
		for(int k = 1; k <= vec.getNumVars(); k++) {
			if(vec.sameOrbit(k,num)) {
				nums.add(cosetPermInv.imageOf(k));
				//				pairs.add(new IntPair(cosetRep.imageOf(k),i));
			}
			if(vec.sameOrbit(-k,num)) {
				nums.add(cosetPermInv.imageOf(-k));
				//				pairs.add(new IntPair(cosetRep.imageOf(k),i));
			}
		}

		return nums;
	}

	public static Set<IntPair> getPossibleMappings(SchreierVector vec, LiteralPermutation cosetRep) {
		TreeSet<IntPair> pairs = new TreeSet<IntPair>();

		for(int k = 1; k <= vec.getNumVars(); k++) {
			for(int i = k; i <= vec.getNumVars(); i++) {
				if(vec.sameOrbit(k,i)) {
					pairs.add(new IntPair(k,cosetRep.imageOf(i)));
					//					pairs.add(new IntPair(cosetRep.imageOf(k),i));
				}
			}
		}

		return pairs;
	}

	private int nextMapping(int curMapping) {
		return curMapping > 0 ? -curMapping : -curMapping +1;
	}

	public boolean isCheckInterrupt() {
		return checkInterrupt;
	}

	public void setCheckInterrupt(boolean checkInterrupt) {
		this.checkInterrupt = checkInterrupt;
	}




	//	public static void main(String[] args) {
	//		List<LiteralPermutation> perms = new ArrayList<LiteralPermutation>();
	//		perms.add(new LiteralPermutation(0,1,2,3,4));
	//		perms.add(new LiteralPermutation(0,1,2,4,3));
	//		perms.add(new LiteralPermutation(0,1,3,2,4));
	//		perms.add(new LiteralPermutation(0,1,2,4,3));
	//		//		perms.add(new LiteralPermutation(0,1,2,3,4));
	//		NaiveLiteralGroup nlg = new NaiveLiteralGroup(perms);
	//
	//
	//		int[] test = new int[]{2,3,4};
	//
	//		ExperimentalSmallerIsomorph iso = new ExperimentalSmallerIsomorph();
	//		LiteralPermutation perm = iso.getSmallerSubsetIfPossible(test,nlg);
	//		System.out.println(perm);
	//		System.out.println(Arrays.toString(perm.applySort(test)));
	//
	//		//		NaiveLiteralGroup nlg = new NaiveLiteralGroup(perms);
	//		//		SchreierVector vec = new SchreierVector(nlg);
	//		//		LiteralPermutation p1 = vec.getPerm(1,2);
	//		//		LiteralGroup stab = nlg.getStabSubGroup(1).reduce();
	//		//		stab = stab.getStabSubGroup(2).reduce();
	//		//		LiteralPermutation p2 = vec.getPerm(2,3);
	//		//		LiteralPermutation p3 = p2.compose(p1);
	//		//
	//		//		System.out.println(stab);
	//		//		System.out.println();
	//		//		System.out.println(p1);
	//		//		System.out.println(p2);
	//		//		System.out.println(p3);
	//		//		System.out.println(getPossibleMappings(stab,p1));
	//		//
	//		//		for(LiteralPermutation perm : stab.getGenerators()) {
	//		//			System.out.println(perm.compose(p3));
	//		//		}
	//
	//
	//	}
}
