package task.symmetry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

import task.symmetry.local.LocalSymClauses;

import io.DimacsLoaderSaver;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;

//Not 100% compatible with RealSymFinder. Will miss some local syms due to how things happen
//May be part of why its slow
@Deprecated
public class SHATTERSymFinder {
	private ClauseList cl;
	private LiteralGroup modelGroup;
	private int numVars;
	private int totalClauses;
	private int[] varTrans;
	private int[] modelTrans;

	private static String home = "/home/goldencoal/Shatter_Linux/";

	public SHATTERSymFinder(ClauseList cl, LocalSymClauses loc) {
		int[] rep = cl.getClauses().get(0);
		varTrans = new int[rep.length+1];
		
		for(int k = 0; k < rep.length; k++) {
			varTrans[k+1] = Math.abs(rep[k]);
		}
		
		this.cl = cl;
		modelTrans = loc.getCurModelTranslation();
		totalClauses = loc.numTotalModels();
	}

	public LiteralGroup getSymGroup() {
		PrintWriter pw;
		numVars = cl.getContext().size();
		
		ClauseList toSave = new ClauseList(new VariableContext());
		
		for(int[] model : cl.getClauses()) {
			int[] newMod = new int[model.length];
			for(int k = 0; k < model.length; k++) {
				int sign = model[k] > 0 ? 1 : -1;
				newMod[k] = sign*(k+1);
			}
			toSave.fastAddClause(newMod);
		}
		
		try {
			pw = new PrintWriter(home+"toShatter");
			DimacsLoaderSaver.saveDimacs(pw,toSave,"");
			CommandLine col = CommandLine.parse("perl getSym.pl " + "toShatter");
//			ExecuteWatchdog wd = new ExecuteWatchdog(30000);
			DefaultExecutor de = new DefaultExecutor();
			de.setWorkingDirectory(new File("/home/goldencoal/Shatter_Linux/"));
//			de.setWatchdog(wd);
			de.execute(col);
//			System.out.println(de.execute(col));
			
//			if(de.getWatchdog().killedProcess()) {
//				System.exit(1);
//			}
			return readFile(new BufferedReader(new FileReader(home+"toShatter.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public LiteralGroup getModelGroup() {
		return modelGroup;
	}

	public LiteralGroup readFile(BufferedReader in) throws IOException {
		String line = null;
		line = in.readLine(); //'['
		if(line == null) {
			System.out.println(line);
			System.out.println(in);
		}
		
		int numLocalVars = varTrans.length-1;
		
//		System.out.println(line);
		ArrayList<LiteralPermutation> varPerms = new ArrayList<LiteralPermutation>();
		ArrayList<LiteralPermutation> modPerms = new ArrayList<LiteralPermutation>();
		
		line = in.readLine();
		line = line.trim();
		while(!line.equals("]")) {
			int curIndex = 1; //skip first paren
			int wordIndex = 1;
			Integer prevNum = null;
			Integer firstNum = null;
			boolean model = false;

			int[] perm = getID(numVars+1);

			while(curIndex < line.length()) {
				while(Character.isDigit(line.charAt(curIndex))) {
					curIndex++;
				}

				String num = line.substring(wordIndex,curIndex);
				wordIndex = curIndex+1;
				
				if(num.length() == 0) break;

				Integer curNum = Integer.parseInt(num);
				if(curNum.intValue() > numLocalVars) {
					curNum = curNum-numLocalVars;
					if(curNum.intValue() > numLocalVars) {
						if(model == false) {
							model = true;
							varPerms.add(new LiteralPermutation(perm));
							perm = getID(totalClauses+1);
						}
						
						curNum = curNum-numLocalVars;
					}
				} else {
					curNum = -curNum;
				}
				
				if(model) {
					curNum = modelTrans[curNum];
				} else {
					int sign = curNum > 0 ? 1 : -1;
					curNum = sign*varTrans[sign*curNum];
				}

				if(prevNum != null) {
					if(prevNum < 0) {
						perm[-prevNum] = -curNum;	
					} else {
						perm[prevNum] = curNum;
					}
				} else {
					firstNum = curNum;
				}
				
				if(curNum == 0) {
					System.out.println("CUR");
				}

				prevNum = curNum;
				
				if(curIndex+1 < line.length() && line.charAt(curIndex+1) == '(') {
					
					//end of cycle, start of next
					if(prevNum != null) {
						if(prevNum < 0) {
							perm[-prevNum] = -firstNum;	
						} else {
							perm[prevNum] = firstNum;
						}
					}
					firstNum = null;
					curNum = null;
					prevNum = null;
					curIndex = curIndex+1;
				} else if(curIndex+2 >= line.length()) {
					//end of perm, could have model ID
					if(prevNum != null) {
						if(prevNum < 0) {
							perm[-prevNum] = -firstNum;	
						} else {
							perm[prevNum] = firstNum;
						}
					}
					if(model) {
						modPerms.add(new LiteralPermutation(perm));
					}
//					Making sure they are local symmetrie is a lot of work...
					else {
						varPerms.add(new LiteralPermutation(perm));
						modPerms.add(new LiteralPermutation(getID(totalClauses+1)));
					}
					curIndex = curIndex+1;
				}

				curIndex = curIndex + 1;
				wordIndex = curIndex;
			}
			line = in.readLine();
			line = line.trim();

		}
		if(modPerms.size() == 0) {
			modPerms.add(new LiteralPermutation(getID(totalClauses+1)));
		}
		modelGroup = new NaiveLiteralGroup(modPerms);
		
		in.close();
		
		if(varPerms.size() == 0) {
			varPerms.add(new LiteralPermutation(getID(numVars+1)));
		}
		return new NaiveLiteralGroup(varPerms);

	}
	
	private static int[] getID(int size) {
		int[] ret = new int[size];
		
		for(int k = 0; k < size; k++) {
			ret[k] = k;
		}
		
		return ret;
	}
}
