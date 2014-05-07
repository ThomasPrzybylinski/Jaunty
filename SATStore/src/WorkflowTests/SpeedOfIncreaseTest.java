package WorkflowTests;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import WorkflowTests.process.ProcessManager;
import task.formula.Primes;
import task.formula.QueensToSAT;
import task.formula.random.SmallAllModelBoolFormula;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AllChoiceConstructionSymAddr;
import workflow.graph.local.AllLocalSymAddr;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;
import workflow.graph.local.PositiveChoices;

public class SpeedOfIncreaseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		for(int exp = 9; exp <= 11; exp++) {
			int num = (int)Math.pow(2,exp);
			System.out.println(num);

			int step = Math.max(1,4096/64); //Math.max(1,num/64);
			
			for(int k = step; k <= num; k+=step) {
				ModelGiver giver = new SmallAllModelBoolFormula(exp,k,2);


				//		for(int k = 1000; k < 20000; k+=1000) {
				//			ModelGiver giver = new Primes(k);

				//		for(int k = 7; k < 20; k++) {
				//			ModelGiver giver = new CNFCreatorModelGiver(new QueensToSAT(k));

				ClauseList cl = new ClauseList(new VariableContext());
				cl.fastAddAll(giver.getAllModels(new VariableContext()));

				long start = System.currentTimeMillis();
				ReportableEdgeAddr em = new AllLocalSymAddr(true,false,false,true);//new AllChoiceConstructionSymAddr(false,true,true,false,new PositiveChoices()); //new GlobalPruningAllLocalSymAdder(false);/
				PossiblyDenseGraph<int[]> g = new PossiblyDenseGraph<int[]>(cl.getClauses());
				em.addEdges(g,cl);
				long end = System.currentTimeMillis();
				System.out.printf("%-35s\t %5d\t %8d\t %5.2f\t %5d%n", em.toString(), cl.size() , em.getIters(), (end-start)/1000., g.numEdges());
			}
		}

	}

}
