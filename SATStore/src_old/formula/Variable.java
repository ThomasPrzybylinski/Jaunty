package formula;


public class Variable implements Comparable<Variable> {
	private String name;
	private Integer compare = null; //Must be same for negated
	private int uid; //this variable is the nth created
	
	private VariableContext creator;
	
	private Literal posLit;
	private Literal negLit;
	
	Variable(VariableContext creator,int num) {
		setupVar(creator,num);
		setupLiterals();
		
	}

	Variable(VariableContext creator, int num, String name) {
		setupVar(creator,num);
		this.name = name;
		setupLiterals();
	}

	private void setupVar(VariableContext creator, int num) {
		assert(num > 0);
		this.creator = creator;
		uid = num;
		compare = num;
		name = ""+num;
	}
	
	private void setupLiterals() {
		posLit = new Literal(this,true);
		negLit = new Literal(this,false);
	}
	
	//UIDs start at 1, increase by 1 every new variable created
	//Negated and unnegated vars have the same UID
	public Integer getUID() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public Literal getPosLit() {
		return posLit;
	}

	public Literal getNegLit() {
		return negLit;
	}
	
	public void setCompare(Integer comp) {
		compare = comp;
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Variable) {
			Variable var = (Variable)arg0;
			if(var.creator != this.creator) {
				throw new IllegalArgumentException("Cannot mix variables of different contexts!");
				
			}
			return var.uid == this.uid;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uid;
	}

	@Override
	public String toString() {
		String suffix = name;// +'['+(uid)+']';
		return suffix;
	}

	public String niceString() {
		return name;
	}

	public int compareTo(Variable o) {
		if(o instanceof Variable) {
			Variable v = (Variable)o;
			
			if(v.creator != this.creator) {
				throw new IllegalArgumentException("Cannot mix variables of different contexts!");
			}

			int comp;
			if(compare == null || v.compare == null) {
				comp = this.getUID().compareTo(v.getUID());
			} else {
				comp = this.compare.compareTo(v.compare);
			}
			return comp;
		} else {
			return 0;
		}
	}
}
