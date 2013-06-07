package de.tum.ascodt.plugin.ui.gef.figures;

import java.util.HashMap;
import java.util.Vector;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

//TODO import de.tum.ascodt.plugin.core.model.CCAClasspathRepository;
import de.tum.ascodt.plugin.ui.gef.model.Port;

/**
 * This class defines a special border with port anchors
 * @author atanasoa
 *
 */
public class ComponentBorder extends org.eclipse.draw2d.LineBorder{
	/**
	 * reference to the repository
	 */
	//TODO private CCAClasspathRepository rep;

	/**
	 * flag remote component 
	 */
	//TODO private boolean isRemote;
	private Figure parent;
	protected HashMap<Port,PortAnchor> inputConnectionAnchors;
	protected HashMap<Port,PortAnchor> outputConnectionAnchors;
	protected Vector<Port> usePorts;
	protected Vector<Port> providePorts;
	protected HashMap<PortAnchor, Port> inputConnectionPorts;
	protected HashMap<PortAnchor, Port> outputConnectionPorts;
	private int []anchorSize;
	private Port matchingPort;
	protected Vector<RectangleFigure> usePortsFigs;
	protected Vector<RectangleFigure> providePortsFigs;
	public ComponentBorder(boolean isRemote, Figure parent,
			Vector<Port> usePorts,Vector<Port> providePorts, 
			Vector<RectangleFigure> input, Vector<RectangleFigure> output){
		super();
		//TODO this.rep=rep;
		//this.isRemote=isRemote;
		this.parent=parent;
		this.usePorts=usePorts;
		this.providePorts=providePorts;
		this.matchingPort=null;
		inputConnectionAnchors = new HashMap<Port,PortAnchor>();
		outputConnectionAnchors = new HashMap<Port,PortAnchor>();
		inputConnectionPorts = new HashMap<PortAnchor, Port>();
		outputConnectionPorts = new HashMap<PortAnchor, Port>();
		usePortsFigs=output;
		providePortsFigs=input;
		anchorSize=new int[2];
		createConnectionAnchors(parent.getBounds().getCopy());


	}
	private int getMaxConnectors(){
		return Math.max(inputConnectionPorts.size(),outputConnectionPorts.size());
	}
	private void createConnectionAnchors(Rectangle rec) {
		int y1 = rec.y,  
		height = rec.height,x1=rec.x,
		right =rec.x+rec.width-(rec.height/8)-1;
		for (int i = 0; i < providePorts.size(); i++){
			y1 = rec.y+((int)(0.27*((double)rec.height))) + (2 * i + 1) * height / 8;
			createTargetConnectionAnchor(providePorts.elementAt(i),x1,y1);
		}
		for (int i = 0; i < usePorts.size(); i++){
			y1 = rec.y+((int)(0.27*((double)rec.height))) + (2 * i + 1) * height / 8;	
			createSourceConnectionAnchor(usePorts.elementAt(i),right,y1);
		}
	}
	private void createSourceConnectionAnchor(Port model,int right, int y1) {
		PortAnchor anchor=new PortAnchor(this,parent,new Point(right,y1));
		outputConnectionAnchors.put(model,anchor);
		outputConnectionPorts.put(anchor,model);
	}

	private void createTargetConnectionAnchor(Port model,int x1, int y1) {
		PortAnchor anchor=new PortAnchor(this,parent,new Point(x1,y1));
		inputConnectionAnchors.put(model,anchor);
		inputConnectionPorts.put(anchor,model);
	}
	private boolean checkIfClassesCompotible(String classUsePort,String classProvidePort){
//		Class<?> usePortClass=null;
//		Class<?> providePortClass=null;
//		try {
//			usePortClass = rep.loadClass(classUsePort);
//
//			providePortClass=rep.loadClass(classProvidePort);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}	
//		if(usePortClass!=null&&providePortClass!=null)
//			return usePortClass.isAssignableFrom(providePortClass);
		return true;
	}
	/**
	 * draws boxes for the different connectors and creates
	 * @param g
	 * @param rec
	 * @param org_height 
	 */
	private void drawConnectors(Graphics g, Rectangle portRectangle, Rectangle componentRectange) {
		anchorSize[1]= portRectangle.height/8;
		anchorSize[0]= portRectangle.width/8;
		int y1 = componentRectange.y,  
		height = portRectangle.height,x1=componentRectange.x,
		right =componentRectange.x+componentRectange.width-anchorSize[0];

		
		

		for (int i = 0; i < providePorts.size(); i++) {
			y1 = componentRectange.y+((int)(0.27*((double)componentRectange.height))) + (2 * i + 1) * height / 8;
			providePortsFigs.elementAt(i).setSize(anchorSize[0],anchorSize[1]);
			providePortsFigs.elementAt(i).setForegroundColor(ColorConstants.green);
			providePortsFigs.elementAt(i).setBackgroundColor(ColorConstants.lightGray);
			if(matchingPort!=null
					&&checkIfClassesCompotible(matchingPort.getDescription().substring(matchingPort.getDescription().indexOf(":")+1),providePorts.elementAt(i).getDescription())
			){
				providePorts.elementAt(i).setIsConnectable(true);
				providePortsFigs.elementAt(i).setBackgroundColor(ColorConstants.green);
			}else{
				providePorts.elementAt(i).setIsConnectable(false);
			}
			providePortsFigs.elementAt(i).setLocation(new Point(x1,y1));
			providePortsFigs.elementAt(i).repaint();
//			connector.translate(x1, y1);
//			g.fillPolygon(connector);
//			g.drawPolygon(connector);
//			
//			connector.translate(-x1, -y1);
			invalidateTargetConnectionAnchor(providePorts.elementAt(i),x1,y1, height / 8);
		}
		g.setBackgroundColor(ColorConstants.lightGray);
		g.setForegroundColor(ColorConstants.red);
		for (int i = 0; i < usePorts.size(); i++) {

			usePortsFigs.elementAt(i).setBackgroundColor(ColorConstants.lightGray);
			usePortsFigs.elementAt(i).setForegroundColor(ColorConstants.red);
			usePortsFigs.elementAt(i).setSize(anchorSize[0],anchorSize[1]);
			y1 = componentRectange.y+((int)(0.27*((double)componentRectange.height))) + (2 * i + 1) * height / 8;
			usePortsFigs.elementAt(i).setLocation(new Point(right,y1));
			usePortsFigs.elementAt(i).repaint();
			invalidateSourceConnectionAnchor(usePorts.elementAt(i),right,y1, height /8);

		}
	}
	/**
	 * draws boxes for the different connectors and creates
	 * @param g
	 * @param rec
	 */
	public void invalidateConnectors(Rectangle componentRectangle) {
		Rectangle portRectangle=componentRectangle.getCopy();
		if(this.getMaxConnectors()>0)
			portRectangle.height=portRectangle.height/(int)Math.ceil(((double)this.getMaxConnectors()/3.0));

		int y1 = componentRectangle.y,  
		height = portRectangle.height,x1=componentRectangle.x,
		right =componentRectangle.x+componentRectangle.width-(portRectangle.height/8)-1;


		for (int i = 0; i < providePorts.size(); i++) {
			y1 = componentRectangle.y+((int)(0.27*((double)componentRectangle.height))) + (2 * i + 1) * height / 8;
			invalidateTargetConnectionAnchor(providePorts.elementAt(i),x1,y1,height/8);
		}
		for (int i = 0; i < usePorts.size(); i++) {
			y1 = componentRectangle.y+((int)(0.27*((double)componentRectangle.height))) + (2 * i + 1) * height / 8;
			invalidateSourceConnectionAnchor(usePorts.elementAt(i),right,y1,height/8);

		}
	}


	private void invalidateSourceConnectionAnchor(Port port, int right, int y1, int width) {
//		double zoom=1.0/((ComponentFigure)parent).getZoom();
//		int w=(int)(((double)width*zoom)/2.0);
		this.outputConnectionAnchors.get(port).setLocation(right+width/2,y1+width/2);
	}

	private void invalidateTargetConnectionAnchor(Port port, int x1, int y1, int width) {
//		double zoom=1.0/((ComponentFigure)parent).getZoom();
//		int w=(int)(((double)width*zoom)/2.0);
		
		this.inputConnectionAnchors.get(port).setLocation(x1+width/2,y1+width/2);
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		return getClosestConnectionAnchor(p,this.inputConnectionAnchors);
	}
	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		return getClosestConnectionAnchor(p,this.outputConnectionAnchors);
	}

	/**
	 * @param p
	 * @return
	 */
	private ConnectionAnchor getClosestConnectionAnchor(Point p,HashMap<Port,PortAnchor> anchors) {		
		ConnectionAnchor closest = null;
		if (!isLink){
			Double min = Double.MAX_VALUE;
			for(ConnectionAnchor anchor:anchors.values()){
				Point p2 = anchor.getLocation(null);
				double d = p.getDistance(p2);
				if (d < min) {
					min = d;
					closest = anchor;
				}
			}
		}

		return closest;
	}


	public void paint(IFigure figure, Graphics g, Insets in) {
		
		Rectangle compoentRectangle=figure.getBounds().getCopy();
		Rectangle portRectangle=figure.getBounds().getCopy();
		if(this.getMaxConnectors()>0)
			portRectangle.height=portRectangle.height/(int)Math.ceil(((double)this.getMaxConnectors()/3.0));
		portRectangle.width=portRectangle.height;
		drawConnectors(g,portRectangle,compoentRectangle);
		
		//super.paint(figure, g, in);
	}

	public ConnectionAnchor getConnectionAnchor(Port port) {
		ConnectionAnchor anchor=null;
		if((anchor=inputConnectionAnchors.get(port))!=null)
			return anchor;
		if((anchor=outputConnectionAnchors.get(port))!=null)
			return anchor;
		return anchor;
	}

	public Port getModelForAnchor(PortAnchor ctor) {
		Port port=null;
		if((port=inputConnectionPorts.get(ctor))!=null)
			return port;
		if((port=outputConnectionPorts.get(ctor))!=null)
			return port;
		return port;
	}

	
	public void unmarkCompatibleTargets(){
		matchingPort=null;
	}
	public void markCompatibleTargets(Port port) {
		if(matchingPort==null)
			matchingPort=port;

	}

	private boolean isLink = false;// true if connection from component is a link to a sticky note

	public void setLink(boolean isLink) {
		this.isLink = isLink;
	}

}