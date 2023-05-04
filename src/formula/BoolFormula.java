package formula;
import java.util.Set;
import java.util.SortedMap;



public abstract class BoolFormula implements Comparable<BoolFormula> {
	private VariableContext curContext = VariableContext.defaultContext;
	
	
	public VariableContext getCurContext() {
		return curContext;
	}
	public void setCurContext(VariableContext curContext) {
		this.curContext = curContext;
		assert this.curContext != null;
	}

	public abstract BoolFormula subst(Literal l, boolean b);
	public abstract SortedMap<Literal,Integer> getFreq(boolean includeNegations);
	public abstract BoolFormula reduce();
	public abstract BoolFormula reduce(int level);
	
	//Gets all variable with unique names (non-negated)
	public abstract Set<Variable> getVars();
	
	//Gets all variables with unique names and values (gives negated and non-negated)
	public abstract Set<Literal> getValues();
	
	public abstract BoolFormula getCopy();
	public  BoolFormula toNNF() {
		return toNNF(false);
	
	}
	public BoolFormula toCNF() {
		BoolFormula from = (new Not(this)).toNNF();
		BoolFormula dnf = from.toDNF();
		return (new Not(dnf)).toNNF();
	}
	
	//Assumes BoolFormula NNF
	public abstract BoolFormula toDNF();
	protected abstract BoolFormula toNNF(boolean distribNeg);
	
	public abstract String treeStringForLength();
	public abstract RITNode[] toRIT();
	public abstract RITNode[] toRITImplicant();
}
