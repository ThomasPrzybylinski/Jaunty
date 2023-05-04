package workflow;

import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public interface ModelGiver {
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException;
	
	public ConsoleDecodeable getConsoleDecoder();
	public FileDecodable getFileDecodabler();
	public String getDirName();

}
