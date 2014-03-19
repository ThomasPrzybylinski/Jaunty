package workflow.eclectic;

public class HalfFunction implements NFunction {

	@Override
	public int calcNum(int n) {
		return n/2;
	}

	@Override
	public String toString() {
		return "Half";
	}
	
}
