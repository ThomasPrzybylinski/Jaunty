package syntaxTests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.IdentityCNFCreator;
import task.formula.random.CNFCreator;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity;
import task.symmetry.sparse.CNFSparseOnlineCNFDiversity.EventStats;

//long tests over many
public class IJCAITests2 {
	
	private static class MyStats {
		long tested;
		long time;
		long found;
		public MyStats(long time) {
			super();
			this.time = time;
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		final int setSize = 100;//Integer.MAX_VALUE;//50;//
		final long timeout = 1000000;//Integer.MAX_VALUE;
		int num = 100;
		final long  period = timeout/100;
		 
		
		String pref = "testcnf/Flat200-479/flat200-";//"testcnf\\uf100-430\\uf100-0";//"testcnf/logistics.a.cnf";// "testcnf/uf250-1065/uf250-0"; //
//		String pref = "testcnf/UniGen/blasted_squaring7.cnf";
		
		List<String> results = new LinkedList<String>();
		results.add("Name \t ProbSize \t ProbVars \t ProbLits \t Time \t Size \t TestedSize \t numClBrk \t NumLitBrk \t SymTime \t SolveTime");

		ArrayList<MyStats> statsBrk = new ArrayList<MyStats>(setSize+1);
		ArrayList<MyStats> statsNoBrk = new ArrayList<MyStats>(setSize+1);
		
		for(long k = 0; k <= timeout; k += period) {
			statsBrk.add(new MyStats(k));
			statsNoBrk.add(new MyStats(k));
		}
		
		System.out.println("------------------Breaking------------------");
		doTest(setSize, timeout, num, period, pref, results, statsBrk, true);
		System.out.println("------------------NEXT------------------");
		results.clear();
		System.out.println("----------------No Breaking------------------");
		doTest(setSize, timeout, num, period, pref, results, statsNoBrk, false);
		
		
		System.out.println("------------------Breaking------------------");
		System.out.println("Time \t Size \t Tested");
		for(MyStats stats : statsBrk) {
			String d = String.format("%d",stats.time/1000);
			System.out.println(d + " \t " + stats.found/(num) + " \t " + stats.tested/(num));
		}
		System.out.println("----------------No Breaking------------------");
		System.out.println("Time \t Size \t Tested");
		for(MyStats stats : statsNoBrk) {
			String d = String.format("%d",stats.time/1000);
			System.out.println(d + " \t " + stats.found/(num) + " \t " + stats.tested/(num));
		}
		
		
		System.out.println("----------------Done------------------");
	}

	private static void doTest(final int setSize, final long timeout, int num,
			final long period, String pref, List<String> results,
			ArrayList<MyStats> statsBrk, boolean breaking) throws ContradictionException,
			TimeoutException {
		for(int k = 0; k < num; k++) {
			VariableContext context = new VariableContext();
			String strNum = ""+(k+1);
			CNFCreator creat = new IdentityCNFCreator(pref+strNum+".cnf",
					pref.substring(pref.lastIndexOf('/')+1,pref.length())+strNum,false);
//			CNFCreator creat = new IdentityCNFCreator(pref,false); //for single tests
//			CNFCreator creat = new AllFilledSquaresCNF(15); //for single tests

			CNF orig;
			if(creat instanceof IdentityCNFCreator) {
				orig = ((IdentityCNFCreator)creat).generateCNF(context,false);
			} else {
				orig = creat.generateCNF(context);
			}
			String name = creat.toString();
			System.out.println(name +"\t");
			
			creat = null;
			
			int probSize = orig.size();
			int probLits = orig.getDeepSize();
			int probVars = orig.getContext().size();
			
			orig = orig.trySubsumption().squeezed();
			
			orig.sort();
			orig = orig.squeezed();
			
			CNFSparseOnlineCNFDiversity lowBreak = new CNFSparseOnlineCNFDiversity(orig);
			lowBreak.setMaxSize(setSize);
			lowBreak.setTimeOut(timeout);
			lowBreak.setSeed((int)System.currentTimeMillis());
//			lowBreak.setRandPhase(false);
			if(!breaking) {
				lowBreak.setUseGlobalSymBreak(false);
				lowBreak.setUseLocalSymBreak(false);
			} else {
				lowBreak.setLocalBrkDelay(10);
			}
			lowBreak.setMaxSyms(10);
			lowBreak.setMaxClBrk(10);
//			lowBreak.setImplicantTimeout(750);
//			lowBreak.setImplicantTimeout(-1);
//			lowBreak.setLexBreaking(false);
//			if(!breaking) {
				lowBreak.setBreakFast(true);
//			}
			
//			lowBreak.forceGlobBreakCl=true;
//			lowBreak.setPrintProgress(true);

			List<int[]> models = lowBreak.getDiverseSet();

			long lastTotalTime = 0;
			int lastNumTested = 0;
			int lastSymBreakClausesAdded = 0;
			long lastNumLitsAdded = 00;
			int lastNumFound = 0;
			System.out.println("Time \t Size \t Tested");
			
			int eventNum = 0;
			int totalsInd = 0;
			double total = lowBreak.getStats().size();

			for(EventStats es : lowBreak.getStats()) {
				eventNum++;
				
				while(lastTotalTime <= totalsInd*period && es.getMillis() > totalsInd*period) {
					if(totalsInd*period > 0) {
						statsBrk.get(totalsInd).found += lastNumFound;
						statsBrk.get(totalsInd).tested += lastNumTested;
					}
					totalsInd++;
				}
				
				lastTotalTime = es.getMillis();
				lastNumTested = es.getNumTested();
				lastNumFound = es.getNumAdded();
				lastSymBreakClausesAdded = es.getSymBreakClausesAdded();
				lastNumLitsAdded = es.getNumLitsAdded();
			}
			
			for(int i = totalsInd; i < statsBrk.size(); i++) {
				statsBrk.get(totalsInd).found += lastNumFound;
				statsBrk.get(totalsInd).tested += lastNumTested;
				totalsInd++;
			}
			
			for(MyStats cur : statsBrk) {
				String d = String.format("%d",cur.time/1000);
				System.out.println(d + " \t " + cur.found/(double)(k+1) + " \t " + cur.tested/(k+1));
			}

			System.out.println();
			
//			results.add("Name \t ProbSize \t ProbVars \t ProbLits \t Time \t Size \t TestedSize \t numClBrk \t NumLitBrk \t SymTime \t SolveTime");
			
			String toAdd = name + "\t " + probSize + "\t " + probVars + "\t" + probLits + "\t " +
					lastTotalTime + "\t " + models.size() + "\t " +
					lastNumTested + "\t " + lastSymBreakClausesAdded + "\t " +
					lastNumLitsAdded + "\t " + lowBreak.getTotalSymTime() + "\t " + lowBreak.getTotalSolverTime()/1000000;
			
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
		System.out.println("Time \t Size \t Tested");
		for(MyStats stats : statsBrk) {
			String d = String.format("%d",stats.time/1000);
			System.out.println(d + " \t " + stats.found/(double)(num) + " \t " + stats.tested/(num));
		}
	}

}
