package task.clustering;


//Does not work well because two models could have no positive vars in common

public class CosineDistance extends ModelDistance {

	@Override
	public double distance(int[] m1, int[] m2) {
		int aSq = 0;
		int bSq = 0;
		int aDotb = 0;
		for(int i = 0; i < m1.length; i++) {
			int a = m1[i] > 0 ? 1 : -1;
			int b = m2[i] > 0 ? 1 : -1;
			aSq += a*a;
			bSq += b*b;
			aDotb += a*b;
		}
		
		return 1 - ((aDotb)/(Math.sqrt(aSq)*Math.sqrt(bSq)));
	}

}
