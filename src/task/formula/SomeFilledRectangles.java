package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

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

public class SomeFilledRectangles implements ModelGiver, ConsoleDecodeable, FileDecodable {
	private int worldSizeX;
	private int worldSizeY;
	private int minRecSizeX;
	private int minRecSizeY;
	private int maxRecSizeX;
	private int maxRecSizeY;

	public SomeFilledRectangles(int worldSizeX, int worldSizeY,
			int minRecSizeX, int maxRecSizeX,
			int minRecSizeY, int maxRecSizeY) {
		super();
		this.worldSizeX = worldSizeX;
		this.worldSizeY = worldSizeY;
		this.minRecSizeX = minRecSizeX;
		this.minRecSizeY = minRecSizeY;
		this.maxRecSizeX = maxRecSizeX;
		this.maxRecSizeY = maxRecSizeY;
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

		for(int k = 0; k < worldSizeX*worldSizeY; k++) {
			if(context.size() <= k) {
				context.createNextDefaultVar();
			}
		}

		for(int s = minRecSizeX; s <= maxRecSizeX; s++) {
			for(int sy = minRecSizeY; sy <= maxRecSizeY; sy++) {
				for(int x = 0; x <= worldSizeX-s; x++) {
					for(int y = 0; y <= worldSizeY-sy; y++) {
						Conjunctions clause = new Conjunctions();
						clause.setCurContext(context);
						for(int x2 = 0; x2 < worldSizeX; x2++) {
							for(int y2 = 0; y2 < worldSizeY; y2++) {
								Variable var = context.getVar(getVar(x2,y2));
								if(isOnRectangle(x,y,s,sy,x2,y2)) {
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
		}

		return new DNF(dis);
	}

	private boolean isOnRectangle(int x, int y, int size, int sizey, int x2, int y2) {
		return ((x2 >= x && x2 <= x+size-1 && y2 >=y && y2 <=y+sizey-1
				//&& (x2 == x || x2 == x+size || y2 ==y || y2 == y+sizey)
				));
	}

	private int getVar(int x, int y) {
		return y*worldSizeX + x + 1;
	}

	@Override
	public String consoleDecoding(int[] model) {
		StringBuilder sb = new StringBuilder();
		for(int x = 0; x< worldSizeX; x++) {
			for(int y = 0; y < worldSizeY; y++) {
				int index = getVar(x,y)-1;

				if(model[index] > 0) {
					sb.append('X');
				} else {
					sb.append('~');
				}


			}

			sb.append(ConsoleDecodeable.newline);
		}

		return sb.toString();
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		File f = new File(dir, filePrefix + ".png");
		ImageIO.write(RectangleBWPictureDecoder.pictureDecoding(model,worldSizeX,worldSizeY),"png",f);
		
	}

	@Override
	public String getDirName() {
		return this.getClass().getSimpleName()+repString();
	}

	@Override
	public String toString() {
		return "SomeFilledRectanges="+repString();
	}
	
	private String repString() {
		return "(wx_" + worldSizeX + "|wy_" + worldSizeY
				+ "|minrx_" + minRecSizeX+ "|minry_" + minRecSizeY
				+ "|maxrx_" + maxRecSizeX+ "|maxry_" + maxRecSizeY+")";
	}





}
