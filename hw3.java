import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;


public class ImageCompression {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 512; // default image width and height
	int height = 512;
	int N = width;

	ArrayList<double[][]>newR = new ArrayList<>();
	ArrayList<double[][]>newG = new ArrayList<>();
	ArrayList<double[][]>newB = new ArrayList<>();

	double r_channel[][] = new double[height][width];
	double g_channel[][] = new double[height][width];
	double b_channel[][] = new double[height][width];

	private double[][] encode(double[][] matrix, int width, int height) {
		int mid = width/2;
		double[][] tmp1 = new double[height][width];
		double[][] tmp2 = new double[height][width];
		//row
		for (int i = 0; i < height; i++) {
			//L
			for (int j = 1, k = 0; j < width; j+=2, k++) {
				tmp1[i][k] = (matrix[i][j]+matrix[i][j-1])/2;
			}
			//H
			for (int j = 1, k = mid; j < width; j+=2, k++) {
				tmp1[i][k] = (matrix[i][j-1]-matrix[i][j])/2;
			}
		}
		//col
		for (int j = 0; j < width; j++) {
			//L
			for (int i = 1, k = 0; i < height; i+=2, k++) {
				tmp2[k][j] = (tmp1[i][j]+tmp1[i-1][j])/2;
			}
			//H
			for (int i = 1, k = mid; i < height; i+=2, k++) {
				tmp2[k][j] = (tmp1[i][j]-tmp1[i-1][j])/2;
			}
		}
		//filter
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i < height && j < width) {
					matrix[i][j] = tmp2[i][j];
				} else {
					matrix[i][j] = 0;
				}
			}
		}
		return matrix;
	}

	private double[][] decode(double[][] matrix, int width, int height) {
		int mid = height/2;
		double[][] tmp1 = new double[height][width];
		double[][] tmp2 = new double[height][width];

		//row
		for (int i = 0; i < height; i++) {
			//L
			for (int j = 0, k = 0; j < mid; j++, k+=2) {
				tmp1[i][k] = (matrix[i][j]+matrix[i][j+mid]);
			}
			//H
			for (int j = 0, k = 1; j < mid; j++, k+=2) {
				tmp1[i][k] = (matrix[i][j]-matrix[i][j+mid]);
			}
		}
		//col
		for (int j = 0; j < width; j++) {
			//L
			for (int i = 0, k = 0; i < mid; i++, k+=2) {
				tmp2[k][j] = (tmp1[i][j]+tmp1[i+mid][j]);
			}
			//H
			for (int i = 0, k = 1; i < mid; i++, k+=2) {
				tmp2[k][j] = (tmp1[i][j]-tmp1[i+mid][j]);
			}
		}
		//filter
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i < height && j < width) {
					matrix[i][j] = tmp2[i][j];
				} else {
					matrix[i][j] = 0;
				}
			}
		}
		return matrix;
	}

	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int len = width*height;
			int frameLength = len*3;
			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			byte[] bytes = new byte[frameLength];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int j = 0; j < width; j++)
				{
					r_channel[y][j] = bytes[ind]&0xff;
					g_channel[y][j] = bytes[ind+height*width]&0xff;
					b_channel[y][j] = bytes[ind+height*width*2]&0xff;

					ind++;
				}
			}
			//showIms(0, len, 512);
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
	
	private double[][] DWT(double[][] matrix, int levels, int channel)
	{
		int w = width;
		int h = height;
		for (int i = 0; i < levels; i++)
		{
			matrix = encode(matrix, w, h);

			double tmp [][] = new double[N][N];
			for (int j = 0; j < h/2; j++) {
				for (int k = 0; k < w/2; k++) {
					 tmp[j][k] = matrix[j][k];
				}
			}
			if (channel == 0) newR.add(tmp);
			else if (channel == 1) newG.add(tmp);
			else newB.add(tmp);
		}

		return matrix;
	} 

	private double[][] IDWT(double[][] matrix, int levels)
	{
		int w = width;
		int h = height;

		for (int i = 0; i < levels - 1; i++)
		{
			h /= 2;
			w /= 2;
		}

		for (int i = 0; i < levels; i++)
		{
			matrix = decode(matrix, w, h);
			h *= 2;
			w *= 2;
		}

		return matrix;
	}

	public void showIms(int width) {
		imgOne = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
		int r, g, b;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < width; j++) {
				r = (int)r_channel[i][j];
				g = (int)g_channel[i][j];
				b = (int)b_channel[i][j];
				int val = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    			imgOne.setRGB(j,i,val);
			}
		}
		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		ImageCompression ren = new ImageCompression();
		int L = Integer.parseInt(args[1]);

		// Read in the specified image
		ren.imgOne = new BufferedImage(ren.width, ren.height, BufferedImage.TYPE_INT_RGB);
		ren.readImageRGB(ren.width, ren.height, args[0], ren.imgOne);

		if (L == -1){
			ren.r_channel = ren.DWT(ren.r_channel, 9, 0);
			ren.g_channel = ren.DWT(ren.g_channel, 9, 1);
			ren.b_channel = ren.DWT(ren.b_channel, 9, 2);
			for (int i = 0; i <= 8; i++) {
				ren.r_channel = ren.IDWT(ren.newR.get(8-i), 9-i);
				ren.g_channel = ren.IDWT(ren.newG.get(8-i), 9-i);
				ren.b_channel = ren.IDWT(ren.newB.get(8-i), 9-i);
				ren.showIms(ren.width);
				//System.out.println("level " + i);
			}
			ren.readImageRGB(ren.width, ren.height, args[0], ren.imgOne);
			ren.showIms(ren.width);
		}
		else if(L == 9) {
			ren.showIms(ren.width);
		}
		else {
			ren.r_channel = ren.DWT(ren.r_channel, 9-L, 0);
			ren.g_channel = ren.DWT(ren.g_channel, 9-L, 1);
			ren.b_channel = ren.DWT(ren.b_channel, 9-L, 2);
			ren.r_channel = ren.IDWT(ren.newR.get(8-L), 9-L);
			ren.g_channel = ren.IDWT(ren.newG.get(8-L), 9-L);
			ren.b_channel = ren.IDWT(ren.newB.get(8-L), 9-L);
			ren.showIms(ren.width);
		}
	}
}
