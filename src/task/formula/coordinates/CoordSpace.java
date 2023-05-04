package task.formula.coordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoordSpace {
	private int dim;
	private List<double[]> pts;
	
	public CoordSpace(int dim) {
		this.dim=dim;
		pts = new ArrayList<double[]>();
	}
	
	public List<double[]> getPts() {
		return Collections.unmodifiableList(pts);
		
	}
	
	public int getDim() {
		return dim;
	}
	
	public void addPt(double... pt) {
		if(pt.length != dim) throw new RuntimeException("Invalid Coord Length");
		pts.add(pt);
	}
	
	public void addPts(double[]... coords) {
		for(double[] pt : coords) {
			addPt(pt);
		}
	}
	
	public void addPts(Iterable<double[]> coords) {
		for(double[] pt : coords) {
			addPt(pt);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(dim+"D space[\n");
		
		for(double[] pt : pts) {
			sb.append(Arrays.toString(pt));
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
}
