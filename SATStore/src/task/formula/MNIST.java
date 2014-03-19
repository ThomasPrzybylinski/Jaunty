package task.formula;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;

import org.sat4j.specs.TimeoutException;

import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.DNF;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import workflow.decoder.RectangleBWPictureDecoder;

public class MNIST implements ModelGiver, ConsoleDecodeable, FileDecodable {
	
	private int width;
	private int height;
	
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
		Disjunctions dis = new Disjunctions();
		dis.setCurContext(context);

		
		
		try {
			File f = new File(filename);
			
			FileInputStream in = new FileInputStream(f);
			DataInputStream din = new DataInputStream(in);
			din.readInt();
			
			int num = din.readInt();
			
			num = 300;
			
			width = din.readInt();
			height = din.readInt();
			
			
			for(int k = 0; k < width*height; k++) {
				if(context.size() <= k) {
					context.createNextDefaultVar();
				}
			}
			
			
			for(int k = 0; k < num; k++) {
				Conjunctions clause = new Conjunctions();
				clause.setCurContext(context);
			
				for(int w = 0; w < width; w++) {
					for(int h = 0; h < width; h++) {
						Variable var = context.getVar(getVar(w,h));
						clause.add(din.readUnsignedByte() > 255/2 ? var.getPosLit() : var.getNegLit());
					}
				}
				
				dis.add(clause);
			}
			
			din.close();
			in.close();
			
		} catch(IOException ie) {
			throw new RuntimeException(ie);
		}
		
		return new DNF(dis).getClauses();
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
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
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,width,height),"png",f);
		
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int x = 0; x< width; x++) {
			for(int y = 0; y < height; y++) {
				int index = getVar(x,y)-1;

				if(model[index] > 0) {
					sb.append('X');
				} else {
					sb.append(' ');
				}


			}

			sb.append(ConsoleDecodeable.newline);
		}

		return sb.toString();

	}

}
