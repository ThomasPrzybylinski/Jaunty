package task.symmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.lit.LitUtil;

//This implementation assumes the arrays are isomorphic
public class OrderedPartitionPair {
	private List<List<Integer>> top;
	private List<List<Integer>> bottom;
	private int num;

	//Create empty partition
	public OrderedPartitionPair() {
		num = 0;
		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();
	}

	public OrderedPartitionPair(int num) {
		this(num,true);
	}

	@SuppressWarnings("unchecked")
	protected OrderedPartitionPair(int num, boolean addNums) {
		this.setNum(num);

		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();

		if(addNums) {
			ArrayList<Integer> ints = new ArrayList<Integer>();

			for(int k = 1; k <=num; k++) {
				ints.add(k);
				ints.add(-k);
			}

			top.add(ints);
			bottom.add((ArrayList<Integer>)ints.clone());
		}
	}

	//Won't work if numbers are not contiguous 
	public OrderedPartitionPair(List<List<Integer>> initial) {
		this.num = -1;

		top = new ArrayList<List<Integer>>(initial.size());
		bottom = new ArrayList<List<Integer>>(initial.size());

		for(List<Integer> part : initial) {
			ArrayList<Integer> topPart = new ArrayList<Integer>(part.size());
			ArrayList<Integer> bottomPart = new ArrayList<Integer>(part.size());

			for(Integer i : part) {
				topPart.add(i);
				bottomPart.add(i);
				this.num = Math.max(this.num,Math.abs(i));
			}

			top.add(topPart);
			bottom.add(bottomPart);
		}
		this.setNum(num); //for debugging purposes
	}

	//Won't work if numbers are not contiguous 
	protected OrderedPartitionPair(List<List<Integer>> toTop, List<List<Integer>> toBottom) {
		this.num = -1;

		top = new ArrayList<List<Integer>>(toTop.size());
		bottom = new ArrayList<List<Integer>>(toBottom.size());

		for(List<Integer> part : toTop) {
			ArrayList<Integer> topPart = new ArrayList<Integer>(part.size());

			for(Integer i : part) {
				topPart.add(i);
				this.num = Math.max(this.num,Math.abs(i));
			}

			top.add(topPart);
		}
		
		for(List<Integer> part : toBottom) {
			ArrayList<Integer> bottomPart = new ArrayList<Integer>(part.size());

			for(Integer i : part) {
				bottomPart.add(i);
				this.num = Math.max(this.num,Math.abs(i));
			}

			bottom.add(bottomPart);
		}
		
		this.setNum(num); //for debugging purposes
	}

	//Make sure is isomorphic OPP
	public boolean checkIsomorphic() {
		return checkIsomorphic(top,bottom);
	}

	private static boolean checkIsomorphic(List<List<Integer>> top, List<List<Integer>> bottom) {
		if(top.size() != bottom.size()) return false;

		for(int k = 0; k < top.size(); k++) {
			if(top.get(k).size() != bottom.get(k).size()) return false;
		}
		return true;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int topParts() {
		return top.size();
	}

	public int bottomParts() {
		return bottom.size();
	}

	public int topPartSize(int part) {
		return top.get(part).size();
	}

	public int bottomPartSize(int part) {
		return bottom.get(part).size();
	}

	public int getTopElt(int part, int partIndex) {
		return top.get(part).get(partIndex);
	}

	public int getBottomElt(int part, int partIndex) {
		return bottom.get(part).get(partIndex);
	}

	public int getTopPartWithElt(int elt) {
		int index = 0;
		for(List<Integer> part : top) {
			if(part.contains(elt)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public int getBottomPartWithElt(int elt) {
		int index = 0;
		for(List<Integer> part : bottom) {
			if(part.contains(elt)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public int getTopPartIndexOfElt(int part, int elt) {
		return top.get(part).indexOf(elt);
	}

	public int getBottomPartIndexOfElt(int part, int elt) {
		return bottom.get(part).indexOf(elt);
	}

	public int getFirstNonUnitPart() {
		int index = 0;
		for(List<Integer> part : top) {
			if(part.size() > 1) return index;
			index++;
		}
		return -1;
	}

	public int getLeastABSNonUnitPart() {
		int index = 0;
		int minIndex = -1;
		int min = Integer.MAX_VALUE;
		for(List<Integer> part : top) {
			if(part.size() > 1) {
				int partMin = part.get(0);
				int absPartMin = Math.abs(partMin); 
				if(absPartMin < min) {
					min = Math.abs(part.get(0));
					minIndex = index;
				} else if(absPartMin == min && partMin > 0) {
					//If part.get(0) is positive
					minIndex = index;
				}
			}
			index++;
		}
		return minIndex;
	}

	public OrderedPartitionPair getCopy() {
		OrderedPartitionPair copy = new OrderedPartitionPair(top,bottom);
//		copy.bottom.clear();
		copy.setNum(this.num);

//		for(List<Integer> part : bottom) {
//			ArrayList<Integer> bottomPart = new ArrayList<Integer>();
//
//			for(Integer i : part) {
//				bottomPart.add(i);
//			}
//
//			copy.bottom.add(bottomPart);
//		}

		return copy;
	}

	public OrderedPartitionPair assignEltsToUnitPart(int topElt, int botElt) {
		int topPart = getTopPartWithElt(topElt);
		int botPart = getBottomPartWithElt(botElt);

		if(topPart != botPart) return null;

		//First part is part of an optimization: our refinement algorithm ignores
		//variables that aren't in the formula.
		//no change necessary since isomporphic POP
		if(topPart == -1 || top.get(topPart).size() == 1) return this; 


		int topIndex = getTopPartIndexOfElt(topPart,topElt);
		int bottomIndex = getBottomPartIndexOfElt(botPart,botElt);

		return assignIndeciesToUnitPart(topPart,topIndex,bottomIndex);
	}

	//Assigns two elements from a large partition to a parition of size 1
	public OrderedPartitionPair assignIndeciesToUnitPart(int part, int topIndex, int bottomIndex) {
		OrderedPartitionPair ret = this.getCopy();

		List<Integer> topPart = ret.top.get(part);
		List<Integer> bottomPart = ret.bottom.get(part);

		int topThing = topPart.remove(topIndex);
		int bottomThing = bottomPart.remove(bottomIndex);

		ArrayList<Integer> newTop = new ArrayList<Integer>();
		newTop.add(topThing);

		ArrayList<Integer> newBottom = new ArrayList<Integer>();
		newBottom.add(bottomThing);

		int k = 0;
		for(; k < ret.top.size(); k++) {
			if(ret.top.get(k).get(0) > topThing) {
				ret.top.add(k,newTop);
				ret.bottom.add(k,newBottom);
				break;
			}
		}

		if(k == ret.top.size()) {
			ret.top.add(newTop);
			ret.bottom.add(newBottom);
		}

		return ret;
	}

	public OrderedPartitionPair refine(SymmetryStatistics stats) {
		return refine(stats,null);
	}

	//newPosUnitParts is new unit partitions where the top is POSITIVE
	public OrderedPartitionPair refine(SymmetryStatistics stats, OrderedPartitionPair newPosUnitParts) {
		if(newPosUnitParts != null) {
			newPosUnitParts.num = this.num;
		}

		//		List<List<Integer>> prevTop = top;
		//		List<List<Integer>> curTop = top;
		//		List<List<Integer>> prevBot = bottom;
		//		List<List<Integer>> curBot= bottom;

		OrderedPartitionPair cur = this;
		OrderedPartitionPair prev = this;


		boolean topIso = false;
		boolean botIso = false;
		boolean ppoIso = true; 
		do {
			//			if(!topIso) {
			//				prevTop = curTop;
			//				curTop = refine(curTop,varToClause,topUnitParts);
			//			}
			//
			//			if(!botIso) {
			//				prevBot = curBot;
			//				curBot = refine(curBot,varToClause,bottomUnitParts);
			//			}

			prev = cur;
			cur = refine(cur,stats,newPosUnitParts);

			if(cur == null) break;

			//Check to see if anything was done
			topIso = checkIsomorphic(cur.top,prev.top);
			botIso = checkIsomorphic(cur.bottom,prev.bottom);
			ppoIso = checkIsomorphic(cur.top,cur.bottom);

		} while((!topIso || !botIso) && ppoIso);

		if(ppoIso) {
			OrderedPartitionPair ret = cur;

			if(newPosUnitParts != null) {
				removeNegativeTops(newPosUnitParts);
			}

			return ret;
		} else {
			return null;
		}
	}


	private void removeNegativeTops(OrderedPartitionPair newPosTopUnitParts) {
		for(int k = 0; k < newPosTopUnitParts.topParts(); k++) {
			if(newPosTopUnitParts.top.get(k).get(0) < 0) {
				newPosTopUnitParts.top.remove(k);
				newPosTopUnitParts.bottom.remove(k);
				k--;
			}
		}

	}

	//varToClause is 1 if clause of index k contains integer i

	private OrderedPartitionPair refine(OrderedPartitionPair toRefine, SymmetryStatistics stats, OrderedPartitionPair newUnits) {
		OrderedPartitionPair ret = new OrderedPartitionPair();
		ret.setNum(toRefine.num);

		int[][] topVarToFreqs = stats.getPartFreqs(toRefine.top);
		int[][] botVarToFreqs = stats.getPartFreqs(toRefine.bottom);


		List<List<Integer>> topBrokenPart = new ArrayList<List<Integer>>();	
		List<List<Integer>> bottomBrokenPart = new ArrayList<List<Integer>>();

		for(int p = 0; p < toRefine.topParts(); p++) {
			List<Integer> topPart = toRefine.top.get(p);
			List<Integer> bottomPart = toRefine.bottom.get(p);

			topBrokenPart.clear();
			bottomBrokenPart.clear();


			if(topPart.size() == 1) {
				ret.top.add(topPart);
				ret.bottom.add(bottomPart);
				continue;
			}

			doRefine(topVarToFreqs, botVarToFreqs, topPart, bottomPart,
					topBrokenPart, bottomBrokenPart);

			if(!checkIsomorphic(topBrokenPart,bottomBrokenPart)) {
				return null;
			}

			getNewUnits(newUnits, topPart, topBrokenPart, bottomBrokenPart);

			ret.top.addAll(topBrokenPart);
			ret.bottom.addAll(bottomBrokenPart);
		}


		return ret;

	}

	private void doRefine(int[][] topVarToFreqs, int[][] botVarToFreqs,
			List<Integer> topPart, List<Integer> bottomPart,
			List<List<Integer>> topBrokenPart,
			List<List<Integer>> bottomBrokenPart) {
		normalBreakup(topVarToFreqs, topPart, topBrokenPart);

		//Bottom refinement

		//normalBreakup(varToFreqs,bottomPart, bottomBrokenPart);
		abnormalBreakup(botVarToFreqs, topVarToFreqs, bottomPart, topBrokenPart,
				bottomBrokenPart);

		//This is unnecessary since our abnormal breakup aligns top and bottom
		//			if(topBrokenPart.size() == bottomBrokenPart.size()) {
		//				for(int k = 0; k < topBrokenPart.size(); k++) {
		//					List<Integer> curTopPart = topBrokenPart.get(k);
		//					List<Integer> curBotPart = bottomBrokenPart.get(k);
		//					
		//					if(curTopPart.size() != curBotPart.size()) {
		//						for(int i = k+1; i < bottomBrokenPart.size(); i++) {
		//							if(bottomBrokenPart.get(i).size() == curTopPart.size()) {
		//								List<Integer> toAdd = bottomBrokenPart.remove(i);
		//								bottomBrokenPart.add(k,toAdd);
		//								break;
		//							}
		//						}
		//					}
		//				}
		//			}
	}

	private void getNewUnits(OrderedPartitionPair newUnits,
			List<Integer> topPart, List<List<Integer>> topBrokenPart,
			List<List<Integer>> bottomBrokenPart) {
		if(topPart.size() > 1 && newUnits != null) {
			for(int i = 0; i < topBrokenPart.size(); i++) {
				List<Integer> topNewPart = topBrokenPart.get(i);
				List<Integer> botNewPart = bottomBrokenPart.get(i);
				if(topNewPart.size() == 1) { ///already checked for isomorphism
					newUnits.top.add(topNewPart);
					newUnits.bottom.add(botNewPart);
				}
			}
		}
	}

	private void abnormalBreakup(int[][] botVarToFreqs,int[][] topVarToFreqs,
			List<Integer> bottomPart, List<List<Integer>> topBrokenPart,
			List<List<Integer>> bottomBrokenPart) {
		for(int k = 0; k < topBrokenPart.size(); k++) {
			bottomBrokenPart.add(new ArrayList<Integer>());
		}

		for(int k = 0; k < bottomPart.size(); k++) {
			boolean added = false;

			int curLit = bottomPart.get(k);
			int[] freqs = botVarToFreqs[LitUtil.getIndex(curLit,num)]; //botVarToFreqs.get(curLit);
			int[] negFreqs = botVarToFreqs[LitUtil.getIndex(-curLit,num)]; //botVarToFreqs.get(-curLit);

			for(List<Integer> otherPart : bottomBrokenPart) {
				if(otherPart.size() == 0) continue;
				int testLit = otherPart.get(0);
				int[] testFreqs = botVarToFreqs[LitUtil.getIndex(testLit,num)]; //botVarToFreqs.get(testLit);
				int[] negTestFreqs = botVarToFreqs[LitUtil.getIndex(-testLit,num)]; //botVarToFreqs.get(-testLit);

				added = testCanAdd(added, curLit, freqs, negFreqs, otherPart, testFreqs, negTestFreqs);
				if(added) break;

			}
			//Need to line bottom up with top
			if(!added) {
				for(int i = 0; i < topBrokenPart.size(); i++) {
					List<Integer> otherPart = topBrokenPart.get(i);
					int testLit = otherPart.get(0);
					int[] testFreqs = topVarToFreqs[LitUtil.getIndex(testLit,num)]; //topVarToFreqs.get(testLit);
					int[] negTestFreqs = topVarToFreqs[LitUtil.getIndex(-testLit,num)]; //topVarToFreqs.get(-testLit);

					added = testCanAdd(added, curLit, freqs,negFreqs, bottomBrokenPart.get(i), testFreqs, negTestFreqs);
					if(added) break;
				}
			}
		}
	}

	private void normalBreakup(int[][] varToFreqs,
			List<Integer> topPart, List<List<Integer>> topBrokenPart) {
		for(int k = 0; k < topPart.size(); k++) {
			boolean added = false;

			int curLit = topPart.get(k);
			int[] freqs = varToFreqs[LitUtil.getIndex(curLit,num)]; //varToFreqs.get(curLit);
			int[] negFreqs = varToFreqs[LitUtil.getIndex(-curLit,num)];//varToFreqs.get(-curLit);

			for(List<Integer> otherPart : topBrokenPart) {
				int testLit = otherPart.get(0);
				int[] testFreqs = varToFreqs[LitUtil.getIndex(testLit,num)];//varToFreqs.get(testLit);
				int[] negTestFreqs = varToFreqs[LitUtil.getIndex(-testLit,num)];//varToFreqs.get(-testLit);

				added = testCanAdd(added, curLit, freqs,negFreqs, otherPart,
						testFreqs, negTestFreqs);
				if(added) break;

			}

			if(!added) {
				List<Integer> newPart = new ArrayList<Integer>();
				newPart.add(curLit);
				topBrokenPart.add(newPart);
			}
		}
	}

	private static boolean testCanAdd(boolean added, int curLit, int[] freqs, int[] negFreqs,
			List<Integer> otherPart, int[] testFreqs, int[] negTestFreqs) {
		if(Arrays.equals(freqs,testFreqs)
				&&	Arrays.equals(negFreqs,negTestFreqs)
				) {
			otherPart.add(curLit);
			return true;
		}
		return false;
	}

	//	public static List<List<Integer>> refine(List<List<Integer>> toRefine, Map<Integer,BitSet> varToClause) {
	//		return refine(toRefine,varToClause, null);
	//	}
	//
	//	//varToClause is 1 if clause of index k contains integer i
	//	public static List<List<Integer>> refine(List<List<Integer>> toRefine, Map<Integer,BitSet> varToClause, List<List<Integer>> newUnits) {
	//		List<List<Integer>> newParts = new ArrayList<List<Integer>>(toRefine.size());
	//
	//		BitSet[] partToClause = new BitSet[toRefine.size()];
	//
	//		setupPartFreqs(toRefine, varToClause, partToClause);
	//
	//		Map<Integer, int[]> varToFreqs = new HashMap<Integer,int[]>();
	//
	//		setupVarPartFreqs(toRefine, varToClause, partToClause, varToFreqs);
	//
	//		for(int p = 0; p < toRefine.size(); p++) {
	//			List<Integer> part = toRefine.get(p);
	//			List<List<Integer>> brokenPart = new ArrayList<List<Integer>>();	
	//
	//			for(int k = 0; k < part.size(); k++) {
	//				boolean added = false;
	//
	//				int curLit = part.get(k);
	//				int[] freqs = varToFreqs.get(curLit);
	//				int[] negFreqs = varToFreqs.get(-curLit);
	//
	//				for(List<Integer> otherPart : brokenPart) {
	//					int testLit = otherPart.get(0);
	//					int[] testFreqs = varToFreqs.get(testLit);
	//					int[] negTestFreqs = varToFreqs.get(-testLit);
	//
	//					added = testCanAdd(added, curLit, freqs, otherPart,
	//							testFreqs);
	//
	//				}
	//
	//				if(!added) {
	//					List<Integer> newPart = new ArrayList<Integer>();
	//					newPart.add(curLit);
	//					brokenPart.add(newPart);
	//				}
	//			}
	//
	//			if(part.size() > 1 && newUnits != null) {
	//				for(List<Integer> newPart : brokenPart) {
	//					if(newPart.size() == 1) {
	//						newUnits.add(newPart);
	//					}
	//				}
	//			}
	//
	//			newParts.addAll(brokenPart);
	//		}
	//
	//
	//		return newParts;
	//
	//	}

	//	private static int[][] setupPartFreqs(List<List<Integer>> toRefine,
	//			Map<Integer, BitSet> varToClause, BitSet[] partToClause, int numClauses) {
	//		int[][] ret = new int[numClauses][toRefine.size()];
	//		
	//		for(int k = 0; k < toRefine.size(); k++) {
	//			BitSet toAdd = null;
	//
	//			List<Integer> part = toRefine.get(k);
	//
	//			for(int i : part) {
	//				BitSet clausesWithi = varToClause.get(i);
	//				
	//				for(int j = 0; j < clausesWithi.size(); j++) {
	//					if(clausesWithi.get(j)) {
	//						ret[j][k]++;
	//					}
	//				}
	//			}
	//		}
	//		
	//		return ret;
	//	}
	//
	//	private static void setupVarPartFreqs(List<List<Integer>> toRefine,
	//			Map<Integer, BitSet> varToClause, int[][] clauseToFreqs,
	//			Map<Integer, int[]> varToFreqs) {
	//
	//		for(int k = 0; k < toRefine.size(); k++) {
	//			List<Integer> part = toRefine.get(k);
	//			for(int i : part) {
	//				BitSet bs = varToClause.get(i);
	//				int[] toAdd = new int[toRefine.size()];
	//				
	//				for(int j = 0; j < bs.size(); j++) {
	//					if(bs.get(j)) {
	//						for(int m = 0; m < toRefine.size(); m++) {
	//							toAdd[m] += clauseToFreqs[j][m];
	//						}
	//					}
	//				}
	//				
	//				varToFreqs.put(i,toAdd);
	//			}
	//		}
	//	}

	//Index 0 ignored
	public int[] getPermutation() {
		int[] perm = new int[num+1];
		perm[0] = 0;

		for(int k = 0; k < num+1; k++) {
			perm[k] = k;
		}

		for(int k = 0; k < top.size(); k++) {
			if(top.get(k).size() == 1 && bottom.get(k).size() == 1) {
				int topPart = top.get(k).get(0);
				int bottomPart = bottom.get(k).get(0);

				//We can implicitly denote the permutation using only where the
				//positive literals end up, since the negative literals go to the
				//dual literal
				int topVar = Math.abs(topPart);
				perm[topVar] =  (topVar/topPart)*bottomPart;
			} else {
				return null; //no well-defined perm yet;
			}
		}
		return perm;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(List<Integer> part : top) {
//			if(part.size() == 1) continue;
			sb.append('[');
			for(int i : part) {
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		sb.append("\n");
		for(List<Integer> part : bottom) {
//			if(part.size() == 1) continue;
			sb.append('[');
			for(int i : part) {
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		return sb.toString();
	}


}

