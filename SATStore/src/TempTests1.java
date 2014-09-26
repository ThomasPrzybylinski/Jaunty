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
		ModelGiver giver = new SmallAllModelBoolFormula(10,32,2);//

		List<int[]> mods = giver.getAllModels(VariableContext.defaultContext);
		
		mods.add(new int[]{1,9,-1,-9,2,-3,3});
		
		for(int[] i : mods) {
			LitSorter.inPlaceSort(i);
			System.out.println(Arrays.toString(i));
		}
		
//		ClauseList cl = new ClauseList(VariableContext.defaultContext);
//		cl.fastAddAll(mods);
//		cl.sort();
//
//		LocalSymClauses rep = new LocalSymClauses(cl);
//
//		for(int i1 = 2; i1 < mods.size(); i1++) {
//			for(int i2 = 39; i2 < mods.size(); i2++) {//i1+1; i2 < mods.size(); i2++) {
//
//				//		int i1 = 1;
//				//		int i2 = 62;
//
//				int[] m1 = cl.getClauses().get(i1);
//				int[] m2 = cl.getClauses().get(i2);
//
//				System.out.println(Arrays.toString(m1));
//				System.out.println(Arrays.toString(m2));
//
//				int[] agreement = SymmetryUtil.getAgreement(m1,m2);
//				int size = 0;
//				for(int i : agreement) {
//					if(i != 0) size++;
//				}
//				int[] newAgr = new int[size];
//				int index = 0;
//				for(int i : agreement) {
//					if(i != 0) {
//						newAgr[index] = i;
//						index++;
//					}
//				}
//
//				agreement = newAgr;
//
//				System.out.println(Arrays.toString(agreement));
//				System.out.println();
//
//				//		ModelMapper gmapper = new ModelMapper(cl);
//				//		if(gmapper.canMap(m1,m2)) {
//				//			System.out.println(gmapper.getFoundPerm());
//				//			System.out.println(Arrays.toString(new int[]{}));
//				//			System.out.println();
//				//		}
//
//				AssignmentIter iter = new BFSAssignmentIter(agreement.length);
//				boolean agrOK = false;
//				boolean globOK = false;
//				boolean sym = false;
//				while(iter.hasNext()) {
//					int[] next = iter.next();
//					size = 0;
//					for(int i : next) {
//						if(i != 0) size++;
//					}
//
//
//					int[] realAssign = new int[size];
//					index = 0;
//
//					for(int i = 0; i < agreement.length; i++) {
//						if(next[i] != 0) {
//							realAssign[index] = agreement[i];
//							index++;
//						}
//					}
//
//					rep.setFilter(realAssign);
//					if(Arrays.equals(rep.getCanonicalInter(realAssign),realAssign)) {
//						ClauseList curCl = rep.getCurList(false);
//						ModelMapper mapper = new ModelMapper(curCl);
//						if(mapper.canMap(m1,m2)) {
//							sym = true;
//							if(Arrays.equals(realAssign,agreement)) agrOK = true;
//							if(Arrays.equals(realAssign,new int[]{})) globOK = true;
//							System.out.println(mapper.getFoundPerm());
//							System.out.println(Arrays.toString(realAssign));
//							System.out.println();
//						}
//					}
//				}
//
//				System.out.println(agrOK);
//				if(!agrOK && !globOK && sym) {
//					System.out.println(i1);
//					System.out.println(i2);
//					System.exit(0);
//				}
//			}
//		}


		//		CNFCreator creator = new IdentityCNFCreator("testcnf\\flat200-1.cnf");
		//		VariableContext context = new VariableContext();
		//		CNF function = creator.generateCNF(context);
		//		ISolver fullSolver = function.getSolverForCNFEnsureVariableUIDsMatch();
		//		ModelIterator iter = new ModelIterator(fullSolver);
		//				ArrayList<int[]> allModels = new ArrayList<int[]>();;
		//		
		//				while(iter.isSatisfiable()) {
		//					allModels.add(iter.model());
		//					System.out.println(allModels.size());
		//				}
		//				System.out.println(allModels.size());
		//		fullSolver.reset();
		//		fullSolver = null;
	}


}
