package task;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.BoolFormula;
import formula.Literal;
import formula.RITNode;
import formula.Variable;

public class RITTask implements FormulaTask {
	private StringBuilder executeLog = new StringBuilder();

	private ISolver satSolve = null;

	private boolean permute = false;

	private int total;
	private int totalComp = 0;
	private int times;


	private int sattimes;
	private int unsattimes;

	private int totalSat;
	private int totalUnsat;

	@Override
	public String aggregateReport() {
		return  "Avg:             " + total/(double)(times) + "\n" +
		"CompAvg:             " + totalComp/(double)(times) + "\n" +
		"Num Sat:         " + sattimes + "\n" +
		"Num Unsat        " + unsattimes + "\n" +
		"Satisfied Avg:   " + totalSat/(double)sattimes + "\n" +
		"Unsatisfied Avg: " + totalUnsat/(double)unsattimes;
	}

	@Override
	public String executeReport() {
		return executeLog.toString();
	}

	@Override
	public void executeTask(BoolFormula formula) {
		times++;

		executeLog = new StringBuilder();
		Set<Variable> vars = formula.getVars();
		Literal[] varSample = new Literal[vars.size()*2];

		int varSampleIndex = 0;
		for(Variable var : vars) {
			varSample[varSampleIndex] = var.getPosLit();
			varSample[varSampleIndex+1] = var.getNegLit();
			varSampleIndex += 2;
		}

		PermutationGenerator gen = new PermutationGenerator(varSample.length/2);

		int maxPermSize = 0;
		int minPermSize = Integer.MAX_VALUE;

		while(gen.hasMore()) {
			int[] perm = gen.getNext();

			for(int i = 0; i < perm.length; i++) {
				varSample[2*i].getVar().setCompare(perm[i]);
			}
			execute(formula, maxPermSize, minPermSize);
			
			if(!permute) break;
		}
		executeLog.append("\n" +"MAXPERM: " + maxPermSize + "\n" +
				"MINPERM: " + minPermSize);
	}

	private void execute(BoolFormula formula, int maxPermSize, int minPermSize) {
		int numBranches;
		BoolFormula unred =  RITCreator.RITFormula(formula);
		BoolFormula impicates = unred.reduce(); 
		numBranches = impicates.treeStringForLength().length();

		maxPermSize = Math.max(maxPermSize, numBranches);
		minPermSize = Math.min(minPermSize, numBranches);

		RITNode parent = new RITNode();
		RITNode[] nodes = impicates.toRIT();

		for(RITNode n : nodes) {
			parent.addNode(n);
		}

		try {
			if(numBranches == minPermSize) {
				File gr = new File("graph");
				PrintWriter out = new PrintWriter(gr);
				//System.out.println("RealSize: " + parent.size());
				out.append(parent.graphizString());
				out.close();

				RITNode parent2 = new RITNode();

				nodes = unred.toRIT();

				for(RITNode n : nodes) {
					parent2.addNode(n);
				}

				File gr2 = new File("graphComp");
				out = new PrintWriter(gr2);
				parent.compress();
				totalComp += parent.size();
				//System.out.println("CompSize: " + parent.size());
				out.append(parent.graphizString());
				out.close();

			}
		} catch(IOException ioe) {
			System.err.println("Exception writing graphiz image");
			ioe.printStackTrace();
		}

		total += numBranches;

		if(satSolve != null) {
			try {
				if(satSolve.isSatisfiable()) {
					totalSat += numBranches;
					sattimes++;
				} else {
					totalUnsat += numBranches;
					unsattimes++;
				}
			} catch(TimeoutException te) {
				System.err.println("TIMED OUT!");
			}
		}

		//per-permuation-iteration printouts (probably task)
		executeLog.append("TS: " + formula + "\n" +
				impicates.toString() + "\n" +
				"UNRNUM:  " + unred.treeStringForLength().length() + "\n" +
				"STRNUM:  " + numBranches + "\n" +
				"RITLEN:  " + parent.size() + "\n" +
				"RITLEAF: " + parent.numLeaves());
	}

	public double getAvg() {
		return total/(double)(times);
	}
	
	public double getCompAvg() {
		return totalComp/(double)(times);
	}
}
