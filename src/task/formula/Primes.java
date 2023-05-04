package task.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.VariableContext;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;

public class Primes  implements ModelGiver, ConsoleDecodeable {
	private int max;

	public Primes(int max) {
		this.max = max;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		int maxFactor = max;

		boolean[] isPrime = new boolean[maxFactor-1]; //2 starts at 0

		int maxVars = 0; //current first free index

		Arrays.fill(isPrime,true);

		//Do sieve
		for(int k = 0; k < isPrime.length; k++) {
			int num = k+2;
			if(isPrime[k]) {
				for(int i = k + num; i < isPrime.length; i += num) {
					isPrime[i] = false;
				}
			}
		}
		//calc how much room we need
		maxVars = (int)(Math.log(max)/Math.log(2))+1; //Log base num

		context.ensureSize(maxVars);
		
		ArrayList<int[]> ret = new ArrayList<int[]>();

		for(int k = 0; k < isPrime.length; k++) {
			int num = k+2;
			if(isPrime[k]) {
				int[] model = new int[maxVars];
				String bin = Integer.toBinaryString(num);

				for(int i = 0; i < model.length; i++) {
					if(i < bin.length()) {
						if(bin.charAt(bin.length()-1-i) == '1') {
							model[i] = i+1;
						} else {
							model[i] = -(i+1);
						}
					} else {
						model[i] = -(i+1);
					}
				}
//				System.out.println(bin);
//				System.out.println(Arrays.toString(model));
//				System.out.println(consoleDecoding(model));
				ret.add(model);
			}
		}
		
		return ret;
	}
	
	public String consoleDecoding(int[] model) {
		int ret = 0;
		int pow = 1;
		
		for(int i : model) {
			if(i > 0) {
				ret += pow;
			}
			pow = pow<<1;
		}
		
		return ""+ret;
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
		return "Primes("+max+")";
	}

	@Override
	public String toString() {
		return "Primes("+max+")";
	}

}
