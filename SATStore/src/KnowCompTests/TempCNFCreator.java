package KnowCompTests;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;

public class TempCNFCreator implements CNFCreator {
	private int numCl;
	private int clSize;
	
	public TempCNFCreator(int numCl,int clSize) {
		this.numCl = numCl;
		this.clSize = clSize;
	}
	
	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = new CNF(context);
		context.ensureSize(numCl*clSize);
		int nextVar = 1;
		for(int k = 0; k < numCl; k++) {
			int[] posCl = new int[clSize];
			int[] negCl = new int[clSize];
			
			for(int i = 0; i < posCl.length; i++) {
				posCl[i] = nextVar+i;
				negCl[i] = -(nextVar+i);
			}
			
			nextVar += posCl.length;//Math.max(1,1+posCl.length/2);
			
			ret.fastAddClause(posCl);
			ret.fastAddClause(negCl);
		}
		
		ret.sort();
		
		return ret;
	}

}
