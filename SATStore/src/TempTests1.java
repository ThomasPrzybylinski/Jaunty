import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import group.LiteralPermutation;
import group.NaiveLiteralGroup;
import group.SchreierVector;
import hornGLB.AssignmentIter;
import hornGLB.BFSAssignmentIter;
import hornGLB.BasicAssignIter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.sat.SATUtil;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import task.symmetry.SHATTERSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import util.lit.DirectedLitGraph;
import util.lit.LitSorter;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;


public class TempTests1 {

	public TempTests1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		CNFCreator creat =
				
////	new IdentityCNFCreator("testcnf\\3blocks.cnf"),
//	new IdentityCNFCreator("testcnf\\4blocks.cnf"),
//	new IdentityCNFCreator("testcnf\\4blocksb.cnf"),
//	new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-01.cnf"),
//	new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-02.cnf"),
//	new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-03.cnf"),
//	new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-04.cnf"),
//	new IdentityCNFCreator("testcnf\\uf250-1065\\uf250-05.cnf"),
	new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-1.cnf");
//	new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-2.cnf"),
//	new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-3.cnf"),
//	new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-4.cnf"),
//	new IdentityCNFCreator("testcnf\\Flat200-479\\flat200-5.cnf"),
//	new IdentityCNFCreator("testcnf\\sw100-1.cnf"), //currently p=2^-5
//	new IdentityCNFCreator("testcnf\\sw100-2.cnf"),
//	new IdentityCNFCreator("testcnf\\ais8.cnf"),
//	new IdentityCNFCreator("testcnf\\ais10.cnf"),
//	new IdentityCNFCreator("testcnf\\ais12.cnf"),
//	new IdentityCNFCreator("testcnf\\qg7-13.cnf"),
//		CNFCreator creat = new IdentityCNFCreator("testcnf\\2bitmax_6.cnf");
		
		CNF cnf = creat.generateCNF(VariableContext.defaultContext);
		
		ISolver solve = cnf.getSolverForCNF();
		
		ModelIterator iter = new ModelIterator(solve);
		long num = 0;
		while(iter.findModel() != null) {
			num++;
			if((num-1)%1024 == 0) {
				System.out.println(num);
			}

		}
	}


}
