package task.clustering;

//We don't care about times when both values are negative
public class AssymDifference extends ModelDistance {

	@Override
	public double distance(int[] m1, int[] m2) {
		int dist = 0;
		int div = 0;
		for(int k = 0; k < m1.length; k++) {
			if(m1[k] != m2[k]) {
				dist++;
			}
			
			if(m1[k] > 0 || m2[k] > 0) {
				div++;
			}
		}
		
		if(div == 0) return 0;
		return dist/(double)div;
	}

}


