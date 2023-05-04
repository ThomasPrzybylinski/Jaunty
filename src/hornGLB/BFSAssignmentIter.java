package hornGLB;

import java.util.LinkedList;

public class BFSAssignmentIter implements AssignmentIter {
	private LinkedList<int[]> assignments;
	
	
	public BFSAssignmentIter(int size) {
		int[] cur = new int[size];
		assignments = new LinkedList<int[]>();
		assignments.push(cur);
	}
	

	@Override
	public boolean hasNext() {
		return assignments.size() > 0;
	}

	@Override
	public int[] next() {
		int[] ret = assignments.removeFirst();
		int maxOne = -1;
		
		for(int k = 0; k < ret.length; k++) {
			if(ret[k] == 1) {
				maxOne = k;
			}
		}
		
		for(int k = maxOne+1; k < ret.length; k++) {
			int[] next = ret.clone();
			next[k] = 1;
			assignments.addLast(next);
		}
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}


	@Override
	public AssignmentIter getInstance(int k) {
		return new BFSAssignmentIter(k);
	}

}
