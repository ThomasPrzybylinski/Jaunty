package workflow.eclectic;



//Two are two close if the distance between the two is <= the distance from one to the furthest of its sqrt(n) nearest neighbors
//Sort of adpated from from  page 486 of Data Mining: Concepts and Techniques books (han et al)
public class ConstantFunctionNClosenessFinder extends ClosenessFinder {
	private NFloatFunction func;
	
	public ConstantFunctionNClosenessFinder(NFloatFunction nFunc) {
		this.func = nFunc;
	}

	@Override
	public boolean areTooClose(int i, int k) {
		if(!pdg.areAdjacent(k,i)) {
			return false;
		}
		float weight = pdg.getEdgeWeight(k,i);
		return func.close(weight);
	}

	@Override
	public void initialize() {

	}

	@Override
	public String toString() {
		return "NN(" + func.toString() +")";
	}
	
	

}
