package task.formula;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import task.formula.random.CNFCreator;
import util.lit.LitUtil.RemovalDecoding;

public class DNFToCNFCreator implements CNFCreator {

	ClauseList dnf;
	int origVars;
	RemovalDecoding decoding = null;
	
	public DNFToCNFCreator(ClauseList dnf) {
		this(dnf,false);
	}
	
	public DNFToCNFCreator(ClauseList dnf, boolean simplifyVars) {
		this.dnf = dnf;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		return generateCNF(context,false);
	}
	
	public CNF generateCNF(VariableContext context, boolean origCNF) {
		origVars = dnf.getContext().size();

		CNF ret = getCNFFromDNF(dnf,false);
		context.ensureSize(ret.getContext().size());
		return ret;
	}

	public static CNF getCNFFromDNF(ClauseList dnf,boolean origCNF) {
		VariableContext origContext = dnf.getContext();
		VariableContext context = new VariableContext();

		int origVars = origContext.size();
		context.ensureSize(origContext.size()+dnf.getClauses().size());

		CNF ret = new CNF(context);

		int[] satOneClause = new int[dnf.getClauses().size()];
		
		for(int k = 0; k < dnf.getClauses().size(); k++) {
			satOneClause[k] = getClauseVar(k,origVars);
		}
		ret.fastAddClause(satOneClause);

		int modelInd = 0;
		for(int[] model : dnf.getClauses()) {
			for(int k = 0; k < model.length; k++) {
				//If the var is not assigned to what the model should be
				//then this model cannot be the model
				//That is (NOT model[k] implies NOT the modelInd-nth model
				int lit;
				if(origCNF) {
					lit = getClauseVar(modelInd,origVars);
				} else {
					lit = -getClauseVar(modelInd,origVars);
				}
				ret.fastAddClause(model[k],lit);
			}
			modelInd++;
		}

		ret.sort();

		return ret;
	}
	
	public int getOrigVars() {
		return origVars;
	}

	private int getClauseVar(int model) {
		return getClauseVar(model,origVars);
	}
	
	private static int getClauseVar(int model, int origVars) {
		return origVars+model+1;
	}
	
	public ClauseList getPrevDNF() {
		return dnf;
	}
	
	
	public int[] curUnused(int[] clause) {
		IntList tempRet = new ArrayIntList(clause.length);
		for(int i : clause) {
			if(Math.abs(i) <= origVars) {
				tempRet.add(i);
			}
		}
		
		return tempRet.toArray();
	}

	//Ignore added symmetry breaking vars
	private static int[] getTrueModel(int[] findModel, int origVars) {
		int[] ret = new int[origVars];
		System.arraycopy(findModel,0,ret,0,origVars);
		return ret;
	}
}
