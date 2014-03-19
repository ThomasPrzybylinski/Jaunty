package task.formula;

import java.util.ArrayList;
import java.util.List;

import task.translate.ConsoleDecodeable;
import formula.VariableContext;
import formula.simple.CNF;

public class NAssymRooks implements ConsoleDecodeable {
	private int boardSize;
	private static int[][] template = new int[][]
			{
		{0,1,1},
		{0,0,1},
		{1,1,1}
			};
	private static int numBoards = 0;
	private static int[][] bNum;

	static {
		bNum = new int[template.length][template[0].length];

		for(int k = template.length - 1; k >= 0; k--) {
			for(int i = 0; i < template[k].length; i++) {
				if(template[k][i] == 1) {
					bNum[k][i] = numBoards;
					numBoards++;
				}
			}
		}
	}

	public NAssymRooks(int boardSize) {
		this.boardSize = boardSize;
	}

	//x,y from bottom left
	//Var increases by 1 as goes up 1 row
	private int getVar(int row, int column) {
		int bRow = (template.length-1)-row/boardSize;
		int bCol = column/boardSize;

		if(template[bRow][bCol] == 0) {
			return -1;
		} else {
			int boardNum = bNum[bRow][bCol];
			return getVar(row, column, boardNum);
		}
	}

	private int getVar(int row, int column, int boardNum) {
		int localRow = row%boardSize;
		int localCol = column%boardSize;
		int localVar = (localRow + localCol*boardSize)+1;
		
		return (boardNum*boardSize*boardSize)+localVar;
	}

	//	private int getBoard(int var) {
	//		return var/boardSize;
	//	}
	//	
	//	private int getRow(int var) {
	//		int board = getBoard(var);
	//		int localVar = var-(boardSize*boardSize*board);
	//		return localVar
	//	}
	//	
	//	private int getBoardNum(int bRow, int bCol) {
	//		
	//	}

	int[] toArray(List<Integer> tempC) {
		int[] ret = new int[tempC.size()];

		for(int k = 0; k < tempC.size(); k++) {
			ret[k] = tempC.get(k);
		}
		return ret;
	}

	public CNF encode(VariableContext context) {
		CNF ret = new CNF(context);

		for(int i = 0; i <= 6*boardSize*boardSize; i++) {
			if(i > context.getNumVarsMade()) {
				context.createNextDefaultVar();
			}
		}

		// one rook in each column
		for (int i = 0; i < boardSize*template.length; i++) { //foreach row
			List<Integer> tempC = new ArrayList<Integer>(boardSize*template.length);
			for(int k = 0; k < boardSize*template[0].length; k++) { //foreach column
				if(getVar(i,k) != -1) {
					tempC.add(getVar(i,k));
				}

				for(int j = k+1; j < boardSize*template[0].length; j++) { //foreach column
					if(getVar(i,k) != -1 && getVar(i,j) != -1) {
						ret.addClause(-getVar(i,k),-getVar(i,j));
					} else {
						break;
					}
				}
			}
			ret.addClause(toArray(tempC));
		}

		for(int k = 0; k < boardSize*template[0].length; k++) { //foreach column
			for (int i = 0; i < boardSize*template.length; i++) { //foreach row
				for (int j = i+1; j < boardSize*template.length; j++) { //foreach row
					if(getVar(i,k) != -1 && getVar(j,k) != -1) {
						ret.addClause(-getVar(i,k),-getVar(j,k));
					} else {
						break;
					}
				}
			}
		}
		return ret;
	}


	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();

		for (int i = boardSize*template.length-1; i >= 0 ; i--) { //foreach row
			List<Integer> tempC = new ArrayList<Integer>(boardSize*template.length);
			for(int k = 0; k < boardSize*template[0].length; k++) { //foreach column
				int var = getVar(i,k);
				if(var == -1) {
					sb.append(' ');
				} else {
					sb.append(model[var-1] > 0 ? 'X' : '*');
				}
			}
			sb.append(newline);
		}
		return sb.toString();

	}


}
