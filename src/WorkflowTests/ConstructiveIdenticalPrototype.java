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
import task.formula.QueensToSAT;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import task.translate.DefaultConsoleDecoder;
import task.translate.FileDecodable;
import util.IntegralDisjointSet;
import util.lit.IntHashMap;
import util.lit.MILEComparator;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.local.ChoiceGetter;
import workflow.graph.local.PositiveChoices;

public class ConstructiveIdenticalPrototype {

	private static int printLevel = 5;
	private static int maxTest1 = 0;//Integer.MAX_VALUE;//
	private static int maxTest2 = 1;//Integer.MAX_VALUE;//

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
		ModelGiver modelGiver = new CNFCreatorModelGiver(new QueensToSAT(8));
		//ModelGiver modelGiver = new AllFilledRectangles(4);
		//ModelGiver modelGiver = new AllFilledSquares(5);
		//ModelGiver modelGiver = new PrototypeModelGiver();


		List<int[]> curModels = modelGiver.getAllModels(curContext); 
		Collections.sort(curModels, new MILEComparator());
		ClauseList models = new ClauseList(curContext);
		models.fastAddAll(curModels);

		ChoiceGetter choice = new PositiveChoices();
		//ChoiceGetter choice = new NotImpliedChoices();
		choice.computeChoices(models);
		models = choice.getList(models);

		LocalSymClauses localSymModels = new LocalSymClauses(models,false);

		RealSymFinder sym = new RealSymFinder(models);

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
			if(globModelOrbits.getLeastEltInSet(k+1) != (k+1)) continue;
			for(int i = 0; i < models.size() && i <= maxTest2; i++) {
				if(!globModelOrbits.sameSet(k+1,i+1) && globModelOrbits.getLeastEltInSet(i+1)==i+1) {
					if(printLevel >= 2) {
						System.out.println("Current Test Models: ("+(k+1)+", "+(i+1)+")");
					}

					//Get all fromModel global models and literals
					IntHashMap<Object> fromModelLits = new IntHashMap<Object>(curContext.size());

					ClauseList fromModelCl = new ClauseList(curContext);

					for(int modelNumber : globModelOrbits.getSetWith(k+1)) {
						fromModelCl.addClause(models.getClauses().get(modelNumber-1));
					}

					for(int l : models.getClauses().get(k)) {
						fromModelLits.put(l,null);
					}
					
					//Get all toModel global models
					
					int[] toModel = models.getClauses().get(i);
					ClauseList toModelCl = new ClauseList(curContext);
					
					for(int modelNumber : globModelOrbits.getSetWith(i+1)) {
						toModelCl.addClause(models.getClauses().get(modelNumber-1));
					}
					

					if(printLevel >= 4) {
						int[] fromModelListOut = fromModelLits.getKeys();
						Arrays.sort(fromModelListOut);
						System.out.println(Arrays.toString(fromModelListOut));
					}

					
					LocalSymClauses fromModelLocalClauses = new LocalSymClauses(fromModelCl);
					LocalSymClauses toModelLocalClauses = new LocalSymClauses(toModelCl);
					

					if(printLevel >= 3) System.out.println("Model2:" + Arrays.toString(toModel));
					boolean possiblyIdentical = true;
					for(int l : fromModelLits.getKeys()) {
						//if(printLevel >= 3) System.out.println(l);
						possiblyIdentical = checkNextLevel(l,localSymModels,k+1,i+1,globModelOrbits,globModelOrbits,false);
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


		System.out.println("Original: " + orig);
		System.out.println("Final   : " + modelOrbits);
	}

	private static boolean checkNextLevel(int l, LocalSymClauses repr, int fromModelNumber, int toModelNumber, 
			IntegralDisjointSet globalModelOrbits, IntegralDisjointSet localModelOrbits, boolean requiresMapTo) {
		boolean branchIdentical = false;
		boolean possiblyIdentical = true;

		repr.post();
		repr.addCondition(l);

		if(printLevel >= 3) {
			System.out.println("Checking lit: " + l);
			System.out.println("Rep:          " + repr.getCurrentCondition());
			System.out.println("Local Models: " + repr);
			System.out.println("Translation:" + Arrays.toString(repr.getCurModelTranslation()));
			System.out.println();
		}

		
		boolean onlyFromModels = true;
		boolean onlyToAndFromGlobModels = true;
		for(int i = 0; i < repr.numTotalModels(); i++) {
			if(!repr.isClauseValid(i)) continue;
			//The from model local orbits shouldbe the same as the to model local orbits since requiresMapTo is false, so it doesn't map to a non-m2
			if(!requiresMapTo && !localModelOrbits.sameSet(fromModelNumber,i+1) && !localModelOrbits.sameSet(toModelNumber,i+1)) {
				onlyToAndFromGlobModels = false;
			}
			
			if(!localModelOrbits.sameSet(fromModelNumber,i+1)) {
				onlyFromModels=false;
			}
		}

		if(!requiresMapTo && onlyToAndFromGlobModels) {
			if(printLevel >= 3) {
				System.out.println("We don't require a map to, and we only have to and from equal models left");
			}
			branchIdentical = true;
		}
		else if(requiresMapTo && onlyFromModels) {
			if(printLevel >= 3) {
				System.out.println("There are no more models to map to, and we need to map it");
			}
			possiblyIdentical=false;
		}
		
		if(possiblyIdentical && !branchIdentical ) {
			RealSymFinder sym = new RealSymFinder(repr.getCurList(false));
			LiteralGroup varSyms = sym.getSymGroup();
			LiteralGroup modelSyms = repr.getModelGroup(varSyms);

			SchreierVector curLocalModelSymVector = new SchreierVector(modelSyms.reduce());

			IntegralDisjointSet newModelOrbits = new IntegralDisjointSet(localModelOrbits);
			IntegralDisjointSet newLocalModelOrbits =  curLocalModelSymVector.transcribeOrbits(false);
			
			for(int root : newLocalModelOrbits.getRoots()) {
				for(int model1 : newLocalModelOrbits.getSetWith(root)) {
					for(int model2 : newLocalModelOrbits.getSetWith(root)) {
						newModelOrbits.join(model1,model2);
					}
				}
			}
			
		
			if(newLocalModelOrbits.sameSet(toModelNumber,fromModelNumber)) branchIdentical=true;
			
			if(!requiresMapTo && !branchIdentical) {
				//See if m1 maps to a non-m2 model
				requiresMapTo = newLocalModelOrbits.getSetWith(toModelNumber).size() != globalModelOrbits.getSetWith(toModelNumber).size();
			}

			if(requiresMapTo && !branchIdentical) {
				int[] fromModel = repr.getClauseAtTranslated(fromModelNumber-1,false);
				IntHashMap<Object> toModelLiterals = getLitsOf(repr,newModelOrbits.getSetWith(toModelNumber));
				
				
				if(printLevel >= 3) {
					System.out.println("fromModel: "   + Arrays.toString(fromModel));
					System.out.println("toModelLits: " + Arrays.toString((toModelLiterals.getKeys())));
				}
				
				for(int fromModelLit : fromModel) {
					if(!toModelLiterals.contains(fromModelLit)) {
						possiblyIdentical=false;
						break;
					}
				}
			}

			if(printLevel >= 3) {
				System.out.println("Syms            : " + varSyms);
				System.out.println("Local Orbits    : " + localModelOrbits);
				System.out.println("CurOrbits       : " + newModelOrbits);
				System.out.println("FoundMap        : " + branchIdentical);
				System.out.println("HasBreakingOrbit: " + requiresMapTo);
				System.out.println();
				System.out.println();
			}

			if(possiblyIdentical && !branchIdentical) {
				IntHashMap<Object> fromModelLits = new IntHashMap<Object>(repr.getContext().size());

				int[] model = repr.getClauseAtTranslated(fromModelNumber,false);
				if(model != null) {
					for(int m : model) {
						fromModelLits.put(m,null);
					}
				}


				for(int lit : fromModelLits.getKeys()) {
					possiblyIdentical = checkNextLevel(lit,repr,fromModelNumber,toModelNumber,globalModelOrbits,newModelOrbits,requiresMapTo);

					if(!possiblyIdentical) {
						branchIdentical = false;
						break;
					}

				}
			}
		}



		if(possiblyIdentical) branchIdentical=true;
		if(printLevel > 2) {
			if(!branchIdentical) {
				System.out.println("Fail!");
			}
		}
		
		
		repr.pop();
		
		return branchIdentical;
	}
	
	private static IntHashMap<Object> getLitsOf(LocalSymClauses repr, List<Integer> modelNumbers) {
		IntHashMap<Object> lits = new IntHashMap<Object>(repr.getContext().size());
		
		for(Integer i : modelNumbers) {
			if(repr.isClauseValid(i.intValue()-1)) {
				for(int lit : repr.getClauseAtTranslated(i.intValue()-1,false)) {
					lits.put(lit,null);
				}
			}
		}
		return lits;
	}

}
