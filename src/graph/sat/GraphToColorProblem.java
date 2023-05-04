package graph.sat;

import util.lit.LitSorter;
import util.lit.LitUtil;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.CNF;
import graph.Node;

public class GraphToColorProblem {

	public static Conjunctions coloringAsConjunction(Node[] graph, int numColors) {
		VariableContext coloringContext = new VariableContext();
		Variable[][] vars = new Variable[graph.length][numColors];
		while(coloringContext.size() < graph.length*numColors) {
			int num = coloringContext.size();
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
	
	public static CNF coloringAsCNF(Node[] graph, int numColors) {
		VariableContext coloringContext = new VariableContext();
		int[][] vars = new int[graph.length][numColors];
		while(coloringContext.size() < graph.length*numColors) {
			int num = coloringContext.size();
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

			vars[node][color] = coloringContext.getOrCreateVar(nodeName +"_"+ colorName).getUID();
		}

		CNF ret = new CNF(coloringContext);

		for(int k = 0; k < graph.length; k++) {
			Node n = graph[k];
			int[] mustHaveAColor = new int[numColors];

			for(int i = 0; i < numColors; i++) {
				mustHaveAColor[i]  = vars[k][i];

				for(int j = i+1; j < numColors; j++) {
					//If colored color i, then not colored color j
					if(j != i) {
						int[] onlyOneColor = new int[2];
						onlyOneColor[0] = -vars[k][i];
						onlyOneColor[1] = -vars[k][j];
						ret.fastAddClause(onlyOneColor);
					}
				}

				int minNode = graph[0].nodeNum;
				for(Node adj : n.getNeighbors()) {
					//if n colored color i, then adj no colored color i
					int[] noAdjVertsSameColor = new int[2];
					noAdjVertsSameColor[0] = -vars[k][i];
					noAdjVertsSameColor[1] = -vars[adj.nodeNum-minNode][i];
					LitSorter.inPlaceSort(noAdjVertsSameColor);
					ret.fastAddClause(noAdjVertsSameColor);
				}
			}
			ret.fastAddClause(mustHaveAColor);
		}
		
		ret.sort();
		ret.trySubsumption();

		return ret;
	}
}
