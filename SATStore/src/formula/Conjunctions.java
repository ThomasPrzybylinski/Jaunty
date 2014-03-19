package formula;
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


public class Conjunctions extends Clause {

	public int[][] thing = null;

	public Conjunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Conjunctions(BoolFormula... operands) {
		super(operands);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BoolFormula reduce() {
		boolean reduceAgain = false;
		Conjunctions conj = new Conjunctions();
		SortedSet<Literal> vars = new TreeSet<Literal>();

		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k).reduce();
			if(kth instanceof Literal) {
				Literal v = (Literal)kth;
				if(vars.contains(v.negate())) {
					return Constant.FALSE;
				} else if(!vars.contains(v)) {
					vars.add(v);
				}
			} else if(kth == Constant.FALSE) {
				return Constant.FALSE;
			} else if(kth instanceof Conjunctions) {
				reduceAgain = true; //May add new conjunction
				conj.toOperate.addAll(((Conjunctions) kth).toOperate);
			} else if(kth != Constant.TRUE) {
				conj.add(kth);
			}
		}

		conj.toOperate.addAll(vars);

		if(reduceAgain) {
			return conj.reduce();
		}

		if(conj.toOperate.size() == 0) {
			return Constant.TRUE;
		}
		if(conj.toOperate.size() > 1) {
			return conj;
		} else {
			return conj.toOperate.get(0);
		}
	}

	//recommend reducing first, in CNF or DNF
	public Conjunctions trySubsumption() {
		Conjunctions conj = new Conjunctions();

		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k);
			if(kth instanceof Literal) {
				Disjunctions d = new Disjunctions();
				d.toOperate.add(kth);
				kth = d;
			}
			if(kth instanceof Disjunctions) {
				Disjunctions curClause = (Disjunctions)kth;
				if(!isSubsumed(k, curClause)) {
					conj.add(curClause);
				}
			} else {
				conj.toOperate.add(kth);
			}
		}
		conj.setCurContext(this.getCurContext());
		return conj;
	}

	private boolean isSubsumed(int k, Disjunctions curClause) {
		for(int i = 0; i < toOperate.size(); i++) {
			if(k == i) continue;
			BoolFormula ith = toOperate.get(i);
			if(ith instanceof Disjunctions) {
				Disjunctions otherClause = (Disjunctions)ith;
				if(curClause.toOperate.size() > otherClause.toOperate.size()
						|| (
								curClause.toOperate.size() >= otherClause.toOperate.size()
								&& i < k
								)

						) {	
					HashSet<BoolFormula> curVars = new HashSet<BoolFormula>();
					curVars.addAll(otherClause.toOperate);
					for(BoolFormula hopeVar : curClause.toOperate) {
						if(curVars.contains(hopeVar)) {
							curVars.remove(hopeVar);
						}
					}
					if(curVars.size() == 0) {
						return true;
					}
				}
			} else if(ith instanceof Literal) {
				if(curClause.toOperate.contains(ith) && curClause.toOperate.contains(ith)) {
					return true;
				}
			}

		}
		return false;
	}

	public Conjunctions toNNF() {
		return (Conjunctions)super.toNNF();
	}

	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		Clause ret;
		if(distribNeg) {
			ret = new Disjunctions();
		} else {
			ret = new Conjunctions();
		}

		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k).toNNF(distribNeg);
			ret.add(kth);
		}

		return ret;
	}

	public Clause getNewInstance()  {
		return new Conjunctions();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(BoolFormula form : toOperate) {
			if(sb.length() > 1) {
				sb.append("&");
			}
			sb.append(form);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public RITNode[] toRIT() {
		ArrayList<RITNode> nodes = new ArrayList<RITNode>();
		for(BoolFormula form : toOperate) {
			RITNode[] nodules = form.toRIT();
			for(RITNode n : nodules) {
				nodes.add(n);
			}
		}
		return nodes.toArray(new RITNode[]{});
	}

	@Override
	public RITNode[] toRITImplicant() {
		RITNode child = null;
		RITNode cur = null;

		for(BoolFormula from : toOperate) {
			if(child == null) {
				RITNode[] nodes = from.toRITImplicant();
				if(nodes.length > 1) {
					throw new RuntimeException("To many returned!");
				}
				child = nodes[0];
				cur = child;
			} else {
				RITNode[] nodes = from.toRITImplicant();
				for(RITNode n : nodes) {
					cur.addNode(n);
				}
				if(nodes.length > 0) {
					cur = nodes[0];
				}
			}
		}

		return new RITNode[]{child};

	}

	public ISolver getSolverForCNF() throws ContradictionException {
		Set<Variable> setVars = getVars();
		ArrayList<Variable> vars = new ArrayList<Variable>(setVars.size());
		vars.addAll(setVars);
		Collections.sort(vars);
		setVars = null;

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(vars.size());

		thing = new int[toOperate.size()][];
		int ci = 0;
		for(BoolFormula form : toOperate) {
			Disjunctions clause;
			if(form instanceof Disjunctions) {
				clause = (Disjunctions)form;
			} else if (form instanceof Literal) {
				clause = new Disjunctions();
				clause.add(form);
			} else {
				return null;
			}
			int[] clauseForSolve = new int[clause.toOperate.size()];

			int clauseIndex = 0;
			thing[ci] = new int[clause.toOperate.size()];

			for(BoolFormula varFrom : clause.toOperate) {
				if(varFrom instanceof Literal) {
					Literal curVar = (Literal)varFrom;
					for(int k = 0; k < vars.size(); k++) {
						if(curVar.getVar().equals(vars.get(k))) {
							clauseForSolve[clauseIndex] = k+1;

							if(clauseForSolve[clauseIndex] == 0) {
								System.out.print("");
							}
							if(!curVar.isPos()) {
								clauseForSolve[clauseIndex] *= -1;
							}
							break;
						}
					}
				} else {
					return null;
				}
				thing[ci][clauseIndex] = clauseForSolve[clauseIndex];
				if(thing[ci][clauseIndex] == 0) {
					System.out.print("");
					getVars();
				}
				clauseIndex++;
			}

			ci++;
			//System.out.println(Arrays.toString(clauseForSolve));
			satSolve.addClause(new VecInt(clauseForSolve));
		}
		return satSolve;
	}

	public ISolver getSolverForCNFEnsureVariableUIDsMatch() throws ContradictionException {
		Set<Variable> setVars = getVars();
		ArrayList<Variable> vars = new ArrayList<Variable>(setVars.size());
		vars.addAll(setVars);
		Collections.sort(vars);
		int maxUID = vars.get(vars.size()-1).getUID();
		setVars = null;

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(maxUID);

		thing = new int[toOperate.size()][];
		int ci = 0;
		for(BoolFormula form : toOperate) {
			Disjunctions clause;
			if(form instanceof Disjunctions) {
				clause = (Disjunctions)form;
			} else if (form instanceof Literal) {
				clause = new Disjunctions();
				clause.add(form);
			} else {
				return null;
			}
			int[] clauseForSolve = new int[clause.toOperate.size()];

			int clauseIndex = 0;
			thing[ci] = new int[clause.toOperate.size()];

			for(BoolFormula varFrom : clause.toOperate) {
				if(varFrom instanceof Literal) {
					Literal curVar = (Literal)varFrom;
					clauseForSolve[clauseIndex] = curVar.getVar().getUID();

					if(clauseForSolve[clauseIndex] == 0) {
						System.out.print("");
					}
					if(!curVar.isPos()) {
						clauseForSolve[clauseIndex] *= -1;
					}

				} else {
					return null;
				}
				thing[ci][clauseIndex] = clauseForSolve[clauseIndex];
				if(thing[ci][clauseIndex] == 0) {
					System.out.print("");
					getVars();
				}
				clauseIndex++;
			}

			ci++;
			//System.out.println(Arrays.toString(clauseForSolve));
			satSolve.addClause(new VecInt(clauseForSolve));
		}
		return satSolve;
	}

	public BoolFormula unitPropagate() {
		return unitPropagate(new ArrayList<Literal>());
	}

	public BoolFormula unitPropagate(List<Literal> propped) {
		HashSet<Literal> trueParts = new HashSet<Literal>();
		HashSet<Literal> falses = new HashSet<Literal>();

		BoolFormula workingCopy = this.reduce();
		boolean workDone = true;
		while(workingCopy instanceof Conjunctions && workDone) {
			workDone = false;
			Conjunctions curCNF = (Conjunctions)workingCopy;

			for(BoolFormula form : curCNF.getFormulas()) {
				if(form instanceof Literal) {
					workDone = true;
					Literal toSub = (Literal)form;
					propped.add(toSub);
					workingCopy = workingCopy.subst(toSub,toSub.isPos());

					if(!toSub.isPos()) {
						falses.add(toSub.negate());
						if(trueParts.contains(toSub.negate())) {
							return Constant.FALSE;
						}

					} else {
						trueParts.add(toSub);
						if(falses.contains(toSub)) {
							return Constant.FALSE;
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

	public Conjunctions getCopy() {
		Conjunctions c= new Conjunctions();
		c.setCurContext(this.getCurContext());
		for(BoolFormula f : toOperate) {
			c.add(f.getCopy());
		}
		return c;
	}

	@Override
	public BoolFormula toDNF() {
		Conjunctions dnfClause = new Conjunctions();
		BoolFormula red = this.reduce(); //Make sure we have no Conjunctions in toOperate

		boolean foundDisjunctions = true;

		int times = 0;
		while(foundDisjunctions) {
			foundDisjunctions = false;
			times++;

			if(red instanceof Conjunctions) {
				Conjunctions workingConj = (Conjunctions)red;
				int k = 0;
				for(; k < workingConj.toOperate.size() -1; k++) {
					BoolFormula left = workingConj.toOperate.get(k);
					BoolFormula right = workingConj.toOperate.get(k+1);

					if(left instanceof Disjunctions || right instanceof Disjunctions) {
						Disjunctions d = (Disjunctions) (left instanceof Disjunctions ? left : right);
						BoolFormula other = (left instanceof Disjunctions ? right : left);
						Disjunctions distr =  d.distributeConj(other);

						dnfClause.add(distr.toDNF().reduce());
						k++; //skip both
						foundDisjunctions = true;
					} else {
						dnfClause.add(left);
					}
				}
				if(k == workingConj.toOperate.size()-1) {
					dnfClause.add(workingConj.toOperate.get(k));
				}
			} else {
				return red.toDNF();
			}
			red = dnfClause.reduce();
			dnfClause = new Conjunctions();
		}
		red = red.reduce(1);

		if(!(red instanceof Disjunctions)) {
			red = new Disjunctions(red).trySubsumption();
		}

		return red;
	}

	@Override
	public int compareTo(BoolFormula o) {
		if(o instanceof Conjunctions) {
			Conjunctions c2 = (Conjunctions)o;
			int num = this.getFormulas().size() - c2.getFormulas().size();

			if(num == 0) {
				for(int k = 0; k < this.getFormulas().size(); k++) {
					num = this.getFormulas().get(k).compareTo(c2.getFormulas().get(k));
					if(num != 0) {
						return num;
					}
				}
				return 0;
			} else {
				return num;
			}

		} else {
			return 0;
		}
	}



}
