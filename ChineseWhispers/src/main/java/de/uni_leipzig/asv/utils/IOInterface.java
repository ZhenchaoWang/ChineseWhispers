/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/IOInterface.java,v 1.8 2005/07/04 09:29:48 steresniak Exp $
 * 
 * Created on 12.05.2005, 17:01:45 by knorke
 * for project knorke
 */
package de.uni_leipzig.asv.utils;


/**
 * @author knorke
 */
public interface IOInterface {

    /**
     * file input encoding
     */
    public String inputEncoding = "utf-16";

    /**
     * file output encoding
     */
    public String outputEncoding = "utf-16";

    /**
     * file input separator
     */
    public String inputseparator = "\t";

    /**
     * file output separator
     */
    public String outputseparator = "\t";

    
    /**
     * how many rows the producerthread should fetch at once?
     * (for database use only)
     */
    public int fetchingInterval = 50000;
    
    /**
     * returns the IOIterator to iterate over file data
     * 
     * @return IO-Iterator to iterate over rows of your resultset
     * @throws IOWrapperException 
     */
    public IOIterator getLineIterator() throws IOWrapperException;

    
    
    /**
     * 
     * write a line to an open file
     * see de.uni_leipzig.asv.utils.IOInterface#setupOutput(java.lang.String)
     * 
     * @return true if everything is ok
     * @throws IOWrapperException
     */
    public boolean writeLine(String line) throws IOWrapperException;
    
    /**
     * simply close the outputfile
     * you must call this method before the program terminates
     * @throws IOWrapperException
     */
    public void closeOutput() throws IOWrapperException;

    /**
     * setup (open) the inputfile for reading
     * 
     * @param inputfile the name of the input file 
     * @param cols in file, separated by tabs - use 0 for auto-detection (dynamic col-count)
     * @throws IOWrapperException
     */
    public void setupInput( String inputfile, int cols ) throws IOWrapperException;

    
    /**
     * setup the inputfile for reading
     * uses the separator to determine the amount of columns (use a simple split(separator)
     * 
     * @param inputfile to open the file for read
     * @throws IOWrapperException
     */
    public void setupInput( String inputfile ) throws IOWrapperException;


    /**
     * setup the outputfile to write in
     * you must call the closeOutput() to flush and close the outputfile
     * 
     * @param outputfile to open the file to write into
    * @throws IOWrapperException
     */
    public void setupOutput( String outputfile ) throws IOWrapperException;


    /**
     * setup the database to write in
     * open a connection to db
     * 
     * @param database
     *            name, "de" for example
     * @param username
     *            to log in
     * @param password
     *            to log in
     * @param hostname
     *            the database runs on
     * @param port =
     *            database port
    * @throws IOWrapperException
     */
    public void setupInput( String database, String username,
            String password, String hostname, int port ) throws IOWrapperException;


    /**
     * same as setupOutput( String ), but with hostname="localhost" and port=3306
     * 
     * @param database
     * @param username
     * @param password
    * @throws IOWrapperException
     */
    public void setupInput( String database, String username, String password ) throws IOWrapperException;


    /**
     * same as for setupInput, but for writing
     * 
     * @param database
     * @param username
     * @param password
     * @param hostname
     * @param port
    * @throws IOWrapperException
     */
    public void setupOutput( String database, String username,
            String password, String hostname, int port ) throws IOWrapperException;


    /**
     * same as above, but with hostname="localhost" and port=3306
     * 
     * @param database
     * @param username
     * @param password
    * @throws IOWrapperException
     */
    public void setupOutput( String database, String username, String password ) throws IOWrapperException;


    /**
     * select the "cols" from table "tablename" and returns an iterator
     * 
     * @param tablename
     * @param cols
     * @return
     * @throws IOWrapperException
     */
    public IOIterator select( String tablename, String[] cols ) throws IOWrapperException;

    
    
    /**
     * select all cols from table "tablename" and returns an iterator
     * @param tablename
     * @return
     * @throws IOWrapperException
     */
    public IOIterator select( String tablename ) throws IOWrapperException;

    /**
     * iterates over the resultset of a given query
     * please read the IOIteratorInterface code
     * 
     * @param query
     * @return
     * @throws IOWrapperException
     */
    public IOIterator sendInputQuery( String query ) throws IOWrapperException;


    /**
     * send a query to database
     * 
     * @param query
     * @return
    * @throws IOWrapperException
     */
    public boolean sendOutputQuery( String query ) throws IOWrapperException;

}
