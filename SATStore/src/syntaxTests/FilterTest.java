package syntaxTests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;

import task.formula.ColoringCNFDecoder;
import task.formula.IdentityCNFCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import task.translate.FileDecodable;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;

public class FilterTest {

	private static LiteralGroup globalGroup;
	public static void main(String[] args) throws Exception{
		CNFCreator creator = new QueensToSAT(10);
//				CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat30-1.cnf"));
		//		SimpleLatinSquareCreator creator = new SimpleLatinSquareCreator(6);
		//		LineColoringCreator creator = new LineColoringCreator(6,3);
		//		IdentityCNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.a.cnf");
		//		IdentityCNFCreator creator = new IdentityCNFCreator("testcnf\\bw_large.c.cnf");
		//		IdentityCNFCreator creator = new IdentityCNFCreator("testcnf\\logistics.b.cnf");
//		CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat200-1.cnf"));
		//		CNFCreator creator = new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\sw100-4.cnf"));

		
		ModelGiver giver = new CNFCreatorModelGiver(creator);
		
		File f = null;
		if(giver.getFileDecodabler() != null) {
			File f1 = new File("SyntaxTest");
			f = new File(f1, creator.toString());
			f.mkdirs();
		}

		VariableContext context = new VariableContext();
//		CNF function = creator.generateCNF(context);
		List<int[]> models = giver.getAllModels(context);
		ClauseList clauses = new ClauseList(context);
		clauses.addAll(models);
		
		

		RealSymFinder finder = new RealSymFinder(clauses);
		globalGroup =  finder.getSymGroup();

		//		List<int[]> allMods = SATUtil.getAllModels(function);
		//		ClauseList cl = new ClauseList(function.getContext());
		//		cl.addAll(allMods);
		//		RealSymFinder finder2 = new RealSymFinder(cl);
		//		LiteralGroup globalGroup2 =  finder2.getSymGroup();

		ArrayList<int[]> curModels = new ArrayList<int[]>();

		curModels.add(models.get(0));

		if(giver.getFileDecodabler() != null) {
			FileDecodable decoder = giver.getFileDecodabler();
			decoder.fileDecoding(f, "model_"+ 0 ,models.get(0));

		}

		int num = 1;

		int numModels = 1;

		int[] nextModel;

		LocalSymClauses manip = new LocalSymClauses(clauses);


		for(int k = 1; k < models.size(); k++) {
			nextModel = models.get(k);
			numModels++;
			boolean add = true;

			for(int[] oldModel : curModels) {
				int[] agree = getAgreement(oldModel,nextModel);
				manip.setFilter(agree);
				add = processSymmetry(oldModel,nextModel,manip.getCurList(),add, agree);

				if(!add) break; //May change if adding sym breaking clauses
			}

			if(add) {
				curModels.add(nextModel);
				if(giver.getFileDecodabler() != null) {
					FileDecodable decoder = giver.getFileDecodabler();
					decoder.fileDecoding(f, "model_"+num ,nextModel);
					num++;
				}
			}
			System.out.println(curModels.size() +"/" + numModels);
		}



		num = 0;

		for(int[] model : curModels) {

			//			System.out.println(creator.consoleDecoding(model));
			System.out.println(Arrays.toString(model));
			//			if(giver.getFileDecodabler() != null) {
			//				FileDecodable decoder = (FileDecodable)creator;
			//				decoder.fileDecoding(new File("SyntaxTest"), creator.toString() + "_"+num ,model);
			//				num++;
			//			}
		}

		System.out.println(curModels);
	}


	private static final LeftCosetSmallerIsomorphFinder mapFinder = new LeftCosetSmallerIsomorphFinder();
	private static boolean processSymmetry(int[] oldModel, int[] nextModel,
			ClauseList reducedCNF, boolean add, int[] agreement) {
		LiteralGroup lg = globalGroup;
		//		LiteralPermutation perm = null;
		LiteralPermutation perm = mapFinder.getMapIfPossible(oldModel,nextModel,lg);
		if(perm == null) {
			RealSymFinder finder = new RealSymFinder(reducedCNF);
			lg =  finder.getSymGroup();
			int[] oldModelNoAg = removeAgreement(oldModel,agreement);
			int[] newModelNoAg = removeAgreement(nextModel,agreement);
			perm = mapFinder.getMapIfPossible(oldModelNoAg,newModelNoAg,lg);
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

		return perm == null; 
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
		return curFunction.unitPropagate().reduce();
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



}

