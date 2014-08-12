package task.formula;

import io.DimacsLoaderSaver;
import task.formula.random.CNFCreator;
import formula.VariableContext;
import formula.simple.CNF;

public class IdentityCNFCreator implements CNFCreator {
	public CNF cnf;
	private String s;
	
	
	public IdentityCNFCreator(String path) {
		this(path,path);
	}
	public IdentityCNFCreator(String path, String name) {
		this.s = name;
		this.cnf = DimacsLoaderSaver.loadDimacsNoException(path);
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = cnf.getCopy();
		context.ensureSize(cnf.getContext().size());
		ret.setContext(context);
		return ret;
	}

	
	@Override
	public String toString() {
		return s;
	}
}
