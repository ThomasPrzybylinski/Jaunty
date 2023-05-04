package task.translate;

import java.util.Arrays;

import formula.Variable;
import formula.VariableContext;

public class ContextConsoleDecoder implements ConsoleDecodeable {
	VariableContext context;
	
	public ContextConsoleDecoder(VariableContext context) {
		this.context = context;
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		if(context == null) {
			return Arrays.toString(model);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			
			for(int i : model) {
				int var = Math.abs(i);
				Variable v = context.getVar(var);
				
				if(i < 0) {
					sb.append('-');
				}
				
				sb.append(v.getName());
				sb.append(' ');
			}
			sb.append(']');
			return sb.toString();
		}
		
 
	}
}
