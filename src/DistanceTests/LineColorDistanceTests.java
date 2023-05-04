package DistanceTests;

import java.util.Collections;
import java.util.List;

import formula.Conjunctions;
import formula.simple.CNF;
import formula.simple.DNF;
import graph.LineCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;
import io.GraphColorIO;
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
public class LineColorDistanceTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numNodes = 5;
		int numColors = 3;
		
		LineCreator creat = new LineCreator();
		Node[] graph = creat.getLine(numNodes); //creat.getLineSkips(numNodes,2,3);//
		Conjunctions color = GraphToColorProblem.coloringAsConjunction(graph,numColors);
		
		CNF cnf = new CNF(color);
		
		System.out.println(cnf);
		System.out.println(cnf.toNumString());
		
		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(cnf);
//		System.out.println(hier.getClusterAtLevel(0).size());
//		System.out.println(hier.getMaxLevel());
//		GeneralizedClusterTest.saveReps("LineColorDist",hier, new GraphColorIO(graph,numColors));
		
		System.out.println("next");
		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			dnfForSym.addClause(i);
		}
		
		DisjointSet<int[]> orb = SymmetryUtil.findSymmetryOrbitsNEW(dnfForSym);
		
		hier = GeneralizedClusterTest.getHierarchy(orb);
		GeneralizedClusterTest.saveReps("LineColorSymDist",hier,0,hier.getMaxLevel(),new GraphColorIO(graph,numColors));

	}
}
