package wackyTests;

import java.util.Arrays;

import task.NChooseRGenerator;
import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import util.PartitionIterator;
import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;

public class FindSmallestNonSym {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numClauses = 4;
		int numVars = (int)Math.pow(2,numClauses);
		
		int[][] potentialModels = new int[(int)Math.pow(2,numVars)][];
		PartitionIterator iter = new PartitionIterator(numVars,2);

		int ind = 0;
		while(iter.hasNext()) {
			potentialModels[ind] = iter.next();

			for(int k = 0; k < potentialModels[ind].length; k++) {
				if(potentialModels[ind][k] == 1) { 
					potentialModels[ind][k] = (k+1);
				} else {
					potentialModels[ind][k] = -(k+1);
				}
			}
			ind++;
		}

		NChooseRGenerator gen = new NChooseRGenerator(potentialModels.length,numClauses);
		ind = 0;
		while(gen.hasMore()) {
			ind++;
			int[] picker = gen.getNext();
			DNF modelList = new DNF(VariableContext.defaultContext);

			for(int k : picker) {
				modelList.addClause(potentialModels[k]);
			}
			System.out.println(numVars + " \t " +  modelList.getClauses().size() + " \t " + ind);
			
			
			ClauseList inv = SymmetryUtil.getInverseList(modelList);
			SimpleSymFinder finder = new SimpleSymFinder(inv);
			DisjointSet<Integer> ds = finder.getSymOrbits();
			
			if(ds.getRoots().size() == inv.getClauses().size()) {
				for(int[] p : (new SimpleSymFinder(modelList)).getSyms()) {
					System.out.println(Arrays.toString(p));
				}
				System.out.println(inv);
				System.out.println(modelList);				
			}
			
//			if(!isAgreementConnected(modelList)) {
//				System.out.println(modelList);
//				
//				int[][] lens = PrototypesUtil.doAgreementSym(modelList);
//
//				for(int k = 0; k < lens.length; k++) {
//					for(int j = 0; j < lens[k].length; j++) {
//						System.out.print(lens[k][j] == 1 ? 1 : 0);
//					}
//					System.out.println();
//				}
//				
//				return;
//			}
			
			
		}

	}
	
//	private static boolean isAgreementConnected(DNF modelList) {
//		IntegralDisjointSet ds = new IntegralDisjointSet(0,modelList.getClauses().size());
//		
//		int[] m0 = modelList.getClauses().get(0);
//		for(int k = 1; k < modelList.getClauses().size(); k++) {
//			int[] mk = modelList.getClauses().get(k);
//			if(SymmetryUtil.doModelsAgreeSym(modelList.getContext(),modelList.getClauses(),m0,mk)) {
//				ds.join(0,k);
//			}
//		}
//		
//		for(int k = 1; k < modelList.getClauses().size(); k++) {
//			int[] mk = modelList.getClauses().get(k);
//			for(int i = k+1; i < modelList.getClauses().size(); i++) {
//				if(ds.sameSet(0,i)) continue;
//				
//				int[] mi = modelList.getClauses().get(i);
//				if(SymmetryUtil.doModelsAgreeSym(modelList.getContext(),modelList.getClauses(),mk,mi)) {
//					ds.join(k,i);
//				}	
//			}
//		}
//		
//		for(int k = 1; k < modelList.getClauses().size(); k++) {
//			if(!ds.sameSet(0,k)) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
}
