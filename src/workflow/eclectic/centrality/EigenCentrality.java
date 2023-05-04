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

public class EigenCentrality extends EclecSetCoverCreator {
	private final double add;
	
	public EigenCentrality() {
		add = 0;
	}
	
	public EigenCentrality(boolean smooth) {
		if(smooth) {
			add = 1;
		} else {
			add = 0;
		}
	}
	
	
	@Override
	public List<List<Integer>> getEclecticSetCover(PossiblyDenseGraph<int[]> pdg) {
		List<List<Integer>> ret = new LinkedList<List<Integer>>();
		Array2DRowRealMatrix mat = new Array2DRowRealMatrix(pdg.getNumNodes(),pdg.getNumNodes());
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
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i)) {
					mat.setEntry(k,i,1-(pdg.getEdgeWeight(k,i)/maxWeight));
				}
			}
		}

		double[] vecData = new double[pdg.getNumNodes()];
		Arrays.fill(vecData,1);
		RealVector vec = new ArrayRealVector(vecData);
		vec = vec.mapDivideToSelf(vec.getNorm());

		double diff = 1;
		
		while(diff > .0000001) {
			RealVector old = vec.copy();
			vec = mat.operate(vec);
			vec.append(add);
			vec = vec.mapDivideToSelf(vec.getNorm());
			old = old.subtract(vec);
			
			diff = Double.MIN_VALUE;
			
			for(double d : old.getData()) {
				diff = Math.max(diff,d);
			}
//			System.out.println(vec);
		}
//		System.out.println(vec);

		vecData = vec.getData();
		double var = StatUtils.variance(vecData);
		if(var != 0) {
			vecData = StatUtils.normalize(vecData);
		}
		double max = StatUtils.max(vecData);
		double threshold = max-.01;
//		System.out.println(Arrays.toString(vecData));
//		System.out.println("Ordinary Solutions");
		
		LinkedList<Integer> largest = new LinkedList<Integer>();
		for(int k = 0; k < vecData.length; k++) {
			if(vecData[k] >= threshold) {
				//System.out.println(k + ": " + vecData[k]);
				largest.add(k);
//				break;
			}
		}

		double min = StatUtils.min(vecData);
		threshold = min+.01;
//		System.out.println("Unique Solutions");
		
		LinkedList<Integer> smallest = new LinkedList<Integer>();
		for(int k = 0; k < vecData.length; k++) {
			if(vecData[k] <= threshold) {
//				System.out.println(k + ": " + vecData[k]);
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

	@Override
	public String getDirLabel() {
		return super.getDirLabel() + (add == 0. ? "" : "Smooth");
	}
	
	
	public boolean displayUnitSets() {
		return true;
	}

	@Override
	public boolean verifyEclecticPair(PossiblyDenseGraph<int[]> pdg, int v1, int v2) {
		throw new NotImplementedException();
	}
	

}
