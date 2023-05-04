import io.DimacsLoaderSaver;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import task.sat.SATUtil;
import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import formula.simple.CNF;


public class TempTests_OLD {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
//		SimpleCNFCreator creat = new SimpleCNFCreator(9,5,3);
//		Conjunctions c = creat.nextFormulaImpl();
//		c = SATSump.getSubsumedConj(c);
//		
//		ClauseList cl =  new CNF(c);
		
		CNF cl = DimacsLoaderSaver.loadDimacs(new FileInputStream("bmc-ibm-2.cnf"));
		
		
//				new ClauseList(VariableContext.defaultContext);
//		
//		for(int k = 0; k < 6; k++) {
//			VariableContext.defaultContext.createNextDefaultVar();
//		}
//		
//		cl.addClause(1);
//		cl.addClause(3,2);
//		cl.addClause(4,5);
//		cl.addClause(6);
//		
		
		SimpleSymFinder ssf = new SimpleSymFinder(cl);
		
		DisjointSet<Integer> ds1 = ssf.getSymOrbits();
		
		for(int root : ds1.getRoots()) {
			for(int k = 1; k <= cl.getContext().size(); k++) {
				if(ds1.sameSet(root,k)) {
					System.out.print(k+" ");
				}
			}
			
			for(int k = 1; k <= cl.getContext().size(); k++) {
				if(ds1.sameSet(root,-k)) {
					System.out.print(-k+" ");
				}
			}
			System.out.println();
		}
		List<int[]> mods = SATUtil.getAllModels(cl);
		boolean[][] g = SymmetryUtil.getSymmetryGraph(cl.getContext(),mods);
		for(boolean[] b : g) {
			int[] toPrint = new int[b.length];
			for(int k = 0; k < toPrint.length; k++) {
				toPrint[k] = b[k] ? 1 : 0;
			}
			Arrays.toString(toPrint);
		}
		
//		ClauseList cl = new ClauseList(VariableContext.defaultContext);
//		
//		for(int k = 0; k < 6; k++) {
//			VariableContext.defaultContext.createNextDefaultVar();
//		}
//		
//		//Agreers
//		cl.addClause(1,2,3,4,5,6);
//		cl.addClause(1,2,3,-4,-5,-6);
//		
//		//Agree come-alongs
//		cl.addClause(1,2,3,4,-5,6);
//		cl.addClause(1,2,3,4,5,-6);		
//
//		//Non-agreers
//		cl.addClause(-1,-2,-3,4,5,6);
//		cl.addClause(-1,-2,-3,-4,-5,-6);
//		
//		cl.addClause(-1,2,-3,4,5,6);
//		cl.addClause(-1,-2,-3,-4,5,-6);
//		cl.addClause(1,-2,3,-4,-5,-6);
//		
//		cl.addClause(1,-2,-3,4,5,6);
//		cl.addClause(-1,-2,-3,4,-5,-6);
//		cl.addClause(1,2,-3,-4,-5,-6);
//
//		for(int[] clause : cl.getClauses()) {
//			int[] cur = clause;
//			for(int k = 0; k < 4; k++) {
//				System.out.println(Arrays.toString(cur));
//				cur = PermutationUtil.permute(cur,new int[]{0,4,5,6,-3,-2,-1});
//			}
//			System.out.println();
//		}
//		
//		
//		//		
//		List<int[]> syms = SymmetryUtil.getSyms(cl);
//		for(int[] i : syms) {
//			System.out.println(PermutationUtil.getPrettyCycles(PermutationUtil.getCycleRepresentation(i)));
//		}
//		
//		DisjointSet<int[]> ds = SymmetryUtil.findSymmetryOrbits(cl);
//		
//		boolean[] printed = new boolean[cl.getClauses().size()];
//		
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
		
		
		//		Simple3SATCreator creat = new Simple3SATCreator(4,4.3);
//		Conjunctions conj = (Conjunctions)creat.nextFormula();
//		Conjunctions conj2 = SATSump.getSubsumedConj(conj);
//		
//		System.out.println(conj);
//		System.out.println(conj2);
		
		
		
//		Variable C1 = Variable.getOrCreateVar("C1");
//		Variable C2 = Variable.getOrCreateVar("C2");
//		Variable C3 = Variable.getOrCreateVar("C3");
//		
//		Variable D1 = Variable.getOrCreateVar("D1");
//		Variable D2 = Variable.getOrCreateVar("D2");
//		Variable D3 = Variable.getOrCreateVar("D3");
//
//		
//		Disjunctions d = new Disjunctions();
//		Conjunctions c = new Conjunctions();
////		
//		c.add(new Disjunctions(C1.negate(), D1.negate()));
//		c.add(new Disjunctions(C2.negate(), D2.negate()));
//		c.add(new Disjunctions(C3.negate(), D3.negate()));
//		
//		d.add(c);
//		
//		c = new Conjunctions();
//		c.add(new Disjunctions(C1.negate(), D2.negate()));
//		c.add(new Disjunctions(C2.negate(), D3.negate()));
//		c.add(new Disjunctions(C3.negate(), D1.negate()));
//		
//		d.add(c);
//		
//		c = new Conjunctions();
//		c.add(new Disjunctions(C1.negate(), D3.negate()));
//		c.add(new Disjunctions(C2.negate(), D1.negate()));
//		c.add(new Disjunctions(C3.negate(), D2.negate()));
//
//		d.add(c);
////		
//		
//		
//		Conjunctions conj = new Conjunctions();
//		conj.add(d);
//		conj.add(new Disjunctions(C1,C2,C3));
//		conj.add(new Disjunctions(C1.negate(),C2.negate()));
//		conj.add(new Disjunctions(C1.negate(),C3.negate()));
//		conj.add(new Disjunctions(C2.negate(),C3.negate()));
//		
//		conj.add(new Disjunctions(D1,D2,D3));
//		conj.add(new Disjunctions(D1.negate(),D2.negate()));
//		conj.add(new Disjunctions(D1.negate(),D3.negate()));
//		conj.add(new Disjunctions(D2.negate(),D3.negate()));
////		
//		
//		System.out.println(conj);
//		BoolFormula from = (new Not(conj)).toNNF();
//		BoolFormula dnf = from.toDNF();
//		BoolFormula bf = (new Not(dnf)).toNNF();
////		
//		System.out.println(((Conjunctions)bf).trySubsumption().reduce());
//		System.out.println(SATSump.getSubsumedConj((Conjunctions)bf));

	}
}


//cl.addClause(-1,2,3,4,5,6);
//cl.addClause(-1,2,3,-4,5,6);
//cl.addClause(-1,2,3,4,-5,6);
//cl.addClause(-1,2,3,4,5,-6);
//cl.addClause(-1,2,3,-4,-5,6);
//cl.addClause(-1,2,3,-4,5,-6);
//cl.addClause(-1,2,3,4,-5,-6);
//cl.addClause(-1,2,3,-4,-5,-6);
//
//cl.addClause(1,-2,3,4,5,6);
//cl.addClause(1,-2,3,-4,5,6);
//cl.addClause(1,-2,3,4,-5,6);
//cl.addClause(1,-2,3,4,5,-6);
//cl.addClause(1,-2,3,-4,-5,6);
//cl.addClause(1,-2,3,-4,5,-6);
//cl.addClause(1,-2,3,4,-5,-6);
//cl.addClause(1,-2,3,-4,-5,-6);
//
//cl.addClause(1,2,-3,4,5,6);
//cl.addClause(1,2,-3,-4,5,6);
//cl.addClause(1,2,-3,4,-5,6);
//cl.addClause(1,2,-3,4,5,-6);
//cl.addClause(1,2,-3,-4,-5,6);
//cl.addClause(1,2,-3,-4,5,-6);
//cl.addClause(1,2,-3,4,-5,-6);
//cl.addClause(1,2,-3,-4,-5,-6);
//
//cl.addClause(-1,-2,3,4,5,6);
//cl.addClause(-1,-2,3,-4,5,6);
//cl.addClause(-1,-2,3,4,-5,6);
//cl.addClause(-1,-2,3,4,5,-6);
//cl.addClause(-1,-2,3,-4,-5,6);
//cl.addClause(-1,-2,3,-4,5,-6);
//cl.addClause(-1,-2,3,4,-5,-6);
//cl.addClause(-1,-2,3,-4,-5,-6);
//
//cl.addClause(-1,2,-3,4,5,6);
//cl.addClause(-1,2,-3,-4,5,6);
//cl.addClause(-1,2,-3,4,-5,6);
//cl.addClause(-1,2,-3,4,5,-6);
//cl.addClause(-1,2,-3,-4,-5,6);
//cl.addClause(-1,2,-3,-4,5,-6);
//cl.addClause(-1,2,-3,4,-5,-6);
//cl.addClause(-1,2,-3,-4,-5,-6);
//
//cl.addClause(1,-2,-3,4,5,6);
//cl.addClause(1,-2,-3,-4,5,6);
//cl.addClause(1,-2,-3,4,-5,6);
//cl.addClause(1,-2,-3,4,5,-6);
//cl.addClause(1,-2,-3,-4,-5,6);
//cl.addClause(1,-2,-3,-4,5,-6);
//cl.addClause(1,-2,-3,4,-5,-6);
//cl.addClause(1,-2,-3,-4,-5,-6);
//
//cl.addClause(-1,-2,-3,4,5,6);
//cl.addClause(-1,-2,-3,-4,5,6);
//cl.addClause(-1,-2,-3,4,-5,6);
//cl.addClause(-1,-2,-3,4,5,-6);
//cl.addClause(-1,-2,-3,-4,-5,6);
//cl.addClause(-1,-2,-3,-4,5,-6);
//cl.addClause(-1,-2,-3,4,-5,-6);
//cl.addClause(-1,-2,-3,-4,-5,-6);
