import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import group.LiteralGroup;
import group.SchreierVector;
import io.GraphIO;
import task.formula.coordinates.CoordsToBinary;
import task.formula.coordinates.GaussCoords;
import task.symmetry.SymmetryUtil;
import task.symmetry.sparse.SparseSymFinder;
import util.ArrayIntersectionHelper;
import util.formula.FormulaForAgreement;
import util.lit.LitsMap;
import workflow.ModelGiver;

//Test for using Topological Data Analysis and Symmetry Similarity
public class TDATest {

	public static void main(String[] args) throws Exception {
		ModelGiver mg = new CoordsToBinary(new GaussCoords());//new CNFCreatorModelGiver(new QueensToSAT(7));//new AllSquares(4);//
		ClauseList models = new ClauseList(VariableContext.defaultContext);
		models.addAll(mg.getAllModels(VariableContext.defaultContext));

		PossiblyDenseGraph<int[]> pdg = getNeighborhoodGraph(models);
		File f = new File("TestGraph.csv");
		PrintWriter pw = new PrintWriter(f);
		pw.println(GraphIO.graphtoPrimativeCSV(pdg));
		pw.close();;

	}

	public static
	PossiblyDenseGraph<int[]> getNeighborhoodGraph(ClauseList orig) {
		List<int[]> representatives = orig.getClauses();
		LitsMap<Object> map = new LitsMap<Object>(orig.getContext().size());
		//		LocalSymClauses rep = new LocalSymClauses(orig);
		FormulaForAgreement rep = new FormulaForAgreement(orig);
		//		LocalSymClauses Rep = new LocalSymClauses(orig);

		LitsMap<Integer> clauseMap = new LitsMap<Integer>(orig.size());
		List<int[]> newNodes = new ArrayList<int[]>();

		{
			SparseSymFinder syms = new SparseSymFinder(orig);
			LiteralGroup group = syms.getSymGroup();
			FormulaForAgreement form = new FormulaForAgreement(orig);
			int[] exist = rep.getExistantClauses();
			LiteralGroup modelGroup = form.getModelGroup(group);
			SchreierVector vec = new SchreierVector(modelGroup);
			addNodesAndEdges(clauseMap, newNodes, vec, exist);
		}
		for(int k = 0; k < representatives.size(); k++) {
			int[] rep1 = representatives.get(k);
			for(int i = k+1; i < representatives.size(); i++) {
				int[] rep2 = representatives.get(i);
				//
				int[] agreement = SymmetryUtil.getAgreement(rep1,rep2);
				//
				if(map.contains(agreement)) {
					continue;
				} else {
					map.put(agreement,null);
				}

				ClauseList cl = rep.getCLFromModels(agreement);


				//				System.out.println(Arrays.toString(agreement));
				//				System.out.println(cl);
				//				System.out.println(cl2);
				//				System.out.println();

				//				RealSymFinder syms = new RealSymFinder(cl);

				SparseSymFinder syms = new SparseSymFinder(cl);
				LiteralGroup group = syms.getSymGroup();
				FormulaForAgreement form = new FormulaForAgreement(cl);
				int[] exist = rep.getExistantClauses();
				LiteralGroup modelGroup = form.getModelGroup(group).reduce();
				SchreierVector vec = new SchreierVector(modelGroup);



				addNodesAndEdges(clauseMap, newNodes, vec,exist);

			}
		}

		PossiblyDenseGraph<int[]> ret = new PossiblyDenseGraph<int[]>(newNodes);
		for(int k = 0; k < newNodes.size(); k++) {
			int[] rep1 = newNodes.get(k);
			for(int i = k+1; i < newNodes.size(); i++) {
				int[] rep2 = newNodes.get(i);

				if(ArrayIntersectionHelper.intersectSize(rep1,rep2) > 0) {
					ret.setAdjacent(k,i);			
				}
			}
		}

		return ret;
	}

	private static void addNodesAndEdges(LitsMap<Integer> clauseMap, List<int[]> newNodes, SchreierVector vec, int[] exist) {
		ArrayList<Integer> vecArr = new ArrayList<Integer>();
		boolean[] inOrbit = new boolean[vec.getNumVars()+1];

		for(int j = 1; j <= vec.getNumVars(); j++) {
			if(inOrbit[j]) continue;
			vecArr.clear();
			vecArr.add(exist[j-1]);
			for(int h = j+1; h <= vec.getNumVars(); h++) {
				if(!inOrbit[h] && vec.sameOrbit(j,h)) {
					inOrbit[h] = true;
					vecArr.add(exist[h-1]);
				}
			}

			if(vecArr.size() > 1) {
				int[] toAdd = new int[vecArr.size()];
				int index = 0;
				for(int clNum : vecArr) {
					toAdd[index]=clNum;
					index++;
				}

				if(!clauseMap.contains(toAdd)) {
					clauseMap.put(toAdd,newNodes.size());
					newNodes.add(toAdd);
				} 

			}
		}
	}

}
