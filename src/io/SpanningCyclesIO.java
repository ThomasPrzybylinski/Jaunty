package io;

import formula.Variable;
import formula.VariableContext;
import graph.Node;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class SpanningCyclesIO implements ConsoleDecodeable, FileDecodable {
	private Node[] graph;

	public SpanningCyclesIO(Node[] graph) {
		this.graph = graph;
	}


	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}
		
	public void fileDecoding(File dir, String filePrefix, int[] model) throws IOException {
//		for(File file : dir.listFiles()) {
//			if((file.getPath().endsWith(".jpg") || file.getPath().endsWith(".dot")) &&
//					(file.getName().startsWith(filePrefix))) {
//				file.delete();
//			}
//		}
		
		String name = filePrefix+".dot";
		File dotFile = new File(dir, name);

		PrintWriter out = new PrintWriter(dotFile);
		out.write(cycleGraphtoGraphiz(graph,model));
		out.close();

		File picFile = new File(dir,filePrefix+".png");
		
		CommandLine cl = CommandLine.parse("dot -Tpng " + dotFile.getAbsolutePath() 
				+ " -o" + picFile.getAbsolutePath());
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);
		
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		return cycleGraphtoGraphiz(graph,model);
	}


	public String cycleGraphtoGraphiz(Node[] graph, String model) {
		int[] param = new int[model.length()];
		for(int k = 0; k < model.length();k++) {
			param[k] = Integer.parseInt(""+model.charAt(k));
		}

		return cycleGraphtoGraphiz(graph,param);
	}

	public String cycleGraphtoGraphiz(Node[] graph, int[] model) {

		StringBuilder sb = new StringBuilder();
		sb.append("graph cycles {");
		sb.append(newline);
		//		sb.append("node [style=filled colorscheme=\"set312\"];");
		//		sb.append(newline);
		sb.append("edge [colorscheme=\"set18\"];");
		sb.append(newline);
		//		sb.append(newline);


		HashSet<Node> visited = new HashSet<Node>();
		int color = 1;
		for(Node n : graph) {
			if(!visited.contains(n)) {
				appendCycle(n,sb,visited,color,model);
				color = (color%8)+1;
			}
		}
		sb.append("}");

		return sb.toString();
	}

	public static void appendCycle(Node n, StringBuilder sb, Set<Node> visited, int color, int[] model) {
		List<Node> toVisit = new ArrayList<Node>();
		for(Node n2 : n.getNeighbors()) {
			if(visited.contains(n2)) continue;

			int num1;
			int num2;
			if(n.nodeNum < n2.nodeNum) {
				num1 = n.nodeNum;
				num2 = n2.nodeNum;
			} else {
				num1 = n2.nodeNum;
				num2 = n.nodeNum;
			}
			sb.append(name(n)+ "--" +name(n2));

			Variable v = VariableContext.defaultContext.getOrCreateVar("E_"+num1+"_"+num2);

			if(model[v.getUID()-1] > 0) {
				sb.append("[color=\""+color+"\" style=\"bold\"]");
				sb.append(";");
				sb.append(newline);
				toVisit.add(n2);
			} else {
				sb.append(";");
				sb.append(newline);
			}
		}

		visited.add(n);
		for(Node n2 : toVisit) {
			if(!visited.contains(n2)) {
				appendCycle(n2,sb,visited,color,model);
			}
		}
	}



	public static String name(Node n) {
		return "V"+(n.nodeNum);

	}

	public static String name(int n) {
		return "V"+(n);

	}


	
}
