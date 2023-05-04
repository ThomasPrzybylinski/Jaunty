import task.formula.LineColoringCreator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;


public class PrototypeTest {

	public PrototypeTest() {
		ModelGiver[] modelCreators = new ModelGiver[]{

				//				new CNFCreatorModelGiver(new QueensToSAT(8)), 
				new CNFCreatorModelGiver(new LineColoringCreator(9,3)),
				//				new CNFCreatorModelGiver(new RestrictedLineColoringCreator(5,3)),
				//				new CNFCreatorModelGiver(new LineColoringCreator(7,3)),
				//				new CNFCreatorModelGiver(new SpaceFillingCycles(8,8)),
				//				new CNFCreatorModelGiver(new MonotonicPath(6,6)),
				//				new CNFCreatorModelGiver(new CycleMatching(11)),
				//				new AllSquares(7),
				//				new AllFilledRectangles(7),
				//				new AllRectangles(7),
				//				new NumberFactors(128),
				//				new CNFCreatorModelGiver(new RelaxedPigeonHoleCreator(4,2)),
				//				new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5)),
				//				new CNFCreatorModelGiver(new SpanningCyclesCreator(7)),
				//				new CNFCreatorModelGiver(new SimpleLatinSquareCreator(4)),
		};
	}

}
