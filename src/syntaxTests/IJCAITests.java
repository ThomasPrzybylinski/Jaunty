package syntaxTests;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import formula.VariableContext;
import formula.simple.CNF;
import io.DimacsLoaderSaver;
import task.formula.IdentityCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity.EventStats;
import util.formula.FormulaForAgreement;

public class IJCAITests {

	static CNFCreator[] creators = new CNFCreator[]{
//		new QueensToSAT(25),
//		new QueensToSAT(50),
//		new AllFilledSquaresCNF(15),
//		new AllFilledSquaresCNF(20),
//		new AllFilledSquaresCNF(25),
//		new AllFilledSquaresCNF(10),
//		new AllFilledSquaresCNF(25),
//		new AllFilledSquaresCNF(30),
//		new AllFilledSquaresCNF(35),
//		new AllFilledSquaresCNF(40),
		
//		new IdentityCNFCreator("D:\\Downloads\\Linux\\nadela\\cnf_mo_prop_9_12912_2.cnf_0.00000000.sat.cnf"),
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring7.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring8.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\blasted_squaring10.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\enqueueSeqSK.sk_10_42.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\karatsuba.sk_7_41.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\LoginService2.sk_23_36.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\s953a_3_2.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\s1196a_7_4.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\scenarios_llreverse.sb.pl.sk_8_25.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\sort.sk_8_52.cnf",false),
//		new IdentityCNFCreator("testcnf\\UniGen\\tutorial3.sk_4_31.cnf",false),
//		new IdentityCNFCreator("testcnf/uf250-1065/uf250-01.cnf",false),
//		new IdentityCNFCreator("testcnf/uf250-1065/uf250-02.cnf",false),
//		new IdentityCNFCreator("testcnf/Flat200-479/flat200-1.cnf",false),
//		new IdentityCNFCreator("testcnf/Flat200-479/flat200-2.cnf",false),
//		new IdentityCNFCreator("testcnf/logistics.a.cnf",false),
//		new IdentityCNFCreator("testcnf\\logistics.b.cnf",false),
//		new IdentityCNFCreator("testcnf\\logistics.c.cnf",false),
//		new IdentityCNFCreator("testcnf\\logistics.d.cnf",false),
//		new IdentityCNFCreator("testcnf\\bw_large.d.cnf","bw_large.d.cnf",false),
//		new EmorySchedule(),
//		new IdentityCNFCreator("testcnf/bmc-ibm-1.cnf","bmc-ibm-1.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-2.cnf","bmc-ibm-2.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-3.cnf","bmc-ibm-3.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-4.cnf","bmc-ibm-4.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-5.cnf","bmc-ibm-5.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-6.cnf","bmc-ibm-6.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-ibm-7.cnf","bmc-ibm-7.cnf",false),
//		new IdentityCNFCreator("testcnf/bmc-galileo-8.cnf","bmc-galileo-8.cnf",false),
		new IdentityCNFCreator("testcnf/bmc-galileo-9.cnf","bmc-galileo-9.cnf",false),
	};


	public static void main(String[] args) throws Exception {
		final int setSize = 50;//Integer.MAX_VALUE;//50;//
		final long timeout = 1000000;//15000;//Integer.MAX_VALUE;
		
		List<String> results = new LinkedList<String>();
		results.add("Name \t ProbSize \t ProbVars \t ProbLits \t Time \t Size \t TestedSize \t numClBrk \t NumLitBrk \t SymTime \t SolveTime \t NumTests \t NumImplTimeout");
		for(int k = 0; k < creators.length; k++) {
			VariableContext context = new VariableContext();
			CNFCreator creat = creators[k];

			CNF orig;
			if(creat instanceof IdentityCNFCreator) {
				orig = ((IdentityCNFCreator)creat).generateCNF(context,false);
			} else {
				orig = creat.generateCNF(context);
				DimacsLoaderSaver.saveDimacs(new PrintWriter("testcnf\\"+creat.toString()+".cnf"),orig,"By Thomas Przybylinski");
			}
			
			creators[k] = null; //save space

			int probSize = orig.size();
			int probLits = orig.getDeepSize();
			int probVars = orig.getContext().size();
			
			int deepSize = orig.getDeepSize();
			int prevDeepSize = -1;
			
//			while(deepSize != prevDeepSize) {//not part of original test
				orig = (new FormulaForAgreement(orig.trySubsumption())).unitPropagate().squeezed(); //WAS part of original test on its own
//				orig = (new FormulaForAgreement(orig)).removeObvEqVars();//not part of original test
//				orig = (new FormulaForAgreement(orig)).binaryResolveToUnitClauses();//not part of original test
//				orig = orig.unitPropagate();//not part of original test
//				orig = (new FormulaForAgreement(orig.trySubsumption())).unitPropagate().squeezed();//not part of original test
//				
//				prevDeepSize = deepSize;
//				deepSize = orig.getDeepSize();
//				System.out.println(deepSize);
//			}
			
			orig.sort();
			orig = orig.squeezed();
			
			CNFSparseOnlineCNFDiversity lowBreak = new CNFSparseOnlineCNFDiversity(orig);
			lowBreak.setMaxSize(setSize);
			lowBreak.setTimeOut(timeout);
//			lowBreak.setRandPhase(false);
//			lowBreak.setSeed((int)System.currentTimeMillis());
			lowBreak.setUseGlobalSymBreak(false);
			lowBreak.setUseLocalSymBreak(false);
//			lowBreak.setImplicantTimeout(750);
			lowBreak.setImplicantTimeout(-1);
			lowBreak.setMaxSyms(10);
			lowBreak.setMaxClBrk(10);
//			lowBreak.setLexBreaking(false);
			lowBreak.setBreakFast(true);
			
//			lowBreak.forceGlobBreakCl=true;
//			lowBreak.setPrintProgress(true);

			List<int[]> models = lowBreak.getDiverseSet();
			
			System.out.println(creat +"\t");

			int lastSymBreakClausesAdded = -1;
			long lastNumLitsAdded = -1;
			System.out.println("Size \t Time \t Tested \t BrkCl \t LitsBrk");
			for(EventStats es : lowBreak.getStats()) {
				lastSymBreakClausesAdded = es.getSymBreakClausesAdded();
				lastNumLitsAdded = es.getNumLitsAdded();
				System.out.println(es.getNumAdded() + "\t " + es.getMillis()  + "\t " +
						es.getNumTested() + "\t " + es.getSymBreakClausesAdded() + "\t " +
						es.getNumLitsAdded());
			}
			System.out.println();
			
//			results.add("Name \t ProbSize \t ProbVars \t ProbLits \t Time \t Size \t TestedSize \t numClBrk \t NumLitBrk \t SymTime \t SolveTime \t NumTests \n NumImplTimeout");
			
			String toAdd = creat + "\t " + probSize + "\t " + probVars + "\t" + probLits + "\t " +
					lowBreak.getTotalTime() + "\t " + models.size() + "\t " +
					lowBreak.getNumCandidates() + "\t " + lowBreak.getNumBrkCl() + "\t " +
					lowBreak.getNumBrkLit() + "\t " + lowBreak.getTotalSymTime() + "\t " + lowBreak.getTotalSolverTime()/1000000  + "\t " +
					lowBreak.getNumTests() + "\t " + lowBreak.getNumImplTimeout();
			
			results.add(toAdd);
			for(String s : results) {
				System.out.println(s);
			}
			System.out.println();

			lowBreak = null;
			
		}
		
		for(String s : results) {
			System.out.println(s);
		}
	}

}
