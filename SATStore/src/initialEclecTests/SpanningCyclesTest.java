package initialEclecTests;
import formula.Conjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;
import graph.CompleteGraphCreator;
import graph.Node;
import graph.sat.SpanningCycleGraphProblem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;



public class SpanningCyclesTest {
	public static String newline = System.getProperty("line.separator");
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numNodes = 8;

		File dir = new File(".");
		for(File file : dir.listFiles()) {
			if((file.getPath().endsWith(".jpg") || file.getPath().endsWith(".dot")) &&
					(file.getName().startsWith("testOrigCyc")
					|| file.getName().startsWith("globCyc")
					|| file.getName().startsWith("locCyc"))
					|| file.getName().startsWith("agreeThingCyc")) {
				file.delete();
			}
		}

		//		SpanningCycleGraphCreator creat = new SpanningCycleGraphCreator();
		//		Node[] graph = creat.getSpanningCycleGraph(numNodes,numEdges,numCycles);

		Node[] graph = CompleteGraphCreator.getCompleteGraph(numNodes);

		Conjunctions conj = SpanningCycleGraphProblem.cycleAsCNF(graph);
		CNF orig = new CNF(conj);

		List<int[]> models = new ArrayList<int[]>();

		System.out.println("Solving");
		ISolver solver = orig.getSolverForCNF();
		ModelIterator iter = new ModelIterator(solver);

		DNF dnfForSym = new DNF(orig.getContext());

		while(iter.isSatisfiable()) {
			int[] model = iter.model();
			models.add(model);
			dnfForSym.addClause(model);
		}
		
		int k = 0;
		List<int[]>  prev = models;
		List<int[]>  cur = models;
//
//		do {
//			System.out.println("Breaking");
//			List<int[]> syms = SymmetryUtil.getSyms(dnfForSym);
//			List<int[]> globModels = SymmetryUtil.breakModels(cur,syms);
//			prev = cur;
//			cur = globModels;
//			k++;
//
//			dnfForSym = new DNF(dnfForSym.getContext());
//			for(int[] mod : cur) {
//				dnfForSym.addClause(mod);
//			}
//
//			saveColoredModelsAsPic(graph, cur,"globCyc"+k+"_");
//		} while(false);//prev.size() != cur.size());
//
//		dnfForSym = new DNF(dnfForSym.getContext());
//		for(int[] mod : cur) {
//			dnfForSym.addClause(mod);
//		}
		
//		System.out.println("LocalBreaking");
//		
//		Collection<int[]> localModels = SymmetryUtil.breakyByUnitLocalSyms(dnfForSym);
//		cur.clear();
//		cur.addAll(localModels);
//		saveColoredModelsAsPic(graph, cur,"locCyc");
//		System.out.println("Thing");
//		
		System.out.println("Agreement");
		int[][] lens = PrototypesUtil.doAgreementSym(models,dnfForSym.getContext());
		int num = 0;
		first: for(k = 0; k < lens.length; k++) {
			for(int i = 0; i < lens[k].length; i++) {
				if(lens[k][i] > 1) {
					List<int[]> blah = new ArrayList<int[]>(1);
					blah.add(models.get(k));
					saveColoredModelsAsPic(graph, blah,"agreeThingCyc"+num+"_");
					num++;
					for(int j = 0; j < lens[k].length; j++) {
						if(k == j) continue;
						if(lens[k][j] > 1) {
							System.out.println(Arrays.toString(models.get(j)) + " " + lens[k][j]);
							blah.clear();
							blah.add(models.get(j));
							saveColoredModelsAsPic(graph, blah,"agreeThingCyc"+num+"_");
							num++;
						}
					}
					break first;
				}
			}
		}
		
//		int[][] other = new int[lens.length][lens[0].length];
//		
//		
//		for(k = 0; k < lens.length; k++) {
//			int index1 = k;
//
//			for(int i = 0; i < lens.length; i++) {
//				//if(i == k) continue;
////				int index2 = globMods.get(i);
//				if(lens[index1][i] > 1) {
//					other[index1][i] = 1;
//				}
//			}
//		}
		
		
//		GraphizUtil.saveGraphAsPic(other,"SpanCycAgreementSymmetryGraph");
		
//		for(int k = 1; k < lens[0].length; k++) {
//			if(lens[0][k] > 1) {
//				System.out.println(models.get(k));
//			}
//		}

	}


	private static String createString(int numNodes) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < numNodes; k++) {

			for(int i = k+1; i < numNodes; i++) {
				if(i == k+1 || (k == 0 && i == numNodes-1)) {
					sb.append(1);
				} else {
					sb.append(0);
				}
			}
		}
		return sb.toString();
	}


	private static void saveColoredModelsAsPic(Node[] graph, List<int[]> models, String filePrefix) throws FileNotFoundException,
	ExecuteException, IOException {
		for(int k = 0; k < models.size(); k++) {
			String name = filePrefix+k+".dot";

			PrintWriter out = new PrintWriter(name);
			out.write(cycleGraphtoGraphiz(graph,models.get(k)));
			out.close();

			CommandLine cl = CommandLine.parse("dot -Tjpg " + name 
					+ " -o" + name + ".jpg");
			DefaultExecutor de = new DefaultExecutor();
			de.execute(cl);
		}
	}

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

	public static String cycleGraphtoGraphiz(Node[] graph, String model) {
		int[] param = new int[model.length()];
		for(int k = 0; k < model.length();k++) {
			param[k] = Integer.parseInt(""+model.charAt(k));
		}

		return cycleGraphtoGraphiz(graph,param);
	}

	public static String cycleGraphtoGraphiz(Node[] graph, int[] model) {

		StringBuilder sb = new StringBuilder();
		sb.append("graph cycles {");
		sb.append(newline);
		//		sb.append("node [style=filled colorscheme=\"set312\"];");
		//		sb.append(newline);
		sb.append("edge [colorscheme=\"set18\"];");
		sb.append(newline);
		//		sb.append(newline);


		HashSet<Node> visited = new HashSet<Node>();
		int color = 1;
		for(Node n : graph) {
			if(!visited.contains(n)) {
				appendCycle(n,sb,visited,color,model);
				color = (color%8)+1;
			}
		}
		sb.append("}");

		return sb.toString();
	}

	public static void appendCycle(Node n, StringBuilder sb, Set<Node> visited, int color, int[] model) {
		List<Node> toVisit = new ArrayList<Node>();
		for(Node n2 : n.getNeighbors()) {
			if(visited.contains(n2)) continue;

			int num1;
			int num2;
			if(n.nodeNum < n2.nodeNum) {
				num1 = n.nodeNum;
				num2 = n2.nodeNum;
			} else {
				num1 = n2.nodeNum;
				num2 = n.nodeNum;
			}
			sb.append(name(n)+ "--" +name(n2));

			Variable v = VariableContext.defaultContext.getOrCreateVar("E_"+num1+"_"+num2);

			if(model[v.getUID()-1] > 0) {
				sb.append("[color=\""+color+"\" style=\"bold\"]");
				sb.append(";");
				sb.append(newline);
				toVisit.add(n2);
			} else {
				sb.append(";");
				sb.append(newline);
			}
		}

		visited.add(n);
		for(Node n2 : toVisit) {
			if(!visited.contains(n2)) {
				appendCycle(n2,sb,visited,color,model);
			}
		}
	}



	public static String name(Node n) {
		return "V"+(n.nodeNum);

	}

	public static String name(int n) {
		return "V"+(n);

	}
}
