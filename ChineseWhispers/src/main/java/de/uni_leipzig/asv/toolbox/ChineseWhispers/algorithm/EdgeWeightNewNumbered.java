/*
 * Created on 07.07.2005
 *
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

/**
 * @author seb
 *
 * 
 */
class EdgeWeightNewNumbered implements Comparable{
    private int newNodeNumber1;
    private int newNodeNumber2;
    private int edgeWeight;
    
    /**
     * Creates new Edge-List Object.
     * @param newNodeNumber1 The node number1.
     * @param newNodeNumber2 The node number2.
     * @param edgeWeight The edgeWeight.
     */
    public EdgeWeightNewNumbered(int newNodeNumber1,int newNodeNumber2,int edgeWeight){
        this.newNodeNumber1=newNodeNumber1;
        this.newNodeNumber2=newNodeNumber2;
        this.edgeWeight=edgeWeight;
    }
    /**
     * Sort by node nr1, edgeWeight.
     */
    public int compareTo(Object o){
        EdgeWeightNewNumbered s = (EdgeWeightNewNumbered) o;
        
        if(s.getNewNodeNumber1()>this.newNodeNumber1){
            return -1;
        }
        else if(s.getNewNodeNumber1()<this.newNodeNumber1){
            return 1;
        }
        else{
            if(s.getEdgeWeight()>this.edgeWeight){
                return 1;
            }
            else if(s.getEdgeWeight()<this.edgeWeight){
                return -1;
            }
            else{
                return 0;
            }
        }
    }
    /**
     * @return Returns the newNodeNumber1.
     */
    public int getNewNodeNumber1() {
        return newNodeNumber1;
    }
    /**
     * @return Returns the newNodeNumber2.
     */
    public int getNewNodeNumber2() {
        return newNodeNumber2;
    }
    /**
     * @return Returns the edgeWeight.
     */
    public int getEdgeWeight() {
        return edgeWeight;
    }
}
