package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.util.Comparator;

import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;


public class MyAbstractGraph extends UndirectedSparseGraph {
       
	protected boolean all_edges = true;
	

	
//needed for sorting list by weight
class My_Comp_Sig implements Comparator{
		
		public int compare(Object o1, Object o2){
			
			int i1 = Integer.parseInt(((String[])o1)[2]);
			int i2 = Integer.parseInt(((String[])o2)[2]);
			return (i2-i1);
		}
		
		public boolean equals(Object obj) {
			
			if(compare(this,obj) == 0 )return true;
			else return false;
			
		}

	}


}

	

