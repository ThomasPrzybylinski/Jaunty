package WorkflowTests.process;

import java.io.IOException;
import java.util.List;

import task.formula.AllFilledRectangles;
import task.formula.AllFilledRectanglesOnSphere;
import task.formula.AllRectangles;
import task.formula.AllRectanglesOnSphere;
import task.formula.AllSquares;
import task.formula.LineColoringCreator;
import task.formula.MonotonicPath;
import task.formula.Primes;
import task.formula.QueensToSAT;
import task.formula.random.SmallAllModelBoolFormula;
import workflow.AllPartInterpModelGiver;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AgreementConstructionAdder;
import workflow.graph.local.AllLocalImpl;
import workflow.graph.local.ConstrImpl;
import workflow.graph.local.ConstructionSymAddr;
import workflow.graph.local.DifferentAllLocalSymAddr;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;
import formula.VariableContext;
import formula.simple.ClauseList;

public class ProcessManager {
	static ClauseList cl  = new ClauseList(VariableContext.defaultContext);
	static {
		cl.addClause(1, 2,3, 4,5,6 );
		cl.addClause(-1, 2,3, 4,5,6 );
		cl.addClause(1, -2,-3, 4,5,6 );
		cl.addClause(1, 2,3, -4,-5,-6 );
	}
	
	static ReportableEdgeAddr[] required = //Need at least 1
			new ReportableEdgeAddr[]{
//		new GlobalPruningAllLocalSymAdder(false),
//		new GlobalPruningAllLocalSymAdder(),

//		new RealAllLocalSymAddr(true,false,false,false),
//		new RealAllLocalSymAddr(false,true,false,false),
//		new DifferentAllLocalSymAddr(false,true,false,false),
//		new RealAllLocalSymAddr(false,false,true,false),
//		new GlobalPruningAllLocalSymAdder(),
//		new RealAllLocalSymAddr(false,false,false,true),
		
//		new ConstructionSymAddr(false,true,true,false),
//		new ConstructionSymAddr(true,false,true,false),
//		new AgreementConstructionAdder(),
//		new AgreementConstructionAdder(true),
		
		
//		new DifferentAllLocalSymAddr(false,true,true,false),
//		new RealAllLocalSymAddr(false,true,true,false),
//		new GlobalPruningAllLocalSymAdder(),
		
//		new DifferentAllLocalSymAddr(true,false,false,false),
//		new DifferentAllLocalSymAddr(false,true,false,false),
//		new GlobalPruningAllLocalSymAdder(),
//		new DifferentAllLocalSymAddr(true,false,true,false),
//		new DifferentAllLocalSymAddr(false,false,false,true),
//		new DifferentAllLocalSymAddr(false,true,true,false),
//		new DifferentAllLocalSymAddr(false,true,false,true),
		
		new DifferentAllLocalSymAddr(false,true,true,false),
		new AllLocalImpl(false,true,true,false),
		
		new ConstructionSymAddr(false,true,true,false),
		new ConstrImpl(),
	};

	static ModelGiver[] modelCreators = new ModelGiver[] {
		new AllPartInterpModelGiver(new CNFCreatorModelGiver(new LineColoringCreator(2,3))),
		new CNFCreatorModelGiver(new LineColoringCreator(2,3)),
		
		new AllPartInterpModelGiver(new AllSquares(2)),
		new AllSquares(2),
		
					new CNFCreatorModelGiver(new QueensToSAT(5)),
//					
//					new CNFCreatorModelGiver(new QueensToSAT(6)),
//					
//					new CNFCreatorModelGiver(new QueensToSAT(7)),				
					
//					new CNFCreatorModelGiver(new QueensToSAT(8)),
					
					new Primes(1250),
		
					new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
					new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
					new CNFCreatorModelGiver(new LineColoringCreator(8,3)),
//					
//					new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
					
		//			new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(5,3)),
		//			new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
		//			new CNFCreatorModelGiver(new MonotonicPath(6,6)),
//					new CNFCreatorModelGiver(new MonotonicPath(5,5)),
//					new CNFCreatorModelGiver(new CycleMatching(11)),
//					new CNFCreatorModelGiver(new CycleMatching(12)),
		//			new AllSquares(7),
		
//					new AllSquares(3),
					new AllSquares(4),
					new AllSquares(5),
					
//					new AllSquares(6),
		
				
//					new AllRectangles(7),
					
//					new AllRectangles(3),
					new AllRectangles(4),
//					new AllRectangles(5),
					
					new AllFilledRectangles(4),
//					new AllFilledRectangles(5),
					
					
					new AllRectanglesOnSphere(4),
//					new AllRectanglesOnSphere(5),
					
					new AllFilledRectanglesOnSphere(4),
//					new AllFilledRectanglesOnSphere(5),
					
		//			new NumberFactors(128),
		//			new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//					new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
		//			new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
//					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
//					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
//					new CNFCreatorModelGiver(new SimpleCNFCreator(12,3.5,3)),
		
					new SmallAllModelBoolFormula(5,16,2),
					new SmallAllModelBoolFormula(9,256,2),
					new SmallAllModelBoolFormula(10,512,2),
					
					new SmallAllModelBoolFormula(11,1024,2),
					
					new CNFCreatorModelGiver(new QueensToSAT(7)),	
		
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
					
//					new IdentityModelGiver(cl),
	};
	



	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		for(int k = 0 ; k < modelCreators.length; k++) {
			System.out.println(modelCreators[k].toString());
			
			List<int[]> models = modelCreators[k].getAllModels(new VariableContext());
			ClauseList cl = new ClauseList(new VariableContext());
			cl.addAll(models);
			System.out.println("Models Found");
			System.out.println("Num Models: " + models.size());
			System.out.println("Complete Edges: " + (models.size()*(models.size()-1))/2);
			
			for(int i = -2; i < required.length; i++) {
				runProcess(k,i);
			}
			System.out.println();
		}

	}
	
	static long timeout = 5000;//900000;//1800000;
	public static void runProcess(int modInd, int type) {
		ProcessBuilder pb = new ProcessBuilder("java","-Xmx4000M", "-jar","SpeedTests.jar",""+modInd,""+type);
		pb = pb.inheritIO();
		
		java.lang.Process p;
		try {
			 p = pb.start();
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			p.waitFor();
		}catch(InterruptedException e) {
			p.destroy();
		}
		
	}

	
//	static class Runner implements Runnable {
//		private ProcessBuilder pb;
//		private int type;
//		
//		public Runner(ProcessBuilder pb, int type) {
//			this.pb = pb;
//			this.type = type;
//		}
//		
//		@Override
//		public void run() {
//			java.lang.Process p;
//			try {
//				 p = pb.start();
//			} catch(IOException e) {
//				e.printStackTrace();
//				return;
//			}
//			try {
//				p.waitFor();
//			}catch(InterruptedException e) {
//				p.destroy();
//			}
//		}
//	}
}
