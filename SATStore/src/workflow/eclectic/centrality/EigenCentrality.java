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

		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i) && pdg.getEdgeWeight(k,i) == 0) {
					mat.setEntry(k,i,1);
				}
			}
		}

		double[] vecData = new double[pdg.getNumNodes()];
		Arrays.fill(vecData,1);
		RealVector vec = new ArrayRealVector(vecData);
		vec = vec.mapDivideToSelf(vec.getNorm());

		for(int k = 0; k < 1000; k++) {
			vec = mat.operate(vec);
			vec.append(add);
			vec = vec.mapDivideToSelf(vec.getNorm());
//			System.out.println(vec);
		}
//		System.out.println(vec);

		vecData = vec.getData();
		vecData = StatUtils.normalize(vecData);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyEclecticSet(PossiblyDenseGraph<int[]> pdg,
			List<Integer> list) {
		return false;
	}

	@Override
	public String getDirLabel() {
		return super.getDirLabel() + (add == 0. ? "" : "Smooth");
	}
	
	
	public boolean displayUnitSets() {
		return true;
	}
	

}
