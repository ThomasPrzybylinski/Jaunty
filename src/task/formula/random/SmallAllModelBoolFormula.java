package task.formula.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.TimeoutException;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;
import formula.simple.DNF;
import hornGLB.AssignmentIter;
import hornGLB.BasicAssignIter;
import task.formula.FormulaCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;


//Creates random formula when numVars is relatively small
public class SmallAllModelBoolFormula extends FormulaCreator implements ModelGiver {
	private int numTrue;
	private Random rand = new Random();
	private ArrayList<int[]> models;
	
	public SmallAllModelBoolFormula(int numVars, int numTrue, int seed) {
		this(numVars,numTrue);
		rand = new Random(seed);
	}
	
	public SmallAllModelBoolFormula(int numVars, int numTrue) {
		super(numVars);
		this.numTrue = numTrue;
		
		models = new ArrayList<int[]>((int)Math.pow(2,numVars));
		
		AssignmentIter iter = new BasicAssignIter(numVars);
		
		while(iter.hasNext()) {
			int[] next = iter.next();
			
//			for(int k = 0; k < next.length; k++) {
//				if(next[k] == 0) {
//					next[k] = -(k+1);
//				} else {
//					next[k] = k+1;
//				}
//			}
			
			models.add(next);
		}
	}
	
	public DNF getDNF() {
		return getDNF(VariableContext.defaultContext);
	}
	
	public DNF getDNF(VariableContext context) {
		Collections.shuffle(models,rand);
		
		DNF dnf = new DNF(context);
		
		for(int k = 0; k < numTrue; k++) {
			int[] model = models.get(k);
			
			int[] clause = new int[model.length];
			for(int i = 0; i < vars.length; i++) {
				if(model[i] == 0) {
					clause[i] = -(i+1);
				} else {
					clause[i] = i+1;
				}
			}
			
			dnf.addClause(clause);
		}
		return dnf;
	}

	@Override
	public BoolFormula nextFormulaImpl() {
		Collections.shuffle(models,rand);
		
		Disjunctions dnf = new Disjunctions();
		for(int k = 0; k < numTrue; k++) {
			int[] model = models.get(k);
			
			Conjunctions clause = new Conjunctions();
			for(int i = 0; i < vars.length; i++) {
				Literal toAdd;
				if(model[i] == 0) {
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
		return getDNF(context).getClauses();
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
		// TODO Auto-generated method stub
		return "SmallAllModel("+models.get(0).length+","+numTrue+")";
	}

	

}
