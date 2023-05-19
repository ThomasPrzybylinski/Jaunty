package task.formula;

import java.util.LinkedList;

import org.apache.commons.collections.primitives.ArrayIntList;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import task.formula.random.CNFCreator;
import task.symmetry.SymmetryUtil;
import task.translate.ConsoleDecodeable;
import util.lit.LitUtil;
import util.lit.LitsMap;

public class DistinctModelsCNF  implements CNFCreator, ConsoleDecodeable{

	private ClauseList models;
	private int numOrigVars = 0;
	
	public DistinctModelsCNF(ClauseList models) {
		this.models=models;
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		return models.toString();
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		numOrigVars = models.getContext().size();
		
		VariableContext newContext = new VariableContext();
		
		for(int k = 1; k <= numOrigVars; k++) {
			newContext.getOrCreateVar("[+"+k+"]");
			newContext.getOrCreateVar("[-"+k+"]");
		}
		
		
		CNF ret = new CNF(newContext);
		
		int iter = 0;
		for(int k = 0; k < models.size(); k++) {
			int[] mk = models.getClauses().get(k);
			for(int i = k+1; i < models.size(); i++) {
				iter++;
				int[] mi = models.getClauses().get(i);
				int[] agreement = SymmetryUtil.getAgreement(mk, mi);
				
				int size = 0;
				for(int lit : agreement) {
					if(lit == 0) size+=2;
				}
				
				
				//For every pair, at least one variable must be part of the disagreement
				int[] clause = new int[size];
				int index = 0;
				for(int j = 0; j < agreement.length; j++) {
					if(agreement[j] == 0) {
						clause[index] = getCNFLitFromOriginal(mk[j]);
						clause[index+1] = getCNFLitFromOriginal(mi[j]);
						index+=2;
					}
				}
				
				ret.fastLocalSortAddClause(clause);
				
				if(iter%100000 == 0) {
					ret = ret.trySubsumption();
				}
			}
		}
		
		ret = ret.trySubsumption();
		
		//optimization to help with unstatisfiable instances for particular size
		//Get minimal covers. If for a given literal, every disagreement with that literal
		//is covered by another literal, we should not make that literal a choice
		
		LinkedList<int[]> newClauses = new LinkedList<>();
		ArrayIntList[] clauseWithIgnoringLitVars = new ArrayIntList[2*numOrigVars+1];
		
		for(int k = 1; k < clauseWithIgnoringLitVars.length; k++) {
			clauseWithIgnoringLitVars[k] = new ArrayIntList();
		}
		
		for(int k = 0; k < ret.size(); k++) {
			int[] clause = ret.getClauses().get(k);
			
			for(int removedVar : clause) {
				int isClauseCoveredWithoutVar_Var = newContext.getOrCreateVar("M"+k+newContext.getVar(removedVar).getName()).getUID().intValue();
				clauseWithIgnoringLitVars[removedVar].add(isClauseCoveredWithoutVar_Var);
				
				for(int otherVar : clause) {
					//Some other var being covered implies clause is covered without our var
					if(otherVar != removedVar) {
						int[] isClauseCoveredWithoutVar = new int[2];
						isClauseCoveredWithoutVar[0] = -otherVar;
						isClauseCoveredWithoutVar[1] = isClauseCoveredWithoutVar_Var;
						newClauses.add(isClauseCoveredWithoutVar);
						
					}
				}
			}
		}
		
		ret.sort();
		
		//If every clause with variable k is covered by another variable, don't try to cover with k
		for(int k = 1; k < clauseWithIgnoringLitVars.length; k++) {
			ArrayIntList varList = clauseWithIgnoringLitVars[k];
			int[] clause = new int[varList.size()+1];
			clause[0] = -k;
			
			for(int i = 0; i < varList.size(); i++) {
				clause[i+1] = -varList.get(i);
			}
			
			newClauses.add(clause);
			clauseWithIgnoringLitVars[k] = null; //Just to try and keep storage under control.
		}
		
		ret.addAll(newClauses);
		return ret;
	}
	
	public int getCNFLitFromOriginal(int lit) {
		int extra = lit > 0 ? -1 : 0;
		return (2*Math.abs(lit) + extra);
	}
	
	public int getOriginalLitFromCNF(int lit) {
		int curLitSign = lit > 0 ? 1 : -1;
		int newLitSign = lit%2 == 0 ? -1 : 1;
		return newLitSign*(lit+curLitSign)/2;
	}
}
