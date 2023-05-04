package workflow.graph.local;

import formula.simple.ClauseList;
import util.lit.IntHashMap;

public class CustomChoices implements ChoiceGetter {
	private IntHashMap<Object> allChoices = new IntHashMap<Object>();
	private static Object placeholder = new Object();
	
	public CustomChoices(int[] choices) {
		for(int i : choices) {
			allChoices.put(i,placeholder);
		}
	}

	@Override
	public ClauseList getList(ClauseList clauses) {
		ClauseList ret = new ClauseList(clauses.getContext());

		for(int[] model : clauses.getClauses()) {
			int[] next = getChoiceInterp(model);

			ret.fastAddClause(next);
		}
		return ret;
	}

	public int[] getChoiceInterp(int[] model) {
		int size = 0;
		for(int i : model) {
			if(allChoices.contains(i)) {
				size++;
			}
		}

		int[] next = new int[size];

		int index = 0;
		for(int i : model) {
			if(allChoices.contains(i)) {
				next[index] = i;
				index++;
			}
		}
		return next;
	}
	
	@Override
	public boolean isChoice(int lit) {
		return allChoices.contains(lit);
	}

	@Override
	public String toString() {
		return "Custom Choices: " + allChoices.getKeys();
	}

	@Override
	public void computeChoices(ClauseList clauses) {
		//No computation necessary
		
	}

}
