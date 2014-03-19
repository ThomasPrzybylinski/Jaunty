package workflow.graph.local;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import task.symmetry.RealSymFinder;
import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.BetterSymFinder;
import task.symmetry.local.LocalModelSymClauses;
import util.DisjointSet;
import workflow.graph.EdgeManipulator;

//Looks at all symmetries from agreement, not just of the two models
public class AllLocalSymAdder_Old extends EdgeManipulator {
	LocalModelSymClauses mCl;
	int numIters = 0;
	int[] filterRep;

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		long start = System.currentTimeMillis();
		numIters = 0;
		int[] rep = representatives.get(0);
		VariableContext vc = new VariableContext();

		while(vc.size() < rep.length) {
			vc.createNextDefaultVar();
		}
		
		ClauseList cl = new ClauseList(vc);
		cl.addAll(representatives);
		
		mCl = new LocalModelSymClauses(cl,true);
		

		int[] localToGlobalIndecies = getLocalToGlobalIndecies(representatives,representatives);
		filterRep = new int[vc.size()];
		localize(g,representatives,localToGlobalIndecies, rep.length, 0, new int[vc.size()]);
		
		long fullEnd = System.currentTimeMillis();
		
		System.out.println("Time: " + (fullEnd-start));
		
		System.out.println(numIters);
		System.out.print("1O: ");
		for(int k = 1; k < g.getNumNodes(); k++) {
			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		}
		System.out.println();

	}

	private void localize(PossiblyDenseGraph<int[]> g,
			List<int[]> representatives, int[] localToGlobalIndecies, int numVars, int nextVarInd, int[] prevFilter) {
		if(nextVarInd >= numVars) return;

		int[] filter = new int[numVars];
		
		doLocalize(g,representatives,localToGlobalIndecies,numVars,nextVarInd+1,filter);

		filter[nextVarInd] = nextVarInd+1;
		filterRep[nextVarInd] = nextVarInd+1;
		doLocalize(g,representatives,localToGlobalIndecies,numVars,nextVarInd+1,filter);

		filter[nextVarInd] = -filter[nextVarInd];
		filterRep[nextVarInd] = -filterRep[nextVarInd];
		doLocalize(g,representatives,localToGlobalIndecies,numVars,nextVarInd+1,filter);
		filterRep[nextVarInd] = 0;

	}

	private void doLocalize(PossiblyDenseGraph<int[]> g,
			List<int[]> representatives, int[] localToGlobalIndecies,
			int numVars, int nextVarInd, int[] filter) {
		boolean nullFilter = true;
		for(int i : filter) {
			if(i != 0) {
				nullFilter = false;
				break;
			}
		}

		mCl.post();
		if(nullFilter && nextVarInd != 1) {
			localize(g,representatives,localToGlobalIndecies,numVars,nextVarInd,filter);
		} else {
			numIters++;
			boolean continueSym = true;
			
			
			if(!nullFilter && filter[nextVarInd-1] != 0) {
				mCl.addCondition(filter[nextVarInd-1]);
			}
			
			List<int[]> agreers = SymmetryUtil.filterModels(representatives,filter);
			int[] nextLocalToGlobalIndecies = localToGlobalIndecies;

			//System.out.println(agreers.size() + " " + mCl.curValidModels());
			if((agreers.size() < representatives.size() && agreers.size() > 1) || nullFilter) {

				 nextLocalToGlobalIndecies = getLocalToGlobalIndecies(agreers,representatives);
				
				for(int k = 0; k < nextLocalToGlobalIndecies.length; k++) {
					nextLocalToGlobalIndecies[k] = localToGlobalIndecies[nextLocalToGlobalIndecies[k]];
				}

				ClauseList cl = SymmetryUtil.getInverseList(agreers,numVars);
				
				SimpleSymFinder finder2 = new SimpleSymFinder(cl);
				DisjointSet<Integer> ds = finder2.getSymOrbits();
				
				RealSymFinder finder = new RealSymFinder(cl);
				List<int[]> genList = finder.getSyms();
				LinkedList<LiteralPermutation> perms = new LinkedList<LiteralPermutation>();
				
				for(int[] i : genList) {
					perms.add(new LiteralPermutation(i));
				}
				
				NaiveLiteralGroup group = new NaiveLiteralGroup(perms);
				SchreierVector vec = new SchreierVector(group);
				ClauseList toPrint = new ClauseList(new VariableContext());
				toPrint.addAll(agreers);
//				System.out.println(Arrays.toString(filterRep));
//				System.out.println(toPrint);
//				System.out.println((new RealSymFinder(toPrint)).getSymGroup());
//				System.out.println("M");
//				System.out.println(group);

				continueSym = false;
				for(int j = 0; j < agreers.size(); j++) {
					for(int h = j+1; h < agreers.size(); h++) {
						int globj = nextLocalToGlobalIndecies[j];
						int globh = nextLocalToGlobalIndecies[h];

						if(vec.sameOrbit(j+1,h+1) != ds.sameSet(j+1,h+1)) {
							finder.getSyms();
							System.out.println(group);
							System.out.println(finder2.getSyms());
							System.out.println(ds.getSets());
							System.out.println(vec.getRep(j+1));
							System.out.println(vec.getRep(h+1));
							throw new RuntimeException();
						}
						
						//if(vec.sameOrbit(j+1,h+1)) { 
						if(ds.sameSet(j+1,h+1)) {
							g.setEdgeWeight(globj,globh,0);	
							int[] nextLocalToGlobalIndecies2 = getLocalToGlobalIndecies(agreers,representatives);
							
							for(int k = 0; k < nextLocalToGlobalIndecies2.length; k++) {
								nextLocalToGlobalIndecies2[k] = localToGlobalIndecies[nextLocalToGlobalIndecies2[k]];
							}
						}
						
						if(!g.areAdjacent(globj,globh) || g.getEdgeWeight(globj,globh) != 0) {
							continueSym = true;
						}
						
					}
				}
			}

			if(continueSym && agreers.size() > 2) {
				localize(g,agreers,nextLocalToGlobalIndecies,numVars,nextVarInd,filter);
			}
		}

		mCl.pop();

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
