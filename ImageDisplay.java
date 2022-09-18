
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JFrame frame2;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage imgOne;
	BufferedImage imgTwo = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	BufferedImage imgThree;

	static int WIDTH = 1920; // default image width and height
	static int HEIGHT = 1080;

    static int Y;
    static int U;
    static int V;
	static float w;
	static float h;
    static int A;

	static int rgbImage[] = new int[3*WIDTH*HEIGHT];
    static double yuvImage[] = new double[3*WIDTH*HEIGHT];

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
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
					//byte a = 0;
					byte r = bytes[ind];
					if(r<0) {
						rgbImage[(y*width+ x)*3] = r+256;
					} else {
						rgbImage[(y*width + x)*3] = r;
					}

					byte g = bytes[ind+height*width];
					if(g<0) {
						rgbImage[(y*width + x)*3 + 1] = g+256;
					} else {
						rgbImage[(y*width + x)*3 + 1] = g;
					}

					byte b = bytes[ind+height*width*2]; 
					if(b<0) {
						rgbImage[(y*width + x)*3 + 2] = b+256;
					} else {
						rgbImage[(y*width + x)*3 + 2] = b;
					}

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);

					//convert to YUV
					double changedY = RGBtoY(rgbImage[(y*width + x)*3], rgbImage[(y*width + x)*3 + 1], rgbImage[(y*width + x)*3 + 2]);
					yuvImage[(y*width + x)*3] = changedY;
					
					double changedU = RGBtoU(rgbImage[(y*width + x)*3], rgbImage[(y*width + x)*3 + 1], rgbImage[(y*width + x)*3 + 2]);
					yuvImage[(y*width + x)*3+1] = changedU;
					
					double changedV = RGBtoV(rgbImage[(y*width + x)*3], rgbImage[(y*width + x)*3 + 1], rgbImage[(y*width + x)*3 + 2]);
					yuvImage[(y*width + x)*3+2] = changedV;

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

	public void showIms(String[] args){

		// Read a parameter from command line
		Y = Integer.parseInt(args[1]);
		U = Integer.parseInt(args[2]);
		V = Integer.parseInt(args[3]);
		w = Float.parseFloat(args[4]);
		h = Float.parseFloat(args[5]);
		A = Integer.parseInt(args[6]);

		// Read in the specified image
		imgOne = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		readImageRGB(WIDTH, HEIGHT, args[0], imgOne);

		//calculate new YUV
		upSample();

		YUVtoRGB();

		scale(w,h);

		// Use label to display the image
		frame = new JFrame();
		frame2 = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		frame2.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));
		lbIm2 = new JLabel(new ImageIcon(imgThree));

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame2.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);

		frame2.pack();
		frame2.setVisible(true);
	}

	public void YUVtoRGB(){
		for(int y = 0; y < HEIGHT; y++){
           
			for(int x = 0; x < WIDTH; x++){
				
				int i = y*WIDTH + x;
				
				byte r, g, b;
				//eliminate data that smaller than 0 or larger than 255
				rgbImage[3*i] = (int)Math.rint(YUVtoR(yuvImage[3*i], yuvImage[3*i+1], yuvImage[3*i+2]));
				if(rgbImage[3*i] < 0) {
					rgbImage[3*i] = 0;
				} else if(rgbImage[3*i] > 255) {
					rgbImage[3*i] = 255;
				}
				
				rgbImage[3*i +1] = (int)Math.rint(YUVtoG(yuvImage[3*i], yuvImage[3*i+1], yuvImage[3*i+2]));
				if(rgbImage[3*i + 1] < 0) {
					rgbImage[3*i + 1] = 0;
				} else if(rgbImage[3*i + 1] > 255) {
					rgbImage[3*i + 1] = 255;
				}
				
				rgbImage[3*i +2] = (int)Math.rint(YUVtoB(yuvImage[3*i], yuvImage[3*i+1], yuvImage[3*i+2]));
				if(rgbImage[3*i + 2] < 0) {
					rgbImage[3*i + 2] = 0;
				} else if(rgbImage[3*i + 2] > 255) {
					rgbImage[3*i + 2] = 255;
				}
		
				//eliminate data that smaller than -128 or larger than 127
				if(rgbImage[3*i] > 127) {
					r = (byte)(rgbImage[3*i] - 256);
				} else {
					r = (byte)rgbImage[3*i];
				}
				
				if(rgbImage[3*i + 1] > 127) {
					g = (byte)(rgbImage[3*i + 1] - 256);
				} else {
					g = (byte)rgbImage[3*i + 1];
				}
				
				if(rgbImage[3*i + 2] > 127) {
					b = (byte)(rgbImage[3*i + 2] - 256);
				} else {
					b = (byte)rgbImage[3*i + 2];
				}
				
				//set pixel for display output image
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				imgTwo.setRGB(x,y,pix);
			}
		}
	}

	public static void upSample() {
        for(int y = 0; y < HEIGHT; y++){
            if(Y > 1) {
                int x;
                for(x = 0; x < WIDTH - Y; x+=Y){
                    //average
                    for(int z=x+1; z<x+Y; z++) {
                        yuvImage[3*(WIDTH*y + z)] = (yuvImage[3*(WIDTH*y + x)] + yuvImage[3*(WIDTH*y + x+Y)])/2;
                    }
                }
                x++;
                //remained value
                for(;x<WIDTH; x++) {
                    yuvImage[3*(WIDTH*y + x)] = yuvImage[3*(WIDTH*y + WIDTH - Y)];
                }
            }
            
            if(U > 1) {
                int x;
                for(x = 0; x < WIDTH - U; x+=U){
                    for(int z=x+1; z<x+U; z++) {
                        yuvImage[3*(WIDTH*y + z) + 1] = (yuvImage[3*(WIDTH*y + x) + 1] + yuvImage[3*(WIDTH*y + x+U) + 1])/2;
                    }
                }
                x++;
                for(;x<WIDTH; x++) {
                    yuvImage[3*(WIDTH*y + x) + 1] = yuvImage[3*(WIDTH*y + WIDTH - U) + 1];
                }
            }
            
            if(V > 1) {
                int x;
                for(x = 0; x < WIDTH - V; x+=V){
                    for(int z=x+1; z<x+V; z++) {
                        yuvImage[3*(WIDTH*y + z) + 2] = (yuvImage[3*(WIDTH*y + x) + 2] + yuvImage[3*(WIDTH*y + x+V) + 2])/2;
                    }
                }
                x++;
                for(;x<WIDTH; x++) {
                    yuvImage[3*(WIDTH*y + x) + 2] = yuvImage[3*(WIDTH*y + WIDTH - V) + 2];
                }
            }
        }
    }

	public void scale(float scalew, float scaleh){
		int new_width = (int)Math.round(1920*scalew);
		int new_height = (int)Math.round(1080*scaleh);

		imgThree = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);

		for (int x=0; x<new_width; x++){
			for (int y=0; y<new_height; y++){

				int mapX=(int)Math.round(x/scalew);
				int mapY=(int)Math.round(y/scaleh);
				if (mapX==WIDTH){
					mapX--;
				}
				if (mapY==HEIGHT){
					mapY--;
				}
				if (A==0){ //no aliasing
					imgThree.setRGB(x,y,imgTwo.getRGB(mapX,mapY));
				}
				else{
					//anti aliasing 
					int averaged_rgb=0;
					averaged_rgb=antiali(mapX,mapY);
					imgThree.setRGB(x,y,averaged_rgb);
				}
			}
		}
	}

	public int antiali(int x,int y){
		int counter=0;
		int tmpRGB=0;
		int tmpR=0;
		int tmpG=0;
		int tmpB=0;

		for (int i=-3;i<=3;i++){
			for (int j=-3;j<=3;j++){

				if (x+i>=0 && x+i<WIDTH){
					if (y+j>=0 && y+j<HEIGHT){
						tmpRGB=imgTwo.getRGB(x+i,y+j);
						tmpB+=tmpRGB & 0xff;
						tmpG+=tmpRGB >> 8 & 0xff;
						tmpR+=tmpRGB >> 16 & 0xff;

						counter++;
					}
				}
			}
		}
		int pix = 0xff000000 | ((tmpR/counter & 0xff) << 16) | ((tmpG/counter & 0xff) << 8) | (tmpB/counter & 0xff);


		return pix;

	}

	public static double RGBtoY(int r, int g, int b) {
		return (0.299*r + 0.587*g + 0.114*b);
	}
   
	public static double RGBtoU(int r, int g, int b) {
		return (0.596*r - 0.274*g - 0.322*b);
	 }
	 
	public static double RGBtoV(int r, int g, int b) {
 		return (0.211*r - 0.523*g + 0.312*b);
	 }
	 
	public static double YUVtoR(double y, double u, double v){
		return (y + 0.956*u + 0.621*v);
	 }
	 
	public static double YUVtoG(double y, double u, double v) {
		return (y - 0.272*u - 0.647*v);
	 }
	 
	public static double YUVtoB(double y, double u, double v) {
		return (y - 1.106*u + 1.703*v);
	 }

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
