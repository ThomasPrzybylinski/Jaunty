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
		ClauseList cl = new ClauseList(VariableContext.defaultContext);
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
