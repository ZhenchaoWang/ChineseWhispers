/*
 *
 *This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *
 *
 **/

package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerListModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uni_leipzig.asv.coocc.*;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.ZoomPanGraphMouse;


public class MyGraphGUI extends JFrame implements ChangeListener, ActionListener, Runnable{
	
    
     JFrame help = this;

     private Timer timer;   

     private MyZoomDialog dialog;

     private FRLayout layout;
     private MyVisualizationViewer vv;
     private MyVisualizationViewer vv_save;
     private GraphZoomScrollPane MyGraphZoomScrollPane;
     private GraphZoomScrollPane GZSP_save;
     private PopupGraphMouse gm;

     private JPanel panel;
     private JPanel upper_bar;
     private JPanel lower_bar;
     private JPanel alg_options;
     private JToolBar upper_bar1;

     private JPanel waitpanel;

     private Container cp;

     // buttons for upper bar
     private JButton zoomer;
     private JButton grab;
     private JButton stop;
     private JButton add;
     private JButton showButton;
     private JButton clear;
     private JButton refresh;
     private JButton labels;
     private JButton plus;
     private JButton minus;

     // buttons for lower bar
     private ButtonGroup Alg_param;	 
     private ButtonGroup Mut_param;
     private ButtonGroup Update_param;

     private JRadioButton top; 
     private JRadioButton dist_log;
     private JRadioButton dist_nolog;
     private JRadioButton vote;
     private JRadioButton dec;
     private JRadioButton constant;
     private JRadioButton stepwise;
     private JRadioButton continuous;    


     private JToolBar radios_alg_opt1;
     private JToolBar radios_alg_opt2;

     private JSpinner vote_value;
     private JSpinner keep_value;
     private JSpinner mut_value;


     private JLabel percent;

     //progress bar and stop button
     private JProgressBar bar;

     private boolean stopFR;
     private boolean sub;
     private boolean only_sub;
     private boolean only_add = false;
     private boolean is_refreshed = false;
     private boolean is_closed;

     private BinFileStrCol nodes; 
     private BinFileMultCol edges;

     //Slider for coloring
     private  JSlider slider;

     private String alg_param_now;	
     private String vote_value_now;	
     private double keep_value_now;	
     private String mut1_now;	
     private double mut_value_now;
     private String update_now;
	 
     //max iterations
     private int nr_of_iterations;
     //iteration steps
     private int fr_iteration;

     // number of display edges
     private int max_display_edges;
     //resolution of graph window
     private int scaleWindow;
     
     // hub value for excluding nodes of high or little degree
     private int hub;

     private MyRenderer pr;
     private ChineseWhispers cw;
     private MyAbstractGraph graph;
     private MyAbstractGraph subgraph;
     private JLabel wait = new JLabel("wait ...");

     public MyGraphGUI(boolean sub,ChineseWhispers cw,int maxDisplayEdges, int scale,int maxFR ,int hub) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event){
                close();     
            }
        });
     
        setLocation(100,100);
        // window size
        setSize(650,650);
        this.fr_iteration= maxFR;
        this.max_display_edges 	= maxDisplayEdges;
        this.scaleWindow = scale;
        this.cw 	= cw;
        this.nodes      = cw.getNodes();
        this.edges      = cw.getEdges();
        this.sub	= sub;	

        this.nr_of_iterations = cw.getIteration();
        this.hub = hub;

        pr = new MyRenderer();
     } // end MyGraphGUI
	 
	 
	 
     private void init(boolean test){
		
        if(!sub) setTitle("The Graph");
        else     setTitle("The SubGraph");

        stopFR = false;
        bar = new JProgressBar( 0, fr_iteration );

        //options for algorithm options panel
        alg_param_now = cw.getAlgOpt();	
        mut1_now = cw.getMutOpt();	
        update_now = cw.getUpdateOpt();	

        vote_value_now= cw.getAlgOptParam();
        keep_value_now= cw.getKeepValue();
        mut_value_now= cw.getMutValue();

	 	
        //---------------------------------Buttons und slider ---
        //stop button
	stop = new JButton("stop");
        stop.setFont(new Font("Dialog",Font.PLAIN,10));
        stop.setMargin(new Insets(0,15,0,15));
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	stop();
                    
            }
        });
        
        // iterations slider
        slider = new JSlider();
        slider.setMinimum(0);
        slider.setMaximum(nr_of_iterations);
        slider.setMajorTickSpacing(slider.getMaximum()/2);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.setMinorTickSpacing(1);
        slider.setValue(pr.getIter());
        slider.setSnapToTicks(true);
        slider.setEnabled(false);
        slider.addChangeListener(this);
        
        //Grab button
        grab = new JButton("Grab");
        grab.setMargin(new Insets(1,1,1,1));
        grab.setFont(new Font("Dialog",Font.PLAIN,9));
        grab.setEnabled(false);
        grab.setToolTipText("Saves a jpg picture of the current view");	
        grab.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vv.grab();               
            }
        });
       
        
        //Button Zoomer
        zoomer = new JButton("Zoom");
        zoomer.setMargin(new Insets(1,1,1,1));
        zoomer.setFont(new Font("Dialog",Font.PLAIN,9));
        zoomer.setEnabled(false);
        zoomer.setToolTipText("opens a zoom window");	
        zoomer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	dialog = new MyZoomDialog(help);
            	dialog.setScaleWindow(getScaleWindow());
            	dialog.setVV(vv);
            	dialog.showDialog();
                    
            }
        });
        
        //add button
        add = new JButton("add");
        add.setMargin(new Insets(1,1,1,1));
        add.setFont(new Font("Dialog",Font.PLAIN,9));
        add.setEnabled(false);
        add.setToolTipText("Adds a node to the subgraph");	
        add.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
            	if(dialog!= null) dialog.setVisible(false); 
            	addSub();
                    
            }
        });

        //show button
        showButton = new JButton(); 
        if(!sub) { showButton.setText("show"); showButton.setToolTipText("shows subgraph");}
        else 	 { showButton.setText("close"); showButton.setToolTipText("closes subgraph");}
        showButton.setFont(new Font("Dialog",Font.PLAIN,9));
        showButton.setMargin(new Insets(1,1,1,1));
        showButton.setEnabled(false);
        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(dialog!= null) dialog.setVisible(false);

            	//as show button
                if(!sub){
                	only_add =false;
                	vv_save = vv;
                	GZSP_save = MyGraphZoomScrollPane;
                	showSub();
                	setTitle("The SubGraph");
                	showButton.setText("close");
                        showButton.setToolTipText("closes subgraph");
                }
                //as close button
                else{
                	only_add =true;
                	sub = false;
                	vv = vv_save;
                	vv.repaint();
                
                	panel.remove(MyGraphZoomScrollPane);
                	MyGraphZoomScrollPane = GZSP_save;
                	panel.add(MyGraphZoomScrollPane, BorderLayout.CENTER); 
                	panel.repaint();
                	
                	setTitle("The Graph");
                	showButton.setText("show");
                        showButton.setToolTipText("shows subgraph");
                }
                
                    
            }
        });
        
        //clear button
        clear= new JButton("clear");
        clear.setFont(new Font("Dialog",Font.PLAIN,9));
        clear.setMargin(new Insets(1,1,1,1));
        clear.setEnabled(false);
        clear.setToolTipText("clears subgraph");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(dialog!= null) dialog.setVisible(false); 
            	clear();
            	if(sub) {showSub(); only_add=false;}
            }
        });
        
        //percent button
        percent = new JLabel();	
        
        //labels
        labels = new JButton("node labels");
        labels.setFont(new Font("Dialog",Font.PLAIN,9));
        labels.setMargin(new Insets(1,1,1,1));
        labels.setEnabled(true);
        labels.setToolTipText("shows/hides node labels");
        labels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pr.changeShapeNode(); vv.repaint();
            }
        });

        //plus
        plus = new JButton("+");
        plus.setFont(new Font("Dialog",Font.PLAIN,9));
        plus.setMargin(new Insets(1,1,1,1));
        plus.setEnabled(true);
        plus.setToolTipText("increases label size");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeBiggerVertex();
            }
        });

        //minus
        minus = new JButton("-");
        minus.setFont(new Font("Dialog",Font.PLAIN,9));
        minus.setMargin(new Insets(1,1,1,1));
        minus.setEnabled(true);
        minus.setToolTipText("decreases label size");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeSmallerVertex();
            }
        });

        //layout *************************************************
    	upper_bar1 = new JToolBar();
    	upper_bar1.setFloatable(false);
    	upper_bar1.add(new JLabel(" view: "));
        upper_bar1.add(grab);
        upper_bar1.add(zoomer);
     	upper_bar1.add(new JLabel("  subgraph: "));
     	upper_bar1.add(add);
     	upper_bar1.add(clear);
     	upper_bar1.add(showButton); 
     	upper_bar1.add(new JLabel("  iteration: "));
     	upper_bar1.add(slider);
     	upper_bar1.add(labels);
     	upper_bar1.add(plus);
     	upper_bar1.add(minus);
    
     	upper_bar = new JPanel(new BorderLayout(0,0));
        upper_bar.setBorder(new LineBorder(Color.BLACK,1));
        upper_bar.add(upper_bar1,BorderLayout.WEST);
     	
        lower_bar = new JPanel(new BorderLayout(0,0));
        lower_bar.add(bar,BorderLayout.CENTER);
        lower_bar.add(stop,BorderLayout.WEST);
        lower_bar.add(percent,BorderLayout.EAST);
        
        //********************************alg options
        alg_options = new JPanel(new BorderLayout(0,0));
        alg_options.setBorder(new LineBorder(Color.BLACK,1));
        refresh = new JButton("refresh");
        refresh.setEnabled(true);
        refresh.setToolTipText("run CW again");
        refresh.setMargin(new Insets(1,1,1,1));
        refresh.setFont(new Font("Dialog",Font.PLAIN,9));
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	refresh();    
            }
        });
        
        Alg_param = new ButtonGroup();
        Mut_param = new ButtonGroup();
        Update_param = new ButtonGroup();
   
        stepwise = new JRadioButton("stepwise",update_now.equals("stepwise"));
        stepwise.setMargin(new Insets(1,1,1,1));
        stepwise.setActionCommand("stepwise");
        stepwise.addActionListener(this);

        continuous = new JRadioButton("continuous",update_now.equals("continuous"));
        continuous.setMargin(new Insets(1,1,1,1));
        continuous.setActionCommand("continuous");
        continuous.addActionListener(this);

        dec = new JRadioButton("exp. decreasing",mut1_now.equals("dec"));
        dec.setMargin(new Insets(1,1,1,1));
        dec.setActionCommand("dec");
        dec.addActionListener(this);

        constant = new JRadioButton("constant",mut1_now.equals("constant"));
        constant.setMargin(new Insets(1,1,1,1));
        constant.setActionCommand("constant");
        constant.addActionListener(this);

        radios_alg_opt1 = new JToolBar();
        radios_alg_opt1.setMargin(new Insets(0,0,0,0));
        radios_alg_opt1.setFloatable(false);
        
        radios_alg_opt2 = new JToolBar();
        radios_alg_opt2.setMargin(new Insets(0,0,0,0));
        radios_alg_opt2.setFloatable(false);
        
        top = new JRadioButton("top",alg_param_now.equals("top"));
        top.setMargin(new Insets(1,1,1,1));
        top.setActionCommand("top");
        top.addActionListener(this);

        dist_log   = new JRadioButton("dist log",alg_param_now.equals("dist") && vote_value_now.equals("log"));
        dist_log.setMargin(new Insets(1,1,1,1));
        dist_log.setActionCommand("dist log");
        dist_log.addActionListener(this);

        dist_nolog   = new JRadioButton("dist nolog",alg_param_now.equals("dist")&& vote_value_now.equals("nolog"));
        dist_nolog.setMargin(new Insets(1,1,1,1));
        dist_nolog.setActionCommand("dist nolog");
        dist_nolog.addActionListener(this);

        vote   = new JRadioButton("vote",alg_param_now.equals("vote"));
        vote.setMargin(new Insets(1,1,1,1));
        vote.setActionCommand("vote");
        vote.addActionListener(this);

        LinkedList L5 = new LinkedList();
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
        vote_value.addChangeListener(new ChangeListener(){
                 public void stateChanged(ChangeEvent event){
                        refresh.setEnabled(true);
                 }
        });

        LinkedList L6 = new LinkedList();
        L6.add(" 0.0 ");
        L6.add(" 0.01 ");
        L6.add(" 0.05 ");
        L6.add(" 0.1 ");
        L6.add(" 0.2 ");
        L6.add(" 0.3 ");
        L6.add(" 0.4 ");
        L6.add(" 0.5 ");
        L6.add(" 0.6 ");
        L6.add(" 0.7 ");
        L6.add(" 0.8 ");
        L6.add(" 0.9 ");
        L6.add(" 1.0 ");
        keep_value = new JSpinner(new SpinnerListModel(L6) );
        keep_value.addChangeListener(new ChangeListener(){
                 public void stateChanged(ChangeEvent event){
                        refresh.setEnabled(true);
                 }
        });
        keep_value.setEnabled(true);
        
        LinkedList L7 = new LinkedList();
        L7.add(" 0.0 ");
        L7.add(" 0.1 ");
        L7.add(" 0.2 ");
        L7.add(" 0.3 ");
        L7.add(" 0.4 ");
        L7.add(" 0.5 ");
        L7.add(" 0.6 ");
        L7.add(" 0.7 ");
        L7.add(" 0.8 ");
        L7.add(" 0.9 ");
        L7.add(" 1.0 ");
        L7.add(" 2.0 ");
        L7.add(" 3.0 ");
        L7.add(" 4.0 ");
        L7.add(" 5.0 ");

        mut_value = new JSpinner(new SpinnerListModel(L7) );
        mut_value.addChangeListener(new ChangeListener(){
                 public void stateChanged(ChangeEvent event){
                        refresh.setEnabled(true);
                 }
        });
        mut_value.setEnabled(true);

        if(alg_param_now.equals("vote"))
        vote_value.setValue(" "+vote_value_now+" ");
        else vote_value.setValue(" 0.0 ");

        //set inactive for all but 'vote'
        if(!alg_param_now.equals("vote")) vote_value.setEnabled(false); 
        JPanel ph = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ph.add(vote_value);

        keep_value.setValue(" "+keep_value_now+" ");
        mut_value.setValue(" "+mut_value_now+" ");

        Alg_param.add(top);
        Alg_param.add(dist_log);
        Alg_param.add(dist_nolog);
        Alg_param.add(vote);
        Mut_param.add(dec);
        Mut_param.add(constant);
        Update_param.add(stepwise);
        Update_param.add(continuous);


        radios_alg_opt1.add(new JLabel(" Algorithm Options: "));
        radios_alg_opt1.add(top);
        radios_alg_opt1.add(dist_log);
        radios_alg_opt1.add(dist_nolog);
        radios_alg_opt1.add(vote);
        radios_alg_opt1.add(ph);
        radios_alg_opt1.add(new JLabel("  "));
        radios_alg_opt1.add(new JLabel(" keep color rate:  "));
        radios_alg_opt1.add(keep_value);
        radios_alg_opt2.add(new JLabel(" mutation: "));
        radios_alg_opt2.add(dec);
        radios_alg_opt2.add(constant);
        radios_alg_opt2.add(mut_value);   
        radios_alg_opt2.add(new JLabel("    "));
        radios_alg_opt2.add(new JLabel("    "));
        radios_alg_opt2.add(new JLabel(" update strategy:  "));
        radios_alg_opt2.add(stepwise);
        radios_alg_opt2.add(continuous);                
        radios_alg_opt2.add(new JLabel("    "));
        radios_alg_opt2.add(refresh);
        alg_options.add(radios_alg_opt1,BorderLayout.NORTH);
        alg_options.add(radios_alg_opt2,BorderLayout.SOUTH);


        //*******************************************

        panel = new JPanel(new BorderLayout(0,0));
        panel.setPreferredSize(new Dimension(640,640));
        
	//if test=true repaint graph 
        if(test) {
            //make layout
            if(sub){
                    if(only_add){
                            //vv.init();
                            layout.update();
                            //vv.init();
                    }
                    else{
                            //subgraph benutzen
                            layout = new FRLayout(subgraph);
                            layout.initialize(new Dimension(scaleWindow,scaleWindow));
                            layout.setMaxIterations(fr_iteration);
                            only_add =true;
                    }
            }
            else{
                    //ganzen graphen benutzen
                    layout = new FRLayout(graph);
                    layout.initialize(new Dimension(scaleWindow,scaleWindow));
                    layout.setMaxIterations(fr_iteration);
            }

            vv = new MyVisualizationViewer(layout, pr,this);
            vv.setBackground(Color.WHITE);
            vv.setSize(700,500);

            //add Listener für ToolTips und Movings
            vv.setToolTipListener(new GraphTipps(vv,nodes));
            vv.setGraphMouse(new ZoomPanGraphMouse(vv));
            vv.setPickSupport(new ShapePickSupport(vv));
            vv.addGraphMouseListener(new TestGraphMouseListener(this));
            gm = new PopupGraphMouse(vv);
            vv.setGraphMouse(gm);
	       
            Timer timer = new Timer();
            timer.schedule(new Observe_progress(timer), 200, 2000); 

        } // fi test
            // else: if graph is not repainted, activate all buttons
        else{
            slider.setEnabled(true);
            zoomer.setEnabled(true);
            grab.setEnabled(true);
            add.setEnabled(true);
            if(!only_sub) showButton.setEnabled(true);
            clear.setEnabled(true);
        }
    	
     	
        //zoompane
    	MyGraphZoomScrollPane = new GraphZoomScrollPane(vv);
    	MyGraphZoomScrollPane.addKeyListener(new MyKeyListener());
        MyGraphZoomScrollPane.setFocusable(true);
        		
        //load from main panel
        panel.add(lower_bar, BorderLayout.SOUTH);
        panel.add(upper_bar, BorderLayout.NORTH);
        panel.add(MyGraphZoomScrollPane, BorderLayout.CENTER);

        getContentPane().add(panel);
        setVisible(true);
        pack();
        show(); 
     } // end init
       
       
 //***********************************************************************************       

     /**
      * this method opens the graph window and paints the graph <br>
      * it is the main method accessible from outside
      * 
      * @param sub start with empty subgraph panel
      * @param cw ChineseWhispers algorithm instance
      * @param max_draw edges
      * @param scale graph-panel (resolution)
      * @param maxFR iteration steps of graph draw algo
      */
     public void myStart(){
	    
    	pr.init(cw);
		
        //default node size
        pr.setSizeDefault();

        if(sub){
                only_sub = true;
                subgraph = new MySubGraph(cw,max_display_edges);
        }
        else{
                only_sub = false;
                graph    = new MyGraph(cw,max_display_edges,hub);
                subgraph = new MySubGraph(graph);
        }

        //delete subgraph if in subgraph mode
        clear();

        //repaint graph window and graph (repaint=true)
    	init(true);
        
        vv.setSize(MyGraphZoomScrollPane.getSize().width,MyGraphZoomScrollPane.getSize().height);
     } // end myStart
	 
    /**
     * this method updates the window without repainting the graph.<br>
     * called if "refresh" button is hit in graph-window
     * 
     */
     private void refresh(){
	 	
	 	
        //obtain new parameters
        String Alg_param_temp= "";
        String Alg_param_value_temp="";
        double Alg_param_keep;
        String Alg_param_mut1="";
        double Alg_param_mut2;
        String Alg_param_update="";

        String ALGOPT = Alg_param.getSelection().getActionCommand();
        if(ALGOPT.equals("top")) {Alg_param_temp="top";}
        if(ALGOPT.equals("dist log")) {Alg_param_temp="dist"; Alg_param_value_temp="log";}
        if(ALGOPT.equals("dist nolog")) {Alg_param_temp="dist"; Alg_param_value_temp="nolog";}
        if(ALGOPT.equals("vote")){
                Alg_param_temp="vote"; 
                Alg_param_value_temp=((String)vote_value.getValue()).trim();
        }

        Alg_param_keep=(new Double(((String)keep_value.getValue()).trim())).doubleValue();
        Alg_param_mut1=Mut_param.getSelection().getActionCommand();
        Alg_param_mut2=(new Double(((String)mut_value.getValue()).trim())).doubleValue();
        Alg_param_update=Update_param.getSelection().getActionCommand();            

        alg_param_now = Alg_param_temp;
        vote_value_now= Alg_param_value_temp;
        keep_value_now= Alg_param_keep;
        mut1_now = Alg_param_mut1;
        mut_value_now = Alg_param_mut2;
        update_now=Alg_param_update;


        //re-run algorithm with probably new parameters
        cw.setCWParameters(cw.getMinWeight(),Alg_param_temp,Alg_param_value_temp,Alg_param_keep, Alg_param_mut1, Alg_param_mut2, Alg_param_update,cw.getIteration(),false);
        cw.runAlgorithm();
        
        // initialise renderer again (for colors)
        pr.init(cw);

        this.nr_of_iterations = cw.getIteration();


        //obtain iteration from slider	 	
        pr.setIterColor(slider.getValue());

        //update graph-panel
        vv.repaint();
	
     } // end refresh
	 
    /**
     * is true, if subgraph is active
     * @return boolean-flag
     */ 
     public boolean isSub(){
            return sub;
     }
	
    /**
     * returns the renderer
     * @return renderer.
     */
    public MyRenderer getRenderer() {
            return pr;
    }
	
    /**
     * Gibt die erste Algotithmus-option zurück
     * @return Algotithmus-option.
     */
    public String getAlgOpt() {
            return alg_param_now;
    }

    /**
     * Gibt die zweite Algotithmus-option zurück
     * @return zweite Algotithmus-option.
     */
    public String getAlgOptParam() {
            return vote_value_now;
    }

    /**
     * this method is called by the windows closing adapter and resets everything.
     * this means: stop algorithm, activate buttons in control panel, close window
     * 
     *
     */
     private void close(){
        //algorithm stop
        stop();

        //timertask thread
        is_closed = true;

        //det node size to default
        pr.setSizeDefault();
        pr.setShapeNodeDefault();

        //activate buttons in controller
        only_add = false;

        //close window
        getContentPane().removeAll();
        setVisible(false);
     }


    /**
     * this method clears the subgraph<br>
     * called by clear-button and method start()
     *
     */
     private void clear(){
            ((MySubGraph)subgraph).clear();
     }

    /**
     * this method stops the algorithm<br>
     * called by stop-button and method close()
     *
     */
     private void stop(){
            vv.suspend(); 
            if (! layout.incrementsAreDone()){vv.stop();}
            stopFR = true;
     }





    /**
     * this method opens subgraph panel<br>
     * called by show-button
     */
     private void showSub(){
        sub = true;

        pr.setSizeDefault();
        pr.setShapeNodeDefault();

        getContentPane().removeAll();

        init(true);
        slider.setValue(pr.getIter());

        getContentPane().add(panel);

        setVisible(true);
        show();
        vv.setSize(MyGraphZoomScrollPane.getSize().width,MyGraphZoomScrollPane.getSize().height);
     } // end showSub

     /**
      * this method opens a dialogue for adding a node to the subgraph,<br>
      * qnd adds the node to the subgraph
      *
      */
     private void addSub(){
        String inputValue = JOptionPane.showInputDialog(this,"Insert node label here:"); 

        if(inputValue != null){
            Integer wrd = nodes.getNumber(inputValue);
            if(wrd.intValue()==0){
                //Error handling from old version of cooccaccess.BinFileStrCol 
                String s = nodes.getWord(new Integer(nodes.getMaxWordNr()));
                if(s.equals(inputValue))wrd=new Integer(nodes.getMaxWordNr());
            } // fi ==0

            //if has edges
            if(!cw.filterByThresh(edges.getData(wrd)).isEmpty()){
                    //add node to subgraph
                    ((MySubGraph)subgraph).addVertex(wrd.toString());
                    //if subgraph open: repaint
                    if(sub) showSub();
            }else
                        JOptionPane.showMessageDialog(this,"Node not in graph/no edges!", "Error", JOptionPane.ERROR_MESSAGE); 

            } // esle has edges
    } // end addSub


    /**
     * this method changes color of nodes according to<br>
     * iteration step of algorithm.<br>
     * called by slider-changeHandler 
     * @param it iteration
     */
     private void changeColor(int it){
            pr.setIterColor(it);
            vv.repaint();
    }

     /**
      * this method increases node size.<br>
      * called by key-event-listener 
      *
      */
     private void makeBiggerVertex(){
            pr.makeBig();
            vv.repaint();
     }

     /**
      * this method decreases node size.<br>
      * called by key-event-listener 
      *
      */
     private void makeSmallerVertex(){
            pr.makeSmall();
            vv.repaint();
     }

     /**
      * this method resets node size.<br>
      * called by key-event-listener 
      *
      */
     private void makeNormaLVertex(){
            pr.setSizeDefault();
            vv.repaint();
     }


     /**
      * slider changeHandler<br>
      * calls changeColor(Iterationsstufe alg) 
      */
     public void stateChanged(ChangeEvent event){
            JSlider sl =  (JSlider)event.getSource();
            if (!sl.getValueIsAdjusting()) {
                    changeColor(sl.getValue());}

     }

    /**
     *  radio-button changeHandler 
     */
    public void actionPerformed(ActionEvent e) {
            refresh.setEnabled(true);
            if(e.getActionCommand().equals("vote")){vote_value.setEnabled(true);}
            else {vote_value.setEnabled(false);}
    }

    public void run(){ // the run for Thread
        myStart();
    }
    /**
     * 
     * @return Windowscale
     */
    public int getScaleWindow(){
        return this.scaleWindow;
    }

	 
//***listener for tooltipps
class GraphTipps implements VisualizationViewer.ToolTipListener {
        MyVisualizationViewer vv;
        double proximity;
        
        public GraphTipps(MyVisualizationViewer vv,BinFileStrCol nodes) {
            this.vv = vv;
            this.proximity = 1.0;
        }
        
        
        public String getToolTipText(MouseEvent e) {
	        PickSupport pickSupport = vv.getPickSupport();
	        Point2D p = vv.transform(e.getPoint());
	        
	        MyGraphZoomScrollPane.requestFocus();

	        Vertex v = pickSupport.getVertex(p.getX(), p.getY());
            if (v != null) {
                return "<html>" +
                	   "<table border=1 width=100%>" +
                	   "<tr><td>word: </td><td colspan=2><b>"+nodes.getWord(new Integer((String)v.getUserDatum("Label"))).trim()+"</b></td></tr>"+
                	   "<tr><td>color:</td><td colspan=2><b>"+ pr.color.getNumber(v)+"</b></td></tr>"+
                	   pr.calcNeighbours(v)+
					   "</table>" +
					   "</html>";
            } else {
                Edge edge = pickSupport.getEdge(p.getX(), p.getY());
                if(edge != null) {
                	String von = nodes.getWord(new Integer((String)((Vertex)edge.getEndpoints().getFirst()).getUserDatum("Label")));
                	String nach= nodes.getWord(new Integer((String)((Vertex)edge.getEndpoints().getSecond()).getUserDatum("Label")));
                	 
                    return 	"<html>" +
             	   			"<table>" +
							"<tr><td align=right>form:</td><td><b>"+von+"</b></td></tr>"+
							"<tr><td align=right>to:</td><td><b>"+nach+"</b></td></tr>"+
							"<tr><td align=right>weight:</td><td><b>"+(String)edge.getUserDatum("Label")+"</b></td></tr>"+
							"</table>" +
							"</html>";
                }
              return null;
            }
        	}
    
	 
	 
       
	 }
	
/**
 * a subclass of ZoomPanGraphMouse that offers popup
 * menu support
 */
protected class PopupGraphMouse extends ZoomPanGraphMouse {
	MyVisualizationViewer vv;
    public PopupGraphMouse(MyVisualizationViewer vv) {
        super(vv);
        this.vv=vv;
    }
    
    public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
            handlePopup(e);
        } else {
            super.mousePressed(e);
        }
    }

    /**
     * if this is the popup trigger, process here, otherwise
     * defer to the superclass
     */
    public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
            handlePopup(e);
        } else {
            super.mouseReleased(e);
        }
    }
    
    /**
     * If this event is over a Vertex, pop up a menu to
     * allow the user to increase/decrease the voltage
     * attribute of this Vertex
     * @param e
     */
    private void handlePopup(MouseEvent e) {
        Point2D p = vv.transform(e.getPoint());
        
       
        PickSupport pickSupport = vv.getPickSupport();
        if(pickSupport != null && (e.getButton()==3)) {
            final Vertex v = pickSupport.getVertex(p.getX(), p.getY());
            if(v != null) {
            	
                JPopupMenu popup = new JPopupMenu();
                popup.add(new JLabel(" actions for "+nodes.getWord(new Integer((String)v.getUserDatum("Label")))));
                
                if(sub){
                popup.add(new AbstractAction("show a new Subgraph") {
                    public void actionPerformed(ActionEvent e) {
                        clear();
                        ((MySubGraph)subgraph).addVertex((String)v.getUserDatum("Label"));
                       	showSub();
                    
                    }
                });
                }

                popup.show(vv, e.getX(), e.getY());
                
            }
           
        }
    }
}

//***listener for clicks on nodes   
class TestGraphMouseListener implements GraphMouseListener {
        
     		JFrame F;
     	
     		public TestGraphMouseListener(JFrame F){
     			this.F = F;
     		}
     		public void graphClicked(Vertex v, MouseEvent me) {
     			
     			if(me.getButton() != 1) return;
     			int test = JOptionPane.showConfirmDialog(
    		    		  F,
						  "Add node \""+nodes.getWord(new Integer((String)v.getUserDatum("Label")))+"\" to subgraph?",
						  "Subgraph",
						  JOptionPane.YES_NO_OPTION 
						  );
				if(test == 0){ ((MySubGraph)subgraph).addVertex((String)v.getUserDatum("Label"));
				if(sub){
					showSub();
					}
				 }
    		    
    		    
    		}
    		public void graphPressed(Vertex v, MouseEvent me) {}
    		public void graphReleased(Vertex v, MouseEvent me){}
    }



//***listener for keys-events
class MyKeyListener extends KeyAdapter
	  { 
	    
		public void keyPressed(KeyEvent event)
	    {

	    	if (event.getKeyCode() == 107) {makeBiggerVertex();}
	    		
	    	if (event.getKeyCode() == 109) {makeSmallerVertex();}
	    	
	    	if (event.getKeyCode() ==  78) {makeNormaLVertex();}
	    	
	    	if (event.getKeyCode() ==  69) {pr.setEdges(); vv.repaint();}
	    	
	    	if (event.getKeyCode() ==  76) {pr.changeShapeNode(); vv.repaint();}
	    	
	    	if (event.getKeyCode() ==  37 || event.getKeyCode() ==  39 ) {slider.requestFocus();}
	    }
	  }



//***listener for progress bar and button activation
class Observe_progress extends TimerTask{
	private Timer t;
		
	public Observe_progress(Timer t){
		this.t = t;
		is_closed = false;
	}
	
	public void run() {
		
		if(layout.incrementsAreDone() || stopFR){
			
		    //stop timer
		    t.cancel();	
			
			if(!is_closed){	

				//delete progress bar
				panel.remove(lower_bar);
			
				//add lower options panel
			
				panel.add(alg_options, BorderLayout.SOUTH);
				alg_options.setVisible(true);
				show();
				
			}
			//activate slider and buttons
			slider.setEnabled(true);
			zoomer.setEnabled(true);
			grab.setEnabled(true);
			add.setEnabled(true);
	    	if(!only_sub) showButton.setEnabled(true);
	    	clear.setEnabled(true);
	    	return;
		}
		
		//get status-string of layout
		String s = layout.getStatus();
		//extract iteration step from status-string
		int j = Integer.valueOf(s.substring(s.indexOf("temp:")-3,s.indexOf("temp:")-1).trim()).intValue();
		//compute percent 
		float p = (float)j/(float)fr_iteration*100;
		percent.setText("  "+Math.round(p)+"%  ");
		//set status bar
		bar.setValue(j);
	}
}
}