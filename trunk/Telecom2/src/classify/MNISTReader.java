package classify;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class MNISTReader {
	
	
	public static final String labels_f = "G:/DATASET/SVM/MNIST/train-labels.idx1-ubyte";
	public static final String img_f = "G:/DATASET/SVM/MNIST/train-images.idx3-ubyte";
	
	public static int width = 0;
	public static int height = 0;
	

	public static void main(String[] args) throws Exception {
		
		int[] labels = readLabels(labels_f);
		List<int[]> imgs = readImages(img_f);
				
		String l = "";
		for(int i=5;i<15;i++)
			l = l + labels[i] + ",";
		createImg(l,imgs.subList(5, 15));
			
		System.out.println("Done!");
	}
		
	public static int[] readLabels(String file) throws Exception {
		// read training label file
		DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		int magic = d.readInt();
		int n_labels = d.readInt();
		//System.out.println(magic+" "+n_labels);
		int[] labels = new int[n_labels];
		for(int i=0;i<labels.length;i++)
			labels[i] = d.readUnsignedByte();
		d.close();
		return labels;
	}
	
	public static List<int[]> readImages(String file) throws Exception {
		// read training image file
		DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		int magic = d.readInt();
		int n_img = d.readInt();
		int rows = d.readInt();
		int columns = d.readInt();
		//System.out.println(magic+" "+n_img+" "+rows+" "+columns);
		
		List<int[]> imgs = new ArrayList<int[]>();
		for(int i=0;i<n_img;i++) {
			int[] img = new int[rows*columns];
			for(int j=0; j<img.length;j++) {
				img[j] = d.readUnsignedByte();
			}
			imgs.add(img);
		}
		d.close();
		
		width = columns;
		height = rows;
		
		return imgs;
	}
	
	public static void createImg(String title, List<int[]> imgs) throws Exception {	
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		for(int[] img: imgs) {
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			int[] rgb = new int[img.length];
			for(int i=0;i<rgb.length;i++) 
				rgb[i] = ((255-img[i])<<16)|((255-img[i])<<8)|(255-img[i]);
			
			bi.setRGB(0, 0, width, height, rgb, 0, width);
			frame.getContentPane().add(new JLabel(new ImageIcon(bi)));
		}
		frame.setTitle(title);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
