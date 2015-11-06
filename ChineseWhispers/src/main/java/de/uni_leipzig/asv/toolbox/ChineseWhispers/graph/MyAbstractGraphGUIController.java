package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

/**
 * @author Rocco
 * 
 */
public abstract interface MyAbstractGraphGUIController {
	
	/**
	 * is called by MyGraphGUI with flag = false (open)<br>
	 * or flag = true (close)<br>
	 * for graph window.
	 * @param flag
	 */
	abstract void setStatus(boolean flag); 
		

}
