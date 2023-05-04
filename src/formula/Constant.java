package formula;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


public class Constant extends BoolFormula {

	private boolean value;
	
	public static final Constant TRUE = new Constant(true);
	public static final Constant FALSE = new Constant(false);
	
	private Constant(){}
	private Constant(boolean value) {
		this.value = value;
	}
	
	public static Constant getConstantFor(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	public boolean isValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	@Override
	public BoolFormula subst(Literal var, boolean b) {
		return this;
	}
	
	public BoolFormula subst(Variable var, Variable replacement) {
		return this;
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
		return new HashSet<Variable>();
	}
	
	public Set<Literal> getValues() {
		return new HashSet<Literal>();
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
	@Override
	public SortedMap<Literal,Integer> getFreq(boolean includeNegations) {
		return new TreeMap<Literal,Integer>();
	}
	@Override
	public String treeStringForLength() {
		return value ? "" : "";
	}
	
	@Override
	public RITNode[] toRIT() {
		return new RITNode[]{};
	}
	@Override
	public RITNode[] toRITImplicant() {
		return new RITNode[]{};
	}
	@Override
	public Constant getCopy() {
		return this;
	}
	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		if(distribNeg) {
			return Constant.getConstantFor(!this.value);
		} else {
			return this;
		}
	}
	@Override
	public BoolFormula toDNF() {
		return this.getCopy();
	}
	@Override
	public int compareTo(BoolFormula o) {
		if(this == Constant.TRUE) {
			if(o == Constant.TRUE) {
				return 0;
			} else {
				return 1;
			}
		} else { //this is FALSE
			if(o == Constant.FALSE) {
				return 0;
			} else {
				return -1;
			}
		}
	}
	

	
	

}
