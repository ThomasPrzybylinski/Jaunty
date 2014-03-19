package formula.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import util.LitSorter;
import util.LitsSet;
import formula.Disjunctions;
import formula.VariableContext;

public class DNF extends ClauseList {
	public static final DNF contradiction = new DNF(VariableContext.defaultContext);
	public static final DNF tautology;

	static {
		tautology = new DNF(VariableContext.defaultContext);
		tautology.addClause();
	}

	public DNF(VariableContext context) {
		super(context);
	}

	//Precondition: The conjunctions must be in CNF form
	public DNF(Disjunctions dnf) {
		super(dnf);
	}


	public DNF subst(int lit, boolean b) {
		DNF ret = new DNF(this.context);

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

					if(!realVal) {
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

	public DNF reduce() {
		DNF ret = new DNF(this.context);
		LitsSet clausesSeen = new LitsSet(this.context.getNumVarsMade());
		SortedSet<Integer> vars = new TreeSet<Integer>();

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
						return tautology;
					}
					vars.add(toAddClause[0]);
				}
			}
		}

		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DNF) {
			return super.equals(obj);
		}
		return false;
	}
}
