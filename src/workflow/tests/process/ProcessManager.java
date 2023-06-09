package workflow.tests.process;

import java.io.IOException;
import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import task.formula.random.SmallAllModelBoolFormula;
import workflow.ModelGiver;
import workflow.graph.ReportableEdgeAddr;
import workflow.graph.local.AgreementConstructionAdder;

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
		
//		new AgreementConstructionAdder(),
		new AgreementConstructionAdder(true),

//		new GlobalSymmetryEdges(),
//		new TerribleAllLocalSymAdder(),
//		new GlobalPruningAllLocalSymAdder(false),
//		new GlobalPruningAllLocalSymAdder(),
		
		
//		new AllLocalSymAddr(true,false,false,false),
//		new AllLocalSymAddr(false,true,false,false),
//		new GlobalPruningAllLocalSymAdder(),
//		new AllLocalSymAddr(true,false,true,false),
		
//		new AllLocalSymAddr(false,false,false,true),

		//		new AllLocalSymAddr(true,false,false,true)
//		new AllLocalSymAddr(false,true,true,false),
//		new AllLocalSymAddr(false,false,false,true),
//		new SparseAllLocalSymAddr(false,false,false,true),
		
//		new ExperimentalGlobalPruningAllLocalSymAdder(true),
//		new SatBasedLocalSymAddr(),
		
//		new ExperimentalAllLocalSymAddr(false,false,false,true),
		
//		new ConstructionSymAddr(false,true,true,false),
		
//		new ConstructionSymAddr(false,false,false,true),
//		new AllChoiceConstructionSymAddr(false,false,false,true, new PositiveChoices()),
//		new AllChoiceConstructionSymAddr(false,false,false,true, new NotImpliedChoices()),
//		new LimitedConstructionSymAddr(false,false,false,true,2),
//		new LimitedConstructionSymAddr(false,false,false,true,3),
//		new MinModelConstructionSymAddr(false,false,false,true,32),
//		new MinModelConstructionSymAddr(false,false,false,true,16),
//		new PercentMinModelConstructionSymAddr(false,false,false,true,.1),
		
		
//		new ExperimentalChoiceConstr(false,false,false,true, new PositiveChoices()),

		
//		new AllChoiceConstructionSymAddr(false,false,false,true),
		
//		new MinimalDistanceEdges(),
		
	};

	static ModelGiver[] modelCreators = new ModelGiver[] {
//		new CNFCreatorModelGiver(new SpaceFillingCycles(6,6)),
//		new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
//		new CNFCreatorModelGiver(new CycleMatching(10)),
//		new CNFCreatorModelGiver(new CycleMatching(11)),
//		new Primes(100),
//		new Primes(500),
//		new Primes(1000),
//		new Primes(2000),
//		new Primes(10000),
//		new AllConnectedGraphs(3),
//		new AllConnectedGraphs(4),
//		new AllConnectedGraphs(5),
//		new AllTrees(4),
//		new AllTrees(5),
//		new AllTrees(6),
		
//		new RandLitFreqBoolFormula(8,256,2),
//		new RandLitFreqBoolFormula(9,512,2),
//		new RandLitFreqBoolFormula(10,1024,2),
//		new RandLitFreqBoolFormula(11,2048,2),
//		new RandLitFreqBoolFormula(12,4096,2),
//		new RandLitFreqBoolFormula(13,8192,2),
		
	
					
//					new WeakTrueBoolFormula(15,1024,2),
//					new WeakTrueBoolFormula(15,1024,2),
//					new WeakTrueBoolFormula(20,1024,2),
//					new WeakTrueBoolFormula(100,100,2),
//					new WeakTrueBoolFormula(150,3000,2),
//		

//					
//					new CNFCreatorModelGiver(new QueensToSAT(5)),
//					
//					new CNFCreatorModelGiver(new QueensToSAT(7)),				
//					
//					new CNFCreatorModelGiver(new QueensToSAT(8)),
//					new CNFCreatorModelGiver(new QueensToSAT(9)),
//					new CNFCreatorModelGiver(new QueensToSAT(10)),
					
//					new Primes(1250),

//					new CNFCreatorModelGiver(new QueensToSAT(5)),
//					new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(8,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(9,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(10,3)),
//					new CNFCreatorModelGiver(new LineColoringCreator(11,3)),
//					
//					new CNFCreatorModelGiver(new LineColoringCreator(6,4)),
//					new CNFCreatorModelGiver(new LineColoringCreator(7,4)),
////					
////					new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
//					
//		//			new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
////					new CNFCreatorModelGiver(new LineColoringCreator(5,3)),
//		//			new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
//		//			new CNFCreatorModelGiver(new MonotonicPath(6,6)),
////					new CNFCreatorModelGiver(new MonotonicPath(5,5)),
////					new CNFCreatorModelGiver(new CycleMatching(11)),
////					new CNFCreatorModelGiver(new CycleMatching(12)),
//		//			new AllSquares(7),
//		
////					new AllSquares(3),
//					new AllSquares(4),
//					new AllSquares(5),
//					new AllSquares(6),
//					
//					new AllFilledSquares(4),
//					new AllFilledSquares(5),
//					new AllFilledSquares(6),
//		new AllFilledSquares(6),
//		new AllFilledSquares(7),
//		new AllFilledSquares(8),
//		new AllFilledSquares(9),
//		new AllFilledSquares(10),
//					new AllFilledSquares(11),
//					new AllFilledSquares(12),
//					new AllFilledSquares(13),
//					new AllFilledSquares(14),
//					new AllFilledSquares(15),
//		
//				
////					new AllRectangles(7),
//					
//					new AllRectangles(2),
//					new AllRectangles(3),
//					new AllRectangles(4),
//					new AllRectangles(5),
//					
//					new AllFilledRectangles(4),
//					new AllFilledRectangles(5),
//					
//					
//					new AllRectanglesOnSphere(4),
//					new AllRectanglesOnSphere(5),
//					
//					new AllFilledRectanglesOnSphere(4),
//					new AllFilledRectanglesOnSphere(5),
//					
//		//			new NumberFactors(128),
//		//			new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
////					new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
//		//			new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
////					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
////					new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
////					new CNFCreatorModelGiver(new SimpleCNFCreator(12,3.5,3)),
//		
//					new SmallAllModelBoolFormula(3,4,2),
//					new SmallAllModelBoolFormula(4,8,2),
//					new SmallAllModelBoolFormula(5,16,2),
//					new SmallAllModelBoolFormula(9,256,2),
//					new SmallAllModelBoolFormula(10,512,2),
//////					
//					new SmallAllModelBoolFormula(11,1024,2),
//					new SmallAllModelBoolFormula(12,2048,2),
					new SmallAllModelBoolFormula(13,2048*2,2),
//					new SmallAllModelBoolFormula(12,4096,2),
//					
//					new CNFCreatorModelGiver(new QueensToSAT(7)),
//					new CNFCreatorModelGiver(new QueensToSAT(8)),
		
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
					
//					new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
					
//					new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.d.cnf","bw_large_d")),
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
			
			System.out.println("Name \t Iters \t Time \t Useful \t Edges");
//			for(int i = -2; i < required.length; i++) {
			for(int i = 0; i < required.length; i++) {
				runProcess(k,i);
			}
			System.out.println();
		}

	}
	
	public static void runProcess(int modInd, int type) throws Exception {
		//Dangerous: random ones will be random
//		try {
//			Process.main(new String[]{""+modInd,""+type});
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
		ProcessBuilder pb = new ProcessBuilder("java","-server","-Xmx30G",
				"-jar","SpeedTests.jar",""+modInd,""+type);
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
