package workflow.decoder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public class RectangleBWPictureDecoder {
	private static int sqSize = 25;
	/**
	 * @param args
	 */
	public static BufferedImage pictureDecoding(int[] model, int width, int height)  {
		BufferedImage img = new BufferedImage(width*sqSize+1,height*sqSize+1,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int var = getVar(x,y,width);
				
				if(model[var-1] > 0) {
					g.setColor(Color.BLACK);
					g.fillRect(x*sqSize,y*sqSize,sqSize,sqSize);	
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(x*sqSize,y*sqSize,sqSize,sqSize);
					g.setColor(Color.BLACK);
					g.drawRect(x*sqSize,y*sqSize,sqSize,sqSize);
				}
			}
		}
		return img;

	}

	private static int getVar(int x, int y, int width) {
		return y*width + x + 1;
	}

	//For MNIST which had to be pre-processed to remove extraneous variables
	public static RenderedImage pictureDecoding(int[] model, int width,
			int length, int[] decoder) {
		BufferedImage img = new BufferedImage(width*sqSize+1,length*sqSize+1,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		
		for(int y = 0; y < length; y++) {
			for(int x = 0; x < width; x++) {

				
				int var = getVar(x,y,width);
				int decoding = decoder[var];
				int absDecoding = Math.abs(decoding);
				
				if(absDecoding <= model.length) {
					//If not a single val
					decoding = (decoding/absDecoding)*(model[absDecoding-1]);
				}
				
				if(decoding > 0) {
					g.setColor(Color.BLACK);
					g.fillRect(x*sqSize,y*sqSize,sqSize,sqSize);	
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(x*sqSize,y*sqSize,sqSize,sqSize);
					g.setColor(Color.BLACK);
					g.drawRect(x*sqSize,y*sqSize,sqSize,sqSize);
				}
			}
		}
		return img;
	}
	
}
