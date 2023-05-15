package task.formula;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.sat4j.specs.TimeoutException;

import formula.Disjunctions;
import formula.Variable;
import formula.VariableContext;
import task.formula.coordinates.CoordSpace;
import task.formula.coordinates.CoordsToBinary;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import workflow.ModelGiver;
import workflow.decoder.RectangleBWPictureDecoder;

public class AllFilledSquares extends SomeFilledRectangles {
	private int size;
	
	public AllFilledSquares(int size) {
		super(size,size,1,size,1,size);
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
	public String toString() {
		return "AllFilledSquares="+size;
	}
	
	
}
