package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.ReducedLatinSquareCreator;
import task.formula.plan.BlocksWorldDeconstruct;
import task.symmetry.RealSymFinder;
import task.symmetry.local.ConstructiveSymHelper;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.IntPair;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.IntLinkedHashMap;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.local.ChoiceGetter;
import workflow.graph.local.CustomChoices;
import workflow.graph.local.NegativeChoices;
import workflow.graph.local.NotImpliedChoices;
import workflow.graph.local.PositiveChoices;

public class ConstructiveDominationPrototype2 {

	private static int printLevel = 2;
	private static int maxTest1 = Integer.MAX_VALUE;//0;//
	private static int maxTest2 = Integer.MAX_VALUE;//1;//
	
	private static class PrototypeModelGiver implements ModelGiver {

		@Override
		public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
			context.ensureSize(4);
			int[][]  mods = new int[][] { {1,-2,3,4},{1,2,-3,4},{1,2,3,-4},{-1,-2,3,-4}};
			ArrayList<int[]> list = new ArrayList<int[]>(4);
			for(int[] mod : mods) {
				list.add(mod);
			}
			return list;
		}

		@Override
		public ConsoleDecodeable getConsoleDecoder() {
			return new DefaultConsoleDecoder();
		}

		@Override
		public FileDecodable getFileDecodabler() {
			return null;			
		}

		@Override
		public String getDirName() {
			return "PrototypeConsEquiv";
		}
		
	}
	
	public static void main(String[] args) throws TimeoutException {
		VariableContext curContext = new VariableContext();
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(8));
		//ModelGiver modelGiver = new AllFilledRectangles(4);
		//ModelGiver modelGiver = new AllFilledSquares(5);
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5));
		ModelGiver modelGiver = new AllFilledSquares(4);
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(3,3));
		//ModelGiver modelGiver = new PrototypeModelGiver();
		
		
		List<int[]> curModels = modelGiver.getAllModels(curContext); 

		Collections.sort(curModels, new MILEComparator());
		
		ClauseList models = new ClauseList(curContext);
		models.fastAddAll(curModels);

		//ChoiceGetter choice = new PositiveChoices();
		//ChoiceGetter choice = new NotImpliedChoices();
		//ChoiceGetter choice = new CustomChoices(new int[] {-1,-2,-3,-4,1});
		//choice.computeChoices(models);
		//models = choice.getList(models);
		
		ConstructiveSymHelper curConstruction = new ConstructiveSymHelper(models);
		IntegralDisjointSet origModelOrbits = curConstruction.getCumulativeModelOrbits();
		TreeSet<IntPair> directedEdges = new TreeSet<IntPair>();
		
		
		for(Integer i : origModelOrbits.getRoots()) {
			for(Integer k : origModelOrbits.getSetWith(i.intValue())) {
				if(i.intValue() != k.intValue()) {
					directedEdges.add(new IntPair(i.intValue(),k.intValue()));
					directedEdges.add(new IntPair(k.intValue(),i.intValue()));
				}
			}
		}
		
		
		
		if(printLevel >= 1) {
			System.out.println("Models: " + curConstruction.getLocalSymClauseRep());
			if(printLevel >= 2) {
				System.out.println("Var Group: " + curConstruction.getCurrentLitGroup());
			}
			System.out.println("Model Orbits: " + origModelOrbits);
		}
		
		
	
	
		for(int k = 0; k < models.size() && k <= maxTest1; k++) {
			if(origModelOrbits.getLeastEltInSet(k+1) != (k+1)) continue;
			for(int i = 0; i < models.size() && i <= maxTest2; i++) {
				if(!origModelOrbits.sameSet(k+1,i+1) && origModelOrbits.getLeastEltInSet(i+1) == (i+1)) {
					if(printLevel >= 2) {
						System.out.println("Current Test Models: ("+(k+1)+", "+(i+1)+")");
					}
					
					//Check if it's even possible to combine: every literal in i+1 must be in some symmetry of k+1
					int[] m1Lits = models.getClauses().get(k);
					
					boolean possiblyIdentical = true;

					
					if(printLevel >= 4) {
						System.out.println(Arrays.toString(m1Lits));
						System.out.println(possiblyIdentical);
					}
					
					if(possiblyIdentical) {
					
						if(printLevel >= 3) System.out.println("Model2:" + Arrays.toString(curModels.get(i)));
						for(int l : m1Lits) {
							//if(printLevel >= 3) System.out.println(l);
							possiblyIdentical = checkNextLevel(l,curConstruction,k+1,i+1);
							if(!possiblyIdentical) break;
						}
						
						if(possiblyIdentical) {
							if(printLevel >= 1) {
								System.out.println("Joined: " + (k+1) + " and " + (i+1));
								
								System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(curModels.get(k)));
								System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(curModels.get(i)));
							}
							
							for(Integer globIdenticalModelM1 : origModelOrbits.getSetWith(k+1)) {
								for(Integer globIdenticalModelM2 : origModelOrbits.getSetWith(i+1)) {
									directedEdges.add(new IntPair(globIdenticalModelM1,globIdenticalModelM2));
								}
							}
							
						}
					}
				}
			}
		}
		
		
		System.out.println("Original: " + origModelOrbits);
		System.out.println("Final   : " + directedEdges);
		
		List<IntPair> newPairs = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if(!origModelOrbits.sameSet(pair.getI1(),pair.getI2())) {
				newPairs.add(pair);
			}
		}
		System.out.println("Additional   : " + newPairs);
	}

	private static boolean checkNextLevel(int l,ConstructiveSymHelper curConstruction, int m1Index, int m2Index) {
		boolean branchIdentical = false;
		
		curConstruction.addLayer(l);
		IntegralDisjointSet curModelOrbits = curConstruction.getCumulativeModelOrbits();
		LocalSymClauses repr = curConstruction.getLocalClauses();
		
		
		boolean inOrbit = curModelOrbits.sameSet(m1Index,m2Index);
		boolean possiblyIdentical = true;
		
		if(!inOrbit) {
			List<Integer> m2Orbit = curModelOrbits.getSetWith(m2Index);
			boolean someM2Exists = false;
			for(Integer m2Model : m2Orbit) {
				someM2Exists = repr.isClauseValid(m2Model.intValue()-1);
				if(someM2Exists) break;
			}
			
			if(!someM2Exists) possiblyIdentical=false;
		}
		
		
		
		if(printLevel >= 3) {
			System.out.println("Checking lit: " + l);
			System.out.println("Rep:          " + repr.getCurrentCondition());
			System.out.println("Local Models: " +repr);
			System.out.println("Translation:" + Arrays.toString(repr.getCurModelTranslation()));

			System.out.println("Syms: " + curConstruction.getCurrentLitGroup());
			System.out.println("Orbits: " + curModelOrbits);
			System.out.println();
	}
		
		if(inOrbit) {
			branchIdentical=true;
		} else if(possiblyIdentical) {
			for(int lit : repr.getClauseAtTranslated(m1Index-1,false)) {
				if(printLevel > 2) System.out.println(l);
				
				branchIdentical = checkNextLevel(lit,curConstruction,m1Index,m2Index);
				if(!branchIdentical) break;
			}
		}

		if(printLevel > 2) System.out.println(branchIdentical + " " + repr.getCurrentCondition());
		curConstruction.pop();

		return branchIdentical;
	}

}
