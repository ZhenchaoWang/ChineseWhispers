/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/Producer.java,v 1.6 2005/07/05 14:58:33 steresniak Exp $
 * 
 * Created on Jun 9, 2005 by knorke
 * 
 * package de.uni_leipzig.asv.utils for knorke project
 * 
 */
package de.uni_leipzig.asv.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author knorke
 * @date Jun 9, 2005 2:32:15 PM
 */
public class Producer extends Thread {

    private int interval = 50000;

    private Consumer consumer;

    private DBConnection connection;

    private String tablename;

    private String[] cols;

    private String primkey;

    private int lastKey = -1;

    private int sleeptime = 50;

    private int consumerthreshold = 10000;


    /**
     * @param connection
     * @param tablename
     * @param cols
     *            can be null or empty (to fetch ALL columns)
     * @param primkey
     */
    public Producer( DBConnection connection, String tablename,
            String[] cols, String primkey ) {

        this.consumer = new Consumer();
        this.consumer.setFillThreshold((int)(0.2*this.interval));
        this.connection = connection;
        this.tablename = tablename;

        if ( cols == null )
            this.cols = new String[0];
        else
            this.cols = cols;

        this.primkey = primkey;
    }


    public Consumer getConsumer() {
        return this.consumer;
    }


    public int getInterval() {
        return this.interval;
    }


    public void setInterval( int i ) {
        if ( i > 0 )
            this.interval = i;
        else
            System.err
                    .println( "ProducerThread: cannot set the fetching interval below zero" );
    }


    private ResultSet fetch( int startkey ) throws SQLException {

        StringBuffer statement = new StringBuffer();

        if ( this.cols.length == 0 ) {
            statement.append( "SELECT * FROM " + this.tablename + " WHERE "
                    + this.primkey + ">" + startkey + " LIMIT "
                    + this.interval );
        } else {
            statement.append( "SELECT " + this.primkey );

            for ( int i = 0; i < this.cols.length; i++ ) {
                statement.append( "," + this.cols[i] );
            }

            statement.append( " FROM " + this.tablename + " WHERE "
                    + this.primkey + ">" + startkey + " LIMIT "
                    + this.interval );
        }

        System.err.println( "ProducerThread: fetching with statement "
                + statement.toString() );

        return this.connection.executeQuery( statement.toString() );
      
      
    }


    public void run() {

        ResultSet resset = null;
        int linenrs = 0, condelta = 0;
        ResultSetMetaData metadata = null;
        String data[] = null;
        // main loop
        while ( true ) {

            synchronized ( this.consumer ) {

                // fuellstand ist unter threshold gefallen
                // ein nachfuellen mit daten ist noetig
                if ( this.consumer.size() <= this.consumer.getFillThreshold() ) {

                    System.err
                            .println( "ProducerThread: good morning  (bufsize: "
                                    + this.consumer.size() + ")" );
                    try {
                        // if this is the first fetch()-call, the first key is
                        // -1
                        resset = this.fetch( this.lastKey );
                        metadata = resset.getMetaData();

                        while ( resset.next() ) {

                            if ( this.cols.length > 0 ) {
                                if ( metadata.getColumnCount() - 1 != this.cols.length ) {
                                    System.err
                                            .println( "ProducerThread: got "
                                                    + metadata
                                                            .getColumnCount()
                                                    + " cols, but i've requested "
                                                    + this.cols.length
                                                    + " cols!" );
                                    throw new RuntimeException(
                                            "critical error! please submit a full bug report" );
                                }

                                data = new String[this.cols.length];

                                // fetch the requested cols by names
                                for ( int i = 0; i < this.cols.length; i++ ) {
                                    data[i] = resset.getString( this.cols[i] );
                                }

                            } // end if(this.cols != null)
                            else {

                                // build a stringarray
                                data = new String[metadata.getColumnCount()];

                                for ( int i = 0; i < data.length; i++ ) {
                                    data[i] = resset.getString( i + 1 );
                                }

                            }

                            // get the primary key, to know the next interval
                            // (from lastkey to lastkey+intervall)
                            this.lastKey = resset.getInt( this.primkey );

                            // count the linenr fetched
                            linenrs++;

                            // some debug output
                            // System.err.println( "ProducerThread: (" +
                            // this.lastKey + ") adding " + data + " " +
                            // data.length );

                            // adding the coldatas to the consumer
                            this.consumer.add( data );
                        }

                    } catch ( SQLException e ) {
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                    // System.err.println("<< "+linenrs+" != "+this.interval);
                    if ( linenrs < this.interval ) {
                        System.err
                                .println( "ProducerThread: all data fetched" );
                        this.consumer.setEOF( true );
                        break;
                    } else {
                        linenrs = 0;
                    }

                } else {
                    try {

                        this.consumer.notify();
                        //this.consumer.wait(5000);
                        System.err
                                .println( "ProducerThread: good n8  (bufsize: "
                                        + this.consumer.size() + ")" );
                        this.consumer.wait();

                    } catch ( InterruptedException e ) {
                        System.err.println( "IOIterator: Exception cought!" );
                        e.printStackTrace();
                        throw new RuntimeException( e.getMessage() );
                    }
                }

            } // end else
        } // end main loop

    }
}
