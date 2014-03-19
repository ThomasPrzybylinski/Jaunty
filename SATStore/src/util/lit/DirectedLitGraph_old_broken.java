package util.lit;

import group.LiteralPermutation;
import group.SchreierVector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeMap;

//TODO: rename to multigraph
@Deprecated //Currently does not work as we want
public class DirectedLitGraph_old_broken {
	private LinkedList<LevelPair>[] graph;
	private int numVars;
	private int curLevel = -1; //First level 0; global symmetry
	//CurLevel is the length of the filter to validate - 1
	private static LitComparator compare = new LitComparator();
	
	private int validateLevel = -1;

	@SuppressWarnings("unchecked")
	public DirectedLitGraph_old_broken(int numVars) {
		this.numVars = numVars;
		graph = (LinkedList<LevelPair>[])(new LinkedList[2*numVars + 1]);

		for(int k = 0; k < graph.length; k++) {
			graph[k] = new LinkedList<LevelPair>();
		}

	}

	protected LevelPair get(int from, int to) {
		LinkedList<LevelPair> edges = graph[LitUtil.getIndex(from,numVars)];
		Iterator<LevelPair> iter = edges.descendingIterator();

		while(iter.hasNext()) {
			LevelPair p = iter.next();

			if(p.getNode() == to) {
				return p;
			}
		}

		return null;
	}

	protected LevelPair get(int from, int to, int maxLevel) {
		LinkedList<LevelPair> edges = graph[LitUtil.getIndex(from,numVars)];
		Iterator<LevelPair> iter = edges.descendingIterator();

		while(iter.hasNext()) {
			LevelPair p = iter.next();

			if(p.getNode() == to && p.getLevel() <= maxLevel) {
				return p;
			}
		}

		return null;
	}



	//Assume that no edge of 'from' to 'to' is >= val
	protected void set(int from, int to, int val, LiteralPermutation perm) {
		graph[LitUtil.getIndex(from,numVars)].addLast(new LevelPair(val, to,perm));
	}

	protected boolean isEdge(int from, int to) {
		LinkedList<LevelPair> edges = graph[LitUtil.getIndex(from,numVars)];
		Iterator<LevelPair> iter = edges.descendingIterator();

		while(iter.hasNext()) {
			LevelPair p = iter.next();

			if(p.getNode() == to) {
				return true;
			}
		}

		return false;
	}

	protected boolean isEdge(int from, int to, int maxLevel) {
		LinkedList<LevelPair> edges = graph[LitUtil.getIndex(from,numVars)];
		Iterator<LevelPair> iter = edges.descendingIterator();

		while(iter.hasNext()) {
			LevelPair p = iter.next();

			if(p.getNode() == to && p.getLevel() <= maxLevel) {
				return true;
			}
		}

		return false;
	}

	public void push(SchreierVector curOrbits) {
		curLevel++;
		for(int k = 0; k < graph.length; k++) {
			int lit1 = LitUtil.getLit(k,numVars);
			if(lit1 == 0) continue;

			for(int i = k+1; i < graph.length; i++) {
				int lit2 = LitUtil.getLit(i,numVars);
				if(lit2 == 0) continue;

				if(curOrbits.sameOrbit(lit1,lit2)) {
					LiteralPermutation perm = curOrbits.getPerm(lit1,lit2);
					set(lit1,lit2,curLevel,perm);
					set(lit2,lit1,curLevel,perm.inverse());
				}
			}
		}
	}

	public void pop() {
		for(int k = 0; k < graph.length; k++) {
			LinkedList<LevelPair> edges = graph[k];
			Iterator<LevelPair> iter = edges.descendingIterator();

			while(iter.hasNext()) {
				LevelPair p = iter.next();

				if(p.getLevel() == curLevel) {
					iter.remove();
				}
				//TODO: might be ok since we are assume list is increasing by level to do a break
			}
		}

		curLevel--;
	}
	
	public int getValidateLevel() {
		return validateLevel;
	}

	public boolean validate(int[] nextFilter) {
		return validatePerm(nextFilter) != null;
	}
		
	public LiteralPermutation validatePerm(int[] nextFilter) {
		//TODO: validate filter and graph are compatible
		
		validateLevel = -1;

		int startLit = nextFilter[nextFilter.length-1];
		int start = LitUtil.getIndex(startLit,numVars);

		PriorityQueue<LevelPair> toSee = new PriorityQueue<LevelPair>();
		TreeMap<Integer,Integer> visitedLevel = new TreeMap<Integer,Integer>();
		visitedLevel.put(startLit,curLevel);

		for(int i = 0; i < graph.length; i++) {
			int iLit = LitUtil.getLit(i,numVars);
			if(iLit == 0) continue;

			if(isEdge(startLit,iLit) && start != i) {
				LevelPair rep = this.get(startLit,iLit);
				LevelPair toVisit = new LevelPair(rep.level,iLit,rep.getPerm());
				toSee.add(toVisit);
			}
		}

		while(!toSee.isEmpty()) {
			LevelPair nextNodeLevel = toSee.poll();
			int lit = nextNodeLevel.getNode();
			int index =  LitUtil.getIndex(lit,numVars);
			int level = nextNodeLevel.getLevel();
			LiteralPermutation perm = nextNodeLevel.getPerm();

			if(visitedLevel.containsKey(lit)) {
				if(visitedLevel.get(lit) >= level) {
					continue;
				}
			}
			visitedLevel.put(lit,level);


			//If the lit can get be moved to be smaller than the current smallest unstable lit,
			//then we know this is not the smallest
			int diff = Math.abs(lit) - Math.abs(nextFilter[level]);

			if(diff < 0 ) {
				validateLevel = level;
				return perm; 
			} else if(diff == 0 && lit > nextFilter[level]) {
				validateLevel = level;
				return perm;
			}

			for(int i = 0; i < graph.length; i++) {
				int iLit = LitUtil.getLit(i,numVars);
				if(iLit == 0) continue;

				if(isEdge(lit,iLit,level) && start != i) {
					LevelPair rep = this.get(lit,iLit,level);
					LevelPair toVisit = new LevelPair(rep.level,iLit,perm.compose(rep.getPerm()));
					toSee.add(toVisit);
				}
			}
		}

		return null;

	}

	protected static class LevelPair implements Comparable<LevelPair> {
		private int level;
		private int nextNode;
		private LiteralPermutation perm;

		public LevelPair(int level, int nextNode, LiteralPermutation perm) {
			this.level = level;
			this.nextNode = nextNode;
			this.perm = perm;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public int getNode() {
			return nextNode;
		}

		public void setNode(int nextNode) {
			this.nextNode = nextNode;
		}
		
		public LiteralPermutation getPerm() {
			return perm;
		}

		private final static LitComparator nodeComp = new LitComparator();
		//We want priority queue to give larger levels first, smaller vars first
		@Override
		public int compareTo(LevelPair o) {
			int comp1 = this.level-o.level;

			if(comp1 != 0) {
				return -comp1;
			}

			return nodeComp.compare(this.nextNode,o.nextNode);
		}

		@Override
		public String toString() {
			return "(" + level + "," + nextNode + ")";
		}



	}
}
