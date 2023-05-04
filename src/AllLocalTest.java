import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.DNF;
import task.formula.QueensToSAT;
import task.formula.random.SimpleCNFCreator;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import util.PartitionIterator;


public class AllLocalTest {
	public static Random rand = new Random();
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VariableContext context = VariableContext.defaultContext;

		QueensToSAT create = new QueensToSAT(8);
		
//		int numNodes = 5;
//		int numEdges =  8;
//		int numColors = 3;
//		
//		ColorableGraphCreator creat = new ColorableGraphCreator();
//		Node[] graph = creat.getColorableGraph(numNodes,numEdges,numColors);
//		Conjunctions color = GraphToColorProblem.coloringAsCNF(graph,numColors);
//		
//		CNF cnf = new CNF(color);
		
		CNF cnf = new CNF((new SimpleCNFCreator(12,3.3,3)).nextFormulaImpl());//CNF cnf = create.encode(context);//  

		System.out.println(cnf);
		System.out.println(cnf.toNumString());

		List<int[]> models = SATUtil.getAllModels(cnf);
		System.out.println(models.size());
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		List<int[]> origModels = new ArrayList<int[]>(models);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			dnfForSym.addClause(i);
		}
		
		DisjointSet<int[]> parts = SymmetryUtil.findSymmetryOrbits(dnfForSym);
		System.out.println(parts.getRoots().size());
		short[][] agreement = new short[models.size()][models.size()];
		
		
		for(int k = 0; k < models.size(); k++) {
			int[] m1 = models.get(k);
			for(int i = k+1; i < models.size(); i++) {
				int[] m2 = models.get(i);
				agreement[i][k] = (short)(parts.sameSet(m1,m2) ? 1 : 0);
				//agreement[k][i] = agreement[k][i];
			}
		}
		
		models = dnfForSym.getClauses();
		for(int k = 0; k < models.size(); k++) {
			System.out.println();
			System.out.println("k:"+k);
			int[] m1 = models.get(k);
			for(int i = k+1; i < models.size(); i++) {
				System.out.print("1");
				int[] m2 = models.get(i);
				if(agreement[k][i] == 0 && agreement[i][k] == 0) {
					
					if(SymmetryUtil.doModelsAgreeSym(dnfForSym.getContext(),models,m1,m2)) {
						System.out.println();
						//agreement[k][i] = 1;
						agreement[i][k] = 1;
					}
				}
			}
		}
		
		System.out.println();
		
		for(short[] row : agreement) {
			System.out.println(Arrays.toString(row));
		}
		
		for(int k = 0; k < models.size(); k++) {
			System.out.println();
			System.out.println("k:"+k);
			int[] m1 = models.get(k);
			for(int i = k+1; i < models.size(); i++) {
				System.out.print("1");
				int[] m2 = models.get(i);
				if(agreement[k][i] == 0 && agreement[i][k] == 0) {
					int[] agree = SymmetryUtil.getAgreement(m1,m2);
					int[] combs = new int[agree.length];
					
					for(int j = 0; j < agree.length; j++) {
						if(agree[j] != 0) {
							combs[j] = 2;
						} else {
							combs[j] = 1;
						}
					}
					
					PartitionIterator iter = new PartitionIterator(combs);
					
					while(iter.hasNext()) {
//						if(rand.nextInt(10000000) == 0) {
//							System.out.print("2");
//						}
						int[] next = iter.next();
						int[] filter = new int[next.length];
						
						int num = 0;
						for(int j = 0; j < next.length; j++) {
							if(next[j] == 1) {
								num++;
								filter[j] = agree[j];
							}
							
						}
					
						boolean localSym = SymmetryUtil.getLocalSymAgreement(dnfForSym.getContext(),models,m1,m2,filter);
						
						if(localSym) {
							agreement[k][i] = 1;
							agreement[i][k] = 1;
							System.out.println();
							break;
						}
					}
				}
			}
		}
		System.out.println();
		
		for(short[] row : agreement) {
			System.out.println(Arrays.toString(row));
		}
	}

}
