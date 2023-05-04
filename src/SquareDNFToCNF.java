import java.util.Arrays;
import java.util.List;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.CNF;
import subsumptionMain.SATSump;
import task.sat.SATUtil;


public class SquareDNFToCNF {

	public static void main(String[] args) throws Exception {
		VariableContext context = new VariableContext();
		context.ensureSize(9);
		
		Variable mid = context.getVar(5);
		Variable top = context.getVar(2);
		Variable bot = context.getVar(4);
		Variable left = context.getVar(6);
		Variable right = context.getVar(8);
		
		Variable topL = context.getVar(1);
		Variable topR = context.getVar(3);
		Variable botL = context.getVar(7);
		Variable botR = context.getVar(9);
		
		Disjunctions d = new Disjunctions();
		Conjunctions c = new Conjunctions(mid.getPosLit(),top.getNegLit(),bot.getNegLit(),left.getNegLit(),right.getNegLit());
		d.add(c);
		c = new Conjunctions(mid.getNegLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getPosLit(),bot.getPosLit(),left.getNegLit(),right.getNegLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getNegLit(),bot.getNegLit(),left.getPosLit(),right.getPosLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getPosLit(),bot.getNegLit(),left.getPosLit(),right.getNegLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getPosLit(),bot.getNegLit(),left.getNegLit(),right.getPosLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getNegLit(),bot.getPosLit(),left.getPosLit(),right.getNegLit());
		d.add(c);
		c = new Conjunctions(mid.getPosLit(),top.getNegLit(),bot.getPosLit(),left.getNegLit(),right.getPosLit());
		d.add(c);

		System.out.println(d);
		BoolFormula form = d.toCNF();
		
		CNF cnf = new CNF((Conjunctions)form);
		cnf = cnf.trySubsumption();
		System.out.println(cnf);
		cnf = SATSump.getSubsumedConj(cnf);
		cnf = cnf.squeezed();
		System.out.println(cnf);
		
		List<int[]> mods =  SATUtil.getAllModels(cnf);
		
		for(int[] i : mods) {
			System.out.println(Arrays.toString(i));
		}
		
//		ModelGiver giver = new AllSquares(3);
//		
//		List<int[]> mods = giver.getAllModels(VariableContext.defaultContext);
//		
//		
//		Disjunctions d = new Disjunctions();
//		
//		for(int[] m : mods) {
//			Conjunctions c= new Conjunctions();
//			for(int i : m) {
//				Variable var = VariableContext.defaultContext.getVar(Math.abs(i));
//				
//				c.add(i < 0 ? var.getNegLit() : var.getPosLit());
//			}
//			
//			d.add(c);
//		}
//		
//		BoolFormula to = d.toNNF().toCNF();
//		System.out.println(to);
//		CNF cnf = new CNF(((Conjunctions)to));
//		cnf = cnf.trySubsumption();
//		System.out.println(cnf);

	}

}
