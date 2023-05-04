package task.formula.random;

import java.util.List;
import java.util.Random;

import org.sat4j.specs.TimeoutException;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;
import formula.simple.ClauseList;
import task.formula.FormulaCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;

public class WeakTrueBoolFormula extends FormulaCreator implements ModelGiver {
	private int numTrue;
	private static final Random rand = new Random();
	
	
	public WeakTrueBoolFormula(int numVars, int numTrue) {
		super(numVars);
		this.numTrue = numTrue;
	}
	
	public WeakTrueBoolFormula(int numVars, int numTrue, int seed) {
		super(numVars);
		this.numTrue = numTrue;
		rand.setSeed(seed);
	}

	@Override
	public Disjunctions nextFormulaImpl() {
		Disjunctions dnf = new Disjunctions();
		for(int k = 0; k < numTrue; k++) {
			Conjunctions clause = new Conjunctions();
			for(int i = 0; i < vars.length; i++) {
				Literal toAdd;
				if(rand.nextInt(2) == 0) {
					toAdd = vars[i].getNegLit();
				} else {
					toAdd = vars[i].getPosLit();
				}
				clause.add(toAdd);
			}
			dnf.add(clause);
		}
		return dnf;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		ClauseList cl = new ClauseList(context);
				
		for(int k = 0; k < numTrue; k++) {
			int[] model = new int[vars.length];
			for(int i = 0; i < vars.length; i++) {
				model[i] = rand.nextInt(2) == 0 ? (i+1) : -(i+1); 
			}
			cl.addClause(model);
		}
		return cl.reduce().getClauses();
		
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDirName() {
		return "WeakTrue("+super.context.size()+','+numTrue+')';
	}

	

}
