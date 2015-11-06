/*
 * ChineseWhispers.java
 *
 * 
 * This is the clustering process of Chinese Whispers
 *
 * it also handles file IO and preparation
 *
 * 
 *This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
*/
package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

// imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import javax.swing.JFileChooser;


// cooc access classes for graph access
import de.uni_leipzig.asv.coocc.*;

// controller GUI
import de.uni_leipzig.asv.toolbox.ChineseWhispers.gui.Controller;


public class ChineseWhispers implements Cloneable,Runnable {

 // for debugging: switch on/off console verbose output 
 private final boolean d=!true;
    
 // for random   
 private long seed; 
 private Random r;
 
 // stores classID per node_id = colour per node
 private int[] node_class;
 private int[] new_node_class;
 
 // false if nodes do not have neighbours
 private boolean[] node_ok; 
 
 // Nodes that have neighbours
 private ArrayList active_nodes;
 
 // stores largest cluister_id so far
 private int largest_class;
 
  // highest node_id
 public int max_node_nr;  

 // parameter: keep graph in RAM 
 public boolean graphInMemory=false;   
 
 
 // parameter: Mutation rate
 private double mut_rate;
   
 // parameter: keep color rate
 private double keep_color_rate;
  
 // parameter: mode (strategy)
 private int mode;
 
 // parameter: mutation option
 private int mut_opt;
 
 // parameter: mutation rate
 private double mut_param;
 
 // parameter: time rate
 private double update_param;
 
 // parameter: bool for dist mode
 private boolean nolog;
 
 // parameter: vote threshold
 private double votethresh;
 
  // parameter: number of iterations
 private int iterations ;
 
 // degrees per node
 private Hashtable degree;
 
 // classes per node
 private Hashtable classTable;
 
 // stores clusterings per iteration
 private Hashtable Iter_Classes[];
 
 // thread controll
 public boolean isActive = false;
 
 // switch: GUI
 private boolean writesOnlyFiles =false;
 
  // switch: DB or file-out vs. no result writing
 private boolean isFileOrDBOut;
 
 // for GUI progress bar
 public int writeFileProgress = 1;
 public int maxWriteFileProgress = 100;
 
 // for GU: nr of nodes
 public int countNodesWithClasses=0;
 
 // process: current iteration 
 private int currentIteration; 

 // File names for access files
 private String nodeListFile, edgeListFile;
 
 // weight threshold
 int min_weight;
 
 // process: max cluster_id
 private int maxClusterID=1;
 
 //file access for node_id-> Label
 private BinFileStrCol nodeLabels=null;
 
 // file access for node1 node2 weight - edgelist
 private BinFileMultCol edges =null; 
 
 // switch: files are renumbered
 public boolean isNumbered;
 
 // options/parameters 
 private String algOpt;
 private String algValue;
 private String algMutOpt;
 private String algUpdateOpt;
 private double algMutValue;
 private double algKeepValue;
 
 // List of objects for renumbering 
 List nodeNewOldLabel;
 
 
 /**
  * Constructor
  * @return
  */
 public ChineseWhispers(){
    seed=new Date().getTime();
    r=new Random(seed);
 } // end constructor
 
 /**
  * Set the param to sign that only write-Methode is used.
  * @param writesOnlyFile
  */
 public void setWritesOnlyFiles(boolean writesOnlyFiles){
     this.writesOnlyFiles=writesOnlyFiles;
 }
 /**
  * Get the param to see that only write-Methode is used.
  * @return 
  */
 public boolean getWritesOnlyFiles(){
     return this.writesOnlyFiles;
 }
 
 /**
  * Prepares the binary files for graph access
  * @return 
  */
 private void makeBinFiles(){
     
     if (d) System.out.println("[cw] makeBinFiles called, files "+nodeListFile+" "+edgeListFile);
     
     // if necessary, renumber the files
      
      if(!isNumbered){
          try{
              reNumber(nodeListFile,edgeListFile);
              isNumbered=true;
              nodeListFile=nodeListFile+".renumbered";
              edgeListFile=edgeListFile+".renumbered";              
          }
          catch(FileNotFoundException fnf){System.err.println("Problems while renumbering: File not found!");}
          catch(IOException ioe){System.err.println("Problems while renumbering: readline error!");}
          
      } // fi !isNumbered
       
     // create binary files
     this.nodeLabels = BinFileConstructor.getBinFileStrCol(nodeListFile,graphInMemory);
     this.edges = BinFileConstructor.getBinFileMultCol(edgeListFile,3,graphInMemory);

 }
 
 public Object clone() throws CloneNotSupportedException {
 	return super.clone(); // flat copy - clone() is sufficient
 }

 /**
     * Creates renumbered node and edge list.
     * 
     * @param sourceNodeList The filename of (unrenumbered) node list file.
     * @param sourceEdgeList The filename of (unrenumbered) edge list file.
     * @throws FileNotFoundException
     * @throws IOException
     */
 public void reNumber(String sourceNodeList, String sourceEdgeList)throws FileNotFoundException, IOException{

     Hashtable nodeNrsForSort = new Hashtable();
     nodeNewOldLabel = new ArrayList();   
    
     System.out.println("*\tRenumbering nodes and edges:");

     if (new File(sourceNodeList+".renumbered").exists()) {
        System.out.println("\t\tfound renumbered node list");
     } else { // renumber node list 
     
        BufferedReader nodeListToRenumberFile = new BufferedReader(new FileReader(sourceNodeList));
        String lineFromSourceFile;

        int linecount = 0;
        while((lineFromSourceFile = nodeListToRenumberFile.readLine())!=null){
            linecount++;
            String[] cols =lineFromSourceFile.split("\t");

            //nodes are renumbered
            Integer oldNodeNumber= new Integer(Integer.parseInt(cols[0]));
            String nodeLabel = cols[1];
            Integer newNodeNumber = new Integer(linecount);

            nodeNewOldLabel.add(new NodeOldNewLabel(newNodeNumber.intValue(),oldNodeNumber.intValue(),nodeLabel));

            nodeNrsForSort.put(oldNodeNumber, new NodeOldNewLabel(newNodeNumber.intValue(),oldNodeNumber.intValue(),nodeLabel));
        }
        nodeListToRenumberFile.close();
        currentIteration=(getIteration()*7)/100;
        Collections.sort(nodeNewOldLabel);

        //writing renumbered node list
        Writer renumberedNodeListWriter = new FileWriter(sourceNodeList+".renumbered");
        Iterator iter  = nodeNewOldLabel.iterator();
        while(iter.hasNext()){
              Object o = iter.next();
              renumberedNodeListWriter.write(((NodeOldNewLabel)o).getNewNodeNumber()+"\t"+((NodeOldNewLabel)o).getNodeLabel()+"\n");
        }
        renumberedNodeListWriter.close();
        System.out.println("\t--\tNode list renumbered:\n\t\t-\t"+sourceNodeList+".renumbered");
        currentIteration=(getIteration()*9)/100;

     } // esle renumbered node list exists
     
     
     if (new File(sourceEdgeList+".renumbered").exists()) {
        System.out.println("\t\tfound renumbered edge list");
     } else { // renumber edge list         
        List newEdgeList = new ArrayList();
        BufferedReader edgeListToRenumberFile = new BufferedReader(new FileReader(sourceEdgeList));
        String lineFromEdgeListToRenumberFile;

        int countErrors=0;
        while((lineFromEdgeListToRenumberFile = edgeListToRenumberFile.readLine())!=null){
            try{
                String cols[] = lineFromEdgeListToRenumberFile.split("\t");
                    int nodeNumberNew1=((NodeOldNewLabel)nodeNrsForSort.get(new Integer(Integer.parseInt(cols[0])))).getNewNodeNumber();
                    int nodeNumberNew2=((NodeOldNewLabel)nodeNrsForSort.get(new Integer(Integer.parseInt(cols[1])))).getNewNodeNumber();
                    int edgeWeight = Integer.parseInt(cols[2]);

                    newEdgeList.add(new EdgeWeightNewNumbered(nodeNumberNew1,nodeNumberNew2,edgeWeight));
            }
            catch(Exception e){
                //mapping does not succeed if node numbers from edge lists are not in node list
                if (d) System.err.println("Word number not found in line: "+lineFromEdgeListToRenumberFile);
                countErrors++;
            }
        } // elihw
        if(countErrors>0){
            System.err.println("Found "+countErrors+" node numbers in edge list that could not be mapped: ignored.");
        }
        edgeListToRenumberFile.close();

        Collections.sort(newEdgeList);
            currentIteration=(getIteration()*12)/100;

        //writing renumbered edge list file
        Writer renumberedEdgeListWriter = new FileWriter(sourceEdgeList+".renumbered");
        Iterator iter  = newEdgeList.iterator();
        while(iter.hasNext()){
              Object o = iter.next();
              renumberedEdgeListWriter.write(((EdgeWeightNewNumbered)o).getNewNodeNumber1()+"\t"+((EdgeWeightNewNumbered)o).getNewNodeNumber2()+"\t"+((EdgeWeightNewNumbered)o).getEdgeWeight()+"\n");
        }
        renumberedEdgeListWriter.close();
        System.out.println("\t--\tEdge list renumbered:\n\t\t-\t"+sourceEdgeList+".renumbered\n*\tdone.");
     } // esle renumbered edge list exists
 
 } // end reNumber/2
 
 
 /**
  * Init initialises the clustering: 
  * - all nodes get different classes
  * - active nodes are determined
  * - degree reweighting is computed
  * @return
  */
 private void initGraph() {
    max_node_nr=nodeLabels.getMaxWordNr(); 

    node_class= new int[max_node_nr+1];
    new_node_class=new int[max_node_nr+1];
    node_ok=new boolean[max_node_nr+1];
   
    for (int i=1;i<=max_node_nr;i++) {
      node_class[i]=i;
      new_node_class[i]=i;
      node_ok[i]=true;  
    }

    node_class[1]=1;
    new_node_class[1]=1;
    largest_class= max_node_nr+1;

    // initialize degrees for mode=2;
    if (mode==2) {
     for(int node_nr=1;node_nr<=max_node_nr;node_nr++) {
       List list=filterByThresh(edges.getData(new Integer(node_nr)));
       if (list.size()==0) {node_ok[node_nr]=false;}
       else if(nolog) {degree.put(new Integer(node_nr), new Double(list.size())); }
       else {degree.put(new Integer(node_nr), new Double(Math.log(list.size()))); }
     } //rof
    } // fi mode=2  
    
    // initialize active_nodes
    active_nodes=new ArrayList();
    
    for(int node_nr=1;node_nr<=max_node_nr;node_nr++) {
    List list=filterByThresh(edges.getData(new Integer(node_nr)));
       if (list.size()>0) {
         active_nodes.add(new Integer(node_nr)); 
         node_ok[node_nr]=true;
       } // fi list size
       else {node_ok[node_nr]=false;} 
    } // rof node_nr
  } // end initGraph

 
 /** Update-step for percolation of colors
  *
  * @return
  *
  *
  */
  private void perform_iteration() {
    Hashtable current_neighbourhood;
    Hashtable adj_nodes;
    List list;
    List list2;
    Integer node_nr_neigh, weight_neigh;
    Integer current_class;
    Double dummy, curr_val;
    int maximal_class;
    double maxvalue;
    double dice;
    double addval;
    double valsum;
    int node_nr;
  
    // randomize order of nodes           
    Collections.shuffle(active_nodes,r);
    
    if (d) System.out.println(" -- new iteration -- ");
    for(Iterator nodes= active_nodes.listIterator();nodes.hasNext();) {
      node_nr=((Integer)nodes.next()).intValue();
    
      
      // determine whether to touch at all   
      dice=r.nextDouble();
      if (dice<=keep_color_rate) { 
          new_node_class[node_nr]=node_class[node_nr];  //Keep colour
      } else {
      
         
        current_neighbourhood= new Hashtable();
      
        list=filterByThresh(edges.getData(new Integer(node_nr)));
        if (list.size()==0) {node_ok[node_nr]=false;}
        if (node_ok[node_nr]) {

         valsum=0; 
       
         // Formal proving ... randomize order of list
         adj_nodes=new Hashtable();
         Collections.shuffle(list,r);
         
         for (Iterator it = list.iterator(); it.hasNext();) {       
            adj_nodes.put(it.next(),"dummy");
         } // rof 
         
         for (Enumeration e = adj_nodes.keys(); e.hasMoreElements();) {
             Integer[] actVals = (Integer[])e.nextElement();
             node_nr_neigh=actVals[0];
             weight_neigh=actVals[1];
             addval=(double)weight_neigh.doubleValue();

             if (mode == 2) {addval=addval/((Double)degree.get(node_nr_neigh)).doubleValue();}

             if (d) System.out.println("Entering "+node_nr+" - "+node_nr_neigh+" with weight "+addval);


             valsum+=addval;
             current_class= new Integer(node_class[node_nr_neigh.intValue()]);
             if (current_neighbourhood.containsKey(current_class)) { //bekannte node_class
                dummy=((Double)current_neighbourhood.get(current_class));
                dummy=new Double((double)dummy.doubleValue()+addval);
                current_neighbourhood.put(current_class, dummy);

             } else { // new class id       
                current_neighbourhood.put(current_class, new Double(addval));
             } // esle
         } // rof Enumeration e

         if (d) {System.out.println("Neighbourhood of node nr "+node_nr+":"+current_neighbourhood.toString());}
         
         //determine winning class in neighbourhood
         maximal_class=0;
         maxvalue=0.0;
         int current_class_int=0;

         // randomize order: put it in list, shuffle list
         list2=new ArrayList();

         for(Enumeration e=current_neighbourhood.keys();e.hasMoreElements();) {
             list2.add((Integer)e.nextElement());
         }  // rof Enum e     
         
         Collections.shuffle(list2,r);
     
         for(Iterator it=list2.iterator();it.hasNext();) {	
                current_class_int=((Integer)it.next()).intValue();
                curr_val=(Double)current_neighbourhood.get(new Integer(current_class_int));
                
                if (d) System.out.println(node_nr+": testing for "+current_class_int+" with value "+curr_val);
                
            if ((current_class_int>0)&&(curr_val.doubleValue()>maxvalue)) {
	        maxvalue=curr_val.doubleValue();
 	        maximal_class=current_class_int;
 	    }
         } // rof Iterator it 

         // update class
         if (maximal_class==0) {maximal_class=node_class[node_nr];}  // inhibit zeroes
         if (d) {System.out.println("Updated node nr. "+node_nr+" with class "+maximal_class);}   	
       
         if (mode==1) {
            if ((maxvalue/valsum)>votethresh) { new_node_class[node_nr]=maximal_class; } else {new_node_class[node_nr]=node_class[node_nr];}
         } else { // take max
            new_node_class[node_nr]=maximal_class; // stay as you are
         }
         // mutation
         if ((maximal_class>0)) {
           dice=r.nextDouble();
           if (dice<mut_rate) {
           largest_class++;
           new_node_class[node_nr]=largest_class;
           if (d) System.out.println("  and mutated to "+new_node_class[node_nr]);
         }} // final maximal_class mutate
        } // fi node_ok 2
        
       } // esle keep_colour dice      
    } // rof enum wordnumbers
  } // end perform_iteration
  
  public String consoleChooserString_colors;
  public String consoleChooserString_colors_read;
  
  /**
   * writes result to file, opens file dialogue
   * 
   * @param withdialog  If true it shows file-dialogue.
   * @param withClusters with or without read-format.
   * @param fromConsole true if on console.
   */
   public synchronized void writeFile(boolean withdialog, boolean withClusters, boolean fromConsole) {
       this.writeFile(withdialog, withClusters,fromConsole, "");
   } 
  
  public synchronized void writeFile(boolean withdialog, boolean withClusters, boolean fromConsole, String filename) {
     
      	writeFileProgress=1;
     	countNodesWithClasses=0;
     	
      	System.out.println("*\tWriting files:");    
      	// node ids
        Integer node_id;
        // class ids
        Integer node_class_new;
        
        String myChooser_colors;
        String myChooser_colors_read;
        if ((withdialog)&&(filename.equals(""))){
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showSaveDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                myChooser_colors= chooser.getSelectedFile().getAbsolutePath();
                myChooser_colors_read=myChooser_colors+".read";
            }else{
                System.out.println("\t--\taborted\n*\tdone.");
                //this call can only be positioned here or at the end of method:
                // for making clear that files have been written 
                setWritesOnlyFiles(false);
                return;
            }
        }
        else {//Case: Write in database
            myChooser_colors=Controller.CLASSES_TMP_FILENAME;
            myChooser_colors_read=myChooser_colors+".read";
        }
        
        //case: running on console
        if(fromConsole){
            myChooser_colors=consoleChooserString_colors;
            myChooser_colors_read=consoleChooserString_colors_read;
        }
    	
        if (!filename.equals("")) {
            myChooser_colors=filename;
            myChooser_colors_read=filename+".read";          
        }
        
          // map classes to 1..n without gaps
          classTable = new Hashtable();
          Integer node_class_old;
          int counter = 1;
          float count =0;
          for(int i=1;i<=max_node_nr;i++) {
              count+=50./max_node_nr;
              writeFileProgress=(int)(count);
              if (node_ok[i]) { 
                  node_class_old = new Integer(node_class[i]);
                  //ist die Farbe noch nicht geändert 
                  if(!classTable.containsKey(node_class_old)){
                      classTable.put(node_class_old,new Integer(counter));
                      counter++;
                  }
              }
          } // rof i
          
          try {
              Writer writer = new FileWriter(myChooser_colors);	
              writeFileProgress=(int)(count);
              for(int i=1;i<=max_node_nr;i++) {
                  count+=50./max_node_nr;
                  writeFileProgress=(int)(count);
                  if (node_ok[i]) {
                      node_id = new Integer(i);
                      node_class_old = new Integer(node_class[i]);
                      node_class_new = ((Integer) classTable.get(node_class_old));
                      
                      // statistcal purposes, obsolete
                      if(node_class_new.intValue()>maxClusterID)maxClusterID=node_class_new.intValue();
                      
                      Object [] neigh_classes = calcNeighbours(node_id.intValue());
                                            
                      //construct out line
                      String a = node_id+"\t" +nodeLabels.getWord(new Integer(i))+"\t"+node_class_new+"\t"+neigh_classes[0]+"\t"+neigh_classes[1]+"\t"+neigh_classes[2]+"\t"+neigh_classes[3];
                                                                 
                      writer.write(a);
                      countNodesWithClasses++;
                      if(i!=max_node_nr) writer.write('\n');
                  }
              }
              writer.close();
              
          }catch(Exception e) {System.err.println("Error while writing files\n"+e);}

          // sort clusters
          if(withClusters){
              writeClusters(myChooser_colors_read);
              System.out.println("\t-\t"+myChooser_colors_read);
          }
          System.out.println("\t-\t"+myChooser_colors+"\n*\tdone.");
          
          //this call can only be positioned here or at the end of method:
          // for making clear that files have been written 
          setWritesOnlyFiles(false);

  } // end writeFile()
  
  
  /**
 * Returns an array of neighbourhood properties
 * @param v Node
 * @return
 */
public Object[] calcNeighbours(int curr_node_nr){

        List line;
        //line econtains all neighbouring node IDs of node ID curr_node_nr
        line = filterByThresh(edges.getData(new Integer(curr_node_nr)));
        if(line.isEmpty()) return null;

        Integer colorV[] = new Integer[line.size()];

        List neighbours = new ArrayList(); 
        int i = 0;
        //for all neighbours
        for (Iterator it = line.iterator(); it.hasNext();) {
                 //gets from neighbor [0]..curr_node_nr; [1]..weight of edge (curr_node_nr,neighbour)
                Integer[] actVals = (Integer[])it.next();

                Integer current_class = new Integer( ( (Integer)classTable.get(new Integer(node_class[actVals[0].intValue()]))).intValue());

                Integer weight = actVals[1];
                Double degr = (Double)degree.get(actVals[0]); 	

                neighbours.add(new NeighbourPreferences(current_class,weight,degr));
        }
        Collections.sort(neighbours);

        Object[] out = new Object[4];
        out[2]=new Integer(0);
        out[3]=new Double(0);

        Iterator iter = neighbours.iterator();

        boolean gotFirstClass=false;

        Integer savedClass = new Integer(0);
        List neigbourHoodEnvironment= new ArrayList();
        double valCounter=0;
        double savedVal=0;

        while(iter.hasNext()){
            NeighbourPreferences np = (NeighbourPreferences)iter.next();
            Integer actColor = np.getClassID();
            Integer actSig = np.getWeight();
            Double actDegr = np.getDegree();

            //gleiche Farben liegen hintereinander, da sort
            if(savedClass.intValue()==actColor.intValue()){
                if(mode==2){
                    savedVal += actSig.intValue()/actDegr.doubleValue();
                }
                else{
                    savedVal += actSig.intValue();
                }
            }
            else{
                if(gotFirstClass){
                    neigbourHoodEnvironment.add(new NodeEnvironment(savedClass,new Double(savedVal)));
                    valCounter += savedVal;
                }
                if(mode==2){
                    savedVal = actSig.intValue()/actDegr.doubleValue();
                }
                else{
                    savedVal = actSig.intValue();
                }

                gotFirstClass=true;
            }
            savedClass=actColor;
        }
        neigbourHoodEnvironment.add(new NodeEnvironment(savedClass,new Double(savedVal)));
        valCounter += savedVal;

        Collections.sort(neigbourHoodEnvironment);
        iter = neigbourHoodEnvironment.iterator();

        while(iter.hasNext()){
            ((NodeEnvironment)iter.next()).scale(new Double(valCounter));
        }

        out[0]=((NodeEnvironment)neigbourHoodEnvironment.get(0)).getColor();
        out[1]=new Double(Math.round((((NodeEnvironment)neigbourHoodEnvironment.get(0)).getWeightedValueOfColor()).doubleValue()*1000)/10.);
        if(neigbourHoodEnvironment.size()>1){
                out[2]=((NodeEnvironment)neigbourHoodEnvironment.get(1)).getColor();
                out[3]=new Double(Math.round((((NodeEnvironment)neigbourHoodEnvironment.get(1)).getWeightedValueOfColor()).doubleValue()*1000)/10.);
        }
        return out;
 } // end calcNeighbours



  //*************************************************Start******************************************
 /**
  * Returns number of iterations
  * @return number of iterations
  */
  public int getIteration(){
  	return iterations;
  }
  /**
   * Returns current iteration
   * @return current iteration
   */
   public int getCurrentIteration(){
   	return currentIteration; 
   }

   /**
   * returns min weight threshold
   * @return min weight threshold
   */
  public int getMinWeight() {
	return this.min_weight;
  }
  
   
  /**
   * returns first option
   * @return first algorithm option
   */
  public String getAlgOpt() {
	return algOpt;
  }
  
  /**
   * returns 2nd option
   * @return 2nd algorithm option
   */
  public String getAlgOptParam() {
	 return algValue;
  }
  
  /**
   * returns mutation option
   * @return mutation option
   */
  public String getMutOpt() {
	return algMutOpt;
  }
  
  /**
   * returns update strategy option
   * @return update strategy option
   */
  public String getUpdateOpt() {
	 return algUpdateOpt;
  }
  
  
  /**
   * returns mutation value as string
   * @return mutation value
   */
  public double getMutValue() {
	return algMutValue;
  }

  /**
   * returns keep color rate
   * @return keep color rate
   */
  public double getKeepValue() {
	 return algKeepValue;
  }
  
  
  /**
   * returns edge list
   * @return edge list 
   */
  public BinFileMultCol getEdges() {
	return edges;
  }
  
  /**
   * returns node list
   * @return node list
   */
 public BinFileStrCol getNodes() {
	return nodeLabels;
 }
 


 public List filterByThresh(List unfilteredList) {
     ArrayList retlist=new ArrayList();
     
     for (Iterator it = unfilteredList.iterator(); it.hasNext();) {       
            Integer current[]=(Integer[])it.next();
            if (current[1].intValue()>=this.min_weight) {
                retlist.add(current);
            } // fi threshold         
     } // rof 
     
     return retlist;
 } // end filterByThresh
 
 /**
  * sets the graph
  * @param nodeListFile
  * @param edgeListFile
  */ 
 public void setCWGraph(String args_nodeListFile, String args_edgeListFile) {
    this.nodeListFile=args_nodeListFile;
    this.edgeListFile=args_edgeListFile;
    if (d) System.out.print("[ChineseWhispers] graph set to:\n\t"+
            "node list file: "+nodeListFile+
            "\n\tedge list file: "+edgeListFile);
    
    // prepare binary files
    makeBinFiles();	   
 
 } // end setCWGraph
 

 
 // sets the parameters
 public void setCWParameters(int args_min_weight,String algOption1,String algOption2,double args_keep, String args_mut1, double args_mut2, String args_update, int args_iterations, boolean isFileOrDBOut){
 
    this.min_weight=args_min_weight;
    this.algOpt=algOption1;
    this.algValue=algOption2;
    this.algKeepValue=args_keep;
    this.algMutOpt=args_mut1;
    this.algMutValue=args_mut2;
    this.algUpdateOpt=args_update; 
    this.iterations = args_iterations;
    this.isFileOrDBOut=isFileOrDBOut;

    System.out.print("\n[ChineseWhispers] initialized with parameters:\t"+
            "\n\tminimum weight: "+min_weight+
            "\n\tnumber of iterations: "+iterations+
            "\n\talgorithm option: "+algOpt+" "+algValue+
            "\n\tkeep color rate: "+algKeepValue+
            "\n\tmutation strategy: "+algMutOpt+
            "\n\tmutation rate: "+algMutValue+
            "\n\tupdate strategy: "+algUpdateOpt+
            "\n\t");
    if (d) if (isFileOrDBOut) {System.out.println("write results");} else {System.out.println("display graph");}
  
 } // end setCWParameters
 
 /**
   * Starts the algorithm
   * 
   * @param algOption1 first algorithm option e.g. 'top'
   * @param algOption2 2nd algorithm option e.g. 'nolog' or '0.5'
   */
  
  //public void runAlgorithm(String algOption1,String algOption2, String args_keep, String arg_mut_opt, String arg_mut_param, String arg_update) {
  public void runAlgorithm() {
      if (d) System.out.println("[cw] running algorithm...");
      
      this.degree		= new Hashtable();
      this.classTable  		= new Hashtable();
      this.Iter_Classes 	 	= new Hashtable[2];
      this.Iter_Classes[0] 	= new Hashtable();
      this.Iter_Classes[1] 	= new Hashtable();

      // defaults
      mode=0;
      nolog=true;
      votethresh=0.0;
      keep_color_rate=0.0;
      mut_opt=1;
      mut_param=0.0;
      update_param=2;
          
          
      // class transfer modes
      if (this.algOpt.equals("top"))  {mode=0;}  // largest class wins
      if (this.algOpt.equals("vote")) {
          mode=1;
          votethresh=(new Double(this.algValue)).doubleValue();} // class with above votethresh wins
      if (this.algOpt.equals("dist")) {
          mode=2;
          if (this.algValue.equals("nolog")) {nolog=true;} 
          else {nolog=false;}
      } // class is reweighted by source's degree, largest wins
      
      // mutation mode
      if (this.algMutOpt.equals("dec")) {mut_opt=1;}
      if (this.algMutOpt.equals("constant")) {mut_opt=2;}
      mut_param=this.algMutValue;
      
      // update mode
      if (this.algUpdateOpt.equals("stepwise")) {update_param=1;}
      if (this.algUpdateOpt.equals("continuous")) {update_param=2;}    
                 
 
      initGraph();
            
      // outer loop for iterations      
      for(int it=1;it<iterations ;it++) {  //iteration
        
        if(! (currentIteration>it))currentIteration++;
        
        if (mut_opt == 1) { // dec
           mut_rate=(double)1/((double)(Math.pow(it,mut_param)));  // change mutation rate dependent on iteration
        } else {  // const
           mut_rate=mut_param;
        }
                      
        perform_iteration();
        
        if(!isFileOrDBOut){
            Iter_Classes[0].put(new Integer(it),node_class.clone());
            Iter_Classes[1].put(new Integer(it),node_ok.clone());
        }
         
        if (update_param == 2) { 
           node_class=new_node_class;  // continuous
        } else { //stepwise         
          for(int somei=1;somei<=max_node_nr;somei++) {
           if(node_ok[somei]) {node_class[somei]=new_node_class[somei]; }
          } // rof
        } // esle
        
        
        // for eval purposes: writes a result file each iteration
        //System.out.println("Iteration "+it);
        //writeFile(false, false, true, "C:\\temp\\cw-temp\\files"+it+".txt");
  
       } // end iteration  
      
       
       mut_rate=0;
       perform_iteration();  //collect random noised in one last iteration
       
       if(!isFileOrDBOut){
           Iter_Classes[0].put(new Integer(iterations),node_class);
           Iter_Classes[1].put(new Integer(iterations),node_ok);
       } // fi isFileORDBOut   
     
  } // end void start
  
  /**
   * returns class per node per iteration
   * @param node node nr
   * @param itera iteration
   * @return class nr.
   */
  public int getColorVertex(int node, int itera){
  	
  	if (itera == 0)  return 0;
  	
  	boolean isOK = ((boolean[])Iter_Classes[1].get(new Integer(itera)))[node];
      	
  	if (!isOK) return 0;
  	else{
  		//holen der Knotenfarbe der Iterationsstufe
  		int i[] = (int[]) Iter_Classes[0].get(new Integer(itera)); 
  		return i[node];
  	}
  }
  
  public Hashtable show_clusters() {
    //shows how many nodes have which class 
       
    Hashtable classIDs=new Hashtable();
    Hashtable nodes_per_class= new Hashtable();
    Integer current_class;
    String dummystr;
    for(int i=1;i<=max_node_nr;i++) {
     if (node_ok[i]) {
       current_class=new Integer(node_class[i]);
       if (classIDs.containsKey(current_class)) { //known node_class
            classIDs.put(current_class, new Integer(((Integer)classIDs.get(current_class)).intValue()+1));
            nodes_per_class.put(current_class, new String( ((String)nodes_per_class.get(current_class))+", "+ nodeLabels.getWord(new Integer(i)) ) );

         } else { // neue node_class        
	    classIDs.put(current_class, new Integer(1));	
            nodes_per_class.put(current_class, nodeLabels.getWord(new Integer(i))); 
         } // esle
     } //fi node_ok
    } // rof for all node_labels
    
    
    Hashtable helphash=new Hashtable();
    Integer clustersize;
    //Integer current_class;
    for(Enumeration e=classIDs.keys();e.hasMoreElements();){
        current_class= (Integer)e.nextElement();
        clustersize=(Integer)classIDs.get(current_class);
        if(helphash.containsKey(clustersize)){
            helphash.put(clustersize,new Integer(((Integer)helphash.get(clustersize)).intValue()+1));
        }
        else{
            helphash.put(clustersize,new Integer(1));
        }
    }
    
    return helphash;  
  } // end show_clusters
  
  /**
   * Starts Chinese Whispers algorithm.
   */
  public synchronized void run(){
      currentIteration=(getIteration()*5)/100;
      
      if (d) System.out.println("[cw] run() called, isNumbered="+isNumbered);
      
      makeBinFiles();
      currentIteration=(getIteration()*15)/100;
      
      runAlgorithm();
      
      isActive=false;
  }
  
  /**
   * Used by writeFile().<br>
   * Writes the by clustersize sorted statistic-file. Before started it is important to use run() and writeFile().
   * @param fileName The filename.
   */

  public void writeClusters(String fileName) {
      writeFileProgress=100;
      ArrayList nodeList=new ArrayList();
      Integer current_class;     
      float count =0;

      // collect nodes with colors
      for(int node_nr=1;node_nr<=max_node_nr;node_nr++) {
        count+=195./max_node_nr;  
        writeFileProgress=(int)(count);  
        if (node_ok[node_nr]) {
            current_class=(Integer)classTable.get(new Integer(node_class[node_nr]));
            nodeList.add(new IntegerPair(new Integer(node_nr), current_class));                  
        } // fi node_ok
      } // rof node_nr
      
      
      // sort
      Collections.sort(nodeList);
      
      // determine cluster sizes
      Hashtable cluster_sizes=new Hashtable();
      
      for(Iterator it = nodeList.listIterator();it.hasNext();) {
          IntegerPair actPair=(IntegerPair)it.next();
          Integer clusterID=actPair.i2();
          
          if (cluster_sizes.containsKey(clusterID)) {
              cluster_sizes.put(clusterID, new Integer(((Integer)cluster_sizes.get(clusterID)).intValue()+1));
              
          } else {
              cluster_sizes.put(clusterID, new Integer(1));
          }
      } // rof it
      
      // output
      
    try {
      Writer writer = new FileWriter(fileName);
 
      
      int oldID=0;
      for(Iterator it = nodeList.listIterator();it.hasNext();) {
          IntegerPair actPair=(IntegerPair)it.next();
          int actwnr=actPair.i1().intValue();
          int clusterID=actPair.i2().intValue();

          if (clusterID==oldID) {
              writer.write(", "+nodeLabels.getWord(new Integer(actwnr)));
          } else  {
              if (oldID!=0) {writer.write("\n"); }              
              
              writer.write(clusterID+"\t"+(Integer)cluster_sizes.get(new Integer(clusterID))+"\t"+nodeLabels.getWord(new Integer(actwnr)));
              oldID=clusterID;
          } // esle
      
      } // rof it
      writer.close();
      writeFileProgress=200;
    }catch(Exception e) {}
 
      
  } // end public void writeClusters
  
  
  /**
   *
   * @author seb
   */	
  class ClusteringByClass implements Comparable{
      
      private Integer classID;
      private Integer node_count;
      private String node_labels;
      /**
       * Creates new classes object that contains the class, membercount and members.
       * @param class The class
       * @param node_count The number of nodes
       * @param node_labels The node labels of this class
       */
      public ClusteringByClass(Integer classID, Integer node_count, String node_labels){
          this.classID=classID;
          this.node_count=node_count;
          this.node_labels=node_labels;
  	  }
      /**
       * Compares membercount.
       */
      public int compareTo(Object i){
  	        
          if( ((ClusteringByClass) i).getNodeCount().intValue() < node_count.intValue()) return -1;
  	      else if( ((ClusteringByClass)i).getNodeCount().intValue() > node_count.intValue())return 1;
  	      else return 0;
  	    }
      /**
       * Get membercount.
       * @return The member count.
       */
      public Integer getNodeCount(){
          return this.node_count;
      }
      /**
       * Get classID.
       * @return The classID.
       */
      public Integer getClassID(){
          return this.classID;
      }
      /**
       * Get all node_labels.
       * @return The node labels.
       */
      public String getNodeLabels(){
          return this.node_labels;
      }
  	}
} // ssalc ClusteringByClass


//***sorting of neighbourhood
class MyCountSort {	
	public LinkedList getCountSort(Object[] w) {
		
        LinkedList L = new LinkedList(); 
        Hashtable h = new Hashtable();
        LinkedList erg;


        for(int k=0 ; k< w.length ; k++){
                //wenn current_class schon gezählt
                if(h.contains(w[k]))continue;
                int zaehler=1;	
                for(int i=k+1 ; i< w.length ; i++){
                    if(w[k].equals(w[i]))zaehler++;
                }
                h.put(new Integer(k),w[k]);

                erg = new LinkedList();
                erg.add(w[k]);
                erg.add(new Integer(zaehler));
                L.add(erg);
                Collections.sort(L, new Mycomp());

        }

        return L;
    }
	
	
    public LinkedList getSort(Object[] w) {
		
        LinkedList L = new LinkedList(); 
        Hashtable h = new Hashtable();
        LinkedList erg;

        for(int k=0 ; k< w.length ; k++){
                //wenn current_class schon gezählt
                if(h.contains(w[k]))continue;
                int zaehler=1;	
                for(int i=k+1 ; i< w.length ; i++){
                if(w[k].equals(w[i]))zaehler++;
                }
                h.put(new Integer(k),w[k]);

                erg = new LinkedList();
                erg.add(w[k]);
                erg.add(new Integer(zaehler));
                L.add(erg);
                Collections.sort(L, new Mycomp());

        }

        return L;
    }
	
class Mycomp implements Comparator{
	
	public int compare(Object o1, Object o2){
		
		int i1 = ( (Integer)((LinkedList)o1).get(1) ).intValue();
		int i2 = ( (Integer)((LinkedList)o2).get(1) ).intValue();
		return (i2-i1);
	}
	
	
	public boolean equals(Object obj) {
		
		if(compare(this,obj) == 0 )return true;
		else return false;
		
	}

} // end Mycomp
} // end class ChineseWhispers


//******