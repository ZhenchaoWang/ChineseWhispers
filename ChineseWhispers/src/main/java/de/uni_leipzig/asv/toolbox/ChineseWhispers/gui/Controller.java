/*
 * This program is free software; you can redistribute it and/or
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
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.db.DBConnect;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.graph.MyGraphGUI;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.parameterHandler.PropertyLoader;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.statistics.Diagram;
import de.uni_leipzig.asv.utils.IOIteratorException;
import de.uni_leipzig.asv.utils.IOWrapperException;

/**
 * @author Rocco & Seb
 *
 */
public class Controller extends JTabbedPane implements ActionListener {

    PropertyLoader                pl_expert                    = new PropertyLoader("CW_ExpertProperties.ini");
    PropertyLoader                pl_DB                        = new PropertyLoader("CW_DBPoperties.ini.");
    PropertyLoader                pl_LastFiles                 = new PropertyLoader("CW_LastFiles.ini");

    public static boolean         running_hack;
    public static final String    NODES_TMP_FILENAME           = "nodelist.tmp";
    public static final String    EDGES_TMP_FILENAME           = "edgelist.tmp";
    public static final String    CLASSES_TMP_FILENAME         = "colors.tmp";

    public static final String[]  COLUMN_NAMES_FOR_DB_OUT      = { "node_id", "node_label", "cluster_id", "cluster_1",
            "strength_1", "cluster_2", "strength_2" };

    private static final String   display_node_degree_default  = "0";
    private static final String   display_edges_default        = "3000";
    private static final String   scale_default                = "1000 x 1000";
    private static final String   minweight_edges_default      = "0";
    private static final String   iterations_default           = "20";
    private static final String   alg_param_default            = "top";
    private static final String   mut_option_default           = "constant";
    private static final String   update_param_default         = "continuous";
    private static final String   vote_value_default           = " 0.5 ";
    private static final String   keepclass_value_default      = " 0.0 ";
    private static final String   mut_value_default            = " 0.0 ";

    private static final boolean  display_sub_default          = false;

    private static final String   hostnameString_default       = "localhost";
    private static final String   databaseString_default       = "cw";
    private static final String   rusernameString_default      = "root";
    private static final String   nodeList_DBtable_default     = "words";
    private static final String   node_ids_DBcol_default       = "w_id";
    private static final String   node_labels_DBcol_default    = "word";
    private static final String   edgeList_DBtable_default     = "co_s";
    private static final String   edgeList_DBcol1_default      = "w1_id";
    private static final String   edgeList_DBcol2_default      = "w2_id";
    private static final String   edgeList_DBcolweight_default = "sig";
    private static final String   result_DBtable_default       = "clustering";
    private static final int      portNr_default               = 3306;

    private String                display_node_degree_current, display_edges_current, scale_current,
    minweight_edges_current, iterations_current, alg_param_current, vote_value_current;
    private String                keepclass_value_current, mut_option_current, mut_value_current, update_param_current;

    private boolean               display_sub_current;

    private boolean               is_already_renumbered        = false;

    private int                   display_edges_temp, scale_temp, display_degree_temp;

    private boolean               is_alg_started               = false;

    private boolean               isGraphStarted               = false,
            isDiagStarted = false, isFileOutStarted = false, isDBOutStarted = false;

    private final JSpinner        nodeDegreeSpinner, displayEdgesSpinner, scaleSpinner, minweightSpinner,
    iterationsSpinner;

    private final ButtonGroup     Alg_param;
    private final ButtonGroup     mutationParameter;
    private final ButtonGroup     Update_param;

    private final JRadioButton    top;
    private final JRadioButton    dist_log;
    private final JRadioButton    dist_nolog;
    private final JRadioButton    vote;
    private final JRadioButton    dec;
    private final JRadioButton    constant;
    private final JRadioButton    stepwise;
    private final JRadioButton    continuous;

    private JPanel                radios;
    private final JSpinner        vote_value;
    private final JSpinner        mut_value;
    private final JSpinner        keep_value;

    private final JTextField      nodefileText;
    private final JTextField      edgeFileText;

    private final JButton         nodeFileBrowseButton;
    private final JButton         edgeFileBrowseButton;
    private final JButton         setdefault;
    private final JButton         save;
    private JButton               start;
    private final JCheckBox       only_sub;
    private final JRadioButton    UseFile, UseDB;

    private final JButton         AsGraph;
    private final JButton         AsDia;
    private final JButton         AsFile;
    private final JButton         AsDB;

    public DBConnect              dbc_out;
    public DBConnect              dbc_in;
    private boolean               is_already_read_from_DB      = false;

    public JProgressBar           loadFromFileProgress;
    public JProgressBar           loadFromDBProgress;
    public JLabel                 loadFRomDBLabel;

    public JProgressBar           writeIntoDBProgress;
    public JLabel                 writeIntoDBLabel;

    public JProgressBar           calculateCWProgress;
    private final JLabel          CalculateCWLabel;

    public JProgressBar           writeIntoFiles;

    private final JCheckBox       FileOutBox;
    private final JCheckBox       DBOutBox;
    private final JCheckBox       FilesNumbered;
    private final JCheckBox       GraphInMemory;

    private final JTextField      FileOutField;

    private final JButton         MultFileOutBrowse;                                                                    // ??

    private final JButton         FileOutBrowseButton;
    private final JButton         startFileDB, dbtake, dbdefault;
    private final JLabel          startGraph, startDiagramm;

    public boolean                isDBReadInUse                = false;                                                 // is
    // momentarily
    // being
    // read
    // from
    // DB?
    public boolean                isDBWriteInUse               = false;                                                 // is
    // momentarily
    // being
    // written
    // to
    // DB?
    public boolean                isFileWriteInUse             = false;                                                 // is
    // momentarily
    // being
    // written
    // to
    // files?
    private boolean               isFileReadInUsed             = true;
    private boolean               isDBInUsed                   = false;
    private boolean               isFileOutselected            = false;
    private boolean               isDBOutselected              = false;

    private boolean               isAlgStartedForGraph         = false;

    private boolean               dBOutError                   = false;
    private boolean               dBInError                    = false;

    // DB
    private final JTextField      rhostfield, ruserfield, rportfield, rdatabasefield, rtablename1efield, colnodeIdfield,
    colnodeLabelfield, rtablename2efield, coledge1field, coledge2field, colweightfield, otablenamefield;
    private String                hostnameString, databaseString, usernameString, passwdString, NodeTableString,
    NodeIDColString, NodeLabelColString, EdgeTableString, EdgeCo1String, EdgeCol2String, EdgeWeightColString,
    resultTableString;
    private int                   portNr;
    private boolean               isDBValuesSet                = false;
    private final JPasswordField  rpasswdfield;

    private final ChineseWhispers cw;
    private MyGraphGUI            g;
    private final JPanel          pg;

    private final JPanel          P1                           = new JPanel(null);
    private final JPanel          P2                           = new JPanel(null);
    private final JPanel          P3                           = new JPanel(null);
    private final JPanel          welcomePanel                 = new JPanel(new GridLayout(3, 1));
    JFrame                        window;
    private LinkedList            L;

    /**
     * Set and controll the GUI, handles IOs.
     *
     */
    public Controller() {

        this.cw = new ChineseWhispers();
        this.pg = new JPanel();
        // set display mode
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
            UIManager.getLookAndFeelDefaults().put("Label.font", new Font(null, Font.ROMAN_BASELINE, 11));
            UIManager.getLookAndFeelDefaults().put("RadioButton.font", new Font(null, Font.ROMAN_BASELINE, 11));
            UIManager.getLookAndFeelDefaults().put("TextField.font", new Font(null, Font.ROMAN_BASELINE, 11));
            UIManager.getLookAndFeelDefaults().put("Button.font", new Font(null, Font.BOLD, 11));
            UIManager.getLookAndFeelDefaults().put("CheckBox.font", new Font(null, Font.ROMAN_BASELINE, 11));
        } catch (Exception e2) {
            System.out.println("Error in GUI");
        }
        this.getSavedValues();
        this.getSavedDBValues();

        // P1.setBackground(myColor);
        // P2.setBackground(myColor);
        // P3.setBackground(myColor);

        // main panel*********************************
        // elemente definieren
        JLabel text0 = new JLabel("<html><u><b>INPUT</b></u></html>");
        JLabel text1 = new JLabel("Nodes:");
        this.nodefileText = new JTextField(25);
        this.nodeFileBrowseButton = new JButton("Browse");
        this.nodeFileBrowseButton.setActionCommand("nodelist");
        JLabel text2 = new JLabel("Edges:");
        this.edgeFileText = new JTextField(25);
        this.edgeFileBrowseButton = new JButton("Browse");
        this.edgeFileBrowseButton.setActionCommand("edgelist");
        this.fillFilenames();// insert values into nodelist and edgelist

        this.FilesNumbered = new JCheckBox("Input is pre-numbered");
        this.FilesNumbered.setBackground(this.P1.getBackground());
        this.FilesNumbered.setToolTipText("skips process of renumbering (applies to input from database and files).");

        this.GraphInMemory = new JCheckBox("keep graph in RAM");
        this.GraphInMemory.setBackground(this.P1.getBackground());
        this.GraphInMemory.setToolTipText("Whether to keep graph in RAM or on disc. Uncheck for large graphs");
        this.GraphInMemory.setSelected(true);

        this.UseFile = new JRadioButton("Input from file", true);
        this.UseFile.setToolTipText("read graph from files. Browse for node list and edge list.");
        this.UseFile.setBackground(this.P1.getBackground());

        this.loadFromFileProgress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        this.loadFromFileProgress.setStringPainted(true);
        this.loadFromFileProgress.setBackground(Color.LIGHT_GRAY);
        this.loadFromFileProgress.setVisible(false);

        this.UseFile.setMargin(new Insets(0, 0, 0, 0));
        this.UseFile.setActionCommand("UseFile");
        this.UseFile.addActionListener(this);

        this.UseDB = new JRadioButton("Input from database", false);
        this.UseDB.setToolTipText("Use database as specified in database panel");
        this.UseDB.setBackground(this.P1.getBackground());
        this.UseDB.setMargin(new Insets(0, 0, 0, 0));
        this.UseDB.setActionCommand("UseDB");
        this.UseDB.addActionListener(this);

        this.loadFromDBProgress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        this.loadFromDBProgress.setStringPainted(true);
        this.loadFromDBProgress.setBackground(Color.LIGHT_GRAY);
        this.loadFromDBProgress.setVisible(false);

        JLabel text3 = new JLabel("<html><u><b>OUTPUT</b></u></html>");

        this.DBOutBox = new JCheckBox("Output to database");
        this.DBOutBox.setBackground(this.P1.getBackground());
        // DBOutBox.setEnabled(false);

        this.DBOutBox.setActionCommand("DBOutBox");
        this.DBOutBox.addActionListener(this);
        this.DBOutBox.setSelected(false);
        this.DBOutBox.setToolTipText("Use database for results as specified in the database tab.");

        this.writeIntoDBProgress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        this.writeIntoDBProgress.setStringPainted(true);
        this.writeIntoDBProgress.setBackground(Color.LIGHT_GRAY);
        this.writeIntoDBProgress.setVisible(false);

        this.FileOutBox = new JCheckBox("Output to file");
        this.FileOutBox.setBackground(this.P1.getBackground());
        this.FileOutBox.setActionCommand("FileOutBox");
        this.FileOutBox.addActionListener(this);
        this.FileOutBox.setSelected(false);
        this.FileOutBox.setToolTipText("Use file for output.");

        this.writeIntoFiles = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        this.writeIntoFiles.setStringPainted(true);
        this.writeIntoFiles.setBackground(Color.LIGHT_GRAY);
        this.writeIntoFiles.setVisible(false);

        this.FileOutField = new JTextField(25);
        this.FileOutField.setBackground(Color.LIGHT_GRAY);
        this.FileOutField.setEnabled(false);

        this.FileOutBrowseButton = new JButton("browse");
        this.FileOutBrowseButton.setActionCommand("FileOutBrowse");
        this.FileOutBrowseButton.addActionListener(this);
        this.FileOutBrowseButton.setEnabled(false);

        this.MultFileOutBrowse = new JButton("browse");
        this.MultFileOutBrowse.setActionCommand("FileOutBrowse");
        this.MultFileOutBrowse.addActionListener(this);
        this.MultFileOutBrowse.setEnabled(false);

        this.startFileDB = new JButton("start");
        this.startFileDB.setActionCommand("startFileDB");
        this.startFileDB.addActionListener(this);
        this.startFileDB.setEnabled(false);
        this.startFileDB.setToolTipText("Starts Chinese Whispers and writes into file or database.");

        this.CalculateCWLabel = new JLabel("calculating chinese whispers");
        this.CalculateCWLabel.setVisible(false);

        this.calculateCWProgress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        this.calculateCWProgress.setStringPainted(true);
        this.calculateCWProgress.setBackground(Color.LIGHT_GRAY);
        this.calculateCWProgress.setVisible(false);

        this.startGraph = new JLabel("Start and open the graph display");
        this.startDiagramm = new JLabel("Start and open the diagram display");

        String s = System.getProperty("file.separator");

        this.AsGraph = new JButton(new ImageIcon(Controller.class.getResource("pics/graphicon.gif")));
        this.AsGraph.setToolTipText("Start and open the graph display");
        this.AsDia = new JButton(new ImageIcon(Controller.class.getResource("pics/diaicon.gif")));
        this.AsDia.setToolTipText("Starts and open the diagram display.");
        this.AsFile = new JButton("File");
        this.AsDB = new JButton("Database");

        // element size and positions
        text0.setBounds(10, 20, 100, 20);
        this.UseFile.setBounds(10, 50, 140, 20);
        this.FilesNumbered.setBounds(146, 50, 200, 20);
        this.GraphInMemory.setBounds(346, 50, 250, 20);
        this.loadFromFileProgress.setBounds(210, 57, 90, 10);
        this.loadFromFileProgress.setBorderPainted(false);
        this.loadFromFileProgress.setStringPainted(false);
        this.loadFromFileProgress.setBackground(Color.WHITE);

        text1.setBounds(10, 75, 100, 20);
        text2.setBounds(10, 100, 100, 20);
        this.nodefileText.setBounds(150, 75, 150, 20);
        this.edgeFileText.setBounds(150, 100, 150, 20);
        this.nodeFileBrowseButton.setBounds(320, 75, 80, 20);
        this.edgeFileBrowseButton.setBounds(320, 100, 80, 20);

        this.UseDB.setBounds(10, 135, 200, 20);
        this.loadFromDBProgress.setBounds(210, 142, 90, 10);
        this.loadFromDBProgress.setBorderPainted(false);
        this.loadFromDBProgress.setStringPainted(false);
        this.loadFromDBProgress.setBackground(Color.WHITE);

        text3.setBounds(10, 170, 100, 20);
        this.DBOutBox.setBounds(10, 200, 130, 20);
        this.writeIntoDBProgress.setBounds(210, 207, 90, 10);
        this.writeIntoDBProgress.setBackground(Color.WHITE);
        this.writeIntoDBProgress.setBorderPainted(false);
        this.writeIntoDBProgress.setStringPainted(false);

        this.FileOutBox.setBounds(10, 225, 120, 20);
        this.FileOutField.setBounds(150, 225, 150, 20);

        this.writeIntoFiles.setBounds(210, 232, 90, 10);
        this.writeIntoFiles.setBackground(Color.WHITE);
        this.writeIntoFiles.setBorderPainted(false);
        this.writeIntoFiles.setStringPainted(false);

        this.FileOutBrowseButton.setBounds(320, 225, 80, 20);

        this.startFileDB.setBounds(10, 255, 80, 20);

        this.startGraph.setBounds(10, 290, 180, 20);
        this.startDiagramm.setBounds(200, 290, 180, 20);

        this.AsGraph.setBounds(10, 315, 51, 51);
        this.AsDia.setBounds(200, 315, 50, 50);

        this.CalculateCWLabel.setBounds(419, 325, 200, 20);
        this.calculateCWProgress.setBounds(420, 345, 150, 10);
        this.calculateCWProgress.setBackground(Color.WHITE);
        this.calculateCWProgress.setBorderPainted(false);
        this.calculateCWProgress.setStringPainted(false);

        // add elements to GUI
        this.P1.add(this.UseFile);
        this.P1.add(this.FilesNumbered);
        this.P1.add(this.GraphInMemory);

        this.P1.add(this.loadFromFileProgress);
        this.P1.add(text0);
        this.P1.add(text1);
        this.P1.add(text2);
        this.P1.add(this.nodefileText);
        this.P1.add(this.edgeFileText);
        this.P1.add(this.nodeFileBrowseButton);
        this.P1.add(this.edgeFileBrowseButton);
        this.P1.add(this.UseDB);
        this.P1.add(this.loadFromDBProgress);
        this.P1.add(text3);

        this.P1.add(this.DBOutBox);
        this.P1.add(this.writeIntoDBProgress);
        this.P1.add(this.FileOutBox);

        this.P1.add(this.writeIntoFiles);

        this.P1.add(this.AsGraph);
        this.P1.add(this.AsDia);
        this.P1.add(this.AsFile);
        this.P1.add(this.AsDB);
        this.P1.add(this.startFileDB);
        this.P1.add(this.startGraph);
        this.P1.add(this.startDiagramm);

        this.P1.add(this.CalculateCWLabel);
        this.P1.add(this.calculateCWProgress);

        // listeners
        this.nodeFileBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.openFile(e);
            }
        });
        this.edgeFileBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.openFile(e);
            }
        });
        this.AsGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.pl_LastFiles.setParam("nodelist-file", Controller.this.nodefileText.getText());
                Controller.this.pl_LastFiles.setParam("edgelist-file", Controller.this.edgeFileText.getText());
                Controller.this.startGraph();
            }
        });
        this.AsDia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.pl_LastFiles.setParam("nodelist-file", Controller.this.nodefileText.getText());
                Controller.this.pl_LastFiles.setParam("edgelist-file", Controller.this.edgeFileText.getText());
                Controller.this.startDia();
            }
        });
        this.AsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        this.AsDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        // ************************************************************************
        // expert panel******************************************************

        // define elements
        JLabel textg = new JLabel("<html><b><u>GRAPH SETTINGS</u></b></html>");
        JLabel text4 = new JLabel("<html><b>- degree of nodes:</b><br>" + "<smaller>"
                + "<b>value &lt; 0</b>, display nodes with higher degree<br>"
                + "<b>value &gt; 0</b>, display nodes with lower degree<br>" + "<b>value = 0</b>, display all nodes"
                + "</smaller></html>");
        this.L = new LinkedList();
        for (int i = -300; i <= 300; i += 1) {
            this.L.add(Integer.toString(i));
        }
        this.nodeDegreeSpinner = new JSpinner(new SpinnerListModel(this.L));
        this.nodeDegreeSpinner.setValue(this.display_node_degree_current);

        JLabel text5 = new JLabel("<html><b>- max. number of edges to draw:</b></html>");
        this.L = new LinkedList();
        this.L.add("all");
        for (int i = 100; i <= 100000; i += 10) {
            this.L.add(Integer.toString(i));
        }
        this.displayEdgesSpinner = new JSpinner(new SpinnerListModel(this.L));
        this.displayEdgesSpinner.setValue(this.display_edges_current);

        // *
        JLabel text6 = new JLabel("<html><b>- scale:</b></html>");
        this.L = new LinkedList();
        this.L.add("600 x 600");
        this.L.add("1000 x 1000");
        this.L.add("2000 x 2000");
        this.L.add("3000 x 3000");
        this.L.add("4000 x 4000");
        this.L.add("5000 x 5000");
        this.L.add("6000 x 6000");
        this.scaleSpinner = new JSpinner(new SpinnerListModel(this.L));
        this.scaleSpinner.setValue(this.scale_current);

        // *
        this.only_sub = new JCheckBox("<html><b> starts with an empty subgraph panel</b></html>");
        this.only_sub.setBackground(this.P1.getBackground());
        this.only_sub.setMargin(new Insets(0, 0, 0, 0));
        this.only_sub.setSelected(this.display_sub_current);

        // *
        JLabel texta = new JLabel("<html><b><u>ALGORITHM</u></b></html>");
        JLabel text7 = new JLabel("<html><b>- minimum edge weight threshold</b></html>");
        this.L = new LinkedList();
        for (int i = 0; i <= 10000; i += 1) {
            this.L.add(Integer.toString(i));
        }
        this.minweightSpinner = new JSpinner(new SpinnerListModel(this.L));
        this.minweightSpinner.setValue(this.minweight_edges_current);

        // *
        JLabel text8 = new JLabel("<html><b>- number of iterations:</b></html>");
        this.L = new LinkedList();
        for (int i = 10; i <= 200; i += 1) {
            this.L.add(Integer.toString(i));
        }
        this.iterationsSpinner = new JSpinner(new SpinnerListModel(this.L));
        this.iterationsSpinner.setValue(this.iterations_current);

        JLabel text9 = new JLabel("<html><b>- algorithm strategy:</b></html>");
        JLabel text10 = new JLabel("<html><b>- mutation:</b></html>");
        JLabel text11 = new JLabel("<html><b>- update strategy:</b></html>");
        JLabel text12 = new JLabel("<html><b>- keep color rate:</b></html>");

        this.Alg_param = new ButtonGroup();
        this.mutationParameter = new ButtonGroup();
        this.Update_param = new ButtonGroup();

        this.dec = new JRadioButton("exp. decreasing", this.mut_option_current.equals("dec"));
        this.dec.setActionCommand("dec");
        this.dec.setBackground(this.P1.getBackground());
        this.dec.setMargin(new Insets(0, 0, 0, 0));
        this.dec.addActionListener(this);

        this.constant = new JRadioButton("constant", this.mut_option_current.equals("constant"));
        this.constant.setActionCommand("constant");
        this.constant.setBackground(this.P1.getBackground());
        this.constant.setMargin(new Insets(0, 0, 0, 0));
        this.constant.addActionListener(this);

        this.stepwise = new JRadioButton("stepwise", this.update_param_current.equals("stepwise"));
        this.stepwise.setActionCommand("stepwise");
        this.stepwise.setBackground(this.P1.getBackground());
        this.stepwise.setMargin(new Insets(0, 0, 0, 0));
        this.stepwise.addActionListener(this);

        this.continuous = new JRadioButton("continuous", this.update_param_current.equals("continuous"));
        this.continuous.setActionCommand("continuous");
        this.continuous.setBackground(this.P1.getBackground());
        this.continuous.setMargin(new Insets(0, 0, 0, 0));
        this.continuous.addActionListener(this);

        this.top = new JRadioButton("top", this.alg_param_current.equals("top"));
        this.top.setActionCommand("top");
        this.top.setBackground(this.P1.getBackground());
        this.top.setMargin(new Insets(0, 0, 0, 0));
        this.top.addActionListener(this);

        this.dist_log = new JRadioButton("dist log", this.alg_param_current.equals("dist log"));
        this.dist_log.setActionCommand("dist log");
        this.dist_log.setBackground(this.P1.getBackground());
        this.dist_log.setMargin(new Insets(0, 0, 0, 0));
        this.dist_log.addActionListener(this);

        this.dist_nolog = new JRadioButton("dist nolog", this.alg_param_current.equals("dist nolog"));
        this.dist_nolog.setActionCommand("dist nolog");
        this.dist_nolog.setBackground(this.P1.getBackground());
        this.dist_nolog.setMargin(new Insets(0, 0, 0, 0));
        this.dist_nolog.addActionListener(this);

        this.vote = new JRadioButton("vote", this.alg_param_current.equals("vote"));
        this.vote.setActionCommand("vote");
        this.vote.setBackground(this.P1.getBackground());
        this.vote.setMargin(new Insets(0, 0, 0, 0));
        this.vote.addActionListener(this);

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
        this.vote_value = new JSpinner(new SpinnerListModel(L5));
        this.vote_value.setValue(this.vote_value_current);

        LinkedList L6 = new LinkedList();
        L6.add(" 0.0 ");
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
        L6.add(" 2.0 ");
        L6.add(" 3.0 ");
        L6.add(" 4.0 ");
        L6.add(" 5.0 ");

        this.mut_value = new JSpinner(new SpinnerListModel(L6));
        this.mut_value.setValue(this.mut_value_current);

        LinkedList L7 = new LinkedList();
        L7.add(" 0.0 ");
        L7.add(" 0.01 ");
        L7.add(" 0.05 ");
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
        this.keep_value = new JSpinner(new SpinnerListModel(L7));
        this.keep_value.setValue(this.keepclass_value_current);

        // inaktive for all options but 'vote'
        if (!this.alg_param_current.equals("vote"))
            this.vote_value.setEnabled(false);

        this.Alg_param.add(this.top);
        this.Alg_param.add(this.dist_log);
        this.Alg_param.add(this.dist_nolog);
        this.Alg_param.add(this.vote);

        this.mutationParameter.add(this.dec);
        this.mutationParameter.add(this.constant);

        this.Update_param.add(this.stepwise);
        this.Update_param.add(this.continuous);

        this.setdefault = new JButton("default");
        this.setdefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.setDefaultValues();
            }
        });

        this.save = new JButton("apply");
        this.save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.saveValues();
            }
        });

        textg.setBounds(10, 20, 100, 20);
        text4.setBounds(10, 50, 250, 60);
        text4.setVerticalAlignment(JLabel.TOP);
        this.nodeDegreeSpinner.setBounds(260, 50, 100, 20);

        text5.setBounds(10, 120, 250, 20);
        this.displayEdgesSpinner.setBounds(260, 120, 100, 20);

        text6.setBounds(10, 150, 250, 20);
        this.scaleSpinner.setBounds(260, 150, 100, 20);

        this.only_sub.setBounds(10, 180, 250, 20);
        texta.setBounds(10, 220, 100, 20);

        text7.setBounds(10, 245, 250, 20);
        this.minweightSpinner.setBounds(260, 245, 100, 20);

        text8.setBounds(10, 275, 250, 20);
        this.iterationsSpinner.setBounds(260, 275, 100, 20);

        text9.setBounds(10, 305, 250, 20);
        this.top.setBounds(260, 305, 50, 20);
        this.dist_log.setBounds(310, 305, 70, 20);
        this.dist_nolog.setBounds(380, 305, 80, 20);
        this.vote.setBounds(460, 305, 50, 20);
        this.vote_value.setBounds(510, 305, 50, 20);

        text10.setBounds(10, 335, 250, 20);
        this.dec.setBounds(260, 335, 100, 20);
        this.constant.setBounds(380, 335, 70, 20);
        this.mut_value.setBounds(450, 335, 50, 20);

        text11.setBounds(10, 365, 250, 20);
        this.stepwise.setBounds(260, 365, 100, 20);
        this.continuous.setBounds(380, 365, 100, 20);

        text12.setBounds(10, 395, 250, 20);
        this.keep_value.setBounds(260, 395, 50, 20);

        this.save.setBounds(10, 450, 80, 20);
        this.setdefault.setBounds(95, 450, 80, 20);

        this.P2.add(textg);
        this.P2.add(text4);
        this.P2.add(this.nodeDegreeSpinner);
        this.P2.add(text6);
        this.P2.add(this.scaleSpinner);
        this.P2.add(this.only_sub);
        this.P2.add(texta);
        this.P2.add(text7);
        this.P2.add(this.minweightSpinner);
        this.P2.add(text8);
        this.P2.add(this.iterationsSpinner);
        this.P2.add(text9);
        this.P2.add(this.top);
        this.P2.add(this.dist_log);
        this.P2.add(this.dist_nolog);
        this.P2.add(this.vote);
        this.P2.add(this.vote_value);
        this.P2.add(text10);
        this.P2.add(this.dec);
        this.P2.add(this.constant);
        this.P2.add(this.mut_value);

        this.P2.add(text11);
        this.P2.add(this.stepwise);
        this.P2.add(this.continuous);
        this.P2.add(text12);
        this.P2.add(this.keep_value);

        this.P2.add(this.save);
        this.P2.add(this.setdefault);

        this.P2.add(text5);
        this.P2.add(this.displayEdgesSpinner);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // database panel
        JLabel rdatabase = new JLabel("<html><u><b>DATABASE</b></u></html>");
        rdatabase.setBounds(10, 20, 100, 20);

        JLabel rhostname = new JLabel("Hostname:");
        rhostname.setBounds(20, 40, 100, 20);
        this.rhostfield = new JTextField(25);
        this.rhostfield.setBounds(20, 60, 150, 20);
        this.rhostfield.setText(this.hostnameString);
        this.rhostfield.setEnabled(false);
        this.rhostfield.setBackground(Color.LIGHT_GRAY);
        this.rhostfield.setToolTipText("Enter the hostname of database server.");

        JLabel rdatabasee = new JLabel("Database:");
        rdatabasee.setBounds(190, 40, 100, 20);
        this.rdatabasefield = new JTextField(25);
        this.rdatabasefield.setBounds(190, 60, 150, 20);
        this.rdatabasefield.setText(this.databaseString);
        this.rdatabasefield.setEnabled(false);
        this.rdatabasefield.setBackground(Color.LIGHT_GRAY);
        this.rdatabasefield.setToolTipText("Enter the database name");

        JLabel rport = new JLabel("Port:");
        rport.setBounds(360, 40, 100, 20);
        this.rportfield = new JTextField(25);
        this.rportfield.setBounds(360, 60, 150, 20);
        this.rportfield.setText(new Integer(this.portNr).toString());
        this.rportfield.setEnabled(false);
        this.rportfield.setBackground(Color.LIGHT_GRAY);
        this.rportfield.setToolTipText("Enter the port of database server (default 3306).");

        JLabel rusername = new JLabel("Username:");
        rusername.setBounds(20, 80, 100, 20);
        this.ruserfield = new JTextField(25);
        this.ruserfield.setBounds(20, 100, 150, 20);
        this.ruserfield.setText(this.usernameString);
        this.ruserfield.setEnabled(false);
        this.ruserfield.setBackground(Color.LIGHT_GRAY);
        this.ruserfield.setToolTipText("Enter your username for database connection.");

        JLabel rpasswd = new JLabel("Password:");
        rpasswd.setBounds(190, 80, 100, 20);
        this.rpasswdfield = new JPasswordField(25);
        this.rpasswdfield.setBounds(190, 100, 150, 20);
        this.rpasswdfield.setText(this.passwdString);
        this.rpasswdfield.setEnabled(false);
        this.rpasswdfield.setBackground(Color.LIGHT_GRAY);
        this.rpasswdfield.setToolTipText("Enter the password for database connection.");

        JLabel rtablename1 = new JLabel("<html><u><b>TABLE CONTAINING NODES</b></u></html>");
        rtablename1.setBounds(10, 120, 250, 20);
        JLabel rtablename1e = new JLabel("Table name:");
        rtablename1e.setBounds(20, 140, 100, 20);
        this.rtablename1efield = new JTextField(25);
        this.rtablename1efield.setBounds(20, 160, 150, 20);
        this.rtablename1efield.setText(this.NodeTableString);
        this.rtablename1efield.setEnabled(false);
        this.rtablename1efield.setBackground(Color.LIGHT_GRAY);
        this.rtablename1efield.setToolTipText("Enter the table name for nodes.");

        JLabel colNodeID = new JLabel("Column node ID:");
        colNodeID.setBounds(190, 140, 200, 20);
        this.colnodeIdfield = new JTextField(25);
        this.colnodeIdfield.setBounds(190, 160, 150, 20);
        this.colnodeIdfield.setText(this.NodeIDColString);
        this.colnodeIdfield.setEnabled(false);
        this.colnodeIdfield.setBackground(Color.LIGHT_GRAY);
        this.colnodeIdfield.setToolTipText("Enter the column name for node IDs.");

        JLabel colNodeLabels = new JLabel("Colum node labels:");
        colNodeLabels.setBounds(360, 140, 200, 20);
        this.colnodeLabelfield = new JTextField(25);
        this.colnodeLabelfield.setBounds(360, 160, 150, 20);
        this.colnodeLabelfield.setText(this.NodeLabelColString);
        this.colnodeLabelfield.setEnabled(false);
        this.colnodeLabelfield.setBackground(Color.LIGHT_GRAY);
        this.colnodeLabelfield.setToolTipText("Enter the column name for node labels.");

        JLabel rtablename2 = new JLabel("<html><u><b>TABLE CONTAINING EDGES</b></u></html>");
        rtablename2.setBounds(10, 180, 250, 20);
        JLabel rtablename2e = new JLabel("Table name:");
        rtablename2e.setBounds(20, 200, 100, 20);
        this.rtablename2efield = new JTextField(25);
        this.rtablename2efield.setBounds(20, 220, 150, 20);
        this.rtablename2efield.setText(this.EdgeTableString);
        this.rtablename2efield.setEnabled(false);
        this.rtablename2efield.setBackground(Color.LIGHT_GRAY);
        this.rtablename2efield.setToolTipText("Enter the table name for edges.");

        JLabel colwn1 = new JLabel("Col. node ID 1:");
        colwn1.setBounds(20, 240, 150, 20);
        this.coledge1field = new JTextField(25);
        this.coledge1field.setBounds(20, 260, 150, 20);
        this.coledge1field.setText(this.EdgeCo1String);
        this.coledge1field.setEnabled(false);
        this.coledge1field.setBackground(Color.LIGHT_GRAY);
        this.coledge1field.setToolTipText("Enter the column name of node ID 1.");

        JLabel colwn2 = new JLabel("Col. node ID 2:");
        colwn2.setBounds(190, 240, 150, 20);
        this.coledge2field = new JTextField(25);
        this.coledge2field.setBounds(190, 260, 150, 20);
        this.coledge2field.setText(this.EdgeCol2String);
        this.coledge2field.setEnabled(false);
        this.coledge2field.setBackground(Color.LIGHT_GRAY);
        this.coledge2field.setToolTipText("Enter the column name of node ID 2.");

        JLabel colsig = new JLabel("Col. weight:");
        colsig.setBounds(360, 240, 150, 20);
        this.colweightfield = new JTextField(25);
        this.colweightfield.setBounds(360, 260, 150, 20);
        this.colweightfield.setText(this.EdgeWeightColString);
        this.colweightfield.setEnabled(false);
        this.colweightfield.setBackground(Color.LIGHT_GRAY);
        this.colweightfield.setToolTipText("Enter the column name for weights.");

        JLabel otablename = new JLabel("<html><u><b>TABLE USED FOR OUTPUT</b></u></html>");
        otablename.setBounds(10, 280, 250, 20);
        JLabel otablenamee = new JLabel("Table name:");
        otablenamee.setBounds(20, 300, 150, 20);
        this.otablenamefield = new JTextField(25);
        this.otablenamefield.setBounds(20, 320, 150, 20);
        this.otablenamefield.setText(this.resultTableString);
        this.otablenamefield.setEnabled(false);
        this.otablenamefield.setBackground(Color.LIGHT_GRAY);
        this.otablenamefield.setToolTipText("Enter the table name for output in DB.");

        // setDefaultDBValues();
        this.dbtake = new JButton("apply");
        this.dbtake.setBounds(10, 350, 80, 20);
        this.dbtake.setEnabled(false);
        this.dbtake.setToolTipText("Use current values for db connection and operations.");
        this.dbtake.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.saveDBValues();
            }
        });
        this.dbdefault = new JButton("default");
        this.dbdefault.setBounds(95, 350, 80, 20);
        this.dbdefault.setEnabled(false);
        this.dbdefault.setToolTipText("Get default values.");
        this.dbdefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.this.setDefaultDBValues();
            }
        });

        this.P3.add(rhostname);
        this.P3.add(this.rhostfield);
        this.P3.add(rport);
        this.P3.add(this.rportfield);
        this.P3.add(rusername);
        this.P3.add(this.ruserfield);

        this.P3.add(rdatabase);
        this.P3.add(rdatabasee);
        this.P3.add(this.rdatabasefield);
        this.P3.add(rpasswd);
        this.P3.add(this.rpasswdfield);

        this.P3.add(rtablename1);
        this.P3.add(rtablename1e);
        this.P3.add(this.rtablename1efield);
        this.P3.add(colNodeID);
        this.P3.add(this.colnodeIdfield);
        this.P3.add(colNodeLabels);
        this.P3.add(this.colnodeLabelfield);

        this.P3.add(rtablename2);
        this.P3.add(rtablename2e);
        this.P3.add(this.rtablename2efield);
        this.P3.add(colwn1);
        this.P3.add(this.coledge1field);
        this.P3.add(colwn2);
        this.P3.add(this.coledge2field);
        this.P3.add(colsig);
        this.P3.add(this.colweightfield);

        this.P3.add(otablename);
        this.P3.add(otablenamee);
        this.P3.add(this.otablenamefield);

        this.P3.add(this.dbtake);
        this.P3.add(this.dbdefault);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ************************************************************************************************************

        // welcome panel
        JLabel l1 = new JLabel("<html><h1><u>Chinese Whispers</u></h1>"
                + "<p align=\"center\">A Graph Clustering Algorithm<br>" + "linear in the number of edges</p></html>");
        l1.setHorizontalAlignment(JLabel.CENTER);
        l1.setVerticalAlignment(JLabel.BOTTOM);
        l1.setBackground(this.welcomePanel.getBackground());

        this.welcomePanel.add(l1);

        JLabel l2 = new JLabel(new ImageIcon(Controller.class.getResource("pics/whisper1.jpg")));
        l2.setHorizontalAlignment(JLabel.CENTER);
        l2.setVerticalAlignment(JLabel.CENTER);
        l2.setBackground(this.welcomePanel.getBackground());
        this.welcomePanel.add(l2);

        JLabel l3 = new JLabel("<html><p align=\"center\">ASV Toolbox 2005<br>" + "Authors:<br>" + "C. Biemann, "
                + "R. Gwizdziel, " + "S. Gottwald</p></html>");
        l3.setHorizontalAlignment(JLabel.CENTER);
        l3.setVerticalAlignment(JLabel.TOP);
        l3.setBackground(this.welcomePanel.getBackground());
        this.welcomePanel.add(l3);

        this.addTab("welcome", this.welcomePanel);
        this.addTab("main", this.P1);
        this.addTab("expert", this.P2);
        this.addTab("database", this.P3);
    }

    // DB
    /**
     * Writes DB-parameters into property-file.
     */
    private void saveDBValues() {
        this.pl_DB.setParam("Hostname", this.rhostfield.getText());
        this.pl_DB.setParam("Port", this.rportfield.getText());
        this.pl_DB.setParam("Username", this.ruserfield.getText());

        String pw = "";
        for (int i = 0; i < this.rpasswdfield.getPassword().length; i++) {
            pw += this.rpasswdfield.getPassword()[i];
        }
        this.pl_DB.setParam("Password", pw);
        this.pl_DB.setParam("Database", this.rdatabasefield.getText());
        this.pl_DB.setParam("nodeTableName", this.rtablename1efield.getText());
        this.pl_DB.setParam("nodeColIDName", this.colnodeIdfield.getText());
        this.pl_DB.setParam("nodeColLabelName", this.colnodeLabelfield.getText());
        this.pl_DB.setParam("edgeTableName", this.rtablename2efield.getText());
        this.pl_DB.setParam("edgeCol1Name", this.coledge1field.getText());
        this.pl_DB.setParam("edgeCol2Name", this.coledge2field.getText());
        this.pl_DB.setParam("edgeColWeightName", this.colweightfield.getText());
        this.pl_DB.setParam("resultTableName", this.otablenamefield.getText());

        this.is_alg_started = false;
        this.is_already_read_from_DB = false;
        this.is_already_renumbered = false;
    } // saveDBvalues

    /**
     * Set all default DB-paramater.
     *
     */
    private void setDefaultDBValues() {
        this.rhostfield.setText(hostnameString_default);
        this.rportfield.setText((new Integer(portNr_default)).toString());
        this.rdatabasefield.setText(databaseString_default);
        this.ruserfield.setText(rusernameString_default);
        this.rpasswdfield.setText("");
        this.rtablename1efield.setText(nodeList_DBtable_default);
        this.colnodeIdfield.setText(node_ids_DBcol_default);
        this.colnodeLabelfield.setText(node_labels_DBcol_default);
        this.rtablename2efield.setText(edgeList_DBtable_default);
        this.coledge1field.setText(edgeList_DBcol1_default);
        this.coledge2field.setText(edgeList_DBcol2_default);
        this.colweightfield.setText(edgeList_DBcolweight_default);
        this.otablenamefield.setText(result_DBtable_default);

        this.is_alg_started = false;
        this.is_already_read_from_DB = false;
        this.is_already_renumbered = false;
    }

    /**
     * Get all DB-values from property-file and set them as actual values.
     *
     */
    private void getSavedDBValues() {
        boolean isSet[] = new boolean[12];
        for (int i = 0; i < 12; i++)
            isSet[i] = false;

        if (this.pl_DB.getParam("Hostname") != null) {
            this.hostnameString = this.pl_DB.getParam("Hostname");
            isSet[0] = true;
        } else
            this.hostnameString = hostnameString_default;
        if (this.pl_DB.getParam("Port") != null) {
            try {
                this.portNr = Integer.parseInt(this.pl_DB.getParam("Port"));
            } catch (NumberFormatException e) {
                this.portNr = portNr_default;
            }
            isSet[1] = true;
        } else
            this.portNr = portNr_default;
        if (this.pl_DB.getParam("Database") != null) {
            this.databaseString = this.pl_DB.getParam("Database");
            isSet[2] = true;
        } else
            this.databaseString = databaseString_default;
        if (this.pl_DB.getParam("Username") != null) {
            this.usernameString = this.pl_DB.getParam("Username");
            isSet[3] = true;
        } else
            this.usernameString = rusernameString_default;
        if (this.pl_DB.getParam("Password") != null) {
            this.passwdString = this.pl_DB.getParam("Password");
        } else
            this.passwdString = "";
        if (this.pl_DB.getParam("nodeTableName") != null) {
            this.NodeTableString = this.pl_DB.getParam("nodeTableName");
            isSet[4] = true;
        } else
            this.NodeTableString = nodeList_DBtable_default;
        if (this.pl_DB.getParam("nodeColIDName") != null) {
            this.NodeIDColString = this.pl_DB.getParam("nodeColIDName");
            isSet[5] = true;
        } else
            this.NodeIDColString = node_ids_DBcol_default;
        if (this.pl_DB.getParam("nodeColLabelName") != null) {
            this.NodeLabelColString = this.pl_DB.getParam("nodeColLabelName");
            isSet[6] = true;
        } else
            this.NodeLabelColString = node_labels_DBcol_default;
        if (this.pl_DB.getParam("edgeTableName") != null) {
            this.EdgeTableString = this.pl_DB.getParam("edgeTableName");
            isSet[7] = true;
        } else
            this.EdgeTableString = edgeList_DBtable_default;
        if (this.pl_DB.getParam("edgeCol1Name") != null) {
            this.EdgeCo1String = this.pl_DB.getParam("edgeCol1Name");
            isSet[8] = true;
        } else
            this.EdgeCo1String = edgeList_DBcol1_default;
        if (this.pl_DB.getParam("edgeCol2Name") != null) {
            this.EdgeCol2String = this.pl_DB.getParam("edgeCol2Name");
            isSet[9] = true;
        } else
            this.EdgeCol2String = edgeList_DBcol2_default;
        if (this.pl_DB.getParam("edgeColWeightName") != null) {
            this.EdgeWeightColString = this.pl_DB.getParam("edgeColWeightName");
            isSet[10] = true;
        } else
            this.EdgeWeightColString = edgeList_DBcolweight_default;
        if (this.pl_DB.getParam("resultTableName") != null) {
            this.resultTableString = this.pl_DB.getParam("resultTableName");
            isSet[11] = true;
        } else
            this.resultTableString = result_DBtable_default;

        for (int i = 0; i < 12; i++) {
            if (isSet[i])
                this.isDBValuesSet = true;
            else {
                this.isDBValuesSet = false;
                break;
            }
        }

        this.is_alg_started = false;
        this.is_already_read_from_DB = false;
        this.is_already_renumbered = false;
    }

    /**
     * Same as saveDBValues but for algorithm options.
     *
     */
    private void saveValues() {
        this.pl_expert.setParam("displayNodeDegree", (String) this.nodeDegreeSpinner.getValue());
        this.pl_expert.setParam("displayEdges", (String) this.displayEdgesSpinner.getValue());
        this.pl_expert.setParam("scale", (String) this.scaleSpinner.getValue());
        this.pl_expert.setParam("minWeight", (String) this.minweightSpinner.getValue());
        this.pl_expert.setParam("iterations", (String) this.iterationsSpinner.getValue());
        this.pl_expert.setParam("mutationParameter", this.mutationParameter.getSelection().getActionCommand());
        this.pl_expert.setParam("Update_param", this.Update_param.getSelection().getActionCommand());
        this.pl_expert.setParam("vote_value", (String) this.vote_value.getValue());
        this.pl_expert.setParam("keep_value", (String) this.keep_value.getValue());
        this.pl_expert.setParam("mut_value", (String) this.mut_value.getValue());
        this.pl_expert.setParam("only_sub", new Boolean(this.only_sub.isSelected()).toString());

        this.is_alg_started = false;
    } // end saveValues

    /**
     * Same as setDefaultDBValues but for algorithm options.
     *
     */
    private void setDefaultValues() {
        this.nodeDegreeSpinner.setValue(display_node_degree_default);
        this.displayEdgesSpinner.setValue(display_edges_default);
        this.scaleSpinner.setValue(scale_default);
        this.minweightSpinner.setValue(minweight_edges_default);
        this.iterationsSpinner.setValue(iterations_default);
        this.vote_value.setValue(vote_value_default);
        this.mut_value.setValue(mut_value_default);
        this.keep_value.setValue(keepclass_value_default);

        this.only_sub.setSelected(display_sub_default);
        this.top.setSelected(true);
        this.continuous.setSelected(true);
        this.dec.setSelected(true);
        this.vote_value.setEnabled(false);
        this.is_alg_started = false;
    }

    /**
     * Same as getSavedDBValues but for algorithm options.
     *
     */
    private void getSavedValues() {

        if (this.pl_expert.getParam("displayNodeDegree") != null) {
            this.display_node_degree_current = this.pl_expert.getParam("displayNodeDegree");
        } else {
            this.display_node_degree_current = display_node_degree_default;
        }

        if (this.pl_expert.getParam("displayEdges") != null) {
            this.display_edges_current = this.pl_expert.getParam("displayEdges");
        } else {
            this.display_edges_current = display_edges_default;
        }

        if (this.pl_expert.getParam("scale") != null) {
            this.scale_current = this.pl_expert.getParam("scale");
        } else {
            this.scale_current = scale_default;
        }

        if (this.pl_expert.getParam("minWeight") != null) {
            this.minweight_edges_current = this.pl_expert.getParam("minWeight");
        } else {
            this.minweight_edges_current = minweight_edges_default;
        }

        if (this.pl_expert.getParam("iterations") != null) {
            this.iterations_current = this.pl_expert.getParam("iterations");
        } else {
            this.iterations_current = iterations_default;
        }

        if (this.pl_expert.getParam("vote_value") != null) {
            this.vote_value_current = this.pl_expert.getParam("vote_value");
        } else {
            this.vote_value_current = vote_value_default;
        }

        if (this.pl_expert.getParam("keep_value") != null) {
            this.keepclass_value_current = this.pl_expert.getParam("keep_value");
        } else {
            this.keepclass_value_current = keepclass_value_default;
        }

        if (this.pl_expert.getParam("mut_value") != null) {
            this.mut_value_current = this.pl_expert.getParam("mut_value");
        } else {
            this.mut_value_current = mut_value_default;
        }

        if (this.pl_expert.getParam("mutationParameter") != null) {
            this.mut_option_current = this.pl_expert.getParam("mutationParameter");
        } else {
            this.mut_option_current = mut_option_default;
        }

        if (this.pl_expert.getParam("Update_param") != null) {
            this.update_param_current = this.pl_expert.getParam("Update_param");
        } else {
            this.update_param_current = update_param_default;
        }

        if (this.pl_expert.getParam("Alg_param") != null) {
            this.alg_param_current = this.pl_expert.getParam("Alg_param");
        } else {
            this.alg_param_current = alg_param_default;
        }

        if (this.pl_expert.getParam("only_sub") != null) {
            this.display_sub_current = new Boolean(this.pl_expert.getParam("only_sub")).booleanValue();
        } else {
            this.display_sub_current = display_sub_default;
        }

        this.is_alg_started = false;
    }

    /**
     * Handels Button-pushed events.
     *
     * @param e
     *            The ActionEvent.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String ec = e.getActionCommand();
        if (ec.equals("vote")) {
            this.vote_value.setEnabled(true);
        } else if (ec.equals("top") || ec.equals("dist log") || ec.equals("dist nolog")) {
            this.vote_value.setEnabled(false);
        }

        if (ec.equals("UseDB")) {

            this.UseDB.setSelected(true);
            this.UseFile.setSelected(false);
            this.setFileInUsed(this.UseFile.isSelected());
            this.setDBInUsed(this.UseDB.isSelected());
            this.nodefileText.setEnabled(false);
            this.nodefileText.setBackground(Color.LIGHT_GRAY);
            this.edgeFileText.setEnabled(false);
            this.edgeFileText.setBackground(Color.LIGHT_GRAY);
            this.nodeFileBrowseButton.setEnabled(false);
            this.edgeFileBrowseButton.setEnabled(false);

            this.rhostfield.setEnabled(true);
            this.ruserfield.setEnabled(true);
            this.rportfield.setEnabled(true);
            this.rdatabasefield.setEnabled(true);
            this.rtablename1efield.setEnabled(true);
            this.colnodeIdfield.setEnabled(true);
            this.colnodeLabelfield.setEnabled(true);
            this.rtablename2efield.setEnabled(true);
            this.coledge1field.setEnabled(true);
            this.coledge2field.setEnabled(true);
            this.colweightfield.setEnabled(true);
            this.rpasswdfield.setEnabled(true);

            this.rhostfield.setBackground(Color.WHITE);
            this.ruserfield.setBackground(Color.WHITE);
            this.rportfield.setBackground(Color.WHITE);
            this.rdatabasefield.setBackground(Color.WHITE);
            this.rtablename1efield.setBackground(Color.WHITE);
            this.colnodeIdfield.setBackground(Color.WHITE);
            this.colnodeLabelfield.setBackground(Color.WHITE);
            this.rtablename2efield.setBackground(Color.WHITE);
            this.coledge1field.setBackground(Color.WHITE);
            this.coledge2field.setBackground(Color.WHITE);
            this.colweightfield.setBackground(Color.WHITE);
            this.rpasswdfield.setBackground(Color.WHITE);
        } else if (ec.equals("UseFile")) {

            this.UseDB.setSelected(false);
            this.UseFile.setSelected(true);

            this.setFileInUsed(this.UseFile.isSelected());
            this.setDBInUsed(this.UseDB.isSelected());

            this.nodefileText.setEnabled(true);
            this.nodefileText.setBackground(Color.WHITE);
            this.edgeFileText.setEnabled(true);
            this.edgeFileText.setBackground(Color.WHITE);
            this.nodeFileBrowseButton.setEnabled(true);
            this.edgeFileBrowseButton.setEnabled(true);

            if (!this.isDBOutselected()) {
                this.rhostfield.setEnabled(false);
                this.ruserfield.setEnabled(false);
                this.rportfield.setEnabled(false);
                this.rdatabasefield.setEnabled(false);
                this.rpasswdfield.setEnabled(false);
                this.rhostfield.setBackground(Color.LIGHT_GRAY);
                this.ruserfield.setBackground(Color.LIGHT_GRAY);
                this.rportfield.setBackground(Color.LIGHT_GRAY);
                this.rdatabasefield.setBackground(Color.LIGHT_GRAY);
                this.rpasswdfield.setBackground(Color.LIGHT_GRAY);
            }
            this.rtablename1efield.setEnabled(false);
            this.colnodeIdfield.setEnabled(false);
            this.colnodeLabelfield.setEnabled(false);
            this.rtablename2efield.setEnabled(false);
            this.coledge1field.setEnabled(false);
            this.coledge2field.setEnabled(false);
            this.colweightfield.setEnabled(false);

            this.rtablename1efield.setBackground(Color.LIGHT_GRAY);
            this.colnodeIdfield.setBackground(Color.LIGHT_GRAY);
            this.colnodeLabelfield.setBackground(Color.LIGHT_GRAY);
            this.rtablename2efield.setBackground(Color.LIGHT_GRAY);
            this.coledge1field.setBackground(Color.LIGHT_GRAY);
            this.coledge2field.setBackground(Color.LIGHT_GRAY);
            this.colweightfield.setBackground(Color.LIGHT_GRAY);
        }
        if ((ec.equals("FileOutBox")) || (ec.equals("MultFileOutBox")) || (ec.equals("DBOutBox"))) {
            if (this.FileOutBox.isSelected()) {
                this.FileOutField.setEnabled(true);
                this.FileOutField.setBackground(Color.WHITE);
                this.FileOutBrowseButton.setEnabled(true);
            }

            if (ec.equals("DBOutBox")) {
                this.setDBOutselected(!this.isDBOutselected);
                if (this.DBOutBox.isSelected()) {
                    this.startFileDB.setEnabled(true);
                    this.otablenamefield.setEnabled(true);
                    this.otablenamefield.setBackground(Color.WHITE);
                    if (!this.isDBInUsed()) {
                        this.rhostfield.setEnabled(true);
                        this.ruserfield.setEnabled(true);
                        this.rportfield.setEnabled(true);
                        this.rdatabasefield.setEnabled(true);
                        this.rpasswdfield.setEnabled(true);
                        this.rhostfield.setBackground(Color.WHITE);
                        this.ruserfield.setBackground(Color.WHITE);
                        this.rportfield.setBackground(Color.WHITE);
                        this.rdatabasefield.setBackground(Color.WHITE);
                        this.rpasswdfield.setBackground(Color.WHITE);
                    }
                }
            }

            if ((!this.DBOutBox.isSelected()) && (!this.FileOutBox.isSelected())) {
                this.startFileDB.setEnabled(false);
            } else {
                this.startFileDB.setEnabled(true);
            }
        }

        if (this.isDBInUsed() || this.isDBOutselected()) {
            this.dbtake.setEnabled(true);
            this.dbdefault.setEnabled(true);
        } else {
            this.dbtake.setEnabled(false);
            this.dbdefault.setEnabled(false);
        }
        if (ec.equals("startFileDB")) {
            if (this.FileOutBox.isSelected()) {
                this.pl_LastFiles.setParam("nodelist-file", this.nodefileText.getText());
                this.pl_LastFiles.setParam("edgelist-file", this.edgeFileText.getText());
                Thread th1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Controller.this.startFileAndDB(1, this);
                    }
                });
                th1.start();
            }

            if (this.DBOutBox.isSelected()) {
                this.pl_LastFiles.setParam("nodelist-file", this.nodefileText.getText());
                this.pl_LastFiles.setParam("edgelist-file", this.edgeFileText.getText());
                // startDBWrite();
                // DBOutBox.setSelected(false);
                Thread th2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Controller.this.startFileAndDB(2, this);
                    }
                });
                th2.start();
            }
        }
    }

    // indicates critical situations (file- and DBout at the same time)
    private boolean semaphore = true;

    /**
     * Should handle and synchronize parallel file and db output.
     *
     * @param fileOrDB
     *            The number what to start with (file:1,db:2)
     * @param r
     */
    public synchronized void startFileAndDB(int fileOrDB, Runnable r) {
        this.dBInError = false;
        if (fileOrDB == 1) {
            // System.out.println("startFileWrite()");
            this.startFileWrite();
            while (/* !dBInError&& */!this.dBOutError && (this.semaphore || (!this.is_alg_started
                    || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                            || (!this.is_already_read_from_DB && this.UseDB.isSelected())))))) {
                try {
                    Thread.sleep(200);
                    // System.out.println("Started startFileWrite(): Still
                    // waiting for finishing.");
                    // r.wait();
                } catch (Exception e) {
                }
            }

        } else if (fileOrDB == 2) {
            // System.out.println("startDBWrite()");
            this.startDBWrite();
            while (/* !dBInError&& */!this.dBOutError && (this.semaphore || (!this.is_alg_started
                    || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                            || (!this.is_already_read_from_DB && this.UseDB.isSelected())))))) {
                try {
                    // r.wait();
                    Thread.sleep(200);
                    // System.out.println("Started startDBWrite(): Still waiting
                    // for finishing.");
                } catch (Exception e) {
                }
            }
        }
    } // end startFileAndDB

    /**
     * Sets, if used, recent graph files into textfields. Otherwise defaults
     * (examples).
     *
     */
    public void fillFilenames() {
        if (this.pl_LastFiles.getParam("nodelist-file") != null
                && this.pl_LastFiles.getParam("edgelist-file") != null) {
            this.nodefileText.setText(this.pl_LastFiles.getParam("nodelist-file"));
            this.edgeFileText.setText(this.pl_LastFiles.getParam("edgelist-file"));
        } else {
            this.nodefileText.setText("examples" + System.getProperty("file.separator") + "20nodes.txt");
            this.edgeFileText.setText("examples" + System.getProperty("file.separator") + "20edges.txt");
        }
    } // end fillFilenames

    /**
     * Set all Buttons either disabled during calculating chinesewhispers or
     * enabled when calculation finished.
     *
     * @param status
     *            The status to set.
     */
    public void setStatus(boolean status) {
        this.AsGraph.setEnabled(status);
        this.AsDia.setEnabled(status);
        this.AsFile.setEnabled(status);
        this.AsDB.setEnabled(status);

        this.FileOutBox.setEnabled(status);
        this.DBOutBox.setEnabled(status);
        this.startFileDB.setEnabled(status);
        if (status && (this.DBOutBox.isSelected() || this.FileOutBox.isSelected())) {
            this.startFileDB.setEnabled(true);
        } else {
            this.startFileDB.setEnabled(false);
        }
    } // end setStatus

    /// * OPEN FILE OLD
    public void openFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();

        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.is_alg_started = false;
            this.is_already_renumbered = false;
            if (e.getActionCommand().equals("nodelist")) {
                this.nodefileText.setText(chooser.getSelectedFile().getAbsolutePath());
            }
            if (e.getActionCommand().equals("edgelist")) {
                this.edgeFileText.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    } // end openFile

    /*
     * NEW openFile using commonChooser public void openFile(ActionEvent e){
     * String[] extension= new String[1]; extension[0]="*";
     *
     *
     * is_alg_started = false; is_already_renumbered=false;
     * if(e.getActionCommand().equals("nodelist")){ CommonFileChooser chooser =
     * new CommonFileChooser(extension, "node list"); String
     * selected=chooser.showDialogAndReturnFilename(this.P2, "Choose"); if
     * (selected!=null) {
     * nodefileText.setText(chooser.showDialogAndReturnFilename(this.P2,
     * "Choose")); } } if(e.getActionCommand().equals("edgelist")){
     * CommonFileChooser chooser = new CommonFileChooser(extension, "edge list"
     * ); String selected=chooser.showDialogAndReturnFilename(this.P2,
     * "Choose"); if (selected!=null) {
     * edgeFileText.setText(chooser.showDialogAndReturnFilename(this.P2,
     * "Choose")); } } }
     *
     *
     * //
     */

    public boolean Alg_isFileOrDBOut;

    /**
     * Says weither DBoutput Or Fileoutput is set.
     *
     * @return DB or File output set.
     */
    public boolean isAlg_isFileOrDBOut() {
        return this.Alg_isFileOrDBOut;
    }

    /**
     * Set DBoutput Or Fileoutput.
     *
     * @return The DB or File output.
     */
    public void setAlg_isFileOrDBOut(boolean Alg_isFileOrDBOut) {
        this.Alg_isFileOrDBOut = Alg_isFileOrDBOut;
    }

    /**
     * Starts the chinesewhispers algorithm as thread
     *
     * @param Alg_isFileOrDBOut
     *            If filedialog expected (write to file) true, false otherwise.
     */
    private void startALG(boolean Alg_isFileOrDBOut) {

        this.Alg_isFileOrDBOut = Alg_isFileOrDBOut;
        // this.isDBReadInUse=true;
        // System.out.println("startALG(boolean Alg_isFileOrDBOut)");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Controller.this.dBOutError = false;
                // dBInError = false;
                String nodeListString;
                String edgeListString;

                // get pparameters
                Controller.this.display_degree_temp = Integer
                        .parseInt((String) Controller.this.nodeDegreeSpinner.getValue());
                Controller.this.display_edges_temp = 1000;

                if (Controller.this.displayEdgesSpinner.getValue().equals("all")) {
                    Controller.this.display_edges_temp = -2;
                } else {
                    Controller.this.display_edges_temp = Integer
                            .parseInt((String) Controller.this.displayEdgesSpinner.getValue());
                }

                Controller.this.scale_temp = 1000;
                if (Controller.this.scaleSpinner.getValue().equals("600 x 600"))
                    Controller.this.scale_temp = 600;
                if (Controller.this.scaleSpinner.getValue().equals("1000 x 1000"))
                    Controller.this.scale_temp = 1000;
                if (Controller.this.scaleSpinner.getValue().equals("2000 x 2000"))
                    Controller.this.scale_temp = 2000;
                if (Controller.this.scaleSpinner.getValue().equals("3000 x 3000"))
                    Controller.this.scale_temp = 3000;
                if (Controller.this.scaleSpinner.getValue().equals("4000 x 4000"))
                    Controller.this.scale_temp = 4000;
                if (Controller.this.scaleSpinner.getValue().equals("5000 x 5000"))
                    Controller.this.scale_temp = 5000;

                int minWeight_temp = Integer.parseInt((String) Controller.this.minweightSpinner.getValue());
                int iterations_temp = Integer.parseInt((String) Controller.this.iterationsSpinner.getValue());

                Controller.this.display_sub_current = Controller.this.only_sub.isSelected();

                String Alg_param_temp = "";
                String Alg_param_value_temp = "";
                String Alg_param_keep = "";
                String Alg_param_mut1 = "";
                String Alg_param_mut2 = "";
                String Alg_param_update = "";

                String ALGOPT = Controller.this.Alg_param.getSelection().getActionCommand();
                if (ALGOPT.equals("top")) {
                    Alg_param_temp = "top";
                }
                if (ALGOPT.equals("dist log")) {
                    Alg_param_temp = "dist";
                    Alg_param_value_temp = "log";
                }
                if (ALGOPT.equals("dist nolog")) {
                    Alg_param_temp = "dist";
                    Alg_param_value_temp = "nolog";
                }
                if (ALGOPT.equals("vote")) {
                    Alg_param_temp = "vote";
                    Alg_param_value_temp = ((String) Controller.this.vote_value.getValue()).trim();
                } // fi vote

                Alg_param_keep = ((String) Controller.this.keep_value.getValue()).trim();
                Alg_param_mut1 = Controller.this.mutationParameter.getSelection().getActionCommand();
                Alg_param_mut2 = ((String) Controller.this.mut_value.getValue()).trim();
                Alg_param_update = Controller.this.Update_param.getSelection().getActionCommand();

                Controller.this.alg_param_current = Alg_param_temp;
                Controller.this.vote_value_current = Alg_param_value_temp;
                Controller.this.keepclass_value_current = Alg_param_keep;
                Controller.this.mut_option_current = Alg_param_mut1;
                Controller.this.mut_value_current = Alg_param_mut2;

                Controller.this.cw.isActive = true;
                if (!Controller.this.is_already_renumbered
                        || ((Controller.this.is_already_read_from_DB && Controller.this.UseFile.isSelected())
                                || (!Controller.this.is_already_read_from_DB && Controller.this.UseDB.isSelected()))) {
                    Controller.this.cw.isNumbered = false;
                    Controller.this.is_already_renumbered = true;
                }

                if (Controller.this.isDBInUsed()) {
                    nodeListString = NODES_TMP_FILENAME;
                    edgeListString = EDGES_TMP_FILENAME;
                    Controller.this.isDBReadInUse = true;

                    // if data is not yet read from DB
                    if (!Controller.this.is_already_read_from_DB) {

                        // delete old files
                        /*
                         * System.out.println("Deleting files with "
                         * +nodeListString+" and "+edgeListString);
                         *
                         * new File(nodeListString).delete(); new
                         * File(nodeListString+".renumbered").delete(); new
                         * File(nodeListString+".renumbered.bin").delete(); new
                         * File(nodeListString+".renumbered.idx").delete(); new
                         * File(nodeListString+".renumbered.meta").delete(); new
                         * File(nodeListString+".renumbered.tmp").delete(); new
                         * File(nodeListString+".bin").delete(); new
                         * File(nodeListString+".idx").delete();
                         *
                         * new File(edgeListString).delete(); new
                         * File(edgeListString+".renumbered").delete(); new
                         * File(edgeListString+".renumbered.bin").delete(); new
                         * File(edgeListString+".renumbered.idx").delete(); new
                         * File(edgeListString+".renumbered.meta").delete(); new
                         * File(edgeListString+".renumbered.tmp").delete(); new
                         * File(edgeListString+".bin").delete(); new
                         * File(edgeListString+".idx").delete();
                         */

                        String[] nodeColumns = { Controller.this.colnodeIdfield.getText(),
                                Controller.this.colnodeLabelfield.getText() };
                        String[] edgeColumns = { Controller.this.coledge1field.getText(),
                                Controller.this.coledge2field.getText(), Controller.this.colweightfield.getText() };
                        String pw = "";

                        for (int i = 0; i < Controller.this.rpasswdfield.getPassword().length; i++)
                            pw += Controller.this.rpasswdfield.getPassword()[i];

                        Controller.this.dbc_out = new DBConnect(Controller.this.rhostfield.getText(),
                                Controller.this.rdatabasefield.getText(), Controller.this.ruserfield.getText(), pw,
                                Integer.parseInt(Controller.this.rportfield.getText()),
                                Controller.this.rtablename1efield.getText(), nodeColumns,
                                Controller.this.rtablename2efield.getText(), edgeColumns);

                        try {
                            // System.out.println("Deleting files with
                            // "+nodeListString+" and "+edgeListString);

                            new File(nodeListString).delete();
                            new File(nodeListString + ".renumbered").delete();
                            new File(nodeListString + ".renumbered.bin").delete();
                            new File(nodeListString + ".renumbered.idx").delete();
                            new File(nodeListString + ".renumbered.meta").delete();
                            new File(nodeListString + ".renumbered.tmp").delete();
                            new File(nodeListString + ".bin").delete();
                            new File(nodeListString + ".idx").delete();

                            new File(edgeListString).delete();
                            new File(edgeListString + ".renumbered").delete();
                            new File(edgeListString + ".renumbered.bin").delete();
                            new File(edgeListString + ".renumbered.idx").delete();
                            new File(edgeListString + ".renumbered.meta").delete();
                            new File(edgeListString + ".renumbered.tmp").delete();
                            new File(edgeListString + ".bin").delete();
                            new File(edgeListString + ".idx").delete();

                            Controller.this.dbc_out.stillWorks = true;
                            Controller.this.dbc_out.getAllFromDbAndWriteIntoTempFiles();

                            Controller.this.is_already_read_from_DB = true;
                        } catch (IOWrapperException iow_e) {
                            System.err.println("Error while loading from DB!\nConnection failed!");
                            JOptionPane.showMessageDialog(null,
                                    "Error while loading from database!\nConnection failed!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            Controller.this.is_alg_started = false;
                            Controller.this.dBOutError = true;
                            Controller.this.dBInError = true;
                            Controller.this.dbc_out.stillWorks = false;
                            Controller.this.semaphore = true;
                            Controller.this.isFileOutStarted = false;
                            Controller.this.isDBOutStarted = false;
                            return;

                        } catch (IOIteratorException ioi_e) {
                            System.err.println("Error while loading from DB!\nCould not iterate over results!");
                            JOptionPane.showMessageDialog(null,
                                    "Error while loading from database!\nCould not iterate over results!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            Controller.this.is_alg_started = false;
                            Controller.this.dBOutError = true;
                            Controller.this.dBInError = true;
                            Controller.this.dbc_out.stillWorks = false;
                            Controller.this.semaphore = true;
                            Controller.this.isFileOutStarted = false;
                            Controller.this.isDBOutStarted = false;
                            return;
                        }

                    } // fi (!is_already_read_from_DB)
                } // fi (isDBInUsed())

                else if (Controller.this.isFileInUsed() && (Controller.this.edgeFileText.getText().length() != 0
                        && Controller.this.nodefileText.getText().length() != 0)) {
                    nodeListString = Controller.this.nodefileText.getText();
                    edgeListString = Controller.this.edgeFileText.getText();
                    Controller.this.is_already_read_from_DB = false;

                } else {
                    nodeListString = "examples" + System.getProperty("file.separator") + "allews.txt";
                    edgeListString = "examples" + System.getProperty("file.separator") + "skoll.txt";
                    Controller.this.is_already_read_from_DB = false;
                }

                // initialize
                double call_mut_value;
                double call_keep_value;

                call_keep_value = (new Double(Alg_param_keep)).doubleValue();
                call_mut_value = (new Double(Alg_param_mut2)).doubleValue();

                Controller.this.cw.isNumbered = Controller.this.FilesNumbered.isSelected();
                Controller.this.cw.graphInMemory = Controller.this.GraphInMemory.isSelected();
                Controller.this.cw.setCWGraph(nodeListString, edgeListString);
                Controller.this.cw.setCWParameters(minWeight_temp, Alg_param_temp, Alg_param_value_temp,
                        call_keep_value, Alg_param_mut1, call_mut_value, Alg_param_update, iterations_temp,
                        Controller.this.isAlg_isFileOrDBOut());

                Controller.this.cw.run();

                Controller.this.is_alg_started = true;
                Controller.this.semaphore = false;
                // setAlg_isFileOrDBOut(false);
            }// run
        });// runnable, thread

        t.start();
        Timer timer = new Timer();
        timer.schedule(new ObserveProgress(timer, t), 200, 2000);
    } // end startAlg

    /**
     * Calculate the graph for output.
     */
    public void startGraph() {
        this.setStatus(false);
        this.isGraphStarted = true;

        // if (Alg not started yet or Alg started, but not ready for graph) or
        // (Alg started but (already read from DB but now fileSelected) or (DB
        // selected but not read yet))
        if ((!this.is_alg_started || !this.isAlgStartedForGraph)
                || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                        || (!this.is_already_read_from_DB && this.UseDB.isSelected())))) {
            this.dBInError = false;
            this.startALG(false);
        } else {
            try {
                Thread t1 = new Thread(new MyGraphGUI(this.display_sub_current, (ChineseWhispers) this.cw.clone(),
                        this.display_edges_temp, this.scale_temp, 30, this.display_degree_temp));
                t1.start();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            this.setStatus(true);
            this.isGraphStarted = false;
            this.isAlgStartedForGraph = true;
        }
    } // end startGraph

    /**
     * Calculate diagrams for output.
     */
    public void startDia() {
        this.setStatus(false);
        this.isDiagStarted = true;
        if (!this.is_alg_started || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                || (!this.is_already_read_from_DB && this.UseDB.isSelected())))) {
            this.dBInError = false;
            this.startALG(true);
        } else {
            try {
                Thread t2 = new Thread(new Diagram((ChineseWhispers) this.cw.clone()));
                t2.start();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            this.setStatus(true);
            this.isDiagStarted = false;
        }
    }

    /**
     * Write clustering to file.
     *
     */
    public void startFileWrite() {
        this.setStatus(false);
        this.isFileOutStarted = true;
        if (!this.is_alg_started || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                || (!this.is_already_read_from_DB && this.UseDB.isSelected())))) {
            this.startALG(true);
        } else {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Controller.this.writeIntoFiles.setVisible(true);
                    Controller.this.isFileWriteInUse = true;
                    Controller.this.cw.setWritesOnlyFiles(true);
                    Controller.this.cw.writeFile(true, true, false);
                }// run()
            });// Thread
            t.start();
            Timer timer = new Timer();
            timer.schedule(new ObserveProgress(timer, t), 200, 2000);
            this.isFileOutStarted = false;
        }
    } // end startWriteFile

    /**
     * Write clustering into DB.
     *
     */
    public void startDBWrite() {
        this.setStatus(false);
        this.isDBOutStarted = true;
        if (!this.is_alg_started || (this.is_alg_started && ((this.is_already_read_from_DB && this.UseFile.isSelected())
                || (!this.is_already_read_from_DB && this.UseDB.isSelected())))) {
            this.startALG(true);
        } else {
            // setStatus(true);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Controller.this.isDBWriteInUse = true;
                    Controller.this.writeIntoDBProgress.setVisible(true);
                    Controller.this.cw.setWritesOnlyFiles(true);
                    Controller.this.cw.writeFile(false, false, false);
                    String pw = "";
                    for (int i = 0; i < Controller.this.rpasswdfield.getPassword().length; i++)
                        pw += Controller.this.rpasswdfield.getPassword()[i];

                    Controller.this.dbc_in = new DBConnect(Controller.this.rhostfield.getText(),
                            Controller.this.rdatabasefield.getText(), Controller.this.ruserfield.getText(), pw,
                            Integer.parseInt(Controller.this.rportfield.getText()),
                            Controller.this.otablenamefield.getText(), COLUMN_NAMES_FOR_DB_OUT);

                    // File: Controller.CLASSES_TMP_FILENAME
                    // Columns: Controller.COLUMN_NAMES_FOR_DB_OUT
                    try {
                        Controller.this.dbc_in.stillWorks = true;
                        Controller.this.dbc_in.maxProgerss = Controller.this.cw.countNodesWithClasses;
                        Controller.this.dbc_in.writeFromFileIntoDB();
                    } catch (IOWrapperException iow_e) {
                        System.err.println("Error while writing into DB!\nConnection failed!");
                        JOptionPane.showMessageDialog(null, "Error while writing into DB!\nConnection failed!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        Controller.this.dBInError = true;
                        Controller.this.dbc_in.stillWorks = false;
                        return;
                    } catch (IOIteratorException ioi_e) {
                        System.err.println("Error while writing into DB!\nCould not iterate over results!");
                        JOptionPane.showMessageDialog(null,
                                "Error while writing into DB!\nCould not iterate over results!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        Controller.this.dBInError = true;
                        Controller.this.dbc_in.stillWorks = false;
                        return;
                    }
                }// run()
            });// Thread

            t.start();
            Timer timer = new Timer();
            timer.schedule(new ObserveProgress(timer, t), 200, 2000);
            this.isDBOutStarted = false;
        }
    }

    /**
     * @return Returns the isDBInUsed.
     */
    public boolean isDBInUsed() {
        return this.isDBInUsed;
    }

    /**
     * @param isDBInUsed
     *            The isDBInUsed to set.
     */
    public void setDBInUsed(boolean isDBInUsed) {
        this.isDBInUsed = isDBInUsed;
    }

    /**
     * @return Returns the isDBOutselected.
     */
    public boolean isDBOutselected() {
        return this.isDBOutselected;
    }

    /**
     * @param isDBOutselected
     *            The isDBOutselected to set.
     */
    public void setDBOutselected(boolean isDBOutselected) {
        this.isDBOutselected = isDBOutselected;
    }

    /**
     * @return Returns the isDBValuesSet.
     */
    public boolean isDBValuesSet() {
        return this.isDBValuesSet;
    }

    /**
     * @param isDBValuesSet
     *            The isDBValuesSet to set.
     */
    public void setDBValuesSet(boolean isDBValuesSet) {
        this.isDBValuesSet = isDBValuesSet;
    }

    /**
     *
     *
     * @return Returns the isFileReadInUsed.
     */
    public boolean isFileInUsed() {
        return this.isFileReadInUsed;
    }

    /**
     *
     *
     * @param isFileReadInUsed
     *            The isFileReadInUsed to set.
     */
    public void setFileInUsed(boolean isFileInUsed) {
        this.isFileReadInUsed = isFileInUsed;
    }

    /**
     * @return Returns the isFileOutselected.
     */
    public boolean isFileOutselected() {
        return this.isFileOutselected;
    }

    /**
     * @param isFileOutselected
     *            The isFileOutselected to set.
     */
    public void setFileOutselected(boolean isFileOutselected) {
        this.isFileOutselected = isFileOutselected;
    }

    /**
     *
     * @author seb
     *
     */
    class ObserveProgress extends TimerTask {
        private final Timer  tImer;
        private final Thread tHread;

        /**
         * Handles threads for cw-algorithm and progess-bar.
         *
         * @param tImer
         *            The Timer to cancel when ready.
         * @param tHread
         *            The Thread to watch.
         */
        public ObserveProgress(Timer tImer, Thread tHread) {
            this.tImer = tImer;
            this.tHread = tHread;
        }

        /**
         * Checks if cw-algorithm is still running. If ready, creates new
         * threads for graphs <br>
         * and/or diagrams and sets the progress-bar visible, valued if ready
         * not visible.
         */
        @Override
        public void run() {

            // Thread: writes files
            if (Controller.this.isFileWriteInUse) {
                // System.out.println("isFileWriteInUse");

                if (Controller.this.cw.getWritesOnlyFiles()) {// as long as
                    // thread is
                    // running
                    int i = Controller.this.cw.writeFileProgress / 2;
                    Controller.this.writeIntoFiles.setValue(i);
                    Controller.this.writeIntoFiles.repaint();
                } else {
                    this.tImer.cancel();
                    System.out.println("*\tWriting into files finished.");
                    Controller.this.isFileWriteInUse = false;
                    Controller.this.writeIntoFiles.setVisible(false);
                    if (!Controller.this.isDBWriteInUse)
                        Controller.this.setStatus(true);
                }
            } // fi isFileWriteInUse
            else
                // Thread: writes from file in DB
                if (Controller.this.isDBWriteInUse) {// as long write process in DB
                    // runs
                    // System.out.println("isDBWriteInUse");

                    // critical, because dbc_in is not available as object in the
                    // beginning
                    boolean dbc_inStillWork;
                    try {
                        dbc_inStillWork = Controller.this.dbc_in.stillWorks;
                    } catch (Exception e) {
                        dbc_inStillWork = false;
                    }

                    if (Controller.this.cw.getWritesOnlyFiles() || dbc_inStillWork) {// as
                        // long
                        // as
                        // the
                        // thread
                        // is
                        // running
                        int i;
                        if (dbc_inStillWork) {
                            i = (Controller.this.cw.writeFileProgress / 2)
                                    + ((Controller.this.dbc_in.singleProgress * 50) / Controller.this.dbc_in.maxProgerss);
                        } else {
                            i = Controller.this.cw.writeFileProgress / 2;
                        }
                        Controller.this.writeIntoDBProgress.setValue(i);
                        Controller.this.writeIntoDBProgress.repaint();
                    } else {
                        this.tImer.cancel();
                        System.out.println("*\tWriting into DB finished.");
                        Controller.this.isDBWriteInUse = false;
                        Controller.this.writeIntoDBProgress.setVisible(false);
                        if (!Controller.this.isFileWriteInUse)
                            Controller.this.setStatus(true);

                        if (Controller.this.dBInError) {
                            this.tImer.cancel();
                            System.err.print("\t FAILED!\n");
                            Controller.this.setStatus(true);
                            Controller.this.dBInError = false;

                            // dBOutError=false;
                        }

                    } // esle
                } else
                    // Thread: lloads from DB and writes in nodelist.tmp and
                    // edgelist.tmp
                    if (Controller.this.isDBReadInUse) {// as lng as read process is
                        // active
                        // System.out.println("isDBReadInUse");

                        Controller.this.loadFromDBProgress.setVisible(true);
                        if (Controller.this.dbc_out.stillWorks) {
                            // System.out.println("Loading from DB active ...");
                            int i = (Controller.this.dbc_out.singleProgress * 100) / Controller.this.dbc_out.maxProgerss;
                            if (i < 1)
                                i = 1;
                            Controller.this.loadFromDBProgress.setValue(i);
                            Controller.this.loadFromDBProgress.repaint();
                        } else {
                            // tImer.cancel();
                            System.out.println("*\tLoading from DB finished.");
                            Controller.this.isDBReadInUse = false;
                            Controller.this.loadFromDBProgress.setVisible(false);

                            if (Controller.this.dBOutError) {
                                this.tImer.cancel();
                                System.err.print("\t FAILED!\n");

                                Controller.this.setStatus(true);

                                Controller.this.isGraphStarted = false;
                                Controller.this.isDiagStarted = false;
                                Controller.this.isFileWriteInUse = false;
                                Controller.this.dBOutError = false;

                                // dBInError=true;

                            }
                        }
                    }
            // Thread: runs Chinese Whispers
                    else if (!Controller.this.dBOutError && !Controller.this.dBInError) {
                        // System.out.println("!dBOutError&&!dBInError");

                        Controller.this.calculateCWProgress.setVisible(true);
                        Controller.this.CalculateCWLabel.setVisible(true);

                        if (Controller.this.cw.isActive) {
                            if (Controller.this.cw.getIteration() > 0)
                                Controller.this.calculateCWProgress.setValue(
                                        (Controller.this.cw.getCurrentIteration() * 100) / Controller.this.cw.getIteration());
                            Controller.this.calculateCWProgress.repaint();
                        } else { // cw not active
                            this.tImer.cancel();
                            System.out.println("*\tCalculating algorithm finished.");
                            Controller.this.calculateCWProgress.setVisible(false);
                            Controller.this.CalculateCWLabel.setVisible(false);

                            if (Controller.this.isGraphStarted) {
                                // System.out.println("isGraphStarted");
                                Controller.this.setStatus(true);
                                Controller.this.isAlgStartedForGraph = true;
                                Controller.this.isGraphStarted = false;
                                try {
                                    Thread t1 = new Thread(new MyGraphGUI(Controller.this.display_sub_current,
                                            (ChineseWhispers) Controller.this.cw.clone(), Controller.this.display_edges_temp,
                                            Controller.this.scale_temp, 30, Controller.this.display_degree_temp));
                                    t1.start();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                            } // fi isGraphStarted

                            if (Controller.this.isDiagStarted) {
                                // System.out.println("isDiagStarted");
                                Controller.this.setStatus(true);
                                Controller.this.isAlgStartedForGraph = false;
                                Controller.this.isDiagStarted = false;
                                try {
                                    Thread t2 = new Thread(new Diagram((ChineseWhispers) Controller.this.cw.clone()));
                                    t2.start();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }

                            } // fi isDiagStarted
                            if (Controller.this.isFileOutStarted) {
                                // System.out.println("isFileOutStarted");
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Controller.this.isFileWriteInUse = true;
                                        Controller.this.writeIntoFiles.setVisible(true);
                                        Controller.this.isAlgStartedForGraph = false;
                                        Controller.this.cw.setWritesOnlyFiles(true);
                                        Controller.this.cw.writeFile(true, true, false);
                                        // isFileWriteInUse=false;
                                    }// run()
                                });// Thread

                                t.start();
                                Timer timer = new Timer();
                                timer.schedule(new ObserveProgress(timer, t), 200, 2000);
                                Controller.this.isFileOutStarted = false;
                            } // fi isFileOutStarted
                            if (Controller.this.isDBOutStarted) {
                                // System.out.println("isDBOutStarted");
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Controller.this.isDBWriteInUse = true;
                                        Controller.this.writeIntoDBProgress.setVisible(true);
                                        Controller.this.isAlgStartedForGraph = false;
                                        Controller.this.cw.setWritesOnlyFiles(true);
                                        Controller.this.cw.writeFile(false, false, false);
                                        String pw = "";
                                        for (int i = 0; i < Controller.this.rpasswdfield.getPassword().length; i++)
                                            pw += Controller.this.rpasswdfield.getPassword()[i];

                                        Controller.this.dbc_in = new DBConnect(Controller.this.rhostfield.getText(),
                                                Controller.this.rdatabasefield.getText(), Controller.this.ruserfield.getText(),
                                                pw, Integer.parseInt(Controller.this.rportfield.getText()),
                                                Controller.this.otablenamefield.getText(), COLUMN_NAMES_FOR_DB_OUT);

                                        // File: Controller.CLASSES_TMP_FILENAME
                                        // Columns: Controller.COLUMN_NAMES_FOR_DB_OUT
                                        try {
                                            Controller.this.dbc_in.stillWorks = true;
                                            Controller.this.dbc_in.maxProgerss = Controller.this.cw.countNodesWithClasses;
                                            Controller.this.dbc_in.writeFromFileIntoDB();
                                        } catch (IOWrapperException iow_e) {
                                            System.err.println("Error while writing into DB!\nConnection failed!");
                                            JOptionPane.showMessageDialog(null,
                                                    "Error while writing into DB!\nConnection failed!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                            Controller.this.dBInError = true;
                                            Controller.this.dbc_in.stillWorks = false;
                                            return;
                                        } // end catch WrapperException
                                        catch (IOIteratorException ioi_e) {
                                            System.err.println("Error while writing into DB!\nCould not iterate over results!");
                                            JOptionPane.showMessageDialog(null,
                                                    "Error while writing into DB!\nCould not iterate over results!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                            Controller.this.dBInError = true;
                                            Controller.this.dbc_in.stillWorks = false;
                                            return;
                                        }
                                        Controller.this.isDBOutStarted = false;
                                    }// run()
                                });// Thread

                                t.start();
                                Timer timer = new Timer();
                                timer.schedule(new ObserveProgress(timer, t), 200, 2000);
                                Controller.this.isDBOutStarted = false;
                            } // end isDBOutStarted

                        } // esle CW not active

                    } // fi (!dBOutError && !dBInError)

        } // end void run()

    } // end class observeProgress

} // end Class Controller
