package task.sat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.sat4j.specs.ContradictionException;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;


//Clauses are assumed to be sorted
public class ClausalIntersectionSolver {

	public static boolean isSat(CNF cnf) {
		List<int[]> clauses = getCover(cnf);

		if(clauses.size() == 0) return true;
		if(clauses.get(0).length == 0) return false;

		CNF newList = new CNF(cnf.getContext());

		for(int k = 0; k < clauses.size(); k++) {
			int[] clause1 = clauses.get(k);
			for(int i = k; i < clauses.size(); i++) { 
				int[] clause2 = clauses.get(i);
				int[] intersect = getIntersection(clause1,clause2);
				
				if(intersect != null) {
					newList.addClause(intersect);
				}
			}
		}

		newList = newList.reduce();// .trySubsumption();

		List<int[]> cover = getCover(newList);

		boolean coversHypercube = cover.size() >= 1 && cover.get(0).length == 0;
		
		boolean sat = !coversHypercube;
		try {
			sat = newList.getSolverForCNF().isSatisfiable();
		} catch(ContradictionException e) {
			sat = false;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
			if((coversHypercube && sat)
					|| (!coversHypercube && !sat)) {
				CNF test = new CNF(newList.getContext());
				test.addAll(cover);
				test = test.reduce();
				getCover(newList);
				getCover(newList);
				throw new UnsupportedOperationException();
			}
		

		if(coversHypercube) {
			for(int[] intersection : newList.getClauses()) {
				if(intersection.length == 0) continue;
				CNF nextTest = getIntersectedClauses(intersection,clauses,cnf.getContext());
				if(!nextTest.equals(clauses)) {
					if(isSat(nextTest.trySubsumption()) == true) {
						return true;
					}
				}
			}

			return false;
		}


		//If covered, need to check intersection of intersections of clauses
		//that is, check to make sure each sub-cube is filled

		return true;
	}

	private static CNF getIntersectedClauses(int[] intersection,
			List<int[]> clauses, VariableContext context) {

		CNF ret = new CNF(context);
		HashSet<Integer> lits = new HashSet<Integer>();

		for(int i : intersection) {
			lits.add(i);
		}

		for(int[] cl : clauses) {
			int[] newClause = getAgreement(lits,cl);

			if(newClause != null) {
				ret.addClause(newClause);
			}
		}

		return ret;

	}

	public static int[] getAgreement(HashSet<Integer> lits, int[] cl) {
		int num = 0;
		int[] clause = new int[cl.length];

		for(int k = 0; k < cl.length; k++) {
			int lit = cl[k];
			if(lits.contains(-lit)) return null;

			if(!lits.contains(lit)) {
				clause[num] = lit;
				num++;
			}
		}


		int[] ret = new int[num];
		for(int k = 0; k < num; k++) {
			ret[k] = clause[k];
		}

		return ret;
	}

	//Utilizies subsumable resolution to see if all areas of the hypercube are covered by non-models
	public static List<int[]> getCover(CNF newList) {
		List<int[]> clauses = newList.getClauses();
		

		while(clauses.size() > 0) {
			List<int[]> newClauses = getSubsumedResolution(clauses);
			
			if(newClauses == null) {
				break;
			} else {
				ClauseList cl = new ClauseList(newList.getContext());
				cl.fastAddAll(newClauses);

				clauses = newClauses;
			}
		}

		for(int[] i : clauses) {
			if(i.length == 0) {
				ArrayList<int[]> ret = new ArrayList<int[]>();
				ret.add(new int[0]);
				return ret;
			}
		}
		return clauses;
	}

	private static List<int[]> getSubsumedResolution(List<int[]> clauses) {
		List<int[]> newClauses = new ArrayList<int[]>();

		boolean[] isSubsumed = new boolean[clauses.size()];

		for(int k = 0; k < clauses.size(); k++) {
			if(isSubsumed[k]) continue;
			int[] clause1 = clauses.get(k);
			for(int i = k+1; i < clauses.size(); i++) {
				if(isSubsumed[i]) continue;
				if(isSubsumed[k]) break;

				int[] clause2 = clauses.get(i);
				int[] newClause = getSubResolvant(clause1,clause2);
				if(newClause != null) {

					if(newClause.length == 0) {
						newClauses.clear();
						newClauses.add(newClause);
						return newClauses;
					}


					newClauses.add(newClause);

					if(clause1.length >= clause2.length) {
						isSubsumed[k] = true;
					} 

					if(clause2.length >= clause1.length) {
						isSubsumed[i] = true;
					}
				}
			}
		}

		if(newClauses.size() == 0) {
			//No new resolvents
			return null;
		}

		for(int k = 0; k < clauses.size(); k++) {
			if(!isSubsumed[k]) newClauses.add(clauses.get(k));
		}


		return newClauses;
	}

	//Returns resolvant if one clause a subset of another clause, save for the sign on a single literal
	private static int[] getSubResolvant(int[] clause1, int[] clause2) {
		if(clause1.length == 0) {
			return clause1;
		}
		if(clause2.length == 0) {
			return clause2;
		}

		int[] clause = new int[Math.max(clause1.length, clause2.length) - 1];

		if(clause.length == 0) {
			if(clause1[0] == -clause2[0]) {
				return clause;
			} else {
				return null;
			}
		}

		int num = 0;
		int ignoreIndex = -1;
		boolean c1Bigger = clause1.length > clause2.length;
		int c1Index = 0;
		int c2Index = 0;

		while(c1Index < clause1.length || c2Index < clause2.length) {
			if(num >= clause.length && ignoreIndex == -1) {
				if(c1Index == clause1.length-1 && c2Index == clause2.length-1
						&& clause1[c1Index] == -clause2[c2Index]) {
					return clause;
				}
				return null;
			}

			if(c2Index == clause2.length || c1Index == clause1.length) {
				if(c2Index == clause2.length) {
					if(!c1Bigger) return null;
					clause[num] = clause1[c1Index];
					num++;
					c1Index++;
				} else if(c1Index == clause1.length) {
					if(c1Bigger) return null;
					clause[num] = clause2[c2Index];
					num++;
					c2Index++;
				}
				continue;
			}

			int c1 = clause1[c1Index];
			int c2 = clause2[c2Index];

			int absDiff = Math.abs(c1)-Math.abs(c2);

			if(absDiff == 0) {
				if(c1 == c2) {
					clause[num] = c1;
					num++;
				} else {
					if(ignoreIndex != -1) return null;
					ignoreIndex = num;
				}
				c1Index++;
				c2Index++;
			} else if(absDiff < 0) {
				if(!c1Bigger) return null;
				clause[num] = c1;
				num++;
				c1Index++;
			} else {
				if(c1Bigger) return null;
				clause[num] = c2;
				num++;
				c2Index++;
			}
		}

		for(int k = 0; k < clause.length; k++) {
			if(clause[k] == 0) throw new UnsupportedOperationException();
		}

		return clause;
	}

	public static int[] getIntersection(int[] clause1, int[] clause2) {
		int num = 0;
		int[] clause = new int[clause1.length + clause2.length];
		int c1Index = 0;
		int c2Index = 0;

		while(c1Index < clause1.length || c2Index < clause2.length) {
			int c1 = c1Index < clause1.length ? clause1[c1Index] : Integer.MAX_VALUE;
			int c2 = c2Index < clause2.length ? clause2[c2Index] : Integer.MAX_VALUE;

			int absDiff = Math.abs(c1)-Math.abs(c2);

			if(absDiff == 0) {
				if(c1 == c2) {
					clause[num] = c1;
					num++;
				}
				c1Index++;
				c2Index++;
			} else if(absDiff < 0 || c2Index == clause2.length) {
				clause[num] = c1;
				num++;
				c1Index++;
			} else {
				clause[num] = c2;
				num++;
				c2Index++;
			}
		}

		int[] ret = new int[num];
		for(int k = 0; k < num; k++) {
			ret[k] = clause[k];
		}
		
		if(ret.length == 0) return null;

		return ret;
	}

}
