package DistanceTests;

import task.clustering.ClusterHierarchy;
import task.formula.random.Simple3SATCreator;
import task.translate.DefaultConsoleDecoder;
import formula.Conjunctions;
import formula.simple.CNF;


/**
 * 
 * @author Thomas Przybylinski
 * 
 * Plan: Let's try a sort of agglomerative clustering:
 * 		We do hierarchical clustering. Whenever we create a cluster, we also choose
 * 		a representative that has the maximum minimum distance to every other cluster.
 *
 */
public class RandomDistanceTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Simple3SATCreator create = new Simple3SATCreator(15,3.5);
		CNF cnf = new CNF(((Conjunctions)create.nextFormulaImpl()));

		System.out.println(cnf);
		System.out.println(cnf.toNumString());
		
		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(cnf);
		System.out.println(hier.getClusterAtLevel(0).size());
		System.out.println(hier.getMaxLevel());
		GeneralizedClusterTest.printReps(hier,new DefaultConsoleDecoder());

	}
}
