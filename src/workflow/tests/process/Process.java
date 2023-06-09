package workflow.tests.process;

import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import workflow.ModelGiver;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AgreementLocalSymAdder;

public class Process {
	static volatile long start = -1;
	static volatile int mod = -1;
	static volatile int type = -1;
	static volatile PossiblyDenseGraph<int[]> g;
	static volatile AgreementLocalSymAdder agAddr;
	static volatile List<int[]> models;
	static volatile boolean done = false;
	
	public Process() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		type = Integer.parseInt(args[1]); //most important one
		mod = Integer.parseInt(args[0]);
		
		
		ModelGiver mg = ProcessManager.modelCreators[mod];
		models = mg.getAllModels(new VariableContext());
		ClauseList cl = new ClauseList(new VariableContext());
		cl.addAll(models);
		IndependentSetCreator creat = new IndependentSetCreator(new MeanClosenessFinder());
		int numCreats = 0;
		
//		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
		
		Thread timeout = new Thread(new Shutdown());
		timeout.setDaemon(true);
		timeout.start();
		
		if(type == -2) {
			start = System.currentTimeMillis();
			g = new PossiblyDenseGraph<int[]>(cl.getClauses());
			agAddr = new AgreementLocalSymAdder();
			agAddr.addEdges(g,cl);
			long end = System.currentTimeMillis();
			long size = 0;
			for(int k = 0; k < numCreats; k++) {
				size += creat.getRandomEclecticSet(g).size();
			}
			System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d\t %5.2f%n", "Agreement", agAddr.iters-agAddr.skipped, (end-start)/1000., (double)agAddr.skipped, g.numEdges(),size/(double)numCreats);
		} else if(type == -1) {
			start = System.currentTimeMillis();
			g = new PossiblyDenseGraph<int[]>(cl.getClauses());
			agAddr = new AgreementLocalSymAdder();
			agAddr.addEdges(g,cl);
			
			(new GlobalSymmetryEdges()).addEdges(g,cl);
			long end = System.currentTimeMillis();
			long size = 0;
			for(int k = 0; k < numCreats; k++) {
				size += creat.getRandomEclecticSet(g).size();
			}
			System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d\t %5.2f%n", "AgreeGlob", agAddr.iters-agAddr.skipped+1, (end-start)/1000., 0., g.numEdges(),size/(double)numCreats);
		} else {
			start = System.currentTimeMillis();
			ReportableEdgeAddr em = ProcessManager.required[type];
			g = new PossiblyDenseGraph<int[]>(cl.getClauses());
			em.addEdges(g,cl);
			long end = System.currentTimeMillis();
			long size = 0;
			for(int k = 0; k < numCreats; k++) {
				size += creat.getRandomEclecticSet(g).size();
			}
			System.out.printf("%-35s\t %8d\t %5.2f\t %8d\t %5d\t %5.2f%n", em.toString(), em.getIters(), (end-start)/1000., em.getNumUsefulModelSyms(), g.numEdges(),size/(double)numCreats);
		}
		done = true;
	}
	
	
	public static class Shutdown implements Runnable {
		static long timeout = 1000000; //1000 seconds
							//600000;//600000; //10 minutesLong.MAX_VALUE; //
							//1800000;//100;//900000;//
		public void run() {
			long start = System.currentTimeMillis();
			long end = start;
			
			while(end-start < timeout) {
				try {
					Thread.sleep(timeout-(end-start));
				} catch(InterruptedException e) {
					
				}
				end  = System.currentTimeMillis();
			}
			System.out.flush();
			if(done) return;
//			System.err.println("Blah!");
//			System.err.println(g);
//			System.err.println(type);

			if(g != null) {
//				end = System.currentTimeMillis();
				if(type == -2 && agAddr != null) {
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d\t %5.2f%n", "Agreement", agAddr.iters-agAddr.skipped, -1., -1., g.numEdges(),-1.);	
				} else if(type == -1 && agAddr != null) {
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d\t %5.2f%n", "AgreeGlob", agAddr.iters-agAddr.skipped, -1., -1., g.numEdges(),-1.);
				} else if(type >= 0) {
					ReportableEdgeAddr em = ProcessManager.required[type];
//					System.err.println(em);
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d\t %5.2f%n", em.toString(), em.getIters(), -1., -1., g.numEdges(),-1.);
					
				}
			}
			System.out.flush();
			System.exit(0);
		}
	}
	
	public static class ShutdownHook implements Runnable {

		@Override
		public void run() {
			if(done) return;
//			System.err.println("Blah!");
//			System.err.println(g);
//			System.err.println(type);

			if(g != null) {
				long end = System.currentTimeMillis();
				if(type == -2 && agAddr != null) {
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", "Agreement", agAddr.iters-agAddr.skipped, (end-start)/1000., -1.0, g.numEdges());	
				} else if(type == -1 && agAddr != null) {
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", "AgreeGlob", agAddr.iters-agAddr.skipped, (end-start)/1000., -1.0, g.numEdges());
				} else if(type > 0) {
					ReportableEdgeAddr em = ProcessManager.required[type];
//					System.err.println(em);
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", em.toString(), em.getIters(), -1., -1., g.numEdges());
					
				}
			}
			
		}
		
	}

}
