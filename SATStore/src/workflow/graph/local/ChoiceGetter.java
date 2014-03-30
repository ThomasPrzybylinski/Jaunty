package workflow.graph.local;

import formula.simple.ClauseList;

public interface ChoiceGetter {
	public ClauseList getChoices(ClauseList clauses);
}
