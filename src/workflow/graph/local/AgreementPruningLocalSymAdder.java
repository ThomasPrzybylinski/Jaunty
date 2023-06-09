package workflow.graph.local;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sat4j.minisat.core.IntQueue;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.sparse.SparseSymFinder;
import util.IntPair;
import util.formula.FormulaForAgreement;
import util.lit.LitsMap;
import workflow.graph.EdgeManipulator;

public class AgreementPruningLocalSymAdder extends EdgeManipulator {

	public int skipped = 0;
	public int iters = 0;
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		FormulaForAgreement rep = new FormulaForAgreement(orig);
		LeftCosetSmallerIsomorphFinder iso = new LeftCosetSmallerIsomorphFinder();
		
		LiteralGroup glob = (new SparseSymFinder(orig)).getSymGroup();
		LiteralGroup globMod = rep.getModelGroup(glob,rep.getExistantClauses()).reduce();
		
		for(int k = 0; k < representatives.size(); k++) {
			if(iso.getSmallerSubsetIfPossible(new int[]{k+1},globMod) != null) {
				continue;
			}
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);
				
				if(iso.getSmallerSubsetIfPossible(new int[]{k+1,i+1},globMod) != null) {
					continue;
				}
				
				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);

				iters++;
				
				if(map.contains(agreement)) {
					skipped++;
					continue;
				} else {
					map.put(agreement,null);
				}
				
				ClauseList cl = rep.getCLFromModels(agreement);

				SparseSymFinder syms = new SparseSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();

				LiteralGroup modelGroup = rep.getModelGroup(group,rep.getExistantClauses());
				
				populateEdges(g, orig, modelGroup);
			}
		}
		
		LinkedList<IntPair> toGen = new LinkedList<IntPair>();
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				if(g.areAdjacent(k,i) && g.getEdgeWeight(k,i) == 0) {
					toGen.add(new IntPair(k+1,i+1));
					
					while(!toGen.isEmpty()) {
						IntPair cur = toGen.poll();
						
						for(LiteralPermutation p : globMod.getGenerators()) {
							IntPair next = cur.applySort(p);
							
							if(!(g.areAdjacent(next.getI1()-1,next.getI2()-1) && g.getEdgeWeight(next.getI1()-1,next.getI2()-1) == 0)) {
								toGen.add(next);
								g.setEdgeWeight(next.getI1()-1,next.getI2()-1,0);
							}
						}
					}
				}
			}
		}
		

	}
	private void populateEdges(PossiblyDenseGraph<int[]> g,
			ClauseList orig, LiteralGroup modelGroup) {
		IntQueue toCompute = new IntQueue();
		toCompute.ensure(modelGroup.size()+1);
		int[] orbits = new int[modelGroup.size()+1];
		int[] localOrbit = new int[modelGroup.size()+1];
		
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
				for(LiteralPermutation perm : modelGroup.getGenerators()) {
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
					g.setEdgeWeight(localOrbit[i]-1,localOrbit[j]-1,0);
				}
			}
			Arrays.fill(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex),0);
		}
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

}
