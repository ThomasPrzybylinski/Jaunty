package WorkflowTests;

import io.DimacsLoaderSaver;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import task.formula.AllFilledRectangles;
import task.formula.AllRectangles;
import task.formula.AllSquares;
import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.SimpleLatinSquareCreator;
import task.formula.random.SmallAllModelBoolFormula;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AgreementSymAdder;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;
import workflow.graph.local.RealAllLocalSymAddr;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class EdgeAdderSpeedTest {
	static ReportableEdgeAddr[] required = //Need at least 1
			new ReportableEdgeAddr[]{
		
//		new BetterAllLocalSymAddr(false,true,false,true),
//		new BetterAllLocalSymAddr(false,true,true,false),
//		
//		new BetterAllLocalSymAddr(true,false,false,true),
//		new BetterAllLocalSymAddr(true,false,true,false),
		
//		new BetterAllLocalSymAddr(true,false,false,false),
//		new BetterAllLocalSymAddr(false,true,false,false),
//
//		new BetterAllLocalSymAddr(false,false,false,true),
//		
		
//		new RealAllLocalSymAddr(false,true,false,false),
		
//		new BetterAllLocalSymAddr(false,false,true,false),

//		new AllLocalSymAdder(),
//		new BetterAllLocalSymAddr(false,false,false,false),
		
//		new BetterAllLocalSymAddr(false,true,true,true),
		
		
//		new RealAllLocalSymAddr(true,false,true,false),
//		new RealAllLocalSymAddr(true,false,false,true),
//
//		new RealAllLocalSymAddr(true,false,false,false),
//		new RealAllLocalSymAddr(true,false,false,true),
//		new RealAllLocalSymAddr(true,false,true,false),
//		new RealAllLocalSymAddr(false,false,false,true),
//		new RealAllLocalSymAddr(false,false,true,false),
		
		new GlobalPruningAllLocalSymAdder(),
//		new GlobalPruningAllLocalSymAdder(false),
//		new RealAllLocalSymAddr(false,false,false,false),
		
	};

	static ModelGiver[] modelCreators = new ModelGiver[] {
					new CNFCreatorModelGiver(new QueensToSAT(5)),
//					new CNFCreatorModelGiver(new QueensToSAT(6)),
					
					new CNFCreatorModelGiver(new QueensToSAT(7)),				
//					
//					new CNFCreatorModelGiver(new QueensToSAT(8)), 
//		
//					new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(8,3)),
					
		//			new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(5,3)),
		//			new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
		//			new CNFCreatorModelGiver(new MonotonicPath(6,6)),
//					new CNFCreatorModelGiver(new MonotonicPath(5,5)),
//					new CNFCreatorModelGiver(new CycleMatching(11)),
//					new CNFCreatorModelGiver(new CycleMatching(12)),
		//			new AllSquares(7),
		
//					new AllSquares(4),
//					new AllSquares(5),
					
//					new AllSquares(6),
		
//					new AllFilledRectangles(4),
					
//					new AllRectangles(7),
					
//					new AllRectangles(4),
					
		//			new NumberFactors(128),
		//			new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//					new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
		//			new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
//					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
//					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
//					new CNFCreatorModelGiver(new SimpleCNFCreator(12,3.5,3)),
		
//					new SmallAllModelBoolFormula(5,16,2),
//					new SmallAllModelBoolFormula(9,256,2),
//					new SmallAllModelBoolFormula(10,512,2),
					
//					new SmallAllModelBoolFormula(11,1024,2),
		
//					new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\uf20-01.cnf")),
//					new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\uf50-01.cnf")),
//					new CNFCreatorModelGiver(new IdentityCNFCreator(DimacsLoaderSaver.loadDimacsNoException("testcnf\\aim-50-2_0-yes1-3.cnf"))),
//					new CNFCreatorModelGiver(new IdentityCNFCreator(DimacsLoaderSaver.loadDimacsNoException("testcnf\\ais6.cnf"))),
//					new CNFCreatorModelGiver(new IdentityCNFCreator(DimacsLoaderSaver.loadDimacsNoException("testcnf\\flat30-1.cnf"))),
					
					
					
//					"F:\workspace\SATStore\testcnf\ uf20-01.cnf"
//					"F:\workspace\SATStore\testcnf\ uf20-02.cnf"
//					"F:\workspace\SATStore\testcnf\ais6.cnf"
//					"F:\workspace\SATStore\testcnf\ais8.cnf"
//					"F:\workspace\SATStore\testcnf\flat30-1.cnf"
//					"F:\workspace\SATStore\testcnf\flat30-2.cnf"
//					"F:\workspace\SATStore\testcnf\flat30-3.cnf"
//					
	};

	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Throwable {

		
		for(ModelGiver mg : modelCreators) {
			System.out.println(mg.toString());
			
			List<int[]> models = mg.getAllModels(new VariableContext());
			ClauseList cl = new ClauseList(new VariableContext());
			cl.addAll(models);
			System.out.println("Models Found");
			System.out.println("Num Models: " + models.size());
			
			long start = System.currentTimeMillis();
			PossiblyDenseGraph<int[]> agGraph = new PossiblyDenseGraph<int[]>(cl.getClauses());
			AgreementLocalSymAdder agAddr = new AgreementLocalSymAdder();
			(new AgreementLocalSymAdder()).addEdges(agGraph,cl);
			long end = System.currentTimeMillis();
			System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", "Agreement", models.size()*(models.size()-1)-agAddr.skipped, (end-start)/1000., (double)agAddr.skipped, agGraph.numEdges());
			
			(new GlobalSymmetryEdges()).addEdges(agGraph,cl);
			end = System.currentTimeMillis();
			System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", "AgreeGlob", models.size()*(models.size()-1), (end-start)/1000., 0., agGraph.numEdges());
			
			
			for(ReportableEdgeAddr em : required) {
				PossiblyDenseGraph<int[]> g = new PossiblyDenseGraph<int[]>(cl.getClauses());
			
				ExecutorService es = Executors.newSingleThreadExecutor();
				RunAddEdges rae = new RunAddEdges(g,cl,em);
				
//				Future f = es.submit(rae);
//				Thread.yield();
//				es.shutdown();
//				boolean success = false;
//				try {
//					success = es.awaitTermination(30,TimeUnit.MINUTES);
//				} catch(InterruptedException ie) {
//					ie.printStackTrace();
//				}
//				
//				System.out.println(f.isCancelled());
//				System.out.println(f.isDone());
				
				long timeout = Long.MAX_VALUE;//900000;//2000;//1800000;
				long startRun = System.currentTimeMillis();
				long endRun = startRun;
				
				Thread t = new Thread(rae);
				//t.setUncaughtExceptionHandler(rae);
				t.start();
				boolean success = false;
				
				while(rae.time == 0 && (endRun - startRun) < timeout) {
					t.join(timeout);
					endRun = System.currentTimeMillis();
					//System.out.println(endRun-startRun);
				}
				
				if(rae.time > 0) {
					success = true;
				}
				
//				while((endRun - startRun) < timeout) {
//					if(rae.time > 0) {
//						success = true;
//						break;
//					} else {
//						t.j
//						endRun = System.currentTimeMillis();
//						System.out.println(endRun-startRun);
//					}
//				}
				
				if(success) {
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", em.toString(), em.getIters(), rae.time/1000., em.getPropogationTime()/1000., g.numEdges());
				} else {
					t.interrupt(); //stop(new Exception());
					System.out.printf("%-35s\t %8d\t %5.2f\t %5.2f\t %5d%n", em.toString(), em.getIters(), -1., -1., g.numEdges());
				}
//				System.out.println(g.compareTo(agGraph));
//				System.out.println(agGraph.compareTo(g));
			}
		}
		
		System.exit(0);

	}
	
	private static class RunAddEdges implements Callable<Object>, Runnable, UncaughtExceptionHandler {
		private PossiblyDenseGraph<int[]>  g;
		private ClauseList cl;
		private ReportableEdgeAddr em;
		private volatile long time;
		
		public RunAddEdges(PossiblyDenseGraph<int[]> g, ClauseList cl,ReportableEdgeAddr em) {
			super();
			this.g = g;
			this.cl = cl;
			this.em = em;
			time = 0;
		}

		@Override
		public Object call() throws Exception {
			try {
				run();
			} catch(Exception e) {
				e.printStackTrace();
			} catch(Throwable t) {
				if(t.getClass() != ThreadDeath.class) {
					t.printStackTrace();
				}
			}
			
			return null;
		}

		@Override
		public void run() {
			try {
			long start = System.currentTimeMillis();
			em.addEdges(g,cl);
			long end = System.currentTimeMillis();
			
			time = end-start;
			}catch(Throwable e) {
				time = -1;
				if(e.getClass() != ThreadDeath.class && e.getClass() != OutOfMemoryError.class
						&& e.getClass() != Exception.class) {
					e.printStackTrace();
				}
			}
			
			
		}

		@Override
		public synchronized void uncaughtException(Thread t, Throwable e) {
			if(e.getClass() != ThreadDeath.class && e.getClass() != OutOfMemoryError.class
					&& e.getClass() != Exception.class) {
				e.printStackTrace();
			}
			
		}
	
	}

}
