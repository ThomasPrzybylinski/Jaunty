package formula;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class Implication extends BoolFormula {
	private BoolFormula left;
	private BoolFormula right;
	
	public Implication(BoolFormula left, BoolFormula right) {
		this.left = left;
		this.right = right;
	}
	
	
	@Override
	public BoolFormula getCopy() {
		return new Implication(left.getCopy(),right.getCopy());
	}

	@Override
	public SortedMap<Literal, Integer> getFreq(boolean includeNegations) {
		TreeMap<Literal,Integer> ret = new TreeMap<Literal,Integer>();
		getFreqs(includeNegations, ret, left);
		getFreqs(includeNegations, ret, right);

		return ret;
	}


	private void getFreqs(boolean includeNegations,
			TreeMap<Literal, Integer> ret, BoolFormula form) {
		SortedMap<Literal,Integer> subMap = form.getFreq(includeNegations);
		for(Literal v : subMap.keySet()) {
			if(ret.containsKey(v)) {
				ret.put(v,ret.get(v)+subMap.get(v));
			} else {
				ret.put(v,subMap.get(v));
			}
		}
	}

	@Override
	public Set<Literal> getValues() {
		ArrayList<Literal> varList = new ArrayList<Literal>();
		getValuesFrom(varList, left);
		getValuesFrom(varList, right);

		TreeSet<Literal> ret = new TreeSet<Literal>();
		ret.addAll(varList);

		return ret;

	}


	private void getValuesFrom(ArrayList<Literal> varList, BoolFormula form) {
		Set<Literal> curVars = form.getValues();
		int curLen = varList.size();
		for(Literal var : curVars) {
			boolean present = false;
			for(int k = 0; k < curLen; k++) {
				if(varList.get(k).getVar().equals(var.getVar())){
					present = true;
					break;
				}
			}
			
			if(!present) {
				varList.add(var);
			}
		}
	}


	@Override
	public Set<Variable> getVars() {
		ArrayList<Variable> varList = new ArrayList<Variable>();
		addVarsFrom(varList, left);
		addVarsFrom(varList, right);
		
		TreeSet<Variable> ret = new TreeSet<Variable>();
		ret.addAll(varList);

		return ret;
	}
	
	private void addVarsFrom(ArrayList<Variable> varList, BoolFormula form) {
		Set<Variable> curVars = form.getVars();
		int curLen = varList.size();
		for(Variable var : curVars) {
			boolean present = false;
			for(int k = 0; k < curLen; k++) {
				if(varList.get(k).getName().equals(var.getName())){
					present = true;
					break;
				}
			}
			
			if(!present) {
				varList.add(var);
			}
		}
	}

	

	@Override
	public BoolFormula reduce() {
		Disjunctions newFrom = toDisjunction();
		return newFrom;
	}
	
	@Override
	public BoolFormula reduce(int level) {
		return this.reduce();
	}


	private Disjunctions toDisjunction() {
		Disjunctions newFrom = new Disjunctions();
		newFrom.add((new Not(left)).reduce());
		newFrom.add(right.reduce());
		return newFrom;
	}

	@Override
	public BoolFormula subst(Literal var, boolean b) {
		return new Implication(left.subst(var,b),right.subst(var,b));
	}
	
	@Override
	protected BoolFormula toNNF(boolean distribNeg) {
		return this.toDisjunction().toNNF(distribNeg);
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
		return null;
	}
	
	public String toString() {
		return left + " => " + right;
	}


	@Override
	public BoolFormula toDNF() {
		return this.toNNF().toDNF();
	}


	@Override
	public int compareTo(BoolFormula o) {
		throw new UnsupportedOperationException();
	}

}
