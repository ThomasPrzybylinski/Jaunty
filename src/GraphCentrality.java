import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.StatUtils;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import task.formula.Primes;
import workflow.ModelGiver;
import workflow.graph.EdgeManipulator;
import workflow.graph.local.AllLocalSymAddr;


public class GraphCentrality {
	public static void main(String args[]) throws Exception{
		ModelGiver giver = new Primes(4096); // new AllFilledSquares(5);//new AllRectangles(4);//new CNFCreatorModelGiver(new LineColoringCreator(6,3)); //new CNFCreatorModelGiver(new QueensToSAT(8));//
		List<int[]> models = giver.getAllModels(VariableContext.defaultContext);
		ClauseList cl = new ClauseList(VariableContext.defaultContext);
		cl.fastAddAll(models);
		
		PossiblyDenseGraph<int[]> pdg = new PossiblyDenseGraph<int[]>(models);
		
		EdgeManipulator addr = new AllLocalSymAddr(false,false,false,true);//new AgreementLocalSymAdder();//
		
		addr.addEdges(pdg, cl);
		
		Array2DRowRealMatrix mat = new Array2DRowRealMatrix(pdg.getNumNodes(),pdg.getNumNodes());
		
		for(int k = 0; k < pdg.getNumNodes(); k++) {
			for(int i = 0; i < pdg.getNumNodes(); i++) {
				if(pdg.areAdjacent(k,i)) {
					mat.setEntry(k,i,1);
				}
			}
		}
		
		double[] vecData = new double[pdg.getNumNodes()];
		Arrays.fill(vecData,1);
		RealVector vec = new ArrayRealVector(vecData);
		vec = vec.mapDivideToSelf(vec.getNorm());
		
		for(int k = 0; k < 1000; k++) {
			vec = mat.operate(vec);
			vec = vec.mapDivideToSelf(vec.getNorm());
			System.out.println(vec);
		}
		System.out.println(vec);
		
		vecData = vec.getData();
		vecData = StatUtils.normalize(vecData);
		double max = StatUtils.max(vecData);
		double threshold = max-.01;
		System.out.println(Arrays.toString(vecData));
		System.out.println("Ordinary Solutions");
		for(int k = 0; k < vecData.length; k++) {
			if(vecData[k] >= threshold) {
				System.out.println(k + ": " + vecData[k]);
			}
		}
		
		double min = StatUtils.min(vecData);
		threshold = min+.01;
		System.out.println("Unique Solutions");
		for(int k = 0; k < vecData.length; k++) {
			if(vecData[k] <= threshold) {
				System.out.println(k + ": " + vecData[k]);
			}
		}
		
	}
}
