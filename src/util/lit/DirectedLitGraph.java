package util.lit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import group.LiteralPermutation;
import group.PairSchreierVector;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;

//TODO: Create dual Schrier Vector to reduce the calls to getModelPart, which is slow
public class DirectedLitGraph {
	private List<PairSchreierVector> graphLevels;
	private int numVars;
	//	private int curLevel = -1; //First level 0; global symmetry
	//CurLevel is the length of the filter to validate - 1
	private int validateLevel = -1;
	private LiteralPermutation validateModelPerm = null;

	private static SetLitCompare comp = new SetLitCompare();

	public DirectedLitGraph(int numVars) {
		this.numVars = numVars;
		graphLevels = new ArrayList<PairSchreierVector>();
	}

	public void push(PairSchreierVector curOrbits) {
		graphLevels.add(curOrbits);
	}

	public PairSchreierVector getLast() {
		return graphLevels.get(graphLevels.size()-1);
	}
	
	public void pop() {
		graphLevels.remove(graphLevels.size()-1);
	}

	public boolean isValidMapping(int l1, int l2) {
		boolean[] validMappings = new boolean[2*numVars+1];
		validMappings[LitUtil.getIndex(l1,numVars)] = true;

		for(int k = graphLevels.size()-1; k >= 0; k--) {
			boolean[] nextValidMappings = new boolean[2*numVars+1];
			PairSchreierVector curVec = graphLevels.get(k);

			for(int i = 0; i < validMappings.length; i++) {
				if(validMappings[i]) {
					nextValidMappings[i] = true; //id
					int litI = LitUtil.getLit(i,numVars);

					for(int j = 0; j < validMappings.length; j++) {
						int litJ = LitUtil.getLit(j,numVars);
						if(curVec.sameOrbit(litI,litJ)) {
							nextValidMappings[j] = true;
						}
					}
				}
			}

			validMappings = nextValidMappings;
			if(validMappings[LitUtil.getIndex(l2,numVars)]) {
				return true;
			}
		}
		return false;
	}


	public int getValidateLevel() {
		return validateLevel;
	}

	public LiteralPermutation getValidateModelPerm() {
		return validateModelPerm;
	}

	public boolean validate(int[] nextFilter) {
		return validatePerm(nextFilter,null) != null;
	}

	public LiteralPermutation validatePerm(int[] nextFilter, LocalSymClauses raClauses) {
		LiteralPermutation[] validMappings = new LiteralPermutation[2*numVars+1];
		LiteralPermutation[] modelMappings = null;
		if(raClauses != null) {
			modelMappings = new LiteralPermutation[2*numVars+1];
		}
		int mappedLit = nextFilter[nextFilter.length-1];

		validMappings[LitUtil.getIndex(mappedLit,numVars)] = new LiteralPermutation(numVars);

		if(modelMappings != null) {
			modelMappings[LitUtil.getIndex(mappedLit,numVars)] = new LiteralPermutation(raClauses.numTotalModels());
		}


		for(int k = graphLevels.size()-1; k >= 0; k--) {
			PairSchreierVector curVec = graphLevels.get(k);

			//Populate new connected literals
			populate(validMappings, modelMappings, curVec, nextFilter[k]);

			for(int i = 0; i < validMappings.length; i++) {
				int litI = LitUtil.getLit(i,numVars);
				int curFilLit = nextFilter[k];
				if(validMappings[i] != null && (Math.abs(litI) < Math.abs(curFilLit) ||
						(Math.abs(litI) == Math.abs(curFilLit) && (litI > curFilLit)))) {
					LiteralPermutation ret = validMappings[i]; 
					validateModelPerm = modelMappings == null ? null : modelMappings[i];
					validateLevel = k;
					return ret;
				}
			}
		}
		return null;

	}

	private void populate(LiteralPermutation[] validMappings,
			LiteralPermutation[] modelMappings, PairSchreierVector curVec, int lowerLit) {
		for(int i = 0; i < validMappings.length; i++) {
			if(validMappings[i] == null) {
				int litI = LitUtil.getLit(i,numVars);

				for(int j = 0; j < validMappings.length; j++) {
					if(validMappings[j] != null) {
						int litJ = LitUtil.getLit(j,numVars);
						if(curVec.sameOrbit(litJ,litI)) {
							validMappings[i] = validMappings[j].compose(curVec.getPerm(litJ,litI));
							if(modelMappings != null) {
								LiteralPermutation toCompose = curVec.getModelPart(); //raClauses.getModelPart(curVec.getPerm(litJ,litI));//
								modelMappings[i] = modelMappings[j].compose(toCompose);

								if(Math.abs(litI) < Math.abs(lowerLit)) return; //We have found a prunable one
							}
							break;
						}
					}
				}
			}
		}
	}
	
	private class ComputeThing {
		int[] set;
		LiteralPermutation varPerm;
		LiteralPermutation modPerm;
		public ComputeThing(int[] set, LiteralPermutation varPerm,
				LiteralPermutation modPerm) {
			super();
			this.set = set;
			this.varPerm = varPerm;
			this.modPerm = modPerm;
		}
		
		
	}

	public LiteralPermutation doFullPruning(int[] nextFilter, LocalSymClauses raClauses) {
		Comparator<int[]> mileComp = new LocalInterpComp();
		LitsSet seen = new LitsSet(numVars);
		seen.add(nextFilter);
		
		LinkedList<ComputeThing> toCompute = new LinkedList<ComputeThing>();
		LinkedList<ComputeThing> toComputeNext = new LinkedList<ComputeThing>();
		
		toCompute.add(new ComputeThing(nextFilter,graphLevels.get(0).getGroup().getId(),graphLevels.get(0).getModelGroup().getId()));
		for(int k = graphLevels.size()-1; k >= 0; k--) {
			PairSchreierVector curVec = graphLevels.get(k);
			
			while(!toCompute.isEmpty()) {
				ComputeThing next = toCompute.poll();
				toComputeNext.add(next);
				
				Iterator<LiteralPermutation> modIter = curVec.getModelGroup().getGenerators().iterator();
				for(LiteralPermutation perm : curVec.getGroup().getGenerators()) {
					LiteralPermutation modPerm = modIter.next();
					int[] nextSet = perm.applySort(next.set);
					
					if(!seen.contains(nextSet)) {
						seen.add(nextSet);
						ComputeThing nextThing = new ComputeThing(nextSet,next.varPerm.compose(perm), next.modPerm.compose(modPerm));
						if(mileComp.compare(nextSet,nextFilter) < 0) {
							validateModelPerm = nextThing.modPerm;
							return nextThing.varPerm;
						}
						toCompute.add(nextThing);
					}
				}
			}
			
			toCompute = toComputeNext;
			toComputeNext = new LinkedList<ComputeThing>();
		}
		
		return null;
		
	}
	
	public LiteralPermutation doFullPruningPROTOTYPE(int[] nextFilter, LocalSymClauses raClauses) {
		LiteralPermutation cosetPerm = graphLevels.get(0).getGroup().getId();
		LiteralPermutation cosetModPerm = graphLevels.get(0).getModelGroup().getId();
		TreeSet<Integer> available = new TreeSet<Integer>(comp);

		for(int i : nextFilter) {
			available.add(i);
		}

		return getSmallerSubsetIfPossible(nextFilter, 0, cosetPerm,cosetModPerm,available);
	}

	private LiteralPermutation getSmallerSubsetIfPossible(int[] curSet,
			int curIndex, LiteralPermutation cosetPerm, LiteralPermutation cosetModPerm, TreeSet<Integer> available) {
		if(curIndex >= curSet.length) return null;

		int curSetLit = curSet[curIndex];
		int[] debugSet = cosetPerm.apply(curSet);

		int curMapping = curIndex == 0 ? 1 : nextMapping(curSet[curIndex-1]);

		while(comp.compare(curMapping, curSet[curIndex]) < 0) {
			Map<Integer,Mapping> mappings = getPossibleMappingsTo(cosetPerm, cosetModPerm, curMapping,curIndex);

			for(int i : available) {
				Mapping ret = null;
				if((ret = mappings.get(i)) != null) {
					//We can map an elt of curSet to a set that was seen earlier.
					System.out.println(ret.varPerm);
					System.out.println(ret.varPerm.compose(cosetPerm.inverse()));
					getPossibleMappingsTo(cosetPerm, cosetModPerm, curMapping,curIndex);
					validateModelPerm = ret.modPerm;
					return ret.varPerm;
				}
			}

			curMapping = nextMapping(curMapping);
		}

		int stabGroupMapping = curMapping;//cosetPerm.inverse().imageOf(curMapping);
		//If we cannot map to a var less than cur, must be equal
		Map<Integer,Mapping> mappings = getPossibleMappingsTo(cosetPerm, cosetModPerm, stabGroupMapping,curIndex);

		for(int i : curSet) {
			int cosetImage = cosetPerm.imageOf(i);
			PairSchreierVector vec = graphLevels.get(curIndex);
			if(mappings.containsKey(i) && available.contains(i) &&  vec.sameOrbit(cosetImage,stabGroupMapping)) {
				Mapping map = mappings.get(i);
				LiteralPermutation newCosetPerm = cosetPerm.compose(vec.getPerm(cosetImage,stabGroupMapping));//map.varPerm;
				LiteralPermutation newCosetModPerm = cosetModPerm.compose(vec.getModelPart());//map.modPerm;

				TreeSet<Integer> newAvailable = new TreeSet<Integer>(comp);
				newAvailable.addAll(available);
				newAvailable.remove(i);

				LiteralPermutation next = getSmallerSubsetIfPossible(curSet,curIndex+1,newCosetPerm,newCosetModPerm,newAvailable);

				if(next != null) {
					return next;
				}
			}
		}
		return null;
	}

	///
	/// EVERYTHING HERE IS PROBABLY WRONG
	///
	private Map<Integer, Mapping> getPossibleMappingsTo(
			LiteralPermutation cosetPerm, LiteralPermutation cosetModPerm, int curMapping, int curIndex) {
		TreeMap<Integer,Mapping> mappings = new TreeMap<Integer,Mapping>();
		int origMapping = curMapping;
		//		curMapping = cosetPerm.imageOf(curMapping);

		LiteralPermutation[] validMappings = new LiteralPermutation[2*numVars+1];
		validMappings[LitUtil.getIndex(curMapping,numVars)] = graphLevels.get(0).getGroup().getId();

		LiteralPermutation[] modelMappings = null;
		modelMappings = new LiteralPermutation[2*numVars+1];
		modelMappings[LitUtil.getIndex(curMapping,numVars)] = graphLevels.get(0).getModelGroup().getId();


		for(int k = curIndex; k < graphLevels.size(); k++) {
			PairSchreierVector curVec = graphLevels.get(k);

			//Populate new connected literals
			for(int i = 0; i < validMappings.length; i++) {
				if(validMappings[i] == null) {
					int litI = LitUtil.getLit(i,numVars);

					for(int j = 0; j < validMappings.length; j++) {
						if(validMappings[j] != null) {
							int litJ = LitUtil.getLit(j,numVars);
							if(curVec.sameOrbit(litJ,litI)) {
								validMappings[i] = validMappings[j].compose(curVec.getPerm(litJ,litI));
								LiteralPermutation toCompose = curVec.getModelPart(); //raClauses.getModelPart(curVec.getPerm(litJ,litI));//
								modelMappings[i] = modelMappings[j].compose(toCompose);
							}
						}
					}
				}
			}
		}

		LiteralPermutation cosetPermInv = cosetPerm.inverse();
		LiteralPermutation cosetModPermInv = cosetModPerm.inverse();
		for(int i = 0; i < validMappings.length; i++) {
			int litI = LitUtil.getLit(i,numVars);
			if(litI == curMapping) continue;

			if(validMappings[i] != null) {
				Mapping ret = new Mapping();
				ret.lit = cosetPermInv.imageOf(litI);//litI;//
				//					ret.varPerm = cosetPerm.compose(validMappings[i].inverse());
				//					ret.modPerm = cosetModPerm.compose(modelMappings[i].inverse());

				ret.varPerm = validMappings[i].compose(cosetPermInv).inverse();
				ret.modPerm = modelMappings[i].compose(cosetModPermInv).inverse();

				if(ret.varPerm.imageOf(ret.lit) != origMapping) {
					System.out.println("AHHH!");
				}

				mappings.put(ret.lit,ret);
			}
		}


		return mappings;
	}

	private int nextMapping(int curMapping) {
		return curMapping > 0 ? -curMapping : -curMapping +1;
	}

	private static class Mapping implements Comparable<Mapping>{
		int lit;
		LiteralPermutation varPerm;
		LiteralPermutation modPerm;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + lit;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Mapping)) {
				return false;
			}
			Mapping other = (Mapping) obj;
			if (lit != other.lit) {
				return false;
			}
			return true;
		}
		@Override
		public int compareTo(Mapping o) {
			return comp.compare(lit,o.lit);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(lit).append(ConsoleDecodeable.newline);
			sb.append(varPerm).append(ConsoleDecodeable.newline);
			sb.append(modPerm).append(ConsoleDecodeable.newline);

			return sb.toString();
		}


	}

	protected static class LevelPair implements Comparable<LevelPair> {
		private int level;
		private int nextNode;
		private LiteralPermutation perm;

		public LevelPair(int level, int nextNode, LiteralPermutation perm) {
			this.level = level;
			this.nextNode = nextNode;
			this.perm = perm;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public int getNode() {
			return nextNode;
		}

		public void setNode(int nextNode) {
			this.nextNode = nextNode;
		}

		public LiteralPermutation getPerm() {
			return perm;
		}

		private final static LitComparator nodeComp = new LitComparator();
		//We want priority queue to give larger levels first, smaller vars first
		@Override
		public int compareTo(LevelPair o) {
			int comp1 = this.level-o.level;

			if(comp1 != 0) {
				return -comp1;
			}

			return nodeComp.compare(this.nextNode,o.nextNode);
		}

		@Override
		public String toString() {
			return "(" + level + "," + nextNode + ")";
		}



	}
}
