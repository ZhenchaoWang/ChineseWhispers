/*
 * Created on 08.07.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.db;

import de.uni_leipzig.asv.toolbox.ChineseWhispers.gui.Controller;
import de.uni_leipzig.asv.utils.IOIteratorException;
import de.uni_leipzig.asv.utils.IOWrapperException;

/**
 * @author seb
 *
 */
public class TestDB {

    public static void main(String[] args) {
        int i;
        try{
            i =Integer.parseInt(args[0]);
        }
        catch(Exception e){i = -1;}
        
        if(i==0){
            (new TestDB()).readFromDB();
        }
        else if(i==1){
            (new TestDB()).writeIntoDB();
        }
        else{
            System.err.println("Inputerror!\nUse arguments:\n\t0\tstarts reading from db\n\t1\tstarts writing into db");
        }

    }
    /**
     * Tests reading from DB.
     *
     */
    public void readFromDB(){
        String [] wortlisteCols = {"wort_nr","wort_alph"};
        String [] kollok_sigCols = {"wort_nr1","wort_nr2","signifikanz"};
        
        DBConnect dbc_out = new DBConnect(
            	"host", 
            	"test_db",
            	"username", 
            	"passwort",
            	3306,
            	"wortliste", 
            	wortlisteCols,
            	"kollok_sig",
            	kollok_sigCols
        );
        try{
            dbc_out.getAllFromDbAndWriteIntoTempFiles();
        }
        catch(IOWrapperException iow_e){System.err.println(iow_e);}
        catch(IOIteratorException ioi_e){System.err.println(ioi_e);}
    }
    /**
     * Tests writing into DB.
     *
     */
    public void writeIntoDB(){
        DBConnect dbc_in=new DBConnect(
            	"host", 
            	"test_db",
            	"username", 
            	"passwort",
            	3306,
            	"wordcolors", 
            	Controller.COLUMN_NAMES_FOR_DB_OUT
    	);
        try{
            dbc_in.writeFromFileIntoDB();
    	}
    	catch(IOWrapperException iow_e){System.err.println(iow_e);}
    	catch(IOIteratorException ioi_e){System.err.println(ioi_e);}
    }
}
