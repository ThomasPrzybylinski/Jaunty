package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.sat4j.specs.TimeoutException;

import task.formula.coordinates.CoordSpace;
import task.formula.coordinates.CoordsToBinary;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import workflow.decoder.RectangleBWPictureDecoder;
import formula.Conjunctions;
import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import formula.simple.DNF;

public class AllFilledSquares implements ModelGiver, ConsoleDecodeable, FileDecodable {
	private int size;
	private List<int[]> models;
	private CoordSpace coords;
	
	public AllFilledSquares(int size) {
		this.size = size;
	}
	
	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		coords = generateSpace(context);
		models = CoordsToBinary.coordsToModels(coords,context);
		return models;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return this;
	}
	
	public CoordSpace generateSpace(VariableContext context) {
		Disjunctions dis = new Disjunctions();
		dis.setCurContext(context);
		
		for(int k = 0; k < size*size; k++) {
			if(context.size() <= k) {
				context.createNextDefaultVar();
			}
		}
		
		CoordSpace space = new CoordSpace(size*size);
		
		for(int s = 0; s <= size; s++) {
			for(int x = 0; x < size-s; x++) {
				for(int y = 0; y < size-s; y++) {
					double[] coord = new double[size*size];
					for(int x2 = 0; x2 < size; x2++) {
						for(int y2 = 0; y2 < size; y2++) {
							Variable var = context.getVar(getVar(x2,y2));
							int index = var.getPosLit().getIntRep()-1;
							if(isOnSquare(x,y,s,x2,y2)) {
								coord[index]=1;
								//clause.add(var.getPosLit());
							} else {
								coord[index]=0;
								//clause.add(var.getNegLit());
							}
						}
					}
					space.addPt(coord);
//					dis.add(clause);
				}
			}
		}
		
		return space;
	}
	
	private boolean isOnSquare(int x, int y, int size, int x2, int y2) {
		return ((x2 >= x && x2 <= x+size && y2 >=y && y2 <=y+size 
//				&& (x2 == x || x2 == x+size || y2 ==y || y2 == y+size)
				));
	}

	private int getVar(int x, int y) {
		return y*size + x + 1;
	}
	
	private static int[] toIntAr(double[] ar) {
		int[] i = new int[ar.length];
		
		for(int k = 0; k < i.length; k++) {
			i[k] = (int)ar[k];
		}
		
		return i;
	}
	
	@Override
	public String consoleDecoding(int[] model) {
		
		for(int k = 0; k < models.size(); k++) {
			if(model.equals(models.get(k))) {
				model=toIntAr(coords.getPts().get(k));
				break;
			}
		}
		
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
		for(int k = 0; k < models.size(); k++) {
			if(model.equals(models.get(k))) {
				model=toIntAr(coords.getPts().get(k));
				break;
			}
		}
		
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,size,size),"png",f);
		
	}

	@Override
	public String toString() {
		return "AllFilledSquares="+size;
	}
	
	
}
