package task.formula;

import io.DimacsLoaderSaver;
import task.formula.random.CNFCreator;
import formula.VariableContext;
import formula.simple.CNF;

public class IdentityCNFCreator implements CNFCreator {
	public CNF cnf;
	private String s;
	
	public IdentityCNFCreator(String s) {
		this.s = s;
		this.cnf = DimacsLoaderSaver.loadDimacsNoException(s);
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = cnf.getCopy();
		ret.setContext(context);
		return ret;
	}

	
	@Override
	public String toString() {
		return s;
	}
}
