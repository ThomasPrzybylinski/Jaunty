package wackyTests;

import java.util.Random;

import formula.VariableContext;
import formula.simple.DNF;
import task.symmetry.PrototypesUtil;
import task.symmetry.SymmetryUtil;
import util.IntegralDisjointSet;
import util.PartitionIterator;

public class FindUnconnectedAgreeSym {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numVars = 9;
		int numIters = 100000;
		Random rand = new Random();
		
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


		for(int i = 0; i < numIters; i++) {

			//List<int[]> curModels = new ArrayList<int[]>();
			DNF modelList = new DNF(VariableContext.defaultContext);

			for(int k = 0; k < potentialModels.length; k++) {
				if(rand.nextBoolean()) { //Seems to have the least connectivity at ~.5 of total models
					modelList.addClause(potentialModels[k]);
				}
			}
			System.out.println(numVars + " \t " +  modelList.getClauses().size() + " \t " + i);
			
			if(!isAgreementConnected(modelList)) {
				System.out.println(modelList);
				
				int[][] lens = PrototypesUtil.doAgreementSym(modelList);

				for(int k = 0; k < lens.length; k++) {
					for(int j = 0; j < lens[k].length; j++) {
						System.out.print(lens[k][j] == 1 ? 1 : 0);
					}
					System.out.println();
				}
				
				return;
			}
			
			
		}

	}
	
	private static boolean isAgreementConnected(DNF modelList) {
		IntegralDisjointSet ds = new IntegralDisjointSet(0,modelList.getClauses().size());
		
		int[] m0 = modelList.getClauses().get(0);
		for(int k = 1; k < modelList.getClauses().size(); k++) {
			int[] mk = modelList.getClauses().get(k);
			if(SymmetryUtil.doModelsAgreeSym(modelList.getContext(),modelList.getClauses(),m0,mk)) {
				ds.join(0,k);
			}
		}
		
		for(int k = 1; k < modelList.getClauses().size(); k++) {
			int[] mk = modelList.getClauses().get(k);
			for(int i = k+1; i < modelList.getClauses().size(); i++) {
				if(ds.sameSet(0,i)) continue;
				
				int[] mi = modelList.getClauses().get(i);
				if(SymmetryUtil.doModelsAgreeSym(modelList.getContext(),modelList.getClauses(),mk,mi)) {
					ds.join(k,i);
				}	
			}
		}
		
		for(int k = 1; k < modelList.getClauses().size(); k++) {
			if(!ds.sameSet(0,k)) {
				return false;
			}
		}
		
		return true;
	}
}
