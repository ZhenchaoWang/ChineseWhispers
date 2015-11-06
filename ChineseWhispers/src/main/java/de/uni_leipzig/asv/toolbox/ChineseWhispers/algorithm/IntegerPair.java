/*
 * IntegerPair.java
 *
 * Created on 6. September 2005, 21:28
 */

package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;
import java.util.*;

 public class IntegerPair implements Comparable<IntegerPair> {
      private Integer i1;
      private Integer i2;
      
      
      public IntegerPair(Integer in1, Integer in2) {
          this.i1=in1;
          this.i2=in2;
      }
      
      public Integer i1() {return i1;}
      public Integer i2() {return i2;}
          
      public boolean equals(Object o) {
         if(!(o instanceof IntegerPair))
             return false;
         IntegerPair n = (IntegerPair)o;
         return (n.i2==this.i2);
          
      }  
      
      public int compareTo(IntegerPair tcp) {
          int r=0;
          if (tcp.i2<this.i2) {
              r=1;
          }
          if (tcp.i2>this.i2) {
              r=-1;
          }         
          return r;
      }
     
      
  } // end class IntegerPair