package util;

public class BinaryTrie {
	private class BinNode {
		BinNode t;
		BinNode f;
		int num = 0;
		
		public BinNode get(boolean b) {
			return b ? t : f;
		}
		
		public void set(boolean b, BinNode bn) {
			if(b) {
				t = bn;
			} else {
				f = bn;
			}
		}
	}
	
	BinNode root = null;
	int num;
	
	public void add(boolean[] bin, int num) {
		if(root == null) {
			root = new BinNode();
		}
		
		BinNode cur = root;
		BinNode prev = null;
		boolean prevB = false;
		
		for(boolean b : bin) {
			if(cur == null) {
				prev.set(prevB,new BinNode());
				cur = prev.get(prevB);
			}
			
			prev = cur;
			cur = cur.get(b);
			prevB = b;
		}
		if(cur == null) {
			prev.set(prevB,new BinNode());
			cur = prev.get(prevB);
		}
		cur.num = num;
	}
	
	public boolean exists(boolean[] bin) {
		BinNode cur = root;
		for(boolean b : bin) {
			if(cur == null) return false;
			cur = cur.get(b);
		}
		
		if(cur == null) {
			return false;
		} else {
			num = cur.num;
			return true;
		}
	}
	
	public int getNum() {
		return num;
	}
	
	public void clear() {
		root = null;
	}

	
}
