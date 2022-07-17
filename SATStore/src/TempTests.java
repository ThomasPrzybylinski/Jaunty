
import io.DimacsLoaderSaver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
import task.formula.AllFilledSquaresCNF;
import task.formula.AllSquaresCNF;
import task.formula.IdentityCNFCreator;
import task.formula.LineColoringCreator;
import task.formula.QueensToSAT;
import task.formula.random.CNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.formula.scheduling.EmorySchedule;
import task.symmetry.RealSymFinder;
import task.symmetry.sparse.SparseSymFinder;
import util.IntegralDisjointSet;
import util.formula.FormulaForAgreement;
import util.lit.LitsSet;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.LiteralPermutation;


public class TempTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VariableContext cntxt = new VariableContext();
		Variable a = cntxt.createNextDefaultVar();
		Variable b = cntxt.createNextDefaultVar();
		Variable c = cntxt.createNextDefaultVar();
		Variable d = cntxt.createNextDefaultVar();
		Variable e = cntxt.createNextDefaultVar();
		Variable f = cntxt.createNextDefaultVar();
		
		Disjunctions conj= new Disjunctions(
				new Conjunctions(a.getPosLit(),b.getPosLit()),
				new Conjunctions(c.getPosLit(),d.getPosLit()),
				new Conjunctions(e.getPosLit(),f.getPosLit())
				);
		System.out.println(conj.toCNF());
				
	}
}
