package task.clustering;

import java.util.List;
import java.util.Set;

public abstract class ClusterHierarchy {
	protected abstract int startNextLevel(); //Starts a new level in the hierarchy and returns index of the new level
	public abstract int getMaxLevel();
	public abstract List<Set<int[]>> getClusterAtLevel(int level);
	protected abstract void join(int index1, int index2); //Take the two clusters at index1 and index 2 of the previous level and join them together
														//returns the index of the resulting cluster
}
