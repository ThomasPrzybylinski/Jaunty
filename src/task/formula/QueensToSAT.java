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


class LinkedStackOfClauses {
	private int N;          // size of the stack
	private Node first;     // top of stack

	// helper Node class
	private class Node {
		private int[] clause;
		private Node next;

		public String toString() {
			String s = "";
			for (int i = 0; i < clause.length; i++) {
				s += clause[i] + " ";
			}
			return s;
		}

	}

	// is the stack empty?
	public boolean isEmpty() { return first == null; }

	// number of elements on the stack
	public int size() { return N; }


	// add an element to the stack
	public void push(int[] item) {
		Node oldfirst = first;
		first = new Node();
		first.clause = item;
		first.next = oldfirst;
		N++;
	}

	// delete and return the most recently added element
	public int[] pop() {
		if (isEmpty()) throw new RuntimeException("Stack underflow");
		int[] item = first.clause;      // save item to return
		first = first.next;            // delete first node
		N--;
		return item;                   // return the saved item
	}



	// test client
	public static void main(String[] args) {

		LinkedStackOfClauses s = new LinkedStackOfClauses();

		int[] item1 = {1,2,3};
		int[] item2 = {3,4};
		int[] item3 = {5,6,7,8};

		s.push(item1);
		s.push(item2);
		s.push(item3);

		while (!s.isEmpty()) {
			int[] elem = s.pop();
			for (int i = 0; i < elem.length; i++) 
				System.out.print(elem[i] + " ");
			System.out.println();
		}

	} 
}

public class QueensToSAT implements ConsoleDecodeable, FileDecodable, CNFCreator {
	int N;  // NxN chess board

	public QueensToSAT(int size) {
		N = size;
	}

	private int toVar(int y, int x) {
		// encode (row,column) to a SAT proposition
		return y + x*N +1; //(N-y-1)+1 + x*N;
	}

	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("Usage: QueenToSat N [-d]");
			System.exit(0);
		}

		QueensToSAT sater = new QueensToSAT(Integer.parseInt(args[0]));

		boolean decode = false;

		if (args.length == 2) 
			decode = (args[1].equals("-d") ? true : false);

		if (! decode) 
			sater.encode(VariableContext.defaultContext);
		//		else
		//			sater.decode();

	}

	public CNF generateCNF(VariableContext context) {
		return encode(context);
	}

	public CNF encode(VariableContext context) {

		CNF ret = new CNF(context);
		int[] aClause;

		int CI = 0;
		// one queen in each row
		for (int i = 0; i < N; i++) {
			aClause = new int[N];
			CI = 0;
			// i represents the queen
			for (int j = 0; j < N; j++) {
				// j represents the column position
				// System.out.print(toVar(i,j) + " ");
				aClause[CI++] = toVar(i,j);
			}
			//System.out.println("0");
			LitSorter.inPlaceSort(aClause);
			ret.fastAddClause(aClause);

			for (int j = 0; j < N-1; j++) {
				// j represents the column position
				// System.out.print(toVar(i,j) + " ");
				for (int k = j + 1; k < N; k++) {
					aClause = new int[2];
					aClause[0] = -toVar(i,j);
					aClause[1] = -toVar(i,k);
					LitSorter.inPlaceSort(aClause);
					ret.fastAddClause(aClause);
				}

			}
		}
		
		// one queen in each col
		for (int i = 0; i < N; i++) {
			aClause = new int[N];
			CI = 0;
			// i represents the queen
			for (int j = 0; j < N; j++) {
				// j represents the row position
				// System.out.print(toVar(i,j) + " ");
				aClause[CI++] = toVar(j,i);
			}
			//System.out.println("0");
			LitSorter.inPlaceSort(aClause);
			ret.fastAddClause(aClause);

			for (int j = 0; j < N-1; j++) {
				// j represents the row position
				// System.out.print(toVar(i,j) + " ");
				for (int k = j + 1; k < N; k++) {
					aClause = new int[2];
					aClause[0] = -toVar(j,i);
					aClause[1] = -toVar(k,i);
					LitSorter.inPlaceSort(aClause);
					ret.fastAddClause(aClause);
				}

			}
		}


		// no attacking queens
		for (int i = 0; i < N; i++) 
			// i represents the first queen
			for (int j = 0; j < N; j++) 
				// j represents the column position of i
				for (int m = i + 1; m < N; m++) 
					// m represents the second queen
					for (int k = 0; k < N; k++)
						// k represents the column position of m
						if (j == k) {  // no queens on the same column
							// System.out.println(-toVar(i,j) + " " +  -toVar(m,k) + " 0");
							aClause = new int[2];
							aClause[0] = -toVar(i,j);
							aClause[1] = -toVar(m,k);
							LitSorter.inPlaceSort(aClause);
							ret.fastAddClause(aClause);
						} 
						else if (Math.abs(k - j) == Math.abs(i - m)) {
							// no queens on the same diagonal
							// System.out.println(-toVar(i,j) + " " +  -toVar(m,k) + " 0");
							aClause = new int[2];
							aClause[0] = -toVar(i,j);
							aClause[1] = -toVar(m,k);
							LitSorter.inPlaceSort(aClause);
							ret.fastAddClause(aClause);
						}
		
		// no attacking queens
		for (int i = 0; i < N; i++) 
			// i represents the first queen
			for (int j = 0; j < N; j++) 
				// j represents the row position of i
				for (int m = i + 1; m < N; m++) 
					// m represents the second queen
					for (int k = 0; k < N; k++)
						// k represents the row position of m
						if (j == k) {  // no queens on the same column
							// System.out.println(-toVar(i,j) + " " +  -toVar(m,k) + " 0");
							aClause = new int[2];
							aClause[0] = -toVar(j,i);
							aClause[1] = -toVar(k,m);
							ret.fastAddClause(aClause);
						} 
						else if (Math.abs(k - j) == Math.abs(i - m)) {
							// no queens on the same diagonal
							// System.out.println(-toVar(i,j) + " " +  -toVar(m,k) + " 0");
							aClause = new int[2];
							aClause[0] = -toVar(j,i);
							aClause[1] = -toVar(k,m);
							LitSorter.inPlaceSort(aClause);
							ret.fastAddClause(aClause);
						}
		ret.sort();
		return ret.trySubsumption();
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
//		ImageManager imageManager = new ImageManager(new DefaultImageContext());
//		ImageSessionContext sessionContext = new DefaultImageSessionContext(
//				imageManager.getImageContext(), null);

//		Image queen;
//		try {
//			ImageInfo info = imageManager.getImageInfo("Chess_tile_ql.eps", sessionContext);
//			queen = imageManager.getImage(
//					info, ImageFlavor.GRAPHICS2D, sessionContext);
//		} catch(ImageException ie) {
//			ie.printStackTrace();
//			return null;
//		} catch(IOException ie) {
//			ie.printStackTrace();
//			return null;
//		}

		Image queen;
		try {
			queen = ImageIO.read(new File("Chess_tile_ql.png"));//""));
		} catch(IOException ioe) {
			return null;
		}

//		PDFDocumentGraphics2D g = new PDFDocumentGraphics2D();
//		g.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

		BufferedImage img = new BufferedImage(N*50,N*50,BufferedImage.TYPE_INT_RGB);

		Graphics g = img.getGraphics();
		queen = queen.getScaledInstance(40,37,Image.SCALE_SMOOTH);
		
//		g.setDeviceDPI(300);
//		g.setSVGDimension(N*50,N*50);
//		g.nextPage();
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
					g.drawImage(queen,x*50 + 5, y*50 + 5,40,40, null);
//					g.addNativeImage(queen, x*50,y*50, 5, 5);
					//					g.setColor(Color.RED);
					//					g.drawString(""+var,x*50 + 12, y*50 + 12);
				}
			}
		}
//		try {
//			g.finish();
//		}catch(IOException e) {
//			e.printStackTrace();
//			return null;
//		}

		return img;//g.getPDFDocument();
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
//		PDFDocument doc = pictureDecoding(model);
//		doc.output(new FileOutputStream(f));
		ImageIO.write(pictureDecoding(model),"png",f);

	}
	
	public int numQueens() {
		return N;
	}

	@Override
	public String toString() {
		return N+"QueensToSAT";
	}




}