package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.lit.LitSorter;

//Not thread safe
public class FormulaCreatorRandomizer implements CNFCreator, ConsoleDecodeable,
		FileDecodable {

	CNFCreator orig;
	int[] translationToRand;
	int[] translationFromRand;
	Random rand;
	
	private CNF cnf = null;
	
	public FormulaCreatorRandomizer(CNFCreator creator, Random rand) {
		this.orig = creator;
		this.rand = rand;
	}
	
	@Override
	public CNF generateCNF(VariableContext context) {
		if(cnf == null) {
			cnf = setupCurRand(context);
		}
		
		CNF ret = new CNF(context);
		
		for(int[] cl : cnf.getClauses()) {
			int[] toAdd = translateToInternal(cl);
			ret.fastAddClause(toAdd);
		}
		ret.sort();
		cnf = null;
		
		return ret;
	}
	
	public void setupRandomnessAlready(VariableContext context) {
		cnf = setupCurRand(context);
	}

	private CNF setupCurRand(VariableContext context) {
		CNF cnf = orig.generateCNF(context);
		translationToRand = new int[context.size()+1];
		translationFromRand = new int[context.size()+1];
		ArrayList<Integer> toRandomize = new ArrayList<Integer>();
		
		int numToRand = context.size();
		if(orig instanceof ModelsCNFCreator) {
			ModelsCNFCreator mcc = ((ModelsCNFCreator)orig);
			if(mcc.getPrevMods() != null) {
				//Our "true true" original vars is different in this case
				numToRand = mcc.getOrigVars();
			}
		}
		
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

	
	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		if(orig instanceof FileDecodable) {
			model = translate(model,translationFromRand);
			
			((FileDecodable)orig).fileDecoding(dir,filePrefix,model);
		}
		//else throw exception?

	}


	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		if(orig instanceof FileDecodable) {
			model = translate(model,translationFromRand);
			
			((FileDecodable)orig).fileDecoding(filePrefix,model);
		}
		//else throw exception?

	}

	@Override
	public String consoleDecoding(int[] model) {
		model = translate(model,translationFromRand);
		if(orig instanceof ConsoleDecodeable) {
			return ((ConsoleDecodeable)orig).consoleDecoding(model);
		} else {
			return (new DefaultConsoleDecoder()).consoleDecoding(model);
		}
		//else throw exception?
	}

	@Override
	public String toString() {
		return "Randomized"+orig.toString();
	}



}
