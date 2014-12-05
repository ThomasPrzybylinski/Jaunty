package subsumptionMain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.AllSquaresCNF;
import task.formula.random.CNFCreator;
import task.sat.SATUtil;
import util.lit.LitSorter;
import util.lit.LitsMap;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.VariableContext;
import formula.simple.CNF;

public class SATSump {

	public static void main(String[] args)  throws Exception{
		CNFCreator creat = new AllSquaresCNF(5);
		CNF cnf = creat.generateCNF(VariableContext.defaultContext);
		System.out.println(cnf.getDeepSize());
		System.out.println(cnf);
		
		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,CNF.COMPARE);
		
		cnf = getSubsumedConj(cnf);
		
		System.out.println(cnf.getDeepSize());
		System.out.println(cnf);
		List<int[]> models2 = SATUtil.getAllModels(cnf);
		Collections.sort(models2,CNF.COMPARE);
		
		if(models.size() != models2.size()) {
			System.out.println("blah");
		} else {
			for(int k = 0; k < models.size(); k++) {
				if(!Arrays.equals(models.get(k),models2.get(k))) {
					System.out.println("rhag");
				}
			}
		}
		
		
//		FormulaCreator fc = new SimpleCNFCreator(100,4.3,3);		
//		Conjunctions c = (Conjunctions)(fc.nextFormula());
//
//		//		Conjunctions c2 = getSubsumedConj(c);
//		//		Collections.sort(c2.getFormulas());
//		//		System.out.println(c2.unitPropagate().reduce());
//		//System.out.println(TruthTableHelper.getModels(c2));
//		System.err.println();
//
//		c = getSubsumedConj(c);
//		System.out.println(c.getFormulas().size());
//		Collections.sort(c.getFormulas());
//		System.out.println(c.reduce());


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
		LitsMap<Object> added = new LitsMap<Object>(c.getCurContext().size());
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
	
	public static CNF getSubsumedConj(CNF c) {
		CNF ret = c.getCopy();
		LitsMap<Object> tested = new LitsMap<Object>(c.getContext().size());
		for(int k = 0; k < ret.getClauses().size(); k++) {
			int[] d = ret.getClauses().get(k);

			if(d.length <= 1) {
				continue;
			}

			for(int i = 0; i < d.length; i++) {
				CNF c2 = c.getCopy(); //We want to see if (c implies (d - ithVariable)) is a tautology. 
				//In other words if not (c implies (d - ithVariable)) is a contradiction (equiv to c and not (d-ithVariable))
				//If it is, adding
				// (d-ithVariable) to c does not change c, and the new clause will subsume the old
				int[] toAdd = new int[d.length-1];
				int index = 0;
				for(int j = 0; j < d.length; j++) {
					if(j == i) continue;
					c2.addClause(-d[j]);
					toAdd[index] = d[j];
					index++;
				}

//				int[] clause = new int[toAdd.length];
//				for(int j = 0; j < toAdd.length; j++) {
//					clause[j] = d[j];
//				}
				
				LitSorter.inPlaceSort(toAdd);


				if(!tested.contains(toAdd)) {
					tested.put(toAdd,null);
					try {
						ISolver solver = c2.getSolverForCNF();

						if(!solver.isSatisfiable()) {
							ret.addClause(toAdd);
						}
					} catch(ContradictionException ce) {
						//if a contradiction, unsat
						ret.addClause(toAdd);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ret.trySubsumption();
	}

}
