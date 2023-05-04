package group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

import task.translate.ConsoleDecodeable;
import util.IntegralDisjointSet;
import util.PermutationUtil;
import util.StablePermComparator;
import util.lit.LitUtil;
import formula.VariableContext;

public abstract class LiteralGroup {
	public abstract List<LiteralPermutation> getGenerators();
	public abstract LiteralPermutation getId();
	//Number of vars, not number of generators
	public abstract int size();

	public abstract LiteralGroup getNewInstance(Collection<LiteralPermutation> generators);

	//TODO: Make more efficient
	public LiteralGroup combine(LiteralGroup g) {
		if(this.size() != g.size()) return null;

		//LitsMap<Object> gens = new LitsMap<Object>(this.size());

		ArrayList<LiteralPermutation> newGens = new ArrayList<LiteralPermutation>();
		for(LiteralPermutation lp : getGenerators()) {
			newGens.add(lp);
		}

		for(LiteralPermutation lp :g.getGenerators()) {
			newGens.add(lp);
		}

		Collections.sort(newGens);

		//Will filter out the identity
		LinkedList<LiteralPermutation> retGens = filter(newGens, g.size());
		Collections.reverse(retGens);
		retGens = filter(retGens, g.size());


		//On second thought this won't work
		//		while(retGens.size() < oldSize) {
		//			oldSize = retGens.size();
		//			retGens = filter(newGens, g.size());
		//		}

		//Add the identity back in
		retGens.addFirst(new LiteralPermutation(g.size())); 
		return this.getNewInstance(retGens);
	}

	private LinkedList<LiteralPermutation> filter(
			List<LiteralPermutation> toFilter, int size) {
		ArrayList<LiteralPermutation> gens = new ArrayList<LiteralPermutation>();
		gens.addAll(toFilter);
		Collections.sort(gens, new StablePermComparator());
		Collections.reverse(gens); //Just to make sure if this is a strong generating set, it stays that way

		LinkedList<LiteralPermutation> newGens = new LinkedList<LiteralPermutation>();

		IntegralDisjointSet set = new IntegralDisjointSet(0,2*size+1);

		for(LiteralPermutation perm : gens) {
			boolean keep = false;

			for(int k = 0; k < perm.size(); k++) {
				int v1 = k+1;
				int v2 = perm.imageOf(v1);

				if(v1 != v2 && !set.sameSet(v1+size,v2+size)) {
					keep = true;
					set.join(v1+size,v2+size);
					set.join(-v1+size,-v2+size);
				}
			}

			if(keep) {
				newGens.add(perm);
			}

		}
		return newGens;
	}

	public LiteralGroup reduce() {
		LinkedList<LiteralPermutation> gens = filter(getGenerators(),size());

		if(gens.size() == 0) {
			gens.add(new LiteralPermutation(size())); //only id
		}

		return getNewInstance(gens);
	}

	public LiteralGroup getStabSubGroup(int lit) {
		boolean alreadyStab = true;
		for(LiteralPermutation lp : this.getGenerators()) {
			if(lp.imageOf(lit) != lit) {
				alreadyStab = false;
				break;
			}
		}
		
		if(alreadyStab) return this;
		
		SchreierVector vec = new SchreierVector(this,lit);

		TreeSet<LiteralPermutation> perms = new TreeSet<LiteralPermutation>();
		
		for(int k = 1; k <= size(); k++) {
			if(k != lit && vec.getRep(k) == lit) {
				addStabs(vec,perms,k);
			}

			if(-k != lit && vec.getRep(-k) == lit) {
				addStabs(vec,perms,-k);
			}
		}

		if(perms.size() == 0) {
			return new NaiveLiteralGroup(this.getId()); //No one is in the orbit of lit
		}

//		LiteralPermutation[] traces = new LiteralPermutation[this.size()*2+1];
//		Queue<Integer> toProcess = new LinkedList<Integer>();
//		toProcess.add(lit);
//		traces[LitUtil.getIndex(lit,this.size())] = this.getId();
//
//		while(!toProcess.isEmpty()) {
//			int curLit = toProcess.poll();
//			int litIndex= LitUtil.getIndex(curLit,this.size());
//			if(curLit == 0) continue;
//			for(LiteralPermutation perm : getGenerators()) {
//				int image = perm.imageOf(curLit);
//				int imageIndex = LitUtil.getIndex(image,this.size());
//
//
//				if(image != lit && traces[imageIndex] == null) {
//					traces[imageIndex] = traces[litIndex].compose(perm);
//					toProcess.add(image);
//				}
//			}
//		}
//		
//		LiteralPermutation[] invTraces = new LiteralPermutation[traces.length];
//		
//		for(int k = 0; k < traces.length; k++) {
//			if(traces[k] != null) {
//				invTraces[k] = traces[k].inverse();
//			}
//		}
//		
//		for(int k = 1; k <= size(); k++) {
//			int kIndex = LitUtil.getIndex(k,this.size()); 
//			if(traces[kIndex] != null) {
//				LiteralPermutation leftSide = traces[kIndex]; 
//				
//				for(LiteralPermutation gen : this.getGenerators()) {
//					int otherIndex = LitUtil.getIndex(gen.imageOf(k),this.size()); 
//					LiteralPermutation farRightSide = invTraces[otherIndex];
//					LiteralPermutation perm = leftSide.compose(gen).compose(farRightSide);
//					perms.add(perm);
//				}
//			}
//			
//			kIndex = LitUtil.getIndex(-k,this.size()); 
//			
//			if(traces[kIndex] != null) {
//				LiteralPermutation leftSide = traces[kIndex];  
//				
//				for(LiteralPermutation gen : this.getGenerators()) {
//					int otherIndex = LitUtil.getIndex(gen.imageOf(-k),this.size()); 
//					LiteralPermutation farRightSide = invTraces[otherIndex];
//					LiteralPermutation perm = leftSide.compose(gen).compose(farRightSide);
//					perms.add(perm);
//				}
//			}
//		}
		
	
		return getNewInstance(perms);
		
	}

	private void addStabs(SchreierVector vec,
			TreeSet<LiteralPermutation> perms, int lit) {
		LiteralPermutation leftSide = vec.trace(lit); 
		
		for(LiteralPermutation gen : this.getGenerators()) {
			LiteralPermutation farRightSide = vec.trace(gen.imageOf(lit)).inverse();
			LiteralPermutation perm = leftSide.compose(gen).compose(farRightSide);
			perms.add(perm);
		}
	}

	//Turn this group into an isomorphic automorphism group where every
	//object has perm applied to it
	public LiteralGroup experimental1(LiteralPermutation perm) {
		LinkedList<LiteralPermutation> newGens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation gen : getGenerators()) {
			newGens.add(perm.inverse().compose(gen).compose(perm));
		}

		return getNewInstance(newGens);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(LiteralPermutation perm : getGenerators()) {
			sb.append(perm.toString());
			sb.append(ConsoleDecodeable.newline);
		}

		return sb.toString(); 
	}
	
	public String toString(VariableContext context) {
		StringBuilder sb = new StringBuilder();

		for(LiteralPermutation perm : getGenerators()) {
			sb.append(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(perm.asArray()),false,context));
			sb.append(ConsoleDecodeable.newline);
		}

		return sb.toString(); 
	}
	@Override
	public int hashCode() {
		int hash = size();
		for(LiteralPermutation perm : getGenerators()) {
			hash += hash*31 + perm.hashCode();
		}
		
		return hash;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LiteralGroup) {
			LiteralGroup other = (LiteralGroup)obj;
			
			List<LiteralPermutation> thisGens = this.getGenerators();
			List<LiteralPermutation> otherGens = other.getGenerators();
			
			if(other.size() != this.size()) return false;
			
			Iterator<LiteralPermutation> thisIt = thisGens.iterator();
			Iterator<LiteralPermutation> otherIt = otherGens.iterator();
			
			while(thisIt.hasNext()) {
				LiteralPermutation thisPerm = thisIt.next();
				LiteralPermutation otherPerm = otherIt.next();
				
				if(!thisPerm.equals(otherPerm)) return false;
			}
		} else {
			return false;
		}
		
		return true;
	}


}
