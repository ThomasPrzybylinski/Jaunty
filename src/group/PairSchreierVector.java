package group;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import util.IntegralDisjointSet;


public final class PairSchreierVector {
	private LiteralPermutation[] vector;
	private LiteralPermutation[] modelVector;
	private int[] backPointer;
	private LiteralGroup group;
	private LiteralGroup modelGroup;
	
	private LiteralPermutation modelPart = null;
	
	private int numVars;
	
	private static boolean PRUNE_DEFAULT = false;
	
	public PairSchreierVector(LiteralGroup g, LiteralGroup modelGroup, boolean pruneModelIdentPairs) {
		this(g,modelGroup,1,pruneModelIdentPairs);
	}
	
	public PairSchreierVector(LiteralGroup g, LiteralGroup modelGroup) {
		this(g,modelGroup,1,PRUNE_DEFAULT);
	}
	
	public PairSchreierVector(LiteralGroup g, LiteralGroup modelGroup, int initial, boolean pruneModelIdentPairs) {
		if(pruneModelIdentPairs) {
			List<LiteralPermutation> varGens = g.getGenerators();
			List<LiteralPermutation> modGens = modelGroup.getGenerators();
			
			LinkedList<LiteralPermutation> newVar = new LinkedList<LiteralPermutation>();
			LinkedList<LiteralPermutation> newMod = new LinkedList<LiteralPermutation>();
			
			ListIterator<LiteralPermutation> modIter = modGens.listIterator();
			for(LiteralPermutation perm : varGens) {
				LiteralPermutation modPerm = modIter.next();
				
				if(!modPerm.isId()) {
					newVar.add(perm);
					newMod.add(modPerm);
				}
			}
			
			if(newVar.isEmpty()) {
				g = new NaiveLiteralGroup(g.getId());
				modelGroup = new NaiveLiteralGroup(modelGroup.getId());
			} else {
				g = new NaiveLiteralGroup(newVar);
				modelGroup  = new NaiveLiteralGroup(newMod);
			}
		}
		
		
		init(g,modelGroup,initial);
	}
		
	public PairSchreierVector(LiteralGroup g, LiteralGroup modelGroup, int initial) {
		this(g, modelGroup, initial,PRUNE_DEFAULT);
	}

	private void init(LiteralGroup g, LiteralGroup modelGroup, int initial) {
		List<LiteralPermutation> generators = g.getGenerators();
		this.group = g;
		this.modelGroup = modelGroup;
		this.numVars = g.size();
	
		vector = new LiteralPermutation[2*g.size()+1];
		modelVector = new LiteralPermutation[2*g.size()+1];
		backPointer = new int[vector.length];
		
		int initialIndex = index(initial);		
		for(int k = initialIndex; k < vector.length; k++) {
			if(vector[k] == null) {
				populateOrbit(k,generators,modelGroup.getGenerators());
			}
			
			//try to make vectory symmetric (e.g. 1 and -1 would be the reps of their respective cosets)
			//TODO: Investigate why this is important (can get incorrect answers if not)
			int negIndex = index(-lit(k));
			if(vector[negIndex] == null) {
				populateOrbit(negIndex,generators,modelGroup.getGenerators());
			}
		}
	}
	
	private final int index(int lit) {
		return lit+numVars;
	}
	
	private final int lit(int index) {
		return index-numVars;
	}

	private void populateOrbit(int repIndex, List<LiteralPermutation> generators,List<LiteralPermutation> modelGenerators ) {
		int rep = lit(repIndex);
		Queue<Integer> toProcess = new LinkedList<Integer>();
		toProcess.add(rep);
		

		while(!toProcess.isEmpty()) {
			int curLit = toProcess.poll();
			
			ListIterator<LiteralPermutation> modIter = modelGenerators.listIterator();
			for(LiteralPermutation perm : generators) {
				LiteralPermutation modPerm = modIter.next();
				
				if(perm != null && modPerm == null) {
					System.out.println("BAD!");
				}
				
				int image = perm.imageOf(curLit);
				int imageIndex = index(image);
				
				
				if(image != rep && vector[imageIndex] == null) {
					vector[imageIndex] = perm;
					modelVector[imageIndex] = modPerm;
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
		LiteralPermutation modelCur = modelGroup.getId();
		int litIndex = index(lit);
		
		while(vector[litIndex] != null) {
			cur = vector[litIndex].compose(cur);
			modelCur = modelVector[litIndex].compose(modelCur);
			litIndex = backPointer[litIndex];
		}
		
		
		modelPart = modelCur;
		return cur;
	}
	
	public LiteralPermutation getPerm(int from, int to) {
		if(!sameOrbit(from,to)) {
			modelPart = null;
			return null;
		}
		LiteralPermutation ret = trace(from).inverse();
		LiteralPermutation modRet = modelPart.inverse();
		
		LiteralPermutation toCompose = trace(to);
		LiteralPermutation modToCompose = modelPart;
		
		modelPart = modRet.compose(modToCompose);
		ret = ret.compose(toCompose);
		return ret;
	}
	
	public IntegralDisjointSet transcribeOrbits() {
		IntegralDisjointSet ret = new IntegralDisjointSet(-numVars,numVars);
		
		for(int k = -numVars; k <= numVars; k++) {
			if(k == 0) continue;
			for(int i = -numVars; i <= numVars; i++) {
				if(i == 0 || i == k) continue;
				if(sameOrbit(k,i)) {
					ret.join(k,i);
				}
			}
		}
		
		return ret;
	}
	
	

	public LiteralPermutation getModelPart() {
		return modelPart;
	}

	public int getNumVars() {
		return numVars;
	}

	public LiteralGroup getGroup() {
		return group;
	}

	public LiteralGroup getModelGroup() {
		return modelGroup;
	}
	
	
}
