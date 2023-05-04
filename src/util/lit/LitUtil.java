package util.lit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import task.formula.FormulaCreatorRandomizer;


//Methods for using literals in arrays;
public class LitUtil {

	public static final int getIndex(int lit, int numVars) {
		return lit + numVars;
	}

	public static final int getLit(int index, int numVars) {
		return index - numVars;
	}

	//Assumes ordered
	public static boolean isSubset(int[] s1, int[] s2) {
		int s2Ind = 0;

		for(int k = 0; k < s1.length; k++) {
			int testLit = s1[k];
			int testVar = Math.abs(testLit);
			for(; s2Ind < s2.length; s2Ind++) {
				int compareLit = s2[s2Ind];
				int compareVar = Math.abs(compareLit);

				if(testVar < compareVar) {
					return false;
				} else if(testVar == compareVar) {
					if(testLit == compareLit) {
						break;
					} else {
						return false;
					}
				}

				if(s2Ind == s2.length - 1) return false; //Did not find the kth literal
			}
		}

		return true;
	}

	public static int[] getAgreement(int[] oldModel, int[] nextModel) {
		int numAgree = 0;

		for(int k = 0; k < oldModel.length; k++) {
			if(oldModel[k] == nextModel[k]) {
				numAgree++;
			}
		}

		int[] ret = new int[numAgree];
		int retIndex = 0;
		for(int k = 0; k < oldModel.length; k++) {
			if(oldModel[k] == nextModel[k]) {
				ret[retIndex] = oldModel[k];
				retIndex++;
			}
		}

		return ret;
	}

	public static final int const_neg = Integer.MIN_VALUE+1;
	public static final int const_pos = Integer.MAX_VALUE-1;

	public static class RemovalDecoding {
		public int[] decoder;
		public boolean[] isRemoved;
		int size;

		public RemovalDecoding(int[] decoder, boolean[] isRemoved, int size) {
			this.decoder = decoder;
			this.isRemoved = isRemoved;
			this.size = size;
		}
	}

	public static int[] reverseDecode(int[] model, RemovalDecoding decoder) {
		int[] ret = new int[decoder.isRemoved.length];

		for(int k = 0; k < ret.length; k++) {
			int var = k+1;
			int decoding = decoder.decoder[var];
			int absDecoding = Math.abs(decoding);

			if(absDecoding <= model.length) {
				//If not a single val
				decoding = (decoding/absDecoding)*(model[absDecoding-1]);
			}

			if(decoding > 0) {
				ret[k] = var;
			} else {
				ret[k] = -var;
			}
		}
		
		return ret;
	}

	public static List<int[]> removeSingleValAndEquivVars(List<int[]> models, VariableContext context) {
		RemovalDecoding decoder = getSingleValAndEquivVarDecoding(models,context);
		return removeSingleValAndEquivVars(models,context,decoder);
	}

	public static List<int[]> removeSingleValAndEquivVars(List<int[]> models, VariableContext context, RemovalDecoding decoder) {
		ArrayList<int[]> ret = new ArrayList<int[]>(models.size());
		int size = decoder.size;

		for(int[] model : models) {
			int[] toAdd = new int[size];
			int toAddInd = 0;
			for(int k = 0; k < model.length; k++) {
				int var = Math.abs(model[k]);
				if(!decoder.isRemoved[var]) {
					toAdd[toAddInd] = (var/model[k])*decoder.decoder[var];
					toAddInd++;
				}
			}

			ret.add(toAdd);
		}

		context.ensureSize(size);
		return ret;
	}
	@Deprecated //Seems to be causing some problems
	public static RemovalDecoding getSingleValAndEquivVarDecoding(List<int[]> models, VariableContext context) {
		int[] firstModel = models.get(0); 
		int numVars = firstModel.length;
		int[] decoder = new int[numVars+1];

		boolean[] rem = new boolean[numVars+1];

		//First remove single-valued vars
		for(int k = 1; k <= numVars; k++) {
			if(rem[k]) continue;

			int val = firstModel[k-1];


			for(int[] model : models) {
				int lit = model[k-1];
				if(val != lit) {
					val = 0;
					break;
				}
			}

			if(val != 0) {
				if(val > 0) {
					decoder[k] = const_pos;
				} else {
					decoder[k] = const_neg;
				}
				rem[k] = true;
			}
		}

		//Then remove based on equality
		for(int k = 1; k <= numVars; k++) {
			if(rem[k]) continue;

			boolean[] areEqual = new boolean[numVars+1];
			boolean[] areInvEqual = new boolean[numVars+1];
			Arrays.fill(areEqual,true);
			Arrays.fill(areInvEqual,true);

			for(int[] model : models) {
				int lit = model[k-1];
				int sign = lit/Math.abs(lit);

				for(int j = k+1; j <= numVars; j++) {
					if(rem[j]) {
						areEqual[j] = false;
						areInvEqual[j] = false;
						continue;
					}
					int lit2 = model[j-1];
					int sign2 = lit2/Math.abs(lit2);

					if(sign != sign2) {
						areEqual[j] = false;
					} else {
						areInvEqual[j] = false;
					}
				}
			}

			for(int j = k+1; j <= numVars; j++) {
				if(areEqual[j]) {
					decoder[j] = k;
					rem[j] = true;
				} else if(areInvEqual[j]) {
					decoder[j] = -k;
					rem[j] = true;
				}
			}
		}

		int size = -1; //since 0 is in rem
		for(boolean b : rem) {
			if(!b) size++;
		}


		int nextNewVar = 1;
		for(int k = 0; k < firstModel.length; k++) {
			int var = Math.abs(firstModel[k]);
			if(!rem[var]) {
				decoder[var] = nextNewVar;
				for(int j = var+1; j <= numVars; j++) {
					if(decoder[j] == var) {
						decoder[j] = nextNewVar;
					} else if(decoder[j] == -var) {
						decoder[j] = -(nextNewVar);
					}
				}
				nextNewVar++;
			}
		}


		return new RemovalDecoding(decoder,rem,size);
	}
	
	public static CNF getEquaSymmetricModelsCNF(FormulaCreatorRandomizer randomizer,
			int origVars, List<int[]> mods) {
		CNF origCNF;
		ClauseList debug;
		origCNF = new CNF(new VariableContext());
		debug = new ClauseList(VariableContext.defaultContext);

		for(int[] i : mods) {
			i = i.clone();
			if(randomizer != null) {
				i = randomizer.translateToInternal(i);
			}
			
			debug.addClause(i.clone());
			for(int k = 0; k < i.length; k++) {
				i[k] = -i[k];
			}
			origCNF.fastAddClause(i);
		}
		origCNF.getContext().ensureSize(origVars);
		origCNF.sort();
		return origCNF;
	}
	
	public static int[] mergeForResolve(int[] c1, int[] c2) {
		int ind1 = 0;
		int ind2 = 0;
		
		boolean foundRes = false;
		
		IntList temp = new ArrayIntList(c1.length+c2.length);
		
		while(ind1 < c1.length && ind2 < c2.length) {
			int lit1 = c1[ind1];
			int var1 = Math.abs(lit1);
			int lit2 = c2[ind2];
			int var2 = Math.abs(lit2);
			
			if(var1 < var2) {
				temp.add(lit1);
				ind1++;
			} else if(var2 < var1) {
				temp.add(lit2);
				ind2++;
			} else { //equal
				if(lit1 == lit2) {
					temp.add(lit1);
					ind1++;
					ind2++;
				} else {
					if(foundRes) {
						return null; // tautology
					} else {
						foundRes = true;
						ind1++;
						ind2++;
					}
				}
			}
		}
		for(int k = ind1; k < c1.length; k++) {
			temp.add(c1[k]);
		}
		
		for(int k = ind2; k < c2.length; k++) {
			temp.add(c2[k]);
		}
		
		if(!foundRes) {
			return null;
		}
		
		int[] ret = new int[temp.size()];
		
		for(int k = 0; k < temp.size(); k++) {
			ret[k] = temp.get(k);
		}
		return ret;
	}
}
