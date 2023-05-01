package task.formula.coordinates;

import java.io.File;
import java.io.IOException;
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

public class AllFilledSquaresCoords implements ModelGiver, ConsoleDecodeable, FileDecodable {
	private int size;
	
	public AllFilledSquaresCoords(int size) {
		this.size = size;
	}
	
	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		return generateDNF(context).getClauses();
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return this;
	}
	
	public DNF generateDNF(VariableContext context) {
		Disjunctions dis = new Disjunctions();
		dis.setCurContext(context);
		
		for(int k = 0; k < size*size; k++) {
			if(context.size() <= k) {
				context.createNextDefaultVar();
			}
		}
		
		for(int s = 0; s <= size; s++) {
			for(int x = 0; x < size-s; x++) {
				for(int y = 0; y < size-s; y++) {
					Conjunctions clause = new Conjunctions();
					clause.setCurContext(context);
					for(int x2 = 0; x2 < size; x2++) {
						for(int y2 = 0; y2 < size; y2++) {
							Variable var = context.getVar(getVar(x2,y2));
							if(isOnSquare(x,y,s,x2,y2)) {
								clause.add(var.getPosLit());
							} else {
								clause.add(var.getNegLit());
							}
						}
					}
					
					dis.add(clause);
				}
			}
		}
		
		return new DNF(dis);
	}
	
	private boolean isOnSquare(int x, int y, int size, int x2, int y2) {
		return ((x2 >= x && x2 <= x+size && y2 >=y && y2 <=y+size 
//				&& (x2 == x || x2 == x+size || y2 ==y || y2 == y+size)
				));
	}

	private int getVar(int x, int y) {
		return y*size + x + 1;
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int x = 0; x< size; x++) {
			for(int y = 0; y < size; y++) {
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

	@Override
	public String getDirName() {
		return this.getClass().getSimpleName()+"("+size+")";
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,size,size),"png",f);
		
	}

	@Override
	public String toString() {
		return "AllFilledSquares="+size;
	}
	
	
}
