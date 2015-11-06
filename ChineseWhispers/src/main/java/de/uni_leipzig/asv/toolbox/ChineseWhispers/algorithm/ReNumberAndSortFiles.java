/*
 * Created on 07.07.2005
 */
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * @author seb
 *
 */
public class ReNumberAndSortFiles {
    
    /**
     * Creates renumbered node and edge list.
     * 
     * @param currentWordlist The filename from unrenumbered wordlist-file.
     * @param currentEdgeList The filename from unrenumbered significancelist-file.
     */
    public ReNumberAndSortFiles(String currentNodeList, String currentEdgeList){
        try{
            reNumber(currentNodeList,currentEdgeList);
        }
        catch(FileNotFoundException fnf){System.err.println("Problems while renumbering: File not found!");}
        catch(IOException ioe){System.err.println("Problems while renumbering: readline error!");}
    }

/**
     * Creates renumbered node and edge list.
     * 
     * @param currentWordlist The filename from unrenumbered wordlist-file.
     * @param currentEdgeList The filename from unrenumbered significancelist-file.
     * @throws FileNotFoundException
     * @throws IOException
     */
public void reNumber(String currentNodeList, String currentEdgeList)throws FileNotFoundException, IOException{
		
    	Hashtable nodeIDsToSort = new Hashtable();
		List list = new ArrayList();
		
		BufferedReader fileToNewNumerateNodeList = new BufferedReader(new FileReader(currentNodeList));
		String lineFromFileToNewNumerateNodeList;
		
		int linecount = 0;
	    while((lineFromFileToNewNumerateNodeList = fileToNewNumerateNodeList.readLine())!=null){
		    linecount++;
	        String[] cols =lineFromFileToNewNumerateNodeList.split("\t");
		    
	        //new IDs for nodes
		    Integer oldNodeID= new Integer(Integer.parseInt(cols[0]));
		    String label = cols[1];
		    Integer newNodeID = new Integer(linecount);
	        
		    list.add(new NodeOldNewLabel(newNodeID.intValue(),oldNodeID.intValue(),label));
		    
		    nodeIDsToSort.put(oldNodeID, new NodeOldNewLabel(newNodeID.intValue(),oldNodeID.intValue(),label));
	    }
		fileToNewNumerateNodeList.close();
		
		Collections.sort(list);
		
		//writing renumbered node list file
		
		Writer nodeListOut = new FileWriter(currentNodeList+".renumbered");
		Iterator iter1  = list.iterator();
       while(iter1.hasNext()){
             Object o = iter1.next();
             nodeListOut.write(((NodeOldNewLabel)o).getNewNodeNumber()+"\t"+((NodeOldNewLabel)o).getNodeLabel()+"\n");
       }
		nodeListOut.close();
		System.out.println("Node list renumbered: "+currentNodeList+".renumbered ...");

		List newEdgeList = new ArrayList();
		BufferedReader fileToNewNumerateEdgeList = new BufferedReader(new FileReader(currentEdgeList));
		String lineFromFileToNewNumerateEdgeList;

	    while((lineFromFileToNewNumerateEdgeList = fileToNewNumerateEdgeList.readLine())!=null){
		    String cols[] = lineFromFileToNewNumerateEdgeList.split("\t");
		    int nodeNumberNew1=((NodeOldNewLabel)nodeIDsToSort.get(new Integer(Integer.parseInt(cols[0])))).getNewNodeNumber();
		    int nodeNumberNew2=((NodeOldNewLabel)nodeIDsToSort.get(new Integer(Integer.parseInt(cols[1])))).getNewNodeNumber();
		    int weight = Integer.parseInt(cols[2]);
		    
		    newEdgeList.add(new EdgeWeightNewNumbered(nodeNumberNew1,nodeNumberNew2,weight));

	    }
	    fileToNewNumerateEdgeList.close();
	    
	    Collections.sort(newEdgeList);
	    
	    //writing renumbered edge list file
		Writer edgesOut = new FileWriter(currentEdgeList+".renumbered");
		Iterator iter2  = newEdgeList.iterator();
       while(iter2.hasNext()){
             Object o = iter2.next();
             edgesOut.write(((EdgeWeightNewNumbered)o).getNewNodeNumber1()+"\t"+((EdgeWeightNewNumbered)o).getNewNodeNumber2()+"\t"+((EdgeWeightNewNumbered)o).getEdgeWeight()+"\n");
       }
       edgesOut.close();
		System.out.println("Edge list renumbered: "+currentEdgeList+".renumbered ...");
	}
}
