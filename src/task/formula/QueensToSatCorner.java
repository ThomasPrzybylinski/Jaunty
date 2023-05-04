package task.formula;

import formula.VariableContext;
import formula.simple.CNF;

public class QueensToSatCorner extends QueensToSAT {

	public QueensToSatCorner(int size) {
		super(size);
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		return this.encode(context);
	}

	@Override
	public CNF encode(VariableContext context) {
		CNF ret = super.encode(context);
		int size = this.N;
		ret.addClause(1,size,size*size-size+1,size*size);
		return ret;
	}

	@Override
	public String toString() {
		return N+"QueensToSATCorner";
	}
	
	

}
