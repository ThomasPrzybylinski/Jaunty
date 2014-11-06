package io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import util.lit.LitSorter;
import util.lit.LitUtil;
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
//		toSave = toSave.reduce(); //Makes sure vars are in order, no duplicate vars in clause
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
	
	public static void saveDimacsGraph(PrintWriter out,CNF toSave, String comments) {
//		toSave = toSave.reduce(); //Makes sure vars are in order, no duplicate vars in clause
		String[] commentLines = comments.split(System.getProperty("line.separator")); //split on newlines
		
		for(String s : commentLines) {
			out.print('c');
			out.print(' ');
			out.println(s);
		}
		
		int numVars = toSave.getContext().size();
		int numClauses = toSave.getClauses().size();
		
		int numNodes = 2*numVars + numClauses;
		int numEdges = numVars; //One for each pos/neg pair
		
		for(int[] cl : toSave.getClauses()) {
			numEdges += cl.length;
		}
		
		out.print("p edge ");
		out.print(numNodes+1);
		out.print(' ');
		out.println(numEdges+1);
		
		for(int k = 1; k < 2*numVars+2; k++) {
			out.println("n "+ k + " 1");
		}
		
		for(int k = 2*numVars+2; k < numClauses + 2*numVars+2; k++) {
			out.println("n "+ k + " 2");
		}
		
		for(int k = 1; k <= numVars; k++) {
			out.println("e " + (LitUtil.getIndex(k,numVars)+1) +" " +(LitUtil.getIndex(-k,numVars)+1));
		}
		
		int cl = 2*numVars+2;
		for(int[] clause : toSave.getClauses()) {
			for(int i : clause) {
				out.println("e " + (LitUtil.getIndex(i,numVars)+1) +" " +cl);
			}
			cl++;
		}
		
		out.close();
	}
}
