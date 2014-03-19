package hornGLB;

public class BasicAssignIter implements AssignmentIter {

	private int[] cur;
	private boolean hasNext = true;
	public BasicAssignIter(int size) {
		cur = new int[size];
	}
	

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public int[] next() {
		int[] ret = cur;
		cur = new int[ret.length];
		
		boolean overFlow = true;
		hasNext = false;
		for(int k = 0; k < ret.length; k++) {
			if(ret[k] == 0) hasNext = true;
			if(overFlow) {
				overFlow = false;
				cur[k] = ret[k]+1;
				
				if(cur[k] > 1) {
					overFlow = true;
					cur[k] = 0;
				}
			} else {
				cur[k] = ret[k];
			}
		}
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}


	@Override
	public AssignmentIter getInstance(int k) {
		return new BasicAssignIter(k);
	}

}
