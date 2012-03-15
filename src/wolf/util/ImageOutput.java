package wolf.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class ImageOutput {
	public int width;
	public int height;
	Color color;
	int rgbcolor;
	public BufferedImage bi;
	Graphics2D g;


	public ImageOutput(int width, int height){
		this.width = width;
		this.height = height;
		bi = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		g = bi.createGraphics();
	}
	
	public void setColor(int x, int y, float r, float g, float b){
		color = new Color(bound(r), bound(g), bound(b));
		rgbcolor = color.getRGB();	
		bi.setRGB(x, y, rgbcolor);
	}

	public void updateColor(int x, int y, float r, float g, float b){
		Color c = new Color(bi.getRGB(x, y));
		color = new Color(Math.min(1,r+((float)c.getRed()/255)), Math.min(1,g+((float)c.getGreen()/255)), Math.min(1,b+((float)c.getBlue()/255)));


		rgbcolor = color.getRGB();

		bi.setRGB(x, y, rgbcolor);
	}

	public void line(int x1, int y1, int x2, int y2,int lineWidth, int red, int green, int blue){
		color = new Color(red,green,blue);
		
		GradientPaint gp = new GradientPaint(x1, y1, color, x2, y2, color.brighter().brighter());
		g.setColor(color);
		g.setPaint(gp);
		g.setStroke(new BasicStroke(lineWidth));
		g.drawLine(x1, y1, x2, y2);
		
	}

	public void circle(int x, int y, int diameter, int red, int green, int blue){
		color = new Color(red,green,blue);
		g.setColor(color);
		g.drawOval(x-(diameter/2), y-(diameter/2), diameter, diameter);
	}

	public void text(int x, int y, String text){
		color = new Color(255,255,255);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	public void saveImage(String filename){
		g.dispose();
		File f = new File(filename+".png");
		f.mkdirs();
		try {
			boolean exists = !f.createNewFile();
			if(exists){
				System.out.println(f.getAbsolutePath()+" exists. Overwriting");
			}
			ImageIO.write(bi, "png", f);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private float bound(float a){
		return Math.max(Math.min(a, 1), 0);
	}
}