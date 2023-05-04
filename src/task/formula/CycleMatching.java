package task.formula;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import formula.VariableContext;
import formula.simple.CNF;

//Problem: find a valid matching on a cycle. The matching with no matched edges is considered a matching
public class CycleMatching implements CNFCreator, FileDecodable {
	private int numNodes; //equal to num edges

	public CycleMatching(int numNodes) {
		super();
		if(numNodes < 3) throw new UnsupportedOperationException("Number of nodes must be at least 3");
		this.numNodes = numNodes;
	}


	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = new CNF(context);
		//Edge vars are 1...numEdges
		//Edge var 1 is from node 0 to 1, 2 is from node 1 to 2 
		//... edge var numEdges is from node numNodes-1 to node 0 
		int numEdges = numNodes;
		
//		ret.addClause(-1,-2);
		
		ret.addClause(-1,-numEdges);
		
		for(int i = 2; i <= numEdges; i++) {
			ret.addClause(-i,-(i-1));
		}
		
		return ret;
		
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		String name = filePrefix+".dot";
		File dotFile = new File(dir, name);

		PrintWriter out = new PrintWriter(dotFile);
		out.write(cycleGraphtoGraphiz(model));
		out.close();

		File picFile = new File(dir,filePrefix+".png");
		
		CommandLine cl = CommandLine.parse("neato -Tpng " + dotFile.getAbsolutePath() 
				+ " -o" + picFile.getAbsolutePath());
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);

	}

	public String cycleGraphtoGraphiz(int[] model) {

		StringBuilder sb = new StringBuilder();
		sb.append("graph cycles {");
		sb.append(ConsoleDecodeable.newline);
		//		sb.append("node [style=filled colorscheme=\"set312\"];");
		//		sb.append(newline);
		sb.append("edge [colorscheme=\"set18\"];");
		sb.append(ConsoleDecodeable.newline);
		//		sb.append(newline);


		for(int k = 0; k < model.length; k++) {
			sb.append(getNodeName(k)+ "--" +getNodeName((k+1)%model.length));
			
			if(model[k] > 0) {
				sb.append("[color=\""+1+"\" style=\"bold\"]");
				sb.append(";");
				sb.append(ConsoleDecodeable.newline);
			} else {
				sb.append(";");
				sb.append(ConsoleDecodeable.newline);
			}
		}
		sb.append("}");

		return sb.toString();
	}
	
	private String getNodeName(int k) {
		return "V"+k;
	}
	
	

}
