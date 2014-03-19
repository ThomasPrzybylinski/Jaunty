package DistanceTests;

import java.util.Collections;
import java.util.List;

import task.clustering.ClusterHierarchy;
import task.formula.NAssymRooks;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;


/**
 * 
 * @author Thomas Przybylinski
 * 
 * Plan: Let's try a sort of agglomerative clustering:
 * 		We do hierarchical clustering. Whenever we create a cluster, we also choose
 * 		a representative that has the maximum minimum distance to every other cluster.
 *
 */
public class RooksDistanceTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VariableContext context = VariableContext.defaultContext;

		NAssymRooks create = new NAssymRooks(2);
		CNF cnf = create.encode(context).reduce();
		System.out.println(create.consoleDecoding(new int[context.getNumVarsMade()+1]));

//		System.out.println(cnf);
//		System.out.println(cnf.toNumString());
//		
//		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(cnf);
//		GeneralizedClusterTest.printReps(hier,create);

		
//		System.out.println("Next");
//		
		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			System.out.println(create.consoleDecoding(i));
			dnfForSym.addClause(i);
		}
		
		DisjointSet<int[]> orb = SymmetryUtil.findSymmetryOrbitsNEW(dnfForSym);
		
		ClusterHierarchy hier = GeneralizedClusterTest.getHierarchy(orb);
		GeneralizedClusterTest.printReps(hier,0,hier.getMaxLevel(), create);
	}
}
