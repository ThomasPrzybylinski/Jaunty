package workflow.graph.local;

import formula.simple.ClauseList;

public class PositiveChoices implements ChoiceGetter {

	@Override
	public ClauseList getChoices(ClauseList clauses) {
		ClauseList ret = new ClauseList(clauses.getContext());
		
		for(int[] model : clauses.getClauses()) {
			int size = 0;
			for(int i : model) {
				if(i > 0) size++;
			}
			
			int[] next = new int[size];
			
			int index = 0;
			for(int i : model) {
				if(i > 0) {
					next[index] = i;
					index++;
				}
			}
			
			ret.fastAddClause(next);
		}
		
		return ret;
	}

}
