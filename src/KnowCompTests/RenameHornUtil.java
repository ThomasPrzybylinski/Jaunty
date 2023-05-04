package KnowCompTests;

import java.util.Arrays;
import java.util.LinkedList;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import util.lit.LitUtil;
import formula.VariableContext;
import formula.simple.CNF;
import group.SchreierVector;

public class RenameHornUtil {

	public static boolean isRenameHorn(CNF cnf) throws TimeoutException{
		if(isHorn(cnf)) return true;
		int numVars = cnf.getContext().size();
		
		boolean[][] forwardConnected = new boolean[2*numVars+1][2*numVars+1];
		
		for(int[] cl : cnf.getClauses()) {
			for(int k = 0; k < cl.length; k++) {
				for(int i = k+1; i < cl.length; i++) {
					//Connected if implies
					forwardConnected[LitUtil.getIndex(-cl[k],numVars)][LitUtil.getIndex(cl[i],numVars)] = true;
					forwardConnected[LitUtil.getIndex(-cl[i],numVars)][LitUtil.getIndex(cl[k],numVars)] = true;
				}
			}
		}
		
		boolean[] visited = new boolean[2*numVars+1];
		LinkedList<Integer> stck = new LinkedList<Integer>();
		for(int k = 1; k <= numVars; k++) {
			int kInd = LitUtil.getIndex(k,numVars);
			int negKInd = LitUtil.getIndex(-k,numVars);

			boolean posToNeg = getIsConnected(forwardConnected, visited, stck, kInd,negKInd);
			boolean negToPos = getIsConnected(forwardConnected, visited, stck,negKInd,kInd);
			
			if(posToNeg && negToPos) return false;
		}
		
		return true;
		
		

//		ISolver satSolve = SolverFactory.newDefault();
//		satSolve.reset();
//		satSolve.newVar(cnf.getContext().size());
//
//		try {
//			for(int[] cl : cnf.getClauses()) {
//				if(cl.length == 1) {
//					satSolve.addClause(new VecInt(cl));
//				} else if(cl.length == 2) {
//					satSolve.addClause(new VecInt(new int[]{cl[0],cl[1]}));
//				} else { //cl.length == 3
//					satSolve.addClause(new VecInt(new int[]{cl[0],cl[1]}));
//					satSolve.addClause(new VecInt(new int[]{cl[0],cl[2]}));
//					satSolve.addClause(new VecInt(new int[]{cl[1],cl[2]}));
//				}
//			}
//
//
//
//
//		}catch(ContradictionException ce) {
//			satSolve.reset();
//			//						System.out.println("C");
//			//			System.out.println(cnf);
//			//			System.out.println(test);
//			//			System.out.println();
//			return false;
//		}
//		boolean ret = satSolve.isSatisfiable();
//		satSolve.reset();
//		return ret;

	}


	private static boolean getIsConnected(boolean[][] forwardConnected,
			boolean[] visited, LinkedList<Integer> stck, int kInd, int negKInd) {
		Arrays.fill(visited,false);
		stck.clear();
		stck.add(kInd);
		
		while(!stck.isEmpty()) {
			int litInd = stck.poll();
			visited[litInd] = true;
			
			boolean[] connections = forwardConnected[litInd];
			for(int i = 0; i < forwardConnected.length; i++) {
				if(litInd == negKInd) return true;
				if(!visited[i] && connections[i]) {
					visited[i] = true;
					forwardConnected[kInd][i] = true;
					stck.push(i);
				}
			}
		}
		return false;
	}
	

	public static void renameToMaximizeTotalNegLits(CNF cnf) {
		VariableContext context = cnf.getContext();
		int numVars = context.size();
		int[] litFreqs = new int[2*context.size()+1];

		for(int[] cl : cnf.getClauses()) {
			for(int l : cl) {
				litFreqs[LitUtil.getIndex(l,numVars)]++;
			}
		}

		for(int[] cl : cnf.getClauses()) {
			for(int k = 0; k < cl.length; k++) {
				int posLit = Math.abs(cl[k]);
				int negLit = -posLit;

				if(litFreqs[LitUtil.getIndex(posLit,numVars)] > litFreqs[LitUtil.getIndex(negLit,numVars)]) {
					cl[k] = -cl[k];
				}
			}
		}
	}

	public static void renameToGreedyMinNonHornPosNumProduct(CNF cnf) {
		VariableContext context = cnf.getContext();
		int numVars = context.size();

		int[] numPosCl = new int[cnf.getClauses().size()];


		long product = 1;

		for(int k = 0; k < cnf.getClauses().size(); k++) {
			int[] cl = cnf.getClauses().get(k);
			int numPos = 0;
			for(int l : cl) {
				if(l > 0) {
					numPos++;
				}
			}

			numPosCl[k] = numPos;
			if(numPos > 1) {
				product *= numPos;
			}
		}

		long[] renameVals = new long[numVars+1];
		while(true) {
			Arrays.fill(renameVals,1);
			for(int k = 0; k < cnf.getClauses().size(); k++) {
				int[] cl = cnf.getClauses().get(k);
				int clInd = 0;
				for(int v = 1; v <= numVars; v++) {
					if(clInd < cl.length && Math.abs(cl[clInd]) == v) {
						int l = cl[clInd];
						if(l > 0) {
							if(numPosCl[k] > 1) {
								renameVals[v] *= (numPosCl[k]-1);	
							}

						} else {
							renameVals[v] *= (numPosCl[k]+1);
						}
						clInd++;
					} else if(numPosCl[k] >= 1) {
						renameVals[v] *= numPosCl[k];
					}
				}
			}

			long minProd = Long.MAX_VALUE;
			int index = -1;

			for(int k = 1; k < renameVals.length; k++) {
				if(renameVals[k] < minProd) {
					minProd = renameVals[k];
					index = k;
				}
			}

			if(minProd < product) {
				for(int i = 0; i < cnf.getClauses().size(); i++) {
					int[] cl = cnf.getClauses().get(i);
					for(int k = 0; k < cl.length; k++) {
						int posLit = Math.abs(cl[k]);
						if(posLit == index) {
							if(cl[k] > 0) {
								numPosCl[i]--;
							} else {
								numPosCl[i]++;
							}

							cl[k] = -cl[k];
						}
					}
				}
				product = minProd;
			} else {
				break;
			}
		}
		//		System.out.println();
		//		System.out.println("p:"+product);

	}

	public static boolean isHorn(CNF cnf) {
		for(int[] cl : cnf.getClauses()) {
			if(!isHorn(cl)) return false;
		}
		return true;
	}

	public static boolean isHorn(int[] cl) {
		boolean pos = false;

		for(int l : cl) {
			if(l > 0) {
				if(pos) {
					return false;
				} else {
					pos = true;
				}
			}
		}
		return true;
	}

	//Is AFF(cnf) <==> cnf?
	//AFF(cnf) ==> cnf b/c at least 1 literal is true in each AFF(cnf) clause, so is true for cnf
	//So the question is cnf ==> AFF(cnf)?
	//or  (NOT cnf) OR AFF(cnf)
	//true if NOT ((NOT cnf) OR AFF(cnf) is a contradiction
	//cnf AND NOT AFF(cnf), which makes AFF(cnf) a disjunction of XORs
	//or cnf AND  (-AFF(C0) OR -AFF(C1)...)
	//which is (cnf AND -AFF(C0)) OR (cnf AND -(AFF(C1)))..., so is |C| sat problems.
	//XORs can be computed as at most 4 clauses.
	public static boolean is3SATAffine(CNF cnf) throws TimeoutException {
		for(int[] cl : cnf.getClauses()) {
			if(cl.length == 1) continue;
			try {
				ISolver solver = cnf.getSolverForCNFEnsureVariableUIDsMatch();

				if(cl.length == 2) {
					solver.addClause(new VecInt(new int[]{-cl[0],cl[1]}));
					solver.addClause(new VecInt(new int[]{cl[0],-cl[1]}));
				} else if(cl.length == 3) {
					solver.addClause(new VecInt(new int[]{-cl[0],-cl[1],-cl[2]}));
					solver.addClause(new VecInt(new int[]{-cl[0],cl[1],cl[2]}));
					solver.addClause(new VecInt(new int[]{cl[0],-cl[1],cl[2]}));
					solver.addClause(new VecInt(new int[]{cl[0],cl[1],-cl[2]}));
				}

				if(solver.isSatisfiable()) {
					solver.reset();
					return false;
				} else {
					solver.reset();
				}
			} catch(ContradictionException ce) {}

		}
		
		return true;



	}

}
