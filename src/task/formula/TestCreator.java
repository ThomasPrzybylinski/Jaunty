package task.formula;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class TestCreator extends FormulaCreator {

	public TestCreator() {
		super(4);
	}

	@Override
	public BoolFormula nextFormulaImpl() {
		Literal p = context.getOrCreateVar("p").getPosLit();
		Literal q = context.getOrCreateVar("q").getPosLit();
		Literal s = context.getOrCreateVar("s").getPosLit();
		Literal r = context.getOrCreateVar("r").getPosLit();

//		Variable notp = p.negate();
//		Variable notq = q.negate();
		Literal nots = s.negate();
//		Variable notr = r.negate();

		Disjunctions d1 = new Disjunctions();
		d1.add(p);
		d1.add(q);
		d1.add(nots);

		Disjunctions d2 = new Disjunctions();
		d2.add(p);
		d2.add(q);
		d2.add(r);

		Disjunctions d3 = new Disjunctions();
		d3.add(p);
		d3.add(r);
		d3.add(s);

		Disjunctions d4 = new Disjunctions();
		d4.add(p);
		d4.add(q);

		Conjunctions sat = new Conjunctions();
		sat.add(d1);
		sat.add(d2);
		sat.add(d3);
		sat.add(d4);

		return sat;

	}

}
