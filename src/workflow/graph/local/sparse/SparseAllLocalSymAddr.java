package workflow.graph.local.sparse;

import java.util.Arrays;

import org.sat4j.minisat.core.IntQueue;

import group.LiteralPermutation;

public class SparseAllLocalSymAddr extends SparseAbstractAllLocalSym {
	public SparseAllLocalSymAddr() {
		super();
	}

	public SparseAllLocalSymAddr(boolean checkFirstInLocalOrbit, boolean checkLitGraph,
			boolean checkFullGlobal, boolean checkFullLocalPath) {
		super(checkFirstInLocalOrbit, checkLitGraph, checkFullGlobal,
				checkFullLocalPath);
	}

	@Override
	protected void computeIso(LatticePart lp) {
		IntQueue toCompute = new IntQueue();
		toCompute.ensure(lp.modelGroup.size()+1);
		int[] orbits = new int[lp.modelGroup.size()+1];
		int[] localOrbit = new int[lp.modelGroup.size()+1];
		
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

}
