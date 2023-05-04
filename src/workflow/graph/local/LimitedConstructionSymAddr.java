package workflow.graph.local;

import group.LiteralPermutation;

import java.util.Arrays;
import java.util.LinkedList;

import org.sat4j.minisat.core.IntQueue;

import util.IntPair;
import workflow.graph.local.AbstractAllLocalSym.LatticePart;

public class LimitedConstructionSymAddr extends AbstractLimitedLocalSym {
	private int depth = 3;
	private int minMods=-1;
	public LimitedConstructionSymAddr() {
		super();
		init();
	}

	public LimitedConstructionSymAddr(boolean checkFirstInLocalOrbit, boolean checkLitGraph,
			boolean checkFullGlobal, boolean checkFullLocalPath, int depth) {
		super(checkFirstInLocalOrbit, checkLitGraph, checkFullGlobal,
				checkFullLocalPath);
		this.depth = depth;
		init();
	}
	
	public LimitedConstructionSymAddr(boolean checkFirstInLocalOrbit, boolean checkLitGraph,
			boolean checkFullGlobal, boolean checkFullLocalPath, int depth, int minModels) {
		super(checkFirstInLocalOrbit, checkLitGraph, checkFullGlobal,
				checkFullLocalPath);
		this.depth = depth;
		this.minMods=minModels;
		init();
	}
	
	

	private void init() {
		setMaxDepth(depth);
		setMinModels(minMods);
		
	}

	@Override
	protected void computeIso(LatticePart lp) {
		IntQueue toCompute = new IntQueue();
		toCompute.ensure(lp.modelGroup.size()+1);
		int[] orbits = new int[lp.modelGroup.size()+1];
		int[] localOrbit = new int[lp.modelGroup.size()+1];
		
		LinkedList<IntPair> toCreateNew = new LinkedList<IntPair>();
		toCreateNew.addAll(lp.getPairs());
		
		//Construct new ascending sequences
		while(!toCreateNew.isEmpty()) {
			IntPair pair = toCreateNew.poll();

			for(LiteralPermutation p : lp.modelGroup.getGenerators()) {
				IntPair newP;// = pair.applySort(p);

//				if(!lp.pairs.contains(newP)) {
//					lp.pairs.add(newP);
//					toCompute.push(newP);
//				}
				
				newP = pair.applySort(p,0);
				
				if(!lp.pairs.contains(newP)) {
					lp.pairs.add(newP);
					toCreateNew.push(newP);
				}
				
				newP = pair.applySort(p,1);
				
				if(!lp.pairs.contains(newP)) {
					lp.pairs.add(newP);
					toCreateNew.push(newP);
				}
			}
		}
		
		
		//add in orbits from this node
		for(int k = 1; k < orbits.length; k++) {
			if(orbits[k] != 0) continue;
			
//			int rep = modVec.getRep(k);

			toCompute.insert(k);
			orbits[k] = k;
			localOrbit[0] = k;
			int localOrbitIndex = 1;
		
			//Compute orbit of k
			while(toCompute.size() > 0) {
				int i = toCompute.dequeue();
				for(LiteralPermutation perm : lp.modelGroup.getGenerators()) {
					int image = perm.imageOf(i);
					if(orbits[image] == 0) {
						orbits[image] = k;
						localOrbit[localOrbitIndex] = image;
						localOrbitIndex++;
						toCompute.insert(image);
					}
				}
			}
			
			//use the orbit to create edges
			Arrays.sort(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex));
			for(int i = 0; i < localOrbitIndex; i++) {
				for(int j = i+1; j < localOrbitIndex; j++) {
					lp.getPairs().add(getCachedPair(localOrbit[i],localOrbit[j]));
				}
			}
			Arrays.fill(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex),0);
			
//			for(int i = k+1; i <= modVec.getNumVars(); i++) {
//				if(modVec.sameOrbit(k,i)) {
////				if(modVec.getRep(i)==rep) {
//					lp.getPairs().add(getCachedPair(k,i));
//				}
//			}
		}
		
		lp.finishPairs();
		//for memory reasons:
//		lp.children = null;
		lp.modelGroup = null;
		lp.varGroup = null;
		
	}
	
	@Override
	public String getDirName() {
		return super.getDirName()+"["+depth+"]";
	}

	@Override
	public String toString() {
		return "Depth("+depth+")"+"ConstrSym";
	}
}
