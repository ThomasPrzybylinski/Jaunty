package workflow.eclectic;

public class SqrtNFunction implements NFunction {

	@Override
	public int calcNum(int n) {
		return (int)Math.sqrt(n);
	}

	@Override
	public String toString() {
		return "Sqrt";
	}
	
}
