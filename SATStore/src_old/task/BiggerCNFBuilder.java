package task;

import java.util.List;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class BiggerCNFBuilder implements FormulaTask {

	@Override
	public String aggregateReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String executeReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeTask(BoolFormula formula) {
		List<BoolFormula> clauses;
		Conjunctions cnf = (Conjunctions)formula;
		
		clauses = cnf.getFormulas();
		
		while(true) {
			for(int k = 0; k < clauses.size(); k++) {
				for(int i = 0; i < clauses.size(); i++) {
					if(i == k) continue;
					 Disjunctions d1 = (Disjunctions)clauses.get(k);
					 Disjunctions d2 = (Disjunctions)clauses.get(i);
					 
					 int resolve1 = -1;
					 int resolve2 = -1;

					 getResolvantsLoop: for(int k2 = 0; k2 < d1.getFormulas().size(); k2++) {
						 for(int i2 = 0; i2 < d2.getFormulas().size(); i2++) {
							 Literal v1 = (Literal)d1.getFormulas().get(k2);
							 Literal v2 = (Literal)d2.getFormulas().get(i2);
							
							if(v1.getVar().equals(v2.getVar()) && v1.isPos() != v2.isPos()) {
								resolve1 = k2;
								resolve2 = i2;
								break getResolvantsLoop;
							}
						 }
					 }
					 
					 if(resolve1 != -1) {
						//Disjunctions newDisj 
					 }
				}
			}
		}

	}

}
