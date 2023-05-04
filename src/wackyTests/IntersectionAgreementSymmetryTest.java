package wackyTests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import task.formula.random.CNFCreator;
import task.formula.random.SimpleCNFCreator;
import task.symmetry.SimpleSymFinder;
import task.symmetry.SymmetryUtil;
import util.DisjointSet;
import formula.VariableContext;
import formula.simple.CNF;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;

public class IntersectionAgreementSymmetryTest {
	
	public static void main(String[] args) throws Exception {
		CNFCreator creat = new SimpleCNFCreator(10,4.3,3);
		CNF cnf = creat.generateCNF(VariableContext.defaultContext);
		List<int[]> clauses = cnf.getClauses();
		
		PossiblyDenseGraph<int[]> pdg = new PossiblyDenseGraph<int[]>(clauses);
		
		for(int k = 0; k < clauses.size(); k++) {
			for(int i = k+1; i < clauses.size(); i++) {
				
				boolean doAgree = areClausesAgreeSym(cnf, clauses, k, i);
				
				if(doAgree) {
					pdg.setAdjacent(k,i);
					
				} 
			}
		}
		
		for(int k = 0; k < clauses.size(); k++) {
			for(int i = 0; i < clauses.size(); i++) {
				if(pdg.areAdjacent(k,i)) {
					System.out.print("1 ");
				} else {
					System.out.print("0 ");
				}
			}
			System.out.println();
		}
		
		System.out.println();
		
		int[][] dist = getDistances(pdg,cnf.getContext());
		for(int k = 0; k < clauses.size(); k++) {
			for(int i = 0; i < clauses.size(); i++) {
				if(dist[k][i] < Integer.MAX_VALUE) {
					System.out.print(dist[k][i] +" ");
				} else {
					System.out.print("0 ");
				}
			}
			System.out.println();
		}
		
		
		
		boolean[] visited = new boolean[clauses.size()];
		Queue<Integer> toVisit = new LinkedList<Integer>();
		toVisit.add(0);
		visited[0] = true;
		
		while(!toVisit.isEmpty()) {
			int cur = toVisit.poll();
			
			for(int i = 0; i < visited.length; i++) {
				if(pdg.areAdjacent(cur,i) && !visited[i]) {
					toVisit.add(i);
					visited[i] = true;
				}
			}
		}
		boolean connected = true;
		
		for(boolean b : visited) {
			if(!b) {
				connected = false;
				break;
			}
		}
		
		System.out.println(connected);
		System.out.println(cnf.getSolverForCNF().isSatisfiable());
		
		
	}

	private static boolean areClausesAgreeSym(ClauseList cnf, List<int[]> clauses, int k,
			int i) {
		boolean doAgree = false;;
		//get if clauses are syntactically agreement symmetric
		int[] agree = getIntersection(clauses.get(k),clauses.get(i));
		
		if(agree == null) return false; //If they don't agree, can't be symmetric
										//Proper interpretation may be global symmetry...
		
		HashSet<Integer> vars = new HashSet<Integer>();
		for(int lit : agree) {
			vars.add(lit);
		}
		
		CNF other = new CNF(cnf.getContext());
		//Indecies of clause k and i in the new formula
		int[] kClause = null;
		int[] iClause = null;
		for(int j = 0; j < clauses.size(); j++) {
			int[] agreement = getAgreement(vars,clauses.get(j));
			
			if(j == k) {
				kClause = agreement;
			}
			
			if(j == i) {
				iClause = agreement;
			}
			
			if(agreement != null) {
				other.addClause(agreement);
			}
		}
		
		if(kClause.equals(iClause)) return true; //Since identity is always a symmetry
		
		other = other.reduce();
		
		int kIndex  = -1;
		int iIndex = -1;
		for(int j = 0; j < other.getClauses().size(); j++) {
			int[] clause = other.getClauses().get(j);
			
			if(Arrays.equals(clause,kClause)) {
				kIndex = j;
			} else if(Arrays.equals(clause,iClause)) {
				iIndex = j;
			}
		}
		
		if(other.getClauses().size() == 2) {
			doAgree = true;
		} else {
			ClauseList inverse = SymmetryUtil.getInverseList(other);
			SimpleSymFinder finder = new SimpleSymFinder(inverse);
			DisjointSet<Integer> sd = finder.getSymOrbits(null,new int[]{kIndex+1,iIndex+1});
			if(sd.sameSet(kIndex+1,iIndex+1)) {
				doAgree = true;
			}

		}
		
		return doAgree;
	}
	
	public static int[] getAgreement(HashSet<Integer> lits, int[] cl) {
		int num = 0;
		int[] clause = new int[cl.length];

		for(int k = 0; k < cl.length; k++) {
			int lit = cl[k];
			if(lits.contains(-lit)) return null;

			if(!lits.contains(lit)) {
				clause[num] = lit;
				num++;
			}
		}


		int[] ret = new int[num];
		for(int k = 0; k < num; k++) {
			ret[k] = clause[k];
		}

		return ret;
	}
	
	public static int[] getIntersection(int[] clause1, int[] clause2) {
		int num = 0;
		int[] clause = new int[clause1.length + clause2.length];
		int c1Index = 0;
		int c2Index = 0;

		while(c1Index < clause1.length || c2Index < clause2.length) {
			int c1 = c1Index < clause1.length ? clause1[c1Index] : Integer.MAX_VALUE;
			int c2 = c2Index < clause2.length ? clause2[c2Index] : Integer.MAX_VALUE;

			int absDiff = Math.abs(c1)-Math.abs(c2);

			if(absDiff == 0) {
				if(c1 == c2) {
					clause[num] = c1;
					num++;
				}
				c1Index++;
				c2Index++;
			} else if(absDiff < 0 || c2Index == clause2.length) {
				clause[num] = c1;
				num++;
				c1Index++;
			} else {
				clause[num] = c2;
				num++;
				c2Index++;
			}
		}

		int[] ret = new int[num];
		for(int k = 0; k < num; k++) {
			ret[k] = clause[k];
		}
		
		if(ret.length == 0) return null;

		return ret;
	}
	
	public static int[][] getDistances(PossiblyDenseGraph<?> pdg, VariableContext context) {
//		for(boolean[] row : graph) {
//			System.out.println(Arrays.toString(row));
//		}

		boolean[] visited = new boolean[pdg.getNumNodes()];
		int[][] length = new int[pdg.getNumNodes()][pdg.getNumNodes()];
		
		int curRow = 0;
		for(int[] row : length) {
			Arrays.fill(row,Integer.MAX_VALUE);
			row[curRow] = 1;
			curRow++;
		}

		for(int k = 0; k < visited.length; k++) {
			Arrays.fill(visited,false);
			Queue<int[]> modelQueue = new LinkedList<int[]>();
			modelQueue.add(new int[]{k,0});
			visited[k] = true;

			while(!modelQueue.isEmpty()) {
				int[] pair = modelQueue.poll();
				int mod = pair[0];
				int len = pair[1];

				int val = len;//Math.min(len,length[k][mod]);
				length[mod][k] = val;
				length[k][mod] = val;
				

				for(int i = 0; i < pdg.getNumNodes(); i++) {
					if(pdg.areAdjacent(k,i) && !visited[i]) {
						modelQueue.add(new int[]{i,len+1});
						visited[i] = true;
					}
				}
			}
		}

		return length;
	}

}
