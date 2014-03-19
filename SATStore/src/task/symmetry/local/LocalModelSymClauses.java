package task.symmetry.local;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralPermutation;

/*
	This class is to help with local symmetry breaking. It may also be more efficient than the regular SemiPermutableClauseList
 */
/*
 *  Ideas to make it more scalable for sym finding:
 *  	Have a "current partition" array. The partial permute functions will only change that array (plus the permuted boolean? Or permutation to 0 means "not permuted")
 *  	We also have a watched literal in each clause (preferably the last element). When it gets permuted, we check the clause
 *  it contains to see if all have been permuted. If yes, we return the fully permuted cause to check up.
 * 
 */

//Unlike a normal ClauseList, our clauses ARE NOT guaranteed to be ordered after partial permutations for efficiency and simplicity reasons
//Also mostly unmodifiable
public class LocalModelSymClauses {

	//Lits are models, clauses represent lits
	private class Clause {
		int[] models;
		int index;

		public Clause(int[] lits, int index) {
			super();
			this.models = lits;
			this.index = index;
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

		//If this model means no more work can be done on this clause, add it and keep model in watched.
		//Otherwise, pick another watch variable
		protected void handleWatched(int toHandle) {
			Iterator<Integer> watchedIter = watchedClauses[toHandle].iterator();
			while(watchedIter.hasNext()) {
				int k = watchedIter.next();

				Clause curClause = dualClauses[k];
				boolean fullyPermuted = true;
				for(int i = 0; i < curClause.models.length; i++) {
					int var =  Math.abs(curClause.models[i]);
					if(validModels[var] && !permuted[var]) {
						fullyPermuted = false;
						watchedIter.remove();
						watchedClauses[var].add(k);
						break;
					}
				}

				if(fullyPermuted) {
					finishedClauses.add(k);
				}
			}

		}

		public List<Integer> getFinishedClauses() {
			return finishedClauses;
		}
	}

	private class PermuteAction extends Action {
		int from;
		int to;

		public PermuteAction(int level, int from, int to) {
			super(level);
			this.from = from;
			this.to = to;
		}

		@Override
		public String toString() {
			return from + "->" + to;
		}

		@Override
		public void apply() {
			finishedClauses = new LinkedList<Integer>();
			//To make lower calculations easier, we can assume from is positive
			if(from < 0) {
				from = Math.abs(from);
				to = -to;
			}

			if(permuted[from] == true) throw new IllegalStateException("Cannot permute twice on same variable");
			if(!validModels[from] || !validModels[Math.abs(to)]) {
				throw new IllegalStateException("Permuation Attempting to Permute Models that are not consistent with the current conditions!");
			}


			partialPermutation[from] = to;
			permuted[from] = true;

			handleWatched(from);
		}

		@Override
		public void undo() {
			finishedClauses = new LinkedList<Integer>();
			//To make lower calculations easier, we can assume from is positive
			if(from < 0) {
				from = Math.abs(from);
				to = -to;
			}

			partialPermutation[from] = from;
			permuted[from] = false;
		}


	}

	private class AddCondition extends Action {
		int lit;
		private Set<Integer> remModels;

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
			remModels = new TreeSet<Integer>();
			finishedClauses = new LinkedList<Integer>();


			litConditions.add(lit);

			Clause c = dualClauses[lit+vars.size()];
			HashSet<Integer> canStay = new HashSet<Integer>();
			for(int mod : c.models) {
				int modVar = Math.abs(mod);

				if(!validModels[modVar]) continue;

				if(permuted[modVar]) {
					throw new IllegalStateException("This condition would remove models that have already been permuted!");
				}
				canStay.add(modVar);
			}

			for(int k = 0; k < validModels.length; k++) {
				if(!canStay.contains(k) && validModels[k]) {
					validModels[k] = false;
					remModels.add(k);
					handleWatched(k);
				}
			}
		}



		@Override
		public void undo() {
			litConditions.remove(lit);
			for(int k : remModels) {
				validModels[k] = true;
			}
		}
	}

	//Not as strict as the SymmetryUtil.getInverseList method
	//Each clause represents a literal, each variable is a clause
	private Clause[] dualClauses;

	private Set<Integer> litConditions;

	private boolean[] validModels;

	//Given a variable, which clauses are existant and have that literal
	//as the current watched literal
	private Set<Integer>[] watchedClauses;

	//Post lit to permuted var
	private int[] partialPermutation;
	private boolean[] permuted;

	private VariableContext vars;
	private VariableContext dualVars;

	private Stack<Action> actions = new Stack<Action>();
	int curLevel = 0;


	public LocalModelSymClauses(ClauseList list) {
		this(list,false);
	}

	@SuppressWarnings("unchecked")
	public LocalModelSymClauses(ClauseList list, boolean copy) {
		vars = list.getContext();
		dualVars = new VariableContext();

		for(int k = 0; k < list.getClauses().size(); k++) {
			dualVars.createNextDefaultVar(); //var num is index+1
		}


		if(copy) {
			list = list.getCopy();
		}

		litConditions = new TreeSet<Integer>();

		partialPermutation = new int[dualVars.size()+1];
		permuted = new boolean[partialPermutation.length];
		validModels = new boolean[partialPermutation.length];
		Arrays.fill(validModels,true);

		for(int k = 0; k < partialPermutation.length; k++) {
			partialPermutation[k] = k;
		}

		LinkedList<Integer>[] tempDual = (LinkedList<Integer>[])new LinkedList[2*vars.size()+1];
		watchedClauses = (Set<Integer>[])new Set[dualVars.size()+1];

		//Setup watched clauses and tempDual
		for(int k = 0; k < tempDual.length; k++) {
			tempDual[k] = new LinkedList<Integer>();
		}

		for(int k = 0; k < dualVars.size()+1; k++) {
			watchedClauses[k] = new TreeSet<Integer>();
		}

		for(int k = 0; k < list.getClauses().size(); k++) {
			int[] clause = list.getClauses().get(k);
			for(int lit : clause) {
				tempDual[lit+vars.size()].add(k+1);
			}
		}

		dualClauses = new Clause[tempDual.length];

		for(int k = 0; k < tempDual.length; k++) {
			if(k == vars.size()) continue; //an empty clause

			List<Integer> tempClause = tempDual[k];
			int[] models = new int[tempClause.size()];

			int index = 0;
			for(int i : tempClause) {
				models[index] = i;
				index++;
			}

			dualClauses[k] = new Clause(models,k);

			if(models.length > 0) {
				watchedClauses[models[models.length-1]].add(k);
			}
		}

	}

	public List<Integer> partialPermute(int from, int to) {

		PermuteAction act = new PermuteAction(curLevel,from,to);
		act.apply();
		actions.push(act);
		return act.finishedClauses;
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
		while(!actions.empty() && actions.peek().getLevel() == curLevel) {
			actions.pop().undo();
		}

		curLevel = curLevel > 0 ? curLevel-1 : curLevel;
	}

	public int curValidModels() {
		int num = 0;
		for(boolean b : validModels) {
			if(b) num++;
		}

		return num;
	}

	public int numTotalModels() {
		return validModels.length;
	}

	public int curNumClauses() {
		int numClauses = 0;
		for(Clause c : dualClauses) {
			if(c != null) { //Not the meaningless clause
				for(int i : c.models) {
					if(validModels[Math.abs(i)]) {
						numClauses++;
						break;
					}
				}
			}
		}

		return numClauses;
	}

	public Set<Integer> curValidLits() {
		TreeSet<Integer> ret = new TreeSet<Integer>();
		int curLit = -vars.size();
		for(int k = 0; k < dualClauses.length; k++) {
			Clause c =  dualClauses[k];
			int prevLit = curLit;
			curLit++;
			//if(
					//litConditions.contains(prevLit) || 
					//litConditions.contains(-prevLit)) continue;
			if(c == null) continue;
			
			for(int i : c.models) {
				if(validModels[Math.abs(i)]) {
					ret.add(prevLit);
					break;
				}
			}
		}
		
		return ret;
	}


	public ClauseList getCurList() {
		return getCurList(false);
	}

	public ClauseList getCurList(boolean addVarLits) {
		ClauseList ret = new ClauseList(dualVars.getCopy());
		
		if(addVarLits) {
			ret.getContext().ensureSize(dualVars.size()+vars.size());
		}

		int curLit = -vars.size();
		for(Clause c : dualClauses) {
			int prevLit = curLit;
			curLit++;
			if(		litConditions.contains(prevLit) || 
					litConditions.contains(-prevLit)) continue;
			
			if(c == null) continue;



			int num = 0;

			for(int i : c.models) {
				if(validModels[Math.abs(i)]) {
					num++;
				}
			}

			if(num > 0) {
				if(addVarLits) {
					num++;
				}

				int[] cl = new int[num];

				int index = 0;
				for(int i : c.models) {
					if(validModels[Math.abs(i)]) {
						cl[index] = i;
						index++;
					}
				}

				if(addVarLits) {
					int trueLit = c.index-vars.size();
					int trueVar = Math.abs(trueLit);
					int addedVar = dualVars.size()+trueVar;
					cl[index] = (trueLit/trueVar)*addedVar;
				}

				ret.fastAddClause(cl);
			}
		}

		return ret;
	}
	
	//Given a literal of the original formula, return the literal
	//that reperesents that lit in getCurList(true)
	public int litToDualVar(int lit) {
		return (lit/Math.abs(lit))*(Math.abs(lit)+dualVars.size());
	}

	public LiteralPermutation getVarPart(LiteralPermutation perm) {
		if(perm.size() < dualVars.size()+vars.size()) {
			return null;
		}

		int[] newPerm = new int[vars.size()+1];

		for(int k = 1; k <= vars.size(); k++) {
			int var = k + dualVars.size();
			int lit = perm.imageOf(var);
			int trueVar = Math.abs(lit)-dualVars.size();

			newPerm[k] = (lit/Math.abs(lit))*trueVar; 
		}

		return new LiteralPermutation(newPerm);
	}

	public LiteralPermutation getModelPart(LiteralPermutation perm) {
		if(perm.size() < dualVars.size()+vars.size()) {
			return null;
		}

		int[] newPerm = new int[dualVars.size()+1];

		for(int k = 1; k <= dualVars.size(); k++) {
			int var = k;
			int lit = perm.imageOf(var);

			newPerm[k] = lit; 
		}

		return new LiteralPermutation(newPerm);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		StringBuilder temp = new StringBuilder();
		for(Clause c : dualClauses) {
			if(c == null) {
				//The empty clause
				sb.append(" [*] ");
				continue; 
			}
			temp.delete(0,temp.length());
			boolean valid = false;

			for(int i : c.models) {
				if(validModels[Math.abs(i)]) {
					valid = true;
					if(temp.length() > 0) {
						temp.append(' ');
					}
					temp.append(partialImageOf(i));

				}
			}

			if(valid) {
				sb.append('(').append(temp).append(')').append(' ');
			} else {
				sb.append("[_]" );
			}
		}

		return sb.toString();
	}

	protected int partialImageOf(int lit) {
		return lit > 0 ? partialPermutation[lit] : -partialPermutation[Math.abs(lit)];
	}
}
