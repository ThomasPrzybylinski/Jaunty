package subsumptionMain;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;

public class TempTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Literal a = VariableContext.defaultContext.getOrCreateVar("a").getPosLit();
		Literal b = VariableContext.defaultContext.getOrCreateVar("b").getPosLit();
		Literal c = VariableContext.defaultContext.getOrCreateVar("c").getPosLit();
		@SuppressWarnings("unused")
		Literal d = VariableContext.defaultContext.getOrCreateVar("d").getPosLit();
		Literal e = VariableContext.defaultContext.getOrCreateVar("e").getPosLit();
		Literal f = VariableContext.defaultContext.getOrCreateVar("f").getPosLit();
		Literal g = VariableContext.defaultContext.getOrCreateVar("g").getPosLit();
		Literal h = VariableContext.defaultContext.getOrCreateVar("h").getPosLit();
		
		Disjunctions d1 = new Disjunctions(a,b,c);
		Disjunctions d2 = new Disjunctions(a.negate(),e.negate());
		Disjunctions d3 = new Disjunctions(e,b,f.negate());
		Disjunctions d4 = new Disjunctions(f,c);
		Disjunctions d5 = new Disjunctions(c.negate(),g.negate());
		Disjunctions d6 = new Disjunctions(g,b,h.negate());
		Disjunctions d7 = new Disjunctions(h,a);
		
		Conjunctions cur = new Conjunctions(d1,d2,d3,d4,d5,d6,d7);
		cur = SATSump.getSubsumedConj(cur);
		System.out.println(cur);

	}

}
