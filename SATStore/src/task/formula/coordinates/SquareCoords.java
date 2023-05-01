package task.formula.coordinates;

public class SquareCoords implements CoordinateGenerator {
	int size = 1;
	
	public SquareCoords(int size) {
		this.size=size;
	}
	
	
	@Override
	public CoordSpace generateCoords() {
		CoordSpace ret = new CoordSpace(4);
		
		for(int x1 = 0; x1 < size; x1++) {
			for(int y1 = 0; y1 < size; y1++) {
				for(int sqSize = 1; sqSize+Math.max(x1,y1)<=size; sqSize++) {
					int x2 = x1+sqSize;
					int y2 = y1+sqSize;
					double[] sq = new double[]{x1,x2,y1,y2};
					ret.addPt(sq);
				}
			}
		}
		
		return ret;
	}

}
