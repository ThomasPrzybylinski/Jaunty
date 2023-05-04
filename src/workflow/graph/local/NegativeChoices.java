package workflow.graph.local;

import formula.simple.ClauseList;

public class NegativeChoices implements ChoiceGetter {

	@Override
	public void computeChoices(ClauseList clauses) {

	}

	@Override
	public ClauseList getList(ClauseList clauses) {
		ClauseList ret = new ClauseList(clauses.getContext());

		for(int[] model : clauses.getClauses()) {
			int[] next = getChoiceInterp(model);

			ret.fastAddClause(next);
		}
		ret.sort();
		return ret;
	}

	public int[] getChoiceInterp(int[] model) {
		int size = 0;
		for(int i : model) {
			if(i < 0) size++;
		}

		int[] next = new int[size];

		int index = 0;
		for(int i : model) {
			if(i < 0) {
				next[index] = i;
				index++;
			}
		}
		return next;
	}
	
	@Override
	public boolean isChoice(int lit) {
		return lit < 0;
	}

	@Override
	public String toString() {
		return "Neg";
	}
	
	
}
