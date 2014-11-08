import io.DimacsLoaderSaver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import task.formula.AllFilledSquares;
import task.formula.LineColoringCreator;
import task.formula.random.CNFCreator;
import task.formula.random.SmallAllModelBoolFormula;
import task.formula.scheduling.EmorySchedule;
import task.symmetry.RealSymFinder;
import task.symmetry.sparse.SparseSymFinder;
import util.IntegralDisjointSet;
import workflow.CNFCreatorModelGiver;
import workflow.ModelGiver;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllLocalSymAddr;
import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;


public class TempTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CNFCreator creat = new EmorySchedule();//new LineColoringCreator(6,3);/
//		DimacsLoaderSaver.saveDimacsGraph(new PrintWriter("EmorySched.graph"),creat.generateCNF(VariableContext.defaultContext),"Emory Schedule for Bliss");
		
		ClauseList cl = creat.generateCNF(VariableContext.defaultContext);

		
//		RealSymFinder finder2 = new RealSymFinder(cl);
//		LiteralGroup group2 = finder2.getSymGroup();
//		System.out.println(group2);
//		
//		
//		System.out.println();
//		System.out.println("------------------");
//		System.out.println();
		
		long start = System.currentTimeMillis();
		SparseSymFinder finder = new SparseSymFinder(cl);
		LiteralGroup group = finder.getSymGroup();
		long end = System.currentTimeMillis();
		
		System.out.println(end-start);
		System.out.println(group);

	}



}
