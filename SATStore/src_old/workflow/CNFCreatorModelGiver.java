package workflow;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.CNF;

import task.formula.random.CNFCreator;
import task.sat.SATUtil;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class CNFCreatorModelGiver implements ModelGiver {
	private CNFCreator creat;
	
	public CNFCreatorModelGiver(CNFCreator creat) {
		super();
		this.creat = creat;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
		CNF cnf = creat.generateCNF(context);
		try {
			return SATUtil.getAllModels(cnf);
		} catch (ContradictionException e) {
			return new ArrayList<int[]>(0);
		} 
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return creat instanceof ConsoleDecodeable ? (ConsoleDecodeable)creat : null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return creat instanceof FileDecodable ? (FileDecodable)creat : null;	
	}

	@Override
	public String getDirName() {
		return creat.getClass().getSimpleName();
	}

}
