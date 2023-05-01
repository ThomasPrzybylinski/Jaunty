package KnowCompTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import subsumptionMain.ResolutionTest;
import subsumptionMain.SATSump;
import task.formula.FormulaCreator;
import task.formula.random.CNFCreator;
import task.formula.random.Simple3SATCreator;
import task.formula.random.SimpleCNFCreator;
import task.formula.random.SimpleUnique3SATCreator;
import task.sat.SATSolvable;
import util.BinaryTrie;
import util.formula.FormulaForAgreement;

public class CopyOfFirstTest {
	static Random rand = new Random(2);
	static BinaryTrie bt = new BinaryTrie();
	public static void main(String[] args) throws TimeoutException{
		int minNumVars = 5;
		int maxNumVars = 50;
		int iters = 1;

		for(int i = minNumVars; i < maxNumVars; i++) {
			CNFCreator creat = new SimpleCNFCreator(i,2,3,1);
			System.out.print(i+"\t");
			
			int max = Integer.MIN_VALUE;
			for(int k = 0; k < iters; k++) {
				CNF test = null;
				boolean sat = false;
				while(!sat) {
					test = creat.generateCNF(VariableContext.defaultContext);
					try {
						ISolver solver = test.getSolverForCNFEnsureVariableUIDsMatch();
						sat = solver.isSatisfiable();
						solver.reset();
					} catch(ContradictionException ce) {}
				}
				
//				test = SATSump.getSubsumedConj(test);
				test = SATSump.getPrimify(test);
//				test.addAll(ResolutionTest.getResolvants(test));
//				test.trySubsumption();
				RenameHornUtil.renameToMaximizeTotalNegLits(test);
				RenameHornUtil.renameToGreedyMinNonHornPosNumProduct(test);
				int num = exhaustiveMinRenameHornCover(test);
//				System.out.println(num);
				max = Math.max(max,num);
//				System.out.println(num);
//				System.out.println();
			}
			System.out.println(max);
		}
	}

	public static int exhaustiveMinRenameHornCover(CNF generateCNF) throws TimeoutException{
		boolean[] choices = new boolean[generateCNF.getContext().size()+1];
		bt.clear();
		return exhaustiveMinRenameHornCover(generateCNF,choices,Integer.MAX_VALUE);
	}
	
	
	enum Phase {NA, POS, NEG, BOTH};
	public static int exhaustiveMinRenameHornCover(CNF generateCNF,boolean[] chosen, int maxNum) throws TimeoutException{
//		if(maxNum == 1) return 1;
		generateCNF = generateCNF.unitPropagate().trySubsumption();
//		generateCNF = SATSump.getPrimify(generateCNF);
//		generateCNF = SATSump.getSubsumedConj(generateCNF); 
		if(generateCNF == CNF.contradiction 
				|| (generateCNF.getClauses().size() == 1 && generateCNF.getClauses().get(0).length == 0)) return 0;
		if(generateCNF == CNF.tautology) return 1;
		
		Phase[] varPhases = new Phase[generateCNF.getContext().size()+1];
//		getVarPhasesInCNF(generateCNF, varPhases);
		
		ArrayList<Integer> modifiedChosenInds = new ArrayList<Integer>();
		int numChosen = 0;
		for(int k = 1; k < varPhases.length; k++) {
//			if(!chosen[k] && varPhases[k] != Phase.BOTH) {
//				chosen[k] = true;
//				modifiedChosenInds.add(k);
//			}
//			
			if(chosen[k]) numChosen++;
		}
		
		if(numChosen == chosen.length-1) return 1; //Super-easy rename to HORN
		
//		if(RenameHornUtil.isHorn(generateCNF)) {
//			resetChosen(chosen, modifiedChosenInds);
//			return 1;
//		}
//		
		if(RenameHornUtil.isRenameHorn(generateCNF)) {
////			try {
////				if(generateCNF.getSolverForCNFEnsureVariableUIDsMatch().isSatisfiable())
////					return 1;
////			}catch(ContradictionException ce) {}
//
////			return 0;
//			System.out.print("H");
			resetChosen(chosen, modifiedChosenInds);
			return 1;

		}
//		
//		if(RenameHornUtil.is3SATAffine(generateCNF)) {
////			System.out.print("A");
//			resetChosen(chosen, modifiedChosenInds);
//			return 1;
//			
//		}
		
		int myMax = (maxNum+1) > 0 ? maxNum+1 : Integer.MAX_VALUE;
		for(int k = 1; k < chosen.length; k++) {
			if(!chosen[k]) {
//				myMax = doSearch(generateCNF, chosen, myMax, k);//
				myMax =  Math.min(myMax, doSearch(generateCNF, chosen, myMax, k));
			}
		}
		
		resetChosen(chosen, modifiedChosenInds);
		
		return myMax;
	}

	private static void resetChosen(boolean[] chosen,
			ArrayList<Integer> modifiedChosenInds) {
		for(int i : modifiedChosenInds) {
			chosen[i] = false;
		}
	}

	private static int doSearch(CNF generateCNF, boolean[] chosen, int myMax,
			int k) throws TimeoutException {
		chosen[k] = true;
		
		if(bt.exists(chosen)) {
			chosen[k] = false;
			return bt.getNum();
		}
		
		int size1 = exhaustiveMinRenameHornCover(generateCNF.subst(-k,true),chosen,myMax-1);
		
		int size2 = 0;
		if(size1 < myMax) {
			size2 = exhaustiveMinRenameHornCover(generateCNF.subst(k,true),chosen,myMax-size1);
		}
		
		if(size1 + size2 < myMax) {
			myMax = (size1+size2);
		}
		
		bt.add(chosen,myMax);
		chosen[k] = false;
		return myMax;
	}

	private static void getVarPhasesInCNF(CNF generateCNF, Phase[] varPhases) {
		Arrays.fill(varPhases,Phase.NA);
		
		for(int[] cl : generateCNF.getClauses()) {
			for(int l : cl) {
				int var = Math.abs(l);
				
				if(varPhases[var] == Phase.NA) {
					varPhases[var] = l > 0 ? Phase.POS : Phase.NEG;
				} else if(l < 0 && varPhases[var] == Phase.POS) {
					varPhases[var] = Phase.BOTH;
				} else if(l > 0 && varPhases[var] == Phase.NEG) {
					varPhases[var] = Phase.BOTH;
				}
			}
		}
	}
	
	private static int countRandRenameHornCover(CNF generateCNF) throws TimeoutException{
//		FormulaForAgreement work = new FormulaForAgreement(generateCNF);
		generateCNF = generateCNF.unitPropagate().trySubsumption();
		generateCNF = SATSump.getSubsumedConj(generateCNF);
		if(generateCNF == CNF.contradiction 
				|| (generateCNF.getClauses().size() == 1 && generateCNF.getClauses().get(0).length == 0)) return 0;
		if(generateCNF == CNF.tautology) return 1;
		

		
		if(RenameHornUtil.isRenameHorn(generateCNF)) {
			return 1;
		}

		int randClause = rand.nextInt(generateCNF.getClauses().size());
//		while(!RenameHornUtil.isHorn( generateCNF.getClauses().get(randClause))) {
//			randClause = rand.nextInt(generateCNF.getClauses().size());
//		}
		int[] theClause = generateCNF.getClauses().get(randClause);
		int markVar = theClause[rand.nextInt(theClause.length)];

		int size1 = countRandRenameHornCover(generateCNF.subst(markVar,true).reduce());
		int size2 = countRandRenameHornCover(generateCNF.subst(-markVar,true).reduce());

		return size1+size2;
	}

}
