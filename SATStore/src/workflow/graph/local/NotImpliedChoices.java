package workflow.graph.local;

import java.util.Arrays;

import util.lit.LitComparator;
import util.lit.LitUtil;
import formula.simple.ClauseList;

public class NotImpliedChoices implements ChoiceGetter {
	private static Integer EQUIVALENT = 1;
	private static Integer IMPLIES = 2;
	private static Integer IMPLIEDBY = 3;
	private static Integer NA = 4;


	@Override
	public ClauseList getChoices(ClauseList orig) {
		int numVars = orig.getContext().size();

		//[k][i], does some literal of [k] imply [i]?
		int[][] choices = new int[2*numVars+1][2*numVars+1];

		for(int k = 0; k < choices.length; k++) {
			Arrays.fill(choices[k],EQUIVALENT);//LitUtil.getLit(k,numVars));
			choices[k][k] = NA;
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
				if(choices[k][i] == IMPLIEDBY) {
					aChoice = false;
					break;
				} else if(choices[k][i] == EQUIVALENT) {
					if(Math.abs(lit) < Math.abs(liti) || (Math.abs(lit) == Math.abs(liti) && lit < 0)) {
						//Not smallest equivalent literal
						aChoice = false;
						break;
					}
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
		int negIndexI = LitUtil.getIndex(-liti,choices.length/2);
		int negIndexK = LitUtil.getIndex(-litk,choices.length/2);
		
//		choices[litKIndex][negIndex] = NA; //litk can't imply -liti at all anymore
		
		if(choices[litKIndex][negIndexI] == EQUIVALENT) {
			//k cannot imply -i, but -i could still impy k
			choices[litKIndex][negIndexI] = IMPLIEDBY;
			choices[negIndexI][litKIndex] = IMPLIES;
		} else if(choices[litKIndex][negIndexI] == IMPLIES) {
			//Previously saw that k cannot imply i, so there is no strict relation
			choices[litKIndex][negIndexI] = NA;
			choices[negIndexI][litKIndex] = NA;
		}
		
//		if(choices[litKIndex][litIIndex] == EQUIVALENT) {
//			//choices initially all EQUIVALENT
//			//choices[negIndexK][negIndexI] = EQUIVALENT;//unnecessary
//			choices[litKIndex][negIndexI] = NA;
//			choices[negIndexK][litIIndex] = NA;
//		} else if(choices[litKIndex][negIndexI] == IMPLIES) {
//			choices[litKIndex][negIndexI] = NA;
//			choices[litKIndex][litIIndex] = NA;
//		} else if(choices[litKIndex][litIIndex] == NA) {
//			if(choices[litKIndex][negIndexI] == EQUIVALENT) {
//				choices[negIndexI][litKIndex] = IMPLIES;
//				choices[litKIndex][negIndexI] = IMPLIEDBY;
//
//				choices[negIndexK][litIIndex] = IMPLIES;
//				choices[litIIndex][negIndexK] = IMPLIEDBY;
//			} 
//		} 
	}

}
