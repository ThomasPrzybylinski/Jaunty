package workflow.graph.local;

import formula.simple.ClauseList;

//Removes nothing. Good for non-model input
public class AllChoices implements ChoiceGetter {

	@Override
	public ClauseList getChoices(ClauseList clauses) {
		
		return clauses;
	}

}
