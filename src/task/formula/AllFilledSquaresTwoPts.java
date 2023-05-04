package task.formula;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import formula.Variable;
import formula.VariableContext;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;

public class AllFilledSquaresTwoPts implements ModelGiver, ConsoleDecodeable {
	private int size;
	private List<int[]> models;
	
	public AllFilledSquaresTwoPts(int size) {
		this.size = size;
	}
	
	@Override
	public List<int[]> getAllModels(VariableContext context)
			throws TimeoutException {
		models = generateSpace(context);
		return models;
	}

	@Override
	public ConsoleDecodeable getConsoleDecoder() {
		return this;
	}

	@Override
	public FileDecodable getFileDecodabler() {
		return null;
	}
	
	public List<int[]> generateSpace(VariableContext context) {
		List<int[]> dis = new ArrayList<int[]>();
		
		for(int k = 0; k < size*size*2; k++) {
			if(context.size() <= k) {
				context.createNextDefaultVar();
			}
		}
		
		for(int s = 0; s <= size; s++) {
			for(int x = 0; x < size-s; x++) {
				for(int y = 0; y < size-s; y++) {
					Variable var = context.getVar(getVar(x,y,true));
					Variable var2 = context.getVar(getVar(x+s,y+s,false));
					dis.add(new int[]{var.getPosLit().getIntRep(),var2.getPosLit().getIntRep()});
				}
			}
		}
		
		return dis;
	}
	
	private boolean isOnSquare(int x, int y, int size, int x2, int y2) {
		return ((x2 >= x && x2 <= x+size && y2 >=y && y2 <=y+size 
//				&& (x2 == x || x2 == x+size || y2 ==y || y2 == y+size)
				));
	}

	private int getVar(int x, int y, boolean topLeft) {
		return y*size + x + 1 + (topLeft ? 0 : size*size);
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
		int var1 = model[0];
		int var2 = model[1];
		
		int x1 = (var1-1)%size; 
		int y1 = (var1-1)/size;
		
		int x2 = (var2-size*size-1)%size; 
		int y2 = (var2-size*size-1)/size;
		
		StringBuilder sb = new StringBuilder();
		for(int x = -1; x< size; x++) {
			for(int y = -1; y < size; y++) {
				if((x1 <= x && y1 <= y) && (x2 >= x && y2 >= y)) {
					sb.append('X');
				} else if(x == -1 && y == -1) {
					sb.append('*');
				} else if (x == -1) {
					sb.append('_');
				} else if (y == -1) {
					sb.append('|');
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

//	@Override
//	public void fileDecoding(String filePrefix, int[] model) throws IOException {
//		fileDecoding(new File("."),filePrefix,model);
//	}
//
//	@Override
//	public void fileDecoding(File dir, String filePrefix, int[] model)
//			throws IOException {
//		for(int k = 0; k < models.size(); k++) {
//			if(model.equals(models.get(k))) {
//				model=toIntAr(coords.getPts().get(k));
//				break;
//			}
//		}
//		
//		File f = new File(dir, filePrefix + ".png");
//		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,size,size),"png",f);
//		
//	}

	@Override
	public String toString() {
		return "AllFilledSquares="+size;
	}
	
	
}
