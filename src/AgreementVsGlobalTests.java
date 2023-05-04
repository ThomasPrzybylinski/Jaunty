import java.util.Arrays;
import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import util.PermutationUtil;


public class AgreementVsGlobalTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClauseList cl = new ClauseList(VariableContext.defaultContext);

		for(int k = 0; k < 6; k++) {
			VariableContext.defaultContext.createNextDefaultVar();
		}

		
		//Agreers
		cl.addClause(1,2,3,4,5,6);
		cl.addClause(1,2,3,-4,-5,-6);

		//Agree come-alongs
		cl.addClause(1,2,3,4,-5,6);
		cl.addClause(1,2,3,4,5,-6);		

		//Non-agreers
		cl.addClause(-1,-2,-3,4,5,6);
		cl.addClause(-1,-2,-3,-4,-5,-6);

		cl.addClause(-1,2,-3,4,5,6);
		cl.addClause(-1,-2,-3,-4,5,-6);
		cl.addClause(1,-2,3,-4,-5,-6);

		cl.addClause(1,-2,-3,4,5,6);
		cl.addClause(-1,-2,-3,4,-5,-6);
		cl.addClause(1,2,-3,-4,-5,-6);

		System.out.println(cl.toNumString());

		//		for(int[] clause : cl.getClauses()) {
		//			int[] cur = clause;
		//			for(int k = 0; k < 4; k++) {
		//				System.out.println(Arrays.toString(cur));
		//				cur = PermutationUtil.permute(cur,new int[]{0,4,5,6,-3,-2,-1});
		//			}
		//			System.out.println();
		//		}

		List<int[]> syms = SymmetryUtil.getSyms(cl);
		for(int[] i : syms) {
			System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(i)));
		}

		DisjointSet<int[]> ds = SymmetryUtil.findSymmetryOrbits(cl);

		boolean[] printed = new boolean[cl.getClauses().size()];

		//		for(int k = 0; k < cl.getClauses().size(); k++) {
		//			if(!printed[k]) {
		//				System.out.println("NEXT ORBIT");
		//				System.out.println(Arrays.toString(cl.getClauses().get(k)));
		//				for(int i = k+1; i < cl.getClauses().size(); i++) {
		//					if(ds.sameSet(cl.getClauses().get(k),cl.getClauses().get(i))) {
		//						printed[i] = true;
		//						System.out.println(Arrays.toString(cl.getClauses().get(i)));
		//					}
		//				}
		//				System.out.println("");
		//			}
		//		}


		for(int k = 0; k < cl.getClauses().size(); k++) {
			for(int i = 0; i < cl.getClauses().size(); i++) {
				String print = (ds.sameSet(cl.getClauses().get(k),cl.getClauses().get(i))) ? "1 " : "0 ";
				System.out.print(print);
			}
			System.out.println();
		}

		System.out.println();
		boolean[][] g = SymmetryUtil.getSymmetryGraph(cl.getContext(),cl.getClauses());

		for(boolean[] bool : g) {
			for(boolean b : bool) {
				String print = b ? "1 " : "0 ";
				System.out.print(print);
			}
			System.out.println();
		}

		System.out.println();
		for(int k = 0; k < cl.getClauses().size(); k++) {
			for(int i = 0; i < cl.getClauses().size(); i++) {
				boolean globAgree = (ds.sameSet(cl.getClauses().get(k),cl.getClauses().get(i)));
				boolean locAgree = g[k][i];
				String print = null;
				if(globAgree && locAgree) {
					print = "1 ";
				} else if(globAgree && !locAgree) {
					print = "@ ";
				} else if(globAgree && !locAgree) {
					print = "# ";
				} else {
					print = "0 ";
				}
				System.out.print(print);
			}
			System.out.println();
		}

		ClauseList cl2 = new ClauseList(VariableContext.defaultContext);
		cl2.addClause(-1,-2,-3);
		cl2.addClause(-1,2,-3);
		cl2.addClause(1,-2,-3);
		cl2.addClause(1,2,3);

		DisjointSet<int[]> cls = SymmetryUtil.findSymmetryOrbits(cl2);
		System.out.println(cls.getRoots().size());

		printed = new boolean[cl2.getClauses().size()];
		for(int k = 0; k < cl2.getClauses().size(); k++) {
			if(!printed[k]) {
				System.out.println("NEXT ORBIT");
				System.out.println(Arrays.toString(cl2.getClauses().get(k)));
				for(int i = k+1; i < cl2.getClauses().size(); i++) {
					if(cls.sameSet(cl2.getClauses().get(k),cl2.getClauses().get(i))) {
						printed[i] = true;
						System.out.println(Arrays.toString(cl2.getClauses().get(i)));
					}
				}
				System.out.println("");
			}
		}

	}

}
