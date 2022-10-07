import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Exception;
import java.util.*;

public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 640;// default image width and height
	int height = 480;
	static ImageDisplay ren;
	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */

	public ArrayList<String> preProcessFile (File[] listOfFiles) {
		ArrayList<String> f = new ArrayList<String>();
		for (int i = 0; i < listOfFiles.length; i++){
			f.add(listOfFiles[i].getAbsolutePath());
		} 
		Collections.sort(f);
		return f;
	}
	
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void showIms(String f){
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, f, imgOne);

		// Use label to display the image
		JLabel label = (JLabel) frame.getContentPane().getComponent(0); 
		label.setIcon(new ImageIcon(imgOne));

		//lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(label, c);

		frame.getContentPane().revalidate(); 
   		frame.getContentPane().repaint();
	}

	public void playVideo(BufferedImage img){
		// Use label to display the image
		//frame = new JFrame();
		//GridBagLayout gLayout = new GridBagLayout();
		//frame.getContentPane().setLayout(gLayout);

		//lbIm1 = new JLabel(new ImageIcon(img));
		JLabel label = (JLabel) frame.getContentPane().getComponent(0); 
		label.setIcon(new ImageIcon(img));

		// GridBagConstraints c = new GridBagConstraints();
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.anchor = GridBagConstraints.CENTER;
		// c.weightx = 0.5;
		// c.gridx = 0;
		// c.gridy = 0;

		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.gridx = 0;
		// c.gridy = 1;
		// frame.getContentPane().add(label, c);
		// frame.pack();
		// frame.setVisible(true);
		frame.getContentPane().revalidate(); 
		frame.getContentPane().repaint();
		
	}
	
	public Boolean isGreen(Float h, Float s, Float b){
		return h >= 85 && h < 180 && s >= 28 && s <= 100 && b >= 28 && b <= 100; 
	}

	public BufferedImage GreenBackground(String foreground, String background) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage bgImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, foreground, img);
		readImageRGB(width, height, background, bgImg);
		
		float[] hsb = new float[3];
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				Color c = new Color(img.getRGB(i, j));
				Color bgColor = new Color(bgImg.getRGB(i, j));
				//convert RGB to HUV
				Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
				Float hue = hsb[0]*360;
				Float saturation = hsb[1]*100;
				Float brightness = hsb[2]*100;
				//replace green screen
				if (isGreen(hue, saturation, brightness)) {
					img.setRGB(i, j, bgImg.getRGB(i, j));
				}
				// else if(hue >= 85 && hue < 180 &&
				// 		saturation < 28 &&
				// 		brightness < 28 ){} 



				//replace green boundary
				// else if( ((hue > 180 && hue <= 200 )||(hue >= 60 && hue < 80 ))&&
				// 		brightness >= 40){
				// 			//System.out.println("boundary");
				// 			int newGreen = bgColor.getGreen();
				// 			int Red = (c.getRed()+bgColor.getRed())/2;
				// 			int Blue = (c.getBlue()+bgColor.getBlue())/2;
				// 			Color replaceC = new Color(newGreen, Red, Blue);
				// 			img.setRGB(i, j, replaceC.getRGB());
				// 		}
			}
		}
		
		return img;
	}
	//current picture, next picture
	public BufferedImage substractBackGround(String foreground, String nextForeground, String background) {
		BufferedImage cur = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage next = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage back = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, foreground, cur);
		readImageRGB(width, height, nextForeground, next);
		readImageRGB(width, height, background, back);
		int totalDiff;
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				Color cc = new Color(cur.getRGB(i, j));
				Color nc = new Color(next.getRGB(i, j));
				totalDiff = Math.abs(cc.getRed() - nc.getRed()) + 
							Math.abs(cc.getGreen() - nc.getGreen()) + 
							Math.abs(cc.getBlue() - nc.getBlue());
				//System.out.println(c.getRed() + ", "+ c.getGreen() + ", " + c.getBlue());
				if (totalDiff <= 5) {
					cur.setRGB(i, j, back.getRGB(i, j));
				}
			}
		}
		
		return cur;
	}
	public static void main(String[] args) {
		//read all rgb files
		ren = new ImageDisplay();
		File foreGround = new File(args[0]);
		ArrayList<String>foregroundF  = ren.preProcessFile(foreGround.listFiles());
		File backGround = new File(args[1]);
		ArrayList<String>backgroundF  = ren.preProcessFile(backGround.listFiles());
		int mode = Integer.parseInt(args[2]);
		ren.frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		//ren.lbIm1 = new JLabel();
		ren.frame.getContentPane().add(new JLabel());
		ren.frame.getContentPane().setLayout(gLayout);
		ren.frame.setVisible(true);
		ren.frame.setSize(ren.width, ren.height);
		if (mode == 1) {
			for (int i = 0; i < foregroundF.size(); i++) {
				try {
					ren.playVideo(ren.GreenBackground(foregroundF.get(i), backgroundF.get(i)));
					Thread.sleep(41); 
					ren.frame.getContentPane().revalidate(); 
   					ren.frame.getContentPane().repaint();
				} catch (Exception e) {

				}
			}
			//ren.renderImage(listOfFiles);
		} else {
			for (int i = 1; i < foregroundF.size(); i++) {
				try {
					ren.playVideo(ren.substractBackGround(foregroundF.get(i-1), foregroundF.get(i), backgroundF.get(i)));
					Thread.sleep(41); 
					ren.frame.getContentPane().revalidate(); 
   					ren.frame.getContentPane().repaint();
				} catch (Exception e) {

				}
			}
		}
	}
}
