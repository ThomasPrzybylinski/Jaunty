package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import graph.Node;
import graph.PossiblyDenseGraph;
import task.translate.ConsoleDecodeable;

public class GraphIO {

	private PossiblyDenseGraph<int[]> graph;

	public GraphIO(PossiblyDenseGraph<int[]> graph) {
		this.graph = graph;
	}


	public void fileDecoding(String filePrefix) throws IOException {
		fileDecoding(new File("."),filePrefix);
	}
		
	public void fileDecoding(File dir, String filePrefix) throws IOException {
//		for(File file : dir.listFiles()) {
//			if((file.getPath().endsWith(".jpg") || file.getPath().endsWith(".dot")) &&
//					(file.getName().startsWith(filePrefix))) {
//				file.delete();
//			}
//		}
		
		String name = filePrefix+".dot";
		File dotFile = new File(dir, name);
		PrintWriter out = new PrintWriter(dotFile);
		out.write(graphtoGraphiz(graph));
		out.close();

		File picFile = new File(dir,filePrefix+".png");
		
		CommandLine cl = CommandLine.parse("neato -Tpng " + dotFile.getAbsolutePath() 
				+ " -o" + picFile.getAbsolutePath());
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);
		
	}
	
	public void consoleDecoding(String filePrefix) throws FileNotFoundException{
		consoleDecoding(new File("."),filePrefix);
	}

	public void consoleDecoding(File dir, String filePrefix)  throws FileNotFoundException{
//		int size = (int)Math.ceil(Math.log10(graph.getNumNodes()+1));
		
		String name = filePrefix+".txt";
		File dotFile = new File(dir, name);
		PrintWriter out = new PrintWriter(dotFile);

		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = 0; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
					out.print("1 ");
				} else {
					out.print("0 ");
				}
			}
			out.println();
		}
		out.close();
		
	}
	
	


	public static String graphtoGraphiz(PossiblyDenseGraph<int[]> graph) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("graph colored {");
		sb.append(ConsoleDecodeable.newline);
		sb.append("overlap=false;");
		sb.append(ConsoleDecodeable.newline);
		sb.append("splines=true;");
		sb.append(ConsoleDecodeable.newline);
		sb.append("node [style=filled colorscheme=\"set312\"];");
		sb.append(ConsoleDecodeable.newline);
		//		sb.append(newline);

		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = k+1; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
//					sb.append(k+ "--" +i+";");
					sb.append(name(graph.getElt(k))+ "--" +name(graph.getElt(i))+";");
					sb.append(ConsoleDecodeable.newline);
				}
			}
		}
		sb.append("}");

		return sb.toString();
	}
	
	
	public static String graphtoGDF(PossiblyDenseGraph<int[]> graph, List<String> labels) {

		StringBuilder sb = new StringBuilder();
		sb.append("nodedef>name VARCHAR,label VARCHAR");
		sb.append(ConsoleDecodeable.newline);
		for(int k = 0; k < graph.getObjs().size(); k++) {
			sb.append(k).append(",").append(labels.get(k));
			sb.append(ConsoleDecodeable.newline);
		}
		
		sb.append("edgedef>node1 VARCHAR,node2 VARCHAR");
		sb.append(ConsoleDecodeable.newline);
		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = k+1; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
//					sb.append(k+ "--" +i+";");
					sb.append(k+ "," +i);
					sb.append(ConsoleDecodeable.newline);
				}
			}
		}
		return sb.toString();
	}
	
	
	//By primitive I mean uses indecies, not names
	public static String graphtoPrimativeCSV(PossiblyDenseGraph<int[]> graph) {

		StringBuilder sb = new StringBuilder();
		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = k+1; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
					sb.append(k+ ";" +i);
					sb.append(ConsoleDecodeable.newline);
				}
			}
		}
		return sb.toString();
	}
	
	
	public static String name(Node n) {
		return "V"+(n.nodeNum+1);

	}
	
	public static String name(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int i : model) {
			if(i > 0) {
				sb.append("1");
			} else {
				sb.append("0");
			}
		}
		
		return sb.toString();
	}

	public static String name(int n) {
		return "V"+(n+1);

	}
}
