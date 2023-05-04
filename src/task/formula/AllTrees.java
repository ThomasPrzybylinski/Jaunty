package task.formula;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import formula.VariableContext;
import formula.simple.CNF;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;

public class AllTrees implements ModelGiver, ConsoleDecodeable,FileDecodable {
	private int numNodes;
	private CNF debug;
	VariableContext theContext;
	public AllTrees(int numNodes) {
		this.numNodes = numNodes;
	}
	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		theContext = new VariableContext();
//		for(int k = 0 ; k < numNodes; k++) {
//			for(int i = k+1 ; i < numNodes; i++) {
//				String name = "E["+k+","+i+"]";
//				theContext.getOrCreateVar(name);
//				System.out.println(name + " " + getEdgeVar(k,i));
//			}
//		}
//
//		for(int k = 1 ; k < numNodes; k++) {
//			for(int i = 1 ; i < numNodes; i++) {
//				String name = "P_"+i+"["+k+"]";
//				theContext.getOrCreateVar(name);
//				System.out.println(name + " " + getLenKPathVar(k,i));
//			}
//		}
//
//		for(int k = 1 ; k < numNodes; k++) {
//			for(int i = 1 ; i < numNodes; i++) {
//				if(k == i) continue;
//				for(int j = 2 ; j < numNodes; j++) {
//					String name = "NP_"+j+"["+k+","+i+"]";
//					theContext.getOrCreateVar(name);
//					System.out.println(name + " " + getNoPathVar(k,i,j));
//				}
//			}
//		}
//
//
//		System.out.println();

		ISolver satSolve;
		try {
			satSolve = getSolver();
		} catch(ContradictionException ce) {
			return new ArrayList<int[]>(0);
		}

		ModelIterator iter = new ModelIterator(satSolve);

		ArrayList<int[]> ret = new ArrayList<int[]>();
		while(iter.isSatisfiable()) {
			int[] retMode = new int[(numNodes*(numNodes-1))/2];
			int[] mod = iter.model();
			System.arraycopy(mod, 0, retMode, 0, retMode.length);
			ret.add(retMode);
			
//			ret.add(iter.model());
		}

		
		

		satSolve.reset();
		return ret;
	}
	private ISolver getSolver() throws ContradictionException{

		debug = new CNF(theContext);
		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar((numNodes*(numNodes-1))/2 + (numNodes)*(numNodes) + numNodes*numNodes);
		satSolve.setExpectedNumberOfClauses(numNodes+numNodes + numNodes*(numNodes-1));

		for(int n1 = 1; n1 < numNodes; n1++) {

			int[] mustHavePathTo0 = new int[numNodes-1];
			for(int len = 1; len < numNodes; len++) {
				mustHavePathTo0[len-1] = getLenKPathVar(n1,len);
			}
			add(satSolve, mustHavePathTo0);

			//			if(k != 0) {
			//There is a path of length 1 to node 0 iff there is an edge from k to 0
			int pathSizeOne = getLenKPathVar(n1,1);
			int edgeTo0 = getEdgeVar(0,n1);

			int[] pathSizeOneifEdgeTo0_one = new int[]{-pathSizeOne,edgeTo0};
			add(satSolve, pathSizeOneifEdgeTo0_one);

			int[] pathSizeOneifEdgeTo0_two = new int[]{pathSizeOne,-edgeTo0};
			add(satSolve, pathSizeOneifEdgeTo0_two);

			//			}

			for(int len = 2; len <= numNodes-1; len++) {
				int[] ifNoJMinusPathsAvailNoJPath = new int[numNodes-1];
				int kHasLenJPath = getLenKPathVar(n1,len);
				ifNoJMinusPathsAvailNoJPath[0] = -kHasLenJPath; 

				for(int n2 = 1; n2 < numNodes; n2++) {
					if(n2 == n1) continue;


					//setup nopaths
					int noPath = getNoPathVar(n2,n1,len);
					int iHasLenJMinusPath = getLenKPathVar(n2,len-1);
					int edge = getEdgeVar(n1,n2);

					ifNoJMinusPathsAvailNoJPath[n2 > n1 ? n2-1: n2] = -getNoPathVar(n2,n1,len);

					int[] ifJMinusPathAndEdgeTheLenJPath = new int[]{kHasLenJPath,-iHasLenJMinusPath,-edge};
					int[] noPathIfJNotHavePath = new int[]{iHasLenJMinusPath,noPath};
					int[] noPathIfNoEdge = new int[]{edge,noPath};
					int[] ifNoPathNoEdgeOrNoSizePath = new int[]{-iHasLenJMinusPath,-noPath,-edge};


					add(satSolve, ifJMinusPathAndEdgeTheLenJPath);
					add(satSolve, noPathIfJNotHavePath);
					add(satSolve, noPathIfNoEdge);
					add(satSolve, ifNoPathNoEdgeOrNoSizePath);
				}

				add(satSolve, ifNoJMinusPathsAvailNoJPath);
			}
		}

		


		int[] next = new int[(numNodes*(numNodes-1))/2];

		for(int k = 0; k < next.length; k++) {
			next[k] = k+1;
		}
		
		int prevMax = satSolve.nVars();
		//Induce order.
		satSolve.newVar(numNodes-1);
		
		satSolve.reset();
		satSolve = debug.getSolverForCNFEnsureVariableUIDsMatch();
		
		
		//Make tree
		satSolve.addExactly(new VecInt(next),numNodes-1);

		return satSolve;
	}
	private void add(ISolver satSolve, int[] clause)
			throws ContradictionException {
		debug.addClause(clause);
//		satSolve.addClause(new VecInt(clause));
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return this;
	}

	@Override
	public String getDirName() {
		return "AllTrees("+numNodes+")";
	}

	int getEdgeVar(int node1, int node2) {
		int least = Math.min(node1,node2);
		int max = Math.max(node1,node2);

		int leastStart = least*(numNodes);

		//		if(least > 0) {		
		leastStart -= ((least)*(least+1))/2;
		//		}

		return leastStart + (max-least);
	}

	//Does not exist for node 0
	int getLenKPathVar(int node, int len) {
		return (numNodes*(numNodes-1))/2 + (node-1)*(numNodes-1) + len; 
	}

	//If there is not way for node1 to go to node 2 to form a path of size len
	//len >=2
	int getNoPathVar(int node1, int node2, int len) {
		int adjustment = 0; //Adjusts for the fact that there is no NP from and edge to itself
		if(node1 > 1) {
			adjustment = (node1-2)*(numNodes-2);
			if(node2 > node1) {
				adjustment += numNodes-2;
			}
		}

		return (numNodes*(numNodes-1))/2 + (numNodes-1)*(numNodes-2)
				+ (node1-1)*(numNodes-1)*(numNodes-2) + (node2-1)*(numNodes-2) + len - adjustment;
	}
	
//	int getEqualsVar(int edge1, int node2, int prevMax) {
//		
//	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {

		String name = filePrefix+".dot";
		File dotFile = new File(dir, name);
		PrintWriter out = new PrintWriter(dotFile);
		out.write(consoleDecoding(model));
		out.close();

		File picFile = new File(dir,filePrefix+".png");

		CommandLine cl = CommandLine.parse("dot -Tpng " + dotFile.getAbsolutePath() 
				+ " -o" + picFile.getAbsolutePath());
		DefaultExecutor de = new DefaultExecutor();
		de.execute(cl);

	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);

	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		sb.append("graph colored {");
		sb.append(newline);
		//		sb.append("node [style=filled colorscheme=\"set312\"];");
		sb.append("node [style=filled colorscheme=\"set19\"];");
		sb.append(newline);
		//		sb.append("rankdir=RL;");
		sb.append(newline);

		for(int k = 0; k < numNodes; k++) {
			for(int j = k+1; j < numNodes; j++) {
				if(model[getEdgeVar(k,j)-1] > 0) {
					sb.append(name(k)+ "--" +name(j)+";");
					sb.append(newline);
				}
			}

		}
		sb.append("}");
		return sb.toString();

	}

	private String name(int k) {
		return "V"+k;
	}
	
	public String toString() {
		return "AllTrees("+numNodes+")";
	}
}