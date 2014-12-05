package WorkflowTests;

import java.util.Arrays;
import java.util.List;

import subsumptionMain.SATSump;
import task.clustering.SimpleDifference;
import task.formula.AllFilledSquares;
import task.formula.AllSquaresCNF;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import workflow.ModelGiver;
import workflow.eclectic.ConstantFunctionNClosenessFinder;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.NFloatFunction;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AgreementPruningLocalSymAdder;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class SampleTest {

	public static void main(String[] args) throws Exception {
		final int size = 10;
		final int iters = 20;
		ModelGiver giver = new AllFilledSquares(size);
		List<int[]> models = giver.getAllModels(new VariableContext());
		ClauseList cl = new ClauseList(new VariableContext());
		System.out.println(models.size());
		cl.addAll(models);
		EclecSetCoverCreator div = new IndependentSetCreator(new ConstantFunctionNClosenessFinder(new NFloatFunction() {

			@Override
			public boolean close(float n) {
				return n < 28;
			}
		}));

		int[] sizes = new int[size+1];

		PossiblyDenseGraph<int[]> pdg = new PossiblyDenseGraph<int[]>(models);

		for(int k = 0; k < models.size(); k++) {
			sizes[getSize(pdg.getElt(k))]++;
		}
		System.out.println("Random Sample");
		for(int k = 1; k < sizes.length; k++) {
			System.out.println(k + " \t" + sizes[k]/(double)(models.size()));
		}

		EdgeManipulator dist = new DistanceEdges(new SimpleDifference());
		dist.addEdges(pdg,cl);

		runTests(size, iters, div, sizes, pdg,"Distance");

		pdg = new PossiblyDenseGraph<int[]>(models);

		EdgeManipulator glob = new GlobalSymmetryEdges();
		glob.addEdges(pdg,cl);
		runTests(size,iters,div,sizes,pdg,"Global");
		
		pdg = new PossiblyDenseGraph<int[]>(models);

		EdgeManipulator loc = new AgreementPruningLocalSymAdder();
		loc.addEdges(pdg,cl);
		runTests(size,iters,div,sizes,pdg,"Agree");
		
		AllSquaresCNF online = new AllSquaresCNF(size);
		CNF cnf = online.generateCNF(new VariableContext());
		cnf = SATSump.getSubsumedConj(cnf);
		
		Arrays.fill(sizes,0);
		double totalSizes = 0;
		double repSizes = 0;
		for(int k = 0; k < iters; k++) {
			CNFSparseOnlineCNFDiversity onlineDiv = new CNFSparseOnlineCNFDiversity(cnf);
//			onlineDiv.forceGlobBreakCl = true;
			List<int[]> eclecs = onlineDiv.getDiverseSet();
			totalSizes += eclecs.size();
			repSizes += Math.min(eclecs.size(), size);
			for(int j = 0; j < Math.min(eclecs.size(), size); j++) {
				sizes[getSize(eclecs.get(j))]++;
			}
		}

		System.out.println("Online Sample");
		System.out.println("Avg Size: " + (totalSizes/(double)iters));
		for(int k = 1; k < sizes.length; k++) {
			System.out.println(k + " \t" + sizes[k]/repSizes);
		}
		

	}

	private static void runTests(final int size, final int iters,
			EclecSetCoverCreator div, int[] sizes, PossiblyDenseGraph<int[]> pdg,
			String sampleType) {
		Arrays.fill(sizes,0);

		double totalSizes = 0;
		double repSizes = 0;
		for(int k = 0; k < iters; k++) {
			List<Integer> eclecs = div.getRandomEclecticSet(pdg);
			totalSizes += eclecs.size();
			repSizes += Math.min(eclecs.size(), size);
			for(int j = 0; j < Math.min(eclecs.size(), size); j++) {

				int index = eclecs.get(j);
				sizes[getSize(pdg.getElt(index))]++;
			}
		}

		System.out.println(sampleType + " Sample");
		System.out.println("Avg Size: " + (totalSizes/(double)iters));
		for(int k = 1; k < sizes.length; k++) {
			System.out.println(k + " \t" + sizes[k]/repSizes);
		}
	}

	public static int getSize(int[] model) {
		int numPos = 0;

		for(int i : model) {
			if(i > 0) numPos++;
		}

		return (int)Math.sqrt(numPos);
	}

}
