package task.symmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

import task.formula.QueensToSAT;
import util.DisjointSet;
import util.PermutationUtil;
import util.lit.LitsSet;
import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;

public class SymmetryUtil {

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
	
	public static DisjointSet<int[]> findSymmetryOrbitsNEW(ClauseList models) {
		ClauseList dual = getInverseList(models);
		SimpleSymFinder ssf = new SimpleSymFinder(dual);
		
		DisjointSet<Integer> clauseThingys = ssf.getSymOrbits();
		
		DisjointSet<int[]> symmetricPartition = new DisjointSet<int[]>(models.getClauses());
		for(int k = 0; k < models.getClauses().size(); k++) {
			if(!clauseThingys.getRootOf(k+1).equals(k+1)) {
				symmetricPartition.join(models.getClauses().get(k),models.getClauses().get(clauseThingys.getRootOf(k+1)-1));
			}
		}
		
		return symmetricPartition;
	}

	public static DisjointSet<int[]> findSymmetryOrbits(ClauseList models) {
		DisjointSet<int[]> symmetricPartition = new DisjointSet<int[]>(models.getClauses());
		return findSymmetryOrbits(models,symmetricPartition);
	}
	

		
	public static DisjointSet<int[]> findSymmetryOrbits(ClauseList models, DisjointSet<int[]> symmetricPartition) {
		
		List<int[]> modList = models.getClauses();
		BitSet partitioned = new BitSet(modList.size()); //True if assigned to a partition, false otherwise

		
		ClauseList dual = getInverseList(models);

		ExistsSymmetricAction act = new ExistsSymmetricAction();
		SimpleSymFinder ssf = new SimpleSymFinder(dual);
		
		//		System.out.println(dual);
		//		System.out.println(dual.reduce());


		//TODO: I can increase efficiency by not calculating those models
		//that have already been put into a partition

		//Partition models
		for(int k = 0; k < modList.size(); k++) {
			if(partitioned.get(k)) continue; //If been partitioned already, then parition is complete
			//System.out.println(k);
			partitioned.set(k);
			int[] m1 = modList.get(k);
			for(int i = k+1; i < modList.size(); i++) {
				int[] m2 = modList.get(i);
				if(partitioned.get(i) || symmetricPartition.sameSet(m1,m2)) continue;
				//System.out.print("_"+i);
				
				
				act.setSymFound(false);
				ssf.getSyms(act,new int[]{k+1,i+1});
				if(act.isSymFound()) {
					for(int j = 0; j < act.perm.length; j++) {
						if(act.perm[j] != j) {
							symmetricPartition.join(modList.get(j-1),modList.get(act.perm[j]-1));
						}
					}
					
//					symmetricPartition.join(m1,m2);
					partitioned.set(i);
				}
			}


		}

		return symmetricPartition;
	}

	//Assumption: no clause has a pos and neg literal of the same variable
	//Maybe a better name is to get the "Dual" list
	//That is clauses become variables, and variables become clauses (essentially)
	public static ClauseList getInverseList(ClauseList orig) {
		List<int[]> clauses = orig.getClauses();
		int numVars = orig.getContext().size();
		
		return getInverseList(clauses, numVars);
	}

	public static ClauseList getInverseList(List<int[]> clauses, int numVars) {
		VariableContext context = new VariableContext();

		ClauseList ret = new ClauseList(context);


		//Variables are clauses
		for(int k = 0; k < clauses.size(); k++) {
			//context.getOrCreateVar("C"+(k+1));
			context.createNextDefaultVar();
		}

		for(int k = 0; k < numVars; k++) {
			int curVar = k+1;
			List<Integer> allTemp = new LinkedList<Integer>();

			for(int i = 0; i < clauses.size(); i++) {
				int[] clause = clauses.get(i);

				for(int lit : clause) {
					if(lit == curVar) {
						allTemp.add(i+1);
						break;
					} else if(lit == -curVar) {
						allTemp.add(-(i+1));
						break;
					}
				}
			}

			int[] allClause = new int[allTemp.size()];
			int index = 0;
			for(int i : allTemp) {
				allClause[index] = i;
				index++;
			}


			int[] invAllClause = new int[allTemp.size()];
			index = 0;
			for(int i : allTemp) {
				invAllClause[index] = -i;
				index++;
			}

			//One of these clauses may be empty
			//But that should be ok and 
			//makes the result more comprehensible
			ret.fastAddClause(allClause);
			ret.fastAddClause(invAllClause);
			//			ret.fastAddClause(posClause);
			//			ret.fastAddClause(negClause);
		}

		//		int[] allClausesClause = new int[clauses.size()];
		//		for(int k = 0; k < clauses.size(); k++) {
		//			allClausesClause[k] = k+1;
		//		}
		//		
		//		ret.fastAddClause(allClausesClause);

		//Make sure only pos to pos symmetries are possible
		for(int k = 0; k < context.size(); k++) {
			ret.fastAddClause(new int[]{k+1});
		}

		return ret;
	}

	public static boolean[][] getSymmetryGraph(VariableContext context, List<int[]> models) {
		boolean[][] graphMatrix = new boolean[models.size()][models.size()];
		int num = 0;
		int total = (models.size()*models.size())/2;
		int ratio = total/10;

		for(int k = 0; k < models.size(); k++) {
			graphMatrix[k][k] = true;
			for(int i = k+1; i < models.size(); i++) {
				num++;
				//				if(num%ratio == 0) System.out.println(num);

				final int[] model1 = models.get(k);
				final int[] model2 = models.get(i);

				boolean doAgree = doModelsAgreeSym(context, models, model1,
						model2);

				if(doAgree) {
					graphMatrix[k][i] = true;
					graphMatrix[i][k] = true;
				}
			}
		}
		return graphMatrix;
	}

	public static boolean doModelsAgreeSym(VariableContext context,
			List<int[]> models, final int[] model1, final int[] model2) {
		int[] agreement = getAgreement(model1,model2);

		boolean doAgree = getLocalSymAgreement(context, models, model1, model2,
				agreement);
		return doAgree;
	}

	public static boolean getLocalSymAgreement(VariableContext context,
			List<int[]> models, final int[] model1, final int[] model2,
			int[] filter) {
		boolean doAgree = false;

		DNF agree = new DNF(context);

		filterModelsForAgreement(agree,models,filter);

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
		return doAgree;
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

	public static void filterModelsForAgreement(DNF agree, List<int[]> models, int[] filter) {
		for(int[] model : models) {
			boolean keep = true;

			for(int k = 0; k < model.length; k++) {
				if(filter[k] != 0 && model[k] != filter[k]) {
					keep = false;
					break;
				}
			}

			if(keep) {
				agree.fastAddClause(model);
			}
		}
	}
	
	public static List<int[]> filterModels(List<int[]> models, int[] filter) {
		List<int[]> ret = new ArrayList<int[]>();
		for(int[] model : models) {
			boolean keep = true;

			for(int k = 0; k < model.length; k++) {
				if(filter[k] != 0 && model[k] != filter[k]) {
					keep = false;
					break;
				}
			}

			if(keep) {
				ret.add(model);
			}
		}
		
		return ret;
	}

	public static ClauseList filterModels(VariableContext context, List<int[]> models, int[] filter) {
		ClauseList ret = new ClauseList(context);
		for(int[] model : models) {
			boolean keep = true;

			for(int k = 0; k < model.length; k++) {
				if(filter[k] != 0 && model[k] != filter[k]) {
					keep = false;
					break;
				}
			}

			if(keep) {
				ret.fastAddClause(model);
			}
		}
		
		return ret;
	}
	
	public static int[] getAgreement(int[] model1, int[] model2) {
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
		LitsSet ret = new LitsSet(dnf.getContext().size());
		ret.addAll(dnf.getClauses());

		List<int[]> debug = new ArrayList<int[]>();

		Set<Integer> units = new HashSet<Integer>();

		for(int[] model : dnf.getClauses()) {
			for(int i : model) {
				units.add(i);
			}
		}
		System.out.println(dnf.getClauses().size());
		System.out.println(units.size());
		for(int i : units) {
			System.out.print(i + " ");
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

			System.out.println((new QueensToSAT(8)).decode(list.getClauses().get(0)));
			System.out.println((new QueensToSAT(8)).decode(list.getClauses().get(1)));


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

	private static class ExistsSymmetricAction implements FoundSymmetryAction {
		private boolean symFound = false;
		private int[] perm = null;
		@Override
		public boolean foundSymmetry(int[] perm) {
			symFound = true;
			this.perm = perm;
			return false;
		}
		public boolean isSymFound() {
			return symFound;
		}
		public void setSymFound(boolean symFound) {
			this.symFound = symFound;
		}


	}


}
