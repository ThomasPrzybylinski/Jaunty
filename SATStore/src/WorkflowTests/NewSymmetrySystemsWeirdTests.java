package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.formula.AllFilledRectangles;
import task.formula.QueensToSAT;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import util.IntPair;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.IntLinkedHashMap;
import util.lit.IntToIntLinkedHashMap;
import util.lit.IntToIntLinkedHashMap.IntEntry;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.local.PositiveChoices;

//There probably won't work at all, but I like brainstorming

public class NewSymmetrySystemsWeirdTests {

	public static void main(String[] args) throws TimeoutException {
		VariableContext curContext = new VariableContext();
		ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(8));
		//ModelGiver modelGiver = new AllFilledRectangles(3);
		
		List<int[]> curModels = modelGiver.getAllModels(curContext); 

		Collections.sort(curModels, new MILEComparator());
		
		ClauseList models = new ClauseList(curContext);
		models.fastAddAll(curModels);

		PositiveChoices positiveChoice = new PositiveChoices();
		models = positiveChoice.getList(models);
	
		LocalSymClauses repr = new LocalSymClauses(models,false);

		RealSymFinder sym = new RealSymFinder(models);
		
		LiteralGroup literalGroup = sym.getSymGroup();
		LiteralGroup modelGroup = repr.getModelGroup(literalGroup);
		SchreierVector globSymOrbits = new SchreierVector(modelGroup);
		IntegralDisjointSet ModelPartition = globSymOrbits.transcribeOrbits(false);
		SchreierVector consEquivOrbits = new SchreierVector(literalGroup);
		IntegralDisjointSet calc = consEquivOrbits.transcribeOrbits(false);
		
		
		System.out.println(repr);
		System.out.println(calc);
		
		System.out.println(ModelPartition);
		System.out.println(modelGroup);
		System.out.println(literalGroup);
		

		TreeSet<IntPair> validPairs = new TreeSet<IntPair>();
		for(int k : ModelPartition.getRoots()) {
			for(int i : ModelPartition.getRoots()) {
				if(k < i) {
					validPairs.add(new IntPair(k,i));
				}
			}
		}
		
		for(Integer lit : repr.curValidLits()) {
			repr.post();
			repr.addCondition(lit.intValue());
			TreeMap<Integer,Integer> numParts = new TreeMap<Integer,Integer>();
			
			for(int k : ModelPartition.getRoots()) {
				numParts.put(Integer.valueOf(k),0);
			}
			
			for(int k = 0; k < repr.numTotalModels(); k++) {
				if(repr.isClauseValid(k)) {
					int globRep = ModelPartition.getLeastEltInSet(k+1);
					if(numParts.containsKey(Integer.valueOf(globRep))) {
						numParts.put(Integer.valueOf(globRep),numParts.get(Integer.valueOf(globRep))+1);
					}
				}
			}
			
			for(int k : ModelPartition.getRoots()) {
				for(int i : ModelPartition.getRoots()) {
					if(k < i && numParts.get(Integer.valueOf(k)).intValue() != numParts.get(Integer.valueOf(i)).intValue()) {
						validPairs.remove(new IntPair(k,i));
					}
				}
			}
			
			System.out.println("Constraint: " + lit);
			for(Integer globRep : numParts.keySet()) {
				System.out.println("[Rep " + globRep + ": " + numParts.get(globRep)+"] ");
			}
			
			System.out.println();
			System.out.println(validPairs);
			System.out.println();
			System.out.println();
			repr.pop();
		}

		System.out.println();
		System.out.println(validPairs);
		System.out.println();
		System.out.println();
		
		List<Integer> lits = calc.getSetWith(1);
		ClauseList cornerModels = new ClauseList(models.getContext());
		for(Integer lit : lits) {
			int litValue = lit.intValue();
			for(int[] model : curModels) {
				if(model[litValue-1]==litValue) {
					cornerModels.fastAddClause(model);
				}
			}
		}
		
		cornerModels = cornerModels.reduce();
		cornerModels.sort();
		
		//cornerModels = positiveChoice.getList(cornerModels);
		
		LocalSymClauses cornerRepr = new LocalSymClauses(cornerModels,false);

		sym = new RealSymFinder(cornerModels);
		
		literalGroup = sym.getSymGroup();
		modelGroup = cornerRepr.getModelGroup(literalGroup);
		globSymOrbits = new SchreierVector(modelGroup);
		ModelPartition = globSymOrbits.transcribeOrbits(false);
		consEquivOrbits = new SchreierVector(literalGroup);
		calc = consEquivOrbits.transcribeOrbits(false);
		
		System.out.println("Corner:");
		System.out.println(cornerRepr);
		System.out.println(ModelPartition);
		System.out.println(modelGroup.reduce());
		System.out.println(literalGroup);

	
	}
	

}
