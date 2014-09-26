package task.formula;

import io.DimacsLoaderSaver;
import task.formula.random.CNFCreator;
import formula.VariableContext;
import formula.simple.CNF;

public class IdentityCNFCreator implements CNFCreator {
	public CNF cnf;
	private String s;
	private boolean returnCopy = true;
	private String path;
	
	public IdentityCNFCreator(String path) {
		this(path,path);
	}
	public IdentityCNFCreator(String path, String name) {
		this(path,name,true);
	}
	
	public IdentityCNFCreator(String path, String name, boolean returnCopy) {
		this.s = name;
		this.path = path;
		this.returnCopy = returnCopy;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		if(cnf == null) {
			this.cnf = DimacsLoaderSaver.loadDimacsNoException(path).squeezed().trySubsumption();
		}
		CNF ret = cnf;
		
		if(returnCopy) {
			ret = ret.getCopy();
		}
		
		context.ensureSize(cnf.getContext().size());
		ret.setContext(context);
		return ret;
	}

	
	
	public String getPath() {
		return path;
	}
	@Override
	public String toString() {
		return s;
	}
}
