package graph.sat;

import java.util.ArrayList;
import java.util.List;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Literal;
import formula.Variable;
import formula.VariableContext;
import graph.Node;
import task.NChooseRGenerator;

public class SpanningCycleGraphProblem {

	//Basically, every node must have exactly two adjacent chosen edges
	public static Conjunctions cycleAsCNF(Node[] graph) {
		VariableContext curContext = VariableContext.defaultContext;

		List<Literal> vars = new ArrayList<Literal>();

		for(int k = 0; k < graph.length; k++) {
			Node n = graph[k];
			for(Node n2 : n.getNeighbors()) {
				if(n.nodeNum < n2.nodeNum) {
					vars.add(curContext.getOrCreateVar("E_"+n.nodeNum+"_"+n2.nodeNum).getPosLit());
				} 
			}
		}

		Conjunctions ret = new Conjunctions();

		for(int k = 0; k < graph.length; k++) {
			Node n = graph[k];
			Variable[] adjEdges = new Variable[n.getNeighbors().size()];

			int index = 0;
			for(Node n2 : n.getNeighbors()) {
				if(n.nodeNum < n2.nodeNum) {
					adjEdges[index] = curContext.getOrCreateVar("E_"+n.nodeNum+"_"+n2.nodeNum);
					index++;
				} else {
					adjEdges[index] = curContext.getOrCreateVar("E_"+n2.nodeNum+"_"+n.nodeNum);
					index++;
				}
			}

			//At least 2 edges per node
			for(int i = 0; i < adjEdges.length; i++) {
				Disjunctions d = new Disjunctions();
				for(int j = 0; j < adjEdges.length; j++) {
					if(j != i) {
						d.add(adjEdges[j].getPosLit());
					}
				}
				ret.add(d);
			}


			if(adjEdges.length > 2) {
				//At most 2 edges per node
				NChooseRGenerator ncr = new NChooseRGenerator(adjEdges.length,3);

				while(ncr.hasMore()) {
					Disjunctions d = new Disjunctions();
					int[] next = ncr.getNext();

					for(int var : next) {
						d.add(adjEdges[var].getNegLit());
					}

					ret.add(d);
				}
			}
		}

		return ret;
	}
}
