package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import formula.VariableContext;
import formula.simple.CNF;


public class DimacsLoaderSaver {
	public static CNF loadDimacs(InputStream is) throws IOException{
		Scanner in = new Scanner(is);
		CNF ret = null;
		while(in.hasNextLine()) {
			String line = in.nextLine();
			
			if(line.trim().isEmpty() || line.charAt(0) == 'c') {
				continue; //Comment line
			} else if(line.startsWith("p cnf")) {
				ret = new CNF(new VariableContext());
			} else {
				String[] parts = line.trim().split("\\s+");
				int[] nums = new int[parts.length-1];
				
				for(int k = 0; k < parts.length-1; k++) {
					nums[k] = Integer.parseInt(parts[k]);
				}
				ret.addClause(nums);
			}
		}
		
		is.close();
		
		return ret;
	}
	
	public static void saveDimacs(PrintWriter out,CNF toSave, String comments) {
		toSave = toSave.reduce(); //Makes sure vars are in order, no duplicate vars in clause
		String[] commentLines = comments.split(System.getProperty("line.separator")); //split on newlines
		
		for(String s : commentLines) {
			out.print('c');
			out.print(' ');
			out.println(s);
		}
		
		out.print("p cnf ");
		out.print(toSave.getContext().getNumVarsMade());
		out.print(' ');
		out.println(toSave.getClauses().size());
		
		
		for(int[] clause : toSave.getClauses()) {
			for(int i : clause) {
				out.print(i);
				out.print(' ');
			}
			out.println('0');
		}
		
		out.close();
	}
}
