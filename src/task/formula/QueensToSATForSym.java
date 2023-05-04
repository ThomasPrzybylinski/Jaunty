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
import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import util.lit.LitSorter;

/*************************************************************************
 *  A stack of clauses -- to help to print the clauses in
 *  reverse order.
 *************************************************************************/
@Deprecated //Currently does not solve NQueens correctly.
public class QueensToSATForSym implements ConsoleDecodeable, FileDecodable, CNFCreator {
	int N;  // NxN chess board

	public QueensToSATForSym(int size) {
		N = size;
	}

	public CNF generateCNF(VariableContext context) {
		return encode(context);
	}

	private int toVar(int y, int x) {
		// encode (row,column) to a SAT proposition
		return y + x*N +1; //(N-y-1)+1 + x*N;
	}

	public CNF encode(VariableContext context) {
		CNF ret = new CNF(context);

		for(int y = 0; y < N; y++) {
			for(int x = 0; x < N; x++) {
				int qAtPos = toVar(y,x);

				ArrayList<Integer> allDiagRowCol = new ArrayList<Integer>(4*N);
				allDiagRowCol.add(qAtPos);

				for(int n2 = 0; n2 < N; n2++) {
					//OnePerCol
					if(n2 != y) {
						int lit = -toVar(n2,x);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);
					}
					//OnePerRow
					if(n2 != x) {
						int lit = -toVar(y,n2);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);
					}
				}



				//One per diag
				for(int n = 1; n < N; n++) {
					int x2 = x + n;
					int y2 = y + n;

					if(inBounds(x2,y2)) {
						int lit = -toVar(y2,x2);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);
					}

					x2 = x - n;

					if(inBounds(x2,y2)) {
						int lit = -toVar(y2,x2);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);

					}

					y2 = y - n;

					if(inBounds(x2,y2)) {
						int lit = -toVar(y2,x2);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);

					}

					x2 = x + n;

					if(inBounds(x2,y2)) {
						int lit = -toVar(y2,x2);
						allDiagRowCol.add(lit);
						int[] cl = new int[]{-qAtPos,lit};
						LitSorter.inPlaceSort(cl);
						ret.fastAddClause(cl);
					}
				}

				int[] ifValidSpaceHasQueen = new int[allDiagRowCol.size()];
				for(int k = 0; k < ifValidSpaceHasQueen.length; k++) {
					ifValidSpaceHasQueen[k] = allDiagRowCol.get(k);
				}
				LitSorter.inPlaceSort(ifValidSpaceHasQueen);
				ret.fastAddClause(ifValidSpaceHasQueen);

			}
		}

//		NChooseRGenerator gen = new NChooseRGenerator(N*N,N);
//		
//		while(gen.hasMore()) {
//			int[] la = gen.getNext();
//			int[] cl = new int[la.length];
//			for(int k = 0; k < cl.length; k++) {
//				cl[k] = la[k]+1;
//			}
//			ret.fastAddClause(cl);
//		}
		ret.sort();
		ret = ret.reduce();

		return ret;
	}

	public boolean inBounds(int x2,int y2) {
		return (x2 >= 0 && x2 < N) && (y2 >= 0 && y2 < N);
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


	@Override
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
					g.setColor(Color.RED);
					g.drawString(""+var,x*50 + 12, y*50 + 12);

				}

			}
		}
		return img;
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(pictureDecoding(model),"png",f);

	}

	@Override
	public String toString() {
		return N+"QueensToSATForSym";
	}




}