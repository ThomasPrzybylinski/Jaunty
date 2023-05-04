package WorkflowTests;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import task.formula.AllFilledSquares;
import task.formula.LineColoringCreator;
import util.ObjectPartitionIterator;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.EclecWorkflowData;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.NullClosenessFinder;
import workflow.eclectic.OppositeOfIndependentSetCreator;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.ConstructionSymAddr;


public class FullTest {


	public static void main(String[] args) throws Exception {
		EdgeManipulator[] required = //Need at least 1
				new EdgeManipulator[]{
//					new NULLEdgeAdder(),
//				    new GlobalSymmetryEdges(),

//					new AllChoiceLocalSymAddr(false,false,false,true, new PositiveChoices()),
//					new AllChoiceConstructionSymAddr(false,false,false,true, new PositiveChoices()),
//					new AllChoiceConstructionSymAddr(false,false,false,false, new PositiveChoices()),
					
//					new ExperimentalChoiceConstr(true,false,false,true, new PositiveChoices()),
//					new ExperimentalChoiceConstr(true,false,false,true, new NegativeChoices()),
//					new ExperimentalChoiceConstr(true,false,false,true, new AllChoices()),
					
					
//					new AllChoiceConstructionSymAddr(false,false,false,true, new NotImpliedChoices()),
//					new ExperimentalChoiceConstr(false,false,false,true, new PositiveChoices()),
//					new AllChoiceConstructionSymAddr(false,false,false,true),
					
//					new IterativeModelSymAdder(),
//					new DistanceEdges(new SimpleDifference()),
//					new MinimalDistanceEdges(1),
//					new MinimalDistanceEdges(0),
//					new AgreementLocalSymAdder(),
//
//					new DifferentAllLocalSymAddr(true,true,true,false),				
				
//					new AllLocalSymAddr(false,false,false,true),
					new ConstructionSymAddr(false,false,false,true),
//					new AllChoiceConstructionSymAddr(false,false,false,true),
//					new AllLocalSymAddr(true,false,false,false),
//					new AllLocalSymAddr(false,true,true,false),
////					new DifferentAllLocalSymAddr(false,true,false,false),
//					new LimitedLocalSymAddr(false,false,false,true),
//					new LimitedConstructionSymAddr(false,false,false,true,1),
//					new LimitedConstructionSymAddr(false,false,false,true,2),
//					new LimitedConstructionSymAddr(false,false,false,true,3),
//					new ConstructionSymAddr(false,false,false,true),
//					new AgreementConstructionAdder(true),
//					new AgreementConstructionAdder(false),
					
//					new AllLocalSymAddr(false,true,false,false),

//					new AllLocalSymAddr(false,false,false,true),
					
//					new ExperimentalAllLocalSymAddr(false,false,false,true),
//					new ExperimentalGlobalPruningAllLocalSymAdder(true),
				
//					new AllLocalSymAdder(),
//					new GlobalPruningAllLocalSymAdder(),
//					new GlobalPruningAllLocalSymAdder(false),
//					new AllLocalSymAdder_NEW(),
//					new TestLocalSyms(),
//					new BFS_AllLocalSymAdder(),
//					blah1,
//					blah2,
//					new SatBasedLocalSymAddr(),
//					new ShortestPathCreator(),
//					new IterativeGraphSymBuildup(),
			};
		EdgeManipulator[] optional = new EdgeManipulator[]{
//				new MakeEquivEdgesSmallDistances(), 
//				new ShortestPathCreator()
//				new RemoveNonTriangles(),
//				new RemoveNonTriangles2(),
//				new CollapseGlobalPartitions()
		};
		EclecSetCoverCreator[] creators = new EclecSetCoverCreator[]{
				new IndependentSetCreator(new MeanClosenessFinder()),
//				new OppositeOfIndependentSetCreator(new MeanClosenessFinder()),
				new OppositeOfIndependentSetCreator(new NullClosenessFinder()),
//				new EigenCentrality(),
//				new DegreeCentrality(),
//				new EigenCentrality(true),
//				new IndependentSetCreator(new NVarsClosenessFinder(.66)),
				//new NonLocalSymIndSetCreator(new MeanClosenessFinder()),
//				new IndependentSetCreator(new FunctionalNClosenessFinder(new HalfFunction())),
//				new IndependentSetCreator(new KNNClosenessFinder(4)),
//				new OppositeOfIndependentSetCreator(new KNNClosenessFinder(4)),
		//, new DBScanEclecCreator(new KNNClosenessFinder(1)),
		//	new ClusterCreator(),
			//new MaximumIndependentSetCreator(new MeanClosenessFinder()),
		//	new RandomCreator()
		};
		ModelGiver[] modelCreators = new ModelGiver[]{
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1},{0,1}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1,2},{0}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1,2,3}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2,3,4},{0,1,2,3,4}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2,3,4},{0,1,2,3,4,5}})),

				
				
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1},{0,1}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1,2}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2,3},{0,1,2,3}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2,3,4},{0,1,2,3,4}})),
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2,3,4,5},{0,1,2,3,4,5}})),
				
//				new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{1,2,3},{4,5,6}})),
//				new AllConnectedGraphs(4),
//				new AllFilledSquares(3),
				new AllFilledSquares(4),
//				new AllFilledSquares(5),
//				new CoordsToBinary(new SquareCoords(4)),
//				new CoordsToBinary(new GaussCoords()),
				
//				new AllFilledSquaresTwoPts(4),
				
//				new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
//				new RandLitFreqBoolFormula(8,128,2),
//				new AllConnectedGraphs(3),
//				new AllConnectedGraphs(4),
//				new AllConnectedGraphs(5),
//				new AllTrees(4),
//				new AllTrees(5),
//				new AllTrees(6),
//				new AllGlobalSymmetryModels(new CNFCreatorModelGiver(new LineColoringCreator(3,5)))
//				new SmallAllModelBoolFormula(10,1024,2),
				
//				new Primes(1250),
//				new CNFCreatorModelGiver(new ApproxColorableGraphCNF(16,31,3,2)),
//				new SmallAllModelBoolFormula(5,16,2),
//				new SmallAllModelBoolFormula(4,8,2),
//				new AllSquares(3),
//				new CNFCreatorModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(3,3)),
//				
//				new CNFCreatorModelGiver(new TestSchedule()),
				
//				new CNFCreatorModelGiver(new QueensToSAT(5)),	
//				new CNFCreatorModelGiver(new QueensToSAT(7)),				
//				new CNFCreatorModelGiver(new QueensToSAT(8)),
//				new CNFCreatorModelGiver(new QueensToSAT(9)),
//				new CNFCreatorModelGiver(new QueensToSAT(10)),
//				new CNFCreatorModelGiver(new QueensToSatCorner(8)),
//				new CNFCreatorModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(4,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(3,6)),
				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(6,4)),
//				new CNFCreatorModelGiver(new LineColoringCreator(8,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(9,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(8,4)),
//				new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//				new CNFCreatorModelGiver(new CycleColoringCreator(7,3)),
				
			
//				new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(6,6)),
//				new CNFCreatorModelGiver(new MonotonicPath(6,6)),
//				new CNFCreatorModelGiver(new MonotonicPath(5,5)),
//				new CNFCreatorModelGiver(new CycleMatching(11)),
//				new CNFCreatorModelGiver(new AllSquaresCNF(10)),
//				new CNFCreatorModelGiver(new AllFilledSquaresCNF(3)),
//				new CNFCreatorModelGiver(new AllFilledSquaresCNF(5)),
//				new CNFCreatorModelGiver(new AllFilledSquaresCNF(7)),
//				new AllSquares(7),
//				new AllSquares(8),
//				new AllSquares(9),
//				new AllSquares(5),
//				new AllSquares(4),
//				new AllSquares(3),
//				new AllSquares(2),
//				new AllSquares(6),
//				new AllFilledSquares(4),
//				new AllFilledSquares(5),
//				new AllFilledSquares(7),
//				new AllFilledSquares(8),
//				new AllFilledSquares(9),
//				new AllFilledSquares(10),
//				new AllFilledSquares(16),
//				new AllFilledRectangles(4),
//				new AllRectangles(7),
//				new AllRectangles(2),
//				new AllRectangles(3),
//				new AllRectangles(4),
//				new AllRectangles(5),
//				new AllRectanglesOnSphere(4),
//				new AllRectanglesOnSphere(5),
//				new AllFilledRectangles(4),
//				new AllFilledRectanglesOnSphere(4),
//				new AllFilledRectanglesOnSphere(5),
//				new AllSquares(5),
//				new MNIST("t10k-images.idx3-ubyte"),
//				new NumberFactors(128),
//				new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(6)),
//				new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
//				new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
				
//				new Primes(4096),
			
//				new CNFCreatorNonModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorNonModelGiver(new SpaceFillingCycles(4,3)),
//				new SmallAllModelBoolFormula(6,32,2),
//				new SmallAllModelBoolFormula(7,64,2),
//				new SmallAllModelBoolFormula(8,128,2),
//				new SmallAllModelBoolFormula(9,256,2),
//				new SmallAllModelBoolFormula(10,512,2),
//			
//				new SmallAllModelBoolFormula(11,1024,2),
//				new SmallAllModelBoolFormula(13,2048*2,2),
				
//				new CNFCreatorModelGiver(new SimpleCNFCreator(32,4.3,3,1)),
				
//				new AllPartInterpModelGiver(new CNFCreatorModelGiver(new LineColoringCreator(2,3))),
//				new CNFCreatorModelGiver(new LineColoringCreator(2,3)),
				
//				new AllPartInterpModelGiver(new AllSquares(2)),
//				new AllSquares(2),

//				new Primes(10000),
				
//				new CNFCreatorModelGiver(new QueensToSAT(8)),

//				new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.a.cnf","bw_large_a")),
//				new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.c.cnf","bw_large_c")),
//				new CNFCreatorModelGiver(new IdentityCNFCreator("testcnf\\bw_large.d.cnf","bw_large_d")),
//				new CNFCreatorModelGiver(new ColoringCNFDecoder(new IdentityCNFCreator("testcnf\\flat30-1.cnf","flat30-1"))),
				
				
		};
		//new CNFCreator[]{new LineColoringCreator(6,3)};

		File baseDir = new File("FullTests");

		for(ModelGiver mGiver : modelCreators) {
			File modelsDir = new File(baseDir,mGiver.getDirName()+"_models");
			LinkedList<EclecWorkflowData> data = new LinkedList<EclecWorkflowData>();

			ObjectPartitionIterator<EdgeManipulator> requiredIter = new ObjectPartitionIterator<EdgeManipulator>(required);

			while(requiredIter.hasNext()) {
				List<EdgeManipulator> realEdge = requiredIter.next();
				StringBuilder dir = new StringBuilder(mGiver.getDirName());

				for(EdgeManipulator em : realEdge) {
					dir.append("_"+em.getDirName());
				}

				if(realEdge.size() != 0) {
					ObjectPartitionIterator<EdgeManipulator> optionalIter = new ObjectPartitionIterator<EdgeManipulator>(optional);

					while(optionalIter.hasNext()) {
						List<EdgeManipulator> optChoices = optionalIter.next();
						StringBuilder dir2 = new StringBuilder(dir);

						for(EdgeManipulator em : optChoices) {
							dir2.append("_"+em.getClass().getSimpleName());
							realEdge.add(em);
						}


						for(EclecSetCoverCreator c : creators) {
							EclecWorkflowData dataInstance = new EclecWorkflowData();
							dataInstance.addEdgeAdder(new CompoundEdgeManipulator(realEdge));
							dataInstance.setCreator(c);


							dataInstance.setDirectory(new File(baseDir,dir2.toString()+"_"+c.getDirLabel()));

							data.add(dataInstance);
						}
					}
				}
			}


			EclecWorkflow workflow = new EclecWorkflow(data,mGiver,modelsDir);
			workflow.setSortPics(false);
			workflow.setSortModels(true);
			workflow.setDoStats(false);
			
			workflow.executeWorkflow();
		}
		
//		ArrayList<int[]> comp1 = blah1.computed;
//		ArrayList<int[]> comp2 = blah2.computed;
//
//		Collections.sort(comp1,new ModelMeasure());
//		Collections.sort(comp2,new ModelMeasure());
//		
//		for(int k = 0; k < comp1.size(); k++) {
//			if(!Arrays.equals(comp1.get(k),comp2.get(k))) {
//				System.out.println(Arrays.toString(comp1.get(k-1)));
//				System.out.println(Arrays.toString(comp1.get(k)));
//				System.out.println(Arrays.toString(comp1.get(k+1)));
//				System.out.println();
////				System.out.println(Arrays.toString(comp2.get(k-2)));
//				System.out.println(Arrays.toString(comp2.get(k-1)));
//				System.out.println(Arrays.toString(comp2.get(k)));
//				System.out.println(Arrays.toString(comp2.get(k+1)));
//				System.out.println();
//				System.out.println();
//				break;
//			}
//		}
		
	}
}
