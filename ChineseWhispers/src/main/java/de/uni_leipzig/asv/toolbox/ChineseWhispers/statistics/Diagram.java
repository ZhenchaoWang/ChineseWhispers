/** class Diagram.java
 *
 *  Paints a diagram for cluster size distributions. Not all options can be chosen directly in the graph window.
 *
 *
 *
 */


package de.uni_leipzig.asv.toolbox.ChineseWhispers.statistics;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerListModel;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;
/**
 * 
 * @author SG & RG
 *
 */
public class Diagram extends JFrame implements Runnable{ 

	private JFreeChart chart;
	private ChineseWhispers cw;
	private XYSeriesCollection dataset = new XYSeriesCollection();
	private ChartPanel chartPanel;
	private boolean isNewWindow=false;
	/** 
     * Creates a new chart.
     * @param cw a chinesewhisper object. 
     * */ 
    public Diagram(ChineseWhispers cwi) { 
        super("CW Diagram"); 
        this.isNewWindow=true;
        this.cw=cwi;
        this.getContentPane().setLayout(new BorderLayout());
        this.setJMenuBar(createMenuBar());
        
        Thread t = new Thread(new Runnable(){
            public void run(){
		        dataset = createDataset(cw); 
		        chart = createChart(dataset); 
            }
        });
        t.start();
        
        Timer timer = new Timer();
	    timer.schedule(new LookAtProgress(timer,t), 200, 2000); 

    }

    public void setFrameOnScreen(){
        chartPanel = new ChartPanel(chart,false,false,false,false,true); 
        chartPanel.setPreferredSize(new Dimension(500, 270));
        chartPanel.setMouseZoomable(false);
        getContentPane().add(chartPanel,BorderLayout.CENTER);
        getContentPane().add(createChooser(cw.getAlgOpt(),cw.getAlgOptParam()),BorderLayout.SOUTH);
        pack();
        RefineryUtilities.centerFrameOnScreen(this); 
        setVisible(true);
    }
    
    /** 
     * Creates dataset.
     * @return The dataset. 
     * */ 
    private XYSeriesCollection createDataset(ChineseWhispers cw) { 
         
    	String label;
    	if(cw.getAlgOptParam().length()!=0){
        	label ="Algorithm \""+cw.getAlgOpt()+" "+cw.getAlgOptParam()+"\" after "+cw.getIteration()+" iterations";
    	}
    	else{
        	label ="Algorithm \""+cw.getAlgOpt()+"\" after "+cw.getIteration()+" iterations";
    	}
    	
    	XYSeries xys = new XYSeries(label);
        Hashtable t = cw.show_clusters();
        int x,y;
        for(Enumeration e=t.keys();e.hasMoreElements();){
            x= ((Integer)e.nextElement()).intValue();
            y= ((Integer)(t.get(new Integer(x)))).intValue();
            xys.add(x,y);
        }
        dataset.addSeries(xys);
        return dataset; 
        } 
    /**
     * Creates a chart. 
     * @param dataset the dataset. 
     * @return The chart. 
     */ 
    private JFreeChart createChart(XYSeriesCollection dataset) { 
        // create the chart... 
        JFreeChart chart = ChartFactory.createXYLineChart( 
                "Cluster Distribution", // chart title 
                "Type", // domain axis label 
                "Value", // range axis label 
                dataset, // data 
                PlotOrientation.VERTICAL, // orientation 
                true, // include legend 
                true, // tooltips 
                false // urls 
                ); 
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);

        NumberAxis na_x = (NumberAxis) plot.getDomainAxis();
        NumberAxis na_y = (NumberAxis) plot.getRangeAxis();
        
        LogarithmicAxis la_x = new LogarithmicAxis("Clustersize");
        la_x.setRange(0.9,na_x.getRange().getUpperBound());
        la_x.setExpTickLabelsFlag(false);
        la_x.setLog10TickLabelsFlag(false);

        LogarithmicAxis la_y = new LogarithmicAxis("Number of Clusters");
        la_y.setRange(0.9,na_y.getRange().getUpperBound());
        la_y.setExpTickLabelsFlag(false);
        la_y.setLog10TickLabelsFlag(false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setLinesVisible(false);
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true);
        
        chart.getXYPlot().setRangeAxis(la_y);
        chart.getXYPlot().setDomainAxis(la_x);
        
        return chart; 
    } 
    
    /**
     * Creates the menu.
     * @return The menubar.
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
       
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("Diagram");
        menu.setMnemonic(KeyEvent.VK_A);
             
        menuItem = new JMenuItem("Save as jpg");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                grab();
}
        });
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Close");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        menu.add(menuItem);
        
        menuBar.add(menu);
        return menuBar;
	}
    
    /**
	  * Grabs the panels source and saves it as png.
	  *
	  */
	 public void grab(){
 		int width = this.getWidth()-(this.getInsets().left+this.getInsets().right);
 		int height = this.getHeight()-(this.getInsets().top+this.getInsets().bottom+this.getJMenuBar().getHeight()+alg_options.getHeight());
 		BufferedImage bi = chart.createBufferedImage(width,height); 
 		JFileChooser fc = new JFileChooser();
 		
 		int returnVal = fc.showSaveDialog(this);
 		String pfad = null;
   	    if (returnVal == JFileChooser.APPROVE_OPTION) {
   	    	File file = fc.getSelectedFile();
   	    	pfad = file.toString();
   	    	//This is where a real application would open the file.
   	    	try{ 
   	    		ImageIO.write(bi,"png",new File(pfad)); 
   	    	}catch(Exception e){e.printStackTrace();} 
   	    }
	 }
	 
	 private JCheckBox top, vote, dist_log, dist_nolog;
	 private LinkedList L5;
	 private JSpinner vote_value;
	 private JPanel alg_options;
	 private JButton refresh;
	 
	/**
	 * Creates a chooser panel.
	 * @param AlgOpt describes the algorithm.
	 * @param AlgOptParam describes the algorithm param.
	 * @return the chooser panel for several algorithms. 
	 */ 
	 public JPanel createChooser(String AlgOpt, String AlgOptParam){

	    alg_options = new JPanel(new FlowLayout(FlowLayout.CENTER));
		alg_options.setBorder(new LineBorder(Color.BLACK,0));
		
		refresh = new JButton("refresh");
		refresh.setEnabled(true);
		refresh.setMargin(new Insets(1,1,1,1));
		refresh.setFont(new Font("Dialog",Font.PLAIN,9));
		refresh.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	refresh();    
		    }
		});

		JToolBar radios = new JToolBar();
		radios.setMargin(new Insets(0,0,0,0));
		radios.setFloatable(false);
	
		top = new JCheckBox("top",false);
		top.setMargin(new Insets(1,1,1,1));
		top.setActionCommand("top");
	
		dist_log = new JCheckBox("dist log",false);
		dist_log.setMargin(new Insets(1,1,1,1));
		dist_log.setActionCommand("dist log");
	
		dist_nolog = new JCheckBox("dist nolog",false);
		dist_nolog.setMargin(new Insets(1,1,1,1));
		dist_nolog.setActionCommand("dist nolog");
	
		vote = new JCheckBox("vote",false);
		vote.setMargin(new Insets(1,1,1,1));
		vote.setActionCommand("vote");
	
		L5 = new LinkedList();
		L5.add(" 0.0 ");
		L5.add(" 0.1 ");
		L5.add(" 0.2 ");
		L5.add(" 0.3 ");
		L5.add(" 0.4 ");
		L5.add(" 0.5 ");
                L5.add(" 0.6 ");
		L5.add(" 0.7 ");
		L5.add(" 0.8 ");
		L5.add(" 0.9 ");
		L5.add(" 1.0 ");
		
		vote_value = new JSpinner(new SpinnerListModel(L5) );

		JPanel ph = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ph.add(vote_value);
	
		radios.add(new JLabel(" Algorithm-Options: "));
		radios.add(top);
		radios.add(dist_log);
		radios.add(dist_nolog);
		radios.add(vote);
		radios.add(ph);
		radios.add(new JLabel("  "));
		radios.add(refresh);
		alg_options.add(radios,BorderLayout.WEST);

		if(AlgOpt.equals("top")){
			top.setSelected(true);
		}
		if(AlgOpt.equals("dist")){
			if(AlgOptParam.equals("log")){
				dist_log.setSelected(true);
			}
			else{
				dist_nolog.setSelected(true);
			}
		}
		if(AlgOpt.equals("vote")){
			vote.setSelected(true);
			vote_value.setValue(" "+AlgOptParam+" ");
		}

		return alg_options;
	}

	/**
	 * Refreshes the Chart.
	 *
	 */
	public void refresh(){
	    
	    refresh.setEnabled(false);
	    top.setEnabled(false);
	    vote.setEnabled(false);
	    dist_log.setEnabled(false);
	    dist_nolog.setEnabled(false);
	    vote_value.setEnabled(false);
	    
	    dataset = new XYSeriesCollection();
		
	    Thread t = new Thread(new Runnable(){
	        public void run(){
				if(top.isSelected()){
					cw.setCWParameters(cw.getMinWeight(),"top","",cw.getKeepValue(),cw.getMutOpt(),cw.getMutValue(),cw.getUpdateOpt(),cw.getIteration(),false);
                                        cw.runAlgorithm();
					createDataset(cw);
				}
				if(dist_log.isSelected()){
					cw.setCWParameters(cw.getMinWeight(),"dist","log",cw.getKeepValue(),cw.getMutOpt(),cw.getMutValue(),cw.getUpdateOpt(),cw.getIteration(),false);
                                        cw.runAlgorithm();
					createDataset(cw);
				}
				if(dist_nolog.isSelected()){
                                    	cw.setCWParameters(cw.getMinWeight(),"dist","nolog",cw.getKeepValue(),cw.getMutOpt(),cw.getMutValue(),cw.getUpdateOpt(),cw.getIteration(),false);
                                        cw.runAlgorithm();
					createDataset(cw);
				}
				if(vote.isSelected()){
                                    	cw.setCWParameters(cw.getMinWeight(),"vote",((String) vote_value.getValue()).trim(),cw.getKeepValue(),cw.getMutOpt(),cw.getMutValue(),cw.getUpdateOpt(),cw.getIteration(),false);
                                        cw.runAlgorithm();
					createDataset(cw);
				}
	        }
	    });
	    t.start();
        Timer timer = new Timer();
	    timer.schedule(new LookAtProgress(timer,t), 200, 2000); 
	}
	/**
	 * There's nothing to do.
	 */
	public void run(){
	    //there is nothing to do
	}
    /**
     * @author seb
     */
	class LookAtProgress extends TimerTask{
    	private Timer tImer;
    	private Thread tHread;
    	
    	/**
    	 * Handles threads for cw-algorithm and progess-bar.
    	 * @param tImer The Timer to cancel when ready.
    	 * @param tHread The Thread to watch for.
    	 */
    	public LookAtProgress(Timer tImer, Thread tHread){
    		this.tImer = tImer;
    		this.tHread=tHread;
    	}
    	
    	/**
    	 * Checks if cw-algorithm still works. If ready, creates new threads for graphs <br>
    	 * and/or diagramms and sets the progress-bar visible, valued if ready not visible.
    	 */
    	public void run() {

    	    if(isNewWindow){
    	        if(tHread.isAlive()){
    	        }
    	        else{
    	            tImer.cancel();
    	            setFrameOnScreen();
    	            isNewWindow=false;
    	        }
    	    }
    	    else if(tHread.isAlive()){
    	        //do something
    	    }
    		else{
    		    tImer.cancel();
    			chart = createChart(dataset); 
    			getContentPane().remove(chartPanel);
    	        chartPanel = new ChartPanel(chart,false,false,false,false,true); 
    	        chartPanel.setPreferredSize(new Dimension(500, 270));
    	        chartPanel.setMouseZoomable(false);
    	        getContentPane().add(chartPanel,BorderLayout.CENTER);
    	        setVisible(true);
    	        refresh.setEnabled(true);
    		    refresh.setEnabled(true);
    		    top.setEnabled(true);
    		    vote.setEnabled(true);
    		    dist_log.setEnabled(true);
    		    dist_nolog.setEnabled(true);
    		    vote_value.setEnabled(true);
    		}
    	}
    }
}




