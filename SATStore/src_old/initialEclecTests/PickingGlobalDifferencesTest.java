package initialEclecTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import task.formula.QueensToSAT;
import task.sat.SATUtil;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import util.PartitionIterator;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import formula.simple.DNF;

public class PickingGlobalDifferencesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VariableContext context = VariableContext.defaultContext;

		QueensToSAT create = new QueensToSAT(8);
		CNF cnf = create.encode(context);

		System.out.println(cnf);
		System.out.println(cnf.toNumString());

		List<int[]> models = SATUtil.getAllModels(cnf);
		Collections.sort(models,SymmetryUtil.LEX_FOLLOWER_COMP);
		List<int[]> origModels = new ArrayList<int[]>(models);
		
		DNF dnfForSym = new DNF(cnf.getContext());
		for(int[] i : models) {
			dnfForSym.addClause(i);
		}
		
		DisjointSet<int[]> symmetricPartition = SymmetryUtil.findSymmetryOrbitsNEW(dnfForSym);
		
//		List<int[]>	perms = SymmetryUtil.getSyms(dnfForSym);
//
//		DisjointSet<int[]> symmetricPartition = new DisjointSet<int[]>(models);
//		
//		//Partition models
//		for(int k = 0; k < models.size(); k++) {
//			int[] m1 = models.get(k);
//			for(int i = k+1; i < models.size(); i++) {
//				int[] m2 = models.get(i);
//				if(!symmetricPartition.sameSet(m1,m2)) {
//					for(int[] perm : perms) {
//						if(Arrays.equals(m1, PermutationUtil.permute(m2,perm))) {
//							symmetricPartition.join(m1,m2);
//							break;
//						}
//					}
//				}
//			}
//		}
		
		HashSet<int[]> roots = symmetricPartition.getRoots();
		HashMap<int[],Integer> rootIndex = new HashMap<int[],Integer>();
		List<List<int[]>> partitions = new ArrayList<List<int[]>>(roots.size());
		
		for(int[] root : roots) {
			rootIndex.put(root,partitions.size());
			ArrayList<int[]> part = new ArrayList<int[]>();
			partitions.add(part);
		}
		
		for(int[] model : models) {
			int partIndex = rootIndex.get(symmetricPartition.getRootOf(model));
			partitions.get(partIndex).add(model);
		}
		
		int[] sizes = new int[partitions.size()];
		for(int k = 0; k < partitions.size(); k++) {
			sizes[k] = partitions.get(k).size();
		}
		System.out.println(Arrays.toString(sizes));
		sizes[0] = 1; //Reduce num we need to look at because of symmetry
		
		
		PartitionIterator iter = new PartitionIterator(sizes);
		long max = Long.MIN_VALUE;
		int[] curMax = null;
		
		ArrayList<int[]> curMods = new ArrayList<int[]>(partitions.size());
		
		int[] brokensizes = new int[partitions.size()];
		while(iter.hasNext()) {
			int[] next = getRandNext(sizes);//iter.next();//
			next = hillClimb(next,sizes,partitions,dnfForSym);
			//System.out.println(Arrays.toString(next));
			curMods.clear();
			for(int k = 0; k < next.length; k++) {
				curMods.add(partitions.get(k).get(next[k]));
			}
			ClauseList temp = new ClauseList(dnfForSym.getContext());
			temp.fastAddAll(curMods);
			DisjointSet<int[]> newParts = SymmetryUtil.findSymmetryOrbits(temp);
			int numParts = newParts.getRoots().size();
			
			if(brokensizes[numParts-1] == 0) {
				brokensizes[numParts-1] = 1;
				
				System.out.println(numParts);
				System.out.println(Arrays.toString(next));
				for(int[] mod : newParts.getRoots()) {
					 System.out.println(create.decode(mod));
				}
				System.out.println();
			}
			
//			int[][] lengths = PrototypesUtil.doAgreementSym(curMods,dnfForSym.getContext());
//			long total = 0;
//			for(int k = 0; k < lengths.length; k++) {
//				for(int i = 0; i < lengths[k].length; i++) {
//					total += lengths[k][i];
//				}
//			}
//			if(total > max) {
//				max = total;
//				curMax = next;
//				
//				for(int[] row : lengths) {
//					System.out.println(Arrays.toString(row));
//				}
//				//clusterAndDisplay(curMods,lengths,dnfForSym.getContext().getNumVarsMade());
//				printSomeThingys(lengths, curMods, create);
//				System.out.println();
//				
//			}
		}
		
		System.out.println(Arrays.toString(curMax));
	}
	
	private static int[] hillClimb(int[] next, int[] sizes, List<List<int[]>> partitions,
			DNF dnfForSym) {
		ArrayList<int[]> curMods = new ArrayList<int[]>(partitions.size());
		int[] ret = new int[next.length];
		
		for(int i = 0; i < next.length; i++) {
			curMods.add(partitions.get(i).get(next[i]));
		}
		
		int minParts = Integer.MAX_VALUE;
		for(int k = 0; k < next.length; k++) {
			for(int j = 0; j < sizes[k]; j++) {
				curMods.set(k,partitions.get(k).get(j));
				
				ClauseList temp = new ClauseList(dnfForSym.getContext());
				temp.fastAddAll(curMods);
				DisjointSet<int[]> newParts = SymmetryUtil.findSymmetryOrbits(temp);
				int numParts = newParts.getRoots().size();
				if(numParts < minParts) {
					minParts = numParts;
					System.arraycopy(next,0,ret,0,next.length);
					ret[k] = j;
				}
			}
			curMods.set(k,partitions.get(k).get(next[k]));
			
		}
		return ret;
	}

	private static Random rand = new Random();
	private static int[] getRandNext(int[] sizes) {
		int[] ret = new int[sizes.length];
		for(int k = 0; k < sizes.length; k++) {
			ret[k] = rand.nextInt(sizes[k]);
		}
		return ret;
	}

	private static void printSomeThingys(int[][] lens, List<int[]> models, QueensToSAT creator) {
		first: for(int k = 0; k < lens.length; k++) {
			for(int i = 0; i < lens[k].length; i++) {
				if(lens[k][i] > 1) {
					System.out.println(creator.decode(models.get(k)));
					
					for(int j = 0; j < lens[k].length; j++) {
						if(k == j) continue;
						if(lens[k][j] > 1) {
							System.out.println(creator.decode(models.get(j)));
						}
					}
					break first;
				}
			}
		}
	}

}
