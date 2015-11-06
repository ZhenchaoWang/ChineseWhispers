package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_leipzig.asv.coocc.*;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;
import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;

/**
 * @author Rocco
 *
 * Class for graph drawing
 */


public class MyGraph extends MyAbstractGraph{
	
        private ChineseWhispers cw;
	private BinFileStrCol nodes; 
	private BinFileMultCol edges;
	private int maxEdges;
	private int hub;
	private Hashtable nodes_map;
	private Hashtable edges_map;
	private int edge_count;
	private int node_count;
	public int  NODE_COUNT;
	public int  EDGE_COUNT;
	
	
	/**
     * creates an undirected graph
     * 
     * 
     * 
     * @param nodes
     * @param edges 
     * @param maxEdges maximal edges to display
     */
	public MyGraph(ChineseWhispers _cw,int maxLine,int hub){
                this.cw=_cw;
		this.nodes = cw.getNodes();
		this.edges = cw.getEdges();
		this.maxEdges = maxLine;
		this.hub = hub;
		this.edges_map = new Hashtable();
		this.nodes_map = new Hashtable();
		edge_count=0;
		node_count=0;
		NODE_COUNT =0;
		EDGE_COUNT =0;
		createGraph();
	}
	
	
	
	/**
     * Create a new Graph using edges
     */
	private void createGraph(){
		
		edge_count = 0;
		
		String[] column = new String[3]; 
		Vertex v1;
		Vertex v2;

                List list= new LinkedList();
		
		
		for(int k=1 ; k<= nodes.getMaxWordNr(); k++){
		
			List current_line = cw.filterByThresh(edges.getData(new Integer(k)));
			
					
			//if node k does not have edges
			if (current_line.size()==0) continue;
			
					
			for (Iterator it = current_line.iterator(); it.hasNext();) {
				Integer[] actVals = (Integer[])it.next();
			
				
				column[0] = String.valueOf(k);
				column[1] = actVals[0].toString();
				column[2] = actVals[1].toString();
				
								
				
				if(hub > 0){//only paint hubs
					if(cw.filterByThresh(edges.getData(new Integer(k))).size()<hub || cw.filterByThresh(edges.getData(actVals[0])).size()< hub)continue;
				}
				
				if(hub < 0){ //omit hubs
					if(cw.filterByThresh(edges.getData(new Integer(k))).size()>hub || cw.filterByThresh(edges.getData(actVals[0])).size()> hub)continue;
				}
				
				if(edges_map.containsKey(column[1]+"-"+column[0]))continue;
				if(edges_map.containsKey(column[0]+"-"+column[1]))continue;
				
				list.add(column.clone());
				edges_map.put(column[0]+"-"+column[1],"1");
			}
			
			
		}
		
		//sort by weight
		Collections.sort(list, new My_Comp_Sig());	
		
		
		//shrink list
		if(list.size() > maxEdges  &&  (maxEdges > 0)){ list = list.subList(0,maxEdges); all_edges =false; }	
		
		for(int j = 0; j< list.size(); j++)
			{
				
				column = (String[])list.get(j);
			    
			   
				
				//Start node
				if(! nodes_map.containsKey(column[0])){
					v1 = (Vertex) addVertex(new UndirectedSparseVertex());
					v1.addUserDatum("Label",column[0], UserData.SHARED);
					nodes_map.put(column[0],v1);
					node_count++;
					NODE_COUNT++;

				}else{
					v1= (Vertex)nodes_map.get(column[0]); 
					
				}
				
				//End node
				if(! nodes_map.containsKey(column[1])){
					v2 = (Vertex) addVertex(new UndirectedSparseVertex());
					v2.addUserDatum("Label",column[1], UserData.SHARED);
					nodes_map.put(column[1],v2);
					node_count++;
					NODE_COUNT++;
				}else{
					v2= (Vertex)nodes_map.get(column[1]); 

				}
				
				
					
				UndirectedSparseEdge e = (UndirectedSparseEdge) addEdge(new  UndirectedSparseEdge(v1,v2));
				e.addUserDatum("Label",column[2], UserData.SHARED);
				edge_count++;
				EDGE_COUNT++;
				
			
				
			}
	
				
		}
	



	/**
     * 
     * 
     * @return Returns the edges.
     */
	public BinFileMultCol getGraphEdges() {
		return edges;
	}
	
	/**
     * 
     * 
     * @return Returns the nodes.
     */
	public BinFileStrCol getGraphNodes() {
		return nodes;
	}
	
	
	/**
	 * @return returns max number of edges for display
	 */
	public int getMaxEdges() {
		return maxEdges;
	}

	/**
	 * @return returns cw
	 */
	public ChineseWhispers getGraphCW() {
		return cw;
	}
        
}
