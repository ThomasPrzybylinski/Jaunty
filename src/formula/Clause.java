package formula;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;


public abstract class Clause extends BoolFormula {
	
	protected List<BoolFormula> toOperate;
	
	public Clause() {
		toOperate = new ArrayList<BoolFormula>();
	}
	
	public Clause(BoolFormula... operands) {
		this();
		
		for(BoolFormula operand : operands) {
			operand.setCurContext(this.getCurContext());
			add(operand);
		}
	}
	
	public List<BoolFormula> getFormulas() {
		return toOperate;
	}
	
	public void add(BoolFormula b) {
		b.setCurContext(this.getCurContext());
		toOperate.add(b);
	}
	
	public abstract Clause trySubsumption();
	
	public BoolFormula reduce(int k) {
		if(k == 0) return this.reduce();
		if(k > 0) return this.reduceMore();
		return this;
	}
	
	public BoolFormula reduceMore() {
		Clause c = this.getNewInstance();
		ArrayList<BoolFormula> list = new ArrayList<BoolFormula>();
		list.addAll(toOperate);
		
		for(int k = 0; k < list.size(); k++) {
			for(int i = k+1; i < list.size(); i++) {
				if(!(list.get(k) instanceof Clause && list.get(i) instanceof Clause)) return this;
				Clause form1 = (Clause)list.get(k);
				Clause form2 = (Clause)list.get(i);
				
				if(form1.getClass() == form2.getClass()
						&& form1.toOperate.size() == form2.toOperate.size()) {
					Set<Literal> vars1 = form1.getValues();
					Set<Literal> vars2 = form2.getValues();
					
					vars1.removeAll(vars2);
					
					if(vars1.size() == 1) {
						Literal var = vars1.iterator().next().negate();
						if(vars2.contains(var)) {
							vars2.remove(var);
							Clause added = form1.getNewInstance();
							added.toOperate.addAll(vars2);
							
							list.remove(i);
							list.remove(k);
							list.add(added);
							k--;
							i-=2;
							break;
						}
					}
				}
			}
			
		}
		
		c.toOperate.addAll(list);
		return c.reduce();
	
	}
	
	public abstract Clause getNewInstance();
	
	@Override
	public BoolFormula subst(Literal var, boolean b) {
		Clause c = this.getNewInstance();
		
		for(BoolFormula form : toOperate) {
			c.add(form.subst(var,b));
		}
		return c;
	}

	

	@Override
	public Set<Variable> getVars() {
		ArrayList<Variable> varList = new ArrayList<Variable>();
		for(BoolFormula form : toOperate) {
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
		TreeSet<Variable> ret = new TreeSet<Variable>();
		ret.addAll(varList);

		return ret;
	}

	
	@Override
	public Set<Literal> getValues() {
		ArrayList<Literal> varList = new ArrayList<Literal>();
		for(BoolFormula form : toOperate) {
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
		TreeSet<Literal> ret = new TreeSet<Literal>();
		ret.addAll(varList);

		return ret;
	}
	

	@Override
	public SortedMap<Literal, Integer> getFreq(boolean includeNegations) {
		TreeMap<Literal,Integer> ret = new TreeMap<Literal,Integer>();
		for(BoolFormula form : toOperate) {
			SortedMap<Literal,Integer> subMap = form.getFreq(includeNegations);
			for(Literal v : subMap.keySet()) {
				if(ret.containsKey(v)) {
					ret.put(v,ret.get(v)+subMap.get(v));
				} else {
					ret.put(v,subMap.get(v));
				}
			}
		}
		return ret;
	}


	@Override
	public String treeStringForLength() {
		StringBuilder sb = new StringBuilder();
		for(BoolFormula form : toOperate) {
			sb.append(form.treeStringForLength());
		}
		return sb.toString();
	}
	
	public int getNumClauses() {
		return toOperate.size();
	}
	
	

	
	
}
