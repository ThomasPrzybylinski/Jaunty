package workflow.graph.local;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import task.symmetry.local.BetterSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import util.IntPair;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class AllLocalSymAdder_NEW_oldBackup extends EdgeManipulator {
	private int numModels;
	private LinkedList<Integer> filter = new LinkedList<Integer>();
	
	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		numModels = representatives.size();

		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();

		while(vc.size() < rep.length) {
			vc.createNextDefaultVar();
		}

		ClauseList cl = new ClauseList(new VariableContext());
		cl.fastAddAll(representatives);

		LocalModelSymClauses cls = new LocalModelSymClauses(cl);

		LiteralGroup glob = addGlobalSyms(g,cls); //find global symmetries
		
		System.out.println(glob);

		LiteralGroup varGlob = getVarGroup(cls,glob);
		
		
		TreeSet<IntPair> allSeen = localize(g,cls, rep.length, 0, varGlob); //find local symmetries

		allSeen = getIsomorphicPairs(g,allSeen,glob);
		
		System.out.println(allSeen);
		
		System.out.print("1N: ");
		for(int k = 1; k < g.getNumNodes(); k++) {
			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		}
		System.out.println();

	}



	private TreeSet<IntPair> localize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives, int numVars, int curFilterVar,
			LiteralGroup curGroup) {
		TreeSet<IntPair> seen = new TreeSet<IntPair>();
		if(curFilterVar > numVars) return seen;
		int nextFilterVar = curFilterVar+1;

		SchreierVector vec = new SchreierVector(curGroup);

		for(int k = nextFilterVar; k <= numVars; k++) {
			if(vec.getRep(k) == k) {
				filter.add(k);
				seen.addAll(doLocalize(g,representatives,numVars,k,k,curGroup.getStabSubGroup(k)));
				filter.removeLast();
			}
			if(vec.getRep(-k) == -k) {
				filter.add(-k);
				seen.addAll(doLocalize(g,representatives,numVars,k,-k,curGroup.getStabSubGroup(-k))); //-k and k should have same stab sub groups
				filter.removeLast();
			}
		}

		//		doLocalize(g,representatives,numVars,nextFilterVar,null);
		//
		//		doLocalize(g,representatives,numVars,nextFilterVar,curFilterVar);
		//
		//		doLocalize(g,representatives,numVars,nextFilterVar,-curFilterVar);
		
		return seen;


	}

	private TreeSet<IntPair> doLocalize(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives,
			int numVars, int curFilterVar, int filter,
			LiteralGroup isoGroup) {

		//new recursive level
		representatives.post();
		boolean continueSym = true;

		int size = representatives.curValidModels();

		representatives.addCondition(filter);
		
		int newSize = representatives.curValidModels();

		//			System.out.println(newSize);

		LiteralGroup nextGroup = null;
		LiteralGroup group = null;
		TreeSet<IntPair> pairs = new TreeSet<IntPair>();
		
		if(newSize < size && newSize > 2) {
			//A new local context
			ClauseList cl = representatives.getCurList(true);

			TreeSet<Integer> existantVars = getExistantGroup(cl);

			group = getSymGroup(cl);

			
			System.out.println(this.filter);
			System.out.println(getModelGroup(representatives, group).reduce());
			
			LiteralGroup varGroup = getVarGroup(representatives,group).reduce();
			
//			System.out.println(this.filter);
//			System.out.println(varGroup);
//			
//			
			nextGroup = isoGroup.combine(varGroup);

//			System.out.println("Iso:");
//			System.out.println(nextGroup);
			
			SchreierVector vec = new SchreierVector(group);
			
			continueSym = addEdges(g, existantVars, vec,pairs);
		} else if(newSize == 2) {
			getSyms2Models(g, representatives,pairs);
		}

		if(continueSym && newSize > 2) {
			TreeSet<IntPair> childrenSeen = localize(g,representatives,numVars,curFilterVar, nextGroup);
			pairs.addAll(getIsomorphicPairs(g,childrenSeen,group));
		}

		//end of this recursive level
		representatives.pop();

		return pairs;
	}



	private TreeSet<IntPair> getIsomorphicPairs(
			PossiblyDenseGraph<int[]> g, TreeSet<IntPair> childrenSeen,
			LiteralGroup group) {
		TreeSet<IntPair> seen = new TreeSet<IntPair>();
		
		LinkedList<IntPair> toCompute = new LinkedList<IntPair>();
		toCompute.addAll(childrenSeen);
		seen.addAll(childrenSeen);
		
		while(!toCompute.isEmpty()) {
			IntPair pair = toCompute.poll();
			
			for(LiteralPermutation p : group.getGenerators()) {
				IntPair newP = pair.apply(p);
				
				if(!seen.contains(newP)) {
					seen.add(newP);
					g.setEdgeWeight(newP.getI1()-1,newP.getI2()-1,0);
					toCompute.push(newP);
				}
			}
		}
		
		return seen;
	}



	private boolean addEdges(PossiblyDenseGraph<int[]> g,
			TreeSet<Integer> existantVars, SchreierVector vec, TreeSet<IntPair> pairs) {
		boolean continueSym;
		continueSym = false;
		for(int j = 0; j < numModels; j++) {
			int var1 = j+1;
			if(!existantVars.contains(var1)) continue;

			for(int h = j+1; h < numModels; h++) {
				int var2 = h+1;
				if(!existantVars.contains(var2)) continue;

				if(vec.sameOrbit(var1,var2)) { 
					//if(ds.sameSet(j+1,h+1)) {
					pairs.add(new IntPair(var1,var2));
					g.setEdgeWeight(j,h,0);	
				}

				if(!g.areAdjacent(j,h) || g.getEdgeWeight(j,h) != 0) {
					continueSym = true;
				}
			}
		}
		return continueSym;
	}



	private TreeSet<Integer> getExistantGroup(ClauseList cl) {
		TreeSet<Integer> existantVars = new TreeSet<Integer>();

		for(int[] c : cl.getClauses()) {
			for(int i : c) {
				existantVars.add(Math.abs(i));
			}
		}
		return existantVars;
	}



	private NaiveLiteralGroup getSymGroup(ClauseList cl) {
		BetterSymFinder finder = new BetterSymFinder(cl);
		List<int[]> genList = finder.getSyms();
		LinkedList<LiteralPermutation> perms = new LinkedList<LiteralPermutation>();

		for(int[] i : genList) {
			perms.add(new LiteralPermutation(i));
		}

		NaiveLiteralGroup group = new NaiveLiteralGroup(perms);

		//		System.out.println();
		//		System.out.println(group);


		return group;
	}

	private NaiveLiteralGroup addGlobalSyms(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives) {
		ClauseList cl = representatives.getCurList(true);
		ClauseList cl2 = representatives.getCurList(false);

		NaiveLiteralGroup group = getSymGroup(cl);

		LiteralGroup group2 = getVarGroup(representatives,group);

		System.out.println(group2);

		SchreierVector vec = new SchreierVector(group);

		for(int j = 0; j < numModels; j++) {
			int var1 = j+1;

			for(int h = j+1; h < numModels; h++) {
				int var2 = h+1;

				if(vec.sameOrbit(var1,var2)) { 
					g.setEdgeWeight(j,h,0);	
				}
			}
		}

		return group;
	}

	private LiteralGroup getVarGroup(LocalModelSymClauses representatives,
			LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			gens.add(representatives.getVarPart(perm));
		}

		return group.getNewInstance(gens);

	}
	
	private LiteralGroup getModelGroup(LocalModelSymClauses representatives, LiteralGroup group) {

		LinkedList<LiteralPermutation> gens = new LinkedList<LiteralPermutation>();

		for(LiteralPermutation perm : group.getGenerators()) {
			gens.add(representatives.getModelPart(perm));
		}

		return group.getNewInstance(gens);

	}



	private void getSyms2Models(PossiblyDenseGraph<int[]> g,
			LocalModelSymClauses representatives, TreeSet<IntPair> pairs) {
		ClauseList cl = representatives.getCurList();

		int[] existantVars = new int[2];

		full: for(int[] c : cl.getClauses()) {
			for(int i : c) {
				if(existantVars[0] == 0) {
					existantVars[0] = i;
				} else if (existantVars[0] != i) {
					existantVars[1] = i;
					break full;
				}
			}
		}
		pairs.add(new IntPair(existantVars[0],existantVars[1]));
		g.setEdgeWeight(existantVars[0]-1,existantVars[1]-1,0);
	}

	//Assumes agreers is a sublist of representatives
	private int[] getLocalToGlobalIndecies(List<int[]> agreers,	List<int[]> representatives) {
		int[] ret = new int[agreers.size()];
		int repIndex = 0;

		for(int k = 0; k < agreers.size(); k++) {
			boolean found = false;
			while(!found) {
				if(Arrays.equals(agreers.get(k),representatives.get(repIndex))) {
					found = true;
					ret[k] = repIndex;
				}
				repIndex++;
			}
		}

		return ret;
	}



	@Override
	public boolean isSimple() {
		return true;
	}

}
