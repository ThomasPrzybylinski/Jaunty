package task.sat;

import hornGLB.BasicAssignIter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import formula.VariableContext;
import formula.simple.DNF;

//Only works well for small variable sizes
public class DNFSAT {
	
	public static List<int[]> getAllModels(DNF dnf) {
		VariableContext context = dnf.getContext();
		
		BasicAssignIter iter = new BasicAssignIter(context.size());
		
		LinkedList<int[]> ret = new LinkedList<int[]>();
		
		while(iter.hasNext()) {
			int[] assign = iter.next();
			
			boolean ok = false;
			for(int[] clause : dnf.getClauses()) {
				if(matches(assign,clause)) {
					ok = true;
					break;
				}
			}
			
			if(ok) {
				int[] model = new int[assign.length];
				for(int k = 0; k < assign.length; k++) {
					model[k] = (k+1) * (assign[k] > 0 ? 1 : -1);
				}
				ret.add(model);
			}
			
		}
		
		return ret;
		
	}
	
	private static boolean matches(int[] assign, int[] clause) {
		for(int i : clause) {
			int var = Math.abs(i);
			int sign = i > 0 ? 1 : 0;
			
			if(assign[var-1] != sign) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		DNF dnf = new DNF(VariableContext.defaultContext);
		dnf.addClause(1,2,3);
		dnf.addClause(-3,-4);
		for(int[] model : getAllModels(dnf)) {
			System.out.println(Arrays.toString(model));
		}
	}
}
