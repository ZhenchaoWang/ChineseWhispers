/*
 * Created on 07.07.2005
 *
 *
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

/**
 * @author seb
 *
 */
class NeighbourPreferences implements Comparable{
    private Integer class_id;
    private Integer weight;
    private Double degree;
    /**
     *
     * @param class_id
     * @param weight
     * @param degree
     */
    public NeighbourPreferences(Integer class_id,Integer weight,Double degree){
        this.class_id=class_id;
        this.weight=weight;
        this.degree=degree;
    }
    /**
     * Sort by class_id.
     */
    public int compareTo(Object o){
        NeighbourPreferences s = (NeighbourPreferences) o;
        
        if(s.getClassID().intValue()>this.class_id.intValue()){
            return -1;
        } else if(s.getClassID().intValue()<this.class_id.intValue()){
            return 1;
        } else{
            return 0;
        }
    }
    
    
    /**
     * @return Returns the class_id.
     */
    public Integer getClassID() {
        return class_id;
    }
    /**
     * @param class_id The class_id to set.
     */
    public void setClassID(Integer class_id) {
        this.class_id = class_id;
    }
    /**
     * @return Returns the degree.
     */
    public Double getDegree() {
        return degree;
    }
    /**
     * @param degree The degree to set.
     */
    public void setDegree(Double degree) {
        this.degree = degree;
    }
    /**
     * @return Returns the weight.
     */
    public Integer getWeight() {
        return weight;
    }
    /**
     * @param weight The weight to set.
     */
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}