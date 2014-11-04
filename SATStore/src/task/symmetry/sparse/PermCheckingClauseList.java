package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import util.PermutationUtil;
import util.lit.LitSorter;
import util.lit.LitsMap;
import formula.Conjunctions;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralPermutation;

/*
 *  Ideas to make it more scalable for sym finding:
 *  	Have a "current partition" array. The partial permute functions will only change that array (plus the permuted boolean? Or permutation to 0 means "not permuted")
 *  	We also have a watched literal in each clause (preferably the last element). When it gets permuted, we check the clause
 *  it contains to see if all have been permuted. If yes, we return the fully permuted cause to check up.
 * 
 */

//Mostly unmodifiable
public class PermCheckingClauseList extends ClauseList {
	
	public PermCheckingClauseList(Conjunctions cnf) {
		super(cnf);
		setupDataStructures();
	}

	public PermCheckingClauseList(VariableContext context) {
		super(context);
		setupDataStructures();
	}

	//WARNING: USES THE int[] OF CNF, SHOULD NOT BE MODIFIED
	public PermCheckingClauseList(ClauseList cnf) {
		this(cnf,false);
	}

	public PermCheckingClauseList(ClauseList cnf, boolean copy) {
		super(cnf.getContext());
		if(copy) {
			cnf = cnf.getCopy();
		}

		for(int[] clause : cnf.getClauses()) {
			clauses.add(clause.clone());
		}
		setupDataStructures();
	}

	private void setupDataStructures() {
		this.sort();
	}


	//Assumes ordered
	public boolean checkPerm(int[] perm) {
		int[] oldCl = null;
		int freq = -1;
		for(int[] cl : getClauses()) {
			
			if(oldCl == null || !Arrays.equals(cl,oldCl)) {
				if(oldCl != null && freq > 0) return false;
				oldCl = cl;
				int[] permCl = PermutationUtil.permuteClause(cl,perm);
				freq = getClauseFreq(permCl);
			}

			freq--;
			
			if(freq < 0) return false;
		}
		
		return true;
	}
	
	
	public int getClauseFreq(int[] cl) {
		List<int[]> clauses = getClauses();
		int index = Collections.binarySearch(clauses,cl,compare);
		
		if(index >= 0) {
			int num = 1;
			for(int k = -1; k + index >= 0; k--) {
				if(Arrays.equals(cl,clauses.get(k+index))) {
					num++;
				} else {
					break;
				}
			}
			
			for(int k = 1; k + index < clauses.size(); k++) {
				if(Arrays.equals(cl,clauses.get(k+index))) {
					num++;
				} else {
					break;
				}
			}
			
			return num;
			
		} else {
			return -1;
		}
		
	}

	@Override
	public void addClause(int... vars) {
		throw new UnsupportedOperationException("SemiPermutableCNFs are meant to be constant at the moment");
		//super.addClause(vars);
	}

	@Override
	public List<int[]> getClauses() {
		return Collections.unmodifiableList(super.getClauses());
	}


}