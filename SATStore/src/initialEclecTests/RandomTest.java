package initialEclecTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import task.clustering.SimpleDBScan;
import task.formula.FormulaCreator;
import task.formula.random.SimpleCNFCreator;
import task.symmetry.SymmetryUtil;
import formula.Conjunctions;
import formula.simple.CNF;
import formula.simple.DNF;



public class RandomTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		int v = 20;
		double cvRat = 3.3;
		int clauseSize = 3;
		FormulaCreator fc = new SimpleCNFCreator(v,cvRat,clauseSize);

		Conjunctions conj = (Conjunctions)fc.nextFormula();
		CNF orig = new CNF(conj);

		List<int[]> models = new ArrayList<int[]>();

		System.out.println("Solving");
		ISolver solver = orig.getSolverForCNF();
		ModelIterator iter = new ModelIterator(solver);

		DNF dnfForSym = new DNF(orig.getContext());

		while(iter.isSatisfiable()) {
			int[] model = iter.model();
			models.add(model);
			dnfForSym.addClause(model);
		}

		for(int[] mod : models) {
			System.out.println(Arrays.toString(mod));
		}

		System.out.println(models.size());

		//saveColoredModelsAsPic(numColors, graph, models,"testOrigCyc");


		int k = 0;
		List<int[]>  prev = models;
		List<int[]>  cur = models;

		do {
			System.out.println("Breaking");
			List<int[]> syms = SymmetryUtil.getSyms(dnfForSym);
			List<int[]> globModels = SymmetryUtil.breakModels(cur,syms);
			prev = cur;
			cur = globModels;
			k++;

			dnfForSym = new DNF(dnfForSym.getContext());
			for(int[] mod : cur) {
				dnfForSym.addClause(mod);
				System.out.println(Arrays.toString(mod));
			}

			System.out.println(cur.size());


		} while(prev.size() != cur.size());

		dnfForSym = new DNF(dnfForSym.getContext());
		for(int[] mod : cur) {
			dnfForSym.addClause(mod);
		}

		System.out.println("LocalBreaking");

		Collection<int[]> localModels = SymmetryUtil.breakyByUnitLocalSyms(dnfForSym);
		cur.clear();
		cur.addAll(localModels);

		for(int[] mod : localModels) {
			System.out.println(Arrays.toString(mod));
		}
		System.out.println(localModels.size());


		System.out.println("ClusteringLocal");
		clusterAndDisplay(new ArrayList<int[]>(localModels));

		System.out.println("Clustering");
		clusterAndDisplay(models);


		//doTheThing(models, dnfForSym.getContext());
//		int[][] stuff = StrangeUtil.doTheThing(models,dnfForSym.getContext());
//		
//		
//		GraphizUtil.saveGraphAsPic(stuff,"RandomAgreementSymmetryGraph");
//
//		int[] row = stuff[0];
//		int max = Integer.MIN_VALUE;
//
//		for(int i : row) {
//			max = Math.max(max,i);
//		}
//
//		System.out.println(Arrays.toString(models.get(0)));
//		for(int i = 0; i < row.length; i++) {
//			if(row[i] == max) {
//				System.out.println(Arrays.toString(models.get(i)));
//			}
//		}



	}
	
	
	private static void clusterAndDisplay(List<int[]> models) {
		SimpleDBScan scan = new SimpleDBScan(1);

		List<Set<int[]>> clusters = scan.getClustering(models);
		Comparator<int[]> comp = SymmetryUtil.LEX_LEADER_COMP;

		for(Set<int[]> curClust: clusters) {
			int[] curModel = null;
			for(int[] model : curClust) {
				if(curModel == null || comp.compare(curModel,model) < 0) {
					curModel = model;
				}
			}

			System.out.println(Arrays.toString(curModel));
			System.out.println(curClust.size());
		}
	}

}
