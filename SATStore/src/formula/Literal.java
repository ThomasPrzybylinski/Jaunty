package formula;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class Literal extends BoolFormula {
	private Variable variable;
	private boolean pos;
	
	Literal(Variable v, boolean assignment) {
		this.variable = v;
		this.pos = assignment;
	}
	
	public Variable getVar() {
		return variable;
	}

	public boolean isPos() {
		return pos;
	}

	public Literal negate() {
		return pos ? variable.getNegLit() : variable.getPosLit();
	}
	
	public int getIntRep() {
		return pos ? variable.getUID() : -variable.getUID();
	}
	
	@Override
	public int compareTo(BoolFormula o) {
		if(o instanceof Literal) {
			Literal l = (Literal)o;
			int comp = variable.compareTo(l.getVar());

			if(comp == 0) {
				if(this.pos && !l.pos) {
					comp = 1;
				} else  if(!this.pos && l.pos) {
					comp = -1;
				}
			}
			return comp;
		} else {
			return 0;
		}
	}

	@Override
	public BoolFormula subst(Literal l, boolean b) {
		if(this.variable.getUID().equals(l.variable.getUID())) {
			b = pos ? b : !b;
			return Constant.getConstantFor(b);
		} else {
			return this;
		}
	}

	@Override
	public SortedMap<Literal, Integer> getFreq(boolean includeNegations) {
		TreeMap<Literal,Integer> ret = new TreeMap<Literal,Integer>();
		if(includeNegations || pos) {
			ret.put(this,1);
		}
		return ret;
	}

	@Override
	public BoolFormula reduce() {
		return this;
	}

	@Override
	public BoolFormula reduce(int level) {
		return this.reduce();
	}

	@Override
	public Set<Variable> getVars() {
		TreeSet<Variable> ret = new TreeSet<Variable>();
		Variable toAdd = this.variable;
		ret.add(toAdd);

		return ret;
	}

	@Override
	public Set<Literal> getValues() {
		TreeSet<Literal> ret = new TreeSet<Literal>();
		Literal toAdd = this;
		ret.add(toAdd);

		return ret;
	}

	@Override
	public BoolFormula getCopy() {
		return this;
	}

	@Override
	public BoolFormula toDNF() {
		return this.getCopy();
	}

	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		if(distribNeg) {
			return this.negate();
		} else {
			return this.getCopy();
		}
	}

	@Override
	public String treeStringForLength() {
		return "V";
	}

	@Override
	public RITNode[] toRIT() {
		RITNode rit = new RITNode();
		rit.setLit(this);
		return  new RITNode[]{rit};
	}

	@Override
	public RITNode[] toRITImplicant() {
		RITNode rit = new RITNode();
		rit.setLit(this);
		return  new RITNode[]{rit};
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Literal) {
			Literal l = (Literal)arg0;
			return (getVar().equals(l.getVar())) && (this.pos == l.pos);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return pos ? variable.hashCode() : -variable.hashCode();
	}

	@Override
	public String toString() {
		String suffix = variable.getName();// +'['+(uid)+']';
		if(!pos) {
			return "-"+suffix;
		}
		return suffix;
	}

	public String niceString() {
		if(!pos) {
			return "Not"+variable.getName();
		}
		return variable.getName();
	}

}
