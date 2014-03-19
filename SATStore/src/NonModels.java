import java.util.Arrays;

import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.SimpleCNFCreator;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import workflow.CNFCreatorModelGiver;
import workflow.EclecWorkflow;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.ConstructionSymAddr;
import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Not;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import formula.simple.DNF;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;

public class NonModels {

	public static void main(String[] args) throws Exception {

		int numVars = 5;
		int factorial = 120;

		
		LineColoringCreator sat = new LineColoringCreator(3,3);
		
		CNF cnf = sat.generateCNF(VariableContext.defaultContext);
		
		Conjunctions c = cnf.toConjunction();
		BoolFormula nonModels = (new Not(c)).reduce().toNNF().toCNF();
		CNF cnfNonModels = new CNF(((Conjunctions) nonModels)).reduce();
		
//		ModelGiver mg = new CNFCreatorModelGiver(new SimpleCNFCreator(numVars,3.7,3)); //new LineColoringCreator(3,3));
	
		ClauseList models = cnfNonModels;//new ClauseList(VariableContext.defaultContext);
		
		//models.addAll(mg.getAllModels(VariableContext.defaultContext));

		System.out.println(models);

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
		
	}

}
