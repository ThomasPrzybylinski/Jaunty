package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.SchreierVector;
import task.formula.LineColoringCreator;
import task.symmetry.local.ConstructiveSymHelper;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.IntPair;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;

public class ConstructiveParitionPrototype {

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
		ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(3,4));
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1}}));
		//ModelGiver modelGiver = new AllFilledSquares(3);
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5));
		//ModelGiver modelGiver = new PrototypeModelGiver();
		
		
		List<int[]> curModels = modelGiver.getAllModels(curContext); 

		Collections.sort(curModels, new MILEComparator());
		
		ClauseList models = new ClauseList(curContext);
		models.fastAddAll(curModels);
		//models.sort();

		//ChoiceGetter choice = new PositiveChoices();
		//ChoiceGetter choice = new NotImpliedChoices();
		//ChoiceGetter choice = new CustomChoices(new int[] {2,4,-2,-4});
		//choice.computeChoices(models);
		//models = choice.getList(models);
		
		ConstructiveSymHelper curConstruction = new ConstructiveSymHelper(models);
		IntegralDisjointSet origModelOrbits = curConstruction.getCumulativeModelOrbits();
		IntegralDisjointSet coarseAssoc = new IntegralDisjointSet(origModelOrbits);
		TreeSet<IntPair> directedEdges = new TreeSet<IntPair>();
		LocalSymClauses clauses = curConstruction.getLocalClauses();
		
		//Only use clauses as source of truth from now on
		models = null;
		curModels = null;
		
		
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
		
		
	
	
		for(int k = 0; k < clauses.numTotalModels() && k <= maxTest1; k++) {
			if(origModelOrbits.getLeastEltInSet(k+1) != (k+1)) continue;
			for(int i = 0; i < clauses.numTotalModels() && i <= maxTest2; i++) {
				//{{ int k = 1; int i = 17;
				if(!origModelOrbits.sameSet(k+1,i+1) && origModelOrbits.getLeastEltInSet(i+1) == (i+1)) {
					if(printLevel >= 2) {
						System.out.println("Current Test Models: ("+(k+1)+", "+(i+1)+")");
					}

					//Check if it's even possible to combine: every literal in i+1 must be in some symmetry of k+1
					int[] m1Lits = clauses.getClauseAtTranslated(k,true);

					boolean possiblyIdentical = true;


					if(printLevel >= 4) {
						System.out.println(Arrays.toString(m1Lits));
						System.out.println(possiblyIdentical);
					}


					int[] searchLits = getValidLiterals(curConstruction,k+1,i+1);
					
					if(searchLits.length == 0) continue;
					
					if(printLevel >= 3) System.out.println("Model2:" + Arrays.toString(clauses.getClauseAtTranslated(i,true)));
					for(int l : searchLits) {

						possiblyIdentical = possiblyIdentical && checkNextLevel(l,curConstruction,k+1,i+1);

						if(printLevel >= 3) {
							System.out.println(l);
							System.out.println(possiblyIdentical);
							System.out.println();
						}

						if(!possiblyIdentical) break;
					}

					if(possiblyIdentical) {
						if(printLevel >= 1) {
							System.out.println((k+1) + " maps to " + (i+1));

							System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(clauses.getClauseAtTranslated(k,true)));
							System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(clauses.getClauseAtTranslated(i,true)));
						}

						coarseAssoc.join(k+1,i+1);

						for(Integer globIdenticalModelM1 : origModelOrbits.getSetWith(k+1)) {
							for(Integer globIdenticalModelM2 : origModelOrbits.getSetWith(i+1)) {
								directedEdges.add(new IntPair(globIdenticalModelM1,globIdenticalModelM2));
							}
						}
					}
				}
			}
		}

		
		
		
		
		List<IntPair> finalPairsGlobal = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if((origModelOrbits.getRootOf(pair.getI1()) == pair.getI1())
					&& (origModelOrbits.getRootOf(pair.getI2()) == pair.getI2())) {
				finalPairsGlobal.add(pair);
			}
		}
		
		
		List<IntPair> newPairs = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if(!origModelOrbits.sameSet(pair.getI1(),pair.getI2())) {
				newPairs.add(pair);
			}
		}
		
		
		
		List<IntPair> newPairsGlobal = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if(!origModelOrbits.sameSet(pair.getI1(),pair.getI2()) 
					&& (origModelOrbits.getRootOf(pair.getI1()) == pair.getI1())
					&& (origModelOrbits.getRootOf(pair.getI2()) == pair.getI2())) {
				newPairsGlobal.add(pair);
			}
		}
		
		System.out.println("Original    : " + origModelOrbits);
		System.out.println("Coarse New  : " + coarseAssoc);
		System.out.println("Final       : " + directedEdges);
		System.out.println("Additional : " + newPairs);
		System.out.println("Final (Global Rep Only): " + finalPairsGlobal);
		System.out.println("Additional (Global Rep Only): " + newPairsGlobal);
	}
	
	private static boolean checkNextLevel(int l,ConstructiveSymHelper curConstruction, int m1Index, int m2Index) {
		boolean branchIdentical = true;
		
		curConstruction.addLayer(l);
		IntegralDisjointSet curModelOrbits = curConstruction.getCumulativeModelOrbits();
		IntegralDisjointSet globalModelOrbits = curConstruction.getModelOrbitsAtLevel(0);
		
		LocalSymClauses repr = curConstruction.getLocalClauses();
		
		
		boolean inOrbit = curModelOrbits.sameSet(m1Index,m2Index);
		boolean possiblyIdentical = true;
		
		if(printLevel >= 3) {
			System.out.println();
			System.out.println("Checking lit: " + l);
			System.out.println("Rep:          " + repr.getCurrentCondition());
			System.out.println("Local Models: " +repr);
			System.out.println("Translation:" + Arrays.toString(repr.getCurModelTranslation()));

			System.out.println("Syms: " + curConstruction.getCurrentLitGroup());
			System.out.println("Orbits: " + curModelOrbits);
			System.out.println("In Orbit: " + inOrbit);
			System.out.println("possiblyIdentical: " + possiblyIdentical);
			System.out.println();
	}
		
		if(inOrbit) {
			branchIdentical = true;
		} else {
			int[] litsToSearch = getValidLiterals(curConstruction,m1Index,m2Index);
			if(printLevel >  2) System.out.println("Lits to check: " + Arrays.toString(litsToSearch));
			
			if(litsToSearch.length == 0) {
				branchIdentical = false;
			} else {

				for(int lit : litsToSearch) {
					if(printLevel > 2) System.out.println(l);

					branchIdentical = branchIdentical && checkNextLevel(lit,curConstruction,m1Index,m2Index);

					if(printLevel >= 3) {
						System.out.println("Ok so far: " + branchIdentical);
					}
					if(!branchIdentical) break;

				}
			}
		} 

		if(printLevel > 2) System.out.println(branchIdentical + " " + repr.getCurrentCondition());
		curConstruction.pop();

		return branchIdentical;
	}

	public static int[] getValidLiterals(ConstructiveSymHelper curConstruction, int m1Index, int m2Index) {
		LocalSymClauses repr = curConstruction.getLocalClauses();
		int[] invalid = repr.getCanonicalInter(new int[] {});
		IntHashMap<Object> invalidLits = new IntHashMap<Object>();
		for(int i : invalid) {
			invalidLits.put(i,null);
		}
		
		int[] curM1 = repr.getClauseAtTranslated(m1Index-1,false);
		
		
		IntegralDisjointSet set = curConstruction.getCumulativeModelOrbits();
		IntHashMap m2Lits = new IntHashMap();
		
		for(Integer m2RelatedModel : set.getSetWith(m2Index)) {
			if(repr.isClauseValid(m2RelatedModel-1)) {
				for(int i : repr.getClauseAtTranslated(m2RelatedModel-1,false)) {
					m2Lits.put(i,null);
				}
			}
		}
		
		ArrayIntList retList = new ArrayIntList(curM1.length);
		SchreierVector vec = new SchreierVector(curConstruction.getCurrentLitGroup());
		IntegralDisjointSet curVarOrbits = vec.transcribeOrbits();
		
		if(printLevel > 2) {
			System.out.println("Test Orbits: " + curVarOrbits);
			System.out.println("Inval Lits : " + invalidLits);
			System.out.println("M2Lits     : " + m2Lits);
			System.out.println("M1         : " + Arrays.toString(curM1));
		}
		
		for(int lit : curM1) {
			List<Integer> otherPossibilities = curVarOrbits.getSetWith(lit);
			boolean smallerSymPossibilityExists = false;
			for(Integer orbitLit : otherPossibilities) {
				if(orbitLit.intValue() < lit && m2Lits.contains(orbitLit.intValue()) && !invalidLits.contains(orbitLit.intValue())) {
					for(int otherLit : curM1) {
						if(otherLit == orbitLit.intValue()) smallerSymPossibilityExists = true;
					}
				}
				if(smallerSymPossibilityExists) break;
			}
			
			if(!smallerSymPossibilityExists && m2Lits.contains(lit) && !invalidLits.contains(lit)) {
				retList.add(lit);
			}
		}
		
		return retList.toArray();
		
	}
	
	 
}
