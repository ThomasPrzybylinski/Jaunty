package formula.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import util.lit.LitSorter;
import util.lit.LitsSet;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;


//TODO: Create superclass that is just a clause list that we can perform symmetries on
//Uses Var UIDs for simplicity and speed
public class CNF extends ClauseList {
	public static final CNF tautology = new CNF(VariableContext.defaultContext);
	public static final CNF contradiction;

	static {
		contradiction = new CNF(VariableContext.defaultContext);
		contradiction.addClause();
	}

	public CNF(VariableContext context) {
		super(context);
	}

	//Precondition: The conjunctions must be in CNF form
	public CNF(Conjunctions cnf) {
		super(cnf);
	}


	public CNF subst(int lit, boolean b) {
		CNF ret = new CNF(this.context);

		for(int[] clause : clauses) {
			boolean addClause = true;
			List<Integer> newTempClause = new ArrayList<Integer>();

			for(int k = 0; k < clause.length; k++) {
				int cLit = clause[k];

				if(Math.abs(cLit) == Math.abs(lit)) {
					boolean realVal;
					if(cLit == lit) {
						realVal = b;
					} else {
						realVal = !b;
					}

					if(realVal) {
						addClause = false;
					}
				} else {
					newTempClause.add(cLit);
				}
			}

			if(addClause) {
				int[] toAddClause = new int[newTempClause.size()];
				for(int j = 0; j < newTempClause.size(); j++) {
					toAddClause[j] = newTempClause.get(j);
				}
				LitSorter.inPlaceSort(toAddClause);
				ret.addClause(toAddClause);
			}
		}
		return ret;
	}

	public CNF reduce() {
		CNF ret = new CNF(this.context);
		SortedSet<Integer> vars = new TreeSet<Integer>();
		LitsSet clausesSeen = new LitsSet(this.context.size());
		
		for(int k = 0; k < clauses.size(); k++) {
			int[] kth = clauses.get(k);
			
			if(clausesSeen.contains(kth)) {
				continue;
			} else {
				clausesSeen.add(kth);
			}
			

			boolean keepClause = true;
			SortedSet<Integer> clauseVars = new TreeSet<Integer>();
			ArrayList<Integer> newClause = new ArrayList<Integer>();

			for(int i = 0; i < kth.length; i++) {
				int lit = kth[i];

				if(clauseVars.contains(-lit)) {
					keepClause = false;
					break;
				} else if(!clauseVars.contains(lit)) {
					clauseVars.add(lit);
					newClause.add(lit);
				}
			}

			if(keepClause) {
				int[] toAddClause = new int[newClause.size()];
				for(int j = 0; j < newClause.size(); j++) {
					toAddClause[j] = newClause.get(j);
				}
				LitSorter.inPlaceSort(toAddClause);
				ret.addClause(toAddClause);

				if(toAddClause.length == 1 || toAddClause.length == 0) {
					if(toAddClause.length == 0 || vars.contains(-toAddClause[0])) {
						return contradiction;
					}
					vars.add(toAddClause[0]);
				}
			}
		}

		return ret;
	}

	//recommend reducing first, in CNF or DNF
	public CNF trySubsumption() {
		CNF ret = new CNF(this.context);

		for(int k = 0; k < clauses.size(); k++) {
			int[] kth = clauses.get(k);
			if(!isSubsumed(k, kth)) {
				ret.addClause(kth);
			}
		}
		return ret;
	}

	private boolean isSubsumed(int k, int[] curClause) {
		for(int i = 0; i < clauses.size(); i++) {
			if(k == i) continue;
			int[] otherClause = clauses.get(i);
			if(curClause.length > otherClause.length
					|| (
							curClause.length >= otherClause.length
							&& i < k
							)

					) {	
				HashSet<Integer> curVars = new HashSet<Integer>();

				for(int var : otherClause) {
					curVars.add(var);
				}


				for(int hopeVar : curClause) {
					if(curVars.contains(hopeVar)) {
						curVars.remove(hopeVar);
					}
				}
				if(curVars.size() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public Set<Integer> getVars() {
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for(int[] clause : clauses) {
			for(int var : clause) {
				ret.add(Math.abs(var));
			}
		}
		return ret;
	}

	public Conjunctions toConjunction() {
		Conjunctions c = new Conjunctions();
		for(int[] clause : clauses) {
			Disjunctions d = new Disjunctions();

			for(int i : clause) {
				Variable var = context.getVar(Math.abs(i));
				d.add(i > 0 ? var.getPosLit() : var.getNegLit());
			}
			c.add(d);
		}
		return c;
	}

	public ISolver getSolverForCNF() throws ContradictionException {
		Set<Integer> setVars = getVars();

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(setVars.size());

		for(int[] clause : clauses) {
			int[] clauseForSolve = new int[clause.length];

			int clauseIndex = 0;

			for(int curVar : clause) {
				clauseForSolve[clauseIndex] = curVar;
				clauseIndex++;
			}
			//System.out.println(Arrays.toString(clauseForSolve));
			satSolve.addClause(new VecInt(clauseForSolve));
		}
		return satSolve;
	}

	public ISolver getSolverForCNFEnsureVariableUIDsMatch() throws ContradictionException {
		Set<Integer> setVars = getVars();
		ArrayList<Integer> vars = new ArrayList<Integer>(setVars.size());
		vars.addAll(setVars);
		Collections.sort(vars);
		int maxUID = vars.get(vars.size()-1);
		setVars = null;

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(maxUID);

		for(int[] clause : clauses) {
			int[] clauseForSolve = new int[clause.length];

			int clauseIndex = 0;

			for(int curVar : clause) {
				clauseForSolve[clauseIndex] = curVar;
				clauseIndex++;
			}
			//System.out.println(Arrays.toString(clauseForSolve));
			satSolve.addClause(new VecInt(clauseForSolve));
		}
		return satSolve;
	}

	public CNF unitPropagate() {
		return unitPropagate(new ArrayList<Integer>());
	}

	public CNF unitPropagate(List<Integer> propped) {
		HashSet<Integer> trueParts = new HashSet<Integer>();
		HashSet<Integer> falses = new HashSet<Integer>();

		CNF workingCopy = this.reduce();
		boolean workMore = true;
		while(workMore) {
			workMore = false;

			CNF curCNF = workingCopy;
			for(int[] clause : curCNF.clauses) {
				if(clause.length == 1) {
					workMore = true;
					int toSub = clause[0];
					propped.add(toSub);

					boolean toSubValue = toSub > 0;

					workingCopy = workingCopy.subst(toSub,toSubValue);

					if(!toSubValue) {
						int toSubVar = Math.abs(toSub);
						falses.add(toSubVar);
						if(trueParts.contains(toSubVar)) {
							return contradiction;
						}

					} else {
						trueParts.add(toSub);
						if(falses.contains(toSub)) {
							return contradiction;
						}
					}
				}
			}

			workingCopy = workingCopy.reduce();
		}

		//System.out.println(trueParts);
		//System.out.println(falses);

		return workingCopy;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CNF) {
			return super.equals(obj);
		}
		return false;
	}

	


}
