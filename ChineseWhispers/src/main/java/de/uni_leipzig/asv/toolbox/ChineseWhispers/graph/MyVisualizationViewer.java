
package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;


/**
 * @author Rocco
 *
 */
public class MyVisualizationViewer extends VisualizationViewer{
	
	MyGraphGUI mgg;
		
	public MyVisualizationViewer(Layout layout, Renderer r,MyGraphGUI mgg){
		super(layout,r);
		this.mgg = mgg;
	}
	
	/**
	 * overrides inherited method<br>
	 * with additional functionality:,<br>
	 * text can be added on panel
	 */
	public void paint(Graphics g){
		try{
		    super.paint(g);
		}
		catch(NullPointerException e){}
		
		Font fon;
		//upper left in graph panel
		fon= new Font("Dialog",Font.BOLD,12);
		g.setFont(fon);
		g.setColor(Color.GRAY);
		if(mgg.isSub()) g.drawString("subgraph:",15,15);
		else g.drawString("graph:",15,15); 
		
		//g.drawLine(15,22,30,22);
		
		fon= new Font("Dialog",Font.BOLD,10);
		g.setFont(fon);
		g.drawString("nodes:     "+Integer.toString(this.getGraphLayout().getGraph().numVertices()),20,32);
		g.drawString("edges:     "+Integer.toString(this.getGraphLayout().getGraph().numEdges()),20,47);
		
		//g.drawLine(15,50,30,50);
		
		g.drawString("iteration:  "+mgg.getRenderer().current_iteration,20,65);
		g.drawString("option:  "+mgg.getAlgOpt()+" "+mgg.getAlgOptParam(),20,80);
		// MISSING: OTHER OPTIONS
                
                
		g.fillOval(16-2,20-2,4,4);
		//g.drawOval(16-2,85-2,4,4);
		g.drawLine(16,20,16,85);
		g.drawLine(16,85,25,85);
		
		if(!((MyAbstractGraph)this.getGraphLayout().getGraph()).all_edges){
			fon= new Font("Dialog",Font.BOLD,9);
			g.setFont(fon);
			g.setColor(Color.red);
			g.drawString("not all edges are drawn!",20,95);
		}
	} 
	
	 /**
	  * this method creates jpg image of view<br>
	  * called by grab-button
	  *
	  */
	 public void grab(){
	 		int width =  this.getSize().width; 
	 		int height = this.getSize().height; 
	 		Color bg = this.getBackground(); 
	 		 
	 		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR); 
	 		Graphics2D graphics = bi.createGraphics(); 
	 		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	 		graphics.setColor(bg); 
	 		graphics.fillRect(0,0, width, height); 
	 		
	 		this.paint(graphics); 
	 		graphics.setColor(Color.red);
	 		
	 		FontMetrics FM = graphics.getFontMetrics();
			int w = FM.stringWidth("ASV-TOOLBOX 2005");
			
	 		graphics.drawString("ASV-TOOLBOX 2005",width-w-2,height-2); 
	 		
	 		String pfad = "pic"+System.getProperty("file.separator")+"test.jpg";
	 		
	 		JFileChooser fc = new JFileChooser();
       	
            //fc.setAccessory(new ImagePreview(fc));
        	int returnVal = fc.showSaveDialog(this);
       	
       	    if (returnVal == JFileChooser.APPROVE_OPTION) {
               File file = fc.getSelectedFile();
               pfad = file.toString();
               //This is where a real application would open the file.
          
	 		try{ 
	 		    ImageIO.write(bi,"jpeg",new File(pfad)); 
	 		}catch(Exception e){e.printStackTrace();} 
            }
         }
}

