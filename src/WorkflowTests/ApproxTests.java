package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.IdentityCNFCreator;
import task.formula.ModelsCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.local.OnlineCNFDiversity;
import util.lit.LitUtil;
import util.lit.LitsMap;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;

public class ApproxTests {

	static CNFCreator[][] creators = new CNFCreator[][]{
//		{new QueensToSAT(5), new QueensToSAT(8), new QueensToSAT(11)},
//		{new ModelsCNFCreator(new AllFilledSquares(5)), 
//			new ModelsCNFCreator(new AllFilledSquares(8)),
//			new ModelsCNFCreator(new AllFilledSquares(16))},
//			{null, new IdentityCNFCreator("testcnf\\bw_large.c.cnf"),  new IdentityCNFCreator("testcnf\\bw_large.d.cnf")},
			{new ModelsCNFCreator(new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.d.cnf")),true),
				new ModelsCNFCreator(new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.c.cnf")),true)
			, new ModelsCNFCreator(new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.d.cnf")),true)},
//			{new LineColoringCreator(9,3), new LineColoringCreator(9,3), new LineColoringCreator(20,3)},

	};


	public static void main(String[] args) throws Exception {
		final int numSets = 0;
		//		private int numSets = 100;

		Random rand = new Random();
		AllLocalSymAddr fullAddr = new AllLocalSymAddr(false,false,false,true);
		AgreementLocalSymAdder agreeAddr = new AgreementLocalSymAdder();

		for(int k = 0; k < creators.length; k++) {
			VariableContext context = new VariableContext();
			EclecSetCoverCreator diver = new IndependentSetCreator(new MeanClosenessFinder());
			diver.setRand(rand);

			if(creators[k][0] != null) {
				CNFCreator creat = creators[k][0];
				List<int[]> models;
				CNF origCNF0 = creators[k][0].generateCNF(new VariableContext());
				if(creat instanceof ModelsCNFCreator) {
					models = ((ModelsCNFCreator)creat).getPrevMods();
				} else {
					ModelGiver giver = new CNFCreatorModelGiver(creators[k][0]);
					models = giver.getAllModels(context);
				}

				ClauseList cl = new ClauseList(context);
				cl.addAll(models);
				PossiblyDenseGraph<int[]> full = new PossiblyDenseGraph<int[]>(cl.getClauses());
				fullAddr.addEdges(full,cl);
				PossiblyDenseGraph<int[]> agree = new PossiblyDenseGraph<int[]>(cl.getClauses());
				agreeAddr.addEdges(agree,cl);
				(new GlobalSymmetryEdges()).addEdges(agree,cl);
				List<List<Integer>> online = getSets(creators[k][0],cl,numSets,rand);

				int[] results = new int[2];
				for(int i = 0;i < numSets; i++) {
					List<Integer> eclec = diver.getRandomEclecticSet(agree);
					if(diver.verifyEclecticSet(full,eclec)) {
						results[0]++;
					}

					if(diver.verifyEclecticSet(full,online.get(i))) {
						results[1]++;
					}
				}
				System.out.println(Arrays.toString(results));
			}
			
			if(creators[k][1] != null) {
				CNFCreator creat = creators[k][1];
				List<int[]> models;
				CNF origCNF0 = creators[k][0].generateCNF(context);
				if(creat instanceof ModelsCNFCreator) {
					models = ((ModelsCNFCreator)creat).getPrevMods();
				} else {
					ModelGiver giver = new CNFCreatorModelGiver(creat);
					models = giver.getAllModels(context);
				}
				List<int[]> origModels = models;
				models = LitUtil.removeSingleValAndEquivVars(models,context);
				ClauseList cl = new ClauseList(context);
				cl.addAll(models);
				PossiblyDenseGraph<int[]> agree = new PossiblyDenseGraph<int[]>(cl.getClauses());
				agreeAddr.addEdges(agree,cl);
				(new GlobalSymmetryEdges()).addEdges(agree,cl);
				
				cl = new ClauseList(context);
				cl.addAll(origModels);
				List<List<Integer>> online = getSets(creat,cl,numSets,rand);

				int[] results = new int[1];
				for(int i = 0;i < numSets; i++) {
					if(diver.verifyEclecticSet(agree,online.get(i))) {
						results[0]++;
					}
				}
				System.out.println(Arrays.toString(results));
			}
		}

	}

	private static List<List<Integer>> getSets(CNFCreator cnfCreator,
			ClauseList models, int numSets, Random rand) throws TimeoutException, ContradictionException {
		LitsMap<Integer> map = new LitsMap<Integer> (models.getContext().size());
		List<int[]> clauses = models.getClauses();

		List<List<Integer>> ret = new ArrayList<List<Integer>>(numSets);

		for(int k = 0; k < clauses.size(); k++) {
			map.put(clauses.get(k),k);
		}

		for(int k = 0; k < numSets; k++) {
			int[] firstModel = clauses.get(rand.nextInt(clauses.size()));
			OnlineCNFDiversity div = new OnlineCNFDiversity(cnfCreator,firstModel);

			List<int[]> set = div.getDiverseSet(rand);
			ArrayList<Integer> toAdd = new ArrayList<Integer>(set.size());

			for(int[] m : set) {
				Integer i = map.get(m);
				if(i == null) {
					throw new RuntimeException();
				}
				toAdd.add(i);
			}
			ret.add(toAdd);
		}

		return ret;
	}


}
