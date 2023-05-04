package KnowCompTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import subsumptionMain.SATSump;
import task.formula.random.CNFCreator;
import task.formula.random.SimpleCNFCreator;
import util.lit.LitsMap;

enum TractableType{Tautology,Horn,RenameHorn,Affine};

class Rep {
	TractableType type;
	int[] pi;
	int repSize;
	CNF rep;

	Rep next;
	Rep prev;

	public Rep(CNF rep, int[] pi, TractableType type) {
		this.type = type;
		repSize = rep.getDeepSize() + pi.length;
		this.pi = pi;
		this.rep = rep;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(':').append(Arrays.toString(pi));
		sb.append("\n");
		sb.append(rep);
		sb.append("\n");
		return sb.toString();
	}


}

class RepList {
	Rep head;
	Rep tail;
	int size = 0;

	public void add(Rep next) {
		if(tail == null) {
			head = next;
			tail = next;
		} else {
			tail.next = next;
			next.prev = tail;
			tail = next;
		}
		size++;
	}

	public void add(RepList next) {
		if(next.head == null) return;
		size += next.size-1;//add() adds 1 to size

		add(next.head);
		this.tail = next.tail; //if this is empty
	}
	
	public RepList getCopy() {
		RepList ret = new RepList();
		Rep cur = head;
		while(cur != null) {
			Rep copy = new Rep(cur.rep,cur.pi,cur.type);
			ret.add(copy);
			cur = cur.next;
		}
		return ret;
	}

	@Override
	public String toString() {
		Rep cur = head;
		StringBuilder sb = new StringBuilder();
		while(cur != null) {
			sb.append(cur);
			cur = cur.next;
		}

		return sb.toString();
	}


}

public class RepTest {
	static Random rand = new Random(3);

	private static final int unchosen = 0;
	private static final int pos = 1;
	private static final int neg = -1;

	private static int MAX_BRANCHING = Integer.MAX_VALUE;//2;1;

	public static void main(String[] args) throws Exception{
		int minNumVars = 3;
		int maxNumVars = 100;
		int iters = 100;//Integer.MAX_VALUE;

		for(int i = minNumVars; i <= maxNumVars; i++) {
//			MAX_BRANCHING = (int)Math.max(1,Math.log(i)/Math.log(2));
			CNFCreator creat = new SimpleCNFCreator(i,2.1,3,1);//new TempCNFCreator(i,3);//new IdentityCNFCreator("testcnf\\uf20-02.cnf");//new LineColoringCreator(i,3); //
			System.out.print(i+"\t");

			RepList bestRep = null;
			CNF curBest = null;
			for(int k = 0; k < iters; k++) {
				CNF test = null;
				boolean sat = false;
				while(!sat) {
					test = creat.generateCNF(VariableContext.defaultContext);
//										test = makeAffine(test);
					try {
						ISolver solver = test.getSolverForCNFEnsureVariableUIDsMatch();
						sat = solver.isSatisfiable();
						solver.reset();
					} catch(ContradictionException ce) {}
				}
				test = test.trySubsumption();
								test = SATSump.getPrimify(test);
				//				RenameHornUtil.renameToMaximizeTotalNegLits(test);
				//				RenameHornUtil.renameToGreedyMinNonHornPosNumProduct(test);

				RepList rep = exhaustiveMinRenameHornCover2(test);
				int size = rep == null ? 0 : rep.size;

				if(bestRep == null || rep.size > size) {
					bestRep = rep;
					curBest = test;
				}
			}
			System.out.println(bestRep == null ? 0 : bestRep.size);
//			System.out.println(curBest);
//			System.out.println(bestRep);
		}
	}


	public static RepList exhaustiveMinRenameHornCover2(CNF generateCNF) throws TimeoutException{
		int[] choices = new int[generateCNF.getContext().size()+1];
		LitsMap<RepList> map = new LitsMap<RepList>(choices.length);
		return exhaustiveMinRenameHornCover2(generateCNF,choices,map,Integer.MAX_VALUE);
	}




	private static RepList exhaustiveMinRenameHornCover2(CNF generateCNF,
			int[] choices, LitsMap<RepList> map, int maxValue) throws TimeoutException {

		RepList ret = new RepList();

		//		generateCNF = generateCNF.getDirectCopy();
						generateCNF = SATSump.getPrimify(generateCNF);

		ArrayList<Integer> propped = new ArrayList<Integer>(generateCNF.getContext().size());
		{
			CNF oldCNF = null;

			while(oldCNF != generateCNF) {
				oldCNF = generateCNF;

				generateCNF = generateCNF.unitPropagate(propped);
			}
		}

		for(int i : propped) {
			choices[Math.abs(i)] = i > 0 ? pos : neg;
		}

		generateCNF = generateCNF.trySubsumption();

		int numChosen = getNumChosen(choices);
		int[] pi = getPI(choices,numChosen);
		boolean contained = false;
		
		if(map.contains(pi)) {
			contained = true;
			ret = map.get(pi).getCopy();
		}else if(generateCNF == CNF.contradiction 
				|| (generateCNF.getClauses().size() == 1 && generateCNF.getClauses().get(0).length == 0)) {
			//Return the 0 size RepList, no further compilation needed
			//ret = ret;
		} else if(maxValue < 1) {//numChosen) {
			ret = null;//Integer.MAX_VALUE; //cannot work
		}		
		else if(generateCNF == CNF.tautology || (generateCNF.getClauses().size() == 0)) {
			ret.add(new Rep(CNF.tautology,getPI(choices,numChosen),TractableType.Tautology));//numChosen+1;
		}
//		else if(RenameHornUtil.isHorn(generateCNF)) {
//			ret.add(new Rep(generateCNF,getPI(choices,numChosen),TractableType.Horn));
//		}
		else if(RenameHornUtil.isRenameHorn(generateCNF)) {
			ret.add(new Rep(generateCNF,getPI(choices,numChosen),TractableType.RenameHorn));
		} 
//		else if(RenameHornUtil.is3SATAffine(generateCNF)) {
//			ret.add(new Rep(generateCNF,getPI(choices,numChosen),TractableType.Affine));
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

			ret = null;
			for(int k = 1; k < choices.length && num < MAX_BRANCHING; k++) {
				if(choices[k] == unchosen && exists[k]) {
					num++;
					RepList potential = doSearch2(generateCNF, choices, map, myMax, k);

					if(potential != null && potential.size < myMax) {
						myMax = potential.size;
						ret = potential;
					}
				}
			}
		}
		
		if(!contained && ret != null) {
			map.put(pi,ret.getCopy());
		}

		for(int i : propped) {
			choices[Math.abs(i)] = unchosen;
		}

		return ret;
	}

	private static RepList doSearch2(CNF generateCNF, int[] chosen, LitsMap<RepList> map, int myMax,
			int k) throws TimeoutException {

		RepList ret = null;
		chosen[k] = neg;
		RepList rep1 = exhaustiveMinRenameHornCover2(generateCNF.subst(-k,true),chosen,map,myMax-1);

		int rep1Size = rep1 == null ? Integer.MAX_VALUE : rep1.size;

		if(rep1 != null && rep1.size <= myMax-1) {
			chosen[k] = pos;
			RepList rep2 = exhaustiveMinRenameHornCover2(generateCNF.subst(k,true),chosen,map,myMax-rep1Size-1);

			if(rep2 == null) {
				ret = null;
			} else {
				rep1.add(rep2);
				ret = rep1;
			}
		} else {
			ret = null;
		}

		chosen[k] = unchosen;

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

	private static int[] getPI(int[] choices, int numChosen) {
		int[] ret = new int[numChosen];

		int retInd = 0;
		for(int k = 1; k < choices.length; k++) {
			if(choices[k] != unchosen) {
				ret[retInd] = k*choices[k];
				retInd++;
			}
		}

		return ret;
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
