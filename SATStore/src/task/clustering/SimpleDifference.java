package task.clustering;

//TODO: Get the real name of this distance metric
public class SimpleDifference extends ModelDistance {

	@Override
	public double distance(int[] m1, int[] m2) {
		int dist = 0;
		for(int k = 0; k < m1.length; k++) {
			if(m1[k] != m2[k]) {
				dist++;
			}
		}
		return dist;
	}

}
