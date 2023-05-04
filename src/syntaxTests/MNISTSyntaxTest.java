package syntaxTests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import formula.VariableContext;
import formula.simple.CNF;
import group.LiteralGroup;
import group.LiteralPermutation;
import task.formula.MNIST;
import task.formula.ModelsCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import task.translate.FileDecodable;
import util.IntPair;
import util.lit.LitSorter;

public class MNISTSyntaxTest {

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

//		QueensToSAT creator = new QueensToSAT(8);
//								CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat30-1.cnf"));
		//						CNFCreator creator = new SimpleLatinSquareCreator(6);
		//						CNFCreator creator = new ReducedLatinSquareCreator(6);
		//		CNFCreator creator = new LineColoringCreator(10,3);
		//		CNFCreator creator = new LineColoringCreator(10,9);
//						CNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.a.cnf");
//		CNFCreator creator = new IdentityCNFCreator("testcnf\\bw_large.c.cnf");
//		CNFCreator creator = new CNFCreator("testcnf\\bw_large.d.cnf");
//						CNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.c.cnf");
		//						CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat200-1.cnf"));
//								CNFCreator creator = new IdentityCNFCreator("testcnf\\flat200-1.cnf");
		//						CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\sw100-4.cnf"));
//		CNFCreator creator = new ModelsCNFCreator(new AllFilledSquares(11));
//		CNFCreator creator = new ModelsCNFCreator(new CNFCreatorModelGiver(new QueensToSAT(11)));
//		CNFCreator creator = new FormulaCreatorToCNFCreator(new Simple3SATCreator(40,4.26,2));
//		CNFCreator creator = new FormulaCreatorToCNFCreator(new Simple3SATCreator(100,4.26));
		
		CNFCreator creator = new ModelsCNFCreator(new MNIST("t10k-images.idx3-ubyte"));

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
		
		if(creator instanceof ModelsCNFCreator) {
			ModelsCNFCreator mcc = ((ModelsCNFCreator)creator);
			//Our "true true" original vars is different in this case
			origVars = mcc.getOrigVars();
			origCNF = new CNF(new VariableContext());
			
			for(int[] i : mcc.getGiver().getAllModels(origCNF.getContext())) {
				for(int k = 0; k < i.length; k++) {
					i[k] = -i[k];
				}
				origCNF.fastAddClause(i);
			}
			origCNF.getContext().ensureSize(origVars);
			origCNF.sort();
		}
		
		globMapper = new ModelMapper(origCNF);
		
		RealSymFinder finder = new RealSymFinder(function);
		globalGroup =  finder.getSymGroup();

		ArrayList<int[]> curModels = new ArrayList<int[]>();

		ISolver solver = function.getSolverForCNFEnsureVariableUIDsMatch();

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

		ArrayList<int[]> allModels = new ArrayList<int[]>();
		allModels.add(firstModel);
		
		TreeSet<Integer> rem = new TreeSet<Integer>();
		while((nextModel = solver.findModel()) != null) {
			nextModel = getTrueModel(solver.findModel(),origVars);
			allModels.add(nextModel);
			numModels++;
			boolean add = true;

			int[] rejectModel = getRejection(nextModel);
			solver.addClause(new VecInt(rejectModel));
			//			solver.clearLearntClauses();

			//			for(int[] oldModel : curModels) {
			for(int k = 0; k < curModels.size(); k++) {
				int[] oldModel = curModels.get(k);
				int[] agree = getAgreement(oldModel,nextModel);
				agree = removeNegs(agree);
				CNF reducedCNF = getFormulaFromAgreement(origCNF,agree);
				
				if(reducedCNF.size() == 0) {
					add = false;
					continue;
				}
				
				reducedCNF = removeSingleValAndEquivVars(reducedCNF,agree,new VariableContext());
				int newAgSize = 0;
				for(int i : agree) {
					if(i != 0) newAgSize++;
				}
				int[] newAg = new int[newAgSize];
				int newAgInd = 0;
				for(int i : agree) {
					if(i != 0) {
						newAg[newAgInd] = i;
						newAgInd++;
					}
				}
				agree = newAg;
				
				if(add) {
					add &= processSymmetry(oldModel,nextModel,reducedCNF, agree);
				}

				
				finder = new RealSymFinder(reducedCNF);
//				finder.setMaxSyms(10);
				LiteralGroup lg =  finder.getSymGroup();
				addFullSymBreakClauses(lg,agree,solver);

//				if(!add) {
//					break;
//				}
			}

			if(add) {
				curModels.add(nextModel);
				if(creator instanceof FileDecodable) {
					FileDecodable decoder = (FileDecodable)creator;
					decoder.fileDecoding(f, "model_"+num ,nextModel);
					num++;
				}
			}
			System.out.println(curModels.size() +"/" + numModels);

			if(curModels.size() == maxDiv) break;
		}
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
		CNF curFunction = function.substAll(agree);

		return curFunction.unitPropagate().trySubsumption().reduce();//.trySubsumption().reduce();//
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
	
	private static CNF removeSingleValAndEquivVars(CNF reducedCNF, int[] agree, VariableContext context) {
		List<int[]> clauses = reducedCNF.getClauses();
		int[] firstModel = clauses.get(0); 
		int numVars = firstModel.length;

		boolean[] rem = new boolean[numVars];

		//First remove single-valued vars
		for(int k = 1; k <= numVars; k++) {
			if(rem[k-1]) continue;

			int val = firstModel[k-1];


			for(int[] model : clauses) {
				int lit = model[k-1];
				if(val != lit) {
					val = 0;
					break;
				}
			}

			if(val != 0) {
				rem[k-1] = true;
			}
		}

		//Then remove based on equality
		for(int k = 1; k <= numVars; k++) {
			if(rem[k-1]) continue;

			boolean[] areEqual = new boolean[numVars];
			boolean[] areInvEqual = new boolean[numVars];
			Arrays.fill(areEqual,true);
			Arrays.fill(areInvEqual,true);

			for(int[] model : clauses) {
				int lit = model[k-1];
				int sign = lit/Math.abs(lit);

				for(int j = k+1; j <= numVars; j++) {
					if(rem[j-1]) {
						areEqual[j-1] = false;
						areInvEqual[j-1] = false;
						continue;
					}
					int lit2 = model[j-1];
					int sign2 = lit2/Math.abs(lit2);

					if(sign != sign2) {
						areEqual[j-1] = false;
					} else {
						areInvEqual[j-1] = false;
					}
				}
			}

			for(int j = k+1; j <= numVars; j++) {
				if(areEqual[j-1]) {
					rem[j-1] = true;
				} else if(areInvEqual[j-1]) {
					rem[j-1] = true;
				}
			}
		}

		int size = 0;
		for(boolean b : rem) {
			if(!b) size++;
		}

		ArrayList<int[]> retCl = new ArrayList<int[]>(clauses.size());

		for(int[] model : clauses) {
			int[] toAdd = new int[size];
			int toAddInd = 0;

			for(int k = 0; k < model.length; k++) {
				if(!rem[k]) {
					toAdd[toAddInd] = model[k];
					toAddInd++;
				}
			}

			retCl.add(toAdd);
		}

		int remIndex = 0;
		for(int k = 0; k < agree.length; k++) {
			while(Math.abs(firstModel[remIndex]) < Math.abs(agree[k])) {
				remIndex++;
			}
			
			if(rem[remIndex]) {
				agree[k] = 0;
			}
		}

		CNF ret = new CNF(reducedCNF.getContext());
		ret.fastAddAll(retCl);
		ret.sort();
		return ret;
	}
	

	private static int[] removeNegs(int[] agree) {
		int numPos = 0;
		for(int i : agree) {
			if(i > 0) numPos++;
		}
		
		int[] ret = new int[numPos];
		int retInd = 0;
		for(int i : agree) {
			if(i > 0) {
				ret[retInd] = i;
				retInd++;
			}
		}
		
		return ret;
	}
}
