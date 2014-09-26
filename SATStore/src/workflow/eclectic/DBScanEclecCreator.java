package workflow.eclectic;

import graph.PossiblyDenseGraph;

import java.util.LinkedList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.DisjointSet;

public class DBScanEclecCreator extends EclecSetCoverCreator {
	private  ClosenessFinder closeFinder;

	public DBScanEclecCreator(ClosenessFinder closeEdgeFinder) {
		this.closeFinder = closeEdgeFinder;
	}

	@Override
	public List<List<Integer>> getEclecticSetCover(
			PossiblyDenseGraph<int[]> pdg) {
		closeFinder.setPdg(pdg);
		closeFinder.initialize();
		
		List<Integer> ints = new LinkedList<Integer>();
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			ints.add(k);
		}
		
		DisjointSet<Integer> clusterer = new DisjointSet<Integer>(ints);
		ints = null;
		
		PossiblyDenseGraph<int[]> clusterRep = new PossiblyDenseGraph<int[]>(pdg.getObjs());
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = k+1; i < pdg.getNumNodes(); i++) {
				if(!clusterer.sameSet(k,i) && closeFinder.areTooClose(i,k)) {
					clusterer.join(k,i);
				}
			}
		}
		
		
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = k+1; i < pdg.getNumNodes(); i++) {
				if(clusterer.sameSet(k,i)) {
					clusterRep.setAdjacent(k,i);
				}
			}
		}
		
		return (new IndependentSetCreator(new NullClosenessFinder())).getEclecticSetCover(clusterRep);
		
	}
	
	@Override
	public List<Integer> getRandomEclecticSet(
			PossiblyDenseGraph<int[]> pdg) {
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
	

}
