package initialEclecTests;

import formula.Conjunctions;
import formula.simple.CNF;
import formula.simple.DNF;
import graph.ColorableGraphCreator;
import graph.Node;
import graph.sat.GraphToColorProblem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import task.symmetry.SymmetryUtil;



public class ColorGraphTests {
	public static String newline = System.getProperty("line.separator");
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		int numNodes = 10;
		int numEdges =  18;
		int numColors = 3;
		
		int num = 0;
		int distinctNum = 0;

		File dir = new File(".");
		for(File file : dir.listFiles()) {
			if((file.getPath().endsWith(".jpg") || file.getPath().endsWith(".dot")) &&
					(file.getName().startsWith("testOrigCol")
					|| file.getName().startsWith("testGlobCol")
					|| file.getName().startsWith("testLocCol"))) {
				file.delete();
			}
		}

		ColorableGraphCreator creat = new ColorableGraphCreator();
		Node[] graph = creat.getColorableGraph(numNodes,numEdges,numColors);
		
		numColors +=0;
		
		Conjunctions color = GraphToColorProblem.coloringAsCNF(graph,numColors);
		
		CNF orig = new CNF(color);
		
		List<int[]> models = new ArrayList<int[]>();
		
		System.out.println("Solve!");
		
		ISolver solver = orig.getSolverForCNF();
		ModelIterator iter = new ModelIterator(solver);
		
		DNF dnfForSym = new DNF(orig.getContext());
		
		while(iter.isSatisfiable()) {
			num++;
			int[] model = iter.model();
			models.add(model);
			dnfForSym.addClause(model);
		}
		
		//saveColoredModelsAsPic(numColors, graph, models,"testOrigCol");
		
		int k = 0;
		List<int[]>  prev = models;
		List<int[]>  cur = models;
		
		do {
			System.out.println("Break!");
			List<int[]> syms = SymmetryUtil.getSyms(dnfForSym);
			List<int[]> globModels = SymmetryUtil.breakModels(cur,syms);
			prev = cur;
			cur = globModels;
			k++;
			
			dnfForSym = new DNF(dnfForSym.getContext());
			for(int[] mod : cur) {
				dnfForSym.addClause(mod);
			}
			
			saveColoredModelsAsPic(numColors, graph, cur,"testGlobCol"+k+"_");
		} while(prev.size() != cur.size());
		
		dnfForSym = new DNF(dnfForSym.getContext());
		for(int[] mod : cur) {
			dnfForSym.addClause(mod);
		}
		
		System.out.println("Local Break!");
		Collection<int[]> localModels = SymmetryUtil.breakyByUnitLocalSyms(dnfForSym);
		cur.clear();
		cur.addAll(localModels);
		saveColoredModelsAsPic(numColors, graph, cur,"testLocCol");
		
		PrototypesUtil.doAgreementSym(models,dnfForSym.getContext());
	}



	private static void saveColoredModelsAsPic(int numColors, Node[] graph,
				List<int[]> models, String filePrefix) throws FileNotFoundException,
			ExecuteException, IOException {
		for(int k = 0; k < models.size(); k++) {
			String name = filePrefix+k+".dot";

			PrintWriter out = new PrintWriter(name);
			out.write(coloredGraphtoGraphiz(graph,numColors,models.get(k)));
			out.close();

			CommandLine cl = CommandLine.parse("dot -Tjpg " + name 
					+ " -o" + name + ".jpg");
			DefaultExecutor de = new DefaultExecutor();
			de.execute(cl);
		}
	}

	

//	private static List<Disjunctions> getSymmBreakingClauses(
//			FileInputStream fileInputStream, int nodes, int colors) {
//		int numVars = nodes*colors;
//		Scanner in = new Scanner(fileInputStream);
//		//Pattern p = Pattern.compile("[(]\\d+(,\\d+)*[)]");
//		ArrayList<Disjunctions> ret = new ArrayList<Disjunctions>();
//		while(in.hasNextLine()) {
//			String line = in.nextLine();
//			
//			if(line.contains("[") || line.contains("]")) continue;
//			
//			line = line.replaceAll("[(]","");
//			String[] things = line.substring(0,line.length()-2).split("[)]");
//
//			for(String s : things) {
//				Disjunctions d = new Disjunctions();
//				String[] vars = s.trim().split(",");
//
//				boolean ok = true;
//				for(int k = 0; k < vars.length; k++) {
//					int varNum = Integer.parseInt(vars[k]);
//					boolean negated = false;
//					if(varNum > numVars) {
//						varNum -= numVars;
//						negated = true;
//					}
//
//					if(varNum > numVars) {
//						ok = false; //Includes a clause var
//						break;
//					}
//
//					Variable realVar = Variable.getVar(varNum);
//					if(negated) {
//						realVar = realVar.negate();
//					}
//					if(k != vars.length-1) {
//						realVar = realVar.negate();
//					}
//					d.add(realVar);
//				}
//
//				if(ok) {
//					ret.add(d);
//				}
//				break;
//			}
//		}
//		in.close();
//		return ret;
//	}
	
	public static String stringifyModel(int[] model, int firstNVars) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < Math.min(model.length,firstNVars); k++) {
			if(model[k] > 0) {
				sb.append(1);
			} else {
				sb.append(0);
			}
		}
		return sb.toString();
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
