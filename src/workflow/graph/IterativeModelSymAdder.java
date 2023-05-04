package workflow.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import util.IntegralDisjointSet;

public class IterativeModelSymAdder extends EdgeManipulator {

	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g,
			ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		int[] localToGlobal = new int[representatives.size()];
		
		for(int k = 0; k < localToGlobal.length; k++) {
			localToGlobal[k] = k;
		}
		
		ClauseList dual = SymmetryUtil.getInverseList(representatives,representatives.get(0).length);
		
		RealSymFinder ssf = new RealSymFinder(dual);
		
		IntegralDisjointSet symOrbits = ssf.getSymOrbits();
		
		symOrbits = deNegate(symOrbits,representatives.size());
		
		IntegralDisjointSet globalOrbits = symOrbits;
		
		int prevSize = representatives.size();
		Set<Integer> newModels = symOrbits.getRoots();
		
		while(newModels.size() < prevSize) {
			prevSize = newModels.size();
			List<int[]> models = new ArrayList<int[]>(newModels.size());
			
			for(int m : newModels) {
				if(m > 0) {
					int var = m-1;
					localToGlobal[models.size()] = var;
					models.add(representatives.get(var));
				}
			}
			
			dual = SymmetryUtil.getInverseList(representatives,representatives.get(0).length);
			
			ssf = new RealSymFinder(dual);
			
			symOrbits = ssf.getSymOrbits();
			symOrbits = deNegate(symOrbits,newModels.size());
			
			newModels = symOrbits.getRoots();
			
			combineSets(symOrbits,globalOrbits,localToGlobal,newModels.size());
		}
		
		for(int k = 0; k < representatives.size(); k++) {
			for(int i = k+1; i < representatives.size(); i++) {
				if(globalOrbits.sameSet(i+1,k+1)) {
					g.setEdgeWeight(k,i,0);
				}
			}
		}
	}

	private IntegralDisjointSet deNegate(IntegralDisjointSet symOrbits, int curModels) {
		IntegralDisjointSet ret = new IntegralDisjointSet(1,curModels);
		
		for(int k = 0; k < curModels; k++) {
			for(int i = k+1; i < curModels; i++) {
				if(symOrbits.sameSet(k+1,i+1)) {
					ret.join(k+1,i+1);
				}
			}
		}
		
		return ret;
		
	}

	public void combineSets(IntegralDisjointSet curSet, IntegralDisjointSet globalSet, int[] localToGlobal, int curModels) {
		for(int k = 0; k < curModels; k++) {
			for(int i = k+1; i < curModels; i++) {
				if(curSet.sameSet(k+1,i+1)) {
					globalSet.join(localToGlobal[k]+1,localToGlobal[i]+1);
				}
			}
		}
	}
	
	@Override
	public boolean isSimple() {
		return true;
	}

}
