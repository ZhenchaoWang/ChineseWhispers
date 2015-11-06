
package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import edu.uci.ics.jung.visualization.BirdsEyeVisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * @author Rocco
 *
 * Der ZoomDialog
 */
public class MyZoomDialog extends JDialog{
	
	BirdsEyeVisualizationViewer  bird;
	Container content;
	VisualizationViewer vv;
	int scaleWindow;
	
	
	public MyZoomDialog(JFrame frame){
		super(frame);
	}
	
	
	public void showDialog(){
		
		
	setResizable(false);
    
           
    content = getContentPane();
    
    // create the BirdsEyeView for zoom/pan
    
    float scale1 = (1000f*0.25f)/(float)scaleWindow;
    bird = new BirdsEyeVisualizationViewer(vv, scale1, scale1);
    
    JButton reset = new JButton("100%");
    // 'reset' unzooms the graph via the Lens
    reset.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
           bird.resetLens();
          
        }
    });
    JButton plus = new JButton("+");
    plus.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            vv.scale(1.1f, 1.1f);
        }
    });
    JButton minus = new JButton("-");
    minus.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            vv.scale(0.9f, 0.9f);
        }
    });
    JButton help = new JButton("Help");
    help.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String zoomHelp = "<html><center>Drag the rectangle to pan<p>"+
            "Drag one side of the rectangle to zoom</center></html>";
            JOptionPane.showMessageDialog(null, zoomHelp);
        }
    });
    
    JPanel main = new JPanel(new BorderLayout());
    
            
    //bird.setBackground(Color.white);
    bird.setBorder(new LineBorder(Color.BLACK,2));
    
    JPanel controls = new JPanel(new GridLayout(1,3));
    controls.add(plus);
    controls.add(reset);
    controls.add(minus);
   
   //controls.add(help);
    
    main.add(bird, BorderLayout.CENTER);
    main.add(controls,BorderLayout.SOUTH);
    
    content.add(main);
    setVisible(true);
	pack();
    }
	
	/**
	 * @param scaleWindow The scaleWindow to set.
	 */
	public void setScaleWindow(int scaleWindow) {
		this.scaleWindow = scaleWindow;
	}
	
	/**
	 * @param vv The vv to set.
	 */
	public void setVV(VisualizationViewer vv) {
		this.vv = vv;
	}
}
