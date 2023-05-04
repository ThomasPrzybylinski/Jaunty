package syntaxTests;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;

import formula.VariableContext;
import formula.simple.CNF;
import io.DimacsLoaderSaver;
import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.random.CNFCreator;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import util.IntPair;
import util.IntegralDisjointSet;
import util.formula.FormulaForAgreement;
import util.lit.LitSorter;
import util.lit.LitUtil;

public class OnlineTimeTests {

	static CNFCreator[] creators = new CNFCreator[]{
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring7.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring8.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring10.cnf"),
//		new IdentityCNFCreator("enqueueSeqSK.sk_10_42.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\karatsuba.sk_7_41.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\LoginService2.sk_23_36.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\s953a_3_2.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\s1196a_7_4.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\scenarios_llreverse.sb.pl.sk_8_25.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\sort.sk_8_52.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\tutorial3.sk_4_31.cnf"),
		
//		new IdentityCNFCreator("testcnf\\blasted_squaring7.cnf"),
//		new IdentityCNFCreator("testcnf\\blasted_squaring14.cnf"),
//		new IdentityCNFCreator("testcnf\\blasted_squaring60.cnf"),
//		new IdentityCNFCreator("testcnf\\blasted_case_1_ptb_2.cnf"),
//		new IdentityCNFCreator("testcnf\\blasted_case_2_ptb_2.cnf"),
//		new SimpleLatinSquareCreator(8),
		
//		new AllFilledSquaresCNF(4),
//		new AllFilledSquaresCNF(10),
//		new AllFilledSquaresCNF(20),
//		new AllFilledSquaresCNF(15),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\encoding.cnf"),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\SAT09\\APPLICATIONS\\diagnosis\\UR-10-5p1.cnf"),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\nadela\\cnf_mo_prop_9_12912_2.cnf_0.00000000.sat.cnf"),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\nadela\\cnf_cbpmas_prop_22_19855_2.cnf_9.97000000.sat.cnf"),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\nadela\\cnf_cpipe_prop_3_6087_2.cnf_512.33000000.sat.cnf"),
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\nadela\\cnf_cpipe_prop_14_23811_2.cnf_292.62000000.sat.cnf"),


//		new EmorySchedule(),
		new LineColoringCreator(3,3),
//		
////		new IdentityCNFCreator("testcnf/uf250-1065/uf250-01.cnf"),
//		new IdentityCNFCreator("testcnf/Flat200-479/flat200-1.cnf"),
//		new IdentityCNFCreator("testcnf/Flat200-479/flat200-2.cnf"),
//		new IdentityCNFCreator("testcnf/logistics.a.cnf"),
//	
////		new IdentityCNFCreator("testcnf/bmc-ibm-7.cnf","bmc-ibm-7.cnf",false), //bmcs don't work well with us
//		
//		new QueensToSAT(50),
//		new QueensToSAT(25),
//		new QueensToSAT(11),
//		new IdentityCNFCreator("testcnf\\2bitmax_6.cnf"),
////		new IdentityCNFCreator("testcnf\\3blocks.cnf"),
//		new IdentityCNFCreator("testcnf\\4blocks.cnf"),
//		new IdentityCNFCreator("testcnf\\4blocksb.cnf"),
//		new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-01.cnf"),
//			new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-02.cnf"),
//		new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-03.cnf"),
//		new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-04.cnf"),
//		new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-05.cnf"),
//		new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-1.cnf"),
//		new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-2.cnf"),
//		new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-3.cnf"),
//		new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-4.cnf"),
//		new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-5.cnf"),
//		new IdentityCNFCreator("testcnf\\sw100-1.cnf"), //currently p=2^-5
//		new IdentityCNFCreator("testcnf\\sw100-2.cnf"),
//		new IdentityCNFCreator("testcnf\\ais8.cnf"),
//		new IdentityCNFCreator("testcnf\\ais10.cnf"),
//		new IdentityCNFCreator("testcnf\\ais12.cnf"),
//		new IdentityCNFCreator("testcnf\\qg7-13.cnf"),
//		new IdentityCNFCreator("testcnf\\qg7-09.cnf"),
//		new IdentityCNFCreator("testcnf\\qg6-09.cnf"),
//		new IdentityCNFCreator("testcnf\\qg5-11.cnf"),
//		new IdentityCNFCreator("testcnf/bmc-ibm-1.cnf","bmc-ibm-1.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-2.cnf","bmc-ibm-2.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-3.cnf","bmc-ibm-3.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-4.cnf","bmc-ibm-4.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-5.cnf","bmc-ibm-5.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-6.cnf","bmc-ibm-6.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-7.cnf","bmc-ibm-7.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-galileo-8.cnf","bmc-galileo-8.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-galileo-8.cnf","bmc-galileo-9.cnf",false),
//		
//		new IdentityCNFCreator("testcnf/bmc-ibm-10.cnf","bmc-ibm-10.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-10.cnf","bmc-ibm-11.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-10.cnf","bmc-ibm-12.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-10.cnf","bmc-ibm-13.cnf",false),
//		new IdentityCNFCreator("testcnf\\uf200-860\\uf200-02.cnf"),
//
////
//		new IdentityCNFCreator("testcnf\\bw_large.c.cnf"),
//		new IdentityCNFCreator("testcnf\\logistics.a.cnf"),
//		new IdentityCNFCreator("testcnf\\logistics.b.cnf"),
//		new IdentityCNFCreator("testcnf\\logistics.c.cnf"),
//		new IdentityCNFCreator("testcnf\\logistics.d.cnf"),
//////		
//		new IdentityCNFCreator("testcnf\\bw_large.d.cnf","bw_large.d.cnf",false),
//
//			new IdentityCNFCreator("testcnf\\ssa7552-038.cnf"), //To many syms?
//			new IdentityCNFCreator("testcnf\\ssa7552-158.cnf"),
//			new IdentityCNFCreator("testcnf\\ssa7552-159.cnf"),
//			new IdentityCNFCreator("testcnf\\ssa7552-160.cnf"),
//			new IdentityCNFCreator("testcnf/bmc-ibm-1.cnf","bmc-ibm-1.cnf",true),
//			new IdentityCNFCreator("testcnf\\g125.17.cnf"), //too slow
//			new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-2.cnf"),
		
		////			new IdentityCNFCreator("testcnf\\bmc-ibm-2.cnf","bmc-ibm-2.cnf",true), //bmcs don't work well with us
		////			new IdentityCNFCreator("testcnf\\g125.17.cnf"), //too slow
		////			new IdentityCNFCreator("testcnf\\par16-5-c.cnf"), //One soln? Slow with sym breaking
		////			new IdentityCNFCreator("testcnf\\ii32d1.cnf"), //Unsat??????
		////			new IdentityCNFCreator("testcnf\\hanoi5.cnf"),
		////			new IdentityCNFCreator("testcnf\\ssa7552-038.cnf"), //To many syms?






	};


	public static void main(String[] args) throws Exception {
		final int setSize = Integer.MAX_VALUE;//50;//
		final long timeout = 1000000;//Integer.MAX_VALUE;//6000000;

		//		private int numSets = 100;

		Random rand = new Random();
		System.out.println("\t Reg\t Globl\t GlobB\t Local\t Reg\t Glob \t GlobB \t Local");

		for(int k = 0; k < creators.length; k++) {
			long[] timeRes = new long[4];
			int[] sizeRes = new int[4];
			long[] solverTime = new long[4];
			VariableContext context = new VariableContext();
			CNFCreator creat = creators[k];

			CNF orig = creat.generateCNF(context);
			orig = (new FormulaForAgreement(orig.trySubsumption())).unitPropagate().squeezed();
			
		
			System.out.println(creat +"\t");
			
//			System.out.println("DS " + orig.getDeepSize());
//			System.out.println("VS " +orig.getContext().size());
//			orig = (new FormulaForAgreement(orig)).unitPropagate().trySubsumption().squeezed();
//			System.out.println("DS " + orig.getDeepSize());
//			System.out.println("VS " +orig.getContext().size());
//	
//			int oldSize = 0;
//			int newSize = orig.getDeepSize();
//			do {
//
//				System.out.println("=");
//				oldSize = newSize;
//				orig = (new FormulaForAgreement(orig)).removeObvEqVars().reduce();
//				orig = (new FormulaForAgreement(orig)).unitPropagate().trySubsumption().squeezed();
//				newSize = orig.getDeepSize();
//
////				System.out.println("DS " + newSize);
////				System.out.println("VS " +orig.getContext().size());
//
//			} while(oldSize != newSize);
//			
			
//			orig = SATSump.getSubsumedConj(orig);
//			{
//				oldSize = -1;
//				newSize = orig.getDeepSize();
//
//				LitsMap<?> seen = new LitsMap<Object>(orig.getContext().size());
//				while(oldSize != newSize) {
//					orig.addAll((new FormulaForAgreement(orig)).getResolvants(seen));// getSubsumedConj(cnf);
//					oldSize = newSize;
//					newSize = orig.getDeepSize();
//
//					if(newSize > 2*oldSize) {
//						orig = orig.trySubsumption();
//						newSize = orig.getDeepSize();
//					}
//					System.out.println(newSize);
//				}
//			}

			orig.sort();
			orig = orig.squeezed();
			
//			ISolver s = orig.getSolverForCNF();
//
//			long start = System.currentTimeMillis();
//			while(s.isSatisfiable()) {
//				s.addClause(new VecInt(getRejection(s.model())));
//				sizeRes[0]++;
//				if(sizeRes[0] == setSize || ((System.currentTimeMillis() - start) > timeout)) {
//					break;
//				}
//				System.out.println(sizeRes[0]);
//			}
//			
//			timeRes[0] = 0;//
//			solverTime[0] = (System.currentTimeMillis()-start)*1000000;
//			s.reset();
//			s = null;

			System.out.print(creat +"\t");
			//This one finds globally assymetric models
			CNFSparseOnlineCNFDiversity globMode = new CNFSparseOnlineCNFDiversity(orig);
			globMode.setMaxSize(setSize);
			globMode.setTimeOut(timeout);
			globMode.setUseLocalSymBreak(false);
			globMode.setUseGlobalSymBreak(false);
			globMode.setTestLocal(false);


			List<int[]> ret;// = globMode.getDiverseSet();
//			timeRes[1] = globMode.getTotalSymTime();//System.currentTimeMillis() - start;
//			sizeRes[1] = ret.size();
//			solverTime[1] = globMode.getTotalSolverTime();
			globMode = null;


			CNFSparseOnlineCNFDiversity lowBreak = new CNFSparseOnlineCNFDiversity(orig);
			lowBreak.setMaxSize(setSize);
			lowBreak.setTimeOut(timeout);
//			lowBreak.setRandPhase(false);
			lowBreak.setMaxSyms(10);
			lowBreak.setMaxClBrk(10);
//			lowBreak.setLocalBrkDelay(100);
			lowBreak.setUseGlobalSymBreak(false);
			lowBreak.setUseLocalSymBreak(false);
			lowBreak.setImplicantTimeout(2000);
//			lowBreak.setLexBreaking(false);
//			lowBreak.setBreakFast(true);
//			lowBreak.setSeed((int)System.currentTimeMillis());
			
//			lowBreak.forceGlobBreakCl=true;
			lowBreak.setPrintProgress(true);

			ret = lowBreak.getDiverseSet();
			System.out.println(Arrays.deepToString(ret.toArray()));
			timeRes[2] = lowBreak.getTotalSymTime();//System.currentTimeMillis() - start;
			sizeRes[2] = ret.size();
			solverTime[2] = lowBreak.getTotalSolverTime();
			lowBreak = null;


			CNFSparseOnlineCNFDiversity all = new CNFSparseOnlineCNFDiversity(orig);
			all.setMaxSize(setSize);
			all.setTimeOut(timeout);
			
//			all.setUseGlobalSymBreak(false);
//			all.setPrintProgress(true);
//			all.forceGlobBreakCl=true;

			
//			ret = all.getDiverseSet();
//			timeRes[3] = all.getTotalSymTime();//System.currentTimeMillis() - start;
//			sizeRes[3] = ret.size();
//			solverTime[3] = all.getTotalSolverTime();
			all = null;

			for(int i = 0; i < timeRes.length; i++) {
				System.out.printf("%8d\t ",timeRes[i]);
			}

			for(int i = 0; i < sizeRes.length; i++) {
				System.out.printf("%8d\t ",sizeRes[i]);
			}
			
			for(int i = 0; i < solverTime.length; i++) {
				System.out.printf("%8d\t ",solverTime[i]/1000000);
			}
			System.out.println();

			creators[k] = null;
		}

	}

	private static CNF removeObvEqVars(CNF orig) {
//		orig = orig.unitPropagate();
		IntegralDisjointSet equive = new IntegralDisjointSet(-orig.getContext().size(),orig.getContext().size());
		HashSet<IntPair> seenPairs = new HashSet<IntPair>();
		int numVars = orig.getContext().size();
		
		for(int[] i : orig.getClauses()) {
			if(i.length == 2) {
				seenPairs.add(new IntPair(i[0],i[1]));
				int[] nextPair = new int[]{-i[0],-i[1]};
				LitSorter.inPlaceSort(nextPair);
				if(seenPairs.contains(new IntPair(nextPair[0],nextPair[1]))) {
					equive.join(i[0],-i[1]);
					equive.join(-i[0],i[1]);
				}
			}
		}

		List<int[]> newCl = new ArrayList<int[]>(orig.size());
		CNF ret = new CNF(new VariableContext());
		boolean[] added = new boolean[2*numVars+1];
		
		for(int[] i : orig.getClauses()) {
			Arrays.fill(added,false);

			boolean[] toAddMask = new boolean[i.length];
			int len = 0;
			for(int k = 0; k < i.length; k++) {
				int rep = equive.getRootOf(i[k]);
				int index = LitUtil.getIndex(rep,numVars);
				if(!added[index]) {
					toAddMask[k] = true;
					added[index] = true;
					len++;
				}
			}
		
			int[] toAdd = new int[len];
			int index = 0;
			
			for(int k = 0; k < i.length; k++) {
				int rep = equive.getRootOf(i[k]);

				if(toAddMask[k]) {
					toAdd[index] = rep;
					index++;
				}
			}
			
			LitSorter.inPlaceSort(toAdd);
			newCl.add(toAdd);
		}
		
		ret.fastAddAll(newCl);
		return ret;
	}
	
	private static CNF removeEqVars(CNF orig, CNFCreator creat) throws Exception {
		//orig = orig.unitPropagate();
	
		int[] varToVar = new int[orig.getContext().size()+1];
		
		for(int k = 0; k < varToVar.length; k++) {
			varToVar[k] = k;
		}
		int numEquiv = 0;
		for(int k = 1; k < orig.getContext().size(); k++) {
			if(varToVar[k] != k) continue;
			for(int j = k+1; j < orig.getContext().size(); j++) {
				if(varToVar[j] != j) continue;
				System.out.println(k +" ?= " + j);
				ISolver solve = orig.getSolverForCNF(true);
				solve.addClause(new VecInt(new int[]{k,j}));
				solve.addClause(new VecInt(new int[]{-k,-j}));
				
				if(!solve.isSatisfiable()) {
					solve.reset();
					varToVar[j] = k;
					numEquiv++;
					System.out.println(k +" == " + j);
//					System.out.println(numEquiv);
					continue;
				}
				solve.reset();
				
				solve = orig.getSolverForCNF(true);
				solve.addClause(new VecInt(new int[]{k,-j}));
				solve.addClause(new VecInt(new int[]{-k,j}));
				
				if(!solve.isSatisfiable()) {
					numEquiv++;
//					System.out.println(numEquiv);
					varToVar[j] = -k;
				}
				solve.reset();
			}
		}
		System.out.println(numEquiv);
		List<int[]> newCl = new ArrayList<int[]>(orig.size());
		CNF ret = new CNF(new VariableContext());
		for(int[] i : orig.getClauses()) {
			int[] toAdd = new int[i.length];
			for(int k = 0; k < i.length; k++) {
				int lit = i[k];
				int var = Math.abs(i[k]);
				toAdd[k] = varToVar[var]*(var/lit);
			}
			LitSorter.inPlaceSort(toAdd);
			int numRem = 0;
			boolean[] rem = new boolean[toAdd.length];
			for(int k = 0; k < toAdd.length; k++) {
				if(numRem == -1) break;
				if(rem[k]) continue;
				for(int j = k+1; j < toAdd.length; j++) {
					if(rem[j]) continue;
					if(toAdd[j] == toAdd[k]) {
						rem[j] = true;
						numRem++;
					} else if(toAdd[j] == -toAdd[k]) {
						numRem = -1;
						break;
					}
				}
			}
			
			if(numRem == -1) continue;
			
			if(numRem > 0) {
				int[] temp = new int[toAdd.length-numRem];
				int index = 0;
				for(int k = 0; k < toAdd.length; k++) {
					if(!rem[k]) {
						temp[index] = toAdd[k];
						index++;
					}
				}
				toAdd = temp;
			}
			
			newCl.add(toAdd);
		}
		
		ret.fastAddAll(newCl);
		ret.sort();
		ret = ret.unitPropagate();
		ret = ret.trySubsumption();
		ret = ret.squeezed();
		ret.sort();
		
		if(creat instanceof IdentityCNFCreator) {
			IdentityCNFCreator cr = (IdentityCNFCreator)creat;
			DimacsLoaderSaver.saveDimacs(new PrintWriter(cr.getPath()+".reduced.cnf"),ret,"Reduced version of " + cr.getPath());
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
