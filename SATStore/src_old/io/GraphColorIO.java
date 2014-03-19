package io;

import graph.Node;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class GraphColorIO implements ConsoleDecodeable, FileDecodable {
	private Node[] graph;
	private int numColors;

	public GraphColorIO(Node[] graph, int numColors) {
		this.graph = graph;
		this.numColors = numColors;
	}


	@Override
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
		out.write(coloredGraphtoGraphiz(graph,numColors, model));
		out.close();

		File picFile = new File(dir,filePrefix+".png");
		
		CommandLine cl = CommandLine.parse("dot -Tpng " + dotFile.getAbsolutePath() 
				+ " -o" + picFile.getAbsolutePath());
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);
		
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		return coloredGraphtoGraphiz(graph,numColors, model);
	}


	public static String coloredGraphtoGraphiz(Node[] graph, int numColors, int[] model) {

		StringBuilder sb = new StringBuilder();
		sb.append("graph colored {");
		sb.append(newline);
		sb.append("node [style=filled colorscheme=\"set312\"];");
		sb.append(newline);
		//		sb.append(newline);

		for(Node n : graph) {
			for(Node n2 : n.getNeighbors()) {
				if(n.nodeNum > n2.nodeNum) {
					sb.append(name(n)+ "--" +name(n2)+";");
					sb.append(newline);
				}
			}
		}

		for(int col : model) {
			if(Math.abs(col) > graph.length*numColors) {
				break;
			}
			if(col > 0) {
				col = col-1;
				int node = col/numColors;
				int color = (col%numColors)+1;
				sb.append(name(node) + "[fillcolor=\""+color+"\"];");
				sb.append(newline);
			}
		}

		sb.append("}");

		return sb.toString();
	}


	public static String name(Node n) {
		return "V"+(n.nodeNum+1);

	}

	public static String name(int n) {
		return "V"+(n+1);

	}


	


	
}
