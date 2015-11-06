/*
 * Created on 24.06.2005
 *
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.db;
import java.util.Timer;
import java.util.TimerTask;

import de.uni_leipzig.asv.toolbox.ChineseWhispers.gui.Controller;
import de.uni_leipzig.asv.utils.*;

/**
 * @author rocco & seb
 *
 */
public class DBConnect {
    //DBInfo for input from DB
    
    public boolean stillWorks = false;
    public int maxProgerss=100;
    public int singleProgress = 1;
    
    private String hostname, dbname, username, password, nodetable, edgetable, outtable; 
    private String [] nodeCols, edgeCols, outcols;
    private int port;
    
    public boolean writeIntoDB = false;
    public boolean readFromDB = false;
    
    private IOWrapper iow;
    private IOIterator iter;
    /**
     * Set the values for DB-connection to input from DB.
     * 
     * 
     * @param hostname The hostname.
     * @param dbname The databasename.
     * @param username The username.
     * @param password The password.
     * @param port The port of db-server.
     * @param nodetable The nodelist tablename.
     * @param nodeCols The nodelist columnnames.
     * @param edgetable The edgelist tablename.
     * @param edgeCols The edgelist columnnames.
     */
    public DBConnect(String hostname, String dbname, String username, String password, int port, String _nodetable, String [] _nodeCols, String _edgetable, String [] _edgeCols){
        this.stillWorks=true;
        this.hostname=hostname;
        this.dbname=dbname;
        this.username=username;
        this.password=password;
        this.nodetable=_nodetable;
        this.edgetable=_edgetable; 
        this.nodeCols=_nodeCols;
        this.edgeCols=_edgeCols;
        this.port=port;
    }
    /**
     * Set the values for DB-connection to output in DB.
     * @param hostname The hostname.
     * @param dbname The databasename.
     * @param username The username.
     * @param password The password.
     * @param port The port of db-server.
     * @param outtable The tablename for outputtable.
     * @param outcols The columnnames in outputtable.
     */
    public DBConnect(String hostname, String dbname, String username, String password, int port, String outtable, String [] outcols){
        this.stillWorks=true;
        this.hostname=hostname;
        this.dbname=dbname;
        this.username=username;
        this.password=password;
        this.port=port;
        this.outtable=outtable;
        this.outcols=outcols;
    }
   /**
    * Get all from DB and save it into temporary outfiles. 
    * Uses the IOWrapper to handle the datatransfer.
    * @param filename_wwl The filename for temporary word-word-list.
    * @param filename_wwsl The filename for temporary word-word-signifikance-list.
    * @throws IOWrapperException
    * @throws IOIteratorException
    * @throws InterruptedException
    */
    public synchronized void getAllFromDbAndWriteIntoTempFiles() throws IOWrapperException, IOIteratorException{

        readFromDB = true;
        iow=new IOWrapper();
        iow.setupOutput(this.dbname,this.username,this.password,this.hostname,this.port);
        iow.setupInput(this.dbname,this.username,this.password,this.hostname,this.port);
        
        iter = iow.sendInputQuery("SELECT COUNT(*) FROM "+nodetable);
        try {
           maxProgerss+=Integer.parseInt(((String[])iter.next())[0]);       
        }  catch (NullPointerException e) {maxProgerss+=100;}
        iter = iow.sendInputQuery("SELECT COUNT(*) FROM "+edgetable);
        try {
            maxProgerss+=Integer.parseInt(((String[])iter.next())[0]);
        }  catch (NullPointerException e) {maxProgerss+=100;}
        //iow.closeOutput();
        

        
        //nodelist
        iter = iow.select(nodetable,nodeCols);
        iow.setupOutput(Controller.NODES_TMP_FILENAME);
        while (iter.hasNext()) {
            singleProgress+=1;
            String[] data = (String[])iter.next();
            iow.writeLine(data[0]+"\t"+data[1]);
        }
        iow.closeOutput();
	
        //edgelist
        iter=iow.select(edgetable,edgeCols);
        iow.setupOutput(Controller.EDGES_TMP_FILENAME);
        while (iter.hasNext()) {
	        singleProgress+=1;
            String[] data = (String[])iter.next();
            iow.writeLine(data[0]+"\t"+data[1]+"\t"+data[2]);
            //System.out.println("wwsl: "+data[0]+"\t"+data[1]+"\t"+data[2]);
        }
        iow.closeOutput();
        readFromDB=false;
        this.stillWorks=false;
    }	
    
    String fromFile_Name;
    IOWrapper iow_ReadFromFile;
    IOIterator iter_ReadFromFile;
   
    /**
     * Writes color-file out into DB.
     * @throws IOWrapperException
     */
    public synchronized void writeFromFileIntoDB()throws IOWrapperException, IOIteratorException{
        
        fromFile_Name=Controller.CLASSES_TMP_FILENAME;
        iow_ReadFromFile = new IOWrapper();
        
        //System.out.println("Readin' from "+fromFile_Name);
        
        iow_ReadFromFile.setupInput(fromFile_Name,0);
        
        iter_ReadFromFile = iow_ReadFromFile.getLineIterator();
        iow_ReadFromFile.setupOutput(this.dbname,this.username,this.password,this.hostname,this.port);
 
        iow_ReadFromFile.sendOutputQuery("DROP TABLE IF EXISTS "+outtable);
        
        iow_ReadFromFile.sendOutputQuery(
                "CREATE TABLE IF NOT EXISTS "
                +outtable+" ("
                +Controller.COLUMN_NAMES_FOR_DB_OUT[0]+" int(10) NOT NULL AUTO_INCREMENT, "//wort_nr
                +Controller.COLUMN_NAMES_FOR_DB_OUT[1]+" varchar(225), "//wort_alph
                +Controller.COLUMN_NAMES_FOR_DB_OUT[2]+" int(10), "//krankheit                                  
                +Controller.COLUMN_NAMES_FOR_DB_OUT[3]+" int(10), "//farbe1                                  
                +Controller.COLUMN_NAMES_FOR_DB_OUT[4]+" real(8,2), "//farbe1prozent
                +Controller.COLUMN_NAMES_FOR_DB_OUT[5]+" int(10), "//farbe2
                +Controller.COLUMN_NAMES_FOR_DB_OUT[6]+" real(8,2), "//farbe2prozent
                +"PRIMARY KEY ("+Controller.COLUMN_NAMES_FOR_DB_OUT[0]+")) "
                +"ENGINE=MyISAM"
        );
        while(iter_ReadFromFile.hasNext()){
            singleProgress+=1;
            String[] data = (String[])iter_ReadFromFile.next();
                        
            //System.out.println("--> processing line "+data);
            //escape quotes ' etc for SQL query
            if(data[1].matches(".*'.*")||data[1].matches(".*\\.*")){
                int sl = data[1].length();
                String escapedString = "";
                
                for (int i = 0; i < sl; i++) {
                    char ch = data[1].charAt(i);
                    switch (ch) {
                    case '\'':
                        escapedString +='\\';
                        escapedString +='\'';
                        break;
                    case '\\':
                        escapedString +='\\';
                        escapedString +='\\';
                        break;
                    default :
                        escapedString +=ch;
                    break;
                    }
                }
                data[1]=escapedString;
            }
            
            String insertStatement="INSERT INTO "
                    +outtable+" ("
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[0]+", "//node id
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[1]+", "//label
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[2]+", "//class id 
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[3]+", "//class id 1                                 
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[4]+", "//percent
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[5]+", "//class id 2
                    +Controller.COLUMN_NAMES_FOR_DB_OUT[6]+") "//percent 
                    +"VALUES ('"
                    +data[0]+"', '"
                    +data[1]+"', '"
                    +data[2]+"', '"
                    +data[3]+"', '"
                    +data[4]+"', '"
                    +data[5]+"', '"
                    +data[6]+"')";
            
            //System.out.println("Sending insert statement:"+insertStatement);
            iow_ReadFromFile.sendOutputQuery(insertStatement);
        }
        
              
        //iow_ReadFromFile.closeOutput();
        
        this.stillWorks=false;
        writeIntoDB=false;
        System.out.println("*\tData written into database.");
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

    	    if(tHread.isAlive()){
    	        //do something - for example start and run progressbar
    	    }
    		else{
    		    tImer.cancel();
    		    if(readFromDB)readFromDB = false;
    		    if(writeIntoDB)writeIntoDB = false;
    		    System.out.println();
    		}
    	}
    }
}
