package KnowCompTests;

import java.util.Random;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import subsumptionMain.SATSump;
import task.formula.IdentityCNFCreator;
import task.formula.random.CNFCreator;
import util.BinaryTrie;
import util.lit.LitsMap;

public class FirstTest {
	static Random rand = new Random(3);
	static BinaryTrie bt = new BinaryTrie();

	private static final int unchosen = 0;
	private static final int pos = 1;
	private static final int neg = -1;
	
	private static final int MAX_BRANCHING = Integer.MAX_VALUE;//1;

	public static void main(String[] args) throws TimeoutException{
		int minNumVars = 5;
		int maxNumVars = 1000;
		int iters = 10;//Integer.MAX_VALUE;

		for(int i = minNumVars; i <= maxNumVars; i++) {
			CNFCreator creat = new IdentityCNFCreator("testcnf\\uf20-02.cnf");//new SimpleCNFCreator(i,2,3);//new LineColoringCreator(i,3); //
			System.out.print(i+"\t");

			int max = Integer.MIN_VALUE;
			for(int k = 0; k < iters; k++) {
				CNF test = null;
				boolean sat = false;
				while(!sat) {
					test = creat.generateCNF(VariableContext.defaultContext);
//					test = makeAffine(test);
					try {
						ISolver solver = test.getSolverForCNFEnsureVariableUIDsMatch();
						sat = solver.isSatisfiable();
						solver.reset();
					} catch(ContradictionException ce) {}
				}

//				test = SATSump.getPrimify(test);
//				RenameHornUtil.renameToMaximizeTotalNegLits(test);
//				RenameHornUtil.renameToGreedyMinNonHornPosNumProduct(test);
	
				int num = exhaustiveMinRenameHornCover2(test);


				//				if(num == 0) {
				//					int num2 = exhaustiveMinRenameHornCover(test);
				//					System.out.println(num2);
				//				}
				//				System.out.println(num);
				max = Math.max(max,num);
//				System.out.println(k+" "+max);
				//				System.out.println(num);
				//				System.out.println();
			}
			System.out.println(max);
		}
	}

	private static CNF makeAffine(CNF test) {
		CNF ret = new CNF(test.getContext());
		for(int[] cl : test.getClauses()) {
			if(cl.length == 1) {
				ret.fastAddClause(cl);
			} else if(cl.length == 2) {
				ret.fastAddClause(cl[0],cl[1]);
				ret.fastAddClause(-cl[0],-cl[1]);
			} else if(cl.length == 3) {
				ret.fastAddClause(cl[0],cl[1],cl[2]);
				ret.fastAddClause(-cl[0],-cl[1],cl[2]);
				ret.fastAddClause(-cl[0],cl[1],-cl[2]);
				ret.fastAddClause(cl[0],-cl[1],-cl[2]);
			}
		}
		ret.sort();
		return ret.trySubsumption();
	}

	public static int exhaustiveMinRenameHornCover2(CNF generateCNF) throws TimeoutException{
		int[] choices = new int[generateCNF.getContext().size()+1];
		LitsMap<Integer> map = new LitsMap<Integer>(1);
		return exhaustiveMinRenameHornCover2(generateCNF,choices,map,Integer.MAX_VALUE);
	}




	private static int exhaustiveMinRenameHornCover2(CNF generateCNF,
			int[] choices, LitsMap<Integer> map, int maxValue) throws TimeoutException {
//		if(maxValue <= 0) return Integer.MAX_VALUE;

		if(map.contains(choices)) {
			return map.get(choices);
		}

		int ret = Integer.MIN_VALUE;

		//		generateCNF = generateCNF.getDirectCopy();
//				generateCNF = SATSump.getPrimify(generateCNF);
		{
			CNF oldCNF = null;

			while(oldCNF != generateCNF) {
				oldCNF = generateCNF;
				generateCNF = generateCNF.unitPropagate();
			}
		}

		generateCNF = generateCNF.trySubsumption();

		int numChosen = getNumChosen(choices);
		
		if(generateCNF == CNF.contradiction 
				|| (generateCNF.getClauses().size() == 1 && generateCNF.getClauses().get(0).length == 0)) {
			ret = 0;
		} else if(maxValue < 1) {//numChosen) {
			ret = Integer.MAX_VALUE; //cannot work
		}		
		else if(generateCNF == CNF.tautology || (generateCNF.getClauses().size() == 0)) {
			ret = 1;//numChosen+1;
		}
		else if(RenameHornUtil.isHorn(generateCNF)) {
			ret = generateCNF.getDeepSize()+numChosen;
		}
//		else if(RenameHornUtil.is3SatRenameHorn(generateCNF)) {
//			ret = 1;//generateCNF.getDeepSize()+numChosen;
//		} 
//		else if(RenameHornUtil.is3SATAffine(generateCNF)) {
//			ret = 1;//generateCNF.getDeepSize()+numChosen;
//		} 		
		else {
			boolean[] exists = new boolean[generateCNF.getContext().size()+1];
			
			for(int[] cl : generateCNF.getClauses()) {
				for(int i : cl) {
					exists[Math.abs(i)] = true;
				}
			}
			
			int myMax = maxValue;
			int num = 0;
			for(int k = 1; k < choices.length && num < MAX_BRANCHING; k++) {
				if(choices[k] == unchosen && exists[k]) {
					num++;
					myMax =  Math.min(myMax, doSearch2(generateCNF, choices, map, myMax, k));
				}
			}
			ret = myMax;
		}
//		map.put(choices,ret);
		return ret;
	}

	private static int getNumChosen(int[] choices) {
		int numChosen = 0;
		for(int i : choices) {
			if(i != unchosen) {
				numChosen++;
			}
		}
		return numChosen;
	}

	private static int doSearch2(CNF generateCNF, int[] chosen, LitsMap<Integer> map, int myMax,
			int k) throws TimeoutException {

		int ret = -1;
		chosen[k] = neg;
		int size1 = exhaustiveMinRenameHornCover2(generateCNF.subst(-k,true),chosen,map,myMax-1);
		if(size1 <= myMax-1) {
			chosen[k] = pos;
			int size2 = exhaustiveMinRenameHornCover2(generateCNF.subst(k,true),chosen,map,myMax-size1-1);
			ret = size1+size2;
			
			if(size2 == Integer.MAX_VALUE) {
				ret = Integer.MAX_VALUE; //prevent overflow
			}
			
		} else {
			ret = Integer.MAX_VALUE;
		}

		chosen[k] = unchosen;

		return ret;
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
