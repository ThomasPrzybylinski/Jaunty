package subsumptionMain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import task.formula.FormulaCreator;
import task.formula.random.SimpleCNFCreator;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;


public class ResolutionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		for(int k = 0; k < 10; k++) {
			FormulaCreator fc = new SimpleCNFCreator(10,4.3,3);		
			Conjunctions c = (Conjunctions)(fc.nextFormula());
			//System.out.println(c.reduce());
			List<Disjunctions> resolvants = getResolvants(c); 
			//System.out.println(resolvants);

			Conjunctions c2 = c.getCopy();

			for(Disjunctions d : resolvants) {
				c2.add(d);
			}

			BoolFormula bf = c2.reduce();

			if(bf instanceof Conjunctions) {
				c2 = (Conjunctions)bf;
				c2 = c2.trySubsumption();

				//System.out.println(c2.reduce());
				System.out.println(c2.unitPropagate().reduce());
			}

			//System.out.println(TruthTableHelper.getModels(c));
		}
	}

	//Assume Conj is in CNF form
	public static List<Disjunctions> getResolvants(Conjunctions c) {
		c = (Conjunctions)c.reduce(); //To ensure consistent literal ordering
		Set<String> curClauses = new HashSet<String>();

		List<Disjunctions> totalClauses = new ArrayList<Disjunctions>();
		List<Disjunctions> resolvants = new ArrayList<Disjunctions>();
		for(BoolFormula f : c.getFormulas()) {
			curClauses.add(f.toString());
			if(f instanceof Literal) {
				f = new Disjunctions(f);
			}
			totalClauses.add((Disjunctions)f);
		}

		for(int k = 0; k < totalClauses.size(); k++) {
			for(int i = k+1; i < totalClauses.size();i++) {
				Disjunctions d1 = totalClauses.get(k);
				Disjunctions d2 = totalClauses.get(i);
				Disjunctions res = getResolvant(d1,d2);
				if(res != null) {
					String cStr = res.toString();
					if(!curClauses.contains(cStr)) {
						curClauses.add(cStr);
						totalClauses.add(res);
						resolvants.add(res);
					}
				}
			}
		}
		return resolvants;
	}

	public static Disjunctions getResolvant(Disjunctions d1, Disjunctions d2) {
		Literal match = getMatchVar(d1, d2);

		if(match == null) return null;

		Disjunctions ret = new Disjunctions();
		for(BoolFormula bf : d1.getFormulas()) {
			Literal v = (Literal) bf;
			if(!v.getVar().equals(match.getVar())) {
				ret.add(v);
			}
		}

		for(BoolFormula bf : d2.getFormulas()) {
			Literal v = (Literal) bf;
			if(!v.getVar().equals(match.getVar())) {
				ret.add(v);
			}
		}

		BoolFormula test = ret.reduce(); //To ensure consistent literal ordering

		if(test instanceof Literal) {
			return new Disjunctions(test);
		} else if(test instanceof Disjunctions) {
			return (Disjunctions)test;
		}

		return null;

	}

	private static Literal getMatchVar(Disjunctions d1, Disjunctions d2) {
		for(BoolFormula bf1 : d1.getFormulas()) {
			for(BoolFormula bf2 : d2.getFormulas()) {
				Literal v1 = (Literal)bf1;
				Literal v2 = (Literal)bf2;

				if(v1.equals(v2.negate())) {
					return v1;
				}
			}
		}
		return null;
	}

}
