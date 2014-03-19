package WorkflowTests;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import task.clustering.SimpleDifference;
import task.formula.AllSquares;
import task.formula.LineColoringCreator;
import util.ObjectPartitionIterator;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.EclecWorkflowData;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.eclectic.NVarsClosenessFinder;
import workflow.graph.AgreementSymAdder;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.MinimalDistanceEdges;
import workflow.graph.ShortestPathCreator;


public class FullTest {


	public static void main(String[] args) throws Exception {
		EdgeManipulator[] required = //Need at least 1
				new EdgeManipulator[]{ 
					new GlobalSymmetryEdges(),
					new DistanceEdges(new SimpleDifference()),
					new AgreementSymAdder(),
					//new MinimalDistanceEdges(0),
			};
		EdgeManipulator[] optional = new EdgeManipulator[]{//new MakeEquivEdgesSmallDistances(), 
													new ShortestPathCreator()
		};
		EclecSetCoverCreator[] creators = new EclecSetCoverCreator[]{
				new IndependentSetCreator(new MeanClosenessFinder()),
				new IndependentSetCreator(new NVarsClosenessFinder(.33)),
				//new NonLocalSymIndSetCreator(new MeanClosenessFinder()),
				//new IndependentSetCreator(new FunctionalNClosenessFinder(new HalfFunction())),
		//, new DBScanEclecCreator(new KNNClosenessFinder(1)),
		//	new ClusterCreator(),
			//new MaximumIndependentSetCreator(new MeanClosenessFinder()),
		//	new RandomCreator()
		};
		ModelGiver[] modelCreators = new ModelGiver[]{
				
//				new CNFCreatorModelGiver(new QueensToSAT(8)), 
				new CNFCreatorModelGiver(new LineColoringCreator(3,3)),
//				new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
//				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
//				new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
//				new CNFCreatorModelGiver(new MonotonicPath(6,6)),
//				new CNFCreatorModelGiver(new CycleMatching(11)),
				new AllSquares(9),
//				new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
				//new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
//				new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
				
				
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
			workflow.executeWorkflow();
		}
	}
}
