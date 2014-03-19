package DistanceTests;

import formula.Conjunctions;
import formula.simple.CNF;
import graph.ColorableGraphCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;
import io.GraphColorIO;
import task.clustering.ClusterHierarchy;


/**
 * 
 * @author Thomas Przybylinski
 * 
 * Plan: Let's try a sort of agglomerative clustering:
 * 		We do hierarchical clustering. Whenever we create a cluster, we also choose
 * 		a representative that has the maximum minimum distance to every other cluster.
 *
 */
public class GraphColorDistanceTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numNodes = 10;
		int numEdges =  18;
		int numColors = 3;
		
		ColorableGraphCreator creat = new ColorableGraphCreator();
		Node[] graph = creat.getColorableGraph(numNodes,numEdges,numColors);
		Conjunctions color = GraphToColorProblem.coloringAsCNF(graph,numColors);
		
		CNF cnf = new CNF(color);
		
		System.out.println(cnf);
		System.out.println(cnf.toNumString());
		
		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(cnf);
		System.out.println(hier.getClusterAtLevel(0).size());
		System.out.println(hier.getMaxLevel());
		GeneralizedClusterTest.saveReps("ColorDist",hier,new GraphColorIO(graph,numColors));

	}
}
