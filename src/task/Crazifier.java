package task;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import hornGLB.AssignmentIter;
import hornGLB.BasicAssignIter;

public class Crazifier {

	public static Conjunctions crazify(Conjunctions cnf) {
		Conjunctions ret = new Conjunctions();
		for(BoolFormula form1 : cnf.getFormulas()) {
			Disjunctions d = (Disjunctions)form1;
			AssignmentIter renames = new BasicAssignIter(d.getFormulas().size());

			while(renames.hasNext()) {
				Disjunctions cur = d;
				int[] renamer = renames.next();

				for(int k = 0; k < renamer.length; k++) {
					if(renamer[k] == 1) {
						cur = invertVar(cur,(Literal)d.getFormulas().get(k));
					}
				}
				
				ret.add(cur);
			}
		}
		return ret;
	}

	public static Disjunctions invertVar(Disjunctions d, Literal v) {
		Disjunctions newD = new Disjunctions();
		for(BoolFormula form2 : d.getFormulas()) {
			Literal var = (Literal)form2;
			if(v.getVar().equals(var.getVar())) {
				newD.add(var.negate());
			} else {
				newD.add(var);
			}
		}
		return newD;
	}


}
