package task.formula.coordinates;

import java.util.ArrayList;
import java.util.Random;

public class GaussCoords implements CoordinateGenerator {

	
	private static final int seed = 3;
	@Override
	public CoordSpace generateCoords() {
		ArrayList<double[]> ret = new ArrayList<double[]>();
		

		Random rand = new Random(seed);
		for(int k = 0; k < 10; k++) {
			double[] next = new double[2];
			rand.nextGaussian();
			next[0] = 30+ rand.nextGaussian()/20;
			rand.nextGaussian();
			next[1] = k + rand.nextGaussian()/20;
			ret.add(next);
		}


		rand = new Random(seed);
		for(int k = 0; k < 10; k++) {
			double[] next = new double[2];
			next[0] = 10+rand.nextGaussian()/20;
			rand.nextGaussian();
			next[1] = k+rand.nextGaussian()/20;
			rand.nextGaussian();
			ret.add(next);
		}
		
		
//		for(int k = 0; k < 10; k++) {
//			double[] next = new double[2];
//			next[0] = 30-rand.nextGaussian();
//			next[1] = -rand.nextInt(10);
//			ret.add(next);
//		}
//		
//		
//		rand = new Random(seed);
//		for(int k = 0; k < 10; k++) {
//			double[] next = new double[2];
//			next[0] = 10+rand.nextGaussian();
//			next[1] = rand.nextInt(10);
//			ret.add(next);
//		}
		
//		for(double d[] : ret) {
//			d[0]=d[0]+rand.nextGaussian();
//			d[1]=d[1]+rand.nextGaussian();
//		}
		
		CoordSpace space = new CoordSpace(2);
		space.addPts(ret);
		return space;
	}

}
