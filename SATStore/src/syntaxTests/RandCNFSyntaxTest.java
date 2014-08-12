package syntaxTests;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import sun.security.krb5.internal.crypto.CksumType;
import task.clustering.SimpleDBScan;
import task.clustering.SimpleDifference;
import task.formula.FormulaCreatorToCNFCreator;
import task.formula.IdentityCNFCreator;
import task.formula.ModelsCNFCreator;
import task.formula.random.CNFCreator;
import task.formula.random.SimpleCNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import util.IntPair;
import util.lit.LitSorter;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.NVarsClosenessFinder;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.MinimalDistanceEdges;
import workflow.graph.local.AgreementConstructionAdder;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;

public class RandCNFSyntaxTest {

	private static final int numRandClustTests = 1000;
	private static final int max_models = Integer.MAX_VALUE;//50000;//100000;//50000;////Integer.MAX_VALUE;//10000;//20000;//
	private static LiteralGroup globalGroup;
	private static CNF origCNF;
	private static ModelMapper globMapper;
	private static HashMap<IntPair, Integer> equalVars;

	private static int[] getRadii() {
		ArrayList<Integer> radii = new ArrayList<Integer>();


		for(int radius = 1; radius < 50; radius+=1) {
			radii.add(radius);
		}

		int[] ret = new int[radii.size()];
		for(int k = 0; k < radii.size(); k++) {
			ret[k] = radii.get(k);
		}

		return ret;
	}

	public static void main(String[] args) throws Exception {
		final int maxDiv = Integer.MAX_VALUE;//20;
		Random rand = new Random();
		int[] radii = getRadii();
		double[] curRatioSum = new double[radii.length];
		double[] numCurHasBetter = new double[radii.length];
		double[] randRatio = new double[radii.length];
		int[] totalLTMaxModel =  new int[radii.length];
		double[] clustSizeRatio =  new double[radii.length];
		double[] SymSizeRatio = new double[radii.length];
		double[] adjRatio =  new double[radii.length];
		double[] randAdjRatio =  new double[radii.length];
		long[] allModelsSize =  new long[radii.length];
		

		int iters = 100;
		for(int inst = 1; inst <= iters; inst++) { 
			//				System.out.println(inst);
									CNFCreator creator = new IdentityCNFCreator("testcnf\\uf75-325\\uf75-0"+inst+".cnf");
//			CNFCreator creator = new IdentityCNFCreator("testcnf\\uf20-91\\uf20-0"+inst+".cnf");
//						CNFCreator creator = new IdentityCNFCreator("testcnf\\uf50-218\\uf50-0"+inst+".cnf");
//									CNFCreator creator = new IdentityCNFCreator("testcnf\\uf200-860\\uf200-0"+inst+".cnf");
//									CNFCreator creator = new IdentityCNFCreator("testcnf\\uf100-430\\uf100-0"+inst+".cnf");
//			CNFCreator creator = new SimpleCNFCreator(75,4.26,3);


			VariableContext context = new VariableContext();
			CNF function = creator.generateCNF(context);
			//			function = randomizeVars(function);
			int origVars = context.size();

			equalVars = new HashMap<IntPair, Integer>();

			origCNF = function;

			globMapper = new ModelMapper(origCNF);

			RealSymFinder finder = new RealSymFinder(function);
			globalGroup =  finder.getSymGroup();

			ArrayList<int[]> curModels = new ArrayList<int[]>();
			ArrayList<int[]> allModels = new ArrayList<int[]>();

			ISolver solver = function.getSolverForCNFEnsureVariableUIDsMatch();
			ISolver fullSolver = function.getSolverForCNFEnsureVariableUIDsMatch();

			addFullSymBreakClauses(globalGroup, new int[]{}, solver);

			int[] firstModel = getTrueModel(solver.findModel(),origVars);
			if(firstModel == null)  {
				System.out.println("Unsatisfiable Theory");
				continue;
			}
			curModels.add(firstModel);
			allModels.add(firstModel);

			int[] rejectFirstModel = getRejection(firstModel);
			solver.addClause(new VecInt(rejectFirstModel));
			fullSolver.addClause(new VecInt(rejectFirstModel));
			//		solver.clearLearntClauses();
			

			int[] nextModel;

			while((nextModel = fullSolver.findModel()) != null) {
				nextModel = getTrueModel(nextModel,origVars);
				allModels.add(nextModel);
				int[] rejectModel = getRejection(nextModel);
				fullSolver.addClause(new VecInt(rejectModel));

				if(allModels.size() > max_models) break;
			}
			fullSolver.reset();
			fullSolver = null;
			if(allModels.size() > max_models) {
				solver.reset();
				continue;
			}
			
			System.out.println(allModels.size());
			
			int tested = 1;
			while((nextModel = solver.findModel()) != null) {
				tested++;
				nextModel = getTrueModel(nextModel,origVars);
				boolean add = true;

				int[] rejectModel = getRejection(nextModel);
				solver.addClause(new VecInt(rejectModel));
				//			solver.clearLearntClauses();

				//			for(int[] oldModel : curModels) {
//				for(int k = 0; k < curModels.size(); k++) {
				for(int k = curModels.size()-1; k >= 0 ; k--) {
					int[] oldModel = curModels.get(k);
					int[] agree = getAgreement(oldModel,nextModel);
					CNF reducedCNF = getFormulaFromAgreement(origCNF,agree);

					if(reducedCNF.size() == 0) {
						add = false;
						continue;
					}

					if(add) {
						add &= processSymmetry(oldModel,nextModel,reducedCNF, agree);
					}
					

					if(!add || solver.nConstraints() <= 5*origCNF.size()) {
						finder = new RealSymFinder(reducedCNF);
						LiteralGroup lg =  finder.getSymGroup();
						addFullSymBreakClauses(lg,agree,solver);
					}

					if(!add && solver.nConstraints() > 5*origCNF.size()) {
						break;
					}

				}

				if(add) {
					curModels.add(nextModel);
					//					System.out.println(curModels.size() +"/" + numModels);
				}

				if(curModels.size() == maxDiv) break;
			}
			solver.reset();
			if(curModels.size() == 1) continue;
			
			

//						ArrayList<int[]> minDistModels = new ArrayList<int[]>();
//						{
//			
//							EdgeManipulator mdist = new AgreementLocalSymAdder();
//							ClauseList mdistCL = new ClauseList(origCNF.getContext());
//							mdistCL.addAll(allModels);
//							PossiblyDenseGraph<int[]> pdg = new PossiblyDenseGraph<int[]>(mdistCL.getClauses());
//							mdist.addEdges(pdg,mdistCL);
//							IndependentSetCreator creat = new IndependentSetCreator(new MeanClosenessFinder());
//							List<Integer> mods = creat.getRandomEclecticSet(pdg);
////							curModels.clear();
//							for(int i : mods) {
//								minDistModels.add(pdg.getElt(i));
////								curModels.add(pdg.getElt(i));
//							}
//						}
//						
//						if(minDistModels.size() == 1) continue;
//						double agrDist = getMeanDist(minDistModels);
			//			System.out.println(curModels.size() +"/" + numModels);
			//			System.out.println("Full Models: " + allModels.size());

		
			
			double curDist = getMeanDist(curModels);
			//			System.out.println(getMeanDist(allModels) + " " + curDist);


			boolean finishedCluster = false;
			
			List<List<int[]>> clusters = null;
			List<List<int[]>> curClusts = null;
			
			for(int k = 0; k < radii.length; k++) {
				if(finishedCluster && curModels.size() != 1) {
					for(int j = 0; j < numRandClustTests; j++) {
						//						numCurHasBetter[k] += 1;
						////						divModDiff[k] += curModels.size();
					}
					continue;
				}
		
				if(finishedCluster) continue;
				

				SimpleDBScan clust = new SimpleDBScan(radii[k]);
				
				if(clusters == null) {
					clusters = clust.getClusteringList(allModels);
					curClusts = clust.getClusteringList(curModels);
					if(clusters.size() == 1) finishedCluster = true; //All cluster afterwards will have mean dist 0
				} else {
					clusters = clust.getTighterClustersing(clusters);
					curClusts = clust.getTighterClustersing(curClusts);
					if(clusters.size() == 1) finishedCluster = true; //All cluster afterwards will have mean dist 0
				}
		//			System.out.println("Num Clusters: " + clusters.size());

				Collections.shuffle(allModels);
				int curRandIndex = 0;
				for(int j = 0; j < numRandClustTests; j++) {
					ArrayList<int[]> randClust = new ArrayList<int[]>(clusters.size());
					ArrayList<int[]> randSymClust = new ArrayList<int[]>(curClusts.size());
					ArrayList<int[]> superRandAdjClust = new ArrayList<int[]>(curClusts.size());
					getRandomClustRep(rand, clusters, randClust);
					
					getRandomClustRep(rand, curClusts, randSymClust);

					
					ArrayList<int[]> superRand= new ArrayList<int[]>(curModels.size());//clusters.size());
					if(curRandIndex + curModels.size() >= allModels.size()) {
						Collections.shuffle(allModels);
						curRandIndex = 0;
					}
					for(int h = 0; h < curModels.size(); h++) {
						superRand.add(allModels.get(curRandIndex));
						curRandIndex++;
					}
					
					List<List<int[]>> superRandClusters = clust.getClusteringList(superRand);
					getRandomClustRep(rand, superRandClusters, superRandAdjClust);

					double randDist =  getMeanDist(randClust);
					double superRandDist = getMeanDist(superRand);
					double superRandAdjDist = getMeanDist(superRandAdjClust);
					//			System.out.println("RandClustLen: " + randDist);

					if(randDist > 0 && curDist > 0) {
						curRatioSum[k] += curDist/randDist;
						randRatio[k] += superRandDist/randDist;//curModels.size()/randClust.size();//
						adjRatio[k] += getMeanDist(randSymClust)/randDist;//agrDist/randDist;
						randAdjRatio[k] += superRandAdjDist/randDist;
						allModelsSize[k] += allModels.size();
						SymSizeRatio[k] += curModels.size();
						clustSizeRatio[k] += clusters.size();
						totalLTMaxModel[k]++;
					}

					//					if(curModels.size() == 1) {
					//						numCurHasBetter[k]--;
					//					}
					//					
					//					if(clusters.size() == 1) {
					//						numCurHasBetter[k]++;
					//					}

				}
			}

			System.out.println(inst);
			for(int k = 0; k < radii.length; k++) {
				System.out.print(radii[k]+"\t");
				System.out.print((curRatioSum[k]/(double)totalLTMaxModel[k])+"\t");
				//				System.out.print((numCurHasBetter[k])+"\t");//(double)(inst*numRandClustTests))+"\t");
				System.out.print((adjRatio[k]/(double)totalLTMaxModel[k])+"\t");
				System.out.print((randRatio[k]/(double)totalLTMaxModel[k])+"\t");
				System.out.print((randAdjRatio[k]/(double)totalLTMaxModel[k])+"\t");
				System.out.print(SymSizeRatio[k]/(double)allModelsSize[k]+"\t");
				System.out.print(clustSizeRatio[k]/(double)allModelsSize[k]+"\t");
				System.out.println(totalLTMaxModel[k]/numRandClustTests);
				
			}
			System.out.println();

		}
		System.out.println("DONE");
		for(int k = 0; k < radii.length; k++) {
			//			System.out.println();
			System.out.print(radii[k]+"\t");
			System.out.print((curRatioSum[k]/(double)totalLTMaxModel[k])+"\t");
			//			System.out.print(numCurHasBetter[k]+"\t");///(double)(numRandClustTests*iters)+"\t");
						System.out.print((adjRatio[k]/(double)totalLTMaxModel[k])+"\t");
			System.out.print((randRatio[k]/(double)totalLTMaxModel[k])+"\t");
			System.out.print((randAdjRatio[k]/(double)totalLTMaxModel[k])+"\t");
			System.out.print(SymSizeRatio[k]/(double)allModelsSize[k]+"\t");
			System.out.print(clustSizeRatio[k]/(double)allModelsSize[k]+"\t");
			System.out.println(totalLTMaxModel[k]/numRandClustTests);
		}
	}

	private static void getRandomClustRep(Random rand,
			List<List<int[]>> curClusts, ArrayList<int[]> randSymClust) {
		for(List<int[]> aClust : curClusts) {
			int amnt = rand.nextInt(aClust.size());
			randSymClust.add(aClust.get(amnt));
		}
	}


	private static CNF randomizeVars(CNF function) {
		int[] varTrans = new int[function.getContext().size()];
		ArrayList<Integer> transOrder = new ArrayList<Integer>(varTrans.length);

		CNF ret = new CNF(function.getContext());

		for(int k = 0; k < varTrans.length; k++) {
			transOrder.add(k+1);
		}
		Collections.shuffle(transOrder);
		for(int k = 0; k < varTrans.length; k++) {
			varTrans[k] = transOrder.get(k);
		}

		for(int[] cl : function.getClauses()) {
			int[] toAdd = new int[cl.length];

			for(int k = 0; k < cl.length; k++) {
				int lit = cl[k];
				int var = Math.abs(lit);
				toAdd[k] = (lit/var)*varTrans[var-1];
			}

			LitSorter.inPlaceSort(toAdd);
			ret.fastAddClause(toAdd);
		}
		ret.sort();
		return ret;

	}

	private static double getMeanDist(List<int[]> models) {
		long sum = 0;
		SimpleDifference dif = new SimpleDifference();
		int size = models.size();

		if(size <= 1) return 0;

		for(int k = 0; k < models.size(); k++) {
			int[] m1 = models.get(k);
			for(int i = k+1; i < models.size(); i++) {
				int[] m2 = models.get(i);
				sum += dif.distance(m1,m2);
			}

		}

		return sum/((double)(((size)*(size-1))/2.));
	}

	private static double getColorScore(ArrayList<int[]> curModels, int numColors) {
		double score = 0;

		int size = 0;
		int numPredicted = 0;
		for(int k = 0; k < curModels.size(); k++) {
			int[] model1 = curModels.get(k);
			size = model1.length/numColors;

			for(int i = k+1; i < curModels.size(); i++) {
				int[][] probs = new int[numColors][numColors];
				int[] model2 = curModels.get(i);

				for(int j = 0; j < model1.length/numColors; j++) {
					int col1 = -1;
					int col2 = -1;
					for(int l = 0; l < numColors; l++) {
						if(model1[j*numColors + l] > 0) {
							col1 = l;
							break;
						}
					}

					for(int l = 0; l < numColors; l++) {
						if(model2[j*numColors + l] > 0) {
							col2 = l;
							break;
						}
					}

					probs[col1][col2]++;
				}
				int max = -1;
				for(int[] prob : probs) {
					for(int next : prob) {
						max = Math.max(next,max);
					}
					numPredicted += max;
				}
			}
		}



		score = (numPredicted/(double)size)/((double)curModels.size()*curModels.size());

		return score;

	}

	//Ignore added symmetry breaking vars
	private static int[] getTrueModel(int[] findModel, int origVars) {
		if(findModel == null) return null;
		int[] ret = new int[origVars];
		System.arraycopy(findModel,0,ret,0,origVars);
		return ret;
	}


	private static boolean doAgree(int[] otherModel, int[] agree) {
		int agInd = 0;
		for(int k = 0; k < otherModel.length; k++) {
			if(agInd >= agree.length) return true;

			if(otherModel[k] == agree[agInd]) {
				agInd++;
			} else if(otherModel[k] == -agree[agInd]) {
				return false;
			}
		}

		return true;
	}

	//Return true if diverse mode, false otherwise
	private static final LeftCosetSmallerIsomorphFinder mapFinder = new LeftCosetSmallerIsomorphFinder();
	private static boolean processSymmetry(int[] oldModel, int[] nextModel,
			CNF reducedCNF, int[] agreement) {

		boolean similar = true; //returns !similar
		LiteralGroup lg = globalGroup;
		LiteralPermutation perm = null;
		//		perm = mapFinder.getMapIfPossible(oldModel,nextModel,lg);
		int[] oldModelNoAg = removeAgreement(oldModel,agreement);
		int[] newModelNoAg = removeAgreement(nextModel,agreement);
		//			perm = mapFinder.getMapIfPossible(oldModelNoAg,newModelNoAg,lg);
		ModelMapper mapper = new ModelMapper(reducedCNF);
		similar = mapper.canMap(oldModelNoAg,newModelNoAg);


		//		if((perm == null) == similar) {
		//			System.err.println("Disagreement!");
		//			perm = mapFinder.getMapIfPossible(oldModel,nextModel,lg);
		//			mapper = new ModelMapper(origCNF);
		//			similar = mapper.canMap(oldModel,nextModel);
		//		}



		if(!similar) {
			//			RealSymFinder finder = new RealSymFinder(reducedCNF);
			//			lg =  finder.getSymGroup();
			mapper = globMapper;
			similar = mapper.canMap(oldModel,nextModel);


			//			if((perm == null) == similar) {
			//				System.err.println("Disagreement!");
			//				perm = mapFinder.getMapIfPossible(oldModelNoAg,newModelNoAg,lg);
			//				mapper = new ModelMapper(reducedCNF);
			//				similar = mapper.canMap(oldModelNoAg,newModelNoAg);
			//			}
		}

		//This is test code
		//		if(perm == null) {
		//			LitsSet set = new LitsSet(reducedCNF.getContext().size());
		//			LinkedList<int[]> toCompute = new LinkedList<int[]>();
		//			toCompute.add(oldModel);
		//			set.add(oldModel);
		//			
		//			while(!toCompute.isEmpty()) {
		//				int[] cur = toCompute.poll();
		//				for(LiteralPermutation p : lg.getGenerators()) {
		//					int[] next = p.applySort(cur);
		//					
		//					if(next.equals(nextModel)) {
		//						perm = mapFinder.getMapIfPossible(oldModel,nextModel,lg);
		//						throw new RuntimeException();
		//					} else if(!set.contains(next)) {
		//						toCompute.add(next);
		//						set.add(next);
		//					}
		//				}
		//			}
		//			
		//		}

		return !similar; 
		//				mapFinder.getMapIfPossible(oldModel,nextModel,lg) == null &&
		//				 == null;
	}

	private static int[] removeAgreement(int[] oldModel, int[] agreement) {
		int[] ret = new int[oldModel.length-agreement.length];
		int retIndex = 0;
		int agreementIndex = 0;

		for(int k = 0; k < oldModel.length; k++) {
			if(agreementIndex < agreement.length && oldModel[k] == agreement[agreementIndex]) {
				agreementIndex++;
			} else {
				ret[retIndex] = oldModel[k];
				retIndex++;
			}

		}

		return ret;
	}

	private static CNF getFormulaFromAgreement(CNF function, int[] agree) {
		CNF curFunction = function;
		curFunction = curFunction.substAll(agree);
		return curFunction.trySubsumption();//.trySubsumption().reduce();//
	}

	private static int[] getAgreement(int[] oldModel, int[] nextModel) {
		int numAgree = 0;

		for(int k = 0; k < oldModel.length; k++) {
			if(oldModel[k] == nextModel[k]) {
				numAgree++;
			}
		}

		int[] ret = new int[numAgree];
		int retIndex = 0;
		for(int k = 0; k < oldModel.length; k++) {
			if(oldModel[k] == nextModel[k]) {
				ret[retIndex] = oldModel[k];
				retIndex++;
			}
		}

		return ret;
	}

	private static int[] getRejection(int[] firstModel) {
		int[] i = new int[firstModel.length];

		for(int k = 0; k < i.length; k++) {
			i[k] = -firstModel[k];
		}

		return i;
	}


	//add condition -> (firstCyc -> map)
	//or !condition OR !firstCyc OR map
	private static void addSymBreakClauses(LiteralGroup globalGroup, int[] condition,
			ISolver solver) throws ContradictionException {
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
			int firstCyc = perm.getFirstUnstableVar();
			if(firstCyc != 0) {
				int map = perm.imageOf(firstCyc);

				int[] clause;
				if(map == -firstCyc) {
					clause = new int[condition.length+1];	
				} else {
					clause = new int[condition.length+2];
				}



				for(int k = 0; k < condition.length; k++) {
					clause[k] = -condition[k];
				}

				clause[condition.length] = -firstCyc;
				if(map != -firstCyc) {
					clause[condition.length+1] = map;
				}
				LitSorter.inPlaceSort(clause);

				solver.addClause(new VecInt(clause));
			}
		}

	}

	private static void addFullSymBreakClauses(LiteralGroup globalGroup, int[] condition,
			ISolver solver) throws ContradictionException {
		for(LiteralPermutation perm : globalGroup.getGenerators()) {
			addFullBreakingClauseForPerm(condition, solver, perm);

		}
	}

	private static void addFullBreakingClauseForPerm(int[] condition,
			ISolver solver, LiteralPermutation perm)
			throws ContradictionException {
		LinkedList<Integer> unstableVarsSeenSoFar = new LinkedList<Integer>();
		HashSet<IntPair> pairsSeen = new HashSet<IntPair>();
		for(int k = 1; k <= perm.size(); k++) {
			int map = perm.imageOf(k);

			if(map == k) continue;

			if(map == -k && unstableVarsSeenSoFar.size() != 0) {
				break; //Everything will be tautalogous since map will never be equal to k
			}

			IntPair oppPair = new IntPair(map,k);
			if(pairsSeen.contains(oppPair)) continue; //Represents the end of a cycle.

			IntPair truePair = new IntPair(k,map);

			int equalVar = 0;
			if(k < perm.size() && equalVars.containsKey(truePair)) {
				equalVar = equalVars.get(truePair);
			} else {
				equalVar = solver.newVar(solver.nVars()+1);
				equalVars.put(truePair,equalVar);
				int[] newClause1 = new int[3];
				newClause1[0] = -k;
				newClause1[1] = -map;
				newClause1[2] = equalVar; //(k AND map) -> equal

				int[] newClause2 = new int[3];
				newClause2[0] = k;
				newClause2[1] = map;
				newClause2[2] = equalVar; //(!k AND !map) -> equal

				int[] newClause3 = new int[3];
				newClause3[0] = k;
				newClause3[1] = -map;
				newClause3[2] = -equalVar; //(!k AND map) -> !equal

				int[] newClause4 = new int[3];
				newClause4[0] = -k;
				newClause4[1] = map;
				newClause4[2] = -equalVar; //(k AND !map) -> !equal

				//So equal IFF k == map

				solver.addClause(new VecInt(newClause1));
				solver.addClause(new VecInt(newClause2));
				solver.addClause(new VecInt(newClause3));
				solver.addClause(new VecInt(newClause4));
			}

			int[] clause;
			if(map == -k) {
				clause = new int[condition.length+unstableVarsSeenSoFar.size()+1];	
			} else {
				clause = new int[condition.length+unstableVarsSeenSoFar.size()+2];
			}

			int curIndex = 0;
			for(int i = 0; i < condition.length; i++) {
				clause[curIndex] = -condition[i];
				curIndex++;
			}

			for(int i : unstableVarsSeenSoFar) {
				clause[curIndex] = -i;
				curIndex++;
			}

			unstableVarsSeenSoFar.add(equalVar);

			clause[curIndex] = -k;
			if(map != -k) {
				curIndex++;
				clause[curIndex] = map;
			}
			LitSorter.inPlaceSort(clause);

			solver.addClause(new VecInt(clause));
		}
	}
}
