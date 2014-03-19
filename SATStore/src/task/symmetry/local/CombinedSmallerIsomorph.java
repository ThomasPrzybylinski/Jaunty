package task.symmetry.local;

import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import util.IntPair;
import util.lit.SetLitCompare;

public class CombinedSmallerIsomorph {
	private SetLitCompare comp = new SetLitCompare();
	private boolean checkInterrupt = false;

	public CombinedSmallerIsomorph() {
	}

	//group and literalGroup must correspond!
	public LiteralPermutation getSmallerSubsetIfPossible(int[] nextCanon,
			List<LiteralGroup> groups, List<LiteralGroup> modelGroups) {

		Iterator<LiteralGroup> litGroups = groups.iterator();
		Iterator<LiteralGroup> modGroups = modelGroups.iterator();
		
		ArrayList<LiteralGroup> newGroups = new ArrayList<LiteralGroup>(groups.size());


		while(litGroups.hasNext()) {
			LiteralGroup group = litGroups.next();
			LiteralGroup modelGroup = modGroups.next();
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


			NaiveLiteralGroup newGroup;
			if(newGens.size() == 0) {
				ArrayList<LiteralPermutation> l = new ArrayList<LiteralPermutation>(1);
				l.add(group.getId());
				newGroup = new NaiveLiteralGroup(l);
			} else {
				newGroup = new NaiveLiteralGroup(newGens);
			}
			
			newGroups.add(newGroup);
		}

		return getSmallerSubsetIfPossible(nextCanon,newGroups);
	}

	public LiteralPermutation getSmallerSubsetIfPossible(int[] curSet, List<LiteralGroup> autoGroups) {
		LiteralPermutation cosetPerm = autoGroups.get(0).getId();
		TreeSet<Integer> available = new TreeSet<Integer>(comp);

		for(int i : curSet) {
			available.add(i);
		}

		return null;//getSmallerSubsetIfPossible(curSet, 0, autoGroups, cosetPerm,available);
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
			int stabGroupMapping = cosetPerm.inverse().imageOf(curMapping);
			Set<Integer> mappings = getPossibleMappingsTo(stabGroup,stabGroupMapping);

			for(int i : available) {
				if(mappings.contains(i)) {

					//We can map an elt of curSet to a set that was seen earlier.
					return vec.getPerm(i,stabGroupMapping).compose(cosetPerm);
				}
			}

			curMapping = nextMapping(curMapping);
		}

		int stabGroupMapping = cosetPerm.inverse().imageOf(curMapping);
		//If we cannot map to a var less than cur, must be equal
		Set<Integer> mappings = getPossibleMappingsTo(stabGroup,stabGroupMapping);

		for(int i : curSet) {
			if(mappings.contains(i) && available.contains(i) && vec.sameOrbit(i,stabGroupMapping)) {
				LiteralPermutation newCosetPerm = vec.getPerm(i,stabGroupMapping).compose(cosetPerm);
				LiteralGroup newStabGroup = stabGroup.getStabSubGroup(i).reduce();

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

	public static Set<Integer> getPossibleMappingsTo(LiteralGroup stab, int num) {
		SchreierVector vec = new SchreierVector(stab);
		TreeSet<Integer> nums = new TreeSet<Integer>();

		for(int k = 1; k <= stab.size(); k++) {
			if(vec.sameOrbit(k,num)) {
				nums.add(k);
				//				pairs.add(new IntPair(cosetRep.imageOf(k),i));
			}
			if(vec.sameOrbit(-k,num)) {
				nums.add(-k);
				//				pairs.add(new IntPair(cosetRep.imageOf(k),i));
			}
		}

		return nums;
	}

	public static Set<IntPair> getPossibleMappings(LiteralGroup stab, LiteralPermutation cosetRep) {
		SchreierVector vec = new SchreierVector(stab);
		TreeSet<IntPair> pairs = new TreeSet<IntPair>();

		for(int k = 1; k <= stab.size(); k++) {
			for(int i = k; i <= stab.size(); i++) {
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
