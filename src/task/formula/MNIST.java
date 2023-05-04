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

import formula.VariableContext;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import workflow.decoder.RectangleBWPictureDecoder;

public class MNIST implements ModelGiver, FileDecodable { //ConsoleDecodeable,  {

	public static final int const_neg = Integer.MIN_VALUE+1;
	public static final int const_pos = Integer.MAX_VALUE-1;
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

		return removeSingleValAndEquivVars(ret,origContext);//ret;//
	}

	private List<int[]> removeSingleValAndEquivVars(List<int[]> clauses, VariableContext context) {
		int[] firstModel = clauses.get(0); 
		int numVars = firstModel.length;
		decoder = new int[numVars+1];

		boolean[] rem = new boolean[numVars+1];

		//First remove single-valued vars
		for(int k = 1; k <= numVars; k++) {
			if(rem[k]) continue;

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
					decoder[k] = const_pos;
				} else {
					decoder[k] = const_neg;
				}
				rem[k] = true;
			}
		}

		//Then remove based on equality
		for(int k = 1; k <= numVars; k++) {
			if(rem[k]) continue;

			boolean[] areEqual = new boolean[numVars+1];
			boolean[] areInvEqual = new boolean[numVars+1];
			Arrays.fill(areEqual,true);
			Arrays.fill(areInvEqual,true);

			for(int[] model : clauses) {
				int lit = model[k-1];
				int sign = lit/Math.abs(lit);

				for(int j = k+1; j <= numVars; j++) {
					if(rem[j]) {
						areEqual[j] = false;
						areInvEqual[j] = false;
						continue;
					}
					int lit2 = model[j-1];
					int sign2 = lit2/Math.abs(lit2);

					if(sign != sign2) {
						areEqual[j] = false;
					} else {
						areInvEqual[j] = false;
					}
				}
			}

			for(int j = k+1; j <= numVars; j++) {
				if(areEqual[j]) {
					decoder[j] = k;
					rem[j] = true;
				} else if(areInvEqual[j]) {
					decoder[j] = -k;
					rem[j] = true;
				}
			}
		}

		int size = -1; //since 0 is in rem
		for(boolean b : rem) {
			if(!b) size++;
		}
		ArrayList<int[]> ret = new ArrayList<int[]>(clauses.size());
		
		int nextNewVar = 1;
		for(int k = 0; k < firstModel.length; k++) {
			int var = Math.abs(firstModel[k]);
			if(!rem[var]) {
				decoder[var] = nextNewVar;
				for(int j = var+1; j <= numVars; j++) {
					if(decoder[j] == var) {
						decoder[j] = nextNewVar;
					} else if(decoder[j] == -var) {
						decoder[j] = -(nextNewVar);
					}
				}
				nextNewVar++;
			}
		}

		for(int[] model : clauses) {
			int[] toAdd = new int[size];
			int toAddInd = 0;
			for(int k = 0; k < model.length; k++) {
				int var = Math.abs(model[k]);
				if(!rem[var]) {
					toAdd[toAddInd] = (var/model[k])*decoder[var];
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
//		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,width,height),"png",f);
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
