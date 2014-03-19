package workflow.graph.local;

import group.LiteralGroup;

import java.util.Set;

import util.IntPair;

public abstract class LatticeHelper {
	public abstract Set<IntPair> getAllEdges();
	//Note: the ith generator of litSyms must be the ith generator of modelSyms
	public abstract void add(int[] filter, int[] full, LiteralGroup litSyms, LiteralGroup modelSyms);
	public abstract void clear();
}
