import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Negativ", "Graustufen", "Bin‰r","Bin‰r5","Quantisiert","Sepia","RestrictedColors","Floyd"};
						//"Rot-Kanal"

	public static void main(String args[]) {

		IJ.open("D:\\(_HTW\\2. Sem\\DIME\\Dime_3\\Bear.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zur√ºckschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Rot-Kanal")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						rn = colorGuard(rn);
						gn = colorGuard(gn); 
						bn = colorGuard(bn);
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Negativ")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = 255-g;
						int gn = 255-b;
						int bn = 255-r;
						
						/*
						 * int rn = 255-(r + g + b)/3;
						 * int gn = 255-(r + g + b)/3;
						 * int bn = 255-(r + g + b)/3;
						 */

						rn = colorGuard(rn);
						gn = colorGuard(gn); 
						bn = colorGuard(bn);
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = (r + g + b)/3;
						int gn = (r + g + b)/3;
						int bn = (r + g + b)/3;

						rn = colorGuard(rn);
						gn = colorGuard(gn); 
						bn = colorGuard(bn);
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Bin‰r")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int value = 0;
						//if((r+g+b)/3 >= 128) value = 255; //ugly
						if((r+g+b)/3 >= 88) value = 255;
						
						value = colorGuard(value);
						
						pixels[pos] = (0xFF<<24) | (value<<16) | (value<<8) | value;
						
					}
				}
			}
			
			if (method.equals("Bin‰r5")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						//int thresholds = colors - 1;
						int thresh0 = 204;
						int thresh1 = 153;
						int thresh2 = 102;
						int thresh3 = 51;
						
						
						int value = 0;
						int grey = (r+g+b)/3;
						if(grey >= thresh0) value = 255;
						if(grey <= thresh0 && grey >= thresh1) value = thresh0;
						if(grey <= thresh1 && grey >= thresh2) value = thresh1;
						if(grey <= thresh2 && grey >= thresh3) value = thresh2;
						if(grey <= thresh3) value = 0;
						
						value = colorGuard(value);
						
						pixels[pos] = (0xFF<<24) | (value<<16) | (value<<8) | value;
						
					}
				}
			}
			
			if (method.equals("Quantisiert")) {

				int fehler = 0;
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int value = 0;
						int greyWithFehler = (r+g+b)/3 + fehler; 
						
						if(greyWithFehler >= 128) value = 255;
						
						fehler = greyWithFehler - value; //value is either 0 or 255
							
						value = colorGuard(value);
						
						pixels[pos] = (0xFF<<24) | (value<<16) | (value<<8) | value;
						
					}
				}
			}
			
			if (method.equals("Sepia")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = (r + g + b)/3;
						int gn = (r + g + b)/3;
						int bn = (r + g + b)/3;
						
						//microsoft recommendet sepia
						/*rn = (int)((r * 0.393) + (g *0.769) + (b * 0.189));
						gn = (int)((r * 0.349) + (g *0.686) + (b * 0.168));
						bn = (int)((r * 0.272) + (g *0.534) + (b * 0.131));*/
						
						rn = (int)((r * 0.693) + (g *0.769) + (b * 0.189));
						gn = (int)((r * 0.449) + (g *0.686) + (b * 0.168));
						bn = (int)((r * 0.272) + (g *0.534) + (b * 0.131));

						rn = colorGuard(rn);
						gn = colorGuard(gn); 
						bn = colorGuard(bn);
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("RestrictedColors")) {
				
				int[][] colors = {new int[3], new int[3], new int[3], new int[3], new int[3], new int[3]};
				
				//set colors by hand, automatic later (https://labs.tineye.com/color/)
				//do same for x for y nested loop and check for each pixel if its already in list
				//if it is just increase [3] (r/g/b/ammount) else add color to the end of list
				//after the loop get the 6 colors with highest ammounts and put them in the color array
				
				colors[0] = setRGB(colors[0], 130, 97, 76);  //brown
				colors[1] = setRGB(colors[1], 56, 102, 135); //blue
				colors[2] = setRGB(colors[2], 27, 34, 34);   //black
				colors[3] = setRGB(colors[3], 80, 76, 71);   //dark grey
				colors[4] = setRGB(colors[4], 208, 207, 209);//white grey
				colors[5] = setRGB(colors[5], 173, 161, 152);//dusty grey
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;
						
						//compare all col arrays to current r,g,b
						int[] desiredCol = getSmallestDistanceCol(colors, r, g, b);

						//set depending on comparison
						int rn = desiredCol[0];
						int gn = desiredCol[1];
						int bn = desiredCol[2];

						rn = colorGuard(rn);
						gn = colorGuard(gn); 
						bn = colorGuard(bn);
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Floyd")) {

				float fehler[][] = new float[width][height];
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int value = 0;
						float greyWithFehler = (r+g+b)/3 + fehler[x][y]; 
						
						if(greyWithFehler >= 128) value = 255;
						
						float fehlerWert = greyWithFehler - value;
						
						pixels[pos] = (0xFF<<24) | (value<<16) | (value<<8) | value;
						
						
						// ------- Floyd -------------
						try {
							fehler[x+1][y]   = fehlerWert * (7f/16f);
							fehler[x-1][y]   = fehlerWert * (3f/16f);
							fehler[x][y+1]   = fehlerWert * (5f/16f);
							fehler[x+1][y+1] = fehlerWert * (1f/16f);
						}
						catch (Exception e){}
					}
				}
			}
		
			
		}
		
		private int[] setRGB(int[] arr, int r, int g, int b)
		{
			int[] tmp = {r, g, b};
			
			return tmp;
		}
		
		
		private int getDistance(int val1, int val2)
		{
			if(val1 > val2) return val1 - val2;
			
			else if(val1 < val2) return val2 - val1;	
			
			else return 0;	
		}
		
		
		private int[] getAllDistances(int[][] arr, int r, int g, int b, int size)
		{
			int[] allDistances = new int[size];
			
			for(int i = 0; i < size; i++)
			{
				int distanceR = getDistance(r, arr[i][0]);
				int distanceG = getDistance(g, arr[i][1]);
				int distanceB = getDistance(b, arr[i][2]);
				allDistances[i] = distanceR + distanceG + distanceB;
			}
			
			return allDistances;
		}
		
		
		private int[] getSmallestDistanceCol(int[][] arr, int r, int g, int b)
		{
			int[] allDistances = getAllDistances(arr, r, g, b, 6);
			
			int i = getMinIndex(allDistances); // find smallest i in allDistances
			
			return arr[i];
		}
		

		private int getMinIndex(int[] array) 
		{
			int minValue = array[0];
			int minIndex = 0;
		    
		    for (int i = 1; i < array.length; i++) 
		    {
		        if (array[i] < minValue) 
		        {
		            minValue = array[i];
		            minIndex = i;
		        }
		    }
		    return minIndex;
		}
		
		

		
		private int colorGuard(int value)
		{
			if(value>255) return 255;
			if(value<0) return 0;
			return value;
		}
		
	} // CustomWindow inner class
} 