/*
 * $Header: /usr/cvs/knorke/src/de/uni_leipzig/asv/utils/IOIterator.java,v 1.3
 * 2005/05/26 11:02:19 steresniak Exp $
 * 
 * Created on 12.05.2005, 17:10:17 by knorke for project knorke
 */
package de.uni_leipzig.asv.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;


/**
 * @author knorke
 */
public class IOIterator implements IOIteratorInterface {

   private Consumer consumer;

   // wait half of a second for active polling
   public long sleeptime = 500;

   private boolean filemode;

   private boolean rawmode;

   private LineNumberReader file;

   private String inputseparator;

   private int cols;

   private String linebuffer;

   private String[] results;

   private ResultSet resultset;


   /**
    * @param producer
    */
   public IOIterator( Producer producer ) throws IOIteratorException {

      if (producer == null) {
         System.err.println( "IOIterator: the producer cannot be null" );
         throw new IOIteratorException( "producer cannot be null" );
      }
      this.filemode = false;
      this.rawmode = false;
      this.consumer = producer.getConsumer();
      System.err.println( "IOIterator: starting ProducerThread" );
      producer.start();
   }


   /**
    * @param reader
    * @param inputseparator
    * @param cols
    * @throws IOIteratorException
    */
   public IOIterator( LineNumberReader reader, String inputseparator, int cols )
         throws IOIteratorException {

      this.filemode = true;
      this.rawmode = false;

      if (reader == null) {
         System.err.println( "IOIterator: reader cannot be null" );
         throw new IOIteratorException( "reader cannot be null" );
      }
      this.file = reader;

      if (inputseparator == null) {
         System.err.println( "IOIterator: inputseparator cannot be null" );
         throw new IOIteratorException( "inputseparator cannot be null" );
      }
      if (inputseparator.equals( "" )) {
         System.err.println( "IOIterator: inputseparator cannot be empty" );
         throw new IOIteratorException( "inputseparator cannot be empty" );
      }
      this.inputseparator = inputseparator;

      if (cols < 0) {
         System.err.println( "IOIterator: cols cannot be <0" );
         throw new IOIteratorException( "cols cannot be <0" );
      }
      this.cols = cols;

      try {
         this.linebuffer = this.file.readLine();
      } catch (IOException e) {
         System.err.println( "IOIterator: IOException cought:" );
         e.printStackTrace();
         throw new IOIteratorException( e.getMessage() );
      }

   }


   /**
    * iterates over each item of a given resultset
    * 
    * @param res
    * @throws IOIteratorException
    */
   public IOIterator( ResultSet res ) throws IOIteratorException {
      if (res == null) {
         System.err.println( "IOIterator: ResultSet cannot be null!" );
         throw new IOIteratorException( "ResultSet cannot be null" );
      }

      this.resultset = res;

      try {
         this.cols = this.resultset.getMetaData().getColumnCount();
         if (res.next()) {
            this.results = new String[this.cols];
            for (int i = 1; i <= this.cols; i++) {
               this.results[i - 1] = this.resultset.getString( i );
            }
         } else {
            this.results = null;
         }
      } catch (SQLException e) {
         System.err.println( "IOIterator: SQLException cought:\n" );
         e.printStackTrace();
         throw new IOIteratorException( e.getMessage() );
      }

      this.filemode = false;
      this.rawmode = true;

   }

   /*
    * (non-Javadoc)
    * 
    * @see de.uni_leipzig.asv.utils.IOIteratorInterface#hasNext()
    */
   public boolean hasNext() {

      // file mode
      if (this.filemode) {
         return this.linebuffer != null;
      }

      // raw (select-) mode
      if (this.rawmode) {
         return this.results != null;
      }

      // db mode
      synchronized (this.consumer) {
         if (this.consumer.size() < this.consumer.getFillThreshold()) {
            // System.err.println( "starting PRODUCER" );
            this.consumer.notify();
         }

         if (!this.consumer.isEmpty())
            return true;

         else {
            if (this.consumer.isEOF())
               return false;
            else
               return true;
         }
      }

   }


   private String[] string2cols() throws IOIteratorException {

      if (this.cols == 0)
         return this.linebuffer.split( this.inputseparator );

      String[] ret = new String[this.cols];

      int startindex = 0;
      int endindex = 0;
      for (int i = 0; i < this.cols - 1; i++) {

         endindex = this.linebuffer.indexOf( this.inputseparator, startindex );

         if (endindex > startindex) {
            ret[i] = "";
            ret[i] += this.linebuffer.substring( startindex, endindex );

         } else {
            System.err.println( "IOIterator: wrong file format in line '"
                  + this.linebuffer + "'" );
            throw new IOIteratorException( "wrong line format near line "
                  + this.file.getLineNumber() );
         }
         startindex = endindex + this.inputseparator.length();

      }

      ret[this.cols - 1] = this.linebuffer.substring( startindex );

      return ret;
   }


   /*
    * (non-Javadoc)
    * 
    * @see de.uni_leipzig.asv.utils.IOIteratorInterface#next()
    */
   public Object next() throws IOIteratorException {

      // file mode
      if (this.filemode) {

         String[] ret = null;

         // below last line
         if (this.linebuffer == null)
            throw new IOIteratorException(
                  "IOIterator: no more elements in file" );
         else {
            ret = this.string2cols();
            try {
               this.linebuffer = this.file.readLine();
            } catch (IOException e) {
               System.err.println( "IOIterator: IOException cought:" );
               e.printStackTrace();
               throw new IOIteratorException( e.getMessage() );
            }
            return ret;
         }

      }

      // raw mode
      if (this.rawmode) {
         String ret[] = this.results;
         try {

            if (this.resultset.next()) {
               this.results = new String[this.cols];
               for (int i = 1; i <= this.cols; i++) {
                  this.results[i - 1] = this.resultset.getString( i );
               }
            } else {
               this.results = null;
            }
         } catch (SQLException e) {
            System.err.println( "IOIterator: SQLException cought:\n" );
            e.printStackTrace();
            throw new IOIteratorException( e.getMessage() );
         }
         return ret;
      }

      // db mode
      synchronized (this.consumer) {
         if (!this.hasNext()) throw new NoSuchElementException();

         // active polling!
         try {
            // while (this.consumer.isEmpty() && (! this.consumer.isEOF()))
            // {
            // if(this.consumer.isEmpty() && (! this.consumer.isEOF())){

            if (this.consumer.isEmpty() && (!this.consumer.isEOF())) {
               // while (this.hasNext()) {
               System.err
                     .println( "IOIterator: waiting for database output... wait()ing "
                           + this.sleeptime + "ms" );

               this.consumer.notify();
               this.consumer.wait( 500 );
               // Thread.sleep( this.sleeptime );

            }
         } catch (InterruptedException e) {
            System.err.println( "IOIterator: Exception cought!" );
            e.printStackTrace();
            throw new IOIteratorException( e.getMessage() );
         }

         // System.err.print(" CONSUMER SIZE "+this.consumer.size()+"\r");
         return this.consumer.get();
      }
   }

}
