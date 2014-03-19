package task.symmetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//This implementation assumes the arrays are isomorphic
public class _OldOrderedPartitionPair {
	private List<List<Integer>> top;
	private List<List<Integer>> bottom;
	private int num;

	//Create empty partition
	public _OldOrderedPartitionPair() {
		num = 0;
		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();
	}

	public _OldOrderedPartitionPair(int num) {
		this(num,true);
	}

	protected _OldOrderedPartitionPair(int num, boolean addNums) {
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
	public _OldOrderedPartitionPair(List<List<Integer>> initial) {
		this.num = -1;

		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();

		for(List<Integer> part : initial) {
			ArrayList<Integer> topPart = new ArrayList<Integer>();
			ArrayList<Integer> bottomPart = new ArrayList<Integer>();

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

	public _OldOrderedPartitionPair getCopy() {
		_OldOrderedPartitionPair copy = new _OldOrderedPartitionPair(top);
		copy.bottom.clear();

		for(List<Integer> part : bottom) {
			ArrayList<Integer> bottomPart = new ArrayList<Integer>();

			for(Integer i : part) {
				bottomPart.add(i);
			}

			copy.bottom.add(bottomPart);
		}
		return copy;
	}

	public _OldOrderedPartitionPair assignEltsToUnitPart(int topElt, int botElt) {
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
	public _OldOrderedPartitionPair assignIndeciesToUnitPart(int part, int topIndex, int bottomIndex) {
		_OldOrderedPartitionPair ret = this.getCopy();

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

	public _OldOrderedPartitionPair refine(Map<Integer,BitSet> varToClause) {
		return refine(varToClause,null);
	}

	//newUnitParts is new unit partitions where the top is POSITIVE
	public _OldOrderedPartitionPair refine(Map<Integer,BitSet> varToClause, _OldOrderedPartitionPair newPosTopUnitParts) {
		List<List<Integer>> topUnitParts = new ArrayList<List<Integer>>();
		List<List<Integer>> bottomUnitParts = new ArrayList<List<Integer>>();

		List<List<Integer>> prevTop = top;
		List<List<Integer>> curTop = top;
		List<List<Integer>> prevBot = bottom;
		List<List<Integer>> curBot= bottom;

		_OldOrderedPartitionPair cur = this;
		_OldOrderedPartitionPair prev = this;


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
			cur = refine(cur,varToClause,newPosTopUnitParts);
			
			if(cur == null) break;

			topIso = checkIsomorphic(cur.top,prev.top);
			botIso = checkIsomorphic(cur.bottom,prev.bottom);
			ppoIso = checkIsomorphic(cur.top,cur.bottom);

		} while((!topIso || !botIso) && ppoIso);

		if(ppoIso) {
			_OldOrderedPartitionPair ret = cur;

			if(newPosTopUnitParts != null) {
				newPosTopUnitParts.num = this.num;
				newPosTopUnitParts.top = topUnitParts;
				newPosTopUnitParts.bottom = bottomUnitParts;

				removeNegativeTops(newPosTopUnitParts);
			}

			return ret;
		} else {
			return null;
		}
	}


	private void removeNegativeTops(_OldOrderedPartitionPair newPosTopUnitParts) {
		for(int k = 0; k < newPosTopUnitParts.topParts(); k++) {
			if(newPosTopUnitParts.top.get(k).get(0) < 0) {
				newPosTopUnitParts.top.remove(k);
				newPosTopUnitParts.bottom.remove(k);
				k--;
			}
		}

	}

	//varToClause is 1 if clause of index k contains integer i
	private static _OldOrderedPartitionPair refine(_OldOrderedPartitionPair toRefine, Map<Integer,BitSet> varToClause, _OldOrderedPartitionPair newUnits) {
		_OldOrderedPartitionPair ret = new _OldOrderedPartitionPair();
		ret.setNum(toRefine.num);

		List<List<Integer>> newParts = new ArrayList<List<Integer>>(toRefine.topParts());

		BitSet[] topPartToClause = new BitSet[toRefine.topParts()];
		BitSet[] bottomPartToClause = new BitSet[toRefine.bottomParts()];

		setupPartFreqs(toRefine.top, varToClause, topPartToClause);
		setupPartFreqs(toRefine.bottom, varToClause, bottomPartToClause);

		Map<Integer, int[]> varToFreqs = new HashMap<Integer,int[]>();

		setupVarPartFreqs(toRefine.top, varToClause, topPartToClause, varToFreqs);
		setupVarPartFreqs(toRefine.bottom, varToClause, bottomPartToClause, varToFreqs);

		//Top refinement
		
		for(int p = 0; p < toRefine.topParts(); p++) {
			List<Integer> topPart = toRefine.top.get(p);
			List<Integer> bottomPart = toRefine.bottom.get(p);

			List<List<Integer>> topBrokenPart = new ArrayList<List<Integer>>();	
			List<List<Integer>> bottomBrokenPart = new ArrayList<List<Integer>>();
			
			if(topPart.size() == 1) {
				ret.top.add(topPart);
				ret.bottom.add(bottomPart);
				continue;
			}

			normalBreakup(varToFreqs, topPart, topBrokenPart);
			
			//Bottom refinement

			normalBreakup(varToFreqs,bottomPart, bottomBrokenPart);
//			abnormalBreakup(varToFreqs, bottomPart, topBrokenPart,
//					bottomBrokenPart);
			

			if(topBrokenPart.size() == bottomBrokenPart.size()) {
				for(int k = 0; k < topBrokenPart.size(); k++) {
					List<Integer> curTopPart = topBrokenPart.get(k);
					List<Integer> curBotPart = bottomBrokenPart.get(k);
					
					if(curTopPart.size() != curBotPart.size()) {
						for(int i = k+1; i < bottomBrokenPart.size(); i++) {
							if(bottomBrokenPart.get(i).size() == curTopPart.size()) {
								List<Integer> toAdd = bottomBrokenPart.remove(i);
								bottomBrokenPart.add(k,toAdd);
								break;
							}
						}
					}
				}
			}

			if(!checkIsomorphic(topBrokenPart,bottomBrokenPart)) {
				return null;
			}

			if(topPart.size() > 1 && newUnits != null) {
				for(int i = 0; i < topBrokenPart.size(); i++) {
					List<Integer> topNewPart = topBrokenPart.get(i);
					List<Integer> botNewPart = topBrokenPart.get(i);
					if(topNewPart.size() == 1) { ///already checked for isomorphism
						newUnits.top.add(topNewPart);
						newUnits.bottom.add(botNewPart);
					}
				}
			}

			ret.top.addAll(topBrokenPart);
			ret.bottom.addAll(bottomBrokenPart);
		}


		return ret;

	}

	private static void abnormalBreakup(Map<Integer, int[]> varToFreqs,
			List<Integer> bottomPart, List<List<Integer>> topBrokenPart,
			List<List<Integer>> bottomBrokenPart) {
		for(int k = 0; k < topBrokenPart.size(); k++) {
			bottomBrokenPart.add(new ArrayList<Integer>());
		}

		for(int k = 0; k < bottomPart.size(); k++) {
			boolean added = false;

			int curLit = bottomPart.get(k);
			int[] freqs = varToFreqs.get(curLit);
			int[] negFreqs = varToFreqs.get(-curLit);

			for(List<Integer> otherPart : bottomBrokenPart) {
				if(otherPart.size() == 0) continue;
				int testLit = otherPart.get(0);
				int[] testFreqs = varToFreqs.get(testLit);
				int[] negTestFreqs = varToFreqs.get(-testLit);

				added = testCanAdd(added, curLit, freqs, negFreqs, otherPart, testFreqs, negTestFreqs);
				if(added) break;

			}
			//Need to line bottom up with top
			if(!added) {
				for(int i = 0; i < topBrokenPart.size(); i++) {
					List<Integer> otherPart = topBrokenPart.get(i);
					int testLit = otherPart.get(0);
					int[] testFreqs = varToFreqs.get(testLit);
					int[] negTestFreqs = varToFreqs.get(-testLit);

					added = testCanAdd(added, curLit, freqs,negFreqs, bottomBrokenPart.get(i), testFreqs, negTestFreqs);
					if(added) break;
				}
			}
		}
	}

	private static void normalBreakup(Map<Integer, int[]> varToFreqs,
			List<Integer> topPart, List<List<Integer>> topBrokenPart) {
		for(int k = 0; k < topPart.size(); k++) {
			boolean added = false;

			int curLit = topPart.get(k);
			int[] freqs = varToFreqs.get(curLit);
			int[] negFreqs = varToFreqs.get(-curLit);

			for(List<Integer> otherPart : topBrokenPart) {
				int testLit = otherPart.get(0);
				int[] testFreqs = varToFreqs.get(testLit);
				int[] negTestFreqs = varToFreqs.get(-testLit);

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

	private static void setupPartFreqs(List<List<Integer>> toRefine,
			Map<Integer, BitSet> varToClause, BitSet[] partToClause) {
		for(int k = 0; k < toRefine.size(); k++) {
			BitSet toAdd = null;

			List<Integer> part = toRefine.get(k);

			for(int i : part) {
				BitSet clausesWithi = varToClause.get(i);
				if(toAdd == null) {
					toAdd = new BitSet(clausesWithi.size());
				}

				toAdd.or(clausesWithi);
			}

			partToClause[k] = toAdd;
		}
	}

	private static void setupVarPartFreqs(List<List<Integer>> toRefine,
			Map<Integer, BitSet> varToClause, BitSet[] partToClause,
			Map<Integer, int[]> varToFreqs) {

		for(int k = 0; k < toRefine.size(); k++) {
			List<Integer> part = toRefine.get(k);
			for(int i : part) {
				BitSet bs = varToClause.get(i);
				BitSet temp = new BitSet(bs.size());

				temp.or(bs);

				int[] toAdd = new int[toRefine.size()];
				for(int j = 0; j < toRefine.size(); j++) {
					BitSet partClauses = partToClause[j];

					temp.and(partClauses);
					toAdd[j] = temp.cardinality();

					temp.clear();
					temp.or(bs);
				}

				varToFreqs.put(i,toAdd);
			}
		}
	}

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
				if(topPart > 0) {
					perm[topPart] = bottomPart;
				}

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
			sb.append('[');
			for(int i : part) {
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		sb.append("\n");
		for(List<Integer> part : bottom) {
			sb.append('[');
			for(int i : part) {
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		return sb.toString();
	}


}

