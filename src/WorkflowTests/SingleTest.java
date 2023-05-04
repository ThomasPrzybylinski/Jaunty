package WorkflowTests;

import java.io.File;
import java.util.LinkedList;

import task.formula.LineColoringCreator;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.EclecWorkflowData;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.IndependentSetCreator;
import workflow.eclectic.MeanClosenessFinder;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.AllLocalSymAddr;
public class SingleTest {




	public static void main(String[] args) throws Exception {
		EdgeManipulator[] required =
				new EdgeManipulator[]{ 
//				new GlobalSymmetryEdges(),new DistanceEdges(new SimpleDifference()),new ShortestPathCreator()
				new AllLocalSymAddr(false,false,false,true),
				};

		EclecSetCoverCreator[] creators = new EclecSetCoverCreator[]{new IndependentSetCreator(new MeanClosenessFinder()),
				//new IndependentSetCreator(new SqrtNClosenessFinder()),
				//, new DBScanEclecCreator(new KNNClosenessFinder(1)),
				//	new ClusterCreator(),
				//new RandomCreator()
		};
		ModelGiver[] cnfCreators = new ModelGiver[]{
				//new QueensToSAT(8), 
				new CNFCreatorModelGiver(new LineColoringCreator(6,3)),
				//new SpaceFillingCycles(8,8),
				//new MonotonicPath(6,6),
				//new ReducedLatinSquareCreator(5),
				//new SimpleLatinSquareCreator(4),
				//new SpanningCyclesCreator(7)
		};
		//new CNFCreator[]{new LineColoringCreator(6,3)};

		File baseDir = new File("FullTests");

		for(ModelGiver cnf : cnfCreators) {
			File modelsDir = new File(baseDir,cnf.getClass().getSimpleName()+"_models");
			LinkedList<EclecWorkflowData> data = new LinkedList<EclecWorkflowData>();


			StringBuilder dir = new StringBuilder(cnf.getClass().getSimpleName());

			for(EdgeManipulator em : required) {
				dir.append("_"+em.getClass().getSimpleName());
			}

			StringBuilder dir2 = new StringBuilder(dir);

			for(EclecSetCoverCreator c : creators) {
				EclecWorkflowData dataInstance = new EclecWorkflowData();
				dataInstance.addEdgeAdder(new CompoundEdgeManipulator(required));
				dataInstance.setCreator(c);


				dataInstance.setDirectory(new File(baseDir,dir2.toString()+"_"+c.getDirLabel()));

				data.add(dataInstance);
			}


			EclecWorkflow workflow = new EclecWorkflow(data,cnf,modelsDir);
			workflow.executeWorkflow();
		}
	}
}
