package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

//This implementation assumes the arrays are isomorphic
public class SparseOrderedPartitionPair {
	private List<IntList> top;
	private List<IntList> bottom;
	private int num;
	private List<int[]> ignore; //Unit clauses that have outlived their usefulness

	//Create empty partition
	public SparseOrderedPartitionPair() {
		num = 0;
		top = new ArrayList<IntList>();
		bottom = new ArrayList<IntList>();
		ignore = new ArrayList<int[]>();
	}

	public SparseOrderedPartitionPair(int num) {
		this(num,true);
	}

	@SuppressWarnings("unchecked")
	protected SparseOrderedPartitionPair(int num, boolean addNums) {
		this.setNum(num);

		top = new ArrayList<IntList>();
		bottom = new ArrayList<IntList>();
		ignore = new ArrayList<int[]>();

		if(addNums) {
			ArrayIntList ints = new ArrayIntList();

			for(int k = 1; k <=num; k++) {
				ints.add(k);
				ints.add(-k);
			}

			top.add(ints);
			bottom.add(new ArrayIntList(ints));
		}
	}

	//Won't work if numbers are not contiguous 
	public SparseOrderedPartitionPair(List<IntList> initial) {
		this.num = -1;

		top = new ArrayList<IntList>(initial.size());
		bottom = new ArrayList<IntList>(initial.size());
		ignore = new ArrayList<int[]>();

		for(IntList part : initial) {
			ArrayIntList topPart = new ArrayIntList(part.size());
			ArrayIntList bottomPart = new ArrayIntList(part.size());

			
			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
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
	protected SparseOrderedPartitionPair(List<IntList> toTop, List<IntList> toBottom, List<int[]> toIgnore) {
		this.num = -1;

		top = new ArrayList<IntList>(toTop.size());
		bottom = new ArrayList<IntList>(toBottom.size());
		ignore = new ArrayList<int[]>(toIgnore.size());

		for(IntList part : toTop) {
			ArrayIntList topPart = new ArrayIntList(part.size());

			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
				topPart.add(i);
				this.num = Math.max(this.num,Math.abs(i));
			}

			top.add(topPart);
		}

		for(IntList part : toBottom) {
			ArrayIntList bottomPart = new ArrayIntList(part.size());

			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
				bottomPart.add(i);
				this.num = Math.max(this.num,Math.abs(i));
			}

			bottom.add(bottomPart);
		}

		for(int[] unit : toIgnore) {
			this.num = Math.max(this.num,Math.max(unit[0],unit[1]));
			ignore.add(unit);
		}

		this.setNum(num); //for debugging purposes
	}

	//Make sure is isomorphic OPP
	public boolean checkIsomorphic() {
		return checkIsomorphic(top,bottom);
	}

	private static boolean checkIsomorphic(List<IntList> top, List<IntList> bottom) {
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
		return top.size() + ignore.size();
	}

	public int bottomParts() {
		return bottom.size() + ignore.size();
	}

	public int topPartSize(int part) {
		if(part < top.size()) {
			return top.get(part).size();
		} else if(part < top.size() + ignore.size()) {
			return 1;
		} else {
			top.get(part); //throw exception
			return -1; //Should never get here
		}
	}

	public int bottomPartSize(int part) {
		if(part < bottom.size()) {
			return bottom.get(part).size();
		} else if(part < bottom.size() + ignore.size()) {
			return 1;
		} else {
			bottom.get(part); //throw exception
			return -1; //Should never get here
		}
	}

	public int getTopElt(int part, int partIndex) {
		if(part < top.size()) {
			return top.get(part).get(partIndex);
		} else if(partIndex == 0) {
			return ignore.get(part-top.size())[0];
		} else {
			top.get(part); //throw exception
			return 0; //Should never get here
		}

	}

	public int getBottomElt(int part, int partIndex) {
		if(part < bottom.size()) {
			return bottom.get(part).get(partIndex);
		} else if(partIndex == 0) {
			return ignore.get(part-bottom.size())[1];
		} else {
			bottom.get(part); //throw exception
			return 0; //Should never get here
		}
	}

	public int getTopPartWithElt(int elt) {
		int index = 0;
		for(IntList part : top) {
			if(part.contains(elt)) {
				return index;
			}
			index++;
		}

		for(int[] i : ignore) {
			if(i[0] == elt) {
				return index;
			}
			index++;
		}

		return -1;
	}

	public int getBottomPartWithElt(int elt) {
		int index = 0;
		for(IntList part : bottom) {
			if(part.contains(elt)) {
				return index;
			}
			index++;
		}

		for(int[] i : ignore) {
			if(i[1] == elt) {
				return index;
			}
			index++;
		}

		return -1;
	}

	public int getTopPartIndexOfElt(int part, int elt) {
		if(part < top.size()) {
			return top.get(part).indexOf(elt);
		} else if(getTopElt(part,0) == elt) {
			return 0;
		} else {
			return -1;
		}
	}

	public int getBottomPartIndexOfElt(int part, int elt) {
		if(part < bottom.size()) {
			return bottom.get(part).indexOf(elt);
		}  else if(getBottomElt(part,0) == elt) {
			return 0;
		} else {
			return -1;
		}
	}

	public int getFirstNonUnitPart() {
		int index = 0;
		for(IntList part : top) {
			if(part.size() > 1) return index;
			index++;
		}
		return -1;
	}

	public int getLeastABSNonUnitPart() {
		int index = 0;
		int minIndex = -1;
		int min = Integer.MAX_VALUE;
		for(IntList part : top) {
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

	public SparseOrderedPartitionPair getCopy() {
		SparseOrderedPartitionPair copy = new SparseOrderedPartitionPair(top,bottom,ignore);
		//		copy.bottom.clear();
		copy.setNum(this.num);

		//		for(IntList part : bottom) {
		//			ArrayIntList bottomPart = new ArrayIntList();
		//
		//			for(Integer i : part) {
		//				bottomPart.add(i);
		//			}
		//
		//			copy.bottom.add(bottomPart);
		//		}

		return copy;
	}

	public SparseOrderedPartitionPair assignEltsToUnitPart(int topElt, int botElt) {
		int topPart = getTopPartWithElt(topElt);
		int botPart = getBottomPartWithElt(botElt);

		if(topPart != botPart) return null;

		//First part is part of an optimization: our refinement algorithm ignores
		//variables that aren't in the formula.
		//no change necessary since isomporphic POP
		if(topPart == -1 || topPartSize(topPart) == 1) return this; 


		int topIndex = getTopPartIndexOfElt(topPart,topElt);
		int bottomIndex = getBottomPartIndexOfElt(botPart,botElt);

		return assignIndeciesToUnitPart(topPart,topIndex,bottomIndex);
	}

	//Assigns two elements from a large partition to a parition of size 1
	public SparseOrderedPartitionPair assignIndeciesToUnitPart(int part, int topIndex, int bottomIndex) {
		SparseOrderedPartitionPair ret = this.getCopy();

		IntList topPart = ret.top.get(part);
		IntList bottomPart = ret.bottom.get(part);

		int topThing = topPart.removeElementAt(topIndex);
		int bottomThing = bottomPart.removeElementAt(bottomIndex);

		ArrayIntList newTop = new ArrayIntList();
		newTop.add(topThing);

		ArrayIntList newBottom = new ArrayIntList();
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

	public SparseOrderedPartitionPair refine(SparseSymmetryStatistics stats) {
		return refine(stats,null);
	}
	
	//newPosUnitParts is new unit partitions where the top is POSITIVE
	public SparseOrderedPartitionPair refine(SparseSymmetryStatistics stats, SparseOrderedPartitionPair newPosUnitParts) {
		if(newPosUnitParts != null) {
			newPosUnitParts.num = this.num;
		}

		//		List<IntList> prevTop = top;
		//		List<IntList> curTop = top;
		//		List<IntList> prevBot = bottom;
		//		List<IntList> curBot= bottom;

		SparseOrderedPartitionPair cur = this;
		SparseOrderedPartitionPair prev = this;


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
			SparseOrderedPartitionPair ret = cur;

			if(newPosUnitParts != null) {
				removeNegativeTops(newPosUnitParts);
			}

			ignoreUnitParts(ret); //No longer useful for refinement

			return ret;
		} else {
			return null;
		}
	}


	private void ignoreUnitParts(SparseOrderedPartitionPair ret) {
		if(ret == null) return;
		for(int k = 0; k < ret.top.size(); k++) {
			if(ret.top.get(k).size() == 1) {
				int[] toIg = new int[]{ret.top.get(k).get(0),ret.bottom.get(k).get(0)};
				
//				if(toIg[0] > 0) {
					ret.ignore.add(toIg); //Only add parts for perm later
//				}
				ret.top.remove(k);
				ret.bottom.remove(k);
				k--;
			}
		}

	}

	private void removeNegativeTops(SparseOrderedPartitionPair newPosTopUnitParts) {
		for(int k = 0; k < newPosTopUnitParts.topParts(); k++) {
			if(newPosTopUnitParts.top.get(k).get(0) < 0) {
				newPosTopUnitParts.top.remove(k);
				newPosTopUnitParts.bottom.remove(k);
				k--;
			}
		}

	}

	//varToClause is 1 if clause of index k contains integer i

	private SparseOrderedPartitionPair refine(SparseOrderedPartitionPair toRefine, SparseSymmetryStatistics stats, SparseOrderedPartitionPair newUnits) {
		SparseOrderedPartitionPair ret = new SparseOrderedPartitionPair();
		ret.ignore.addAll(this.ignore);
		ret.setNum(toRefine.num);
		
		int size = 5+(int)Math.sqrt(toRefine.num) + (int)Math.sqrt(toRefine.top.size());
		
		List<IntList> topBrokenPart = new ArrayList<IntList>(size);	
		List<IntList> bottomBrokenPart = new ArrayList<IntList>(size);

		for(int p = 0; p < toRefine.top.size(); p++) {
			IntList topPart = toRefine.top.get(p);
			IntList bottomPart = toRefine.bottom.get(p);

			if(topPart.size() == 1) {
				ret.top.add(topPart);
				ret.bottom.add(bottomPart);
				continue;
			}

			boolean correct = doRefine(topPart, bottomPart,
					topBrokenPart, bottomBrokenPart,stats,toRefine.top,toRefine.bottom);

			if(!correct || !checkIsomorphic(topBrokenPart,bottomBrokenPart)) {
				return null;
			}

			getNewUnits(newUnits, topPart, topBrokenPart, bottomBrokenPart);

			ret.top.addAll(topBrokenPart);
			ret.bottom.addAll(bottomBrokenPart);
		}


		return ret;

	}
	
	private boolean doRefine(
			IntList topPart, IntList bottomPart,
			List<IntList> realTopBrokenPart,
			List<IntList> realBottomBrokenPart, SparseSymmetryStatistics stats,
			List<IntList> topList,
			List<IntList> botList) {
		
		realTopBrokenPart.clear();
		realBottomBrokenPart.clear();
		
		return doRefine2(topPart,bottomPart,realTopBrokenPart,realBottomBrokenPart,stats,topList,botList);
		
//		List<IntList> tempTopBrokenPart1 = new ArrayList<IntList>();	
//		List<IntList> tempBottomBrokenPart1 = new ArrayList<IntList>();
//		
//		List<IntList> tempTopBrokenPart2 = new ArrayList<IntList>();	
//		List<IntList> tempBottomBrokenPart2 = new ArrayList<IntList>();
//		
//		
//		
//		boolean temp1 = true;
//		
//		if(!doRefine2(topPart,bottomPart,tempTopBrokenPart1,tempBottomBrokenPart1,stats,topList,botList)) {
//			return false;
//		}
//		
//		if(tempTopBrokenPart1.size() == 1)  {
//			return true;
//		}
//		
//		do {
//			List<IntList> tempTop = temp1 ? tempTopBrokenPart1 : tempTopBrokenPart2;
//			List<IntList> tempBot = temp1 ? tempBottomBrokenPart1 : tempBottomBrokenPart2;
//			
//			List<IntList> otherTop = !temp1 ? tempTopBrokenPart1 : tempTopBrokenPart2;
//			List<IntList> otherBot = !temp1 ? tempBottomBrokenPart1 : tempBottomBrokenPart2;
//			
//			otherTop.clear();
//			otherBot.clear();
//			
//			for(int k = 0; k < tempTop.size(); k++) {
//				boolean correct = doRefine2(tempTop.get(k),tempBot.get(k),otherTop,otherBot,stats,tempTop,tempBot);
//				if(!correct) return false;
//			}
//			
//			temp1 = !temp1;
//			
//		} while(!checkIsomorphic(tempTopBrokenPart1,tempTopBrokenPart2));
//		
//		realTopBrokenPart.addAll(tempTopBrokenPart1);
//		realBottomBrokenPart.addAll(tempBottomBrokenPart1);
//		
//		return true;
	}

	private boolean doRefine2(
			IntList topPart, IntList bottomPart,
			List<IntList> realTopBrokenPart,
			List<IntList> realBottomBrokenPart,SparseSymmetryStatistics stats,
			List<IntList> topList,
			List<IntList> botList) {
		
		List<IntList> topBrokenPart = new ArrayList<IntList>();
		List<IntList> bottomBrokenPart = new ArrayList<IntList>();
		
		int[][][] topFreqs = stats.getPartFreqs(topPart,topList);
		int[][][] botFreqs = stats.getPartFreqs(bottomPart,botList);
		
//		int[][] topPos = topFreqs[0];
//		int[][] topNeg = topFreqs[1];
//		
//		int[][] botPos = botFreqs[0];
//		int[][] botNeg = botFreqs[1];
		
		int[] topPosHash = new int[topFreqs[0].length];
		int[] botPosHash = new int[botFreqs[0].length];
		int[] topNegHash = new int[topFreqs[1].length];
		int[] botNegHash = new int[botFreqs[1].length];
		
		//Given an index for a new refined cell, give an example index
		//Is the last elt added, to make sure we can always unify
		//the top and bottom
		int[] topAssn = new int[topPart.size()];
		int[] botAssn = new int[bottomPart.size()];
		
		Arrays.fill(topAssn,-1);
		Arrays.fill(botAssn,-1);
		
		//Top and bottoms should be isomorphic, so same indecies
		for(int k = 0; k < topFreqs[0].length; k++) {
			topPosHash[k] = Arrays.hashCode(topFreqs[0][k]);
			botPosHash[k] = Arrays.hashCode(botFreqs[0][k]);
			
			topNegHash[k] = Arrays.hashCode(topFreqs[1][k]);
			botNegHash[k] = Arrays.hashCode(botFreqs[1][k]);
		}
		
		for(int k = 0; k < topPart.size(); k++) {
			int top = topPart.get(k);
			int bot = bottomPart.get(k);
			
			if(areEqv(k,topPosHash,botPosHash,topNegHash,botNegHash,topFreqs,botFreqs)) {
				for(int i = 0; i < topAssn.length; i++) {
					int topA = topAssn[i];
					if(topA == -1 && botAssn[i] == -1) {
						
						IntList newPart;
						if(topBrokenPart.size() == i) {
							newPart = new ArrayIntList();
							topBrokenPart.add(newPart);
						} else {
							newPart = topBrokenPart.get(i);
						}

						topAssn[i] = k;
						newPart.add(top);
					
						if(bottomBrokenPart.size() == i) {
							newPart = new ArrayIntList();
							bottomBrokenPart.add(newPart);
						} else {
							newPart = bottomBrokenPart.get(i);
						}

						botAssn[i] = k;
						newPart.add(bot);
						break;
						
					} else if(topA != -1) {
						if(areEqv(k,topA,topPosHash,topNegHash,topFreqs)) {
							topBrokenPart.get(i).add(top);
							bottomBrokenPart.get(i).add(bot);
							topAssn[i] = k;
							botAssn[i] = k;
							break;
						}
					} else if(botAssn[i] != -1) {
						if(areEqv(k,botAssn[i],botPosHash,botNegHash,botFreqs)) {
							topBrokenPart.get(i).add(top);
							bottomBrokenPart.get(i).add(bot);
							topAssn[i] = k;
							botAssn[i] = k;
							break;
						}
					}
				}
			} else {
				for(int i = 0; i < topAssn.length; i++) {
					int topA = topAssn[i];
					int botA = botAssn[i];
					if(topA == -1 && botA == -1) {
						IntList newPart = new ArrayIntList();
						newPart.add(top);
						topBrokenPart.add(newPart);
						topAssn[i] = k;
						
						newPart = new ArrayIntList();
						bottomBrokenPart.add(newPart);
						
						break;
					} else if(topA == -1 && botA != -1
							&& areEqv(k,botA,topPosHash,botPosHash, topNegHash, botNegHash,topFreqs,botFreqs)) {
						topBrokenPart.get(i).add(top);
						topAssn[i] = k;
						break;
					} else if(topA != -1 && areEqv(k,topA,topPosHash, topNegHash,topFreqs)) {
						topBrokenPart.get(i).add(top);
						break;
					}
				}
				
				for(int i = 0; i < botAssn.length; i++) {
					int topA = topAssn[i];
					int botA = botAssn[i];
					if(topA == -1 && botA == -1) {
						IntList newPart = new ArrayIntList();
						newPart.add(bot);
						bottomBrokenPart.add(newPart);
						botAssn[i] = k;
						
						newPart = new ArrayIntList();
						topBrokenPart.add(newPart);
						
						break;
					} else if(topA != -1 && botA == -1
							&& areEqv(topA,k,topPosHash,botPosHash, topNegHash, botNegHash,topFreqs,botFreqs)) {
						bottomBrokenPart.get(i).add(bot);
						botAssn[i] = k;
						break;
					}else if(botA != -1 && areEqv(k,botA,botPosHash, botNegHash, botFreqs)) {
						bottomBrokenPart.get(i).add(bot);
						break;
					}
				}
			}
		}
		
		if(!checkIsomorphic(bottomBrokenPart,topBrokenPart)) {
			return false;
		} else if(topBrokenPart.size() == 1) {
			//no change
			realTopBrokenPart.add(topPart);
			realBottomBrokenPart.add(bottomPart);
		} else {
			for(int k = 0; k < topBrokenPart.size(); k++) {
				if(!doRefine2(topBrokenPart.get(k),bottomBrokenPart.get(k),
						realTopBrokenPart,realBottomBrokenPart,
						stats,topBrokenPart,bottomBrokenPart)) {
					return false;
				}
			}
		}
		
		return true;
		

	}
	
	private boolean areEqv(int k, int i, int[] topPosHash, int[] botPosHash,
			int[] topNegHash, int[] botNegHash, int[][][] topFreqs,
			int[][][] botFreqs) {
		return topPosHash[k] == botPosHash[i] &&
				topNegHash[k] == botNegHash[i];
//						&&
//				Arrays.equals(topFreqs[0][k],botFreqs[0][i]) &&
//				Arrays.equals(topFreqs[1][k],botFreqs[1][i]);
	}

	private boolean areEqv(int k, int[] topPosHash, int[] botPosHash,
			int[] topNegHash, int[] botNegHash, int[][][] topFreqs,
			int[][][] botFreqs) {
		return topPosHash[k] == botPosHash[k] &&
				topNegHash[k] == botNegHash[k];
//						&&
//				Arrays.equals(topFreqs[0][k],botFreqs[0][k]) &&
//				Arrays.equals(topFreqs[1][k],botFreqs[1][k]);
	}

	private boolean areEqv(int k, int i, int[] posHash,int[] negHash, int[][][] freqs) {
		return posHash[k] == posHash[i] &&
				negHash[k] == negHash[i];
//						&&
//				Arrays.equals(freqs[0][k],freqs[0][i]) &&
//				Arrays.equals(freqs[1][k],freqs[1][i]);
	}

	
	private void getNewUnits(SparseOrderedPartitionPair newUnits,
			IntList topPart, List<IntList> topBrokenPart,
			List<IntList> bottomBrokenPart) {
		if(topPart.size() > 1 && newUnits != null) {
			for(int i = 0; i < topBrokenPart.size(); i++) {
				IntList topNewPart = topBrokenPart.get(i);
				IntList botNewPart = bottomBrokenPart.get(i);
				if(topNewPart.size() == 1) { ///already checked for isomorphism
					newUnits.top.add(topNewPart);
					newUnits.bottom.add(botNewPart);
				}
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
				int topVar = Math.abs(topPart);
				perm[topVar] =  (topVar/topPart)*bottomPart;
			} else {
				return null; //no well-defined perm yet;
			}
		}

		for(int[] i : ignore) {
			int topPart = i[0];
			int bottomPart = i[1];

			//We can implicitly denote the permutation using only where the
			//positive literals end up, since the negative literals go to the
			//dual literal
			int topVar = Math.abs(topPart);
			perm[topVar] =  (topVar/topPart)*bottomPart;
		}
		return perm;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(IntList part : top) {
//			if(part.size() == 1) continue;
			sb.append('[');
			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		for(int[] part : ignore) {
			sb.append('{');
			sb.append(part[0]).append(' ');
			sb.append('}').append(' ');
		}
		sb.append("\n");
		for(IntList part : bottom) {
//			if(part.size() == 1) continue;
			sb.append('[');
			for(int j = 0; j < part.size(); j++) {
				int i = part.get(j);
				sb.append(i).append(' ');
			}
			sb.append(']').append(' ');
		}
		for(int[] part : ignore) {
			sb.append('{');
			sb.append(part[1]).append(' ');
			sb.append('}').append(' ');
		}
		return sb.toString();
	}


}

