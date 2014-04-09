import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.List;

import task.formula.LineColoringCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.symmetry.RealSymFinder;
import task.symmetry.SHATTERSymFinder;
import util.lit.DirectedLitGraph;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;


public class TempTests1 {

	public TempTests1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DirectedLitGraph graph = new DirectedLitGraph(4);
		
		List<LiteralPermutation> gens = new ArrayList<LiteralPermutation>();
		
		gens.add(new LiteralPermutation(0,2,1,3,4));
		
		NaiveLiteralGroup nlg = new NaiveLiteralGroup(gens);
		SchreierVector vec = new SchreierVector(nlg);
		
		int numClauses = 4;//24;
		int numUnique = 16;//10;//(int)Math.pow(2,numClauses);
		ModelGiver giver =new SmallAllModelBoolFormula(13,2048*2,2); //new CNFCreatorModelGiver(new LineColoringCreator(3,3));// SmallAllModelBoolFormula(numUnique,numClauses,2);
		List<int[]> models = giver.getAllModels(VariableContext.defaultContext);
		
//		if(k != 191) continue;
		
//		models = getUniqueVars(models);
		
//		ClauseList orig = new ClauseList(VariableContext.defaultContext);
//		orig.addAll(models);
//		
//		
//		SHATTERSymFinder finder = new SHATTERSymFinder(orig);
//		System.out.println(finder.getSymGroup());
//		
//		System.out.println();
//		
//		RealSymFinder finder2 = new RealSymFinder(orig);
//		System.out.println(finder.getSymGroup());
		
//		graph.push(vec);
//		
//		
//		gens = new ArrayList<LiteralPermutation>();
//		gens.add(new LiteralPermutation(0,1,3,4,2));
//		nlg = new NaiveLiteralGroup(gens);
//		vec = new SchreierVector(nlg);
//		
//		graph.push(vec);
//		
//		
//		for(int k = 3; k <= 4; k++) {
//			System.out.println(graph.isValidMapping(1,k));
//		}

	}

}
