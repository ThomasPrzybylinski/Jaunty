package formula.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import util.lit.LitSorter;
import util.lit.LitsSet;
import util.lit.MILEComparator;
import formula.BoolFormula;
import formula.Clause;
import formula.Literal;
import formula.VariableContext;

public class ClauseList {
	protected VariableContext context;
	protected ArrayList<int[]> clauses;
	
	public static final Comparator<int[]> COMPARE = new MILEComparator();
	
	public ClauseList(VariableContext context) {
		this.context = context;
		clauses = new ArrayList<int[]>();
	}

	//Precondition: The conjunctions must be in CNF form
	public ClauseList(Clause cnf) {
		this.context = cnf.getCurContext();
		clauses = new ArrayList<int[]>();

		for(BoolFormula form : cnf.getFormulas()) {
			Clause clause = (Clause)form;
			List<BoolFormula> varList = clause.getFormulas();

			int size = varList.size();
			int[] toAdd = new int[size];

			for(int k = 0; k < size; k++) {
				Literal l = (Literal)varList.get(k);
				toAdd[k] = l.getIntRep();
				context.ensureSize(Math.abs(toAdd[k]));
			}

			addClause(toAdd);
		}
	}
	
	public void addAll(List<int[]> clauses) {
		for(int[] i : clauses) {
			addClause(i);
		}
	}
	
	public void fastAddAll(List<int[]> clauses) {
		this.clauses.addAll(clauses);
		
		for(int[] vars : clauses) {
			if(vars.length > 0) {
				int maxVar = vars[vars.length-1];
				if(Math.abs(maxVar) > context.size()) {
					do {
						context.createNextDefaultVar();
					}while(Math.abs(maxVar) > context.size());
				}
			}
		}
	}
	
	//Adds clause without sorting
	public void fastAddClause(int... vars) {
		if(vars.length > 0) {
			int maxVar = Math.abs(vars[vars.length-1]);
			context.ensureSize(maxVar);
		}
		
		clauses.add(vars);
	}
	
	public void addClause(int ... vars) {
		for(int v : vars) {
			if(v == 0) throw new UnsupportedOperationException("Cannot add a variable 0");
		}
		LitSorter.inPlaceSort(vars);

		fastAddClause(vars);
		
		Collections.sort(clauses,COMPARE);
	}
	
	public void sort() {
		Collections.sort(clauses,COMPARE);
	}
	
	public int size() {
		return clauses.size();
	}
	
	public int getDeepSize() {
		int ret = 0;
		for(int[] i : clauses) {
			ret += i.length;
		}
		
		return ret;
	}

	
	public VariableContext getContext() {
		return context;
	}

	public void setContext(VariableContext context) {
		this.context = context;
	}

	//WARNING: MODIFICATIONS TO THIS ARRAY WILL EFFECT CNF
	//We allow this because CNF is meant to be low-level and relatively lightweight
	public List<int[]> getClauses() {
		return clauses;
	}

	public CNF getCopy() {
		CNF ret = new CNF(this.context);

		for(int[] clause : clauses) {
			ret.fastAddClause(clause.clone());
		}

		return ret;
	}
	
	//Only gets rid of duplicate entries
	public ClauseList reduce() {
		ClauseList ret = new ClauseList(this.context);
		LitsSet clausesSeen = new LitsSet(this.context.size());
		
		for(int k = 0; k < clauses.size(); k++) {
			int[] kth = clauses.get(k);
			
			if(clausesSeen.contains(kth)) {
				continue;
			} else {
				clausesSeen.add(kth);
				ret.fastAddClause(kth);
			}
		}

		return ret;
	}
	

	//Also swaps inverses
	//Index 0 is ignored. Start at 1 (1st variable UID)
	public CNF permute(int[] permutation) {
		CNF ret = this.getCopy();

		for(int[] clause : ret.clauses) {
			for(int k = 0; k < clause.length; k++) {
				int curVar = clause[k];
				int permIndex = Math.abs(curVar);
				int toReplace = permutation[permIndex];

				clause[k] = toReplace*(curVar > 0 ? 1 : -1);
			}
			LitSorter.inPlaceSort(clause);
		}

		Collections.sort(ret.clauses,COMPARE);

		return ret;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clauses == null) ? 0 : clauses.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ClauseList other = (ClauseList) obj;
		if (clauses == null) {
			if (other.clauses != null) {
				return false;
			}
		} else if (!internalListEquals(clauses, other.clauses)) {
			return false;
		}
		if (context == null) {
			if (other.context != null) {
				return false;
			}
		} else if (!context.equals(other.context)) {
			return false;
		}
		return true;
	}
	
	protected boolean internalListEquals(List<int[]> c1, List<int[]> c2) {
		if(c1.size() == c2.size()) {
			for(int k = 0; k < c1.size(); k++) {
				if(!Arrays.equals(c1.get(k),c2.get(k))) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	public String toNumString() {
		StringBuilder sb = new StringBuilder();

		for(int[] clause : clauses) {
			sb.append('(');

			boolean first = true;
			for(int i : clause) {
				if(first) {
					first = false;
				} else {
					sb.append(',');
				}

				sb.append(i);
			}

			sb.append(") ");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(int[] clause : clauses) {
			sb.append('(');

			boolean first = true;
			for(int i : clause) {
				if(first) {
					first = false;
				} else {
					sb.append(',');
				}

				if(i < 0) {
					sb.append('-');
				}

				sb.append(context.getVar(Math.abs(i)));
			}

			sb.append(") ");
		}
		return sb.toString();
	}
	
}
