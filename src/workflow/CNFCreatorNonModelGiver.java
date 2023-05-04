package workflow;

import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Not;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;
import task.formula.random.CNFCreator;
import task.sat.DNFSAT;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class CNFCreatorNonModelGiver implements ModelGiver {
	private CNFCreator creat;
	
	public CNFCreatorNonModelGiver(CNFCreator creat) {
		super();
		this.creat = creat;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
		CNF cnf1 = creat.generateCNF(context);
		Conjunctions c = cnf1.toConjunction();
		BoolFormula nonModels = (new Not(c)).toDNF();
		DNF dnf = new DNF(((Disjunctions) nonModels)).reduce();
		
		return DNFSAT.getAllModels(dnf);
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
		return "NonModels_"+creat.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "NonModels: " + creat.toString();
	}
	
	

}
