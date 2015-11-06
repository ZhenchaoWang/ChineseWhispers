/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/Consumer.java,v 1.5 2005/07/05 14:58:33 steresniak Exp $
 * 
 * Created on Jun 9, 2005 by knorke
 * 
 * package de.uni_leipzig.asv.utils for knorke project
 * 
 */
package de.uni_leipzig.asv.utils;

import java.util.Vector;

/**
 * @author knorke
 * @date Jun 9, 2005 2:14:41 PM
 */
public class Consumer {

    private Vector buffer;

    private boolean EOF;

    private int lastIndex;

    private int fillThreshold;


    public Consumer() {
        this.buffer = new Vector();
        this.EOF = false;
        this.lastIndex = 0;
        this.fillThreshold = 10000;
    }


    /**
     * @return Returns the EOF.
     */
    public synchronized boolean isEOF() {
        return this.EOF;
    }


    /**
     * @param eof
     *            The EOF to set.
     */
    public synchronized void setEOF( boolean eof ) {
        this.EOF = eof;
    }


    /**
     * @return Returns the lastIndex.
     */
    public synchronized int getLastIndex() {
        return this.lastIndex;
    }


    /**
     * @param lastIndex
     *            The lastIndex to set.
     */
    public synchronized void setLastIndex( int lastIndex ) {
        this.lastIndex = lastIndex;
    }


    /**
     * 
     * @param o
     *            the object to add to the queue
     */
    public void add( Object o ) {
        synchronized ( this.buffer ) {
            this.buffer.add( o );
        }
    }


    /**
     * returns the next element of the FIFO
     * 
     * @return an element
     */
    public Object get() {
        synchronized ( this.buffer ) {
            if ( this.buffer.isEmpty() )
                return null;
            else {
                // remove the first element of buffer and return it
                Object ret = this.buffer.get( 0 );
                this.buffer.remove( 0 );
                return ret;
            }
        }
    }


    /**
     * 
     * @return true if the pipe is empty
     */
    public boolean isEmpty() {
        synchronized ( this.buffer ) {
            return this.buffer.isEmpty();
        }
    }


    public synchronized int size() {
        return this.buffer.size();
    }


    /**
     * @return Returns the fillThreshold.
     */
    public synchronized int getFillThreshold() {
        return this.fillThreshold;
    }


    /**
     * @param fillThreshold
     *            The fillThreshold to set.
     */
    public synchronized void setFillThreshold( int fillThreshold ) {
       if(fillThreshold<1) return;
       	this.fillThreshold = fillThreshold;
    }
}
