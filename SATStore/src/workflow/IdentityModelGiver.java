package workflow;

import java.util.List;

import org.sat4j.specs.TimeoutException;

import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import formula.VariableContext;
import formula.simple.ClauseList;

public class IdentityModelGiver implements ModelGiver {
	ClauseList models;
	
	public IdentityModelGiver(ClauseList dnf) {
		models = dnf;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
		List<int[]> ret = models.getClauses();
		if(ret.size() > 0) {
			context.ensureSize(ret.get(0).length);
		}
		return ret;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;	
	}

	@Override
	public String getDirName() {
		return models.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return "Creat: " + models.getClass().getSimpleName();
	}
	
	

}
