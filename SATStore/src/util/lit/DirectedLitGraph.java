package util.lit;

import group.LiteralPermutation;
import group.PairSchreierVector;

import java.util.ArrayList;
import java.util.List;

import task.symmetry.local.LocalSymClauses;

//TODO: Create dual Schrier Vector to reduce the calls to getModelPart, which is slow
public class DirectedLitGraph {
	private List<PairSchreierVector> graphLevels;
	private int numVars;
	//	private int curLevel = -1; //First level 0; global symmetry
	//CurLevel is the length of the filter to validate - 1
	private int validateLevel = -1;
	private LiteralPermutation validateModelPerm = null;

	public DirectedLitGraph(int numVars) {
		this.numVars = numVars;
		graphLevels = new ArrayList<PairSchreierVector>();
	}

	public void push(PairSchreierVector curOrbits) {
		graphLevels.add(curOrbits);
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
