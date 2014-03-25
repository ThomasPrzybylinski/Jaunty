package WorkflowTests;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import task.formula.AllFilledRectanglesOnSphere;
import task.formula.AllRectangles;
import task.formula.AllRectanglesOnSphere;
import task.formula.AllSquares;
import task.formula.LineColoringCreator;
import task.formula.Primes;
import task.formula.QueensToSAT;
import task.formula.random.SmallAllModelBoolFormula;
import util.ObjectPartitionIterator;
import workflow.AllPartInterpModelGiver;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.EclecWorkflowData;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.OppositeOfIndependentSetCreator;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.ConstructionSymAddr;
import workflow.graph.local.DifferentAllLocalSymAddr;
import workflow.graph.local.GlobalPruningAllLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;


public class FullTest {


	public static void main(String[] args) throws Exception {
		EdgeManipulator[] required = //Need at least 1
				new EdgeManipulator[]{ 
//					new GlobalSymmetryEdges(),
//					new IterativeModelSymAdder(),
//					new DistanceEdges(new SimpleDifference()),
//					new AgreementSymAdder(),
//					new MinimalDistanceEdges(1),
//					new AgreementLocalSymAdder(),

//					new DifferentAllLocalSymAddr(true,true,true,false),				
				
//					new BetterAllLocalSymAddr(false,true,false,false),
//					new RealAllLocalSymAddr(true,false,true,false),
//					new RealAllLocalSymAddr(false,true,false,false),
//					new DifferentAllLocalSymAddr(false,true,false,false),
//					new ConstructionSymAddr(true,true,true,false),
//					new AgreementConstructionAdder(true),
				

				
//					new AllLocalSymAdder(),
					new GlobalPruningAllLocalSymAdder(),
//					new GlobalPruningAllLocalSymAdder(false),
//					new AllLocalSymAdder_NEW(),
//					new TestLocalSyms(),
//					new BFS_AllLocalSymAdder(),
//					blah1,
//					blah2,
				
			};
		EdgeManipulator[] optional = new EdgeManipulator[]{//new MakeEquivEdgesSmallDistances(), 
													//new ShortestPathCreator()
		};
		EclecSetCoverCreator[] creators = new EclecSetCoverCreator[]{
				new IndependentSetCreator(new MeanClosenessFinder()),
				new OppositeOfIndependentSetCreator(new MeanClosenessFinder()),
				//new IndependentSetCreator(new NVarsClosenessFinder(.33)),
				//new NonLocalSymIndSetCreator(new MeanClosenessFinder()),
				//new IndependentSetCreator(new FunctionalNClosenessFinder(new HalfFunction())),
		//, new DBScanEclecCreator(new KNNClosenessFinder(1)),
		//	new ClusterCreator(),
			//new MaximumIndependentSetCreator(new MeanClosenessFinder()),
		//	new RandomCreator()
		};
		ModelGiver[] modelCreators = new ModelGiver[]{
//				new CNFCreatorModelGiver(new QueensToSAT(7)),				
//				new CNFCreatorModelGiver(new QueensToSAT(8)), 
//				new CNFCreatorModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(8,3)),
//				new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(7,7)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(6,6)),
//				new CNFCreatorModelGiver(new MonotonicPath(6,6)),
//				new CNFCreatorModelGiver(new MonotonicPath(5,5)),
//				new CNFCreatorModelGiver(new CycleMatching(11)),
//				new AllSquares(7),
//				new AllSquares(4),
//				new AllFilledRectangles(7),
//				new AllRectangles(7),
//				new AllRectangles(2),
//				new AllRectangles(3),
//				new AllRectangles(4),
//				new AllRectangles(5),
//				new AllRectanglesOnSphere(4),
//				new AllRectanglesOnSphere(5),
//				new AllFilledRectanglesOnSphere(4),
//				new AllFilledRectanglesOnSphere(5),
//				new AllSquares(5),
//				new MNIST("t10k-images.idx3-ubyte"),
//				new NumberFactors(128),
//				new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
//				new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
//				new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
				
//				new Primes(1250),
			
//				new CNFCreatorNonModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorNonModelGiver(new SpaceFillingCycles(4,3)),
//				new SmallAllModelBoolFormula(10,512,2),
//				new SmallAllModelBoolFormula(11,1024,2),
				
				new AllPartInterpModelGiver(new CNFCreatorModelGiver(new LineColoringCreator(2,3))),
				new CNFCreatorModelGiver(new LineColoringCreator(2,3)),
				
				new AllPartInterpModelGiver(new AllSquares(2)),
				new AllSquares(2),

				
				
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
					dir.append("_"+em.getClass().getSimpleName());
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
			workflow.setSortModels(false);
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
