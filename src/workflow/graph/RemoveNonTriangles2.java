package workflow.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import formula.Variable;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import util.IntPair;

public class RemoveNonTriangles2 extends EdgeManipulator {

	@Override
	public
	void addEdges(PossiblyDenseGraph<int[]> g, ClauseList orig) {
		
		VariableContext vc = new VariableContext();
		ArrayList<IntPair> varToPair = new ArrayList<IntPair>();
		varToPair.add(null);
		Map<IntPair,Integer> pairToVar = new TreeMap<IntPair,Integer>();

		CNF satProb = new CNF(vc);
		
		for(int k = 0; k < g.getNumNodes(); k++) {
			for(int i = 0; i < g.getNumNodes(); i++) {
				if(k==i || !g.areAdjacent(k,i)) continue;
				
				
				ArrayIntList maximalClause = new ArrayIntList();
				for(int triComplete = 0; (triComplete < g.getNumNodes()); triComplete++) {
					if(triComplete==i || triComplete==k) continue;
					
					boolean kAdj = g.areAdjacent(k,triComplete);
					boolean iAdj = g.areAdjacent(i,triComplete);
					
					if((kAdj && !iAdj) || (!kAdj && iAdj)) {
						int kiEdgeVar = ensureVar(new IntPair(k,i),varToPair,pairToVar,vc);
						int iTriEdgeVar = ensureVar(new IntPair(i,triComplete),varToPair,pairToVar,vc);
						satProb.fastAddClause(-kiEdgeVar,-iTriEdgeVar);
						
						if(maximalClause.size() == 0) {
							maximalClause.add(kiEdgeVar);
						}
						maximalClause.add(iTriEdgeVar);
					}
				}
				
				//If nothing is preventing an edge from being added, add the edge
				if(maximalClause.size() > 0) {
					satProb.fastAddClause(maximalClause.toArray());
				}
			}
		}

		satProb=satProb.reduce();
		
		System.out.println("Sat Triangle Prob: " + satProb);
		System.out.println("Num Edges        : " + vc.size());
		if(satProb.size() == 0) return; //No need to elimate anything!
		
		
		
		int[] maxModel = null;
	
		try {
	
			ISolver solver = satProb.getSolverForCNF();
			solver.setDBSimplificationAllowed(false);
			solver.setKeepSolverHot(true);
			//solver.setTimeout(2);
			
			int min = 0;
			int max = vc.size();
			
			int[] allVarsArray = new int[vc.size()-1];
			for(int k = 0; k < allVarsArray.length; k++) {
				allVarsArray[k] = k+1;
			}
			VecInt allVars = new VecInt(allVarsArray);
			
			
			System.out.println("Triangle  Models");
			/*for(int k = min; k <= max; k++) {
				IConstr constr = solver.addAtLeast(allVars,k);
				
				int[] curModel = solver.findModel();
				
				if(curModel == null) break;
				maxModel=curModel;
				
				System.out.println(Arrays.toString(maxModel));
				
				int posLits = 0;
				for(int i : maxModel) {
					if(i > 0) posLits++;
				}
				k = posLits;
				
				System.out.println(posLits);
		
				//solver.removeConstr(constr); //Maybe unnecessary since we're only increasing the constraint? Maybe learned clauses still exist
				
			}*/
			
			while(min <= max) {
				IConstr constr = solver.addAtLeast(allVars,(min+max)/2);
				
				int[] curModel = solver.findModel();
				System.out.println(Arrays.toString(curModel));
				
				if(curModel == null) {
					max = (min+max)/2-1;
					System.out.println("New Max: " + max);
				} else {
					maxModel = curModel;
					
					int posLits = 0;
					for(int i : curModel) {
						if(i > 0) posLits++;
					}
					
					min = posLits+1;
					System.out.println(max + ". " + min + ", " + posLits);
				}
				solver.removeConstr(constr);
			}
			System.out.println(max + ". " + min);
			System.out.println(Arrays.toString(maxModel));
			
		} catch (TimeoutException te) {
			System.err.println("Timeout in triangle eliminator!");
		} catch (ContradictionException ce) {
			//This should never happen
			System.err.println("Contradiction in triangle eliminator!");
		}
		
		for(int i : maxModel) {
			if(i < 0) {
				IntPair pairToDelete = varToPair.get(Math.abs(i));
				g.setAdjacent(pairToDelete.getI1(),pairToDelete.getI2(),false);
			}
		}

	}
	
	private int ensureVar(IntPair pair,ArrayList<IntPair> varToPair,Map<IntPair,Integer> pairToVar,VariableContext vc ) {
		if(pair.getI1() > pair.getI2()) {
			pair = new IntPair(pair.getI2(),pair.getI1());
		}
		
		Integer var = pairToVar.get(pair);
		if(var == null) {
			Variable v = vc.getOrCreateVar("["+pair.getI1()+","+pair.getI2()+"]");
			varToPair.add(pair);
			pairToVar.put(pair,v.getUID());
			var = v.getUID();
		}
		return var.intValue();
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}
	
	public String toString() {
		return "RemoveNonTriangles";
	}

}
