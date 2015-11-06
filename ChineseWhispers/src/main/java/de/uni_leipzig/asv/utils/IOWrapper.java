/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/IOWrapper.java,v 1.4
 * 2005/05/26 11:02:19 steresniak Exp $
 * 
 * Created on May 16, 2005 by knorke
 * 
 * package de.uni_leipzig.asv.utils for knorke project
 * 
 */
package de.uni_leipzig.asv.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author knorke
 * @date May 16, 2005 2:47:11 PM
 */
public class IOWrapper implements IOInterface {

    private DBConnection con_in;

    private DBConnection con_out;

    private LineNumberReader lnreader;

    private BufferedWriter bwriter;

    private boolean db_input_done = false;

    private boolean db_output_done = false;

    private boolean file_input_done = false;

    private boolean file_output_done = false;

    private int file_cols;


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#getLineIterator()
     */
    public IOIterator getLineIterator() throws IOWrapperException {

        if ( !this.file_input_done ) {
            System.err
                    .println( "IOWrapper: you must setup the input (inputfile) first!" );
            throw new IOWrapperException(
                    "please setup the (file)input first with setupImput(filename)" );
        }

        try {
            return new IOIterator( this.lnreader, IOWrapper.inputseparator,
                    this.file_cols );
        } catch ( IOIteratorException e ) {
            System.err.println( "IOWrapper: IOIteratorException cought:" );
            e.printStackTrace();
            throw new IOWrapperException( e.getMessage() );
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#writeLine(java.lang.String)
     */
    public boolean writeLine( String line ) throws IOWrapperException {
        if ( !this.file_output_done ) {
            System.err
                    .println( "please setup an outputfile with setupOutput() first" );
            throw new IOWrapperException( "no outputfile initialized" );
        }

        if ( line == null ) {
            System.err.println( "cannot write a null-string" );
            throw new IOWrapperException( "cannot write a null-string" );
        }

        // empty lines now permitted
        //if ( line.equals( "" ) ) return false;

        try {
            this.bwriter.write( line );
            this.bwriter.newLine();
            //this.bwriter.flush();
        } catch ( Exception e ) {
            System.err.println( "exception cought:" );
            e.printStackTrace();
            throw new IOWrapperException( e.getMessage() );
        }

        return true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#closeOutput()
     */
    public void closeOutput() throws IOWrapperException {
        if ( this.file_output_done ) {
            try {
                this.bwriter.close();
            } catch ( IOException e ) {
                System.err.println( "exception cought:" );
                e.printStackTrace();
                throw new IOWrapperException( e.getMessage() );
            }
        } else {
            System.err.println( "no outputfile open" );
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupInput(java.lang.String,
     *      int)
     */
    public void setupInput( String inputfile, int cols )
            throws IOWrapperException {

        // some checks
        if ( inputfile == null ) {
            System.err.println( "inputfile cannot be null" );
            throw new IOWrapperException( "inputfile cannot be null" );
        }
        if ( inputfile.equals( "" ) ) {
            System.err.println( "inputfile cannot be an empty string" );
            throw new IOWrapperException(
                    "inputfile cannot be an empty string" );
        }

        try {
            this.lnreader = new LineNumberReader( new BufferedReader(
                    new FileReader( inputfile ) ) );
        } catch ( IOException e ) {
            System.err.println( "error while opening inputfile " + inputfile
                    + ": " + e.getMessage() );
            throw new IOWrapperException( e.getMessage() );
        }
        this.file_cols = cols;
        this.file_input_done = true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupInput(java.lang.String)
     */
    public void setupInput( String inputfile ) throws IOWrapperException {
        this.setupInput( inputfile, 0 );
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupOutput(java.lang.String)
     */
    public void setupOutput( String outputfile ) throws IOWrapperException {

        if ( outputfile == null ) {
            System.err.println( "outputfile cannot be null" );
            throw new IOWrapperException( "outputfile cannot be null" );
        }
        if ( outputfile.equals( "" ) ) {
            System.err.println( "outputfile cannot be an empty string" );
            throw new IOWrapperException(
                    "outputfile cannot be an empty string" );
        }
        try {
            this.bwriter = new BufferedWriter( new FileWriter( outputfile ) );
        } catch ( Exception e ) {
            System.err.println( "exception cought:" );
            e.printStackTrace();
            throw new IOWrapperException( e.getMessage() );
        }
        this.file_output_done = true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupInput(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, int)
     */
    public void setupInput( String database, String username,
            String password, String hostname, int port )
            throws IOWrapperException {

        this.con_in = new DBConnection( "jdbc:mysql://" + hostname + ":"
                + port + "/" + database, username, password );
        this.db_input_done = true;

    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupInput(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void setupInput( String database, String username, String password )
            throws IOWrapperException {
        this.setupInput( database, username, password, "localhost", 3306 );

    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupOutput(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, int)
     */
    public void setupOutput( String database, String username,
            String password, String hostname, int port )
            throws IOWrapperException {

        this.con_out = new DBConnection( "jdbc:mysql://" + hostname + ":"
                + port + "/" + database, username, password );
        System.out.println( this.con_out );
        this.db_output_done = true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#setupOutput(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void setupOutput( String database, String username, String password )
            throws IOWrapperException {
        this.setupOutput( database, username, password, "localhost", 3306 );
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#select_intelligently(java.lang.String,
     *      java.lang.String[])
     */
    public IOIterator select( String tablename, String[] cols )
            throws IOWrapperException {

        if ( !this.db_input_done ) {
            System.err
                    .println( "IOWrapper: please setup the database input connection first!" );
            throw new IOWrapperException(
                    "please setup the database input connection first!" );
        }

        // primaerschluessel ermitteln
        String primkey = getPrimaryKey( tablename );

        System.err.println( "building the iterator" );

        Producer producer = new Producer( this.con_in, tablename, cols,
                primkey );
        
        producer.setInterval(IOWrapper.fetchingInterval);

        try {
            return new IOIterator( producer );
        } catch ( IOIteratorException e ) {
            System.err.println( "IOWrapper: IOIteratorException cought:" );
            e.printStackTrace();
            throw new IOWrapperException( e.getMessage() );
        }
    }


    /**
     * 
     * @param tablename
     *            to search the key for
     * @return the name of the primary key
     * @throws IOWrapperException
     */
    private String getPrimaryKey( String tablename )
            throws IOWrapperException {
        
       System.err.println( "searching for primary key..." );
        
        String primkey = null;
        
        try {
           ResultSet resset = this.con_in.executeQuery( "DESC " + tablename );
        
        	System.err.println( "IOWrapper: fetchsize was "
                    + resset.getFetchSize() );

            // finde raus, wieviele spalten gefunden wurden
            ResultSetMetaData metadata = resset.getMetaData();
            int colCount = metadata.getColumnCount();

            // wenn spaltenanzahl kleiner 4 ist was faul
            if ( colCount < 4 ) {
                System.err
                        .println( "IOWrapper: found less than four columns in DESC "
                                + tablename + "!" );
                throw new IOWrapperException( "cannot find primary key" );
            }

            // primary key raussuchen.
            while ( resset.next() ) {

                if ( resset.getString( 4 ).equals( "PRI" ) ) {
                    System.err.println( "IOWrapper: primary key found: "
                            + resset.getString( 1 ) );

                    // primaerschluessel vom korrekten typ?
                    String tempkey = resset.getString( 2 ).toLowerCase();
                    if ( tempkey.startsWith( "int(" )
                            || tempkey.startsWith( "mediumint(" )
                            || tempkey.startsWith( "smallint(" )
                            || tempkey.startsWith( "tinyint(" )
                            || tempkey.startsWith( "bigint(" ) ) {

                        System.err
                                .println( "IOWrapper: primary key is an integer. very good." );
                        primkey = resset.getString( 1 );
                        break;
                    } else {
                        System.err
                                .println( "IOWrapper: primary key must be an integer!" );
                        throw new IOWrapperException(
                                "primary key for table " + tablename
                                        + " is not an integer!" );
                    }
                }
            }
        } catch ( SQLException e ) {
            System.err.println( "IOWrapper: SQLException cought:\n" );
            e.printStackTrace();
            throw new IOWrapperException( e.getMessage() );
        }
        if ( primkey == null ) {
            System.err
                    .println( "IOWrapper: no primary key(int) found for table "
                            + tablename );
            throw new IOWrapperException( "no primary key found for table "
                    + tablename );
        }

        return primkey;
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#select(java.lang.String)
     */
    public IOIterator select( String tablename ) throws IOWrapperException {
        return this.select( tablename, null );
    }


    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#sendInputQuery(java.lang.String)
     */
    public IOIterator sendInputQuery( String query )
            throws IOWrapperException {
       if ( !this.db_input_done ) {
          System.err
                  .println( "IOWrapper: please setup the database input connection first!" );
          throw new IOWrapperException(
                  "please setup the database input connection first!" );
       }
       
       ResultSet temp = null;
       
       // getting data
       try{
          temp = this.con_out.executeQuery(query);
       } catch ( SQLException e ) {
          System.err.println( "IOWrapper: query was > " + query + " <");
          System.err.println( "IOWrapper: SQLException cought:\n" );
          e.printStackTrace();
          throw new IOWrapperException( e.getMessage() );
       }

       // building an iterator
       try{
          return new IOIterator(temp);
       }catch(IOIteratorException f){
          System.err.println( "IOWrapper: query was > " + query + " <");
          System.err.println( "IOWrapper: SQLException cought while building IOIterator:\n" );
          f.printStackTrace();
          throw new IOWrapperException(f.getMessage());
       }
    }





    /*
     * (non-Javadoc)
     * 
     * @see de.uni_leipzig.asv.utils.IOInterface#sendOutputQuery(java.lang.String)
     */
    public boolean sendOutputQuery( String query ) throws IOWrapperException {
       if ( !this.db_output_done ) {
          System.err
                  .println( "IOWrapper: please setup the database output connection first!" );
          throw new IOWrapperException(
                  "please setup the database output connection first!" );
       }
       try{
          this.con_out.executeUpdate(query);
       } catch ( Exception e ) {
          System.err.println( "IOWrapper: query was > " + query + " <");
          System.err.println( "IOWrapper: Exception cought:\n" );
          e.printStackTrace();
          throw new IOWrapperException( e.getMessage() );
       }
       
       return true;
    }


    public static void main( String[] args ) throws IOWrapperException,
            IOIteratorException, InterruptedException {

        System.err
                .println( "IOWrapper $Id: IOWrapper.java,v 1.15 2005/07/05 14:59:20 steresniak Exp $" );
        System.err
                .println( "please report bugs or write requests to teresniak@informatik.uni-leipzig.de" );
        System.err.println();

        //Random rand = new Random();
        IOWrapper iow = new IOWrapper();

        if ( args.length < 2 ) {
            System.err.println( "need two filenames (input and output)" );
            System.exit( 1 );
        }

        iow.setupInput( args[0] );
        iow.setupOutput( args[1] );
        IOIterator iter = iow.getLineIterator();

        //String[] cols = {"wort_nr", "wort_bin"};
        /*
         * String[] cols = {}; iow.setupInput( "de", "root", "keins" );
         * IOIterator iter = iow.select( "wortliste", cols );
         */
        StringBuffer buf = new StringBuffer();
        while ( iter.hasNext() ) {
            String[] data = (String[])iter.next();

            //System.out.print( "CONSUMER: " );
            for ( int i = 0; i < data.length; i++ ) {
                System.out.print( data[i] + "; " );
                buf.append( data[i] + "\t" );
            }
            //Thread.sleep( 1 );
            System.out.println();
            iow.writeLine( buf.toString() );
        }
        iow.closeOutput();
    }
}
