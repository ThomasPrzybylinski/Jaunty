package graph.graphviz;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class GraphizUtil {
	public static String newline = System.getProperty("line.separator");
	
	public static void saveGraphAsPic(int[][] graph, String filePrefix) throws FileNotFoundException,
	ExecuteException, IOException {
		String name = filePrefix+".dot";

		PrintWriter out = new PrintWriter(name);
		out.write(graphtoGraphiz(graph));
		out.close();

		CommandLine cl = CommandLine.parse("neato -Tjpg " + name 
				+ " -o" + name + ".jpg");
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);
	}


	public static String graphtoGraphiz(int[][] graph) {

		StringBuilder sb = new StringBuilder();
		sb.append("graph  agreementSym {");
		sb.append(newline);
//		sb.append("K = .4;");
//		sb.append(newline);
//		sb.append("dim = 2;");
//		sb.append(newline);
		//sb.append("splines=spline;");
//		sb.append(newline);
		sb.append("splines=true;");
		sb.append(newline);
		//sb.append("len=.5;");
//		sb.append("sep=.5;");
//		sb.append(newline);
		sb.append("overlap=false;");
		sb.append(newline);
		//		sb.append(newline);

		for(int k = 0; k < graph.length; k++) {
			for(int i = k+1; i < graph[k].length; i++) {
				if(graph[k][i] == 1) {
					sb.append(name(k)+ "--" +name(i)+";");
					sb.append(newline);
				}
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public static String name(int n) {
		return "N"+(n);

	}

}
