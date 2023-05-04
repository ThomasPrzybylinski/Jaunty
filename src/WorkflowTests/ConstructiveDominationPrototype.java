package WorkflowTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.SchreierVector;
import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
import task.formula.QueensToSAT;
import task.formula.ReducedLatinSquareCreator;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.IntLinkedHashMap;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.local.ChoiceGetter;
import workflow.graph.local.NegativeChoices;
import workflow.graph.local.NotImpliedChoices;
import workflow.graph.local.PositiveChoices;

public class ConstructiveDominationPrototype {

	private static int printLevel = 5;
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
		ModelGiver modelGiver = new AllFilledRectangles(4);
		//ModelGiver modelGiver = new AllFilledSquares(5);
		//modelGiver = new CNFCreatorModelGiver(new ReducedLatinSquareCreator(5));
		//ModelGiver modelGiver = new PrototypeModelGiver();
		
		
		List<int[]> curModels = modelGiver.getAllModels(curContext); 

		Collections.sort(curModels, new MILEComparator());
		
		ClauseList models = new ClauseList(curContext);
		models.fastAddAll(curModels);

		//ChoiceGetter choice = new PositiveChoices();
		//ChoiceGetter choice = new NotImpliedChoices();
		//choice.computeChoices(models);
		//models = choice.getList(models);
	
		LocalSymClauses localSymModels = new LocalSymClauses(models,false);

		RealSymFinder sym = new RealSymFinder(models,true);
		
		LiteralGroup lg = sym.getSymGroup();
		LiteralGroup modelGroup = localSymModels.getModelGroup(lg);
		SchreierVector globModelVector = new SchreierVector(modelGroup);
		IntegralDisjointSet globModelOrbits = globModelVector.transcribeOrbits(false);
		IntegralDisjointSet modelOrbits = globModelVector.transcribeOrbits(false);
		IntegralDisjointSet orig = globModelVector.transcribeOrbits(false);
		
		
		if(printLevel >= 1) {
			System.out.println("Models: " + localSymModels);
			if(printLevel >= 2) {
				System.out.println("Var Group: " + lg);
			}
			System.out.println("Model Orbits: " + modelOrbits);
		}
	
	
		for(int k = 0; k < models.size() && k <= maxTest1; k++) {
			if(modelOrbits.getLeastEltInSet(k+1) != (k+1)) continue;
			for(int i = 0; i < models.size() && i <= maxTest2; i++) {
				if(!globModelOrbits.sameSet(k+1,i+1)) {
					if(printLevel >= 2) {
						System.out.println("Current Test Models: ("+(k+1)+", "+(i+1)+")");
					}
					
					//Check if it's even possible to combine: every literal in i+1 must be in some symmetry of k+1
					IntHashMap<Object> m1Lits = new IntHashMap<Object>(curContext.size());
					
					ClauseList m1Cl = new ClauseList(curContext);
					
					for(int j : globModelOrbits.getSetWith(k+1)) {
						m1Cl.addClause(models.getClauses().get(j-1));
						for(int l : models.getClauses().get(j-1)) {
							m1Lits.put(l,null);
						}
					}
					
					boolean possiblyIdentical = true;
					int[] m2 = models.getClauses().get(i);
					for(int l : m2) {
						if(!m1Lits.contains(l)) {
							possiblyIdentical=false;
							break;
						}
					}
					
					if(printLevel >= 4) {
						int[] m1ListOut = m1Lits.getKeys();
						Arrays.sort(m1ListOut);
						System.out.println(Arrays.toString(m1ListOut));
						System.out.println(possiblyIdentical);
					}
					
					if(possiblyIdentical) {
						ClauseList m2Cl = new ClauseList(curContext);
						m2Cl.addClause(m2);
						LocalSymClauses modM2 = new LocalSymClauses(m2Cl);
						
						LocalSymClauses modM1 = new LocalSymClauses(m1Cl);
						
						if(printLevel >= 3) System.out.println("Model2:" + Arrays.toString(m2));
						for(int l : m2) {
							//if(printLevel >= 3) System.out.println(l);
							possiblyIdentical = checkNextLevel(l,localSymModels,globModelOrbits.getSetWith(k+1),i+1,modM1,modM2);
							if(!possiblyIdentical) break;
						}
						
						if(possiblyIdentical) {
							if(printLevel >= 1) {
								System.out.println("Joined: " + (k+1) + " and " + (i+1));
								
								System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(curModels.get(k)));
								System.out.println(modelGiver.getConsoleDecoder().consoleDecoding(curModels.get(i)));
							}
							modelOrbits.join(k+1,i+1);
						}
					}
				}
			}
		}
		
		
		System.out.println("Original: " + orig);
		System.out.println("Final   : " + modelOrbits);
	}

	private static boolean checkNextLevel(int l, LocalSymClauses repr, List<Integer> m1Models, int m2Index, LocalSymClauses modM1, LocalSymClauses modM2) {
		boolean branchIdentical = false;
		
		repr.post();
		modM1.post();
		modM2.post();
		
		repr.addCondition(l);
		modM1.addCondition(l);
		modM2.addCondition(l);
		
		if(printLevel >= 3) {
			System.out.println("Checking lit: " + l);
			System.out.println("Rep:          " + repr.getCurrentCondition());
			System.out.println("Local Models: " +repr);
			System.out.println("Translation:" + Arrays.toString(repr.getCurModelTranslation()));
		}
		
		IntHashMap<Object> curValidM1Models = new IntHashMap<Object>(m1Models.size());
		for(int i : m1Models) {
			if(repr.isClauseValid(i-1)) curValidM1Models.put(i,null);
		}
		
		
		RealSymFinder sym = new RealSymFinder(repr.getCurList(false),true);
		LiteralGroup varSyms = sym.getSymGroup();
		LiteralGroup modelSyms = repr.getModelGroup(varSyms);
		
		SchreierVector vec = new SchreierVector(modelSyms.reduce());
		
		boolean inOrbit = false;
		
		for(int i : m1Models) {
			if(vec.sameOrbit(i,m2Index)) {
				inOrbit=true;
			}
			if(inOrbit) break;
		}
		
		if(printLevel >= 3) {
			System.out.println("Syms: " + varSyms);
			System.out.println("Orbits: " + vec.transcribeOrbits(false));
			System.out.println();
	}
		
		if(inOrbit) {
			branchIdentical=true;
		} else {
			ClauseList modM1NewCl = modM1.getCurList(false);
			
			for(int m1Rep : m1Models) {
				List<Integer> m1Partition = vec.transcribeOrbits(false).getSetWith(m1Rep);
				for(int i : m1Partition) {
					curValidM1Models.put(i,null);
				}
			}
			
			IntHashMap<Object> m1Lits = new IntHashMap<Object>(modM1NewCl.getContext().size());
			
			for(int m1ModelIndex : curValidM1Models.getKeys()) {
				int[] model = repr.getClauseAtTranslated(m1ModelIndex-1,false);
				if(model != null) {
					modM1NewCl.addClause(model);
					for(int m : model) {
						m1Lits.put(m,null);
					}
				}
			}
			modM1NewCl = modM1NewCl.reduce();
			
			
			boolean possiblyIdentical = true;
			int[] m2 = repr.getClauseAtTranslated(m2Index-1,false);
			
			
			if(printLevel >= 3) {
				System.out.println(Arrays.toString(curValidM1Models.getKeys()));
				System.out.println("M1s    : " + modM1NewCl);
				System.out.println("M1Lits :" + Arrays.toString(m1Lits.getKeys()));
				System.out.println("M2     : " + Arrays.toString(m2));
				System.out.println();
			}
			
			for(int lit : m2) {
				if(!m1Lits.contains(lit)) {
					possiblyIdentical=false;
					break;
				}
			}
			if(possiblyIdentical) {
				LocalSymClauses modM1New = new LocalSymClauses(modM1NewCl);
				for(int lit : m2) {
					if(printLevel > 2) System.out.println(l);
					
					ArrayList<Integer> m1List = new ArrayList<Integer>(curValidM1Models.getKeys().length);
					for(int i : curValidM1Models.getKeys()) {
						m1List.add(i);
					}
					
					
					branchIdentical = checkNextLevel(lit,repr,m1List,m2Index,modM1New,modM2);
					if(!branchIdentical) break;
				}
			}
		}
		

		
		repr.pop();
		modM1.pop();
		modM2.pop();

		return branchIdentical;
	}

}
