import java.util.LinkedList;
import java.util.List;

import task.formula.AllSquares;
import task.formula.LineColoringCreator;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import task.symmetry.local.LocalSymClauses;
import util.PartitionIterator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;



//To Compare different ways of pruning
public class PruneTests3 {

	public static void main(String[] args) throws Exception {
		ModelGiver giver = new CNFCreatorModelGiver(new LineColoringCreator(6,3));//new AllSquares(4);//new CNFCreatorModelGiver(new QueensToSAT(8)); //
		VariableContext var = new VariableContext();
		List<int[]> models = giver.getAllModels(var);
		
		ClauseList orig = new ClauseList(var);
		orig.addAll(models);
		
		LocalModelSymClauses modelClause = new LocalModelSymClauses(orig);
		LocalSymClauses clause = new LocalSymClauses(orig);
		
		PartitionIterator parts = new PartitionIterator(var.size(),3);
		
		LinkedList<Integer> cur = new LinkedList<Integer>();
		
		modelClause.post();
		clause.post();
		cur.clear();

		//[-1, -10, -11] missing from better, as it should
		cur.add(-1);
		cur.add(-10);
//		cur.add(-11);
		
		setConditions(modelClause,clause,cur);
		test(modelClause, clause, cur);
		
		modelClause.pop();
		clause.pop();
	
//		while(parts.hasNext()) {
//			int[] next = parts.next();
//			
//			modelClause.post();
//			clause.post();
//			cur.clear();
//			
//			for(int k = 0; k < next.length; k++) {
//				if(next[k] == 1) {
//					cur.add(k+1);
//				} else if(next[k] == 2) {
//					cur.add(-(k+1));
//				}
//			}
//			
//			setConditions(modelClause, clause, cur);
//			
//			test(modelClause, clause, cur);
//			
//			modelClause.pop();
//			clause.pop();
//			
//		}
	}

	private static void setConditions(LocalModelSymClauses modelClause,
			LocalSymClauses clause, LinkedList<Integer> cur) {
		for(int k : cur) {
			modelClause.addCondition(k);
			clause.addCondition(k);
		}
	}

	private static void test(LocalModelSymClauses modelClause,
			LocalSymClauses clause, LinkedList<Integer> cur) {
		ClauseList c1 = modelClause.getCurList(true);
		ClauseList c2 = clause.getCurList(true);
		
		System.out.println(c1);
		System.out.println(c2);
		
		RealSymFinder sym1 = new RealSymFinder(c1);
		RealSymFinder sym2 = new RealSymFinder(c2);
		
		LiteralGroup g1 = sym1.getSymGroup();
		g1 = getVarGroup(modelClause,g1);
		LiteralGroup g2 = sym2.getSymGroup();
		
		System.out.println(cur);
		System.out.println(g1);
		System.out.println();
		System.out.println(g2);
		System.out.println();
		System.out.println(new SchreierVector(g1).transcribeOrbits());
//		System.out.println();
		System.out.println(new SchreierVector(g2).transcribeOrbits());
		System.out.println();
	}
	
	private static LiteralGroup getVarGroup(LocalModelSymClauses representatives,
			LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			gens.add(representatives.getVarPart(perm));
		}

		return group.getNewInstance(gens);

	}

}
