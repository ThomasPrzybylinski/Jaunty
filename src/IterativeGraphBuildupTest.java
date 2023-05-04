import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import task.formula.AllConnectedGraphs;
import task.symmetry.RealSymFinder;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.MinimalDistanceEdges;

public class IterativeGraphBuildupTest {

	public static void main(String[] args) throws Exception {
		VariableContext vc = new VariableContext();
		ModelGiver mg = new AllConnectedGraphs(5);//new CNFCreatorModelGiver(new QueensToSAT(8));
		EdgeManipulator manip = new MinimalDistanceEdges(1);
		ClauseList cl = new ClauseList(vc);
		List<int[]> models = mg.getAllModels(vc);

		cl.addAll(models);
		PossiblyDenseGraph<int[]> modGraph = new PossiblyDenseGraph<int[]>(cl.getClauses());
		manip.addEdges(modGraph,cl);
		
		

		boolean addedEdges = true;
		int times = 0;
		while(addedEdges) {
			times++;
			System.out.println("Round "+times);
			addedEdges = false;

			ClauseList graphRep = new ClauseList(vc);
			for(int k = 0; k < cl.size(); k++) {
				for(int i = k+1; i < cl.size(); i++) {
					if(modGraph.areAdjacent(k,i)) {
						graphRep.addClause(k+1,i+1);
					}
				}
			}
			
			RealSymFinder finder = new RealSymFinder(graphRep);
			LiteralGroup lg = finder.getSymGroup();
			SchreierVector sv = new SchreierVector(lg);
			
			for(int k = 0; k < cl.size(); k++) {
				for(int i = k+1; i < cl.size(); i++) {
					if(!modGraph.areAdjacent(k,i) && sv.sameOrbit(k+1,i+1)) {
						addedEdges=true;
						modGraph.setAdjacent(k,i);
					}
				}
			}
		}
	}
}
