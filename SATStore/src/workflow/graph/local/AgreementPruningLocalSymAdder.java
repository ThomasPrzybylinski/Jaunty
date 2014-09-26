package workflow.graph.local;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;

import java.util.Arrays;
import java.util.List;

import org.sat4j.minisat.core.IntQueue;

import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import util.lit.LitsMap;
import workflow.graph.EdgeManipulator;

@Deprecated//(Not ready for use)
public class AgreementPruningLocalSymAdder extends EdgeManipulator {

	public int skipped = 0;
	public int iters = 0;
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		skipped = 0;
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		LocalSymClauses rep = new LocalSymClauses(orig);
		
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);

				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);

				iters++;
				
				if(map.contains(agreement)) {
					skipped++;
					continue;
				} else {
					map.put(agreement,null);
				}
				rep.post();

				for(int j = 0; j < agreement.length; j++) {
					int var = Math.abs(agreement[j]);
					if(agreement[j] > 0) {
						rep.addCondition(var);
					} else if(agreement[j] < 0) {
						rep.addCondition(-(var));
					}
				}

				
				
				ClauseList cl = rep.getCurList(false);
				
				RealSymFinder syms = new RealSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();

				LiteralGroup modelGroup = rep.getModelGroup(group);

				populateEdges(g, orig, modelGroup);

				rep.pop();
				
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
