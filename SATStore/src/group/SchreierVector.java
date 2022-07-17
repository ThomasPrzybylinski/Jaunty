package group;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import util.IntegralDisjointSet;


public final class SchreierVector {
	private LiteralPermutation[] vector;
	private int[] backPointer;
	private LiteralGroup group;

	private final int numVars;

	public SchreierVector(LiteralGroup g) {
		this(g,1);
	}

	public SchreierVector(LiteralGroup g, int initial) {
		List<LiteralPermutation> generators = g.getGenerators();
		this.group = g;
		this.numVars = g.size();

		vector = new LiteralPermutation[2*g.size()+1];
		backPointer = new int[vector.length];

		int initialIndex = index(initial);		
		for(int k = initialIndex; k < vector.length; k++) {
			if(vector[k] == null) {
				populateOrbit(k,generators);
			}

			//try to make vectory symmetric (e.g. 1 and -1 would be the reps of their respective cosets)
			//TODO: Investigate why this is important (can get incorrect answers if not)
			int negIndex = index(-lit(k));
			if(vector[negIndex] == null) {
				populateOrbit(negIndex,generators);
			}
		}
	}

	private final int index(int lit) {
		return lit+numVars;
	}

	private final int lit(int index) {
		return index-numVars;
	}

	private void populateOrbit(int repIndex, List<LiteralPermutation> generators) {
		int rep = lit(repIndex);
		Queue<Integer> toProcess = new LinkedList<Integer>();
		toProcess.add(rep);

		while(!toProcess.isEmpty()) {
			int curLit = toProcess.poll();
			if(curLit == 0) continue;
			for(LiteralPermutation perm : generators) {
				int image = perm.imageOf(curLit);
				int imageIndex = index(image);


				if(image != rep && vector[imageIndex] == null) {
					vector[imageIndex] = perm;
					backPointer[imageIndex] = index(curLit);
					toProcess.add(image);
				}
			}
		}
	}

	public int getRep(int lit) {
		int litIndex = index(lit);

		while(vector[litIndex] != null) {
			litIndex = backPointer[litIndex];
		}

		return lit(litIndex);
	}

	public boolean sameOrbit(int lit1, int lit2) {
		return getRep(lit1) == getRep(lit2);
	}

	public LiteralPermutation trace(int lit) {
		LiteralPermutation cur = group.getId();
		int litIndex = index(lit);

		while(vector[litIndex] != null) {
			cur = vector[litIndex].compose(cur);
			litIndex = backPointer[litIndex];
		}

		return cur;
	}

	//Returns all traces of literals in the same orbit as i.
	//If the literal i is always the represenative we
	//can perform some optimizations.
	//Each index uses LitUtil indexing for the literals,
	//has the literal permutation trace if exists, otherwise null
	public LiteralPermutation[] allTraces(int i) {
		LiteralPermutation[] ret = new LiteralPermutation[vector.length];
		int initialIndex = index(i); 

		if(vector[initialIndex] == null) {

			for(int k = 0; k < vector.length; k++) {
				if(getRep(lit(k)) == i) {
					//Do trace
					int litIndex = k;
					LiteralPermutation cur = group.getId();
					while(vector[litIndex] != null) {
						cur = vector[litIndex].compose(cur);
						litIndex = backPointer[litIndex];
					}
					ret[k] = cur;
				}

			}
		} else {
			for(int k = 0; k < vector.length; k++) {
				int litK = lit(k); 
				if(getRep(litK) == i) {
					ret[k] = getPerm(litK,i);
				}
			}
		}
		
		return ret;
	}

	//	public List<LiteralPermutation> traceAll() {
	//		LiteralPermutation cur = group.getId();
	//		int litIndex = index(lit);
	//		
	//		while(vector[litIndex] != null) {
	//			cur = vector[litIndex].compose(cur);
	//			litIndex = backPointer[litIndex];
	//		}
	//		
	//		return cur;
	//	}

	public LiteralPermutation getPerm(int from, int to) {
		if(!sameOrbit(from,to)) return null;
		return trace(from).inverse().compose(trace(to));
	}

	public IntegralDisjointSet transcribeOrbits() {
		return transcribeOrbits(true);
	}
    public IntegralDisjointSet transcribeOrbits(boolean includeNegativeLiterals) {
    	int minVar = includeNegativeLiterals ? -numVars : 1;
		IntegralDisjointSet ret = new IntegralDisjointSet(minVar,numVars);

		for(int k = minVar; k <= numVars; k++) {
			if(k == 0) continue;
			for(int i = minVar; i <= numVars; i++) {
				if(i == 0 || i == k) continue;
				if(sameOrbit(k,i)) {
					ret.join(k,i);
				}
			}
		}

		return ret;
	}

	public int getNumVars() {
		return numVars;
	}


}
