import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import formula.VariableContext;
import formula.simple.ClauseList;
import graph.PossiblyDenseGraph;
import io.GraphIO;
import task.clustering.SimpleDifference;
import task.formula.coordinates.CSVLoader;
import task.formula.coordinates.CoordsToBinary;
import task.formula.coordinates.GaussCoords;
import workflow.ModelGiver;
import workflow.eclectic.EclecSetCoverCreator;
import workflow.eclectic.KNNClosenessFinder;
import workflow.eclectic.OppositeOfIndependentSetCreator;
import workflow.graph.CompoundEdgeManipulator;
import workflow.graph.DistanceEdges;
import workflow.graph.EdgeManipulator;
import workflow.graph.GlobalSymmetryEdges;
import workflow.graph.MinimalDistanceEdges;
import workflow.graph.local.AgreementConstructionAdder;
import workflow.graph.local.AgreementLocalSymAdder;
import workflow.graph.local.AllChoiceConstructionSymAddr;
import workflow.graph.local.ConstructionSymAddr;
import workflow.graph.local.LimitedConstructionSymAddr;
import workflow.graph.local.NotImpliedChoices;
import workflow.graph.local.PositiveChoices;


public class SeeGraph {
	public static void main(String[] args) throws Exception {
//		CoordsToBinary mg =  new CoordsToBinary(new GaussCoords());
//		ClauseList models = new ClauseList(VariableContext.defaultContext);
//		models.addAll(mg.getAllModels(VariableContext.defaultContext));
		
		CSVLoader load = new CSVLoader("iris.data",4);
		ClauseList models = new ClauseList(VariableContext.defaultContext);
		models.addAll(load.getAllModels(VariableContext.defaultContext));
		
		
		EdgeManipulator e1 = 
//				new AllChoiceConstructionSymAddr(false,false,false,true, new PositiveChoices());
//				new LimitedConstructionSymAddr(false,false,false,true,3);//
//				new AgreementConstructionAdder(true);
//				new GlobalSymmetryEdges();
				new MinimalDistanceEdges(); 
//				new AgreementLocalSymAdder(); 
				//new AllChoiceConstructionSymAddr(false,false,false,true, new NotImpliedChoices());
				//new ConstructionSymAddr(false,false,false,true);//new AgreementLocalSymAdder();
					//new AllChoiceConstructionSymAddr(false,false,false,true, new PositiveChoices());
					////
//				new CompoundEdgeManipulator( new LimitedConstructionSymAddr(false,false,false,true,0),new MinimalDistanceEdges());
		EdgeManipulator e2 = new DistanceEdges(new SimpleDifference());
		
		PossiblyDenseGraph<int[]> g1 = new PossiblyDenseGraph<int[]>(models.getClauses());
		
		e1.addEdges(g1,models);
		
		System.out.println("SYM:"+getAvgSameResponseDeg(g1,load));
		System.out.println("SYM:"+getPrediction(g1,load));
		
		
		PossiblyDenseGraph<int[]> g2 = new PossiblyDenseGraph<int[]>(models.getClauses());
		
		e2.addEdges(g2,models);
		
		
		EclecSetCoverCreator ecelc = new OppositeOfIndependentSetCreator(new KNNClosenessFinder(4));

		PossiblyDenseGraph<int[]> closeGraph = new PossiblyDenseGraph<int[]>(g2.getObjs());
		for(int k = 0; k<closeGraph.getNumNodes(); k++) {
			for(int i = k+1; i < closeGraph.getNumNodes(); i++) {
				if(ecelc.verifyEclecticPair(g2,k,i)) {
					closeGraph.setAdjacent(k,i);
				}
			}
		}
		
		System.out.println("DIST:"+getAvgSameResponseDeg(closeGraph,load));
		System.out.println("DIST:"+getPrediction(closeGraph,load));
		
		File f = new File("GDFGraph.gdf");
		PrintWriter pw = new PrintWriter(f);
//		String graph = mg.getGDF(g1);
		String graph = load.getGDF(g1);
		pw.println(graph);
		pw.close();
		
		f = new File("GDFGraphDist.gdf");
		pw = new PrintWriter(f);
//		graph = mg.getGDF(closeGraph);
		graph = load.getGDF(closeGraph);
		pw.println(graph);
		pw.close();
		
	}

	//Stupid implementation but oh well
	private static double getAvgSameResponseDeg(PossiblyDenseGraph<int[]> g1, CSVLoader load) {
		int deg=0;
		int inDeg=0;
		for(int k = 0; k < g1.getNumNodes(); k++) {
			String resK=load.getResponse(k);
			for(int i = 0; i < g1.getNumNodes(); i++) {
				if(i==k)continue;
				
				if(g1.areAdjacent(k,i)) {
					deg++;
					if(resK.equals(load.getResponse(i))) {
						inDeg++;
					}
				}
			}
		}
		
		return inDeg/(double)deg;
	}
	
	private static double getPrediction(PossiblyDenseGraph<int[]> g1, CSVLoader load) {
		
		int correct = 0;
		
		for(int k = 0; k < g1.getNumNodes(); k++) {
			String resK=load.getResponse(k);
			HashMap<String,Integer> pred = new HashMap<String,Integer>();
			for(int i = 0; i < g1.getNumNodes(); i++) {
				if(i==k)continue;
				
				if(g1.areAdjacent(k,i)) {
					String resI=load.getResponse(k);
					if(pred.containsKey(resI)) {
						pred.put(resI,pred.get(resI)+1);
					} else {
						pred.put(resI,1);
					}
				}
			}
			
			int max = -1;
			String maxPred = "";
			for(Entry<String,Integer> entry : pred.entrySet()) {
				if(entry.getValue() > max) {
					max=entry.getValue();
					maxPred=entry.getKey();
				}
			}
			
			if(maxPred.equals(resK)) {
				correct++;
			}
					
		}
		
		return correct/(double)g1.getNumNodes();
	}
}
