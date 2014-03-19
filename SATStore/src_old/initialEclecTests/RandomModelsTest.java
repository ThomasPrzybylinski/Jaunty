package initialEclecTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import task.clustering.SimpleDBScan;
import task.formula.FormulaCreator;
import task.formula.random.WeakTrueBoolFormula;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import formula.Disjunctions;
import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;

public class RandomModelsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		int v = 10;
		int numTrue = 800;
		FormulaCreator fc = new WeakTrueBoolFormula(v,numTrue);
		
		Disjunctions dis = (Disjunctions)fc.nextFormula();
		
		ClauseList orig = new DNF(dis);
		DNF dnfForSym = new DNF(dis);
		dnfForSym = dnfForSym.reduce();

		List<int[]> models = new ArrayList<int[]>();

		for(int[] mod : dnfForSym.getClauses()) {
			System.out.println(Arrays.toString(mod));
			models.add(mod);
		}
		
		System.out.println(models.size());

		//saveColoredModelsAsPic(numColors, graph, models,"testOrigCyc");

		
//		int k = 0;
//		List<int[]>  prev = models;
//		List<int[]>  cur = models;
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
//				System.out.println(Arrays.toString(mod));
//			}
//			
//			System.out.println(cur.size());
//
//			
//		} while(prev.size() != cur.size());
//
//		dnfForSym = new DNF(dnfForSym.getContext());
//		for(int[] mod : cur) {
//			dnfForSym.addClause(mod);
//		}
//		
//		System.out.println("LocalBreaking");
//
//		Collection<int[]> localModels = SymmetryUtil.breakyByUnitLocalSyms(dnfForSym);
//		cur.clear();
//		cur.addAll(localModels);
//		
//		for(int[] mod : localModels) {
//			System.out.println(Arrays.toString(mod));
//		}
//		System.out.println(localModels.size());
//		
//		
//		System.out.println("ClusteringLocal");
//		clusterAndDisplay(new ArrayList<int[]>(localModels));
//		
//		System.out.println("Clustering");
//		clusterAndDisplay(models);
		
		
		//doTheThing(models, dnfForSym.getContext());
		
//		DisjointSet<int[]> ds = SymmetryUtil.findSymmetryOrbits(dnfForSym);

		boolean[] printed = new boolean[dnfForSym.getClauses().size()];

		//		for(int k = 0; k < cl.getClauses().size(); k++) {
		//			if(!printed[k]) {
		//				System.out.println("NEXT ORBIT");
		//				System.out.println(Arrays.toString(cl.getClauses().get(k)));
		//				for(int i = k+1; i < cl.getClauses().size(); i++) {
		//					if(ds.sameSet(cl.getClauses().get(k),cl.getClauses().get(i))) {
		//						printed[i] = true;
		//						System.out.println(Arrays.toString(cl.getClauses().get(i)));
		//					}
		//				}
		//				System.out.println("");
		//			}
		//		}


//		for(int k = 0; k < dnfForSym.getClauses().size(); k++) {
//			for(int i = 0; i < dnfForSym.getClauses().size(); i++) {
//				String print = (i != k && ds.sameSet(dnfForSym.getClauses().get(k),dnfForSym.getClauses().get(i))) ? "1 " : "0 ";
//				System.out.print(print);
//			}
//			System.out.println();
//		}
//
//		System.out.println();
		
//		boolean[][] thing = SymmetryUtil.getSymmetryGraph(dnfForSym.getContext(),models);
//		
//		
//		for(boolean[] la : thing) {
//			for(boolean i : la) {
//				if(i) {
//					System.out.print("1 ");
//				} else {
//					System.out.print("0 ");
//				}
//			}
//			System.out.println();
//		}
		
		int[][] thing = PrototypesUtil.doAgreementSym(dnfForSym);
		
		
		for(int[] la : thing) {
			for(int i : la) {
				System.out.print(i+" ");
			}
			System.out.println();
		}
		
		
		

	}

	private static void doTheThing(List<int[]> models, VariableContext context) {
		boolean[][] graph = SymmetryUtil.getSymmetryGraph(context,models);
		
		for(boolean[] row : graph) {
			System.out.println(Arrays.toString(row));
		}
		
		//BFS
		boolean[] visited = new boolean[models.size()];
		int[][] length = new int[models.size()][models.size()];
		
		for(int k = 0; k < visited.length; k++) {
			if(!visited[k]) {
				Queue<int[]> modelQueue = new LinkedList<int[]>();
				modelQueue.add(new int[]{k,0});
				visited[k] = true;
				
				while(!modelQueue.isEmpty()) {
					int[] pair = modelQueue.poll();
					int mod = pair[0];
					int len = pair[1];
					
					boolean[] neighbors = graph[mod];
					for(int i = 0; i < neighbors.length; i++) {
						if(neighbors[i] && !visited[i]) {
							length[mod][i] = len+1;
							length[i][mod] = len+1;
							modelQueue.add(new int[]{i,len+1});
							visited[mod] = true;
						}
					}
				}
			}
		}
		
		for(int[] row : length) {
			System.out.println(Arrays.toString(row));
		}
	}

	private static void clusterAndDisplay(List<int[]> models) {
		SimpleDBScan scan = new SimpleDBScan(1);
		
		List<Set<int[]>> clusters = scan.getClustering(models);
		Comparator<int[]> comp = SymmetryUtil.LEX_LEADER_COMP;

		for(Set<int[]> curClust: clusters) {
			int[] curModel = null;
			for(int[] model : curClust) {
				if(curModel == null || comp.compare(curModel,model) < 0) {
					curModel = model;
				}
			}
			
			System.out.println(Arrays.toString(curModel));
			System.out.println(curClust.size());
		}
	}

}
