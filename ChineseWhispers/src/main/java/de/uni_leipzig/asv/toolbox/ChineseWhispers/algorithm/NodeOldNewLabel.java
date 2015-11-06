/*
 * Created on 07.07.2005
 *
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

/**
 * @author seb
 *
 */
class NodeOldNewLabel implements Comparable{
    private int newNodeNumber;
    private int oldNodeNumber;
    private String nodeLabel;
    
    /**
     * Creates new Object.
     * @param newNodeNumber The new node number.
     * @param oldNodeNumber The old node number.
     * @param nodeLabel The word.
     */
    public NodeOldNewLabel(int newNodeNumber,int oldNodeNumber,String nodeLabel){
        this.newNodeNumber=newNodeNumber;
        this.oldNodeNumber=oldNodeNumber;
        this.nodeLabel=nodeLabel;
    }
    
    public int compareTo(Object o){
        if(((NodeOldNewLabel) o).getNewNodeNumber() > newNodeNumber)return -1;
        else if(((NodeOldNewLabel) o).getNewNodeNumber() < newNodeNumber)return 1;
        else return 0;
    }
    /**
     * Get the node label
     * @return The node label.
     */
    public String getNodeLabel(){
      return this.nodeLabel;  
    }
    /**
     * Get the old node number.
     * @return The old node number.
     */
    public int getOldNodeNumber(){
        return this.oldNodeNumber;
    }
    /**
     * Get the new node number.
     * @return The old node number.
     */
    public int getNewNodeNumber(){
        return this.newNodeNumber;
    }

}
