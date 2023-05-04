package workflow.graph.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import util.lit.LitComparator;
import util.lit.LitUtil;
import formula.simple.ClauseList;


//Lit x is a choice if:
// x is the chosen rep of some set of equiv variables or
// x is not equivalent to some disjunctive clause of literals
//	(this is equivalent to saying that x is not implied by any literal
//		OR x is implied by >=1 literal, and at least one of those literals
//			is always part of any clause x is.

//THE CURRENT IMPL IS WRONG (OR ACTUALLY I THINK IT'S THE UNDERLYING PROCEDURE)
public class NotImpliedChoices implements ChoiceGetter {
	private static Integer EQUIVALENT = 1;
	private static Integer IMPLIES = 2;
	private static Integer IMPLIEDBY = 3;
	private static Integer NA = 4;

	boolean[] choice;
	int numVars;

	@Override
	public void computeChoices(ClauseList orig) {
		numVars = orig.getContext().size();

		//[k][i], does some literal of [k] imply [i]?
		int[][] choices = new int[2*numVars+1][2*numVars+1];

		for(int k = 0; k < choices.length; k++) {
			Arrays.fill(choices[k],NA);//EQUIVALENT);//LitUtil.getLit(k,numVars));
			//			choices[k][k] = NA;
		}

		boolean[] exists = new boolean[2*numVars+1];
		for(int[] model : orig.getClauses()) {
			for(int i : model) {
				exists[LitUtil.getIndex(i,numVars)] = true;
			}
		}
		
		for(int[] model : orig.getClauses()) {
			//In order to ensure works even if all lits aren't present
			for(int k = 0; k < model.length; k++) {
				int kPosIndex = LitUtil.getIndex(model[k],numVars);
				for(int i = 0; i < model.length; i++) {
					if(i == k) continue;
					int iPosIndex = LitUtil.getIndex(model[i],numVars);

					choices[kPosIndex][iPosIndex] = EQUIVALENT;
				}
			}
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
		choice = new boolean[2*numVars+1];
		int numChoices = 0;
		HashSet<Integer> impliedLits = new HashSet<Integer>();
		for(int k = 0; k < 2*numVars + 1; k++) {
			if(!exists[k]) continue;
			impliedLits.clear();
			
			int lit = LitUtil.getLit(k,numVars);
			if(lit == 0) continue;
			boolean aChoice = true;
			for(int i = 0; i < 2*numVars + 1; i++) {
				if(!exists[i]) continue;
				
				int liti = LitUtil.getLit(i,numVars);
				if(liti == 0 || liti == -lit) continue;
				if(choices[k][i] == IMPLIEDBY) {
					impliedLits.add(liti);
					System.out.println((liti < 0 ? "-" : "") + orig.getContext().getVar(Math.abs(liti)).getName() + " implies " + (lit < 0 ? "-" : "") +  orig.getContext().getVar(Math.abs(lit)).getName());
				} else if(choices[k][i] == EQUIVALENT) {
					if(Math.abs(lit) < Math.abs(liti) || (Math.abs(lit) == Math.abs(liti) && lit < 0)) {
						//Not smallest equivalent literal
						aChoice = false;
						break;
					}
				}
			}
			
			if(aChoice && impliedLits.size() > 0) { 
				aChoice = false;
				for(int[] model : orig.getClauses()) {
					boolean foundLit = false;
					boolean foundImplied = false;
					for(int i = 0; i < model.length; i++) {
						int liti = model[i];
						if(impliedLits.contains(liti)) {
							foundImplied = true;
							break;
						} else if(liti == lit) {
							foundLit = true;
						} else if(liti == -lit) {
							continue;
						}
					}
					
					if(foundLit && !foundImplied) {
						aChoice = true;
						break;
					}
				}
			}

			if(aChoice) {
				choice[k] = true;
				numChoices++;
			} 
		}


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

	@Override
	public ClauseList getList(ClauseList clauses) {
		ClauseList choiceList = new ClauseList(clauses.getContext());

		for(int[] model : clauses.getClauses()) {
			int[] next = getChoiceInterp(model);
			choiceList.fastAddClause(next);
		}
		return choiceList;
	}

	@Override
	public int[] getChoiceInterp(int[] interp) {
		int nextIndex = 0;
		int curSize = 0;
		for(int i : interp) {
			if(choice[LitUtil.getIndex(i,numVars)]) {
				curSize++;
			}
		}

		int[] next = new int[curSize];
		//			int[] next = new int[numChoices];

		for(int i : interp) {
			if(choice[LitUtil.getIndex(i,numVars)]) {
				next[nextIndex] = i;
				nextIndex++;
			}
		}
		return next;
	}

	@Override
	public boolean isChoice(int lit) {
		return choice[LitUtil.getIndex(lit,numVars)];
	}
	
	@Override
	public String toString() {
		return "NotImpl";
	}

}
