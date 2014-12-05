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

import task.formula.AllSquaresCNF;
import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.formula.scheduling.EmorySchedule;
import task.sat.SATUtil;
import task.symmetry.LeftCosetSmallerIsomorphFinder;
import task.symmetry.ModelMapper;
import task.symmetry.RealSymFinder;
import task.symmetry.SHATTERSymFinder;
import task.symmetry.SymmetryUtil;
import task.symmetry.local.LocalSymClauses;
import task.symmetry.sparse.SparseSymFinder;
import util.lit.DirectedLitGraph;
import util.lit.LitSorter;
import util.lit.LitsMap;
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
		CNFCreator creat = new AllSquaresCNF(5);//new EmorySchedule();//new QueensToSAT(5);//new LineColoringCreator(6,3);// new IdentityCNFCreator("D:\\Downloads\\Linux\\SAT09\\APPLICATIONS\\diagnosis\\UR-10-5p1.cnf");//
		ClauseList cl = creat.generateCNF(VariableContext.defaultContext);

		LitsMap<Object> lm = new LitsMap<Object>(cl.getContext().size());
		for(int[] i : cl.getClauses()) {
			lm.put(i,null);
		}

		Random rand = new Random();
		for(int[] i : cl.getClauses()) {
			int randInd = rand.nextInt(i.length);
			i[randInd] = -i[randInd];
			if(!lm.contains(i)) {
				System.out.println(Arrays.toString(i));
			}
		}

	}


}
