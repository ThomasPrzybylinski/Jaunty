package task.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import formula.VariableContext;
import formula.simple.CNF;
import util.lit.LitSorter;

//Not thread safe
public class FormulaRandomizer {

	int[] translationToRand;
	int[] translationFromRand;
	Random rand;
	
	private CNF cnf = null;
	private int origVar;
	
	public FormulaRandomizer(CNF cnf, Random rand, int origVars) {
		this.cnf = cnf;
		this.rand = rand;
		this.origVar = origVars;
		setupCurRand(cnf.getContext());
	}
	
	public CNF getRandom() {
		CNF ret = new CNF(cnf.getContext());
		
		for(int[] cl : cnf.getClauses()) {
			int[] toAdd = new int[cl.length];

			for(int k = 0; k < cl.length; k++) {
				int lit = cl[k];
				int var = Math.abs(lit);
				toAdd[k] = (lit/var)*translationToRand[var];
			}

			LitSorter.inPlaceSort(toAdd);
			ret.fastAddClause(toAdd);
		}
		ret.sort();
		cnf = null;
		
		return ret;
	}
	
	public void setupRandomness() {
		cnf = setupCurRand(cnf.getContext());
	}

	private CNF setupCurRand(VariableContext context) {
		translationToRand = new int[context.size()+1];
		translationFromRand = new int[context.size()+1];
		ArrayList<Integer> toRandomize = new ArrayList<Integer>();
		
		int numToRand = origVar;
		
		for(int k = 1; k <= numToRand; k++) {
			toRandomize.add(k);
		}
		
		Collections.shuffle(toRandomize);
		
		for(int k = 1; k <= numToRand; k++) {
			translationToRand[k] = toRandomize.get(k-1) * (rand.nextBoolean() ? 1 : -1);
			translationFromRand[Math.abs(translationToRand[k])] = k * (translationToRand[k]/Math.abs(translationToRand[k]));
		}
		for(int k = numToRand+1; k <= context.size(); k++) {
			translationToRand[k] = k;
			translationFromRand[k] = k;
		}
		return cnf;
	}
	
	public int[] translateToOrig(int[] model) {
		return translate(model,translationFromRand);
	}
	
	public int[] translateToInternal(int[] model) {
		return translate(model,translationToRand);
	}
	
	private int[] translate(int[] model, int[] translation) {
		int[] ret = new int[model.length];
		
		for(int k = 0; k < model.length; k++) {
			int lit = model[k];
			int var = Math.abs(lit);
			ret[k] = translation[var]*(lit/var);
		}
		LitSorter.inPlaceSort(ret);
		
		return ret;
	}
}
