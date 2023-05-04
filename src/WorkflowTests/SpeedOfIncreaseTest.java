package WorkflowTests;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.random.SmallAllModelBoolFormula;
import workflow.ModelGiver;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;

public class SpeedOfIncreaseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		int numIters = 1;//5;
		
		ReportableEdgeAddr[] addrs = new ReportableEdgeAddr[] {
				new GlobalPruningAllLocalSymAdder(false),
//				new GlobalPruningAllLocalSymAdder(true),
//				new AllLocalSymAddr(true,false,false,false),
//				new AllLocalSymAddr(true,false,true,false),
//				new AllLocalSymAddr(false,false,false,true),
		};

		for(int exp = 10; exp <= 10; exp++) {
			int num = (int)Math.pow(2,exp);
			System.out.println(num);

			int step = Math.max(1,num/64); //Math.max(1,num/64);

			System.out.print("Size \t");
			for(ReportableEdgeAddr em : addrs) {
				System.out.print(em);
				System.out.print('\t');
			}
			for(ReportableEdgeAddr em : addrs) {
				System.out.print(em);
				System.out.print('\t');
			}
			System.out.println();

			for(int k = 1024; k <= num; k+=step) {//step; k <= num; k+=step) {
				ModelGiver giver = new SmallAllModelBoolFormula(exp,k,2);


				//		for(int k = 1000; k < 20000; k+=1000) {
				//			ModelGiver giver = new Primes(k);

				//		for(int k = 7; k < 20; k++) {
				//			ModelGiver giver = new CNFCreatorModelGiver(new QueensToSAT(k));

				
//						for(int k = 5; k <= 6; k++) {
//							ModelGiver giver = new AllSquares(k);
			
//			for(int k = 10; k <= 12; k++) {
//				ModelGiver giver = new CNFCreatorModelGiver(new LineColoringCreator(k,3));

				

				long[] time = new long[addrs.length];
				int[] numEdges = new int[addrs.length];

				int index = 0;
				for(ReportableEdgeAddr em : addrs) {
					long totalTime = 0;
					int totalVisited = 0;
					for(int i = 0; i < numIters; i++) {
						ClauseList cl = new ClauseList(new VariableContext());
						cl.fastAddAll(giver.getAllModels(new VariableContext()));
						cl.sort();
						long start = System.currentTimeMillis();
						PossiblyDenseGraph<int[]> g = new PossiblyDenseGraph<int[]>(cl.getClauses());
						em.addEdges(g,cl);
						long end = System.currentTimeMillis();
						totalTime += (end-start);
						totalVisited += em.getIters();
					}
					time[index] = totalTime/numIters;
					numEdges[index] = totalVisited/numIters;
					index++;
				}
				System.out.print(k);
				System.out.print("\t");
				for(int i : numEdges) {
					System.out.printf("%8d\t",i);
					System.out.print('\t');
				}

				for(long l : time) {
					System.out.printf("%8d\t",l);
					System.out.print('\t');
				}
				System.out.println();

				//				System.out.printf("%-35s\t %5d\t %8d\t %5.2f\t %5d%n", em.toString(), cl.size() , em.getIters(), (end-start)/1000., g.numEdges());
			}
		}

	}

}
