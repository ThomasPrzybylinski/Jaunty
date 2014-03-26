package workflow.graph.local;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.minisat.core.IntQueue;

import task.symmetry.SmallerIsomorphFinder;
import task.symmetry.RealSymFinder;
import task.symmetry.local.LocalSymClauses;
import util.IntPair;
import util.lit.DirectedLitGraph;
import util.lit.LitSorter;
import workflow.graph.ReportableEdgeAddr;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;
import group.PairSchreierVector;
import group.SchreierVector;

public class GlobalPruningAllLocalSymAdder extends ReportableEdgeAddr {
	private int iters;

	public ClauseList debug;

	private VariableContext context;

	private boolean globPrune = true;

	private SmallerIsomorphFinder iso = new SmallerIsomorphFinder();

	public GlobalPruningAllLocalSymAdder() {}

	public GlobalPruningAllLocalSymAdder(boolean prune) {
		globPrune = prune;

	}

	private LiteralGroup modelGlobSyms;


	public void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		this.context = orig.getContext();
		List<int[]> representatives = orig.getClauses();
		iters = 1; //At least global
		ClauseList globalModels = new ClauseList(new VariableContext());

		debug = globalModels;

		globalModels.addAll(representatives);

		LocalSymClauses clauses = new LocalSymClauses(globalModels);

		RealSymFinder globalFinder = new RealSymFinder(globalModels);
		LiteralGroup globalSyms = globalFinder.getSymGroup();
		modelGlobSyms = clauses.getModelGroup(globalSyms);



		DirectedLitGraph lit = new DirectedLitGraph(globalModels.getContext().size());
		lit.push(new PairSchreierVector(globalSyms,modelGlobSyms));


		//		System.out.println(modelGlobSyms);

		addEdges(g,clauses,modelGlobSyms);

		LinkedList<LocalInfo> info = new  LinkedList<LocalInfo>();
		info.add(new LocalInfo(globalFinder,globalSyms, new int[]{}));

		int[] canonical = clauses.getCanonicalInter(new int[]{});

		//		System.out.println(Arrays.toString(new int[]{}));
		//		System.out.println(Arrays.toString(canonical));
		//		System.out.println(globalSyms.toString(context));

		generateNext(g,clauses,lit,info,new int[]{}, canonical);

		//		System.out.println(iters);
		//		System.out.println(numComp);
		//		System.out.print("1B: ");
		//		for(int k = 1; k < g.getNumNodes(); k++) {
		//			if(g.areAdjacent(0,k)) System.out.print((k+1) + " ");
		//		}
		//		
		//		System.out.println();

	}

	private void generateNext(PossiblyDenseGraph<int[]> g, LocalSymClauses clauses, DirectedLitGraph litGraph,
			LinkedList<LocalInfo> info, int[] prevFilter, int[] prevCanon) {

		Set<Integer> validLits = clauses.curUsefulLits();//clauses.curValidLits();//

		for(int next : validLits) {
			int[] nextFilter = new int[prevFilter.length+1];
			System.arraycopy(prevFilter,0,nextFilter,0,prevFilter.length);
			nextFilter[nextFilter.length-1] = next;

			LitSorter.inPlaceSort(nextFilter);

			clauses.post();
			clauses.addCondition(next);

			int curNumModels = clauses.curValidModels();

			if(curNumModels <= 1) {
				clauses.pop();
				continue; //Irrelevant
			}

			int[] nextCanon = clauses.getCanonicalInter(nextFilter);


			TreeSet<Integer> allInNext = new TreeSet<Integer>();
			for(int i : nextCanon) {
				allInNext.add(i);
			}

			TreeSet<Integer> allInPrev = new TreeSet<Integer>();
			for(int i : prevCanon) {
				allInPrev.add(i);
			}


			boolean lexLoserPath = false;
			for(int i : allInNext) {
				if(!allInPrev.contains(i) && Math.abs(i) < Math.abs(next)) {
					lexLoserPath = true;
					break;
				} 
			}

			if((prevFilter.length > 0 && Math.abs(prevFilter[prevFilter.length-1]) > Math.abs(next)) 
					||
					lexLoserPath
					) {
			} else {

				LiteralGroup globalGroup = info.getFirst().getSyms();
				boolean symValid = true;

				if(globPrune) {
					//					symValid = info.getFirst().getSymUtil().getSmallerSubsetIfPossible(nextCanon,globalGroup) == null;
					symValid = iso.getSmallerSubsetIfPossible(nextCanon,globalGroup) == null;
				}

				if(symValid) {
					findSyms(g,nextFilter,nextCanon,clauses,litGraph,info);
				}
			}
			clauses.pop();
		}

	}

	private void findSyms(PossiblyDenseGraph<int[]> g,int[] filter, int[] canon, LocalSymClauses clauses,
			DirectedLitGraph litGraph, LinkedList<LocalInfo> info) {
		iters++;
		//		int oldSize = clauses.curValidModels();
		//		clauses.post();
		//		clauses.addCondition(filter[filter.length-1]);

		//				System.out.println(Arrays.toString(filter));
		//				System.out.println(Arrays.toString(canon));

		ClauseList cl = clauses.getCurList(true);
		int numModels = cl.getClauses().size();

		if(numModels > 1) {
			RealSymFinder finder = new RealSymFinder(cl);
			finder.addKnownSubgroup(info.getLast().getSyms().getStabSubGroup(filter[filter.length-1]).reduce());
			LiteralGroup syms = finder.getSymGroup();
			//						System.out.println(syms.toString(context));

			//			System.out.println(syms);
			LiteralGroup modelGroup  = null;
			modelGroup = clauses.getModelGroup(syms);

			//			System.out.println(modelGroup.reduce());


			boolean continueSeach = 
					addEdges(g,clauses,modelGroup); //Don't continue if every edge added
			//			litGraph.push(new SchreierVector(syms));

			info.addLast(new LocalInfo(finder,syms,filter));

			if(
					continueSeach && 
					numModels > 2) {
				generateNext(g,clauses,litGraph,info,filter,canon);
			}

			info.pollLast();

			//			litGraph.pop();

		} else if(numModels == 1 || numModels == 0) {
			iters--;
		}


		//		clauses.pop();
	}

	private boolean addEdges(PossiblyDenseGraph<int[]> g, LocalSymClauses clauses, LiteralGroup modelGroup) {
		IntQueue toCompute = new IntQueue();
		toCompute.ensure(modelGroup.size()+1);
		int[] orbits = new int[modelGroup.size()+1];
		int[] localOrbit = new int[modelGroup.size()+1];

		boolean cont = true;
		
		for(int k = 1; k < orbits.length; k++) {
			if(orbits[k] != 0) continue;

			//			int rep = modVec.getRep(k);

			toCompute.insert(k);
			orbits[k] = k;
			localOrbit[0] = k;
			int localOrbitIndex = 1;

			//Compute orbit of k
			while(toCompute.size() > 0) {
				int i = toCompute.dequeue();
				for(LiteralPermutation perm : modelGroup.getGenerators()) {
					int image = perm.imageOf(i);
					if(orbits[image] == 0) {
						orbits[image] = k;
						localOrbit[localOrbitIndex] = image;
						localOrbitIndex++;
						toCompute.insert(image);
					}
				}
			}

			//use the orbit to create edges
			Arrays.sort(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex));
			for(int i = 0; i < localOrbitIndex; i++) {
				for(int j = i+1; j < localOrbitIndex; j++) {
					g.setEdgeWeight(localOrbit[i]-1,localOrbit[j]-1,0);

					if(globPrune) {
						LinkedList<IntPair> edges = new LinkedList<IntPair>();
						edges.add(new IntPair(localOrbit[i],localOrbit[j]));

						while(!edges.isEmpty()) {
							IntPair edge = edges.poll();
							for(LiteralPermutation perm : modelGlobSyms.getGenerators()) {
								IntPair next = edge.applySort(perm);
								int p1 = next.getI1()-1;
								int p2 = next.getI2()-1;
								if(!g.areAdjacent(p1,p2) || g.getEdgeWeight(p1,p2) != 0) {
									g.setEdgeWeight(p1,p2,0);
									edges.add(next);
								}
							}
						}
					}
				}
			}
			Arrays.fill(localOrbit,0,Math.min(localOrbit.length-1,localOrbitIndex),0);

			//			for(int i = k+1; i <= modVec.getNumVars(); i++) {
			//				if(modVec.sameOrbit(k,i)) {
			////				if(modVec.getRep(i)==rep) {
			//					lp.getPairs().add(getCachedPair(k,i));
			//				}
			//			}
		}

//		SchreierVector vec = new SchreierVector(modelGroup);
//
//		boolean cont = false;
//
//		for(int k = 0; k < g.getNumNodes(); k++) {
//			for(int i = k+1; i < g.getNumNodes(); i++) {
//				if(vec.sameOrbit(k+1,i+1)) {
//
//					LinkedList<IntPair> edges = new LinkedList<IntPair>();
//					edges.add(new IntPair(k+1,i+1));
//					while(!edges.isEmpty()) {
//						IntPair edge = edges.poll();
//						int e1 = edge.getI1()-1;
//						int e2 = edge.getI2()-1;
//
//						g.setEdgeWeight(e1,e2,0);
//
//
//						if(globPrune) {
//							for(LiteralPermutation perm : modelGlobSyms.getGenerators()) {
//								IntPair next = edge.applySort(perm);
//								int p1 = next.getI1()-1;
//								int p2 = next.getI2()-1;
//								if(!g.areAdjacent(p1,p2) || g.getEdgeWeight(p1,p2) != 0) {
//									edges.add(next);
//								}
//							}
//						}
//					}
//				}
//				if(!g.areAdjacent(k,i) || g.getEdgeWeight(k,i) != 0) {
//					cont = true;
//				}
//				//				Doesn't do what its supposed to do atm
//				//				if(!g.areAdjacent(k,i) || g.getEdgeWeight(k,i) != 0) {
//				//					cont = true;
//				//				}
//
//
//
//			}
//		}

		return cont;
	}

	public int getIters() {
		return iters;
	}

	public void setIters(int iters) {
		this.iters = iters;
	}

	public boolean isSimple() {
		return true;
	}

	public String toString() {
		return globPrune ? "Sym(GlobalOnly)" : "Sym(NoPrune)";
	}

	@Override
	public long getPropogationTime() {
		return 0;
	}

	class LocalInfo {
		private RealSymFinder symUtil;
		private LiteralGroup syms;
		private int[] filter;
		public LocalInfo(RealSymFinder symUtil, LiteralGroup syms, int[] filter) {
			super();
			this.symUtil = symUtil;
			this.syms = syms;
			this.filter = filter;
		}

		public RealSymFinder getSymUtil() {
			return symUtil;
		}
		public void setSymUtil(RealSymFinder symUtil) {
			this.symUtil = symUtil;
		}
		public LiteralGroup getSyms() {
			return syms;
		}
		public void setSyms(LiteralGroup syms) {
			this.syms = syms;
		}

		public int[] getFilter() {
			return filter;
		}

		public void setFilter(int[] filter) {
			this.filter = filter;
		}
	}
}

