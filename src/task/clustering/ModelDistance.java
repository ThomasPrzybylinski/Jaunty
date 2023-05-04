package task.clustering;

public abstract class ModelDistance {
	public abstract double distance(int[] m1, int[] m2);
	
	//Can be overridden for speed (e.g. large models)
	public boolean lte(int[] m1, int[] m2,double radius) {
		return distance(m1,m2) <= radius;
	}
}
