package task.formula.plan;

import java.util.Arrays;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;

public class BlocksWorldDeconstruct implements CNFCreator, ConsoleDecodeable
//		FileDecodable
		{
	int[][] config;
	int numBlocks;
	int numAlone;
	int steps;
	int[][] isAlone;
	int[][] isBlocked;
	
	
	public BlocksWorldDeconstruct(int[][] startConfig) {
		config=startConfig.clone();
		numBlocks=0;
		numAlone=0;
		for(int k = 0; k < config.length;k++) {
			for(int i = 0; i < config[k].length;i++) {
				config[k][i]=numBlocks;
				numBlocks++;
			}
			if(config[k].length==1) {
				numAlone++;
			}
		}
		
		steps=numBlocks-startConfig.length;
	}
	
	@Override
	public CNF generateCNF(VariableContext context) {
		context.ensureSize(numBlocks*(steps+1)*2);
		
		CNF ret = new CNF(context);
		
		isAlone = new int[steps+1][numBlocks];
		isBlocked = new int[steps+1][numBlocks];
		
		int curVar=1;
		for(int k = 0; k < isBlocked.length; k++) {
			for(int i = 0; i < isBlocked[k].length; i++) {
				context.getVar(curVar).setName("A"+i+"S"+k);
				isAlone[k][i]=curVar;
				curVar++;
				context.getVar(curVar).setName("B"+i+"S"+k);
				isBlocked[k][i]=curVar;
				curVar++;
			}
		}
		
		int[] beneath = new int[numBlocks];
		int[] above = new int[numBlocks];
		Arrays.fill(beneath,-1);
		Arrays.fill(above,-1);
		//Initial Setup
		for(int k = 0; k < config.length;k++) {
			if(config[k].length!=1) {
				for(int i = 0; i < config[k].length; i++) {
					ret.addClause(-isAlone[0][config[k][i]]);
					
					if(i > 0) {
						beneath[config[k][i]]=config[k][i-1];
						above[config[k][i-1]]=config[k][i];
						ret.addClause(isBlocked[0][config[k][i]]);
					} else {
						ret.addClause(-isBlocked[0][config[k][i]]);
					}
				}
			} else {
				ret.addClause(isAlone[0][config[k][0]]);
				ret.addClause(-isBlocked[0][config[k][0]]);
			}
		}
		
		//Do The Steps
		for(int s = 1; s <= steps; s++) {
			for(int k = 0; k < numBlocks; k++) {
				//Exactly one that is not alone at s-1 must become alone at s
				//At most one
				//If less than one at some step, cannot be satisfiable because we have min steps
				for(int i = k+1; i < numBlocks; i++) {
					//Two alone blocks implies one was alone before
					//Unless it is a bottom block
					if(beneath[k] != i && beneath[i] != k) {
						ret.addClause(-isAlone[s-1][k],-isAlone[s-1][i],isAlone[s][k],isAlone[s][i]);
						ret.addClause(-isAlone[s][k],-isAlone[s][i],isAlone[s-1][k],isAlone[s-1][i]);
					}
				}
				
				//A block alone stays alone
				ret.addClause(-isAlone[s-1][k],isAlone[s][k]);
				//An unblocked block stays unblocked (cannot be stacked up)
				ret.addClause(isBlocked[s-1][k],-isBlocked[s][k]);
				//A block that is blocked cannot become alone
				//Unless it is a bottom block
				if(above[k] != -1) {
					ret.addClause(-isBlocked[s-1][k],-isAlone[s][k]);
				}
				//Just for consistency, all alone blocks are unblocked
				ret.addClause(-isAlone[s][k],-isBlocked[s][k]);
				
				
				//A block below another block becomes unblocked if and only if
				//the block above it becomes alone
				if(beneath[k] != -1) {
					//k is alone means that the block below is unblocked
					ret.addClause(-isAlone[s][beneath[k]],-isBlocked[s][k]);
					//k is not alone means that the block below is blocked
					ret.addClause(isAlone[s][beneath[k]],isBlocked[s][k]);
				}
				
				//A bottom block is alone iff the block above it is alone
				if(above[k] == -1 && beneath[k] != -1) {
					ret.addClause(isAlone[s][beneath[k]],-isAlone[s][k]);
					ret.addClause(-isAlone[s][beneath[k]],isAlone[s][k]);
				}
			}
		}
		//In the end, all blocks must be alone
		for(int k = 0; k < numBlocks; k++) {
			ret.addClause(isAlone[steps][k]);
		}
		
		System.out.println(ret.trySubsumption());
		return ret;
	}

	
//	@Override
//	public void fileDecoding(File dir, String filePrefix, int[] model)
//			throws IOException {
//		sched.fileDecoding(dir,filePrefix,model);
//
//	}
//
//	@Override
//	public void fileDecoding(String filePrefix, int[] model) throws IOException {
//		sched.fileDecoding(filePrefix,model);
//
//	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuffer out = new StringBuffer();
		out.append("Initial:").append(newline);
		for(int k = 0; k < config.length; k++) {
			for(int i = 0; i < config[k].length; i++) {
				out.append(config[k][i]);
			}
			out.append(newline);
		}
		
//		out.append("Alone:").append(newline);
//		for(int i = 0; i < isAlone[0].length; i++) {
//			if(model[isAlone[0][i]-1]>0) {
//				out.append(i).append(',');
//			}
//		}
//		out.append(newline);
//		
//		out.append("Blocked:").append(newline);
//		for(int i = 0; i < isAlone[0].length; i++) {
//			if(model[isBlocked[0][i]-1]>0) {
//				out.append(i).append(',');
//			}
//		}
//		out.append(newline);
		
		
		out.append(newline);
		for(int k = 1; k <= steps; k++) {
//			out.append("Step ").append(k).append(newline);
			
			
			for(int i = 0; i < isAlone[k].length; i++) {
				if(model[isAlone[k-1][i]-1] < 0 && model[isAlone[k][i]-1] > 0) {
					out.append(i).append(newline);
				}
			}
//			for(int j = 0; j < config.length; j++) {
//				for(int i = 0; i < config[j].length; i++) {
//					if(model[isAlone[k][config[j][i]]-1] < 0) {
//						out.append(config[j][i]);
//					}
//				}
//				out.append(newline);
//			}
			
//			out.append("Alone:").append(newline);
//			for(int i = 0; i < isAlone[k].length; i++) {
//				if(model[isAlone[k][i]-1]>0) {
//					out.append(i).append(',');
//				}
//			}
//			out.append(newline);
//			
//			out.append("Blocked:").append(newline);
//			for(int i = 0; i < isAlone[k].length; i++) {
//				if(model[isBlocked[k][i]-1]>0) {
//					out.append(i).append(',');
//				}
//			}
//			out.append(newline);
			
			
//			for(int i = 0; i < isAlone[k].length; i++) {
//				if(model[isAlone[k-1][i]-1] < 0 && model[isAlone[k][i]-1] > 0) {
//					out.append("Newly Alone:").append(i).append(newline);
//				}
//			}
//			out.append(newline);
			out.append(newline);
		}
		return out.toString();
	}

	public String toString() {
		String rep=Arrays.deepToString(config).replace(",","_");
		rep=rep.replace("[","");
		rep=rep.replace("]","-");
		rep=rep.replace(" ","");
		return "BlockDeconstruct-"+rep;
	}

}
