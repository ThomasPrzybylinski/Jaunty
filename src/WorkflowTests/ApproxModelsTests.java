package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.FormulaCreatorRandomizer;
import task.formula.LineColoringCreator;
import task.formula.ModelsCNFCreator;
import task.symmetry.local.OnlineCNFDiversity;
import util.lit.LitUtil;
import util.lit.LitsMap;
import workflow.CNFCreatorModelGiver;
import workflow.IdentityModelGiver;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;

public class ApproxModelsTests {

	static ModelGiver[] creators = new ModelGiver[]{
//		new CNFCreatorModelGiver(new QueensToSAT(5)), 	//0
//		new CNFCreatorModelGiver(new QueensToSAT(7)), 	//1
//		new CNFCreatorModelGiver(new QueensToSAT(8)), 	//2
//		new AllFilledSquares(5), 						//3
//		new AllFilledSquares(8),						//4
//		new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.d.cnf")), //5
		new CNFCreatorModelGiver(new LineColoringCreator(6,3)),	//6
		new CNFCreatorModelGiver(new LineColoringCreator(9,3)),	//7
//		new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\flat30-1.cnf")),	//8
	};

	static EdgeManipulator[] processes = //Need at least 1
			new EdgeManipulator[]{
		new CompoundEdgeManipulator( new EdgeManipulator[]{new AllLocalSymAddr(false,false,false,true)}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new AgreementLocalSymAdder()}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new GlobalSymmetryEdges()}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new GlobalSymmetryEdges(), new AgreementLocalSymAdder()}),
		
//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference())}),
//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges()}),
//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),new ShortestPathCreator()}),
//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),
//				new AgreementLocalSymAdder()}),
//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),
//					new AgreementLocalSymAdder(), new MakeEquivEdgesSmallDistances(), new ShortestPathCreator()}),
	};


	static boolean[][] canDo = new boolean[][]{
//		{true,true,true,true,true,true,true,true},		//0
//		{false,true,true,true,true,true,true,true,true},	//1
//		{false,true,true,true,true,true,true,true,true},	//2
//		{true,true,true,true,true,true,true,true},		//3
//		{false,true,true,true,true,true,true,true,true},	//4
//		{false,false,true,true,true,true,true,false,false},	//5
		{true,true,true,true,true,true,true,true,true},	//6
		{true,true,true,true,true,true,true,true,true},	//7
//		{false,false,true,true,true,true,true,true,true},	//8
	};


	public static void main(String[] args) throws Exception {
		final int numSets = 1000;
		//		private int numSets = 100;

		Random rand = new Random();

		for(int k = 0; k < creators.length; k++) {
			VariableContext context = new VariableContext();
			EclecSetCoverCreator diver = new IndependentSetCreator(new MeanClosenessFinder());
			diver.setRand(rand);
			ModelGiver creat = creators[k];
			System.out.println(creat);
			List<int[]> models;
			models = creat.getAllModels(context);

//			List<int[]> origModels = models;
//			models = LitUtil.removeSingleValAndEquivVars(models,context);

			ClauseList cl = new ClauseList(context);
			cl.addAll(models);

			PossiblyDenseGraph[] graphs = new PossiblyDenseGraph[processes.length];
			for(int i = 0; i < processes.length; i++) {
				if(canDo[k][i]) {
					graphs[i] = new PossiblyDenseGraph<int[]>(cl.getClauses());
					processes[i].addEdges(graphs[i],cl);
				}
			}

			List<List<Integer>> online = getSets(cl,numSets,rand);

			for(int i = 0; i < processes.length; i++) {
				if(canDo[k][i]) {
					int[] maxSizes = new int[numSets];
					
					for(int j = 0; j < maxSizes.length; j++) {
						maxSizes[j] = diver.getRandomEclecticSet(graphs[i]).size();
					}
					
					int[] results = new int[processes.length+1];
					double[] scores = new double[processes.length+1];
					double[] scores2 = new double[processes.length+1];
					for(int j = 0; j < processes.length; j++) {
						if(!canDo[k][j]) {
							results[j] = -1;
							continue;
						}
						for(int n = 0; n < numSets; n++) {
							List<Integer> eclec = diver.getRandomEclecticSet(graphs[j]);
							Collections.shuffle(eclec,rand);
							
//							while(eclec.size() > maxSizes[n]) {
//								eclec.remove(eclec.size()-1);
//							}
							
//							if(diver.verifyEclecticSet(graphs[i],eclec)) {
//								results[j]++;
//							}
							results[j] += eclec.size();
							scores[j] += Math.log(diver.getEclecticSetScore(graphs[i],eclec))/Math.log(2);
							scores2[j] += diver.getEclecticSetScore(graphs[i],eclec);
						}
					}
					for(int n = 0; n < numSets; n++) {
						List<Integer> eclec = new ArrayList(online.get(n));
						
						Collections.shuffle(eclec,rand);
						
//						while(eclec.size() > maxSizes[n]) {
//							eclec.remove(eclec.size()-1);
//						}
						
//						if(diver.verifyEclecticSet(graphs[i],eclec)) {
//							results[results.length-1]++;
//						}
						results[results.length-1] += eclec.size();
						scores[results.length-1] += Math.log(diver.getEclecticSetScore(graphs[i],eclec))/Math.log(2);
						scores2[results.length-1] += diver.getEclecticSetScore(graphs[i],eclec);
						
					}
					
					for(int j = 0; j < scores.length; j++) {
						results[j] /= (double)numSets;
//						results[j] = (int)(results[j]*(100/(double)numSets));
//						scores[j] = (int)(100*(scores[j]/(double)numSets));
						scores[j] = (scores[j])/(double)numSets;
						scores[j] = Math.pow(2,scores[j]);
						scores2[j] = scores2[j]/(double)numSets;
					}
					
					System.out.println(processes[i]);
					System.out.println(Arrays.toString(results));
					System.out.println(Arrays.toString(scores));
					System.out.println(Arrays.toString(scores2));
					System.out.println();
				}
			}
			
			System.out.println();
		}
	}

	private static List<List<Integer>> getSets(ClauseList models, int numSets, Random rand) throws TimeoutException, ContradictionException {
		LitsMap<Integer> map = new LitsMap<Integer> (models.getContext().size());
		List<int[]> clauses = models.getClauses();
		int origVars = clauses.get(0).length;

		List<List<Integer>> ret = new ArrayList<List<Integer>>(numSets);

		for(int k = 0; k < clauses.size(); k++) {
			map.put(clauses.get(k),k);
		}

		ModelsCNFCreator creat = new ModelsCNFCreator(new IdentityModelGiver(models),true);
		FormulaCreatorRandomizer randc = new FormulaCreatorRandomizer(creat,rand);
		//		FormulaRandomizer frand = new FormulaRandomizer(orig,rand,clauses.get(0).length);

		for(int k = 0; k < numSets; k++) {
			int[] firstModel = clauses.get(rand.nextInt(clauses.size()));

			CNF cnf = randc.generateCNF(new VariableContext());//creat.generateCNF(new VariableContext());//
			//			randc.setupRandomnessAlready(models.getContext());
			firstModel = randc.translateToInternal(firstModel);

			OnlineCNFDiversity div = new OnlineCNFDiversity(creat,firstModel);

			ClauseList eqSym = LitUtil.getEquaSymmetricModelsCNF(null,origVars,clauses);

			CNF randEqSym = new CNF(eqSym.getContext());
			for(int[] i : eqSym.getClauses()) {
				randEqSym.fastAddClause(randc.translateToInternal(i));
			}
			randEqSym.sort();

			List<int[]> set = div.getDivSetFromCNF(null,cnf,origVars,randEqSym);
			ArrayList<Integer> toAdd = new ArrayList<Integer>(set.size());

			for(int[] m : set) {
				int[] transM = randc.translateToOrig(m);
				Integer i = map.get(transM);
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
