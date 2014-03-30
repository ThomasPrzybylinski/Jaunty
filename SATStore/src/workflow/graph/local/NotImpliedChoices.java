package workflow.graph.local;

import java.util.Arrays;

import util.lit.LitUtil;
import formula.simple.ClauseList;

public class NotImpliedChoices implements ChoiceGetter {

	@Override
	public ClauseList getChoices(ClauseList orig) {
		int numVars = orig.getContext().size();

		//[k][i], does some literal of [k] imply [i]?
		int[][] choices = new int[2*numVars+1][2*numVars+1];

		for(int k = 0; k < choices.length; k++) {
			Arrays.fill(choices[k],LitUtil.getLit(k,numVars));
			choices[k][k] = 0;
		}

		for(int[] model : orig.getClauses()) {
			for(int k = 0; k < model.length; k++) {
				int litk = model[k];
				int litKIndex = LitUtil.getIndex(litk,numVars);
				for(int i = 0; i < model.length; i++) {
					if(i == k) continue;

					int liti = model[i];
					int litIIndex = LitUtil.getIndex(liti,numVars);

					setChoices(choices, litk, litKIndex, liti, litIIndex);

					//					setChoices(choices, -litk, LitUtil.getIndex(-litk,numVars), -liti, LitUtil.getIndex(-liti,numVars));

				}
			}
		}

		//Will be true if lit of k not implied by any other literal
		boolean[] choice = new boolean[2*numVars+1];
		int numChoices = 0;
		for(int k = 0; k < 2*numVars + 1; k++) {
			int lit = LitUtil.getLit(k,numVars);
			if(lit == 0) continue;
			boolean aChoice = true;
			for(int i = 0; i < 2*numVars + 1; i++) {
				int liti = LitUtil.getLit(i,numVars);
				if(liti == 0 || liti == -lit) continue;
				if(Math.abs(choices[i][k]) == Math.abs(lit)) {
					//i implies -k
					aChoice = false;
					break;
				}
			}

			if(aChoice) {
				choice[k] = true;
				numChoices++;
			} 
		}

		ClauseList choiceList = new ClauseList(orig.getContext());

		for(int[] model : orig.getClauses()) {

			int nextIndex = 0;
			int curSize = 0;
			for(int i : model) {
				if(choice[LitUtil.getIndex(i,numVars)]) {
					curSize++;
				}
			}

			int[] next = new int[curSize];
//			int[] next = new int[numChoices];

			for(int i : model) {
				if(choice[LitUtil.getIndex(i,numVars)]) {
					next[nextIndex] = i;
					nextIndex++;
				}
			}

			choiceList.addClause(next);
		}
		return choiceList;
	}
	
	private void setChoices(int[][] choices, int litk, int litKIndex,
			int liti, int litIIndex) {
		int negIndex = LitUtil.getIndex(-liti,choices.length/2);
		
		if(choices[litKIndex][litIIndex] == litk) {
			//First time filled in
			choices[litKIndex][litIIndex] = liti;
			choices[litKIndex][negIndex] = 0;
		} else if(choices[litKIndex][negIndex] != 0) {
			choices[litKIndex][litIIndex] = 0;
			choices[litKIndex][negIndex] = 0;
		} else if(choices[litKIndex][litIIndex] == -Math.abs(liti) && liti > 0) {
			//If k implied -i so far but now it implies i
			choices[litKIndex][litIIndex] = 0;
		} else if(choices[litKIndex][litIIndex] == Math.abs(liti) && liti < 0) {
			//If k implied i so far but now it implies -i
			choices[litKIndex][litIIndex] = 0;
		}
	}

}
