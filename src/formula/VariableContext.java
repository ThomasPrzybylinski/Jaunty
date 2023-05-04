package formula;

import java.util.ArrayList;
import java.util.HashMap;


/*
 * Variable contexts allow for different formulas to use different sets of Variables.
 * 
 * If Variables from different context are used together, Variables with equal UIDs are considered equal
 * 
 */
public class VariableContext {
	private HashMap<String,Variable> vars = new HashMap<String,Variable>();
	protected  HashMap<String,Variable> varsForDebug = vars;
	private ArrayList<Variable> varsByNum = new ArrayList<Variable>();
	
	public static final VariableContext defaultContext = new VariableContext();

	private int num = 1; //1st variable will have num 1, then add 1 each time

	public VariableContext() {
		varsByNum.add(null); //Index num 0 shouldn't exist. Vars start at 1
	}
	
	//From 1 to x
	public Variable getVar(int UID) {
		return varsByNum.get(UID);
	}
	
	public Variable getOrCreateVar(String name) {
		if(vars.containsKey(name)) {
			return vars.get(name);
		} else {
			Variable v = addNewVar(name);
			return v;
		}
	}

 
	private Variable addNewVar(String name) {
		Variable v = new Variable(this,num,name);
		num++;
		vars.put(name,v);
		varsByNum.add(v);
		return v;
	}
	
	public Variable createNextDefaultVar() {
		return addNewVar(""+num);
	}
	
	//Number of vars made
	//Also maxUID
	public int size() {
		return num-1;
	}
	
	public void ensureSize(int size) {
		while(size() < size) {
			createNextDefaultVar();
		}
	}
	
	//Return a variablecontext that is compatible with the old context
	public VariableContext getCopy() {
		VariableContext ret = new VariableContext();
		for(Variable var : this.varsByNum) {
			if(var == null) continue;
			ret.addNewVar(var.getName());
		}
		
		return ret;
	}

}
