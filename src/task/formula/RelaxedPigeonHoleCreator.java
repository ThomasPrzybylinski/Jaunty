package task.formula;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;

//The pigeon hole problem, except more than 1 pigeon can be in each hole 
public class RelaxedPigeonHoleCreator implements CNFCreator, ConsoleDecodeable {
	private int pigeons;
	private int holes;
	
	public RelaxedPigeonHoleCreator(int pigeons, int holdes) {
		super();
		this.pigeons = pigeons;
		this.holes = holdes;
	}

	public static CNF createPigeonHole(int numPigeons, int numHoles) {
		CNF ret = new CNF(new VariableContext());
		
		for(int k = 0; k < numPigeons; k++) {
			int initialVar = (k)*(numHoles)+1;

			int[] pigeonInOneHole = new int[numHoles];
			
			//A pigeon must be in one hole
			for(int i = 0; i < pigeonInOneHole.length; i++) {
				pigeonInOneHole[i] = initialVar+i;

				//A pigeon cannot be in more than 1 hole at once
				for(int l = i+1; l < numHoles; l++) {
					ret.addClause(-(initialVar+i),-(initialVar+l));
				}
			}
			
			ret.addClause(pigeonInOneHole);
		
		}
		
		return ret;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		return createPigeonHole(pigeons,holes);
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < holes; k++) {
			sb.append('H').append(k).append(':');
			for(int i = 0; i < pigeons; i++) {
				int var = (i)*(holes)+k;
				if(model[var] > 0) {
					sb.append("P").append(i).append(' ');
				}
			}
			sb.append(ConsoleDecodeable.newline);
		}
		return sb.toString();
	}

}
