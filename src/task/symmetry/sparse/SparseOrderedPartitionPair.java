package task.symmetry.sparse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

//This implementation assumes the arrays are isomorphic
public class SparseOrderedPartitionPair {
	private OrderedPartition top;
	private OrderedPartition bottom;
	private int num;

	public SparseOrderedPartitionPair(int num) {
		top = new OrderedPartition(num);
		bottom = new OrderedPartition(num);
	}

	
	//Won't work if numbers are not contiguous 
	public SparseOrderedPartitionPair(List<IntList> initial) {
		top = new OrderedPartition(initial);
		bottom = new OrderedPartition(initial);
		
		num = top.getNum();
	}

	public SparseOrderedPartitionPair(ArrayList<IntList> newTop,
			ArrayList<IntList> newBot) {
		top = new OrderedPartition(newTop);
		bottom = new OrderedPartition(newBot);
		
		num = Math.max(top.getNum(), bottom.getNum());
		
	}


	public void post() {
		top.post();
		bottom.post();
	}
	
	public void pop() {
		top.pop();
		bottom.pop();
	}
	
	
	//Make sure is isomorphic OPP
	public boolean checkIsomorphic() {
		return top.isIsomorphicWith(bottom);
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	public int getNum() {
		return num;
	}

	public int topParts() {
		return top.parts();
	}

	public int bottomParts() {
		return bottom.parts();
	}

	public int topPartSize(int part) {
		return top.partSize(part);
	}

	public int bottomPartSize(int part) {
		return bottom.partSize(part);
	}

	public int getTopElt(int part, int partIndex) {
		return top.getElt(part,partIndex);
	}

	public int getBottomElt(int part, int partIndex) {
		return bottom.getElt(part,partIndex);
	}

	public int getTopPartWithElt(int elt) {
		return top.getPartWithElt(elt);
	}

	public int getBottomPartWithElt(int elt) {
		return bottom.getPartWithElt(elt);
	}

	public int getTopPartIndexOfElt(int part, int elt) {
		return top.getPartIndexOfElt(elt);
	}

	public int getBottomPartIndexOfElt(int part, int elt) {
		return bottom.getPartIndexOfElt(elt);
	}

	public int getFirstNonUnitPart() {
		return top.getFirstNonUnitPart();
	}

	public int getLeastABSNonUnitPart() {
		return top.getLeastABSNonUnitPart();
	}

	public boolean assignEltsToUnitPart(int topElt, int botElt) {
		
		int topPart = getTopPartWithElt(topElt);
		int botPart = getBottomPartWithElt(botElt);

		if(topPart != botPart) return false;

		top.assignEltsToUnitPart(topElt);
		bottom.assignEltsToUnitPart(botElt);
		
		return true;
	}
	
	public void assignIndeciesToUnitPart(int part, int topInd, int botInd) {
		top.assignEltsToUnitPart(part,topInd);
		bottom.assignEltsToUnitPart(part,botInd);
	}

	public void setAsBasePoint() {
		top.setBasePoint();
		bottom.setBasePoint();
	}

	public boolean refine(SparseSymmetryStatistics stats) {
		return refine(stats,false);
	}
	
	public boolean refine(SparseSymmetryStatistics stats, boolean initial) {
		return top.refine(bottom,stats,initial);
	}
	

	//Index 0 ignored
	public int[] getPermutation() {
		return top.getPermutation(bottom, num);
	}
	
	public int[] getShortcutPermutation() {
		return top.getShortcutPermutation(bottom, num);
	}
	
	public int[] getPartialPermutation() {
		return top.getPartialPermutation(bottom, num);
	}
	
	public int[] getMatch() {
		return top.matchOf(bottom, num);
	}
	
	public boolean matching() {
		return top.matches(bottom);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(top.toString());
		sb.append("\n");
		sb.append(bottom.toString());

		return sb.toString();
	}



	



}

