package task.formula.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.TimeoutException;

import task.formula.FormulaCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;
import formula.simple.ClauseList;

public class RandLitFreqBoolFormula implements ModelGiver {
	private int numTrue;
	private int numVars;
	private static final Random rand = new Random();
	
	
	public RandLitFreqBoolFormula(int numVars, int numTrue) {
		this.numTrue = numTrue;
	}
	
	public RandLitFreqBoolFormula(int numVars, int numTrue, int seed) {
		this.numTrue = numTrue;
		this.numVars = numVars;
		rand.setSeed(seed);
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		context.ensureSize(numVars);
		ClauseList cl = new ClauseList(context);
		ArrayList<int[]> models = new ArrayList<int[]>(numTrue);
		
		for(int k = 0; k < numTrue; k++) {
			models.add(new int[numVars]);
		}
				
		for(int k = 1; k <= numVars; k++) {
			Collections.shuffle(models,rand);
			int numPos = rand.nextInt(numTrue);
			
			for(int i = 0; i < numTrue; i++) {
				int[] model = models.get(i);
				model[k-1] = i <= numPos ? k : -k;
			}
		}
		cl.addAll(models);
		return cl.reduce().getClauses();
		
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;
	}

	@Override
	public String getDirName() {
		return "WeakTrue("+numVars+','+numTrue+')';
	}

	

}
