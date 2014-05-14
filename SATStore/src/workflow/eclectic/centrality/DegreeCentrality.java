package workflow.eclectic.centrality;

import graph.PossiblyDenseGraph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.StatUtils;

import workflow.eclectic.EclecSetCoverCreator;

public class DegreeCentrality extends EclecSetCoverCreator {
	public DegreeCentrality() {

	}
	
	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		List<List<Integer>> ret = new LinkedList<List<Integer>>();
		int[] deg = new int[pdg.getNumNodes()];

		int max = -1;
		int min = pdg.getNumNodes()+1;
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i) && pdg.getEdgeWeight(k,i) == 0) {
					deg[k]++;
				}
			}
			max = Math.max(max,deg[k]);
			min = Math.min(min,deg[k]);
		}

		LinkedList<Integer> largest = new LinkedList<Integer>();
		for(int k = 0; k <  pdg.getNumNodes(); k++) {
			if(deg[k] >= max) {
				//System.out.println(k + ": " + vecData[k]);
				largest.add(k);
//				break;
			}
		}

		
		LinkedList<Integer> smallest = new LinkedList<Integer>();
		for(int k = 0; k <  pdg.getNumNodes(); k++) {
			if(deg[k] <= min) {
				//System.out.println(k + ": " + vecData[k]);
				smallest.add(k);
//				break;
			}
		}
		
		ret.add(largest);
		ret.add(smallest);
		
		return ret;
	}

	@Override
	public List<Integer> getRandomEclecticSet(PossiblyDenseGraph<int[]> pdg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		return false;
	}


	public boolean displayUnitSets() {
		return true;
	}
	

}
