package task.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//I'm pretty sure this prototype is a failure.

//This implementation assumes the arrays are isomorphic
public class _PrototypePairwiseOrderedPartition {
	private List<List<Integer>> top;
	private List<List<Integer>> bottom;
	private int num;

	private Map<Integer,Integer> prevPart = new HashMap<Integer,Integer>(); //Assumed isomorphic, so top-part == bottom part;
	private Map<Integer,Integer> prevTopIndex = new HashMap<Integer,Integer>();
	private Map<Integer,Integer> prevBotIndex = new HashMap<Integer,Integer>();

	public _PrototypePairwiseOrderedPartition(int num) {
		this.num = num;

		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();

		ArrayList<Integer> ints = new ArrayList<Integer>();

		for(int k = 1; k <=num; k++) {
			ints.add(k);
			prevPart.put(k,0);
			prevTopIndex.put(k,ints.size()-1);
			prevBotIndex.put(k,ints.size()-1);
		}

		top.add(ints);
		bottom.add((List<Integer>)ints.clone());
	}

	//Won't work if numbers are not contiguous 
	public _PrototypePairwiseOrderedPartition(List<List<Integer>> initial) {
		this.num = -1;

		top = new ArrayList<List<Integer>>();
		bottom = new ArrayList<List<Integer>>();

		for(List<Integer> part : initial) {
			ArrayList<Integer> topPart = new ArrayList<Integer>();
			ArrayList<Integer> bottomPart = new ArrayList<Integer>();

			for(Integer i : part) {
				prevPart.put(i,top.size());
				prevTopIndex.put(i,topPart.size());
				prevBotIndex.put(i,bottomPart.size());

				topPart.add(i);
				bottomPart.add(i);
				this.num = Math.max(this.num,i);
			}

			top.add(topPart);
			bottom.add(bottomPart);
		}
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
	
	protected Integer getLocationPrediction(int elt) {
//		int predictedPart = prevPart.get(elt);
//		int predictedIndex = prevTopIndex.get(elt);
//
//		Integer pred = null;
//
//		List<Integer> predList = top.get(predictedPart); //Shouldn't out of bounds
//		if(predictedIndex < predList.size()) {
//			pred = predList.get(predictedIndex);
//		}
//		
//		if(pred.equals(elt)) return pred;
		
		return null;
	}

	public int getTopPartWithElt(int elt) {
		Integer pred = getLocationPrediction(elt);

		if(pred == null) {
			int index = 0;
			for(List<Integer> part : top) {
				if(part.contains(elt)) {
					return index;
				}
				index++;
			}
			return -1;
		} else {
			return prevPart.get(elt);
		}
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
		List<Integer> partList = top.get(part);
//		int predIndex = prevTopIndex.get(elt);
//		
//		if(predIndex < partList.size()) {
//			int pred = partList.get(predIndex);
//			
//			if(pred == elt) return predIndex;
//		}
		
		return partList.indexOf(elt);
	}

	public int getBottomPartIndexOfElt(int part, int elt) {
		List<Integer> partList = bottom.get(part);
//		int predIndex = prevBotIndex.get(elt);
//		
//		if(predIndex < partList.size()) {
//			int pred = partList.get(predIndex);
//			
//			if(pred == elt) return predIndex;
//		}
		return partList.indexOf(elt);
	}

	public int getFirstNonUnitPart() {
		int index = 0;
		for(List<Integer> part : top) {
			if(part.size() > 1) return index;
			index++;
		}
		return -1;
	}

	public _PrototypePairwiseOrderedPartition getCopy() {
		_PrototypePairwiseOrderedPartition copy = new _PrototypePairwiseOrderedPartition(top);
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

	public _PrototypePairwiseOrderedPartition assignEltsToSinglePart(int topElt, int botElt) {
		int topPart = getTopPartWithElt(topElt);
		int botPart = getBottomPartWithElt(botElt);

		if(topPart != botPart) return null;
		
		if(top.get(topPart).size() == 1) {
			return this; //no change necessary since isomporphic POP
		}

		
		int topIndex = getTopPartIndexOfElt(topPart,topElt);
		int bottomIndex = getBottomPartIndexOfElt(botPart,botElt);


		return assignIndeciesToUnitPart(topPart,topIndex,bottomIndex);
	}

	//Assigns two elements from a large partition to a parition of size 1
	public _PrototypePairwiseOrderedPartition assignIndeciesToUnitPart(int part, int topIndex, int bottomIndex) {
		_PrototypePairwiseOrderedPartition ret = this;//.getCopy();

		List<Integer> topPart = ret.top.get(part);
		List<Integer> bottomPart = ret.bottom.get(part);

		int topThing = topPart.remove(topIndex);
		int bottomThing = bottomPart.remove(bottomIndex);
		
		prevPart.put(topThing,part);
		prevPart.put(bottomThing,part);
		
		prevTopIndex.put(topThing,topIndex);
		prevBotIndex.put(bottomThing,bottomIndex);

		ArrayList<Integer> newTop = new ArrayList<Integer>();
		newTop.add(topThing);

		ArrayList<Integer> newBottom = new ArrayList<Integer>();
		newBottom.add(bottomThing);

		//Added to the end to preseve indecies
		ret.top.add(newTop);
		ret.bottom.add(newBottom);


		return ret;
	}
	
	//Does not do error checking
	public void undoUnity() {
		Integer topElt = top.remove(top.size()-1).get(0);
		Integer botElt = bottom.remove(bottom.size()-1).get(0);
		
		int topPartIndex = prevPart.get(topElt);
		int botPartIndex = prevPart.get(botElt);
		
		int topIndex = prevTopIndex.get(topElt);
		int botIndex = prevBotIndex.get(botElt);
		
		top.get(topPartIndex).add(topIndex,topElt);
		bottom.get(botPartIndex).add(botIndex,botElt);
	}

	//Index 0 ignored
	public int[] getPermutation() {
		int[] perm = new int[num+1];
		perm[0] = 0;

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


