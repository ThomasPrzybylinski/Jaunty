package workflow.graph.local;

import formula.simple.ClauseList;

//Removes nothing. Good for non-model input
public class AllChoices implements ChoiceGetter {

	@Override
	public void computeChoices(ClauseList clauses) {
	}

	@Override
	public ClauseList getList(ClauseList clauses) {
		return clauses;
	}

	@Override
	public int[] getChoiceInterp(int[] interp) {
		return interp;
	}

	@Override
	public boolean isChoice(int lit) {
		return true;
	}
}
