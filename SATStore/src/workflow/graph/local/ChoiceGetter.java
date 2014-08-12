package workflow.graph.local;

import formula.simple.ClauseList;

public interface ChoiceGetter {
	public ClauseList getList(ClauseList clauses);
	public int[] getChoiceInterp(int[] interp);
	public void computeChoices(ClauseList clauses);
	public boolean isChoice(int lit);
}
