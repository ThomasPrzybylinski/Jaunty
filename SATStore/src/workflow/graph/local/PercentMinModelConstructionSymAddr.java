package workflow.graph.local;

import task.symmetry.local.LocalSymClauses;


public class PercentMinModelConstructionSymAddr extends ConstructionSymAddr {
	private double percentage = 1;
	public PercentMinModelConstructionSymAddr() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PercentMinModelConstructionSymAddr(boolean checkFirstInLocalOrbit,
			boolean checkLitGraph, boolean checkFullGlobal,
			boolean checkFullLocalPath, double percentage) {
		super(checkFirstInLocalOrbit, checkLitGraph, checkFullGlobal,
				checkFullLocalPath);
		this.percentage = percentage;
	}

	@Override
	protected void init(LocalSymClauses clauses) {
		super.init(clauses);
		int numModels = (int)Math.max(2,Math.round(percentage*clauses.curValidModels()));
		setMinModels(numModels);
	}
	
	
}
