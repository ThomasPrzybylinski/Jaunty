package task.formula;

import task.formula.random.CNFCreator;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Constant;
import formula.Disjunctions;
import formula.VariableContext;
import formula.simple.CNF;

public class FormulaCreatorToCNFCreator implements CNFCreator {
	private FormulaCreator creat;
	
	public FormulaCreatorToCNFCreator(FormulaCreator  creat) {
		this.creat = creat;
	}
	
	@Override
	public CNF generateCNF(VariableContext context) {
		BoolFormula form = creat.nextFormula();
		
		while(context.getNumVarsMade() < form.getCurContext().getNumVarsMade()) {
			context.createNextDefaultVar();
		}
		
		form = form.toCNF();
		
		if(form instanceof Conjunctions) {
			CNF ret = new CNF((Conjunctions)form);
			return ret;
		} else if (form instanceof Disjunctions) {
			Conjunctions conj = new Conjunctions(form);
			CNF ret = new CNF(conj);
			return ret;
		} else if(form == Constant.FALSE) {
			return CNF.contradiction;
		} else if(form == Constant.TRUE) {
			return CNF.tautology;
		}
		
		
		return null;
	}

}
