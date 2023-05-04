package formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class Disjunctions extends Clause {

	public Disjunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Disjunctions(BoolFormula... operands) {
		super(operands);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BoolFormula reduce() {
		Disjunctions dis = new Disjunctions();
		SortedSet<Literal> vars = new TreeSet<Literal>();
		
		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k).reduce();
			if(kth instanceof Literal) {
				Literal v = (Literal)kth;
				 if(vars.contains(v.negate())) {
					 return Constant.TRUE;
				 } else if(!vars.contains(v)) {
					 vars.add(v);
				 }
			} else if(kth == Constant.TRUE) {
				return Constant.TRUE;
			} else if(kth instanceof Disjunctions) {
				dis.toOperate.addAll(((Disjunctions) kth).toOperate);
			} else if(kth != Constant.FALSE) {
				dis.add(kth);
			}
		}
		dis.toOperate.addAll(vars);
		
		
		if(dis.toOperate.size() == 0) {
			return Constant.FALSE;
		}
		if(dis.toOperate.size() > 1) {
			return dis;
		} else {
			return dis.toOperate.get(0);
		}
	}

	//recommend reducing first, in CNF or DNF
	public Disjunctions trySubsumption() {
		Disjunctions conj = new Disjunctions();
		
		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k);
			
			if(kth instanceof Literal) {
				Conjunctions c = new Conjunctions();
				c.toOperate.add(kth);
				kth = c;
			}
			
			if(kth instanceof Conjunctions) {
				Conjunctions curClause = (Conjunctions)kth;
				if(!isSubsumed(k, curClause)) {
					conj.add(curClause);
				}
			} else {
				conj.toOperate.add(kth);
			}
		}
		return conj;
	}

	private boolean isSubsumed(int k, Conjunctions curClause) {
		for(int i = 0; i < toOperate.size(); i++) {
			if(k == i) continue;
			
			BoolFormula ith = toOperate.get(i);
			
			
			if(ith instanceof Conjunctions) {
				Conjunctions otherClause = (Conjunctions)ith;
				if(curClause.toOperate.size() > otherClause.toOperate.size()
						|| (
								curClause.toOperate.size() == otherClause.toOperate.size()
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
				if(curClause.toOperate.size() == 1 && curClause.toOperate.contains(ith)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Clause getNewInstance()  {
		return new Disjunctions();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(BoolFormula form : toOperate) {
			if(sb.length() > 1) {
				sb.append("|");
			}
			sb.append(form);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public RITNode[] toRIT() {
		RITNode child = null;
		RITNode cur = null;
		
		for(BoolFormula from : toOperate) {
			if(child == null) {
				RITNode[] nodes = from.toRIT();
				if(nodes.length > 1) {
					throw new RuntimeException("To many returned!");
				}
				child = nodes[0];
				cur = child;
			} else {
				RITNode[] nodes = from.toRIT();
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
	
	public RITNode[] toRITImplicant() {
		ArrayList<RITNode> nodes = new ArrayList<RITNode>();
		for(BoolFormula form : toOperate) {
			RITNode[] nodules = form.toRITImplicant();
			for(RITNode n : nodules) {
				nodes.add(n);
			}
		}
		return nodes.toArray(new RITNode[]{});
	}
	
	public Disjunctions getCopy() {
		Disjunctions d= new Disjunctions();
		
		for(BoolFormula f : toOperate) {
			d.add(f.getCopy());
		}
		return d;
	}
	
	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		Clause ret;
		if(distribNeg) {
			ret = new Conjunctions();
		} else {
			ret = new Disjunctions();
		}
		
		for(int k = 0; k < toOperate.size(); k++) {
			BoolFormula kth = toOperate.get(k).toNNF(distribNeg);
			ret.add(kth);
		}
		
		return ret;
	}

	public Disjunctions distributeConj(BoolFormula other) {
		Disjunctions distrDis = new Disjunctions();
		
		if(other instanceof Disjunctions) {
			Disjunctions d = (Disjunctions)other;
			for(BoolFormula bf : toOperate) {
				for(BoolFormula bf2 : d.toOperate) {
					distrDis.add(new Conjunctions(bf,bf2));
				}
			}
		} else {
			for(BoolFormula form : toOperate) {
				Conjunctions c = new Conjunctions();
				c.add(form.getCopy());
				c.add(other.getCopy());
				distrDis.add(c);
			}
		}
		return distrDis;
	}

	@Override
	public BoolFormula toDNF() {
		Disjunctions dis = new Disjunctions();
		
		for(BoolFormula form : toOperate) {
			dis.add(form.toDNF());
		}
		BoolFormula ret = dis.trySubsumption().reduce();
		
		if(!(ret instanceof Disjunctions)) {
			Disjunctions d = new Disjunctions(ret);
			ret = d;
		}
		
		return ret;
		
	}
	
	@Override
	public int compareTo(BoolFormula o) {
		if(o instanceof Disjunctions) {
			Disjunctions c2 = (Disjunctions)o;
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
	
	/*public boolean subsumes(Disjunctions d) {
		if(toOperate.size() < d.toOperate.size()) {
			return false;
		}
		
		for(BoolFormula b : d.toOperate) {
			if(b instanceof Variable) {
				if(!toOperate.contains((Variable)b)) {
					return false;
				}
			} else {
				throw new UnsupportedOperationException();
			}
		}
		return true;
	}*/
	
}
