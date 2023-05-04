package syntaxTests;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.clustering.SimpleDifference;
import task.formula.FormulaCreatorRandomizer;
import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.MNIST;
import task.formula.ModelsCNFCreator;
import task.formula.QueensToSAT;
import task.formula.QueensToSATForSym;
import task.formula.SimpleLatinSquareCreator;
import task.formula.random.CNFCreator;
import task.sat.SATUtil;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import task.translate.FileDecodable;
import util.IntPair;
import util.lit.LitSorter;
import workflow.CNFCreatorModelGiver;

public class TestSyntaxTest {

	/**
	 * @param args
	 */
	private static LiteralGroup globalGroup;
	private static CNF origCNF;
	private static ModelMapper globMapper;
	private static HashMap<IntPair, Integer> equalVars;

	public static void main(String[] args) throws Exception {
		final int maxDiv = Integer.MAX_VALUE;//20;
		Random rand = new Random();

//						CNFCreator creator = new QueensToSAT(8);
//						CNFCreator creator = new QueensToSATForSym(8);
						CNFCreator creator = new FormulaCreatorRandomizer(new QueensToSATForSym(8),rand);
//						CNFCreator creator = new ModelsCNFCreator(new CNFCreatorModelGiver(new FormulaRandomizer(new QueensToSAT(8),rand)));
//												CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat30-1.cnf"));
//								CNFCreator creator = new SimpleLatinSquareCreator(6);
		//						CNFCreator creator = new ReducedLatinSquareCreator(6);
//				CNFCreator creator = new LineColoringCreator(10,3);
//				CNFCreator creator = new LineColoringCreator(10,9);
//		CNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.a.cnf");
		//		CNFCreator creator = new IdentityCNFCreator("testcnf\\bw_large.c.cnf");
//				CNFCreator creator = new IdentityCNFCreator("testcnf\\bw_large.d.cnf");
		//						CNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.c.cnf");
//										CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat200-1.cnf"));
//										CNFCreator creator = new IdentityCNFCreator("testcnf\\flat200-1.cnf");
		//						CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\sw100-4.cnf"));
//				CNFCreator creator = new ModelsCNFCreator(new AllFilledSquares(20));
//				CNFCreator creator = new ModelsCNFCreator(new AllFilledRectangles(10));
		//		CNFCreator creator = new ModelsCNFCreator(new CNFCreatorModelGiver(new QueensToSAT(11)));
		//		CNFCreator creator = new FormulaCreatorToCNFCreator(new Simple3SATCreator(40,4.26,2));
//						CNFCreator creator = new FormulaCreatorToCNFCreator(new Simple3SATCreator(100,4.26));

//				CNFCreator creator = new ModelsCNFCreator(new MNIST("t10k-images.idx3-ubyte"));
//				CNFCreator creator = new ModelsCNFCreator(new SmallAllModelBoolFormula(14,8192),true);


		File f = null;
		if(creator instanceof FileDecodable) {
			File f1 = new File("SyntaxTest");
			f = new File(f1, creator.toString());
			f.mkdirs();

			for(File del : f.listFiles()) {
				del.delete();
			}
		}

		VariableContext context = new VariableContext();
		CNF function = creator.generateCNF(context);
		int origVars = context.size();



		equalVars = new HashMap<IntPair, Integer>();

		origCNF = function;

		ClauseList debug = null;
		if(creator instanceof ModelsCNFCreator) {
			ModelsCNFCreator mcc = ((ModelsCNFCreator)creator);
			if(mcc.getPrevMods() != null) {
				//Our "true true" original vars is different in this case
				origVars = mcc.getOrigVars();
				origCNF = new CNF(new VariableContext());
				debug = new ClauseList(VariableContext.defaultContext);

				for(int[] i : mcc.getPrevMods()) {
					i = i.clone();
					debug.addClause(i.clone());
					for(int k = 0; k < i.length; k++) {
						i[k] = -i[k];
					}
					origCNF.fastAddClause(i);
				}
				origCNF.getContext().ensureSize(origVars);
				origCNF.sort();
			}
		}

		globMapper = new ModelMapper(origCNF);

		RealSymFinder finder = new RealSymFinder(origCNF);
		globalGroup =  finder.getSymGroup();

		ArrayList<int[]> curModels = new ArrayList<int[]>();

		ISolver solver = function.getSolverForCNFEnsureVariableUIDsMatch();
//		ISolver fullSolver = function.getSolverForCNFEnsureVariableUIDsMatch();
//		ModelIterator iter = new ModelIterator(fullSolver);
//				ArrayList<int[]> allModels = new ArrayList<int[]>();;
//		
//				while(iter.isSatisfiable()) {
//					allModels.add(iter.model());
//				}
//				System.out.println(allModels.size());
//		fullSolver.reset();
//		fullSolver = null;

//		SchreierVector vec = new SchreierVector(globalGroup);
		
//		for(int k = -origVars; k <= origVars; k++) {
//			if(k == 0) continue;
//			for(int i = k+1; i <= origVars; i++) {
//				if(i == 0) continue;
//				LiteralPermutation perm = vec.getPerm(k,i);
//				if(perm != null) {
//					addSmallSymBreakClause(new int[]{},solver,perm);
//				}
//			}
//		}
		
		addFullSymBreakClauses(globalGroup, new int[]{}, solver);

		int[] firstModel = getTrueModel(solver.findModel(),origVars);
		if(firstModel == null)  {
			System.out.println("Unsatisfiable Theory");
			System.exit(0);
		}
		curModels.add(firstModel);

		if(creator instanceof FileDecodable) {
			FileDecodable decoder = (FileDecodable)creator;
			decoder.fileDecoding(f, "model_"+ 0 ,firstModel);

		}

		int num = 1;

		int[] rejectFirstModel = getRejection(firstModel);
		solver.addClause(new VecInt(rejectFirstModel));
		//		solver.clearLearntClauses();

		int numModels = 1;

		int[] nextModel;

		TreeSet<Integer> rem = new TreeSet<Integer>();
		int numSinceNew = 0;
		while((nextModel = solver.findModel()) != null) {
			numSinceNew++;
			nextModel = getTrueModel(nextModel,origVars);
			numModels++;
			boolean add = true;

			int[] rejectModel = getRejection(nextModel);
			solver.addClause(new VecInt(rejectModel));
			//			solver.clearLearntClauses();

			//			for(int[] oldModel : curModels) {
//			for(int k = 0; k < curModels.size(); k++) {
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
				
				if(!add || solver.nConstraints() <= 5*origCNF.size()) {// || numSinceNew > 10) {
					TreeSet<Integer> newCond = new TreeSet<Integer>();
					finder = new RealSymFinder(reducedCNF);
					LiteralGroup lg =  finder.getSymGroup();
					
					for(LiteralPermutation perm : lg.getGenerators()) {
						int[] cond = globMapper.greedyFindLessRestrictiveConditionForPerm(agree,perm);
						for(int condL : cond) {
							newCond.add(condL);
						}
					}
					if(newCond.size() < agree.length) {
						agree = new int[newCond.size()];
						int index = 0;
						for(int lit : newCond) {
							agree[index] = lit;
							index++;
						}
					}
					
					addFullSymBreakClauses(lg,agree,solver);
				}

				if(!add && solver.nConstraints() > 5*origCNF.size()) {
//					
					if(numSinceNew > 0) {
						for(int i = k-1; i >= 0 ; i--) {
							oldModel = curModels.get(k);
							agree = getAgreement(oldModel,nextModel);
							
							int[] oldModelNoAg = removeAgreement(oldModel,agree);
							int[] newModelNoAg = removeAgreement(nextModel,agree);
							
							ModelMapper mapper = new ModelMapper(reducedCNF);
							mapper.canMap(oldModelNoAg,newModelNoAg);
							LiteralPermutation perm = mapper.getFoundPerm();

							if(perm != null) {
								int[] cond = globMapper.greedyFindLessRestrictiveConditionForPerm(agree,perm);
								if(cond.length < agree.length) {
									System.out.println(cond.length-agree.length);
									agree = cond;
								}
//								System.out.println(agree.length);
//								System.out.println(cond.length);								
//								System.out.println(agree.length-cond.length);
//								System.out.println();
								addFullBreakingClauseForPerm(agree,solver,perm);
							}
							
							agree = getAgreeSubset(agree,rand);
							reducedCNF = getWeakFormulaFromCondition(origCNF,agree);
							oldModelNoAg = removeAgreement(oldModel,agree);
							newModelNoAg = removeAgreement(nextModel,agree);
							
							mapper = new ModelMapper(reducedCNF);
							mapper.canMap(oldModelNoAg,newModelNoAg);
							perm = mapper.getFoundPerm();
							if(perm != null) {
								addFullBreakingClauseForPerm(agree,solver,perm);
							}
							
							
							mapper = globMapper;
							mapper.canMap(oldModel,nextModel);
							perm = mapper.getFoundPerm();
							if(perm != null) {
								addFullBreakingClauseForPerm(agree,solver,perm);
							}
						}
					}
//					break;
				}
			}
			if(add) {
				numSinceNew = 0;
				curModels.add(nextModel);
				if(creator instanceof FileDecodable) {
					FileDecodable decoder = (FileDecodable)creator;
					decoder.fileDecoding(f, "model_"+num ,nextModel);
					num++;
				}
			}
			//			System.out.println(allModels.size());
			System.out.println(curModels.size() +"/" + numModels);

			if(curModels.size() == maxDiv) break;
		}
	
	}
	
	private static int[] getAgreeSubset(int[] agree, Random rand) {
		int retLen = rand.nextInt(agree.length)+1;
		int[] ret = new int[retLen];
		ArrayList<Integer> indecies = new ArrayList<Integer>(agree.length);
		for(int k = 0; k < agree.length; k++) {
			indecies.add(k);
		}
		Collections.shuffle(indecies,rand);
		for(int k = 0; k < agree.length - ret.length; k++) {
			indecies.remove(indecies.size()-1);
		}
		Collections.sort(indecies);
		
		if(indecies.size() != ret.length) {
			throw new RuntimeException();
		}
		
		for(int k = 0; k < indecies.size(); k++) {
			ret[k] = agree[indecies.get(k)];
		}
		
		return ret;
	}

	private static ArrayList<int[]> getRandomClustRep(Random rand,
			List<List<int[]>> curClusts) {
		ArrayList<int[]> randSymClust = new ArrayList<int[]>(curClusts.size());
		for(List<int[]> aClust : curClusts) {
			int amnt = rand.nextInt(aClust.size());
			randSymClust.add(aClust.get(amnt));
		}
		return randSymClust;
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
	private static boolean processSymmetry(int[] oldModel, int[] nextModel,
			CNF reducedCNF, int[] agreement) {

		boolean similar = true; //returns !similar
		
		int[] oldModelNoAg = removeAgreement(oldModel,agreement);
		int[] newModelNoAg = removeAgreement(nextModel,agreement);
		
		ModelMapper mapper = new ModelMapper(reducedCNF);
		similar = mapper.canMap(oldModelNoAg,newModelNoAg);

		if(!similar) {
			mapper = globMapper;
			similar = mapper.canMap(oldModel,nextModel);
		}

		return !similar; 
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
		if(agree.length == 0) return function;
		CNF curFunction = function.substAll(agree);
		return curFunction.trySubsumption();//.trySubsumption().reduce();//
	}
	
	private static CNF getWeakFormulaFromCondition(CNF function, int[] condition) {
		if(condition.length == 0) return function;
		CNF curFunction = function.substAll(condition);
		return curFunction;//.reduce();//
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
			addSmallSymBreakClause(condition, solver, perm);
		}

	}

	private static void addSmallSymBreakClause(int[] condition, ISolver solver,
			LiteralPermutation perm) throws ContradictionException {
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

}
