package io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import util.lit.LitSorter;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;


public class DimacsLoaderSaver {
	
	public static CNF loadDimacsNoException(String fileName) {
		try {
		FileInputStream fis = new FileInputStream(fileName);
		CNF ret = loadDimacs(fis);
		fis.close();
		
		return ret;
		}catch(IOException e) {
			return null;
		}
	}
	
	
	public static CNF loadDimacs(String fileName) throws IOException{
		FileInputStream fis = new FileInputStream(fileName);
		CNF ret = loadDimacs(fis);
		fis.close();
		
		return ret;
	}
	
	public static CNF loadDimacs(InputStream is) throws IOException{
		Scanner in = new Scanner(is);
		CNF ret = null;
		while(in.hasNextLine()) {
			String line = in.nextLine().trim();
			
			if(line.isEmpty() || line.charAt(0) == 'c' || line.charAt(0) == '0' || line.charAt(0) == '%') {
				continue; //Comment line
			} else if(line.startsWith("p cnf")) {
				ret = new CNF(new VariableContext());
			} else {
				String[] parts = line.trim().split("\\s+");
				int length = parts[parts.length-1].charAt(0) == '0' ? parts.length -1 : parts.length;
				int[] nums = new int[length];
				
				for(int k = 0; k < length; k++) {
					nums[k] = Integer.parseInt(parts[k]);
				}
				LitSorter.inPlaceSort(nums); //Sometimes out of order...
				ret.fastAddClause(nums);
			}
		}
		
		ret.sort();  //sort now for efficiency
		
		is.close();
		in.close();
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
		out.print(toSave.getContext().size());
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
	
	public static void saveDimacs(PrintWriter out,ClauseList toSave, String comments) {
		toSave = toSave.reduce(); //Makes sure vars are in order, no duplicate vars in clause
		String[] commentLines = comments.split(System.getProperty("line.separator")); //split on newlines
		
		for(String s : commentLines) {
			out.print('c');
			out.print(' ');
			out.println(s);
		}
		
		out.print("p cnf ");
		out.print(toSave.getContext().size());
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
