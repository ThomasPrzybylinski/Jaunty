package WorkflowTests;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import WorkflowTests.process.ProcessManager;
import task.formula.Primes;
import workflow.ModelGiver;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AllLocalSymAddr;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;

public class SpeedOfIncreaseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		for(int k = 1000; k < 10000; k+=1000) {
			ModelGiver giver = new Primes(k);
			ClauseList cl = new ClauseList(new VariableContext());
			cl.fastAddAll(giver.getAllModels(new VariableContext()));
			
			long start = System.currentTimeMillis();
			ReportableEdgeAddr em = new GlobalPruningAllLocalSymAdder(false);//new AllLocalSymAddr(true,false,false,false);
			PossiblyDenseGraph<int[]> g = new PossiblyDenseGraph<int[]>(cl.getClauses());
			em.addEdges(g,cl);
			long end = System.currentTimeMillis();
			System.out.printf("%-35s\t %8d\t %5.2f\t %5d\t %5d%n", em.toString(), em.getIters(), (end-start)/1000., cl.size(), g.numEdges());
		}

	}

}
