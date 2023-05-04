import formula.simple.DNF;
import graph.PossiblyDenseGraph;
import task.formula.random.WeakTrueBoolFormula;
import workflow.graph.local.AllLocalSymAddr;


public class AllLocalRandomSizes {

	public static void main(String[] args) {
		final int numVars = 100;
		int min = 2;
		int max = 32;
		
		int numIters = 1;
		
		
		AllLocalSymAddr addr = new AllLocalSymAddr();
		
		for(int k = min; k <= max; k += Math.max(1,(max-2)/100)) {
//			SmallAllModelBoolFormula creat = new SmallAllModelBoolFormula(numVars,k);
			WeakTrueBoolFormula creat = new WeakTrueBoolFormula(numVars,k);
			
			double total = 0;
			
			for(int i = 0; i < numIters; i++) {
//				DNF cur = creat.getDNF();
				DNF cur = new DNF(creat.nextFormulaImpl());
				PossiblyDenseGraph<int[]> g = new PossiblyDenseGraph<int[]>(k);
				addr.addEdges(g,cur);
				
				double avgDeg = avgDeg(g);
				total += avgDeg/(k-1);
			}
			
			System.out.printf("%16d :\t %1.3f",k,(float)(total/(double)numIters));
			System.out.println();
		}

	}

	private static double avgDeg(PossiblyDenseGraph<int[]> g) {
	int num = 0;
	for(int k = 0; k < g.getNumNodes(); k++) {
		for(int i = 0; i < g.getNumNodes(); i++) {
			if(i == k) continue;
			
			if(g.areAdjacent(k,i)) {
				num++;
			}
		}
	}
	return num/(double)g.getNumNodes();
}
	
//	private static int countEdges(PossiblyDenseGraph<int[]> g) {
//		int num = 0;
//		for(int k = 0; k < g.getNumNodes(); k++) {
//			for(int i = k+1; i < g.getNumNodes(); i++) {
//				if(g.areAdjacent(k,i)) {
//					num++;
//				}
//			}
//		}
//		return num;
//	}

}
