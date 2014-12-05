package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import util.lit.LitSorter;
import util.lit.LitsSet;
import formula.VariableContext;


//Assumed to be literal permutations (so element 0 is ignored)
public class PermutationUtil {

	public static int[] permute(int[] input, int[] permutation) {
		int[] ret = new int[input.length];
		
		for(int k = 0; k < input.length; k++) {
			int curVar = input[k];
			int permIndex = Math.abs(curVar);
			int toReplace = permutation[permIndex];

			ret[k] = toReplace*(curVar > 0 ? 1 : -1);
		}
		return ret;
	}
	
	public static int permute(int input, int[] permutation) {
		int permIndex = Math.abs(input);
		int toReplace = permutation[permIndex];

		int ret = toReplace*(input > 0 ? 1 : -1);
		
		return ret;
	}
	
	public static int[] permutePartial(int[] input, int[] permutation) {
		int[] ret = new int[input.length];
		
		for(int k = 0; k < input.length; k++) {
			int curVar = input[k];
			int permIndex = Math.abs(curVar);
			int toReplace = permutation[permIndex];
			
			if(toReplace == 0) return null;

			ret[k] = toReplace*(curVar > 0 ? 1 : -1);
		}
		return ret;
	}
	
	
	//Permutes and sorts literals
	public static int[] permuteClause(int[] clause, int[] permutation) {
		int[] ret = permute(clause,permutation);
		LitSorter.inPlaceSort(ret);
		return ret;
	}
	
	//Permutes and sorts literals
	public static int[] permuteClauseParital(int[] clause, int[] permutation) {
		int[] ret = permutePartial(clause,permutation);
		if(ret != null) {
			LitSorter.inPlaceSort(ret);
		}
		return ret;
	}
	
	public static int[] getInverse(int[] permutation) {
		int[] ret = new int[permutation.length];
		
		for(int k = 0; k < permutation.length; k++) {
			int curVar = permutation[k];
			int permIndex = Math.abs(curVar);
			
			ret[permIndex] = curVar > 0 ? k : -k;
		}
		return ret;
	}
	
	public static int[][] getCycleRepresentation(int[] permutation) {
		List<List<Integer>> temp = new ArrayList<List<Integer>>();
		
		boolean[] done = new boolean[permutation.length];
		done[0] = true;
		
		int curLit = 0;
		List<Integer> curCycle = null;
		
		while(true) {
			if(curLit == 0) {
				int prevFirst = 0;
				if(curCycle != null) {
					prevFirst = curCycle.get(0);	
				}
				 
				curCycle = new ArrayList<Integer>();
				int k;
				for(k = prevFirst; k < done.length; k++) {
					if(!done[k]) {
						curLit = k;
						break;
					}
				}
				if(k == done.length) {
					break; //Everyone is done
				}
			}
			
			done[Math.abs(curLit)] = true;
			
			curCycle.add(curLit);
			
			int next = permutation[Math.abs(curLit)];
			//if(inverted) next = -next;
			if(curLit < 0) next = -next;
			
//			if(Math.signum(next) != Math.signum(curLit)) {
//				inverted = !inverted;
//			}
			
			if(curCycle.get(0) == next) {
				temp.add(curCycle);
				curLit = 0;
			} else {
				curLit = next;
			}
		}
		
		int[][] cycles = new int[temp.size()][];
		
		for(int k = 0; k < temp.size(); k++) {
			curCycle = temp.get(k);
			cycles[k] = new int[curCycle.size()];
			
			for(int i = 0; i < curCycle.size(); i++) {
				cycles[k][i] = curCycle.get(i);
			}
		}
		
		return cycles;
	}
	
	public static String getPrettyCycles(int[][] cycles) {
		return getPrettyCycles(cycles,true);
	}
	
	
	public static String getPrettyCycles(int[][] cycles, boolean showUnit) {
		return getPrettyCycles(cycles,showUnit,null);
	}
	public static String getPrettyCycles(int[][] cycles, boolean showUnit,  VariableContext context) {
		StringBuilder sb = new StringBuilder();
		for(int[] i : cycles) {
			if(!showUnit && i.length == 1) continue; 
			sb.append('(');
			
			for(int j : i) {
				if(context == null) {
					sb.append(j).append(' ');
				} else {
					sb.append(j < 0 ? "-" : "");
					sb.append(context.getVar(Math.abs(j))).append(' ');
				}
			}
			
			sb.deleteCharAt(sb.length()-1);
			sb.append(')');
		}
		
		if(sb.length() == 0) { //e.g. is identity and showUnit off
			sb.append("()");
		}
		return sb.toString();
	}
	
	public static Set<int[]> getAllPermsFromGenerators(Iterable<int[]> generators) {
		LitsSet ret = null;
		Queue<int[]> toComp = new LinkedList<int[]>();
		
		for(int[] gen : generators) {
			if(ret == null) {
				ret = new LitsSet(gen.length);
			}
			ret.add(gen);
			toComp.add(gen);
		}
		
		while(!toComp.isEmpty()) {
			int[] cur = toComp.poll();
			
			for(int[] gen : generators) {
				int[] toAdd = permute(cur,gen);
				
				if(!ret.contains(toAdd)) {
					ret.add(toAdd);
					toComp.add(toAdd);
				}
			}
		}
		
		return ret;
	}
}
