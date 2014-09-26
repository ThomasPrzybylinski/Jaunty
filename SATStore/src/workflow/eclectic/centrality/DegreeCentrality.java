package workflow.eclectic.centrality;

import graph.PossiblyDenseGraph;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.StatUtils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import workflow.eclectic.EclecSetCoverCreator;

public class DegreeCentrality extends EclecSetCoverCreator {
	public DegreeCentrality() {

	}
	
	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		List<List<Integer>> ret = new LinkedList<List<Integer>>();
		double[] deg = new double[pdg.getNumNodes()];
		
		double maxWeight = Double.MIN_VALUE;

		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i)) {
					maxWeight = Math.max(maxWeight,pdg.getEdgeWeight(k,i));
				}
			}
		}
		
		if(maxWeight == 0) {
			maxWeight = 1;
		}

		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i)) {
					deg[k] += (1-(pdg.getEdgeWeight(k,i)/maxWeight));
				}
			}
			max = Math.max(max,deg[k]);
			min = Math.min(min,deg[k]);
		}
		
		max = max-.001;
		LinkedList<Integer> largest = new LinkedList<Integer>();
		for(int k = 0; k <  pdg.getNumNodes(); k++) {
			if(deg[k] >= max) {
				//System.out.println(k + ": " + vecData[k]);
				largest.add(k);
//				break;
			}
		}

		min = min+.001;
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
		throw new NotImplementedException();
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}

	@Override
	public double getEclecticSetScore(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		throw new NotImplementedException();
	}

	public boolean displayUnitSets() {
		return true;
	}
	

}
