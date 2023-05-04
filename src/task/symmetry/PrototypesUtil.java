package task.symmetry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import task.clustering.SimpleDBScan;
import task.symmetry.SymmetryUtil;
import formula.VariableContext;
import formula.simple.ClauseList;

public class PrototypesUtil {

	public static void clusterAndDisplay(List<int[]> models) {
		SimpleDBScan scan = new SimpleDBScan(1);

		List<Set<int[]>> clusters = scan.getClustering(models);
		Comparator<int[]> comp = SymmetryUtil.LEX_LEADER_COMP;

		for(Set<int[]> curClust: clusters) {
			int[] curModel = null;
			for(int[] model : curClust) {
				if(curModel == null || comp.compare(curModel,model) < 0) {
					curModel = model;
				}
			}

			System.out.println(Arrays.toString(curModel));
			System.out.println(curClust.size());
		}
	}

	public static int[][] doAgreementSym(ClauseList models) {
		return doAgreementSym(models.getClauses(),models.getContext());
		
	}
	
	public static int[][] doAgreementSym(List<int[]> models, VariableContext context) {
		boolean[][] graph = SymmetryUtil.getSymmetryGraph(context,models);

//		for(boolean[] row : graph) {
//			System.out.println(Arrays.toString(row));
//		}

		boolean[] visited = new boolean[models.size()];
		int[][] length = new int[models.size()][models.size()];
		
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
				

				boolean[] neighbors = graph[mod];
				for(int i = 0; i < neighbors.length; i++) {
					if(neighbors[i] && !visited[i]) {
						modelQueue.add(new int[]{i,len+1});
						visited[i] = true;
					}
				}
			}
		}

//		for(int[] row : length) {
//			System.out.println(Arrays.toString(row));
//		}
		
		return length;
	}
	


}
