package task.formula.coordinates;

public class HyperPlane {
	private int dim;
	private double[] coeffs;
	
	public HyperPlane(int dim, double... coeffs) {
		assert(coeffs.length == dim+1);
		this.dim=dim;
		this.coeffs=coeffs;
	}
	
	//Only for x-y linese
	public String getXYString() {
		int singleVar = -1;
		
		for(int k = 1; k < coeffs.length; k++) {
			if(coeffs[k] != 0) {
				if(singleVar != -1) {
					singleVar=-1;
					break;
				} else {
					singleVar=k;
				}
			}
		}
		
		String ret = null;
		if(singleVar > -1) {
			char thing='\0';
			if(singleVar==1) {
				 thing ='v';
			} else if(singleVar==2) {
				thing='h';
			}
			if(thing != '\0') {
				ret = "abline("+thing+"="+-coeffs[0]+",col=\"red\")";
			}
		} else {
			ret = "abline(a="+(coeffs[0]/-coeffs[2])+",b="+(coeffs[1]/-coeffs[2])+",col=\"red\")";
		}
		
		return ret;
	}
	
	public double value(double[] pt) {
		double ret = coeffs[0];
		
		for(int k = 0; k < pt.length; k++) {
			ret += coeffs[k+1]*pt[k];
		}
		
		return ret;
	}
	
}
