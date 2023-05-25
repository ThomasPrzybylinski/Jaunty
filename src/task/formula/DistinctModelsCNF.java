package task.formula;

import java.util.LinkedList;

import org.apache.commons.collections.primitives.ArrayIntList;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralGroup;
import group.SchreierVector;
import task.formula.random.CNFCreator;
import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import task.translate.ConsoleDecodeable;
import util.IntegralDisjointSet;
import util.lit.LitUtil;
import util.lit.LitsMap;


//Given a set of models, find a minimal list of variables which renders them distinct.
//Used for searching to find minimum overall by adding max positive variable constraints later
//This reduces to finding hitting set on a bipartite graph
//based on the disagreement of every pair of models (and so set cover) 
public class DistinctModelsCNF implements CNFCreator, ConsoleDecodeable{

	private ClauseList models;
	private int numOrigVars = 0;
	private boolean preserveGlobalSymmetry = false;

	public DistinctModelsCNF(ClauseList models) {
		this.models=models;
	}

	public DistinctModelsCNF(ClauseList models, boolean preserveGlobalSymmetry) {
		this(models);
		this.preserveGlobalSymmetry = preserveGlobalSymmetry;
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
			newContext.getOrCreateVar("["+k+"]");
		}


		CNF ret = new CNF(newContext);

		if(preserveGlobalSymmetry) {
			RealSymFinder sym = new RealSymFinder(models);

			LiteralGroup lg = sym.getSymGroup();
			SchreierVector symOrbits = new SchreierVector(lg);
			IntegralDisjointSet orbits = symOrbits.transcribeOrbits();

			for(int v1 = 1; v1 <= numOrigVars; v1++) {
				for(int l2 : orbits.getSetWith(v1)) {
					int v2 = Math.abs(l2);

					if(v1 != v2) {
						ret.fastAddClause(-v1,v2);
					}
				}
			}
		}

		int iter = 0;
		for(int k = 0; k < models.size(); k++) {
			int[] mk = models.getClauses().get(k);
			for(int i = k+1; i < models.size(); i++) {
				iter++;
				int[] mi = models.getClauses().get(i);
				int[] agreement = SymmetryUtil.getAgreement(mk, mi);

				int size = 0;
				for(int lit : agreement) {
					if(lit == 0) size++;
				}


				//For every pair, at least one variable must be part of the disagreement
				int[] clause = new int[size];
				int index = 0;
				for(int j = 0; j < agreement.length; j++) {
					if(agreement[j] == 0) {
						clause[index] = Math.abs(mk[j]);
						index+=1;
					}
				}

				ret.fastLocalSortAddClause(clause);

				if(iter%100000 == 0) {
					ret = ret.trySubsumption();
				}
			}
		}

		ret = ret.trySubsumption();

		if(!preserveGlobalSymmetry) {
			//optimization to help with unstatisfiable instances for particular size
			//Get minimal covers. If for a given literal, every disagreement with that literal
			//is covered by another literal, we should not make that literal a choice
			
			//Does not work if we preserve global symmetries

			LinkedList<int[]> newClauses = new LinkedList<>();
			ArrayIntList[] clauseWithIgnoringLitVars = new ArrayIntList[numOrigVars+1];

			for(int k = 1; k < clauseWithIgnoringLitVars.length; k++) {
				clauseWithIgnoringLitVars[k] = new ArrayIntList();
			}

			for(int k = 0; k < ret.size(); k++) {
				int[] clause = ret.getClauses().get(k);

				for(int removedVar : clause) {
					int isClauseCoveredWithoutVar_Var = newContext.getOrCreateVar("M"+k+newContext.getVar(removedVar).getName()).getUID().intValue();
					clauseWithIgnoringLitVars[removedVar].add(isClauseCoveredWithoutVar_Var);

					//At least one var besides k must selected to be covered without k
					//(that not happening implies not covered)
					int[] mustSelectOtherVar = new int[clause.length];  
					int otherIndex = 0;

					for(int otherVar : clause) {

						if(otherVar != removedVar) {
							mustSelectOtherVar[otherIndex] = otherVar;
							otherIndex++;						

							//Some other var being covered implies clause is covered without our var
							int[] isClauseCoveredWithoutVar = new int[2];
							isClauseCoveredWithoutVar[0] = -otherVar;
							isClauseCoveredWithoutVar[1] = isClauseCoveredWithoutVar_Var;
							newClauses.add(isClauseCoveredWithoutVar);
						}
					}
					mustSelectOtherVar[otherIndex] = -isClauseCoveredWithoutVar_Var;
					newClauses.add(mustSelectOtherVar);
				}
			}

			ret.sort();

			//If every clause with variable k is covered by another variable, don't try to cover with k
			for(int k = 1; k < clauseWithIgnoringLitVars.length; k++) {
				ArrayIntList varList = clauseWithIgnoringLitVars[k];
				int[] dontCoverKClause = new int[varList.size()+1];
				dontCoverKClause[0] = -k;

				for(int i = 0; i < varList.size(); i++) {
					dontCoverKClause[i+1] = -varList.get(i);
				}

				newClauses.add(dontCoverKClause);

				//

				clauseWithIgnoringLitVars[k] = null; //Just to try and keep storage under control.
			}

			ret.fastAddAll(newClauses);
		}


		ret.sort();
		return ret;
	}

	public int getCNFLitFromOriginal(int lit) {
		return lit;
	}

	public int getOriginalLitFromCNF(int lit) {
		return lit;
	}
}
