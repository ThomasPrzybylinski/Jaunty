
import io.DimacsLoaderSaver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import task.formula.AllFilledRectangles;
import task.formula.AllFilledSquares;
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
		CNFCreator creat = new AllSquaresCNF(10);//new EmorySchedule();//new QueensToSAT(5);//new LineColoringCreator(6,3);// new IdentityCNFCreator("D:\\Downloads\\Linux\\SAT09\\APPLICATIONS\\diagnosis\\UR-10-5p1.cnf");//
//		DimacsLoaderSaver.saveDimacsGraph(new PrintWriter("EmorySched.graph"),creat.generateCNF(VariableContext.defaultContext),"Emory Schedule for Bliss");
		ClauseList cl = creat.generateCNF(VariableContext.defaultContext);

//		List<int[]> models = (new AllFilledRectangles(3)).getAllModels(VariableContext.defaultContext);//new CNFCreatorModelGiver(new QueensToSAT(7)).getAllModels(VariableContext.defaultContext); //
		
//		ClauseList cl = new ClauseList(VariableContext.defaultContext);
//		cl.addAll(models);
		
		RealSymFinder finder2 = new RealSymFinder(cl);
		LiteralGroup group2 = finder2.getSymGroup();
		System.out.println(group2);
		
		
		System.out.println();
		System.out.println("------------------");
		System.out.println();
		
		long start = System.currentTimeMillis();
//		cl = (new FormulaForAgreement(cl)).unitPropagate().trySubsumption().squeezed();
		SparseSymFinder finder = new SparseSymFinder(cl);
		LiteralGroup group = finder.getSymGroup();
		long end = System.currentTimeMillis();
		
		System.out.println();
		System.out.println(group);
		System.out.println("Miilis: " + (end-start));
		System.out.println("Order:  " + finder.getGroupOrder());
		System.out.println("Gens:   " + finder.getGensFound());
	}
}
