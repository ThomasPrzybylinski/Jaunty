package task.formula;
/*************************************************************************
 *  Compilation:  javac QueensToSAT.java
 *  Execution:    java QueensToSAT N [-d]
 *  
 *  An translate from N Queens to and from SAT.
   -- * -- * -- * -- * -- * -- * -- * -- * -- * -- * -- * -- * -- * -- 

 INSTRUCTIONS:

  - Typing
      java QueensToSat N 
    will generate the boolean satisfiability encoding of the 
    N Queens problem out to the terminal.

  - To redirect the output into a file called NQ.wff, type
      java QueensToSat N > NQ.wff

  - Now run your SAT solver on the file by typing
      java yourProgram < NQ.wff

    or

      java yourProgram NQ.wff

    if your program takes the name of the input file as a 
    commandline argument.

  - If you want to output the result of your program into a file, 
    say NQ.sol, type
      java yourProgram < NQ.wff > NQ.sol

  - To see the result nicely displayed, typed
      java QueensToSat N -d < NQ.sol

 *************************************************************************/

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import formula.VariableContext;
import formula.simple.CNF;


public class SpaceFillingMarkersToSAT //implements ConsoleDecodeable, FileDecodable {
{
	int N;  // NxN chess board

	public SpaceFillingMarkersToSAT(int size) {
		N = size;
	}

	private int toVar(int y, int x) {
		// encode (row,column) to a SAT proposition
		return (N-y-1)+1 + x*N;
	}

	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("Usage: QueenToSat N [-d]");
			System.exit(0);
		}

		SpaceFillingMarkersToSAT sater = new SpaceFillingMarkersToSAT(Integer.parseInt(args[0]));

		boolean decode = false;

		if (args.length == 2) 
			decode = (args[1].equals("-d") ? true : false);

		if (! decode) 
			sater.encode(VariableContext.defaultContext);
		//		else
		//			sater.decode();

	}

	public CNF encode(VariableContext context) {

		CNF ret = new CNF(context);
		
		for(int k = 0; k < N; k++) {
			for(int i = 0; i < N; i++) {
				int var = toVar(k,i);
				int[] spaceVars = getSpaceAround(k,i);
				
				//var can only exist if its space is empty
				for(int j = 0; j < spaceVars.length; j++) {
					ret.addClause(-var,-spaceVars[j]);
				}

				
				//If its space is empty, a var must exist
				int[] toAdd = new int[spaceVars.length+1];
				toAdd[0] = var;
				for(int j = 0; j < spaceVars.length; j++) {
					toAdd[j+1] = spaceVars[j];
				}
				ret.addClause(toAdd);
			}
		}

		return ret;
	}

	private int[] getSpaceAround(int row, int col) {
		int rowSize = Math.abs(row-(N/2));
		int colSize = Math.abs(col-(N/2)+1);
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
		for(int k = -rowSize; k <= rowSize; k++) {
			for(int j = -colSize; j <= colSize; j++) {
				if((row+k >= 0 && row+k < N)
						&& (col+j >= 0 && col+j < N)
						&& (k != 0 || j != 0)
						&& (k == 0 || j == 0) ) { //|| Math.abs(j) == Math.abs(k))) {
					temp.add(toVar(row+k,col+j));
				}
			}
		}
	
		int[] ret = new int[temp.size()];
		
		for(int k = 0; k < temp.size(); k++) {
			ret[k] = temp.get(k);
		}
		return ret;
	}

	public String decode(int[] model) {
		// assume that the variables are inputted in increasing order
		StringBuilder sb = new StringBuilder();

		// satisfiable, so read the solution
		//		for (int i = 0; i < N*N; i++) {
		//			//	    while (input.hasNext()) {
		//			int var = model[i];
		//			if (var > 0) {  // a positive literal
		//				int varX = toX(var);  
		//
		//				// verify that the variable corresponds to the right row
		//				assert row++ == varX;
		//
		//				int varY = toY(var);
		//				for (int j = 0; j < N; j++) {
		//					if (j == varY) sb.append("Q ");
		//					else sb.append("* ");
		//				}
		//				sb.append("\n");
		//			}
		//		}

		for(int y = 0; y < N; y++) {
			for(int x = 0; x < N; x++) {
				int var = toVar(y,x);

				if(model[var-1] > 0) {
					sb.append("Q ");
				} else {
					sb.append("* ");
				}
			}
			sb.append("\n");
		}

		return sb.toString();

	}

	//	//To match up with how other people show the models
	//	public String decodeBackwards(int[] model) {
	//		// assume that the variables are inputted in increasing order
	//		int row = 0;
	//		StringBuilder sb = new StringBuilder();
	//		char[][] string = new char[N][N];
	//		// satisfiable, so read the solution
	//		for (int i = N*N-1; i >= 0 ; i--) {
	//			//	    while (input.hasNext()) {
	//			int var = model[i];
	//			if (var > 0) {  // a positive literal
	//				int varX = toX(var);  
	//
	//				// verify that the variable corresponds to the right row
	//				assert row++ == varX;
	//
	//				int varY = toY(var);
	//				for (int j = 0; j < N; j++) {
	//					
	//					if (j == varY) string[j][varX] = 'Q';//sb.append("Q ");
	//					else string[j][varX] = '*';//sb.append("* ");
	//				}
	//				//sb.append("\n");
	//			}
	//			
	//		}
	//		
	//		char[][] flippedString = new char[N][N];
	//		int maxInd = N-1;
	//		for(int y = 0; y < N; y++) {
	//			for(int x = 0; x < N; x++) {
	//				flippedString[maxInd-y][x] = string[maxInd-x][maxInd-y]; 
	//			}
	//		}
	//
	//		for(int y = 0; y < N; y++) {
	//			for(int x = 0; x < N; x++) {
	//				sb.append(flippedString[x][y]).append(' '); 
	//			}
	//			sb.append("\n");
	//		}
	//
	//		
	//		return sb.toString();
	//
	//	}

	public void decodeAndSavePicture(int[] model, String filename) throws IOException {
		fileDecoding(filename, model);
	}


	public String consoleDecoding(int[] model) {
		return decode(model);
	}

	public BufferedImage pictureDecoding(int[] model)  {
		Image queen;
		try {
			queen = ImageIO.read(new File("Chess_tile_ql.png"));//"Chess_queen_icon.png"));
		} catch(IOException ioe) {
			return null;
		}
		
		BufferedImage img = new BufferedImage(N*50,N*50,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		for(int y = 0; y < N; y++) {
			for(int x = 0; x < N; x++) {
				if((x+y)%2 == 0) {
					g.setColor(Color.DARK_GRAY);
				} else {
					g.setColor(Color.LIGHT_GRAY);
				}
				g.fillRect(x*50,y*50,50,50);

				int var = toVar(y,x);

				if(model[var-1] > 0) {
					//g.setColor(Color.GRAY);
					//g.fillOval(x*50 + 12, y*50 + 12, 24, 24);
					g.drawImage(queen,x*50 + 5, y*50 + 5, null);
				}
			}
		}
		return img;
	}

	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		File f = new File(filePrefix + ".png");
		ImageIO.write(pictureDecoding(model),"png",f);
	}


}