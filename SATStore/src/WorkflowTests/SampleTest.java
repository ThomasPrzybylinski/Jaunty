package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.sat4j.minisat.core.ICDCL;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import subsumptionMain.SATSump;
import task.clustering.SimpleDifference;
import task.formula.AllFilledSquares;
import task.formula.AllFilledSquaresCNF;
import task.formula.AllSquares;
import task.formula.AllSquaresCNF;
import task.sat.SATUtil;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import task.symmetry.sparse.RandomPhase;
import util.formula.FormulaForAgreement;
import util.lit.LitsMap;
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
		final int size = 5;
		final int iters = 10;
		ModelGiver giver = new AllFilledSquares(size);
		AllFilledSquaresCNF online = new AllFilledSquaresCNF(size);
		CNF cnf = online.generateCNF(new VariableContext());

//				cnf = SATSump.getSubsumedConj(cnf);
//		{
//			int oldSize = -1;
//			int newSize = cnf.getDeepSize();
//			LitsMap<?> seen = new LitsMap<Object>(cnf.getContext().size());
//			while(oldSize != newSize) {
//				cnf.addAll((new FormulaForAgreement(cnf)).getResolvants());// getSubsumedConj(cnf);
//				cnf = cnf.reduce();
//				oldSize = newSize;
//				newSize = cnf.getDeepSize();
//				System.out.println(newSize);
//			}
//		}
//		cnf = cnf.trySubsumption();
//		System.out.println(cnf.getDeepSize());

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
			sizes[getSize(models.get(k))]++;
		}

		System.out.println("Random Sample 1");
		for(int k = 1; k < sizes.length; k++) {
			System.out.println(k + " \t" + sizes[k]/(double)(models.size()));
		}

		Arrays.fill(sizes,0);
		double totalSizes = 0;
		double repSizes = 0;
		for(int k = 0; k < iters; k++) {
			ISolver solver = cnf.getSolverForCNF();
			ICDCL engine = ((ICDCL)solver.getSolvingEngine());
			engine.getOrder().setPhaseSelectionStrategy(new RandomPhase()); //);// engine.getOrder()));

			ModelIterator it = new ModelIterator(solver);

			for(int j = 0; j < size && it.isSatisfiable(); j++) {
				sizes[getSize(it.model())]++;
				repSizes++;
			}


		}

		System.out.println("Random Sample 2");
		for(int k = 1; k < sizes.length; k++) {
			System.out.println(k + " \t" + sizes[k]/(double)repSizes);
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



		Arrays.fill(sizes,0);
		totalSizes = 0;
		repSizes = 0;
		for(int k = 0; k < iters; k++) {
			CNFSparseOnlineCNFDiversity onlineDiv = new CNFSparseOnlineCNFDiversity(cnf);
			//			onlineDiv.setMaxSize(size);
			//			onlineDiv.setBreakFast(true);
			onlineDiv.setPrintProgress(true);
						onlineDiv.setUseLocalSymBreak(false);
						onlineDiv.setUseGlobalSymBreak(false);
			//			onlineDiv.forceGlobBreakCl = true;
			List<int[]> eclecs = onlineDiv.getDiverseSet();//new Random());//, models);
			totalSizes += eclecs.size();
			//			Collections.shuffle(eclecs);
			repSizes += Math.min(eclecs.size(), size);
			for(int j = 0; j < Math.min(eclecs.size(), size); j++) {//Math.min(eclecs.size(), size); j++) {
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
			Collections.shuffle(eclecs);
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

		//	For non-filled version
		//		int ret = numPos;
		//		if(numPos > 4) {
		//			ret = (numPos+4)/4;
		//		} else if(numPos == 4) {
		//			ret = 2;
		//		}

		return (int)Math.sqrt(numPos);//ret
	}

}
