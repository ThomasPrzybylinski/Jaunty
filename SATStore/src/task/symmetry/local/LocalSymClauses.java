package task.symmetry.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import util.PermutationUtil;
import util.lit.LitsMap;
import util.lit.SetLitCompare;
import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;

//This class is to help with local symmetry breaking.



public class LocalSymClauses {
	boolean modelMode = true;

	private class Clause {
		int[] lits;
		int index;

		public Clause(int[] lits, int index) {
			super();
			this.lits = lits;
			this.index = index;
		}

		public String toString() {
			return index + ":" + Arrays.toString(lits);
		}
	}

	private abstract class Action {
		private int level;
		protected List<Integer> finishedClauses;

		public Action(int level) {
			this.level = level;
		}

		private int getLevel() {
			return level;
		}
		public abstract void apply();
		public abstract void undo();

	}

	private class AddCondition extends Action {
		int lit;
		private List<Integer> remClauses;

		public AddCondition(int level, int reqLit) {
			super(level);
			this.lit = reqLit;
		}

		@Override
		public String toString() {
			return "Req: " + lit;
		}

		@Override
		public void apply() {
			validLits = null;
			remClauses = new LinkedList<Integer>();
			finishedClauses = new LinkedList<Integer>();

			litConditions.add(lit);

			for(Clause c : clauses) {
				if(!validClauses[c.index]) continue;
				boolean litFound = false;
				for(int i : c.lits) {
					if(lit == -i) {
						break;
					} else if(lit == i) {
						litFound = true;
					}
				}

				if(!litFound) {
					validClauses[c.index] = false;
					remClauses.add(c.index);
				}
			}
		}

		@Override
		public void undo() {
			litConditions.remove(lit);
			validLits = null;
			for(int k : remClauses) {
				validClauses[k] = true;
			}
		}
	}

	private Clause[] clauses;

	private Set<Integer> litConditions;

	private Set<Integer> validLits;

	private boolean[] validClauses;

	private VariableContext vars;

	private Deque<Action> actions = new LinkedList<Action>();
	int curLevel = 0;


	public LocalSymClauses(ClauseList list) {
		this(list,false);
	}

	public LocalSymClauses(ClauseList list, boolean copy) {
		vars = list.getContext();

		if(copy) {
			list = list.getCopy();
		}

		litConditions = new TreeSet<Integer>();

		validClauses = new boolean[list.getClauses().size()];
		Arrays.fill(validClauses,true);


		clauses = new Clause[list.getClauses().size()];

		for(int k = 0; k < clauses.length; k++) {
			clauses[k] = new Clause(list.getClauses().get(k),k);
		}

	}

	public List<Integer> addCondition(int lit) {
		AddCondition act = new AddCondition(curLevel,lit);
		act.apply();
		actions.push(act);
		return act.finishedClauses;
	}

	public void post() {
		curLevel++;
	}

	public void pop() {
		while(!actions.isEmpty() && actions.peek().getLevel() == curLevel) {
			actions.pop().undo();
		}

		curLevel = curLevel > 0 ? curLevel-1 : curLevel;
	}

	public void setFilter(int[] filter) {
		while(!actions.isEmpty()) {
			pop();
		}

		this.post();
		for(int i : filter) {
			addCondition(i);
		}
	}

	public boolean isModelMode() {
		return modelMode;
	}

	public void setModelMode(boolean modelMode) {
		this.modelMode = modelMode;
	}

	public int curValidModels() {
		int num = 0;
		for(boolean b : validClauses) {
			if(b) num++;
		}

		return num;
	}

	public int numTotalModels() {
		return validClauses.length;
	}

	//Useful filter literals
	public Set<Integer> curUsefulLits() {
		Set<Integer> valid = curValidLits();
		TreeSet<Integer> ret = new TreeSet<Integer>(new SetLitCompare());

		for(int i : valid) {
			if(valid.contains(-i)) {
				ret.add(i);
			}
		}

		return ret;
	}

	public Set<Integer> curValidLits() {
		if(validLits == null) {
			validLits = new HashSet<Integer>();//new SetLitCompare());
			for(int k = 0; k < clauses.length; k++) {
				//		for(Clause c : clauses) {
				//			boolean valid = true;
				//			for(int i : c.lits) {
				//				if(litConditions.contains(-i)) {
				//					valid = false;
				//					break;
				//				}
				//			}

				//			if(valid) {
				if(validClauses[k]) {
					for(int i : clauses[k].lits) {
						validLits.add(i);
					}
				}
			}
		}
		return validLits;
	}


	public ClauseList getCurList() {
		return getCurList(false);
	}

	//keepSingleValVars==true means that we keep vars that only have a single literal value 
	public ClauseList getCurList(boolean keepSingleValVars) {
		ClauseList ret = new ClauseList(vars);
		Set<Integer> validLits = curValidLits();


		for(Clause c : clauses) {
			int[] cl = getCurClause(keepSingleValVars, validLits, c);
			if(cl != null) {
				ret.fastAddClause(cl);
			}
		}

		return ret;
	}

	private int[] getCurClause(boolean keepSingleValVars,
			Set<Integer> validLits, Clause c) {

		if(!validClauses[c.index]) return null;


		LinkedList<Integer> tempCl = new LinkedList<Integer>();
		for(int i : c.lits) {
			if(!validLits.contains(i)) {
				return null;
			}

			if(!litConditions.contains(i) && (keepSingleValVars || validLits.contains(-i))) {
				tempCl.add(i);
			}
		}

//		There are times when a clause may be empty		
//		if(tempCl.size() > 0) {
			int[] cl = new int[tempCl.size()];

			for(int k = 0; k < cl.length; k++) {
				cl[k] = tempCl.poll();
			}
			return cl;
//		}	

//		return null;
	}

	public LiteralGroup getModelGroup(LiteralGroup varGroup) {
		LinkedList<LiteralPermutation> newPerms = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : varGroup.getGenerators()) {
			newPerms.add(getModelPart(perm));
		}

		return new NaiveLiteralGroup(newPerms);
	}

	//perm is a permutation of the variables
	public LiteralPermutation getModelPart(LiteralPermutation perm) {
		LitsMap<Integer> clausesToIndex = new LitsMap<Integer> (vars.size());
		Set<Integer> validLits = curValidLits();

		for(int k = 0; k < clauses.length; k++) {
			Clause c = clauses[k];
			int[] newClause = getCurClause(true,validLits,c);

			if(newClause != null) {
				clausesToIndex.put(newClause,k);
			}
		}

		int[] newPerm = new int[clauses.length+1];
		for(int k = 0; k < clauses.length; k++) {
			Clause c = clauses[k];
			int[] newClause = getCurClause(true,validLits,c);

			if(newClause != null) {
				int[] permedClause = PermutationUtil.permuteClause(newClause,perm.asArray());
				int index = clausesToIndex.get(permedClause);
				newPerm[k+1] = index+1;
			} else {
				newPerm[k+1] = k+1;
			}
		}

		return new LiteralPermutation(newPerm);
	}

	//perm is a permutation of the variables
	public LiteralPermutation getModelPartSpecial(LiteralPermutation perm) {
		LitsMap<Integer> clausesToIndex = new LitsMap<Integer> (vars.size());
		TreeSet<Integer> toFind = new TreeSet<Integer>();
		Set<Integer> validLits = curValidLits();

		for(int k = 0; k < clauses.length; k++) {
			Clause c = clauses[k];
			clausesToIndex.put(c.lits,k);

			int[] newClause = getCurClause(true,validLits,c);

			if(newClause != null) {
				toFind.add(k);
			}
		}

		int[] newPerm = new int[clauses.length+1];
		ArrayList<Integer> curOrbit = new ArrayList<Integer>();
		for(int k = 0; k < clauses.length; k++) {
			if(toFind.contains(k)) {
				curOrbit.clear();
				curOrbit.add(k);
				Clause c = clauses[k];
				int[] permedClause = c.lits;
				boolean ok = true;
				int prevIndex = k;
				while(true) {
					permedClause = PermutationUtil.permuteClause(permedClause,perm.asArray());
					Integer index = clausesToIndex.get(permedClause);
					if(index == null) {
						ok = false;
						break;
					} else {
						toFind.remove(index);
						newPerm[prevIndex+1] = index+1;
						prevIndex = index;

						if(index == k) {
							break;
						}

						curOrbit.add(index);
					}
				}

				if(!ok) {
					for(int i : curOrbit) {
						newPerm[i+1] = i+1;
					}
				}
			} else if(newPerm[k+1] == 0) {
				//If not yet assigned
				newPerm[k+1] = k+1;
			}
		}

		return new LiteralPermutation(newPerm);
	}

	public VariableContext getContext() {
		return vars;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		StringBuilder temp = new StringBuilder();
		int index = 0;
		for(Clause c : clauses) {
			temp.delete(0,temp.length());
			boolean valid = true;
			if(validClauses[index]) {
				for(int i : c.lits) {
					if(temp.length() > 0) {
						temp.append(' ');
					}
					temp.append(i);
				}
			}
			sb.append(index+1).append(':');
			if(valid) {
				sb.append('(').append(temp).append(')').append(' ');
			} else {
				sb.append("[_] " );
			}

			index++;
		}

		return sb.toString();
	}

	public int[] getCanonicalInter(int[] filter) {
		TreeSet<Integer> units = new TreeSet<Integer>(new SetLitCompare());

		if(modelMode) {
			//This only works if all the clauses are models
			Set<Integer> valid = curValidLits();
			for(int i : valid) {
				if(!valid.contains(-i)) {
					units.add(i);
				}
			}
		} else {

			boolean first = true;
			for(int k = 0; k < clauses.length; k++) {
				if(validClauses[k]) {
					if(first) {
						for(int i : clauses[k].lits) {
							units.add(i);
						}
						first = false;
					} else {
						TreeSet<Integer> otherUnits = new TreeSet<Integer>(new SetLitCompare());

						for(int i : clauses[k].lits) {
							otherUnits.add(i);
						}

						Iterator<Integer>  iter  = units.iterator();

						while(iter.hasNext()) {
							Integer i = iter.next();
							if(!otherUnits.contains(i)) {
								iter.remove();
							}
						}

					}
				}
			}
		}

		for(int i : filter) {
			units.add(i);
		}

		int[] ret = new int[units.size()];

		int index = 0;
		for(int i : units) {
			ret[index] = i;
			index++;
		}

		return ret;
	}
}
