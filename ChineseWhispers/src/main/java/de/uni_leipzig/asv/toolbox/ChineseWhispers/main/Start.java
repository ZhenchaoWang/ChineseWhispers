/*
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
 */


/*  Chinese Whispers Main Class
 *  Last version number indicates version
 *
 *  Version 1.0
 *  Version 1.0.1: reparied DB out, which was empty in 1.0
 *
 *
 **/

package de.uni_leipzig.asv.toolbox.ChineseWhispers.main;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.io.File;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.db.DBConnect;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.gui.Controller;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.parameterHandler.PropertyLoader;
import de.uni_leipzig.asv.utils.IOIteratorException;
import de.uni_leipzig.asv.utils.IOWrapperException;

/**
 * @author Rocco & seb
 *
 */
public class Start {

    // version
    private static final String CURRENT_VERSION="1.01";
    
    //debugging
    private static boolean d=false;
    
    
    
    // DB settings
    private String hostnameString;
    private String databaseString; 
    private String usernameString; 
    private String passwdString;
    private String nodeListDBTableString;
    private String NodeListDBColnodeIDString;
    private String NodeListDBColLabelString;
    private String EdgeListDBTableString;
    private String EdgeListDBColID1String;
    private String EdgeListDBColID2String;
    private String EdgeListDBColWeightString;
    private String ResultTableDBString;
    private int portNr;
	
    public String NodeListFile;
    public String EdgeListFile;
    public String ALGOPT="";
    public String Alg_para_update="continuous";
    public String outFileName;

    boolean isFileOut=false;
    boolean isDBOut=false;

    boolean filesAreSorted=false;
    boolean graphInMemory=true;

    PropertyLoader pl_expert = new PropertyLoader("CW_ExpertProperties.ini");
    PropertyLoader pl_DB = new PropertyLoader("CW_DBPoperties.ini.");
    PropertyLoader pl_LastFiles = new PropertyLoader("CW_LastFiles.ini");
    
    boolean isDBValuesSet=false;
    boolean isFileInUsed=false;
    boolean isDBInUsed=false;
    boolean gotFileInNames=false;
    boolean gotFileOutNames=false;
    int Alg_iterations_temp = 20;
    int alg_minweight_temp = 0;
    double call_keep_value=0.0;
    String Alg_param_mut1="dec";
    double call_mut_value=1.0;
    
    
    
    static String help = "Help for Chinese Whispers"+
    	"\n\n\tjava -jar -Xmx512M cw.jar [-H -h --help [-D | -F [-i filname1 filename2]] " +
    	"-a algorithm-option -t weight threshold -u update-strategy "+
    	"[-d iterations] [-o filename] [-O] [-S] [-R]]"+
    	"\n\n\t" +
    	"No options starts GUI.\n\n\t"+
    	"-H | -h | --help\tWrites out this Help.\n\t"+
    	"-D\tUse database specified in CW_DBproperties.ini as input.\n\t"+
    	"-F\tUse files specified by -i as input.\n\t"+
    	"-i\tUse files as input.\n\t\t\t"+
    	"filename1\tThe node list 2-col.\n\t\t\tfilename2\tThe edge list 3-col.\n\t"+
    	"-a\tSets the algorithm options\n\t\t\t"+
    	"\"top\"\n\t\t\t" +
    	"\"dist_nolog\"\n\t\t\t" +
    	"\"dist_log\"\n\t\t\t" +
    	"\"vote x\" with x in [0.0,1.0]\n\t"+
        "-t\tWeight threshold (default 0)\n\t"+
        "-k\tKeep class rate (default 0.0)\n\t"+
        "-m\tMutation mode [dec|constant] value(pos.real) \n\t"+    
    	"-d\tNumber of iterations x>0 (default x=20).\n\t"+
    	"-o\tWrites clustering to filename.\n\t\t\t" +
    	"filename\t The filename for output.\n\t"+
    	"-O\tWrites clustering into database specified in CW_DBproperties.ini.\n\t"+
    	"-S\tDo not renumber input.\n\t"+
        "-R\tkeep graph on disk (large graphs).\n";

    
    /**  
     * 
     * @param args The input values.
     */
	public static void main(String[] args) {
	    boolean readFromFile=false;
	    boolean readFromDB=false;
	    boolean gotError=false;
	    //GUI
	    if(args.length<1){
	        new Start().mainVisual();
	    }
	    else{
	        boolean algIsGiven = false;
                boolean stratIsGiven = false;
                boolean mutIsGiven = false;
                
	        Start s = new Start();
	        for (int i = 0; i < args.length; ) {
	            if (d) System.out.println("Processing arg"+i+"="+args[i]);
	            if(args[i].equals("-F")) {//read from File
	                i++;
	                if(readFromDB){
	                    System.err.println("Please use only one, either DB (-D) or file (-F) as input!\n");
	                    gotError=true;
	                }
	                else{
	                    readFromFile=true;
	                    s.setFileInUsed(true);
	                }
	            }
	            else if(args[i].equals("-D")){//read from DB
	                i++;
	                if(readFromFile){
	                    System.err.println("Please use only one, either DB (-D) or file (-F) as input!\n");
	                    gotError=true;
	                }
	                else{
	                    readFromDB=true;
	                    s.setDBInUsed(true);
	                }
	            }
	            else if(args[i].equals("-i")){//files 1.nodes, 2.edges
	                if(!readFromFile || (i+2>args.length) || args[i+1].startsWith("-")||args[i+2].startsWith("-") || readFromDB){
	                    System.err.println("Input Error!\n\tUsage for file input:"+
	                    	"\n\t\tNeed two filenames as input -F -i nodelist-file (2 cols), edgelist-file (3 cols)!\n");
	                    gotError=true;
	                }
	                else{
	                    s.NodeListFile=args[i+1];
	                    s.EdgeListFile=args[i+2];
	                    s.gotFileInNames=true;
	                }
	                i+=3;
	            }

	            else if(args[i].equals("-a")){//AlgOpt (top, dist_log, dist_nolog, vote x.x)
	                if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -a [\"top\"|\"dist_nolog\"|\"dist_log\"|\"vote x\"] with x in [0.0,1.0]!\n");
	                    gotError=true;
	                }
	                else{
	                    s.ALGOPT=args[i+1];
	                    algIsGiven = true;
	                }
	                i+=2;
                        if(i<=args.length) if (!args[i].startsWith("-")) {
                            s.ALGOPT=s.ALGOPT+" "+args[i];    
                            i+=1;
                        }
	            }
                    else if(args[i].equals("-m")){//mutation mode: mode and value
                        if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -m [dec|constant] [0..]!\n");
	                    gotError=true;
	                }
                        if((i+2>args.length) || args[i+2].startsWith("-")){
	                    System.err.println("Please use -m [dec|constant] [0..]!\n");
	                    gotError=true;
	                }
	                else{
	                    try{
	                        s.call_mut_value=Double.parseDouble(args[i+2]);
	                    }
	                    catch(Exception e){System.out.println("-m "+args[i+2]+" is not a number! Now running with 0.0!\n");}

                            
                            s.Alg_param_mut1=args[i+1];
	                    mutIsGiven = true;
	                }
	                i+=3;
	            }
                    else if(args[i].equals("-o")){//fileoutname
	                if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -o outfilename!\n");
	                    gotError=true;
	                }
	                else{
	                    s.setFileOut(true);
	                    s.outFileName=args[i+1];
	                    s.gotFileOutNames=true;
	                }
		            i+=2;
	            }
	            else if(args[i].equals("-O")){//DB-OUT
	                if(i+1<args.length&&!args[i+1].startsWith("-")){
	                    System.err.println("Please use only -O for database output!\n");
	                    gotError=true;
	                }
	                else{
	                    s.setDBOut(true);
	                    //s.outFileName=args[i];
	                }
		            i++;
	            }
	            else if(args[i].equals("-S")){//sort
	                if(i+1<args.length&&!args[i+1].startsWith("-")){
	                    System.err.println("Please use only -S for not to sort input!\n");
	                    gotError=true;
	                }
	                else{
	                    s.filesAreSorted=true;
	                }
		            i++;
	            }
                    else if(args[i].equals("-R")){//sort
	                if(i+1<args.length&&!args[i+1].startsWith("-")){
	                    System.err.println("Please use only -R for keep graph in RAM!\n");
	                    gotError=true;
	                }
	                else{
	                    s.graphInMemory=false;
	                }
		            i++;
	            }
                    
                    
	            else if(args[i].equals("-d")){//iterations 
	                if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -d x with x in [1,2,..] to set iterations (default 20)!\n");
	                    gotError=true;
	                }
	                else{
	                    try{
	                        s.Alg_iterations_temp=Integer.parseInt(args[i+1]);
	                    }
	                    catch(Exception e){System.out.println("-d "+args[i+1]+" is not a number! Now running with -d 20!\n");}
	                }
	                i+=2;
	            }
	            else if(args[i].equals("-t")){//min weight threshold 
	                if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -t x with x in [0,1,2,..] to set min weight threshold (default 0)!\n");
	                    gotError=true;
	                }
	                else{
	                    try{
	                        s.alg_minweight_temp=Integer.parseInt(args[i+1]);
	                    }
	                    catch(Exception e){System.out.println("-t "+args[i+1]+" is not a number! Now running with -t 0!\n");}
	                }
	                i+=2;
	            }
	            else if(args[i].equals("-k")){//keep class rate
	                if((i+1>args.length) || args[i+1].startsWith("-")){
	                    System.err.println("Please use -k x with x in [0..1] to set keep class rate (default 0.0)!\n");
	                    gotError=true;
	                }
	                else{
	                    try{
	                        s.call_keep_value=Double.parseDouble(args[i+1]);
	                    }
	                    catch(Exception e){System.out.println("-k "+args[i+1]+" is not a number! Now running with -k 0.0!\n");}
	                }
	                i+=2;
	            }

                    
                    else if(args[i].equals("-H")||args[i].equals("-h")||args[i].equals("--help")){
	                System.out.println(help);
	                System.exit(1);
	            } else { // parameter not recognized
                        System.err.println("Skipping parameter "+args[i]+": not recognized!");
                        i++;
                    }
	            if(gotError){
	                System.err.println(help);
	                System.exit(0);
	            }
	        }
            if(!algIsGiven){
                System.err.println("Using default option -a top!\n");
                algIsGiven=true;
                s.ALGOPT="top";
             }
            if(!stratIsGiven){
                System.err.println("Using default option -u continuous!\n");
                stratIsGiven=true;
                s.Alg_para_update="continuous";
             }
            if(!mutIsGiven){
                System.err.println("Using default option -m constant 0.0!\n");
                mutIsGiven=true;
                s.Alg_param_mut1="constant";
                s.call_mut_value=0.0;
             }
                
                
           //starts run of CW
	    s.mainConsole();
	    }
	}
	
    /**
     * @return Returns the isDBOut.
     */
    public boolean isDBOut() {
        return isDBOut;
    }
    /**
     * @param isDBOut The isDBOut to set.
     */
    public void setDBOut(boolean isDBOut) {
        this.isDBOut = isDBOut;
    }
    /**
     * @return Returns the isFileOut.
     */
    public boolean isFileOut() {
        return isFileOut;
    }
    /**
     * @param isFileOut The isFileOut to set.
     */
    public void setFileOut(boolean isFileOut) {
        this.isFileOut = isFileOut;
    }
    /**
     * @return Returns the isDBInUsed.
     */
    public boolean isDBInUsed() {
        return isDBInUsed;
    }
    /**
     * @param isDBInUsed The isDBInUsed to set.
     */
    public void setDBInUsed(boolean isDBInUsed) {
        this.isDBInUsed = isDBInUsed;
    }
    /**
     * @return Returns the isFileInUsed.
     */
    public boolean isFileInUsed() {
        return isFileInUsed;
    }
    /**
     * @param isFileInUsed The isFileInUsed to set.
     */
    public void setFileInUsed(boolean isFileInUsed) {
        this.isFileInUsed = isFileInUsed;
    }
	/**
	 * Constructs standalone Chinese Whispers with GUI.
	 *
	 */
	public void mainVisual(){
		JFrame mainWindow = new JFrame();
		mainWindow.setTitle("Chinese Whispers "+CURRENT_VERSION);
		mainWindow.setLocation(100, 100);
		mainWindow.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		mainWindow.getContentPane().add(new Controller());
		mainWindow.setSize(750, 560);
		mainWindow.setVisible(true);
		// mainWindow.show(); deprecated
                mainWindow.setEnabled(true);
	}
	/**
	 * Returns the GUI-object of Chinese Whispers to add on a JFrame.<br>
	 * Usage:<br>
	 * JTabbedPane jtp = new(de.uni_leipzig.asv.toolbox.ChineseWhispers.src.main.Start().getTabbedPane());<br>
	 * JFrame jf = new JFrame();<br>
	 * jf.setSize(600,440);<br>
	 * jf.add(jft);
	 * @return The GUI-object.
	 */
	public JTabbedPane getTabbedPane(){
	    return  new Controller();
	}
	/**
	 * Constructs standalone Chinese Whispers without GUI.
	 *
	 */
	public void mainConsole(){
	    
	    boolean dBOutError=false;
	    boolean dBInError=false;
	    
	    boolean gotAlgParam=false;
	    boolean gotUpdateParam=false;
	    
	    String Alg_param_temp="";
	    String Alg_param_value_temp="";
	    String Update_param="";

            
	    ChineseWhispers cw = new ChineseWhispers();
	    

	    // Check validity of ALGOPT	
	    if(ALGOPT.equals("top")) {Alg_param_temp="top"; gotAlgParam=true;}
	    if(ALGOPT.equals("dist_log")) {Alg_param_temp="dist"; Alg_param_value_temp="log";gotAlgParam=true;}
	    if(ALGOPT.equals("dist_nolog")) {Alg_param_temp="dist"; Alg_param_value_temp="nolog";gotAlgParam=true;}
	    if(ALGOPT.startsWith("vote")){
	        Alg_param_temp="vote"; 
	        Alg_param_value_temp=ALGOPT.split(" ")[1];
	        gotAlgParam=true;
	    }
	    if(!gotAlgParam){
	        System.err.println("Error!\nWrong algorithm parameter:\tPlease use -a [\"top\"|\"dist_nolog\"|\"dist_log\"|\"vote x\"] with x in [0.0,1.0]!");
	        System.exit(0);
	    } // fi !gotAlgPAram
            
            //Check validity of Alg_para_update
            if(Alg_para_update.equals("continuous")||Alg_para_update.equals("c")) {Update_param="continuous";gotUpdateParam=true;}
            if(Alg_para_update.equals("stepwise")||Alg_para_update.equals("s")) {Update_param="stepwise";gotUpdateParam=true;}
	    if(!gotUpdateParam){
	        System.err.println("Error!\nWrong update strategy parameter:\tPlease use -u [continuous|stepwise]!");
	        System.exit(0);
	    } // fi !gotUpdatePAram
            
            
	    if(isDBInUsed()){
		    
                if(!getSavedDBValues()){
                    System.err.println("Not all required DB-Connection values are provided!");
                    System.exit(0);
                }

                NodeListFile = Controller.NODES_TMP_FILENAME;
                EdgeListFile  = Controller.EDGES_TMP_FILENAME;
                String [] NodeListdbInfo={NodeListDBColnodeIDString,NodeListDBColLabelString}; 
                String [] EdgeListDBInfo={EdgeListDBColID1String,EdgeListDBColID2String,EdgeListDBColWeightString};
                DBConnect dbc_out = new DBConnect(
                    hostnameString, 
                    databaseString,
                    usernameString, 
                    passwdString,
                    portNr,
                    nodeListDBTableString, 
                    NodeListdbInfo,
                    EdgeListDBTableString,
                    EdgeListDBInfo
                );
                try{
                    
                    new File(nodeListDBTableString).delete();
                    new File(nodeListDBTableString+".renumbered").delete();
                    new File(nodeListDBTableString+".renumbered.bin").delete();
                    new File(nodeListDBTableString+".renumbered.idx").delete();
                    new File(nodeListDBTableString+".renumbered.meta").delete();
                    new File(nodeListDBTableString+".renumbered.tmp").delete();                                
                    new File(nodeListDBTableString+".bin").delete();
                    new File(nodeListDBTableString+".idx").delete();                            

                    new File(EdgeListDBTableString).delete();
                    new File(EdgeListDBTableString+".renumbered").delete();                                
                    new File(EdgeListDBTableString+".renumbered.bin").delete();                               
                    new File(EdgeListDBTableString+".renumbered.idx").delete();
                    new File(EdgeListDBTableString+".renumbered.meta").delete();
                    new File(EdgeListDBTableString+".renumbered.tmp").delete();                                  
                    new File(EdgeListDBTableString+".bin").delete();                               
                    new File(EdgeListDBTableString+".idx").delete();   
                    
                    dbc_out.stillWorks=true;
                    dbc_out.getAllFromDbAndWriteIntoTempFiles();
                    
          
                    
                    while(dbc_out.stillWorks){
                        try{
                            Thread.sleep(200);
                        }
                        catch(Exception e){System.err.println("Problems with Thread while reading from DB!");}
                    }
                }
                catch(IOWrapperException iow_e){
                    System.err.println("Error while loading from DB!\nConnection failed!");
                    dBOutError=true;
                    return;
                }
                catch(IOIteratorException ioi_e){
                    System.err.println("Error while loading from DB!\nCouldn't iterate over results!");
                    dBOutError=true;
                    return;
                }
            }//if(isDBInUsed())
	    else if(!isFileInUsed()||!gotFileInNames){
	        System.err.println("Error:\nPlease use correct input values!");
	        System.exit(0);
	    }
		
            //initialize
            //cw.start(NodeListFile,EdgeListFile,0,Alg_param_temp,Alg_param_value_temp,alg_iterations_temp,true);
            
            
            // setgraph, setParameters
            
            cw.isNumbered=filesAreSorted;
            cw.graphInMemory=graphInMemory;
            cw.setCWGraph(NodeListFile,EdgeListFile);
	    cw.setCWParameters(alg_minweight_temp,Alg_param_temp,Alg_param_value_temp, call_keep_value, Alg_param_mut1, call_mut_value, Alg_para_update, Alg_iterations_temp,true);
 
            cw.run();

            //FileOut
            if(isFileOut()){
                cw.consoleChooserString_colors=outFileName;
                cw.consoleChooserString_colors_read=outFileName+".read";
                cw.writeFile(false,true,true);
            }
            //DB-Out
            else if(isDBOut()){
                if(!getSavedDBValues()){
                    System.err.println("Not all needed DB-Connection values are filled in!");
                    System.exit(0);
                }
                cw.writeFile(false,false,false);

                DBConnect dbc_in=new DBConnect(
                    hostnameString, 
                    databaseString,
                    usernameString, 
                    passwdString,
                    portNr,
                    ResultTableDBString, 
                    Controller.COLUMN_NAMES_FOR_DB_OUT
                );
                dbc_in.stillWorks=true;
                //File: Controller.COLORS_TMP_FILENAME
                //Columns: Controller.COLUMN_NAMES_FOR_DB_OUT
                try{
                     dbc_in.writeFromFileIntoDB();
                     try{
                         while(dbc_in.stillWorks){
                             Thread.sleep(200);
                         }
                     }
                     catch(Exception e){System.err.println("Problems with Thread while writing into DB!");}
                }
                catch(IOWrapperException iow_e){
                            System.err.println("Error while writing into DB!\nConnection failed!");
                            dBInError=true;
                            return;
                }
                catch(IOIteratorException ioi_e){
                        System.err.println("Error while writing into DB!\nCouldn't iterate over results!");
                        dBInError=true;
                        return;
                }
            } // fi is DBOut
            System.out.println("DONE.");
	} // end MainConsole

	/**
	 * Get all DB-values from property-file and set them as current values.
	 *@return true if all DB-connection values are defined in property-file.
	 */
	private boolean getSavedDBValues(){
		
	    boolean isSet []=new boolean[12];
		for(int i=0;i<12;i++)isSet[i]=false;
		
		if(pl_DB.getParam("Hostname")!= null){
		    hostnameString= pl_DB.getParam("Hostname"); 
		    isSet[0]=true;
		}	
		if(pl_DB.getParam("Port")!= null){
		    try{
		        portNr= Integer.parseInt(pl_DB.getParam("Port"));
		    }catch(NumberFormatException e){
		        portNr=3306;
		    }
		    isSet[1]=true;
		}
		if(pl_DB.getParam("Database")!= null){databaseString= pl_DB.getParam("Database"); isSet[2]=true;}
		if(pl_DB.getParam("Username")!= null){usernameString= pl_DB.getParam("Username"); isSet[3]=true;}
		if(pl_DB.getParam("Password")!= null){passwdString= pl_DB.getParam("Password"); }
		if(pl_DB.getParam("WdWdList")!= null){nodeListDBTableString= pl_DB.getParam("WdWdList");isSet[4]=true;}
		if(pl_DB.getParam("WdWdListColWnr")!= null){NodeListDBColnodeIDString= pl_DB.getParam("WdWdListColWnr");isSet[5]=true;}
		if(pl_DB.getParam("WdWdListColWbsp")!= null){NodeListDBColLabelString= pl_DB.getParam("WdWdListColWbsp");isSet[6]=true;}
		if(pl_DB.getParam("WdWdSList")!= null){EdgeListDBTableString= pl_DB.getParam("WdWdSList");isSet[7]=true;}
		if(pl_DB.getParam("WdWdSListColWnr1")!= null){EdgeListDBColID1String = pl_DB.getParam("WdWdSListColWnr1");isSet[8]=true;}
		if(pl_DB.getParam("WdWdSListColWnr2")!= null){EdgeListDBColID2String = pl_DB.getParam("WdWdSListColWnr2");isSet[9]=true;}
		if(pl_DB.getParam("WdWdSListColSig")!= null){EdgeListDBColWeightString = pl_DB.getParam("WdWdSListColSig");isSet[10]=true;}
		if(pl_DB.getParam("WdCoCo")!= null){ResultTableDBString = pl_DB.getParam("WdCoCo");isSet[11]=true;}

		for(int i=0;i<12;i++){
		    //if at least on parameter is missing
		    if(!isSet[i]) return false;
		}
		//if all parameters could be found:
		return true;
	} // end getSavedDBValues 
} // end class Start
