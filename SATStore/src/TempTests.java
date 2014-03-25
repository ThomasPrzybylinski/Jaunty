import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import task.formula.random.SmallAllModelBoolFormula;
import util.IntegralDisjointSet;
import workflow.ModelGiver;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;


public class TempTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numClauses = 4;//24;
		int numUnique = 16;//10;//(int)Math.pow(2,numClauses);
		ModelGiver giver = new SmallAllModelBoolFormula(numUnique,numClauses,2);
		AllLocalSymAddr addr = new AllLocalSymAddr(false,false,true,false);
		AgreementLocalSymAdder agree = new AgreementLocalSymAdder();
		GlobalSymmetryEdges glob = new GlobalSymmetryEdges();

		VariableContext var = new VariableContext();
		for(int k = 0; k < 16777216*2; k++) {
			if(k%1000 == 0) {
				System.out.println(k);
			}
			
			

			List<int[]> models = giver.getAllModels(var);
			
//			if(k != 191) continue;
			
			models = getUniqueVars(models);
			
			ClauseList orig = new ClauseList(var);
			orig.addAll(models);

			PossiblyDenseGraph<int[]> pdg1 = new PossiblyDenseGraph<int[]>(orig.getClauses());

			addr.addEdges(pdg1,orig);

			int num1 = pdg1.numEdges();

			PossiblyDenseGraph<int[]> pdg2 = new PossiblyDenseGraph<int[]>(orig.getClauses());
			glob.addEdges(pdg2,orig);
			agree.addEdges(pdg2,orig);

			int num2 = pdg2.numEdges();

			if(num1 != num2) {
				System.out.println(k);
				System.out.println(orig);
				System.out.println(num1);
				System.out.println(num2);
				System.out.println(pdg1.compareTo(pdg2));
				System.out.println(pdg2.compareTo(pdg1));
				break;
			}

		}
	}

	private static List<int[]> getUniqueVars(List<int[]> models) {
		int[] rep = models.get(0);
		String[] varRep = new String[rep.length];

		for(int k = 0; k < rep.length; k++) {
			StringBuilder sb = new StringBuilder();
			for(int[] m : models) {
				if(m[k] > 0) {
					sb.append('1');
				} else {
					sb.append('0');
				}
			}

			varRep[k] = sb.toString();
		}

		IntegralDisjointSet ids = new IntegralDisjointSet(0,rep.length-1);

		for(int k = 0; k < rep.length; k++) {
			if(ids.getLeastEltInSet(k) == k) {
				for(int i = k+1; i < rep.length; i++) {
					if(varRep[k].equals(varRep[i])) {
						ids.join(k,i);
					}
				}
			}
		}
		
		Set<Integer> roots = ids.getRoots();
		int newSizes = roots.size();
		
		ArrayList<int[]> ret = new ArrayList<int[]>(models.size());
		
		for(int[] i : models) {
			int[] put = new int[newSizes];
			
			int index = 0;
			for(int varInd : roots) {
				put[index] = i[varInd];
				index++;
			}
			
			ret.add(put);
		}
		
		return ret;
	}



}
