package group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.StablePermComparator;

//If the group is a strong generating set,
//can figure things out here
public class StrongGroupOverlay {
	int[] base;
	SchreierVector[] vecs;
	
	public StrongGroupOverlay(LiteralGroup g) {
		ArrayList<LiteralGroup> stabGroups = new ArrayList<LiteralGroup>();
		
		List<LiteralPermutation> perms = new ArrayList<LiteralPermutation>(g.getGenerators().size());
		perms.addAll(g.getGenerators());
		
		Collections.sort(perms,new StablePermComparator());
		
		System.out.println("LA:"+perms);
		
		ArrayList<Integer> base = new ArrayList<Integer>();
		
		int last = 1;
		stabGroups.add(g);
		base.add(1);
		
		for(int k = 0; k < perms.size(); k++) {
			LiteralPermutation p1 = perms.get(k);
			int next = p1.getFirstUnstableVar();
			if(next == 0) continue;
			if(next != last) {
				ArrayList<LiteralPermutation> nextGroup = new ArrayList<LiteralPermutation>();
				for(int i = k; i < perms.size(); i++) {
					nextGroup.add(perms.get(i));
				}
				base.add(next);
				last = next;
				stabGroups.add(new NaiveLiteralGroup(nextGroup));
			}
		}
		
		this.base = new int[base.size()];
		this.vecs = new SchreierVector[stabGroups.size()];
		
		for(int k = 0; k < base.size(); k++) {
			this.base[k] = base.get(k);
			this.vecs[k] = new SchreierVector(stabGroups.get(k),this.base[k]);
		}
	}
	
	public int baseSize() {
		return this.base.length;
	}
	
	public int baseVal(int index) {
		return this.base[index];
	}
	
	public LiteralPermutation getPerm(int[] mapsTo) {
		if(mapsTo.length > base.length) return null;
		LiteralPermutation orig = new LiteralPermutation(vecs[0].getNumVars()); //ID initially
		
		for(int k = 0; k < mapsTo.length; k++) {
			if(orig.imageOf(base[k]) == mapsTo[k]) continue;
			LiteralPermutation toCompose = vecs[k].getPerm(base[k], orig.inverse().imageOf(mapsTo[k]));
			if(toCompose == null) {
				return null;
			}
			orig = toCompose.compose(orig);
		}
		
		return orig;
	}

}
