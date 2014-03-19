import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import task.formula.QueensToSAT;
import task.symmetry.RealSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalModelSymClauses;
import util.IntPair;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import formula.VariableContext;
import formula.simple.ClauseList;
import formula.simple.DNF;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.SchreierVector;


public class TempAgreeTests {

	public static void main(String[] args) throws Exception {
		ModelGiver giver = new CNFCreatorModelGiver(new QueensToSAT(8));//new AllSquares(7); //new CNFCreatorModelGiver(new SpanningCyclesCreator(7)); //new SimpleCNFCreator(9,2.3,3));
		List<int[]> reps = giver.getAllModels(new VariableContext());//giver.getDNF().getClauses();
		
		while(reps.size() == 0) {
			reps = giver.getAllModels(new VariableContext());
		}
		
		
		DNF dnf = new DNF(new VariableContext());
		dnf.addAll(reps);
		reps = dnf.getClauses();
		
		System.out.println( (new RealSymFinder(dnf)).getSymGroup());
		
//		PossiblyDenseGraph<int[]> agree = new PossiblyDenseGraph<int[]>(reps);
		TreeSet<IntPair> seen = new TreeSet<IntPair>();
		TreeSet<IntPair> globSeen = new TreeSet<IntPair>();
		TreeSet<IntPair> symPairs = new TreeSet<IntPair>();
		LocalModelSymClauses representatives = new LocalModelSymClauses(dnf);
		
		LiteralGroup glob = (new RealSymFinder(representatives.getCurList())).getSymGroup();
		
		for(int k = 0; k < reps.size(); k++) {
			int[] rep1 = reps.get(k);
			for(int i = k+1; i < reps.size(); i++) {
				if(seen.contains(new IntPair(k,i))) continue;
				
				representatives.post();
				int[] rep2 = reps.get(i);
				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
				
				for(int j = 0; j < agreement.length; j++) {
					if(agreement[j] > 0) {
						representatives.addCondition(j+1);
						System.out.print(j+1+" ");
					} else if(agreement[j] < 0) {
						representatives.addCondition(-(j+1));
						System.out.print(-(j+1)+" ");
					}
				}
				System.out.println();
				
				ClauseList cl = representatives.getCurList();
				RealSymFinder syms = new RealSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();
				SchreierVector vec = new SchreierVector(group);
				TreeSet<IntPair> curPairs = new TreeSet<IntPair>();
				
				
				for(int j = 0; j < group.size(); j++) {
					for(int h = j+1; h < group.size(); h++) {
						if(vec.sameOrbit(j,h)) {
							curPairs.add(new IntPair(j,h));
						}
					}
				}
				
				computePairs(curPairs,glob,seen);
				symPairs.addAll(curPairs);
				
				globSeen.add(new IntPair(k,i));
				
				TreeSet<IntPair> curSymPairs = new TreeSet<IntPair>();
				curSymPairs.add(new IntPair(k,i));
				computePairs(curSymPairs,glob, new TreeSet());
				seen.addAll(curSymPairs);
				
				representatives.pop();

			}
		}
		System.out.println(globSeen.size());
		System.out.println(globSeen);
		System.out.println(seen.size());
		System.out.println(seen);
	}

	private static void computePairs(TreeSet<IntPair> curPairs,
			LiteralGroup glob, TreeSet<IntPair> seen) {
		LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
		toCompute.addAll(curPairs);
		
		while(!toCompute.isEmpty()) {
			IntPair curPair = toCompute.removeFirst();
			if(seen.contains(curPair)) continue;
			curPairs.add(curPair);
			
			for(LiteralPermutation perm : glob.getGenerators()) {
				IntPair next = curPair.applySort(perm);
				if(!curPairs.contains(next) && !seen.contains(next)) {
					toCompute.add(next);
				}
			}
		}
		
	}
}
