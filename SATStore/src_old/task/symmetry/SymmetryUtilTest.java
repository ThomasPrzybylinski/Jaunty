package task.symmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

import util.LitsSet;
import util.PermutationUtil;
import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;

public class SymmetryUtilTest {

	public static final Comparator<int[]> LEX_LEADER_COMP = new Comparator<int[]> () {
		@Override
		public int compare(int[] o1, int[] o2) {
			for(int i = 0; i < o1.length; i++) {
				if(o1[i] < o2[i]) {
					return -1;
				} else if(o1[i] > o2[i]) {
					return 1;
				}
			}

			return 0;
		}
	};

	public static final Comparator<int[]> LEX_FOLLOWER_COMP = new Comparator<int[]> () {
		@Override
		public int compare(int[] o1, int[] o2) {
			for(int i = 0; i < o1.length; i++) {
				if(o1[i] < o2[i]) {
					return 1;
				} else if(o1[i] > o2[i]) {
					return -1;
				}
			}

			return 0;
		}
	};

	public static List<int[]> getSyms(ClauseList cl) {
		SimpleSymFinder finder = new SimpleSymFinder(cl);
		return finder.getSyms();
	}

	public static List<int[]> breakModels(List<int[]> models, List<int[]> syms) {
		return breakModels(models,syms,LEX_LEADER_COMP);
	}

	public static List<int[]> breakModels(List<int[]> models, List<int[]> syms, Comparator<int[]> comp) {
		ArrayList<int[]> ret = new ArrayList<int[]>();

		for(int[] curModel : models) {
			boolean add = true;

			for(int[] perm : syms) {
				int[] permuted = PermutationUtil.permuteClause(curModel,perm);
				int result = comp.compare(curModel, permuted);
				if(result < 0) {
					add = false;
					break;
				}
			}

			if(add) {
				ret.add(curModel);
			}
		}

		return ret;
	}

	public static Collection<int[]> breakModelsCarefully(Collection<int[]> models, Collection<int[]> syms, Comparator<int[]> comp) {
		ArrayList<int[]> ret = new ArrayList<int[]>();

		for(int[] curModel : models) {
			boolean add = true;

			for(int[] perm : syms) {
				int[] permuted = PermutationUtil.permuteClause(curModel,perm);

				if(models.contains(permuted)) {
					int result = comp.compare(curModel, permuted);
					if(result < 0) add = false;
				}
			}

			if(add) {
				ret.add(curModel);
			}
		}

		return ret;
	}

	//Assumption: no clause has a pos and neg literal of the same variable
	//Maybe a better name is to get the "Dual" list
	//That is clauses become variables, and variables become clauses (essentially)
	public static ClauseList getInverseList(ClauseList orig) {
		VariableContext context = new VariableContext();

		List<int[]> clauses = orig.getClauses();

		ClauseList ret = new ClauseList(context);

		//Variables are clauses
		for(int k = 0; k < clauses.size(); k++) {
			context.getOrCreateVar("C"+(k+1));
		}

		for(int k = 0; k < orig.getContext().getNumVarsMade(); k++) {
			List<Integer> posTemp = new LinkedList<Integer>();
			List<Integer> negTemp = new LinkedList<Integer>();
			for(int i = 0; i < clauses.size(); i++) {
				int[] clause = clauses.get(i);
				boolean addPos = false;
				boolean addNeg = false;
				for(int lit : clause) {
					if(lit == k) {
						addPos = true;
						break;
					} else if(lit == -k) {
						addNeg = true;
						break;
					}
				}

				if(addPos) {
					posTemp.add(i+1); //Clause var UID list index + 1
				} else if(addNeg) {
					negTemp.add(i+1);
				}
			}

			int[] clause = new int[posTemp.size() + negTemp.size()];
			int index = 0;
			for(int i : posTemp) {
				clause[index] = i;
				index++;
			}

			for(int i : negTemp) {
				clause[index] = -i;
				index++;
			}

			//One of these clauses may be empty
			//But that should be ok and 
			//makes the result more comprehensible
			ret.addClause(clause);
		}

		return ret;
	}

	public static boolean[][] getSymmetryGraph(VariableContext context, List<int[]> models) {
		boolean[][] graphMatrix = new boolean[models.size()][models.size()];
		int num = 0;
		int total = (models.size()*models.size())/2;
		int ratio = total/10;
		
		for(int k = 0; k < models.size(); k++) {
			for(int i = k+1; i < models.size(); i++) {
				num++;
//				if(num%ratio == 0) System.out.println(num);
				
				final int[] model1 = models.get(k);
				final int[] model2 = models.get(i);


				int[] agreement = getAgreement(model1,model2);

				boolean doAgree = false;

				DNF agree = new DNF(context);

				setupAgreedModels(agree,models,agreement);

				if(agree.getClauses().size() == 2) {
					doAgree = true;
				} else {
					AgreementAction act = new AgreementAction(agree.getClauses(),model1,model2);

					int m1Index = -1;
					int m2Index = -1;
					for(int j = 0; j < agree.getClauses().size(); j++) {
						if(m1Index != -1 && m2Index != -1) break;
						int[] clause = agree.getClauses().get(j);

						if(Arrays.equals(clause,model1)) {
							m1Index = j;
						}

						if(Arrays.equals(clause,model2)) {
							m2Index = j;
						}
					}

					ClauseList inverse = getInverseList(agree);
					SimpleSymFinder finder = new SimpleSymFinder(inverse);
					finder.getSyms(act,new int[]{m1Index+1,m2Index+1});
					doAgree = act.doAgree;
				}

				if(doAgree) {
					graphMatrix[k][i] = true;
					graphMatrix[i][k] = true;
				}
			}
		}
		return graphMatrix;
	}

	private static class AgreementAction implements FoundSymmetryAction {
		int m1Index = -1;
		int m2Index = -1;
		int numFound = 0;
		private List<int[]> models;

		public boolean doAgree = false;
		public AgreementAction(List<int[]> models, int[] model1,int[] model2) {
			this.models = models;
			for(int k = 0; k < models.size(); k++) {
				if(m1Index != -1 && m2Index != -1) {
					break;
				}
				if(Arrays.equals(model1,models.get(k))) {
					m1Index = k;
				} else if(Arrays.equals(model2,models.get(k))) {
					m2Index = k;
				}
			}
		}

		@Override
		public boolean foundSymmetry(int[] perm) {
			numFound++;
			if(numFound%100000 == 0) {
				System.out.println("F"+numFound);
			}

			int m1PermIndex = m1Index+1;
			int m2PermIndex = m2Index+1;

			if(m1PermIndex == perm[m2PermIndex] || m2PermIndex == perm[m1PermIndex]) {
				doAgree = true;
				return false;
			}
			return true;
		}
	}

	private static void setupAgreedModels(DNF agree, List<int[]> models, int[] agreement) {
		for(int[] model : models) {
			boolean keep = true;

			for(int k = 0; k < model.length; k++) {
				if(agreement[k] != 0 && model[k] != agreement[k]) {
					keep = false;
					break;
				}
			}

			if(keep) {
				agree.fastAddClause(model);
			}
		}
	}

	private static int[] getAgreement(int[] model1, int[] model2) {
		int[] agree = new int[model1.length];

		for(int k = 0; k < model1.length; k++) {
			if(model1[k] == model2[k]) {
				agree[k] = model1[k];
			}
		}
		return agree;

	}

	//Gets local syms that exist from satisfiable unit conditions
	//
	public static Collection<int[]> breakyByUnitLocalSyms(DNF dnf) throws TimeoutException{
		LitsSet ret = new LitsSet(dnf.getContext().getNumVarsMade());
		ret.addAll(dnf.getClauses());

		List<int[]> debug = new ArrayList<int[]>();

		Set<Integer> units = new HashSet<Integer>();

		for(int[] model : dnf.getClauses()) {
			for(int i : model) {
				units.add(i);
			}
		}

		for(int i : units) {
			DNF reduced = dnf.subst(i,true); //negative carried out by making neg lit true

			ClauseList list = new ClauseList(reduced.getContext());

			int stillAround = 0;
			for(int[] j : reduced.getClauses()) {
				int[] toAdd = new int[j.length+1];
				System.arraycopy(j,0,toAdd,1,j.length);
				toAdd[0] = i;
				list.addClause(toAdd);

				if(ret.contains(list.getClauses().get(list.getClauses().size()-1))) {
					stillAround++;
				}
			}



			if(stillAround > 2) {
				ClauseList real = getInverseList(reduced);//.reduce();
				SimpleSymFinder finder = new SimpleSymFinder(real);
				InverseBreakingAction ba = new InverseBreakingAction(list.getClauses());
				finder.getSyms(ba);

				ret.removeAll(ba.getRemovedModels());
			} else if(stillAround == 2) {
				ret.remove(list.getClauses().get(0));
			}
			debug.clear();
			for(int[] asd : ret) {
				debug.add(asd);
			}
		}

		return ret;
	}

	//This class assumes that we are breaking the inverse version.
	//So Variable UID k is model at index k-1
	private static class InverseBreakingAction implements FoundSymmetryAction {
		private List<int[]> models;
		private boolean[] isRemoved;
		int times = 0;

		public InverseBreakingAction(List<int[]> curModels) {
			this.models = curModels;
			isRemoved = new boolean[curModels.size()];
		}

		public List<int[]> getRemovedModels() {
			List<int[]> ret = new ArrayList<int[]>();
			for(int k = 0; k < models.size(); k++) {
				if(isRemoved[k]) {
					ret.add(models.get(k));
				}
			}

			return ret;
		}

		@Override
		public boolean foundSymmetry(int[] perm) {
			for(int k = 1; k < perm.length; k++) {
				int realIndex = k-1;

				if(perm[k] != k) {
					int otherIndex = perm[k]-1;
					int result = LEX_LEADER_COMP.compare(models.get(realIndex), models.get(otherIndex));
					if(result < 0) {
						isRemoved[realIndex] = true;
					} else if(result > 0) {
						isRemoved[otherIndex] = true;
					}
				}
			}

			//			for(int[] curModel : curModels) {
			//				int[] permuted = PermutationUtil.permute(curModel,perm);
			//
			//				boolean add = true;
			//				while(!Arrays.equals(curModel, permuted)) {
			//					boolean removeOther = false;
			//
			//					if(curModels.contains(permuted)) {
			//						int result = LEX_LEADER_COMP.compare(curModel, permuted);
			//						if(result < 0) add = false;
			//						else if(result > 0) removeOther = true;
			//					}
			//
			//					if(removeOther) {
			//						removedModels.add(permuted);
			//					}
			//					permuted = PermutationUtil.permute(permuted,perm);
			//				}
			//				if(add) {
			//					ret.add(curModel);
			//				} else {
			//					removedModels.add(curModel);
			//				}
			//			}

			times++;

			int numAround = 0;
			boolean doContinue = false;
			for(boolean b : isRemoved) {
				if(!b) {
					numAround++;
					if(numAround > 1) {
						doContinue = true;
						break;
					}
				}
			}

			return doContinue;
		}
	}



}
