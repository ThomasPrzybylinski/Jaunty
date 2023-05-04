package workflow.graph.local;


public class MinModelConstructionSymAddr extends ConstructionSymAddr {
	public MinModelConstructionSymAddr() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MinModelConstructionSymAddr(boolean checkFirstInLocalOrbit,
			boolean checkLitGraph, boolean checkFullGlobal,
			boolean checkFullLocalPath, int numModels) {
		super(checkFirstInLocalOrbit, checkLitGraph, checkFullGlobal,
				checkFullLocalPath);
		setMinModels(numModels);
	}
}
