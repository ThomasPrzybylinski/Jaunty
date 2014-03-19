import java.util.Arrays;
import java.util.List;

import task.formula.QueensToSAT;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.SchreierVector;


public class PruneTests {

	static int numNodes = 0;
	public static void main(String[] args) throws Exception {
		ModelGiver giver = new CNFCreatorModelGiver(new QueensToSAT(8)); //new AllSquares(4);
		VariableContext var = new VariableContext();
		List<int[]> models = giver.getAllModels(var);
		
		
//		var = new VariableContext();
		
		ClauseList orig = new ClauseList(var);
		orig.addAll(models);
		
		
//		orig.addClause(1,2,3);
//		orig.addClause(-1,-2,-3);
		
		LocalModelSymClauses clauses = new LocalModelSymClauses(orig);
		
		RealSymFinder finder = new RealSymFinder(orig);
		LiteralGroup group = finder.getSymGroup(); 
		System.out.println(group);
		System.out.println((new SchreierVector(group)).transcribeOrbits());
		
		//[-5, -9, -11]
		
		System.out.println(Arrays.toString(finder.getSmallerSubsetIfPossible(new int[]{2,13},group)));
		
		for(int k = 1; k <= var.size(); k++) {
			int[] test = new int[]{-k};
			System.out.println(Arrays.toString(test));
			int[] other = finder.getSmallerSubsetIfPossible(test,group);
			System.out.println(Arrays.toString(other));
			System.out.println();
			
			for(int i = k+1; i <= var.size(); i++) {
				test = new int[]{k,i};
				System.out.println(Arrays.toString(test));
				other = finder.getSmallerSubsetIfPossible(test,group);
				System.out.println(Arrays.toString(other));
				System.out.println();
			}
		}
		
//		biTraverse(clauses,var.size(),0, new int[]{},finder,group);
		
		System.out.println(numNodes);

	}
	private static void biTraverse(LocalModelSymClauses clauses, int size, int i, int[] filter, RealSymFinder finder,LiteralGroup group) {
		numNodes++;
		System.out.println(Arrays.toString(filter));
		
		if(i >= group.size()) return;
		
		int[] newFilter = new int[filter.length+1];
		System.arraycopy(filter,0,newFilter,0,filter.length);
		newFilter[newFilter.length-1] = i+1;
		clauses.post();
		clauses.addCondition(i+1);
		
		
		if(clauses.curValidModels() > 0 && finder.getSmallerSubsetIfPossible(newFilter,group) == null) {
			biTraverse(clauses,size,i+1,newFilter,finder,group);
		}
		clauses.pop();
		
		newFilter[newFilter.length-1] = -(i+1);
		clauses.post();
		clauses.addCondition(-(i+1));
		
		if(clauses.curValidModels() > 0 && finder.getSmallerSubsetIfPossible(newFilter,group) == null) {
			biTraverse(clauses,size,i+1,newFilter,finder,group);
		}
		clauses.pop();
		
	}

}
