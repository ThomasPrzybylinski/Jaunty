package workflow.tests;

import java.lang.reflect.Constructor;
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
import task.formula.AllFilledSquares;
import task.formula.FormulaCreatorRandomizer;
import task.formula.ModelsCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.local.OnlineCNFDiversity;
import util.lit.LitUtil;
import util.lit.LitsMap;
import workflow.CNFCreatorModelGiver;
import workflow.IdentityModelGiver;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.RandomCreator;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;

public class MultiModelsSpeedApproxTests {

	static Class[] creators = new Class[]{
//				LineColoringCreator.class,
				AllFilledSquares.class,
		//		QueensToSAT.class,
//		ApproxColorableGraphCNF.class,

	};

	static Class[][] param = new Class[][]{
//				{int.class,int.class},
				{int.class},
		//		{int.class},
//		{int.class,int.class,int.class,int.class},
	};

	static Object[][][] inst = new Object[][][]{ 
		//		//Path Coloring
//				{{2,3},{3,3},{4,3},{5,3},{6,3},{7,3},{8,3},{9,3},{10,3},{11,3},{12,3},{13,3},{14,3},{15,3},{16,3}},
		//		//Filled Squares
				{{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13},{14},{15},{16}},
		//		//Queens
		//		{{5},{7},{8},{9},{10},{11}},
//		{{3,1,3,4},{4,2,3,4},{5,4,3,4},{6,6,3,4},{7,8,3,4},{8,10,3,4},{9,13,3,4},{10,15,3,4},{11,17,3,4},{12,19,3,4}},//,{13,18,3,3},{14,19,3,3},{15,21,3,3}},
	};

	static EdgeManipulator[] processes = //Need at least 1
			new EdgeManipulator[]{
		new CompoundEdgeManipulator( new EdgeManipulator[]{new AllLocalSymAddr(false,false,false,true)}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new GlobalSymmetryEdges(), new AgreementLocalSymAdder()}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new AgreementLocalSymAdder()}),
		new CompoundEdgeManipulator( new EdgeManipulator[]{new GlobalSymmetryEdges()}),
		

		//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference())}),
		//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges()}),
		//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),new ShortestPathCreator()}),
		//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),
		//				new AgreementLocalSymAdder()}),
		//		new CompoundEdgeManipulator( new EdgeManipulator[]{new DistanceEdges(new SimpleDifference()), new GlobalSymmetryEdges(),
		//					new AgreementLocalSymAdder(), new MakeEquivEdgesSmallDistances(), new ShortestPathCreator()}),
	};

	static Object[][][] maxForProc = new Object[][][]{
		//Path Coloring
				{{10,3},{12,3},{12,3},{12,3}},
//		{{10,3},{10,3},{10,3},{10,3}},
		//Filled Squares
		{{6},{11},{11},{11}},
		//		{{6},{11},{15},{15}},
		//Queens
//		{{7},{11},{11},{11}},
		//		{{7},{11},{11},{11}},
//		{{12,17,3,3},{12,17,3,3},{-1,-1,-1,-1},{-1,-1,-1,-1},},
	};


	public static void main(String[] args) throws Exception {
		final int numSets = 1;
		final int numRandSets = 100;
		//		private int numSets = 100;

		Random rand = new Random(5);

		for(int k = 0; k < creators.length; k++) {
			VariableContext context = new VariableContext();
			EclecSetCoverCreator diver = new IndependentSetCreator(new MeanClosenessFinder());
			diver.setRand(rand);

			RandomCreator randDiv = new RandomCreator(rand);

			boolean[] finished = new boolean[processes.length];

			Constructor c = creators[k].getConstructor(param[k]);
			System.out.println(creators[k]);
			for(int j = 0; j < inst[k].length; j++) {
				boolean stop = true;
				for(int i = 0; i < processes.length; i++) {
					if(!finished[i]) {
						if(Arrays.equals(inst[k][j],maxForProc[k][i])) {
							finished[i] = true;
						}
					}

					stop &= finished[i];
				}

				if(stop) break;

				ModelGiver creat;
				if(CNFCreator.class.isAssignableFrom(creators[k])) {
					creat = new CNFCreatorModelGiver((CNFCreator)c.newInstance(inst[k][j]));
				} else {
					creat = (ModelGiver)c.newInstance(inst[k][j]);
				}

				System.out.println(Arrays.toString(inst[k][j]));

				int[][] fullResults = new int[processes.length][processes.length+1];
				double[][] fullScores = new double[processes.length][processes.length+1];
				double[][] fillScores2 = new double[processes.length][processes.length+1];
				long[] totalTimes = new long[processes.length+1];
				for(int numSteps = 0; numSteps < numRandSets; numSteps++) {
					List<int[]> models;
					models = creat.getAllModels(context);
					if(models.size() <= 1) {
//						System.err.println("UNSAT!");
						continue;
					}
					//			List<int[]> origModels = models;
					//			models = LitUtil.removeSingleValAndEquivVars(models,context);

					ClauseList cl = new ClauseList(context);
					cl.addAll(models);

					PossiblyDenseGraph[] graphs = new PossiblyDenseGraph[processes.length];
					for(int i = 0; i < processes.length; i++) {

						if(!finished[i]) {
							graphs[i] = new PossiblyDenseGraph<int[]>(cl.getClauses());
							long start = System.currentTimeMillis();
							processes[i].addEdges(graphs[i],cl);
							long end = System.currentTimeMillis();
							totalTimes[i] +=(end-start);
							//							System.out.println(processes[i] + " \t"+(end-start));
							if((end-start) > 1000000) finished[i] = true;
						}
					}
					long start = System.currentTimeMillis();
					List<List<Integer>> online = getSets(cl,numSets,rand);
					long end = System.currentTimeMillis();
					totalTimes[processes.length] += (end-start)/numSets;

					for(int compProc = 0; compProc < processes.length; compProc++) {
						if(!finished[compProc]) {

							int[] results = fullResults[compProc];
							double[] scores = fullScores[compProc];
							double[] scores2 = fillScores2[compProc];

							for(int n = 0; n < numSets; n++) {
								for(int testingProc = 0; testingProc < processes.length; testingProc++) {
									if(finished[testingProc]) {
										results[testingProc] = -numSets;
										continue;
									}


									List<Integer> eclec = diver.getRandomEclecticSet(graphs[testingProc]);
									double score = diver.getEclecticSetScore(graphs[compProc],eclec);

									double meanRandScore = 1;
									//								double meanRandScore = 0;
									//								for(int m = 0; m < numRandSets; m++) {
									//									List<Integer> randEclec = randDiv.getRandomEclecticSet(graphs[testingProc],eclec.size());
									//									meanRandScore += diver.getEclecticSetScore(graphs[compProc],randEclec);
									//								}
									//								meanRandScore /= numRandSets;
									//								Collections.shuffle(eclec,rand);
									results[testingProc] += eclec.size();
									scores[testingProc] += Math.log((score/meanRandScore))/Math.log(2);
									scores2[testingProc] += (score/meanRandScore);
								}

								List<Integer> eclec = new ArrayList<Integer>(online.get(n));

								double score = diver.getEclecticSetScore(graphs[compProc],eclec);

								double meanRandScore = 1;
								//							double meanRandScore = 0;
								//							for(int m = 0; m < numRandSets; m++) {
								//								List<Integer> randEclec = randDiv.getRandomEclecticSet(graphs[compProc],eclec.size());
								//								meanRandScore += diver.getEclecticSetScore(graphs[compProc],randEclec);
								//							}
								//							meanRandScore /= numRandSets;

								//								Collections.shuffle(eclec,rand);
								results[results.length-1] += eclec.size();
								scores[results.length-1] += Math.log((score/meanRandScore))/Math.log(2);
								scores2[results.length-1] += (score/meanRandScore);
							}
						}
					}
				}
				for(int compProc = 0; compProc < processes.length; compProc++) {
					System.out.println(processes[compProc] + " \t"+totalTimes[compProc]/numRandSets);
					int[] results = fullResults[compProc];
					double[] scores = fullScores[compProc];
					double[] scores2 = fillScores2[compProc];
					for(int s = 0; s < scores.length; s++) {
						results[s] /= (double)(numSets*numRandSets);
						//						results[s] = (int)(results[s]*(100/(double)numSets));
						//						scores[s] = (int)(100*(scores[s]/(double)numSets));
						scores[s] = (scores[s])/(double)(numSets*numRandSets);
						scores[s] = Math.pow(2,scores[s]);
						scores2[s] = scores2[s]/(double)(numSets*numRandSets);
					}
				}

				System.out.println("Online" + " \t"+totalTimes[processes.length]/numRandSets);
				
				for(int compProc = 0; compProc < processes.length; compProc++) {
					int[] results = fullResults[compProc];
					double[] scores = fullScores[compProc];
					double[] scores2 = fillScores2[compProc];
					System.out.println(processes[compProc]);
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

		ModelsCNFCreator creat = new ModelsCNFCreator(new IdentityModelGiver(models),false);
		FormulaCreatorRandomizer randc = new FormulaCreatorRandomizer(creat,rand);
		//		FormulaRandomizer frand = new FormulaRandomizer(orig,rand,clauses.get(0).length);
		long total = 0;
		for(int k = 0; k < numSets; k++) {
			int[] firstModel = clauses.get(rand.nextInt(clauses.size()));

			CNF cnf = randc.generateCNF(new VariableContext());//creat.generateCNF(new VariableContext());//
			//			randc.setupRandomnessAlready(models.getContext());
			firstModel = randc.translateToInternal(firstModel);

			OnlineCNFDiversity div = new OnlineCNFDiversity(creat,firstModel);
			div.setUseLocalSymBreak(false);
			div.setUseGlobalSymBreak(false);


			ClauseList eqSym = LitUtil.getEquaSymmetricModelsCNF(null,origVars,clauses);

			CNF randEqSym = new CNF(eqSym.getContext());
			for(int[] i : eqSym.getClauses()) {
				int[] i2 = randc.translateToInternal(i);
				randEqSym.fastAddClause(i2);
				if(!Arrays.equals(randc.translateToOrig(i2),i)) {
					throw new RuntimeException();
				}
			}
			randEqSym.sort();

			long start = System.currentTimeMillis();
			List<int[]> set = div.getDivSetFromCNF(null,cnf,origVars,randEqSym);
			long end = System.currentTimeMillis();
			total += (end-start);
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

		//		System.out.println("Online \t"+(total/numSets));

		return ret;
	}


}
