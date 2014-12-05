package formula.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import util.formula.FormulaForAgreement;
import util.lit.IntToIntLinkedHashMap;
import util.lit.LitSorter;
import util.lit.LitUtil;
import util.lit.LitsMap;
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
					if(cLit == lit) {
						addClause = false;
						break;
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
				ret.fastAddClause(toAddClause);
			}
		}
		ret.sort();
		return ret;
	}

	public CNF substAll(int[] agree) {
		LitSorter.inPlaceSort(agree);
		CNF ret = new CNF(this.context);

		LinkedList<int[]> toAdd = new LinkedList<int[]>();

		int[] newTempClause = new int[context.size()];
		for(int[] clause : clauses) {
			Arrays.fill(newTempClause,0);
			int tempClInd = 0;
			boolean addClause = true;

			int agInd = 0;
			int agVar = Math.abs(agree[0]);
			for(int k = 0; k < clause.length; k++) {
				int cLit = clause[k];
				int cVar = Math.abs(cLit);

				while(agInd < agree.length-1 && cVar > agVar) {
					agInd++;
					agVar = Math.abs(agree[agInd]);
				}

				if(cLit == agree[agInd]) {
					addClause = false;
					break;
				} else if(cLit != -agree[agInd]) {
					newTempClause[ tempClInd] = cLit;
					tempClInd++;
				} 
			}

			if(addClause) {
				int[] toAddClause = new int[tempClInd];
				for(int j = 0; j < tempClInd; j++) {
					toAddClause[j] = newTempClause[j];
				}
				LitSorter.inPlaceSort(toAddClause);
				toAdd.add(toAddClause);
			}
		}
		ret.fastAddAll(toAdd);
		ret.sort();
		return ret;

	}

	public CNF reduce() {
		CNF ret = new CNF(this.context);
		LitsMap clausesSeen = new LitsMap(this.context.size());

		for(int k = 0; k < clauses.size(); k++) {
			int[] kth = clauses.get(k);

			//Do trySubsumption instead
			if(clausesSeen.contains(kth)) {
				continue;
			} else {
				clausesSeen.put(kth,null);
			}


			boolean keepClause = true;
			IntToIntLinkedHashMap clauseVars = new IntToIntLinkedHashMap();
			ArrayList<Integer> newClause = new ArrayList<Integer>();

			for(int i = 0; i < kth.length; i++) {
				int lit = kth[i];

				if(clauseVars.contains(-lit)) {
					keepClause = false;
					break;
				} else if(!clauseVars.contains(lit)) {
					clauseVars.put(lit,1);
					newClause.add(lit);
				}
			}

			IntToIntLinkedHashMap vars = new IntToIntLinkedHashMap();
			if(keepClause) {
				int[] toAddClause = new int[newClause.size()];
				for(int j = 0; j < newClause.size(); j++) {
					toAddClause[j] = newClause.get(j);
				}
				LitSorter.inPlaceSort(toAddClause);
				ret.fastAddClause(toAddClause);

				if(toAddClause.length == 1 || toAddClause.length == 0) {
					if(toAddClause.length == 0 || vars.contains(-toAddClause[0])) {
						return contradiction;
					}
					vars.put(toAddClause[0],1);
				}
			}
		}
		ret.sort();;
		return ret;
	}

	public CNF squeezed() {
		VariableContext context = new VariableContext();
		TreeSet<Integer> foundVars = new TreeSet<Integer>();
		for(int[] c : clauses) {
			for(int i : c) {
				foundVars.add(Math.abs(i));
			}
		}
		int[] trans = new int[this.context.size()+1];
		int curVar = 1;
		for(int i : foundVars) {
			trans[i] = curVar;
			curVar++;
		}

		CNF ret = new CNF(context);
		for(int[] c : clauses) {
			int[] toAdd = new int[c.length];
			for(int k = 0; k < c.length; k++) {
				toAdd[k] = trans[Math.abs(c[k])]*(Math.abs(c[k])/c[k]);
			}
			ret.fastAddClause(toAdd);
		}

		return ret;
	}

	public CNF trySubsumption() {

		if(this.getClauses().size() > 500) {
			//do advanced
			FormulaForAgreement toSum = new FormulaForAgreement(this);
			return toSum.doSubsumption();

		} else {
			CNF ret = new CNF(this.context);

			for(int k = 0; k < clauses.size(); k++) {
				int[] kth = clauses.get(k);
				if(!isSubsumed(k, kth)) {
					ret.fastAddClause(kth);
				}
			}
			return ret;
		}
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

				int otherInd = 0;
				for(int hopeLit : curClause) {
					if(hopeLit == otherClause[otherInd]) {
						otherInd++;
						if(otherInd == otherClause.length) {
							return true;	
						}
					}
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
		return getSolverForCNF(false);
	}

	public ISolver getSolverForCNF(boolean useContextVars) throws ContradictionException {
		int numVars = -1;

		if(useContextVars) {
			numVars = getContext().size();
		} else {
			Set<Integer> setVars = getVars();
			numVars = setVars.size();
		}

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(numVars);

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

		CNF workingCopy = this.getCopy();//this.reduce();
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

			//			workingCopy = workingCopy.trySubsumption();
		}

		//System.out.println(trueParts);
		//System.out.println(falses);

		return workingCopy;//.trySubsumption();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CNF) {
			return super.equals(obj);
		}
		return false;
	}







}
