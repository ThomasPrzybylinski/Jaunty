package task.formula;

import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import formula.VariableContext;
import formula.simple.CNF;

public class ReducedLatinSquareCreator implements ConsoleDecodeable, CNFCreator {
	private int size;


	public ReducedLatinSquareCreator(int size) {
		super();
		this.size = size;
	}

	@Override
	public CNF generateCNF(VariableContext context) {
		CNF ret = new CNF(context);

		//fix first row and col
		for(int row = 0; row < size; row++) {
			ret.addClause(getVar(row,0,row));
		}

		//fix first row and col
		for(int col = 1; col < size; col++) {
			ret.addClause(getVar(0,col,col));
		}


		for(int k = 0; k < size; k++) {
			for(int row = 0; row < size; row++) {
				int[] onePerCol = new int[size];	
				for(int col = 0; col < size; col++) {
					onePerCol[col] = getVar(row,col,k);

					for(int col2 = col+1; col2 < size; col2++) {
						//only one per col
						ret.addClause(-getVar(row,col,k),-getVar(row,col2,k));
					}

					for(int row2 = row+1; row2 < size; row2++) {
						//Only one per row
						ret.addClause(-getVar(row,col,k),-getVar(row2,col,k));
					}

					for(int i = k+1; i < size; i++) {
						//One val per cell
						ret.addClause(-getVar(row,col,k),-getVar(row,col,i));
					}

					int[] onePerRow = new int[size];
					for(int row2 = 0; row2 < size; row2++) {
						//At least one per row
						onePerRow[row2] = getVar(row2,col,k); 
					}
					ret.addClause(onePerRow);

				}
				ret.addClause(onePerCol);
			}
		}

		return ret;
	}

	private int getVar(int row, int col, int val) {
		return row*size*size + col*size + val + 1;
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int k = 0; k < size; k++) {
			for(int i = 0; i < size; i++) {
				for(int j = 0; j < size; j++) {
					if(model[getVar(k,i,j)-1] > 0) {
						sb.append(j).append(' ');
					}
				}
			}
			sb.append(newline);
		}
		return sb.toString();
	}

}
