package workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.sat4j.specs.TimeoutException;

import formula.Variable;
import formula.VariableContext;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.lit.ModelComparator;

public class AllPartInterpModelGiver implements ModelGiver,ConsoleDecodeable {
	private ModelGiver giver;
	private VariableContext context;

	public AllPartInterpModelGiver(ModelGiver mg) {
		this.giver = mg;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		this.context = context;
		List<int[]> origModels = giver.getAllModels(context.getCopy());

		int[] rep = origModels.get(0);

		for(int k = 0; k < rep.length; k++) {
			int var = k+1;
			context.getOrCreateVar(var+"P"); //Var is positive
			context.getOrCreateVar(var+"N"); //Var is negative
			context.getOrCreateVar(var+"E"); //Var does not exist
		}

		TreeSet<int[]> nextModels = new TreeSet<int[]>(new ModelComparator());
		LinkedList<int[]> toCompute = new LinkedList<int[]>();
		
		for(int[] mod : origModels) {
			int[] next = new int[context.size()];

			for(int i = 0; i < mod.length; i++) {
				int lit = mod[i];
				int var = Math.abs(lit);

				Variable pVar = context.getOrCreateVar(var+"P");
				Variable nVar = context.getOrCreateVar(var+"N");
				Variable eVar = context.getOrCreateVar(var+"E");

				if(lit > 0) {
					next[3*i] = pVar.getPosLit().getIntRep();
					next[3*i+1] = nVar.getNegLit().getIntRep();
					next[3*i+2] = eVar.getNegLit().getIntRep();
				} else {
					next[3*i] = pVar.getNegLit().getIntRep();
					next[3*i+1] = nVar.getPosLit().getIntRep();
					next[3*i+2] = eVar.getNegLit().getIntRep();
				}
			}
			nextModels.add(next);
			toCompute.add(next);
		}
		
		while(!toCompute.isEmpty()) {
			int[] mod = toCompute.poll();
			
			
			for(int i = 0; i < mod.length/3; i++) {
				if(mod[3*i] < 0 && mod[3*i+1] < 0) continue;
				
				int[] next = new int[mod.length];
				System.arraycopy(mod,0,next,0,mod.length);
				
				if(mod[3*i] > 0) {
					next[3*i] = -next[3*i];
					next[3*i+2] = -next[3*i+2]; 
				} else if(mod[3*i+1] > 0) {
					next[3*i+1] = -next[3*i+1];
					next[3*i+2] = -next[3*i+2]; 
				}
				
				if(nextModels.add(next)) {
					toCompute.add(next);
				}
			}
		}
		
		ArrayList<int[]> ret = new ArrayList<int[]>(nextModels.size());
		ret.addAll(nextModels);

		return ret;
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
		return "AllPartInter_"+giver.getDirName();
	}

	@Override
	public String consoleDecoding(int[] model) {
		if(context == null) {
			return Arrays.toString(model);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			
			for(int i : model) {
				int var = Math.abs(i);
				int actual = (i/3) + (i%3 > 0 ? 1 : 0);
				Variable v = context.getVar(var);
				
				if(i > 0) {
					if(i%3 == 2) {
						sb.append('-'); //Neg Var Type
					}
					
					if(i % 3 != 0) { //Does exist
						sb.append(actual);//v.getName());
						sb.append(' ');
					}
				}
				
				
			}
			sb.append(']');
			return sb.toString();
		}
	}

}
