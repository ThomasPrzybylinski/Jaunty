package workflow.tests.prototypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import task.formula.AllFilledSquares;
import task.symmetry.local.ConstructiveSymHelper;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.IntPair;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.MILEComparator;
import workflow.ModelGiver;

public class ConstructiveIdenticalPrototype2 {

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
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new LineColoringCreator(4,4));
		//ModelGiver modelGiver = new CNFCreatorModelGiver(new BlocksWorldDeconstruct(new int[][]{{0,1,2},{0,1}}));
		ModelGiver modelGiver = new AllFilledSquares(4);
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
					
					if(possiblyIdentical) {
					
						ReturnVal retVal = null;
						boolean mappedSomewhere = false;
						if(printLevel >= 3) System.out.println("Model2:" + Arrays.toString(clauses.getClauseAtTranslated(i,true)));
						for(int l : m1Lits) {
							
							retVal = checkNextLevel(l,curConstruction,k+1,i+1);
							possiblyIdentical = retVal.IsOk;
							mappedSomewhere = mappedSomewhere || retVal.M1M2MappedSomewhere;
							
							if(printLevel >= 3) {
								System.out.println(l);
								System.out.println(retVal + " " + possiblyIdentical + " " + mappedSomewhere);
								System.out.println();
							}
							
							if(!possiblyIdentical) break;
						}
						
						if(possiblyIdentical && mappedSomewhere) {
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
		}
		
		
		System.out.println("Original    : " + origModelOrbits);
		System.out.println("Coarse New  : " + coarseAssoc);
		System.out.println("Final       : " + directedEdges);
		
		List<IntPair> newPairs = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if(!origModelOrbits.sameSet(pair.getI1(),pair.getI2())) {
				newPairs.add(pair);
			}
		}
		System.out.println("Additional : " + newPairs);
		
		
		List<IntPair> newPairsGlobal = new LinkedList<IntPair>();
		
		for(IntPair pair : directedEdges) {
			if(!origModelOrbits.sameSet(pair.getI1(),pair.getI2()) 
					&& (origModelOrbits.getRootOf(pair.getI1()) == pair.getI1())
					&& (origModelOrbits.getRootOf(pair.getI2()) == pair.getI2())) {
				newPairsGlobal.add(pair);
			}
		}
		System.out.println("Additional (Global Rep Only): " + newPairsGlobal);
	}
	
	private static class ReturnVal {
		private boolean M1M2MappedSomewhere;
		private boolean IsOk;
		
		public ReturnVal(boolean m1m2MappedSomewhere, boolean isOk) {
			this.M1M2MappedSomewhere=m1m2MappedSomewhere;
			this.IsOk = isOk;
		}

		public boolean isM1M2MappedSomewhere() {
			return M1M2MappedSomewhere;
		}

		public void setM1M2MappedSomewhere(boolean m1m2MappedSomewhere) {
			M1M2MappedSomewhere = m1m2MappedSomewhere;
		}

		public boolean isIsOk() {
			return IsOk;
		}

		public void setIsOk(boolean isOk) {
			IsOk = isOk;
		}

		@Override
		public String toString() {
			return "IsOK : " + isIsOk() + "; Mapped Somewhere: "  + M1M2MappedSomewhere + ";";
		}
		
		
		
	}

	private static ReturnVal checkNextLevel(int l,ConstructiveSymHelper curConstruction, int m1Index, int m2Index) {
		boolean branchIdentical = false;
		
		curConstruction.addLayer(l);
		IntegralDisjointSet curModelOrbits = curConstruction.getCumulativeModelOrbits();
		IntegralDisjointSet globalModelOrbits = curConstruction.getModelOrbitsAtLevel(0);
		
		LocalSymClauses repr = curConstruction.getLocalClauses();
		
		
		boolean inOrbit = curModelOrbits.sameSet(m1Index,m2Index);
		boolean possiblyIdentical = true;
		boolean nonGlobalAssoc = false;
		boolean onlyGlobalM1 = true;
		
		if(!inOrbit) {
			List<Integer> m1Orbit = curModelOrbits.getSetWith(m1Index);
			for(int m1GlobalSymIndex : m1Orbit) {
				nonGlobalAssoc = !globalModelOrbits.sameSet(m1Index,m1GlobalSymIndex);
				if(nonGlobalAssoc) break;
			}
			
			for(int k = 0; k < repr.numTotalModels(); k++) {
				if(repr.isClauseValid(k) && !globalModelOrbits.sameSet(m1Index,k+1)) {
					onlyGlobalM1 = false;
					break;
				}
			}
			
			if(nonGlobalAssoc) {
				List<Integer> m2Orbit = curModelOrbits.getSetWith(m2Index);
				boolean someM2Exists = false;
				for(Integer m2Model : m2Orbit) {
					someM2Exists = repr.isClauseValid(m2Model.intValue()-1);
					if(someM2Exists) break;
				}
				
				if(!someM2Exists) possiblyIdentical=false;
			}
		}
		
		
		
		if(printLevel >= 3) {
			System.out.println("Checking lit: " + l);
			System.out.println("Rep:          " + repr.getCurrentCondition());
			System.out.println("Local Models: " +repr);
			System.out.println("Translation:" + Arrays.toString(repr.getCurModelTranslation()));

			System.out.println("Syms: " + curConstruction.getCurrentLitGroup());
			System.out.println("Orbits: " + curModelOrbits);
			System.out.println("In Orbit: " + inOrbit);
			System.out.println("possiblyIdentical: " + possiblyIdentical);
			System.out.println("nonGlobalAssoc: " + nonGlobalAssoc);
			System.out.println("onlyGlobalM1: " + onlyGlobalM1);
			System.out.println();
	}
		
		ReturnVal retVal;
		if(inOrbit) {
			retVal = new ReturnVal(true,true);
		} else if(possiblyIdentical) {
			retVal = new ReturnVal(false,true);
			if(onlyGlobalM1) {
				branchIdentical = !nonGlobalAssoc;
			} else {

				if(nonGlobalAssoc) {
					if(hasUmappableLiteral(curConstruction,m1Index,m2Index)) {
						retVal.IsOk = false;
					}
				}

				if(retVal.IsOk) {
					for(int lit : repr.getClauseAtTranslated(m1Index-1,false)) {
						if(printLevel > 2) System.out.println(l);

						ReturnVal lowerRetVal = checkNextLevel(lit,curConstruction,m1Index,m2Index);

						retVal.IsOk = lowerRetVal.IsOk;
						retVal.M1M2MappedSomewhere = retVal.M1M2MappedSomewhere || lowerRetVal.M1M2MappedSomewhere;

						if(printLevel >= 3) {
							System.out.println("Lower: " + lowerRetVal);
							System.out.println("Cur  : " + retVal);
						}
						if(!retVal.IsOk) break;
					}
				}
			}
		} else {
			retVal = new ReturnVal(false,false);
		}

		if(printLevel > 2) System.out.println(branchIdentical + " " + repr.getCurrentCondition());
		curConstruction.pop();

		return retVal;
	}

	private static boolean hasUmappableLiteral(ConstructiveSymHelper curConstruction, int m1Index, int m2Index) {
		LocalSymClauses repr = curConstruction.getLocalClauses();
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
		
		boolean hasUmappableLiteral = false;
		
		for(int m1Lit : curM1) {
			if(!m2Lits.contains(m1Lit)) {
				hasUmappableLiteral = true;
				break;
			}
		}
		
		return hasUmappableLiteral;
	}
	
	 
}
