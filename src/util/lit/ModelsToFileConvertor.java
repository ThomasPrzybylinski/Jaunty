package util.lit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ModelsToFileConvertor {
	public static void saveAsArff(Iterable<int[]> models, String name) throws IOException {
		File f = new File(name+".arff");
		
		PrintWriter out = new PrintWriter(f);
		out.println("@relation " + name);
		out.println();
		
		boolean first = true;
		
		for(int[] m : models) {
			if(first) {
				for(int k = 0; k < m.length; k++) {
					//out.println("@attribute " + "V_" + k + " numeric");
					out.println("@attribute " + "V_" + k + " {true,false}");
				}
				out.println();
				out.println("@data");
				first = false;
			}
			
			boolean printFirst = true;
			for(int i : m) {
				if(!printFirst) {
					out.print(",");
				} else {
					printFirst = false;
				}

				//out.print(i > 0 ? 1 : 0);
				out.print(i > 0 ? "true" : "false");
			}
			out.println();
		}
		out.close();
	}
	
	public static void saveAsCSV(Iterable<int[]> models, String name) throws IOException {
		File f = new File(name+".csv");
		
		PrintWriter out = new PrintWriter(f);

		for(int[] m : models) {
			boolean printFirst = true;
			for(int i : m) {
				if(!printFirst) {
					out.print(",");
				} else {
					printFirst = false;
				}

				out.print(i > 0 ? 1 : 0);
				//out.print(i > 0 ? "true" : "false");
			}
			out.println();
		}
		out.close();
	}
}
