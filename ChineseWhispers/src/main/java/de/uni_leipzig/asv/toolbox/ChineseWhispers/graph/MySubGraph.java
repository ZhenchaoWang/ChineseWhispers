
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
 */
public class MySubGraph extends MyAbstractGraph{

        private ChineseWhispers cw;
	private BinFileStrCol nodes; 
	private BinFileMultCol edges;
	private int maxEdges;
	private Hashtable node_map;
	private Hashtable node_map_temp;
	private Hashtable edge_map;
	private Hashtable edge_map_temp;
	private int edge_count;
	private int node_count;
	private String addNode;
	
	
	
	/**
	 * Constructor for SubGraph from graph
	 * @param graph the whole graph
	 * @param L List of nodes for sub graph
	 */
	public MySubGraph(MyAbstractGraph graph){
                this.cw = ((MyGraph)graph).getGraphCW();
		this.nodes   = ((MyGraph)graph).getGraphNodes();
		this.edges   = ((MyGraph)graph).getGraphEdges();
		this.maxEdges = ((MyGraph)graph).getMaxEdges();
		edge_map = new Hashtable();
		node_map = new Hashtable();
		node_map_temp = new Hashtable();
		edge_map_temp = new Hashtable();
		addNode = new String();
		edge_count =0;
		node_count =0;
	} // end constructor
	
	
	/**
     * Constructor/3 
     * 
     * @param nodes
     * @param edges
     * @param maxEdges
     */
	public MySubGraph(ChineseWhispers _cw,int maxEdges){
                this.cw=_cw;
		this.nodes = cw.getNodes();
		this.edges = cw.getEdges();
		this.maxEdges = maxEdges;
		edge_map = new Hashtable();
		node_map = new Hashtable();
		node_map_temp = new Hashtable();
		edge_map_temp = new Hashtable();
		addNode = new String();
		edge_count =0;
		node_count =0;	
	} // end constructor/3
	
	
	
	
	/**
	 * adds a node to subgraph
	 * @param v intern ID of node
	 */
	public void addVertex(String v){
		
		node_map_temp = new Hashtable();
		edge_map_temp = new Hashtable();
		
		createGraph(v);
	}
	
	
	/**
	 * clears subgraph
	 *
	 */
	public void clear(){
		removeAllVertices();
		removeAllEdges();
		all_edges =true;
		this.edge_map = new Hashtable();
		this.node_map = new Hashtable();
		this.node_map_temp = new Hashtable();
		this.edge_map_temp = new Hashtable();
		edge_count =0;
		node_count =0;	
	}
	
	
	
	/**
	 * help function for graph creation
	 *
	 */
	private void createGraph(String v){
		
				
		String[] column = new String[3]; 
		Vertex v1;
		Vertex v2;
		
		edge_count = 0;
		node_count = 0;
		
		
		List list= new LinkedList();
		
		
		for(int k=0 ; k<= nodes.getMaxWordNr(); k++){
			
			//if already processed (k=0)
			if(v.equals(Integer.toString(k))) continue;
			
			List current_line;
			//scan neighbourhood of node-to-add
                                           
			if(k == 0){
				current_line = cw.filterByThresh(edges.getData(new Integer(v)));                               
			}
			else{
				current_line = cw.filterByThresh(edges.getData(new Integer(k)));
			}
						
			//if node's neighbourood is empty
			if (current_line.size()==0) continue;
			
					
			for (Iterator it = current_line.iterator(); it.hasNext();) {
				Integer[] actVals = (Integer[])it.next();
			
				if(k == 0) column[0] = String.valueOf(v);
				else       column[0] = String.valueOf(k);
				
				column[1] = actVals[0].toString();
				column[2] = actVals[1].toString();
				
								
				if( 	!(
						(v.equals(column[0])|| v.equals(column[1])) ||
						(node_map_temp.containsKey(column[0]) && node_map_temp.containsKey(column[1]))
						 )	
					) continue;
				
				
				if(edge_map_temp.containsKey(column[1]+"-"+column[0]))continue;
				if(edge_map_temp.containsKey(column[0]+"-"+column[1]))continue;
				
				list.add(column.clone());
				edge_map_temp.put(column[0]+"-"+column[1],"1");
				if(!node_map_temp.containsKey(column[0]))node_map_temp.put(column[0],"1");
				if(!node_map_temp.containsKey(column[1]))node_map_temp.put(column[1],"1");
			}
				
				
			}
			
			//sort by weight
			Collections.sort(list, new My_Comp_Sig());	
			
			
			//shrink list
			if(list.size() > maxEdges  &&  (maxEdges > 0)){ list = list.subList(0,maxEdges); all_edges =false;}
		
		
		
		
			for(int j = 0; j< list.size(); j++)
			{
				
				column = (String[])list.get(j);
			    
			   
				
				//start node
				if(! node_map.containsKey(column[0])){
					v1 = (Vertex) addVertex(new UndirectedSparseVertex());
					v1.addUserDatum("Label",column[0], UserData.SHARED);
					node_map.put(column[0],v1);
					node_count++;
				

				}else{
					v1= (Vertex)node_map.get(column[0]); 
					
				}
				
				//end node
				if(! node_map.containsKey(column[1])){
					v2 = (Vertex) addVertex(new UndirectedSparseVertex());
					v2.addUserDatum("Label",column[1], UserData.SHARED);
					node_map.put(column[1],v2);
					node_count++;
					
				}else{
					v2= (Vertex)node_map.get(column[1]); 

				}
				
				
				if(edge_map.containsKey(column[1]+"-"+column[0]))continue;
				if(edge_map.containsKey(column[0]+"-"+column[1]))continue;
				
				edge_map.put(column[0]+"-"+column[1],"1");
				UndirectedSparseEdge e = (UndirectedSparseEdge) addEdge(new  UndirectedSparseEdge(v1,v2));
				e.addUserDatum("Label",column[2], UserData.SHARED);
				edge_count++;
					
			}
	
				
		}
	
	
}


