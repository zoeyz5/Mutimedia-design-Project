import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;


public class JPG2000 {

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

	private double[][] decode(double[][] x, int width, int height) {
		int mid = height/2;
		double[][] tmp1 = new double[height][width];
		double[][] tmp2 = new double[height][width];

		//row
		for (int i = 0; i < height; i++) {
			//L
			for (int j = 0, k = 0; j < mid; j++, k+=2) {
				tmp1[i][k] = (x[i][j]+x[i][j+mid]);
			}
			//H
			for (int j = 0, k = 1; j < mid; j++, k+=2) {
				tmp1[i][k] = (x[i][j]-x[i][j+mid]);
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
					x[i][j] = tmp2[i][j];
				} else {
					x[i][j] = 0;
				}
			}
		}
		return x;
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
	
	private double[][] DWT(double[][] data, int levels, int channel)
	{
		int w = data.length;
		int h = data[0].length;
		h = w = N;
		for (int i = 0; i < levels; i++)
		{
			data = encode(data, w, h);
			w >>= 1;
			h >>= 1;
			double temp [][] = new double[N][N];
			for (int j = 0; j < h; j++) {
				for (int k = 0; k < w; k++) {
					 temp[j][k] = data[j][k];
				}
			}
			if (channel == 0) newR.add(temp);
			else if (channel == 1) newG.add(temp);
			else newB.add(temp);
		}

		return data;
	} 

	private double[][] IDWT(double[][] data, int levels)
	{
		int w = data.length;
		int h = data[0].length;

		for (int i = 0; i < levels - 1; i++)
		{
			h >>= 1;
			w >>= 1;
		}

		for (int i = 0; i < levels; i++)
		{
			data = decode(data, w, h);
			h <<= 1;
			w <<= 1;
		}

		return data;
	}

	public void show2D(int len) {
		imgOne = new BufferedImage(len, len, BufferedImage.TYPE_INT_RGB);
		//System.out.println(len+ " : " + start + " : " + Y.length);
		int r, g, b;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				r = (int)r_channel[i][j];
				g = (int)g_channel[i][j];
				b = (int)b_channel[i][j];
				int val = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    			imgOne.setRGB(j,i,val);//it just like scan row by row from col 1 to n
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

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		JPG2000 j2 = new JPG2000();
		int mode = Integer.parseInt(args[1]);
		Boolean presentAll = false;
		if (mode < 0) {
			mode = 0;
			presentAll = true;	
		}
		// Read in the specified image
		j2.imgOne = new BufferedImage(j2.width, j2.height, BufferedImage.TYPE_INT_RGB);
		j2.readImageRGB(j2.width, j2.height, args[0], j2.imgOne);

		//if it is level 9 then just show
		if (mode == 9) {
			j2.show2D(j2.width);
			return;
		}

		if (presentAll) {
			//show original pic first
			//j2.show2D(j2.width);
			j2.DWT(j2.r_channel, 9, 0);
			j2.DWT(j2.g_channel, 9, 1);
			j2.DWT(j2.b_channel, 9, 2);
			for (int i = 0; i < 8; i++) {
				j2.r_channel = j2.IDWT(j2.newR.get(8-i), 9-i);
				j2.g_channel = j2.IDWT(j2.newG.get(8-i), 9-i);
				j2.b_channel = j2.IDWT(j2.newB.get(8-i), 9-i);
				j2.show2D(j2.width);
			}
			j2.readImageRGB(j2.width, j2.height, args[0], j2.imgOne);
			j2.show2D(j2.width);
		} else {
			j2.r_channel = j2.DWT(j2.r_channel, 9-mode, 0);
			j2.g_channel = j2.DWT(j2.g_channel, 9-mode, 1);
			j2.b_channel = j2.DWT(j2.b_channel, 9-mode, 2);
			//j2.show2D(j2.width);
			//System.out.println("I give you "+ j2.yy.length);
			j2.r_channel = j2.IDWT(j2.newR.get(8-mode), 9-mode);
			j2.g_channel = j2.IDWT(j2.newG.get(8-mode), 9-mode);
			j2.b_channel = j2.IDWT(j2.newB.get(8-mode), 9-mode);
			j2.show2D(j2.width);
		}
	}

}
