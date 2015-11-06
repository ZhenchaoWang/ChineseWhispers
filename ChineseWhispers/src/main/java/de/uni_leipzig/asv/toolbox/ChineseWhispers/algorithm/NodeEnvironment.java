/*
 * Created on 07.07.2005
 *
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

/**
 * @author seb
 *
 */
class NodeEnvironment implements Comparable{
    private Integer color;
    private Double waightedValueOfColor;
    
    /**
     * 
     * @param color
     * @param waightedValueOfColor
     */
    public NodeEnvironment(Integer color,Double waightedValueOfColor){
        this.color=color;
        this.waightedValueOfColor=waightedValueOfColor;
    }
    /**
     * Sort by color.
     */
    public int compareTo(Object o){
        NodeEnvironment s = (NodeEnvironment) o;
        
        if(s.getWeightedValueOfColor().doubleValue()<this.waightedValueOfColor.doubleValue()){
            return -1;
        }
        else if(s.getWeightedValueOfColor().doubleValue()>this.waightedValueOfColor.doubleValue()){
            return 1;
        }
        else{
            return 0;
        }
    }
    
    /**
     * Scales colorvalues.
     * @param factor scale
     */
    public void scale(Double factor){
        this.waightedValueOfColor=new Double(this.waightedValueOfColor.doubleValue()/factor.doubleValue());
    }
    
     /**
     * @return Returns the color.
     */
    public Integer getColor() {
        return color;
    }
    /**
     * @param color The color to set.
     */
    public void setColor(Integer color) {
        this.color = color;
    }
    /**
     * @return Returns the weightedValueOfColor.
     */
    public Double getWeightedValueOfColor() {
        return waightedValueOfColor;
    }
    /**
     * @param weightedValueOfColor The weightedValueOfColor to set.
     */
    public void setWeightedValueOfColor(Double waightedValueOfColor) {
        this.waightedValueOfColor = waightedValueOfColor;
    }
}
