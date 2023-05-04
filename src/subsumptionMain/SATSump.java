package subsumptionMain;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import task.formula.AllFilledSquaresCNF;
import task.formula.random.CNFCreator;
import task.sat.SATUtil;
import util.formula.FormulaForAgreement;
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
		CNFCreator creat = new AllFilledSquaresCNF(3);
		int mid = 12;
		CNF cnf = creat.generateCNF(VariableContext.defaultContext);
		System.out.println(cnf.getDeepSize());
		System.out.println(cnf);

	
		//		for(int k = 0; k < models.size(); k++) {
		//			System.out.println(numPosVars(models.get(k)));
		//		}

		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,CNF.COMPARE);
		
		cnf = SATSump.getSubsumedConj(cnf);
		int oldSize = -1;
		int newSize = cnf.getDeepSize();
		


		while(oldSize != newSize) {
			cnf.addAll((new FormulaForAgreement(cnf)).getResolvants());// getSubsumedConj(cnf);
			cnf = cnf.trySubsumption();
			oldSize = newSize;
			newSize = cnf.getDeepSize();
			System.out.println(newSize);
		}
		System.out.println(cnf.getDeepSize());

		for(int[] i : cnf.getClauses()) {
			if(i[0] == 1 || i[0] == -1) {
				System.out.println(Arrays.toString(i));
			}
		}

		List<int[]> models2 = SATUtil.getAllModels(cnf);
		Collections.sort(models2,CNF.COMPARE);

		for(int k = 0; k < Math.min(models.size(), models2.size()) ; k++) {
			if(!Arrays.equals(models.get(k),models2.get(k))) {
				System.out.println("rhag");
			}
		}


//		if(models.size() != models2.size()) {
//			System.out.println("Blah");
//			for(int[] i : models) {
//				System.out.println(Arrays.toString(i));
//			}
//			
//			System.out.println();
//			
//			for(int[] i : models2) {
//				System.out.println(Arrays.toString(i));
//			}
//			
//		}
	}

	private static int numPosVars(int[] model) {
		int num = 0;
		for(int i : model) {
			if(i > 0) num++;
		}
		return num;
	}

	public static CNF getSubsumedConj(CNF cnf) {
		int oldSize = -1;
		int newSize = cnf.getDeepSize();

		LitsMap computed = new LitsMap(cnf.getContext().size());
		LitsMap tested = new LitsMap(cnf.getContext().size());

		while(oldSize != newSize) {
			cnf = getSubsumedConjPart(cnf,computed,tested);
			oldSize = newSize;
			newSize = cnf.getDeepSize();
			//			System.out.println(newSize);
		}
		cnf.sort();
		return cnf;
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

	public static CNF getSubsumedConjPart(CNF c) {
		return getSubsumedConjPart(c, null, new LitsMap(c.getContext().size()));
	}

	
	public static CNF getSubsumedConjPart(CNF c, LitsMap computed, LitsMap tested) {
		return getSubsumedConjPart(c,computed,tested,true);
	}
	
	public static CNF getSubsumedConjPart(CNF c, LitsMap computed, LitsMap tested,final boolean addAll) {
		int prevSize = c.size();
		boolean added = false;
		CNF ret = c.getDirectCopy();
		ISolver solver;
		try {
			solver = ret.getSolverForCNF();
		} catch(ContradictionException ce) {
			return CNF.contradiction;
		}

		LinkedList<int[]> toTry = new LinkedList<int[]>();

		for(int k = 0; k < c.getClauses().size(); k++) {
			int[] d = ret.getClauses().get(k);

			if(computed != null && computed.contains(d)) {
				continue;
			} else if(computed != null) {
				computed.put(d,null);
			}

			if(d.length <= 1) {
				continue;
			}

			for(int i = 0; i < d.length; i++) {
				//We want to see if (c implies (d - ithVariable)) is a tautology. 
				//In other words if not (c implies (d - ithVariable)) is a contradiction (equiv to c and not (d-ithVariable))
				//If it is, adding
				// (d-ithVariable) to c does not change c, and the new clause will subsume the old
				int[] toAdd = new int[d.length-1];
				int[] constr = new int[d.length-1];
				int index = 0;
				for(int j = 0; j < d.length; j++) {
					if(j == i) continue;
					constr[index] = -d[j];
					//					c2.fastAddClause(-d[j]);
					toAdd[index] = d[j];
					index++;
				}
//				if(computed != null && computed.contains(toAdd)) {
//					continue;
//				} else if(computed != null) {
//					computed.put(toAdd,null);
//				}
				
				//				int[] clause = new int[toAdd.length];
				//				for(int j = 0; j < toAdd.length; j++) {
				//					clause[j] = d[j];
				//				}

				LitSorter.inPlaceSort(toAdd);


				if(!tested.contains(toAdd)) {
					tested.put(toAdd,null);
					try {
						if(!solver.isSatisfiable(new VecInt(constr) )) {
							added = true;
							ret.fastAddClause(toAdd);
							if(!addAll) break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		solver.reset();
		ret.sort();
		return added ? ret.trySubsumption() : c;
	}

	public static CNF getSubsumedConjPartDFS(CNF c, LitsMap computed) {
		int prevSize = c.size();
		CNF ret = c.getCopy();
		ISolver solver;
		try {
			solver = ret.getSolverForCNF();
		} catch(ContradictionException ce) {
			return CNF.contradiction;
		}

		LitsMap<Object> tested = new LitsMap<Object>(c.getContext().size());

		for(int k = 0; k < c.getClauses().size(); k++) {
			int[] d = ret.getClauses().get(k);

			if(computed != null && computed.contains(d)) {
				continue;
			} else if(computed != null) {
				computed.put(d,null);
			}

			if(d.length <= 1) {
				continue;
			}

			for(int i = 0; i < d.length; i++) {
				//We want to see if (c implies (d - ithVariable)) is a tautology. 
				//In other words if not (c implies (d - ithVariable)) is a contradiction (equiv to c and not (d-ithVariable))
				//If it is, adding
				// (d-ithVariable) to c does not change c, and the new clause will subsume the old
				int[] toAdd = new int[d.length-1];
				int[] constr = new int[d.length-1];
				int index = 0;
				for(int j = 0; j < d.length; j++) {
					if(j == i) continue;
					constr[index] = -d[j];
					toAdd[index] = d[j];
					index++;
				}

				LitSorter.inPlaceSort(toAdd);

				if(!tested.contains(toAdd)) {
					tested.put(toAdd,null);
					try {
						if(!solver.isSatisfiable(new VecInt(constr) )) {
							d = toAdd;
							i--;
							ret.fastAddClause(toAdd);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ret.trySubsumption();
	}

	public static CNF testGetSubsumedConj(CNF cnf) {
		int oldSize = -1;
		int newSize = cnf.getDeepSize();

		LitsMap computed = new LitsMap(cnf.getContext().size());

		while(oldSize != newSize) {
			cnf = getSubsumedConjPartDFS(cnf,computed);
			oldSize = newSize;
			newSize = cnf.getDeepSize();
			System.out.println(newSize);
		}
		cnf.sort();
		cnf = getSubsumedConj(cnf);
		return cnf;
	}
	
	
	public static CNF getPrimify(CNF cnf) {
		CNF old = null;
		CNF cur = cnf;

		LitsMap computed = new LitsMap(cnf.getContext().size());
		LitsMap tested = new LitsMap(cnf.getContext().size());

		while(old != cur) {
			old = cur;
			cur = getSubsumedConjPart(cur,computed,tested,false);
			//			System.out.println(newSize);
		}
		cur.sort();
		return cur;
	}



}
