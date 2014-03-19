import task.formula.random.SimpleCNFCreator;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.ConstructionSymAddr;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;

public class ModelsAsListeralListVsSetTest {

	public static void main(String[] args) throws Exception {

		int numVars = 5;
		int factorial = 120;



		ModelGiver mg = new CNFCreatorModelGiver(new SimpleCNFCreator(numVars,3.7,3)); //new LineColoringCreator(3,3));
		ClauseList models = new ClauseList(VariableContext.defaultContext);
		models.addAll(mg.getAllModels(VariableContext.defaultContext));

		System.out.println(models);

		int numListVars = numVars*numVars;

		VariableContext listContext = new VariableContext();

		for(int k = 0; k < numListVars; k++) {
			int origVar = (k%numVars)+1;
			int varOrder = (k/numVars);
			listContext.getOrCreateVar(origVar + "_" + varOrder);
		}


		ClauseList lists = new ClauseList(listContext);
		for(int[] model : models.getClauses()) {
			PermutationGenerator gen = new PermutationGenerator(numVars);

			while(gen.hasMore()) {
				int[] order = gen.getNext();

				int[] toAdd = new int[model.length];

				for(int k = 0; k < order.length; k++) {
					int var = order[k]+1;
					toAdd[k] = k*numVars + var;
					if(model[var-1] < 0) toAdd[k] = -toAdd[k];
				}

				lists.fastAddClause(toAdd);
			}
		}


		//		for(int[] model : lists.getClauses()) {
		//			for(int i : model) {
		//				System.out.print(listContext.getVar(i).toString() + " ");
		//			}
		//			System.out.println();
		//		}

		LocalSymClauses clauses = new LocalSymClauses(models, false);
		RealSymFinder finder = new RealSymFinder(models);
		LiteralGroup setVarGroup = finder.getSymGroup();
		LiteralGroup setGroup = clauses.getModelGroup(setVarGroup).reduce();
		SchreierVector vec = new SchreierVector(setGroup);

		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				if(vec.sameOrbit(k+1,i+1)) {
					System.out.println((k+1) +  "-" + (i+1));
				}
			}
		}


		System.out.println("Next:");

		clauses = new LocalSymClauses(lists, false);
		finder = new RealSymFinder(lists);
		LiteralGroup listVarGroup = finder.getSymGroup();
		LiteralGroup listGroup = clauses.getModelGroup(listVarGroup).reduce();
		vec = new SchreierVector(listGroup);

		for(int k = 0; k < models.size(); k++) {
			la: for(int i = k+1; i < models.size(); i++) {
				for(int kIndex = 0; kIndex < factorial; kIndex++) {
					for(int iIndex = 0; iIndex < factorial; iIndex++) {
						if(vec.sameOrbit(k*factorial+kIndex+1,i*factorial+iIndex+1)) {
							System.out.println((k+1) +  "-" + (i+1));
							break la;
						}

					}
				}
			}
		}
		
		System.out.println("Next:");

		EdgeManipulator e1 = new ConstructionSymAddr(true,false,true,false);
		
		PossiblyDenseGraph<int[]> g1 = new PossiblyDenseGraph<int[]>(models.getClauses());
		
		e1.addEdges(g1,models);
		
		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				if(g1.areAdjacent(k,i)) {
					System.out.println((k+1) +  "-" + (i+1));
				}
			}
		}

		System.out.println("Groups:");
		System.out.println(setVarGroup);
		System.out.println();
		System.out.println(listVarGroup.toString(listContext));
		
		

	}

}
