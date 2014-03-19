//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.HashSet;
//import java.util.Random;
//import java.util.Set;
//import java.util.SortedMap;
//import java.util.TreeMap;
//import java.util.Map.Entry;
//import java.util.zip.Deflater;
//
//import org.apache.commons.exec.CommandLine;
//import org.apache.commons.exec.DefaultExecutor;
//import org.sat4j.core.VecInt;
//import org.sat4j.minisat.SolverFactory;
//import org.sat4j.specs.ContradictionException;
//import org.sat4j.specs.ISolver;
//import org.sat4j.specs.TimeoutException;
//
//import formula.BoolFormula;
//import formula.Conjunctions;
public class Main {
	
}
//import formula.Constant;
//import formula.Disjunctions;
//import formula.RITNode;
//import formula.Variable;
//
//
//public class Main {
//
//	private static final int times = 1;
//	public static void main(String[] args) throws TimeoutException, IOException {
//		Random rand = new Random();
//		double clauseVarRatio = 4.3;//rand.nextInt(7)+1;
//		double numVars = 6;//rand.nextInt(7)+3;
//		
//		
//		int addedTimes = 0;
//
//		long total = 0;
//		long totalSat = 0;
//		long totalUnsat = 0;
//
//		long sattimes = 0;
//		long unsattimes = 0;
//
//		//create variables
//		Variable[] varSample = new Variable[(int)(numVars*2)];
//		for(int i = 0; i < varSample.length; i+=2) {
//			Variable b = new Variable("Var"+i/2);
//			varSample[i] = b;
//			varSample[i+1] = b.negate();
//		}
//
//		for(int k = 0; k < times; k++) {
//
//			ISolver satSolve = SolverFactory.newLight();
//
//			satSolve.newVar((int)numVars);
//
//			int numBranches = 0;
//
//			Conjunctions threeSat = new Conjunctions();
//
//			populateSAT(clauseVarRatio, numVars, rand, varSample, threeSat,satSolve);
//		
//			PermutationGenerator gen = new PermutationGenerator(varSample.length/2);
//
//			int maxPermSize = 0;
//			int minPermSize = Integer.MAX_VALUE;
//			
//			addedTimes += gen.getTotal().intValue()-1;
//			
//			//while(gen.hasMore()) {
//				int[] perm = gen.getNext();
//				
//				for(int i = 0; i < perm.length; i++) {
//					varSample[2*i].setCompare(perm[i]);
//					varSample[2*i+1].setCompare(perm[i]);
//					
//				}
//				
//				BoolFormula unred =  RITFormula(threeSat);
//				BoolFormula impicates = unred.reduce(); 
//				numBranches = impicates.treeStringForLength().length();
//				
//				maxPermSize = Math.max(maxPermSize, numBranches);
//				minPermSize = Math.min(minPermSize, numBranches);
//
//				RITNode parent = new RITNode();
//				RITNode[] nodes = impicates.toRIT();
//
//				for(RITNode n : nodes) {
//					parent.addNode(n);
//				}
//				
//				if(numBranches == minPermSize) {
//					File gr = new File("graph");
//					PrintWriter out = new PrintWriter(gr);
//					out.append(parent.graphizString());
//					out.close();
//					
//					RITNode parent2 = new RITNode();
//					
//					nodes = unred.toRIT();
//
//					for(RITNode n : nodes) {
//						parent2.addNode(n);
//					}
//					
//					File gr2 = new File("graphUnRed");
//					out = new PrintWriter(gr2);
//					out.append(parent.graphizString());
//					out.close();
//					
//				}
//
//
//				Deflater def = new Deflater();
//				def.setInput(impicates.toString().getBytes());
//				def.finish();
//				byte[] bytes = new byte[impicates.toString().getBytes().length*2];
//				def.deflate(bytes);
//				bytes = null;
//
//
//
//				total += numBranches;
//
//				if(satSolve.isSatisfiable()) {
//					totalSat += numBranches;
//					sattimes++;
//				} else {
//					totalUnsat += numBranches;
//					unsattimes++;
//				}
//
//				System.out.println("TS: " + threeSat);
//				System.out.println(impicates.toString());
//				System.out.println("UNRNUM:  " + unred.treeStringForLength().length());
//				System.out.println("STRNUM:  " + numBranches);
//				System.out.println("RITLEN:  " + parent.size());
//				System.out.println("RITLEAF: " + parent.numLeaves());
//				System.out.println("Compression: " + def.getTotalIn()/ (double)def.getTotalOut());
//				
//				
//				//System.out.println(parent.graphizString());
//		//	}
//			System.out.println("MAXPERM: " + maxPermSize);
//			System.out.println("MAXPERM: " + minPermSize);
//			
//		}
//		
//		
//
//
//
//		//				Variable p = new Variable("p");
//		//				Variable q = new Variable("q");
//		//				Variable s = new Variable("s");
//		//				Variable r = new Variable("r");
//		//				
//		//				Variable notp = p.negate();
//		//				Variable notq = q.negate();
//		//				Variable nots = s.negate();
//		//				Variable notr = r.negate();
//		//				
//		//				Disjunctions d1 = new Disjunctions();
//		//				d1.add(p);
//		//				d1.add(q);
//		//				d1.add(nots);
//		//				
//		//				Disjunctions d2 = new Disjunctions();
//		//				d2.add(p);
//		//				d2.add(q);
//		//				d2.add(r);
//		//				
//		//				Disjunctions d3 = new Disjunctions();
//		//				d3.add(p);
//		//				d3.add(r);
//		//				d3.add(s);
//		//				
//		//				Disjunctions d4 = new Disjunctions();
//		//				d4.add(p);
//		//				d4.add(q);
//		//				
//		//				Conjunctions sat = new Conjunctions();
//		//				sat.add(d1);
//		//				sat.add(d2);
//		//				sat.add(d3);
//		//				sat.add(d4);
//		//				
//		//				BoolFormula out = RITFormula(sat);
//		//				System.out.println(out);
//		//				BoolFormula outr = out.reduce();
//		//				System.out.println(out.reduce());
//		//				total = outr.toString().length();
//		//		
//		//		
//		//				RITNode[] returned = outr.toRIT();
//		//				
//		//				total = returned[0].numLeaves();
//		//				
//		//				System.out.println(returned[0].toString());
//
//
//		System.out.println("Avg:             " + total/(double)(times));//+addedTimes));
//		System.out.println("Num Sat:         " + sattimes);
//		System.out.println("Num Unsat        " + unsattimes);
//		System.out.println("Satisfied Avg:   " + totalSat/(double)sattimes);
//		System.out.println("Unsatisfied Avg: " + totalUnsat/(double)unsattimes);
//		
//		CommandLine cl = CommandLine.parse("'C:\\Program Files (x86)\\Graphviz2.26.3\\bin\\dot' -Tjpg graph -og.jpg");
//		DefaultExecutor de = new DefaultExecutor();
//		de.execute(cl);
//	}
//
//	public static BoolFormula RITFormula(BoolFormula threeSat) {
//		SortedMap<Variable,Integer> sm = threeSat.getFreq(false);
//		return RITFormula(threeSat,sm);
//	}
//	
//	public static BoolFormula RITFormula(BoolFormula threeSat, SortedMap<Variable,Integer> sm) {
//		threeSat = threeSat.reduce();
//		Conjunctions c = new Conjunctions();
//
//		Set<Variable> vars = threeSat.getVars();
//
//		for(Entry<Variable,Integer> v : sm.entrySet()) {
//			//System.out.println(v.getKey() + " = " + v.getValue());
//		}
//		BoolFormula red = threeSat;//.reduce();
//
//		if(red instanceof Constant) {
//			return red;
//		}
//
//		Variable toSubst;
//
//		if(sm.isEmpty()) {
//			toSubst = vars.iterator().next();
//		} else {
//			//toSubst = vars.iterator().next();
//			toSubst = sm.firstKey(); 
//		}
//			
//		SortedMap<Variable,Integer> send = new TreeMap();
//		
//		send.putAll(sm);
//		send.remove(toSubst);
//
//		BoolFormula form1 = threeSat.subst(toSubst,false);
//		Disjunctions d = new Disjunctions();
//		d.add(toSubst);
//		d.add(RITFormula(form1,send));
//		c.add(d);
//
//		BoolFormula form2 = threeSat.subst(toSubst,true);
//		d = new Disjunctions();
//		d.add(toSubst.negate());
//		d.add(RITFormula(form2,send));
//		c.add(d);
//
//		Disjunctions form3 = new Disjunctions();
//		form3.add(form1);
//		form3.add(form2);
//		c.add(RITFormula(form3,send));
//
//		return c;
//
//	}
//
//	public static RITNode buildRIT(BoolFormula threeSat) {
//		RITNode root = new RITNode();
//		buildRIT(threeSat,root);
//		return root;
//	}
//
//	public static boolean buildRIT(BoolFormula threeSat, RITNode parent) {
//		Set<Variable> vars = threeSat.getVars();
//		BoolFormula red = threeSat.reduce();
//
//		if(red == Constant.TRUE) {
//			return true;
//		} else if(red == Constant.FALSE) {
//			return false;
//		}
//
//		Variable toSubst = vars.iterator().next();
//
//		BoolFormula form1 = threeSat.subst(toSubst,false);
//		RITNode child1 = new RITNode();
//		child1.setVar(toSubst);
//
//		BoolFormula form2 = threeSat.subst(toSubst,true);
//		RITNode child2 = new RITNode();
//		child2.setVar(toSubst.negate());
//
//		Disjunctions form3 = new Disjunctions();
//		form3.add(form1);
//		form3.add(form2);
//
//		boolean implicate = false;
//
//		if(buildRIT(form1,child1)) {
//			implicate = true;
//			parent.addNode(child1);
//		}
//
//		if(buildRIT(form2,child2)) {
//			implicate = true;
//			parent.addNode(child2);
//		}
//
//		if(buildRIT(form3,parent)) {
//			implicate = true;
//		}
//		return implicate;
//	}
//
//	private static int rit(BoolFormula threeSat) {
//		//System.out.println("F: " + threeSat);
//		//System.out.println("Red " + threeSat.reduce());
//		Set<Variable> vars = threeSat.getVars();
//		BoolFormula red = threeSat.reduce();
//
//		if(vars.size() == 0) {
//			return red == Constant.FALSE ? 0 : 1;
//		}
//		if(red == Constant.TRUE) {
//			return 0;
//		}
//
//		Variable toSubst = vars.iterator().next();
//
//		BoolFormula form1 = threeSat.subst(toSubst,false);
//		BoolFormula form2 = threeSat.subst(toSubst,true);
//
//		Disjunctions form3 = new Disjunctions();
//		form3.add(form1);
//		form3.add(form2);
//
//		int subTotal = 0;
//
//		int t1 = rit(form1);
//		int t2 = rit(form2);
//		subTotal += rit(form3);
//
//		if(t1 > 0) {
//			subTotal += t1+1;
//		}
//
//		if(t2 > 0) {
//			subTotal += t2+1;
//		}
//
//		return subTotal > 0 ? subTotal : 0;
//	}
//
//	private static void populateSAT(double clauseVarRatio, double numVars,
//			Random rand, Variable[] varSample, Conjunctions threeSat, ISolver satSolve) {
//
//		for(int i = 0; i < numVars*clauseVarRatio; i++) {
//			int[] clauseForSolve = new int[3];
//			HashSet<Integer> prevSeen = new HashSet<Integer>();
//			Disjunctions clause = new Disjunctions();
//
//			for(int j = 0; j < 3; j++) {
//				int varInd = -1;
//				do {
//					varInd = rand.nextInt(varSample.length);
//				} while(prevSeen.contains((varInd/2)));
//
//				prevSeen.add(varInd/2);
//				clauseForSolve[j] = (varInd/2)+1;
//				if(varInd%2 == 1) {
//					clauseForSolve[j] *= -1;
//				}
//
//				clause.add(varSample[varInd]);
//			}
//			threeSat.add(clause);
//			try {
//				satSolve.addClause(new VecInt(clauseForSolve));
//			} catch (ContradictionException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//}
