package DistanceTests;

import formula.Conjunctions;
import formula.simple.CNF;
import formula.simple.DNF;
import graph.CompleteGraphCreator;
import graph.Node;
import graph.sat.SpanningCycleGraphProblem;
import io.SpanningCyclesIO;

import java.util.Collections;
import java.util.List;

import task.clustering.ClusterHierarchy;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;


/**
 * 
 * @author Thomas Przybylinski
 * 
 * Plan: Let's try a sort of agglomerative clustering:
 * 		We do hierarchical clustering. Whenever we create a cluster, we also choose
 * 		a representative that has the maximum minimum distance to every other cluster.
 *
 */
public class SpanCycleDistanceTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Node[] graph = CompleteGraphCreator.getCompleteGraph(8);

		Conjunctions conj = SpanningCycleGraphProblem.cycleAsCNF(graph);
		CNF cnf = new CNF(conj);

		System.out.println(cnf);
		System.out.println(cnf.toNumString());
		
//		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(cnf);
//		System.out.println(hier.getClusterAtLevel(0).size());
//		System.out.println(hier.getMaxLevel());
		//GeneralizedClusterTest.saveReps("SpanCyceDist",hier,new SpanningCyclesIO(graph));
		
		System.out.println("Next");
		
		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			dnfForSym.addClause(i);
		}
		
		DisjointSet<int[]> orb = SymmetryUtil.findSymmetryOrbitsNEW(dnfForSym);
		
		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(orb);
		GeneralizedClusterTest.saveReps("SpanCyceGlobSymDist",hier,0,hier.getMaxLevel(), new SpanningCyclesIO(graph));

	}
}
