package subsumptionMain;

import java.util.Collections;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.FormulaCreator;
import task.formula.random.SimpleCNFCreator;
import util.LitSorter;
import util.LitsMap;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;

public class SATSump {

	public static void main(String[] args)  throws Exception{
		FormulaCreator fc = new SimpleCNFCreator(100,4.3,3);		
		Conjunctions c = (Conjunctions)(fc.nextFormula());

		//		Conjunctions c2 = getSubsumedConj(c);
		//		Collections.sort(c2.getFormulas());
		//		System.out.println(c2.unitPropagate().reduce());
		//System.out.println(TruthTableHelper.getModels(c2));
		System.err.println();

		c = getSubsumedConj(c);
		System.out.println(c.getFormulas().size());
		Collections.sort(c.getFormulas());
		System.out.println(c.reduce());


		//		
		//		List<Disjunctions> resolvants = ResolutionTest.getResolvants(c); 
		//		Conjunctions c3 = c.getCopy();
		//
		//		for(Disjunctions d : resolvants) {
		//			c3.add(d);
		//		}
		//
		//		BoolFormula bf = c3.reduce();
		//
		//		if(bf instanceof Conjunctions) {
		//			c3 = (Conjunctions)bf;
		//			Collections.sort(c3.getFormulas());
		//			c3 = c3.trySubsumption();
		//
		//
		//			System.out.println(c3.getFormulas().size());
		//			System.out.println(c3.reduce());
		//			//System.out.println(c3.unitPropagate().reduce());
		//			//System.out.println(TruthTableHelper.getModels(c3));
		//		}
	}

	public static Conjunctions getSubsumedConj(Conjunctions c) {
		Conjunctions ret = c.getCopy();
		LitsMap<Object> added = new LitsMap<Object>(c.getCurContext().getNumVarsMade());
		for(int k = 0; k < ret.getFormulas().size(); k++) {
			BoolFormula bf = ret.getFormulas().get(k);

			if(bf instanceof Literal) {
				bf = new Disjunctions(bf);
			}

			Disjunctions d = (Disjunctions)bf;

			if(d.getFormulas().size() <= 1) {
				continue;
			}

			for(int i = 0; i < d.getFormulas().size(); i++) {
				Conjunctions c2 = c.getCopy(); //We want to see if (c implies (d - ithVariable)) is a tautology. 
				//In other words if not (c implies (d - ithVariable)) is a contradiction (equiv to c and not (d-ithVariable))
				//If it is, adding
				// (d-ithVariable) to c does not change c, and the new clause will subsume the old
				Disjunctions toAdd = new Disjunctions();
				for(int j = 0; j < d.getFormulas().size(); j++) {
					if(j == i) continue;
					c2.add(((Literal)d.getFormulas().get(j)).negate());
					toAdd.add(d.getFormulas().get(j));
				}

				int[] clause = new int[toAdd.getFormulas().size()];
				for(int j = 0; j < toAdd.getFormulas().size(); j++) {
					clause[j] = ((Literal)d.getFormulas().get(j)).getIntRep();
				}
				
				LitSorter.inPlaceSort(clause);


				if(!added.contains(clause)) {
					try {
						ISolver solver = c2.getSolverForCNF();

						if(!solver.isSatisfiable()) {
							added.put(clause,null);
							ret.add(toAdd);
						}
					} catch(ContradictionException ce) {
						//if a contradiction, unsat
						added.put(clause,null);
						ret.add(toAdd);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ret.trySubsumption();
	}

}
