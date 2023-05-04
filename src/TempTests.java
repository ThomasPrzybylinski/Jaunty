
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;


public class TempTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VariableContext cntxt = new VariableContext();
		Variable a = cntxt.createNextDefaultVar();
		Variable b = cntxt.createNextDefaultVar();
		Variable c = cntxt.createNextDefaultVar();
		Variable d = cntxt.createNextDefaultVar();
		Variable e = cntxt.createNextDefaultVar();
		Variable f = cntxt.createNextDefaultVar();
		
		Disjunctions conj= new Disjunctions(
				new Conjunctions(a.getPosLit(),b.getPosLit()),
				new Conjunctions(c.getPosLit(),d.getPosLit()),
				new Conjunctions(e.getPosLit(),f.getPosLit())
				);
		System.out.println(conj.toCNF());
				
	}
}
