package task.formula.coordinates;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import graph.PossiblyDenseGraph;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;

public class CSVLoader implements CoordinateGenerator, ModelGiver, ConsoleDecodeable {

	private String fileName;
	private int response;
	
	String[] responses;
	CoordSpace space;
	
	List<int[]> models;
	
	
	public CSVLoader(String file,int responseIndex) {
		this.response=responseIndex;
		this.fileName=file;
		
	}
	
	@Override
	public CoordSpace generateCoords() {
		try {
			CSVParser parse = CSVFormat.DEFAULT.parse(new FileReader(fileName));
			List<CSVRecord> records = parse.getRecords();
			
			responses = new String[records.size()];
			
			int nCol = 0;
			if(records.size() > 0) {
				nCol = records.get(0).size();
			} else {
				return null;
			}
			
			space = new CoordSpace(nCol-1);
			
			int recordNum=-1;
			for(CSVRecord record : records) {
				recordNum++;
				double[] coord = new double[nCol-1];
				for(int k = 0; k < record.size(); k++) {
					if(k==response) {
						responses[recordNum]=record.get(k);
					} else {
						double d = Double.parseDouble(record.get(k));
						coord[k > response ? k-1:k]=d;
					}
				}
				
				boolean dup = false;
				for(int k = 0; k < space.getPts().size(); k++) {
					if(Arrays.equals(coord,space.getPts().get(k))) {
						dup=true;
						break;
					}
				}
				
				if(!dup) {
					space.addPt(coord);
				}
			}
			
			
		} catch(IOException e) {
			return null;
		}
		return space;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context) throws TimeoutException {
		CoordsToBinary bin = new CoordsToBinary(this);
		models = bin.getAllModels(context);
		return models;
		
	}
	
	public String getResponse(int k) {
		return responses[k];
	}

	
	@Override
	public String consoleDecoding(int[] model) {
		for(int k = 0; k < models.size(); k++) {
			if(Arrays.equals(models.get(k),model)) {
				return responses[k];
			}
		}
		return null;
	}


	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDirName() {
		return fileName;
	}
	
	public String getGDF(PossiblyDenseGraph<int[]> graph) {
		StringBuilder sb = new StringBuilder();
		sb.append("nodedef>name VARCHAR,label VARCHAR,x DOUBLE,y DOUBLE");
		sb.append(ConsoleDecodeable.newline);
		for(int k = 0; k < graph.getObjs().size(); k++) {
			sb.append(k).append(",'").append(responses[k]).append("'");
			sb.append(","+space.getPts().get(k)[0]+","+space.getPts().get(k)[1]);
			sb.append(ConsoleDecodeable.newline);
		}
		
		sb.append("edgedef>node1 VARCHAR,node2 VARCHAR,weight DOUBLE");
		sb.append(ConsoleDecodeable.newline);
		
		for(int k = 0; k < graph.getNumNodes(); k++) {
			for(int i = k+1; i < graph.getNumNodes(); i++) {
				if(graph.areAdjacent(k,i)) {
//					sb.append(k+ "--" +i+";");
					sb.append(k+ "," +i+","+"10");
					sb.append(ConsoleDecodeable.newline);
				}
			}
		}
		return sb.substring(0,sb.length()-1);
	}

}
