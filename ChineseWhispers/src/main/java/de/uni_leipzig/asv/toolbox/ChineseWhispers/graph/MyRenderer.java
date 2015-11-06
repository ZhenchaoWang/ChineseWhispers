package de.uni_leipzig.asv.toolbox.ChineseWhispers.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_leipzig.asv.coocc.*;

import de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm.ChineseWhispers;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.NumberVertexValue;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberVertexValue;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;
import edu.uci.ics.jung.visualization.PluggableRenderer;



/**
 * 
 * @author Rocco
 *
 */
public class MyRenderer extends PluggableRenderer {
	
	protected final static Object WIDTH = "width";
	protected NumberVertexValue width   = new UserDatumNumberVertexValue(WIDTH);
	
	protected final static Object HEIGHT  = "height";
	protected NumberVertexValue height    = new UserDatumNumberVertexValue(HEIGHT);
	
	protected final static Object COLOR  = "color";
	protected NumberVertexValue color    = new UserDatumNumberVertexValue(COLOR);
	
	
	protected VertexShapeSizeAspect vssa;
    
	private ChineseWhispers cw;
	private BinFileStrCol nodes; 
	private BinFileMultCol edges;
	
	protected int current_iteration = 0;
	
	public final int default_rect_size = 12;
	public final int default_circle_size = 6;
	protected int rect_size = default_rect_size;
	protected int circle_size    = default_circle_size;
	private boolean edgeColor;
	private boolean show_label = false;
	
		
	public void init(ChineseWhispers cw){
		this.cw = cw;
		this.nodes = cw.getNodes();
		this.edges = cw.getEdges();
		edgeColor = true;
		current_iteration = 0;
		vssa = new VertexShapeSizeAspect(width,height);
		setVertexShapeFunction(vssa);
		
	} // end init
	
	
	
	
	public void paintEdge(Graphics g,Edge e,int x1,int y1,int x2,int y2) { 
		
            String fromNode = nodes.getWord(new Integer((String)((Vertex)e.getEndpoints().getFirst()).getUserDatum("Label")));
            String toNode= nodes.getWord(new Integer((String)((Vertex)e.getEndpoints().getSecond()).getUserDatum("Label")));

            if(edgeColor) g.setColor(Color.GRAY);
            else g.setColor(Color.white);
            g.drawLine(x1,y1,x2,y2);			
	} // end paint edge
	
	
	/**
	 * increases node size
	 *
	 */
	public void makeBig(){
		rect_size++; 
		circle_size++;
	}
	
	/**
	 * decreases node size
	 *
	 */
	public void makeSmall(){
		if(rect_size >0) {rect_size--;}
		if(circle_size >0)  {circle_size--;}
	}
	
	/**
	 * defaults node size
	 *
	 */
	public void setSizeDefault(){
		rect_size = default_rect_size;
		circle_size = default_circle_size;
	}
	
	/**
	 * sets node color for singe node
	 * @param it iteration step
	 */
	public void setIterColor(int it){
		this.current_iteration = it;
	}
	
	
	/**
	 * adds/deletes edges
	 *
	 */
	public void setEdges(){
		edgeColor = !edgeColor;
	}
	
	/**
	 * changes node shape<br>
	 * from circke to rectangle with label or vice versa<br>
	 *
	 */
	public void changeShapeNode(){
		show_label = !show_label;
	}
	
	/**
	 * set node shape to circle
	 */
	public void setShapeNodeDefault(){
		show_label = false;
	}
	
	/**
	 * returns neighbour properties in html format
	 * @param v Knoten
	 * @return
	 */
	public String calcNeighbours(Vertex v){
    	
            List line;
            line = cw.filterByThresh(edges.getData(new Integer((String)v.getUserDatum("Label"))));
            if(line.isEmpty()) return null;


            Integer colorV[] = new Integer[line.size()];
            int i = 0;
            for (Iterator it = line.iterator(); it.hasNext();) {
                            Integer[] actVals = (Integer[])it.next();
                            colorV[i++]= new Integer(cw.getColorVertex(actVals[0].intValue(),current_iteration));
            }
            MyCountSort MS= new MyCountSort();

            LinkedList L = MS.getCountSort(colorV);

            String back = "<tr><td colspan=3><b>neighborhood:<br>("+line.size()+" Vertices)</p></td></tr>";

            for(int l=0 ; l < L.size(); l++){
            if(l==4)break;

            int C1     = ((Integer)((LinkedList)L.get(l) ).get(0)).intValue();
            int Anz_C1 = ((Integer)((LinkedList)L.get(l) ).get(1)).intValue();

            String colorweb = getColorWeb(C1);

            back+=  "<tr><td bgcolor="+colorweb+">"+(l+1)+". color:</td><td><b>"+C1+"</b></td><td><b>"+Math.round(Anz_C1*1000/line.size())/10.0+" %</b></td></tr>"; 
            }
            return back;
        } // end calcNeighbours
 
	
	
	public void paintVertex(Graphics g,Vertex v,int x1,int y1) { 
		
		
		String Label = nodes.getWord(new Integer((String)v.getUserDatum("Label")));
	    boolean bigger = false;
		
		Font fon= new Font("Dialog",Font.PLAIN,rect_size);
		g.setFont(fon);

		FontMetrics FM = g.getFontMetrics();
		int w = FM.stringWidth(Label);
		int h = FM.getHeight();
		
		
		int Rand= h/4;
		
		
		int node = (Integer.parseInt((String)v.getUserDatum("Label")));
		
		Color Farbe;
		
		if(current_iteration != 0){
			Farbe = colorFunk(cw.getColorVertex(node,current_iteration));
			color.setNumber(v, new Integer(cw.getColorVertex(node,current_iteration)));
		}
		else{
			Farbe = Color.WHITE;
			color.setNumber(v, new Integer(0));
		}
		
		if(show_label){ // rectangle

                    width.setNumber(v, new Float(w+Rand));
                    height.setNumber (v, new Float(h+Rand));

                    g.setColor(Farbe);
                    g.fillRoundRect(x1-(w+Rand)/2,y1-(h+Rand)/2,w+Rand,h+Rand,10,10);
                    
                    g.setColor(Color.black);
                    g.drawString(Label,x1-w/2,Math.round(y1+((float)h/2f)-Rand));

                    g.setColor(Color.black);
                    g.drawRoundRect(x1-(w+Rand)/2,y1-(h+Rand)/2,w+Rand,h+Rand,10,10);
			
                } else{ // circle

                    width.setNumber(v, new Float(circle_size));
                    height.setNumber (v, new Float(circle_size));
                    g.setColor(Farbe);
                    g.fillOval(x1-circle_size/2,y1-circle_size/2,circle_size,circle_size);
                    g.setColor(Color.black);
                    g.drawOval(x1-circle_size/2,y1-circle_size/2,circle_size,circle_size);
		}
	}
	
	
	/**
	 * computes color for int value
	 * @param i classID of node
	 * @return color
	 */
	private Color colorFunk(int i){
		
		if(i==0) return Color.WHITE;
		int c[] = {0,0,0};
		
		c[i%3]=255;
		i = (i*i)%256;   // CB changed here
			
		for(int k=1; k < 3*i+5 ; k++)
		{
			c[((i/k))%3]+= 50;			
		}
			
		Color nc = new Color( Math.abs(c[0]%256) ,Math.abs(c[1]%256) ,Math.abs(c[2]%256) );
		
		return nc;

	} // end colorFunk
	
	/**
	 * computes web color 
	 * @param i classID
	 * @return webcolor in format like  #FF00AA
	 */
	private String getColorWeb(int i){
		
		Color c = colorFunk(i);
		
		String red = Integer.toHexString(c.getRed());
		String green = Integer.toHexString(c.getGreen());
		String blue = Integer.toHexString(c.getBlue());
		
		//leading zero
		if(red.length()   == 1) red   = "0" +red;
		if(green.length() == 1) green = "0" +green;
		if(blue.length()  == 1) blue  = "0" +blue;
				
		return "#"+red+green+blue;
	}
	
	/**
	 * @return Returns the current iteration
	 */
	public int getIter() {
		return current_iteration;
	}


//********
	/**
	 * 
	 * @author Rocco
	 * class for handling node shape, size, ratio
	 */
	 private final static class VertexShapeSizeAspect 
	    extends AbstractVertexShapeFunction 
	    implements VertexSizeFunction, VertexAspectRatioFunction
	    {
	       
	        protected NumberVertexValue width;
	        protected NumberVertexValue height;
	        
	        public VertexShapeSizeAspect(NumberVertexValue width,NumberVertexValue height)
	        {
	            this.width = width;
	            this.height = height;
	            setSizeFunction(this);
	            setAspectRatioFunction(this);
	        }
	        
	     
	        
	        public int getSize(Vertex v)
	        {
	        	try{
	        	    return (int)(width.getNumber(v).intValue());
	        	}
	        	catch(NullPointerException ne){System.err.println("Catched NullpointerException while trying getSize(Vertex v)!"); return 1;}
	        }
	        
	        
	        public float getAspectRatio(Vertex v)
	        {
	          return ( height.getNumber(v).floatValue()/ width.getNumber(v).floatValue()  );
	        }
	        
	        
	        public Shape getShape(Vertex v)
	        {
	        	return factory.getRoundRectangle(v);        	
	        }	
	        
	    }

//********

//***sortieren
	 class MyCountSort {

		
		
		
		public LinkedList getCountSort(Object[] w) {
			
			LinkedList L = new LinkedList(); 
			Hashtable h = new Hashtable();
			LinkedList results;
			
			
			for(int k=0 ; k< w.length ; k++){
				//if already seen
				if(h.contains(w[k])) continue;
				int counter=1;	
				for(int i=k+1 ; i< w.length ; i++){
				if(w[k].equals(w[i]))counter++;
				}
				h.put(new Integer(k),w[k]);
				
				results = new LinkedList();
				results.add(w[k]);
				results.add(new Integer(counter));
				L.add(results);
				Collections.sort(L, new Mycomp());
				
			}
			
			return L;
		}
		
		
		public LinkedList getSort(Object[] w) {
			
			LinkedList L = new LinkedList(); 
			Hashtable h = new Hashtable();
			LinkedList results;
			
			for(int k=0 ; k< w.length ; k++){
				//if already seen
				if(h.contains(w[k]))continue;
				int counter=1;	
				for(int i=k+1 ; i< w.length ; i++){
				if(w[k].equals(w[i]))counter++;
				}
				h.put(new Integer(k),w[k]);
				
				results = new LinkedList();
				results.add(w[k]);
				results.add(new Integer(counter));
				L.add(results);
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

	}
	}
	 
//******
}
