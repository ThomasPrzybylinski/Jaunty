package task.formula;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import formula.VariableContext;

public class NumberFactors implements ModelGiver, ConsoleDecodeable {
	private int maxNum;
	private int[] maxIndex;
	private int modelSize;
	
	public NumberFactors(int maxNum) {
		this.maxNum = maxNum;
		
		int maxFactor = maxNum;
		
		maxIndex = new int[maxFactor-1]; //2 starts at 0
		
		int curInd = 0; //current first free index
		
		//Do sieve
		for(int k = 0; k < maxIndex.length; k++) {
			int num = k+2;
			if(maxIndex[k] != -1) {
				for(int i = k + num; i < maxIndex.length; i += num) {
					maxIndex[i] = -1;
				}
				//calc how much room we need
				curInd = curInd + Math.max(1,(int)(Math.log(maxNum)/Math.log(num))); //Log base num
				maxIndex[k] = curInd-1;
			}
			
			
		}
		modelSize = curInd;
	}
	
	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		List<int[]> ret = new ArrayList<int[]>(maxNum);
		
		for(int k = 2; k < maxNum; k++) {
			int[] curModel = new int[modelSize];
			
			int curNum = k;
			int curIndex = 0;
			for(int i = 0; i < maxIndex.length; i++) {
				if(curNum == 1) break;
				int divisor = i+2;
				if(maxIndex[i] == -1) continue;
				
				while(curNum%divisor == 0) {
					curModel[curIndex] = 1;
					curIndex++;
					curNum = curNum/divisor;
				}
				curIndex = maxIndex[i]+1;
			}
			
			if(getNumberFromModel(curModel) != k) {
				System.out.println("AH!");
			}
			ret.add(curModel);
		}
		
		return ret;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;
	}

	@Override
	public String getDirName() {
		return "Factor_"+maxNum;
	}

	@Override
	public String consoleDecoding(int[] model) {
		int ret = getNumberFromModel(model);
		
		return ""+ret;
	}

	private int getNumberFromModel(int[] model) {
		int curIndex = 0;
		int ret = 1;
		for(int i = 0; i < maxIndex.length; i++) {
			int curMax = maxIndex[i];
			if(curMax == -1) continue;
			int curNum = i+2;
			
			for(;curIndex <= curMax; curIndex++) {
				if(model[curIndex] == 1) {
					ret = ret*curNum;
				}
			}
		}
		return ret;
	}

	
}
