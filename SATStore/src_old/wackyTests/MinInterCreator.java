package wackyTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import subsumptionMain.SATSump;
import task.formula.random.CNFCreator;
import task.formula.random.SimpleCNFCreator;
import task.sat.SATUtil;
import util.ModelComparator;
import formula.VariableContext;
import formula.simple.CNF;

public class MinInterCreator {

	private static class InitialHolder {
		public int[] initial;

		public InitialHolder(int[] initial) {
			super();
			this.initial = initial;
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CNF cnf;
		CNFCreator creat = new SimpleCNFCreator(14,4.3,3);
		cnf = creat.generateCNF(VariableContext.defaultContext);

		cnf = getSubsumedResolution(cnf.reduce().trySubsumption()).reduce().trySubsumption();
		System.out.println(cnf);
		System.out.println();
		CNF total = new CNF(cnf.getContext());
		for(int k = 0; k < cnf.getClauses().size(); k++) {
			//total.addClause(cnf.getClauses().get(0));
			//if(k != 0) break;
			int[] c1 = cnf.getClauses().get(k);
			for(int i = k+1; i < cnf.getClauses().size(); i++) {
				int[] c2 = cnf.getClauses().get(i);
				int[] intersection = getIntersection(c1,c2);
				if(intersection != null) {
					InitialHolder holder = new InitialHolder(cnf.getClauses().get(k));
					
					CNF out = getIntersectedClauses(k,intersection,cnf.getClauses(),cnf.getContext(),holder).reduce();
					
					//out = getCloseIntersection(out,holder).reduce();

					if(!out.equals(CNF.contradiction)) {
						//System.out.println(out);
						total.addAll(out.getClauses());
					}
				}
			}
		}
		
		total = getSubsumedResolution(total.reduce().trySubsumption()).reduce().trySubsumption();
		System.out.println(total);
		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models, new ModelComparator());
		for(int[] m : models) {
			System.out.println(Arrays.toString(m));
		}
		System.out.println();
		
		models = SATUtil.getAllModels(total);
		Collections.sort(models, new ModelComparator());

		
		for(int[] m : models) {
			System.out.println(Arrays.toString(m));
		}
		
		CNF out = new CNF(SATSump.getSubsumedConj(cnf.toConjunction()));
		System.out.println(out);
		
		out = new CNF(SATSump.getSubsumedConj(total.toConjunction()));
		System.out.println(out);

	}

	
	private static CNF getCloseIntersection(CNF out, InitialHolder holder) {
		CNF ret = new CNF(out.getContext());
		getCloseIntersection(out,holder,ret, 0);
		return ret;
	}


	private static void getCloseIntersection(CNF out, InitialHolder holder,
			CNF ret, int times) {
//		CNF tmp = new CNF(out.getContext());
//		List<int[]> tempList = getSubsumedResolution(out.getClauses());
//		if(tempList == null) {
//			ret.addAll(out.getClauses());
//			return;
//		}
//		tmp.addAll(tempList);
//		out = tmp;
		
		
		if(out.getClauses().size() <= 2 || times > 10) {
			ret.addAll(out.getClauses());
		} else {
			for(int k = 0; k < out.getClauses().size(); k++) {
				int[] clause = out.getClauses().get(k);
				if(Arrays.equals(holder.initial,clause)) continue;
				
				int[] intersection = getIntersection(clause,holder.initial);
				
				if(intersection != null) {
					InitialHolder holder2 = new InitialHolder(holder.initial);
					
					CNF out2 = getIntersectedClauses(k,intersection,out.getClauses(),out.getContext(),holder).reduce();
					
					getCloseIntersection(out2,holder2,ret,times+1);
				}
			}
		}
	}


	private static int getInitialIndex(CNF out, InitialHolder holder) {
		for(int k = 0; k < out.getClauses().size(); k++) {
			if(out.getClauses().get(k).equals(holder.initial)) {
				return k;
			}
		}
		return -1;
	}


	private static CNF getIntersectedClauses(int initial, int[] intersection,
			List<int[]> clauses, VariableContext context, InitialHolder holder) {

		CNF ret = new CNF(context);
		HashSet<Integer> lits = new HashSet<Integer>();

		for(int i : intersection) {
			lits.add(i);
		}

		for(int k = 0; k < clauses.size(); k++) {
			int[] cl = clauses.get(k);
			int[] newClause = getAgreement(lits,cl);

			if(newClause != null) {
				ret.addClause(newClause);
				if(k == initial) {
					holder.initial = newClause;
				}
			}
		}

		return ret;

	}

	public static int[] getAgreement(HashSet<Integer> lits, int[] cl) {
		int num = 0;
		int[] clause = new int[cl.length + lits.size()];

		for(int k = 0; k < cl.length; k++) {
			int lit = cl[k];
			if(lits.contains(-lit)) return null;

			if(!lits.contains(lit)) {
				clause[num] = lit;
				num++;
			}
		}

		for(int lit : lits) {
			clause[num] = lit;
			num++;
		}
		


		int[] ret = new int[num];
		for(int k = 0; k < num; k++) {
			ret[k] = clause[k];
		}
		Arrays.sort(ret);

		return ret;
	}

	
	private static CNF getSubsumedResolution(CNF cnf) {
		List<int[]> clauses = cnf.getClauses();
		List<int[]> newClauses = new ArrayList<int[]>();
		CNF ret = new CNF(cnf.getContext());

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
						ret.addClause(newClause);
						return ret;
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

		ret.addAll(newClauses);
		return ret;
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
