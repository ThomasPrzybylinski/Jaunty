package formula;

import java.util.Set;
import java.util.SortedMap;

public class Not extends BoolFormula {

	private BoolFormula formula;
	
	public Not(BoolFormula bf) {
		formula = bf;
	}
	
	@Override
	public SortedMap<Literal, Integer> getFreq(boolean includeNegations) {
		return formula.getFreq(includeNegations);
	}

	@Override
	public Set<Variable> getVars() {
		return formula.getVars();
	}
	
	@Override
	public Set<Literal> getValues() {
		return formula.getValues();
	}

	@Override
	public BoolFormula reduce() {
		BoolFormula reduced = formula.reduce();
		if(reduced instanceof Literal) {
			return ((Literal)reduced).negate();
		} else if (reduced instanceof Constant) {
			return Constant.getConstantFor(!((Constant)reduced).isValue());
		} else {
			return new Not(reduced);
		}
	}
	
	@Override
	public BoolFormula reduce(int level) {
		return this.reduce();
	}
	
	public Not getCopy() {
		return new Not(formula.getCopy());
	}

	@Override
	public BoolFormula subst(Literal var, boolean b) {
		return new Not(formula.subst(var,b));
	}
	
	@Override
	public RITNode[] toRIT() {
		return null;
	}
	
	@Override
	public RITNode[] toRITImplicant() {
		return null;
	}

	@Override
	public String treeStringForLength() {
		return "~"+formula.treeStringForLength();
	}

	@Override
	public String toString() {
		return "~"+formula.toString();
	}

	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		return formula.toNNF(!distribNeg);
	}

	@Override
	public BoolFormula toDNF() {
		return formula.toNNF(true).toDNF();
	}

	@Override
	public int compareTo(BoolFormula o) {
		throw new UnsupportedOperationException();
	}

}
