package workflow.graph.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralPermutation;
import util.lit.LitUtil;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.ReportableEdgeAddr;

public class SatBasedLocalSymAddr  extends ReportableEdgeAddr{

	private int numVars;
	private int numLits;
	private int numModels;
	private int totalSize;
	
	private int iters = 0;
	
	private ClauseList curList;

	@Override
	public void addEdges(PossiblyDenseGraph<int[]> g, ClauseList representatives) {
		iters = 0;
		curList = representatives;
		
//		AgreementLocalSymAdder addr1 = new AgreementLocalSymAdder();
//		addr1.addEdges(g,representatives);
//		
//		GlobalSymmetryEdges addr2 = new GlobalSymmetryEdges();
//		addr2.addEdges(g,representatives);
//		addr1 = null;
//		addr2 = null;
	
		ISolver solver;
		try {
			solver = getSolverFor(representatives);
		} catch(ContradictionException e) {
			return; //No possible edges
		}
		
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = k+1; i < g.getNumNodes(); i++) {
				if(g.getEdgeWeight(k,i) == 0) continue;
				int[] mustMapConstr = new int[1];
				mustMapConstr[0] = getModelMapVar(k,i);
				try {
					if(solver.isSatisfiable(new VecInt(mustMapConstr))) {
						g.setEdgeWeight(k,i,0);
						int[] model = solver.model();
//						printNiceModel(model);
					} else {
						System.out.print("");
					}
				} catch(TimeoutException e) {
					System.err.println("TIMEOUT!");
				}
				solver.clearLearntClauses();
				iters++;
			}
		}
		solver.reset();

	}


	public ISolver getSolverFor(ClauseList reps) throws ContradictionException {
		numVars = reps.getContext().size();
		numLits = 2*numVars;
		numModels = reps.size();
		totalSize = numLits + numLits*numLits + numModels + numModels*numModels;

		ISolver satSolve = SolverFactory.newDefault();
		satSolve.reset();
		satSolve.newVar(totalSize);
		satSolve.setExpectedNumberOfClauses(numVars*numVars*numVars+numModels*numModels*numModels);
		
		validateVars();

		for(int k = 0; k <= numLits; k++) {
			int lit1 = k-numVars;
			if(lit1 == 0) continue;

			int[] kMustBeMapped = new int[numLits];
			int kMustBeMappedIndex = 0;
			for(int j = 0; j <= numLits; j++) {
				int lit2 = j-numVars;
				if(lit2 == 0) continue;

				kMustBeMapped[kMustBeMappedIndex] = getLitMapVar(lit1,lit2);
				kMustBeMappedIndex++;

				int[] ensurePhase = new int[2];
				ensurePhase[0] = -getLitMapVar(lit1,lit2);
				ensurePhase[1] = getLitMapVar(-lit1,-lit2);
				satSolve.addClause(new VecInt(ensurePhase));

				for(int h = 0; h <= numLits; h++) {
					int lit3 = h-numVars;
					if(lit3 == 0) continue;
					if(lit3 != lit2) {
						int[] kCanOnlyMapToOneLiter = new int[2];
						kCanOnlyMapToOneLiter[0] = -getLitMapVar(lit1,lit2);
						kCanOnlyMapToOneLiter[1] = -getLitMapVar(lit1,lit3);
						satSolve.addClause(new VecInt(kCanOnlyMapToOneLiter));
					}

					if(lit3 != lit1) {
						int[] kTojMeansNo_hToj = new int[2];
						kTojMeansNo_hToj[0] = -getLitMapVar(lit1,lit2);
						kTojMeansNo_hToj[1] = -getLitMapVar(lit3,lit2);
						satSolve.addClause(new VecInt(kTojMeansNo_hToj));
					}

				}
			}

			satSolve.addClause(new VecInt(kMustBeMapped));

			int[] partInterpLitMustMapToSelf = new int[2];
			partInterpLitMustMapToSelf[0] = -getPartInterpVar(lit1);
			partInterpLitMustMapToSelf[1] = getLitMapVar(lit1,lit1);
			satSolve.addClause(new VecInt(partInterpLitMustMapToSelf));
		}

		int[] oneModelMustExist = new int[numModels];
		for(int k = 0; k < numModels; k++) {
			int model1 = k;

			oneModelMustExist[k] = getModelExistsVar(model1);
			
			int[] kMustBeMappedOrNotExist = new int[numModels+1];
			int kMustBeMappedIndex = 0;
			for(int j = 0; j < numModels; j++) {
				int model2 = j;
				
				int[] ifDoeseNotExistCannotBeMapped = new int[2];
				ifDoeseNotExistCannotBeMapped[0] = getModelExistsVar(model1);
				ifDoeseNotExistCannotBeMapped[1] = -getModelMapVar(model1,model2);
				satSolve.addClause(new VecInt(ifDoeseNotExistCannotBeMapped));
				
				int[]ifDoeseNotExistCannotBeMappedTo = new int[2];
				ifDoeseNotExistCannotBeMappedTo[0] = getModelExistsVar(model1);
				ifDoeseNotExistCannotBeMappedTo[1] = -getModelMapVar(model2,model1);
				satSolve.addClause(new VecInt(ifDoeseNotExistCannotBeMappedTo));

				kMustBeMappedOrNotExist[kMustBeMappedIndex] = getModelMapVar(model1,model2);
				kMustBeMappedIndex++;

				for(int h = 0; h < numModels; h++) {
					int model3 = h;
					if(model3 == 0) continue;
					if(model3 != model2) {
						int[] kCanOnlyMapToOneLiter = new int[2];
						kCanOnlyMapToOneLiter[0] = -getModelMapVar(model1,model2);
						kCanOnlyMapToOneLiter[1] = -getModelMapVar(model1,model3);
						satSolve.addClause(new VecInt(kCanOnlyMapToOneLiter));
					}

					if(model3 != model1) {
						int[] kTojMeansNo_hToj = new int[2];
						kTojMeansNo_hToj[0] = -getModelMapVar(model1,model2);
						kTojMeansNo_hToj[1] = -getModelMapVar(model3,model2);
						satSolve.addClause(new VecInt(kTojMeansNo_hToj));
					}

				}
			}

			kMustBeMappedOrNotExist[kMustBeMappedIndex] = -getModelExistsVar(model1);

			satSolve.addClause(new VecInt(kMustBeMappedOrNotExist));

			int[] modelRep1 = reps.getClauses().get(k);

			for(int j = 0; j < numModels; j++) {

				int[] modelRep2 = reps.getClauses().get(j);
				for(int i = 0; i < modelRep1.length; i++) {
					for(int h = 0; h < modelRep2.length; h++) {
						//If i maps to something not in model j, then our current
						//model cannot map to model j
						int[] cannotMapModelIfLitMap = new int[2];
						cannotMapModelIfLitMap[0] = -getLitMapVar(modelRep1[i],-modelRep2[h]);
						cannotMapModelIfLitMap[1] = -getModelMapVar(k,j);
						satSolve.addClause(new VecInt(cannotMapModelIfLitMap));
					}
				}
			}
			
			int[] existsIfNotForcedToNotExist = new int[modelRep1.length+1];
		
			int exIndex = 0;
			for(int i = 0; i < modelRep1.length; i++) {
				int[] howToNotExist = new int[2];
				int oppLit = -modelRep1[i];
				//opposing literal in part inter means model does not exist
				howToNotExist[0] = -getPartInterpVar(oppLit);
				howToNotExist[1] = -getModelExistsVar(model1);
				
				satSolve.addClause(new VecInt(howToNotExist));
				
				existsIfNotForcedToNotExist[exIndex] = getPartInterpVar(oppLit);
				exIndex++;
			}
			existsIfNotForcedToNotExist[exIndex] = getModelExistsVar(model1);
			satSolve.addClause(new VecInt(existsIfNotForcedToNotExist));
		}
		satSolve.addClause(new VecInt(oneModelMustExist));
		
		ArrayList<ArrayList<Integer>> oppLitsToModels = new ArrayList<ArrayList<Integer>>(2*numVars+1);
		for(int k = 0; k < 2*numVars+1; k++) {
			oppLitsToModels.add(new ArrayList<Integer>());
		}
		
		for(int k = 0; k < numModels; k++) {
			int[] modelRep1 = reps.getClauses().get(k);
			
			for(int i : modelRep1) {
				oppLitsToModels.get(LitUtil.getIndex(-i,numVars)).add(k);
			}
		}

//		for(int k = 0; k < oppLitsToModels.size(); k++) {
//			//Either there exists a model with an opposing literal, or the literal
//			//is part of the partial interpreation (roughly equivalent to fixed literal pruning)
//			ArrayList<Integer> oppModels = oppLitsToModels.get(k);
//			int[] ensureFixedLiterals = new int[oppModels.size()+1];
//			
//			for(int i = 0; i < oppModels.size(); i++) {
//				ensureFixedLiterals[i] = getModelExistsVar(oppModels.get(i));
//			}
//			ensureFixedLiterals[oppModels.size()] = getPartInterpVar(LitUtil.getLit(k,numVars));
//			satSolve.addClause(new VecInt(ensureFixedLiterals));
//		}

		return satSolve;
	}

	private void validateVars() {
		boolean[] seen = new boolean[totalSize+1];
		
		for(int k = 1; k <= numVars; k++) {
			int index = getPartInterpVar(k);
			dup(seen, index);
			
			index = getPartInterpVar(-k);
			dup(seen, index);
			
			for(int j = 1; j <= numVars; j++) {
				index = getLitMapVar(k,j);
				dup(seen, index);
				
				index = getLitMapVar(k,-j);
				dup(seen, index);
				
				index = getLitMapVar(-k,j);
				dup(seen, index);
				
				index = getLitMapVar(-k,-j);
				dup(seen, index);
			}
		}
		
		for(int k = 0; k < numModels; k++) {
			int index = getModelExistsVar(k);
			dup(seen, index);
			
			for(int j = 0; j < numModels; j++) {
				index = getModelMapVar(k,j);
				dup(seen, index);
			}
		}
		
		for(int k = 1; k < seen.length; k++) {
			if(!seen[k]) {
				System.out.println("UNUSED");
			}
		}
		
		if(seen[0]) {
			System.out.println("OVERUSED");
		}
		
	}


	private void dup(boolean[] seen, int index) {
		if(seen[index]) {
			System.out.println("DUP!");
		} else {
			seen[index] = true;
		}
	}


	int getPartInterpVar(int lit1) {
		return LitUtil.getIndex(lit1,numVars) + (lit1 < 0 ? 1 : 0);
	}

	int getLitMapVar(int lit1, int lit2) {
//		int lit1Index = LitUtil.getIndex(lit1,numVars) + (lit1 > 0 ? -1 : 0);
//		int lit2Index = LitUtil.getIndex(lit2,numVars) + (lit2 > 0 ? -1 : 0);
		
		int lit1Index = 2*Math.abs(lit1) + (lit1 < 0 ? 1 : 0) - 2;
		int lit2Index = 2*Math.abs(lit2) + (lit2 < 0 ? 1 : 0) - 2;
		
		
		return numLits + numLits*lit1Index + lit2Index + 1;
	}

	int getModelExistsVar(int model) {
		int numSoFar = numLits + numLits*numLits;
		return model + numSoFar + 1;
	}

	int getModelMapVar(int model1, int model2) {
		int numSoFar = numLits + numLits*numLits + numModels;
		return numSoFar + numModels*model1 + model2 + 1;
	}

	private void printNiceModel(int[] model) {
		int[] trueModel = new int[totalSize+1];
		for(int i : model) {
			trueModel[Math.abs(i)] = i;
		}
		
		model = trueModel;
		
		int[] perm = new int[numVars+1];
		
		ArrayList<Integer> partInterp = new ArrayList<Integer>();
		for(int k = 1; k <= numVars; k++) {
			
			if(model[getPartInterpVar(k)] > 0) {
				partInterp.add(k);
			}
			if(model[getPartInterpVar(-k)] > 0) {
				partInterp.add(-k);
			}
			
			for(int i = -numVars; i <= numVars; i++) {
				if(i == 0) continue;
				if(model[getLitMapVar(k,i)] > 0) {
//					System.out.println(k+"->"+i);
					perm[k] = i;
					break;
				}
			}
		}
		System.out.println(partInterp);
		LiteralPermutation nice = new LiteralPermutation(perm);
		System.out.println(nice);
		
		System.out.println("ModelStuff:");
		ArrayList<Integer> modelsAround = new ArrayList<Integer>();
		
		for(int k = 0; k < numModels; k++) {
			if(model[getModelExistsVar(k)] > 0) {
				System.out.print(k+":"+Arrays.toString(curList.getClauses().get(k)));
			}
			
			for(int h = 0; h < numModels; h++) {
				if(model[getModelMapVar(k,h)] > 0) {
//					System.out.println(k+"->"+h);
				}
			}
		}
		
		
		
		System.out.println();
		System.out.println();
	}


	@Override
	public int getIters() {
		return iters;
	}

	@Override
	public long getNumUsefulModelSyms() {
		return 0;
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public String toString() {
		return "SatLocal";
	}
	
	

}
