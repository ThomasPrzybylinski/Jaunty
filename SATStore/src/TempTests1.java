import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;

import java.util.ArrayList;
import java.util.List;

import util.lit.DirectedLitGraph;


public class TempTests1 {

	public TempTests1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DirectedLitGraph graph = new DirectedLitGraph(4);
		
		List<LiteralPermutation> gens = new ArrayList<LiteralPermutation>();
		
		gens.add(new LiteralPermutation(0,2,1,3,4));
		
		NaiveLiteralGroup nlg = new NaiveLiteralGroup(gens);
		SchreierVector vec = new SchreierVector(nlg);
		
//		graph.push(vec);
//		
//		
//		gens = new ArrayList<LiteralPermutation>();
//		gens.add(new LiteralPermutation(0,1,3,4,2));
//		nlg = new NaiveLiteralGroup(gens);
//		vec = new SchreierVector(nlg);
//		
//		graph.push(vec);
//		
//		
//		for(int k = 3; k <= 4; k++) {
//			System.out.println(graph.isValidMapping(1,k));
//		}

	}

}
