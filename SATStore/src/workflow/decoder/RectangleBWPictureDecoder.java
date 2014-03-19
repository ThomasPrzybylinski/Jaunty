package workflow.decoder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class RectangleBWPictureDecoder {
	private static int sqSize = 25;
	/**
	 * @param args
	 */
	public static BufferedImage pictureDecoding(int[] model, int width, int length)  {
		BufferedImage img = new BufferedImage(width*sqSize+1,length*sqSize+1,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		
		for(int y = 0; y < length; y++) {
			for(int x = 0; x < width; x++) {

				
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
	
}
