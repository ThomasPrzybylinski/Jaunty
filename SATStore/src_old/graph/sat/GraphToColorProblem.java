package graph.sat;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import graph.Node;

public class GraphToColorProblem {

	public static Conjunctions coloringAsCNF(Node[] graph, int numColors) {
		VariableContext coloringContext = new VariableContext();
		Variable[][] vars = new Variable[graph.length][numColors];
		while(coloringContext.getNumVarsMade() < graph.length*numColors) {
			int num = coloringContext.getNumVarsMade();
			int node = num/numColors;
			int color = num%numColors;

			String nodeName = graph[node].label != null ? graph[node].label : ""+node;
			String colorName = ""+color;

			if(numColors <= 3) {
				switch(color) {
				case 0 : colorName = "R"; break;
				case 1 : colorName = "G"; break;
				case 2 : colorName = "B"; break;
				default : break;
				}
			}

			vars[node][color] = coloringContext.getOrCreateVar(nodeName +"_"+ colorName);
		}

		Conjunctions ret = new Conjunctions();
		ret.setCurContext(coloringContext);

		for(int k = 0; k < graph.length; k++) {
			Node n = graph[k];
			Disjunctions mustHaveAColor = new Disjunctions();

			for(int i = 0; i < numColors; i++) {
				mustHaveAColor.add(vars[k][i].getPosLit());

				for(int j = 0; j < numColors; j++) {
					//If colored color i, then not colored color j
					if(j != i) {
						Disjunctions onlyOneColor = new Disjunctions();
						onlyOneColor.add(vars[k][i].getNegLit());
						onlyOneColor.add(vars[k][j].getNegLit());
						ret.add(onlyOneColor);
					}
				}

				int minNode = graph[0].nodeNum;
				for(Node adj : n.getNeighbors()) {
					//if n colored color i, then adj no colored color i
					Disjunctions noAdjVertsSameColor = new Disjunctions();
					noAdjVertsSameColor.add(vars[k][i].getNegLit());
					noAdjVertsSameColor.add(vars[adj.nodeNum-minNode][i].getNegLit());
					ret.add(noAdjVertsSameColor);
				}
			}
			ret.add(mustHaveAColor);
		}

		return ret;
	}
}
