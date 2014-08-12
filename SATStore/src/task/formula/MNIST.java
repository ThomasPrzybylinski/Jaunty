package task.formula;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.sat4j.specs.TimeoutException;

import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import workflow.decoder.RectangleBWPictureDecoder;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.DNF;

public class MNIST implements ModelGiver, FileDecodable { //ConsoleDecodeable,  {

	public static final int const_neg = Integer.MIN_VALUE;
	public static final int const_pos = Integer.MAX_VALUE;
	private int width;
	private int height;

	int[] decoder;

	String filename;

	public MNIST(String filename) {
		this.filename = filename;

	}

	private int getVar(int x, int y) {
		return y*width + x + 1;
	}

	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		VariableContext origContext = context;
		context = new VariableContext(); //I needed to change this without side-effects

		ArrayList<int[]> ret = new ArrayList<int[]> ();


		try {
			File f = new File(filename);

			FileInputStream in = new FileInputStream(f);
			DataInputStream din = new DataInputStream(in);
			din.readInt();

			int num = din.readInt();

			num = 1500;
			
			ret.ensureCapacity(num);

			width = din.readInt();
			height = din.readInt();


			for(int k = 0; k < width*height; k++) {
				if(context.size() <= k) {
					context.createNextDefaultVar();
				}
			}


			for(int k = 0; k < num; k++) {
				int[] clause = new int[width*height];
				for(int w = 0; w < width; w++) {
					for(int h = 0; h < height; h++) {
						int var = getVar(w,h);
						clause[var-1] = din.readUnsignedByte() > 255/2 ? var : -var;
					}
				}

				ret.add(clause);
			}

			din.close();
			in.close();

		} catch(IOException ie) {
			throw new RuntimeException(ie);
		}

		return removeSingleValAndEquivVars(ret,origContext);
	}

	private List<int[]> removeSingleValAndEquivVars(List<int[]> clauses, VariableContext context) {
		int[] firstModel = clauses.get(0); 
		int numVars = firstModel.length;
		decoder = new int[numVars];

		boolean[] rem = new boolean[numVars];

		//First remove single-valued vars
		for(int k = 1; k <= numVars; k++) {
			if(rem[k-1]) continue;

			int val = firstModel[k-1];


			for(int[] model : clauses) {
				int lit = model[k-1];
				if(val != lit) {
					val = 0;
					break;
				}
			}

			if(val != 0) {
				if(val > 0) {
					decoder[k-1] = const_pos;
				} else {
					decoder[k-1] = const_neg;
				}
				rem[k-1] = true;
			}
		}

		//Then remove based on equality
		for(int k = 1; k <= numVars; k++) {
			if(rem[k-1]) continue;

			boolean[] areEqual = new boolean[numVars];
			boolean[] areInvEqual = new boolean[numVars];
			Arrays.fill(areEqual,true);
			Arrays.fill(areInvEqual,true);

			for(int[] model : clauses) {
				int lit = model[k-1];
				int sign = lit/Math.abs(lit);

				for(int j = k+1; j <= numVars; j++) {
					if(rem[j-1]) {
						areEqual[j-1] = false;
						areInvEqual[j-1] = false;
						continue;
					}
					int lit2 = model[j-1];
					int sign2 = lit2/Math.abs(lit2);

					if(sign != sign2) {
						areEqual[j-1] = false;
					} else {
						areInvEqual[j-1] = false;
					}
				}
			}

			for(int j = k+1; j <= numVars; j++) {
				if(areEqual[j-1]) {
					decoder[j-1] = k;
					rem[j-1] = true;
				} else if(areInvEqual[j-1]) {
					decoder[j-1] = -k;
					rem[j-1] = true;
				}
			}
		}

		int size = 0;
		for(boolean b : rem) {
			if(!b) size++;
		}

		ArrayList<int[]> ret = new ArrayList<int[]>(clauses.size());

		for(int[] model : clauses) {
			int[] toAdd = new int[size];
			int toAddInd = 0;

			for(int k = 0; k < model.length; k++) {
				if(!rem[k]) {
					decoder[k] = toAddInd+1;
					for(int j = k+1; j < numVars; j++) {
						if(decoder[j] == k+1) {
							decoder[j] = toAddInd+1;
						} else if(decoder[j] == -(k+1)) {
							decoder[j] = -(toAddInd+1);
						}
					}

					toAdd[toAddInd] = (model[k]/Math.abs(model[k]))*(toAddInd+1);
					toAddInd++;
				}
			}

			ret.add(toAdd);
		}

		context.ensureSize(size);
		return ret;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return null;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return this;
	}

	@Override
	public String getDirName() {
		return this.getClass().getSimpleName();
	}


	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,width,height,decoder),"png",f);

	}

//	@Override
//	public String consoleDecoding(int[] model) {
//		StringBuilder sb = new StringBuilder();
//		for(int x = 0; x< width; x++) {
//			for(int y = 0; y < height; y++) {
//				int index = getVar(x,y)-1;
//
//				if(model[index] > 0) {
//					sb.append('X');
//				} else {
//					sb.append(' ');
//				}
//
//
//			}
//
//			sb.append(ConsoleDecodeable.newline);
//		}
//
//		return sb.toString();
//
//	}

}
