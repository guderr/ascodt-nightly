package de.tum.ascodt.plugin.ui.gef.figures;

import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import de.tum.ascodt.plugin.ASCoDTKernel;
import de.tum.ascodt.plugin.ui.gef.model.Port;



/**
 * Creates a box with header and buttons to open UI and Viz
 * @author atanasoa
 *
 */
public class ComponentFigure extends Figure{
	private ImageFigure openGUIImage;
	private ImageFigure closeGUIImage;
	private EditableLabel classLabel;
	private ComponentBorder componentBorder;
	
	/**
	 * figure validity flag
	 */
	private boolean valid;
	private double zoom=1.0;
	private boolean isRemote;
	private Vector<RectangleFigure> input;

	private Vector<RectangleFigure> output;
	


	

	public ComponentFigure(boolean hasGUI,boolean isRemote,String ref,String className,String iconPath,
			Vector<Port> usePorts,Vector<Port> providePorts){
		setBackgroundColor(ColorConstants.tooltipBackground);
		setForegroundColor(ColorConstants.tooltipForeground);
		input=new Vector<RectangleFigure>();
		output=new Vector<RectangleFigure>();
		for(int i=0;i<providePorts.size();i++){
			RectangleFigure rf=new RectangleFigure();
			Label l=new Label(providePorts.elementAt(i).getDescription());
			rf.setToolTip(l);
			input.add(rf);
			add(rf);
		}
		for(int i=0;i<usePorts.size();i++){
			RectangleFigure rf=new RectangleFigure();
			Label l=new Label(usePorts.elementAt(i).getDescription());
			rf.setToolTip(l);
			output.add(rf);
			add(rf);
		}
		
		componentBorder=new ComponentBorder(isRemote,this,usePorts,providePorts,input,output);
		this.setBorder(componentBorder);
		openGUIImage=new ImageFigure( ImageDescriptor.createFromFile(ASCoDTKernel.class, "./ui/resources/openGUI.png").createImage());
		closeGUIImage=new ImageFigure( ImageDescriptor.createFromFile(ASCoDTKernel.class, "./ui/resources/closeGUI.png").createImage());
		classLabel=new EditableLabel(ref+":"+className);
		this.isRemote=isRemote;
		setLayoutManager(new FreeformLayout());
		add(classLabel);
		if(hasGUI){
			add(openGUIImage);
			add(closeGUIImage);
			closeGUIImage.setVisible(false);
			
		}
		
		
	}

	

	public void paintFigure(Graphics graphics) {
		graphics.setLineWidth(1);
		graphics.setLineStyle(Graphics.LINE_SOLID);
		graphics.setXORMode(false);
		if(isRemote){
			

			Rectangle rect = this.getBounds();

			int top = rect.y;
			int left = rect.x;
			int bottom = rect.bottom() - 1;
			int right = rect.right() - 1;
			Color color;
			Color []br=new Color[]{
					new Color(Display.getDefault(),0,0,0),
					new Color(Display.getDefault(),0,0,0),
					new Color(Display.getDefault(),0,0,0),
					new Color(Display.getDefault(),0,0,0)
			};
			for (int i = 0; i < br.length; i++) {
				color = br[i];
				graphics.setForegroundColor(color);
				graphics.drawLine(right - i, bottom - i, right - i, top + 4);
				graphics.drawLine(right - i, bottom - i, left + 8, bottom - i);
			}
		}
		Color color=new Color(Display.getDefault(),0,0,0);
		Rectangle rect = this.getBounds();
			graphics.setForegroundColor(color);
			graphics.drawLine(rect.right()-4, rect.bottom()-4, rect.right()-4,rect.y);
			graphics.drawLine(rect.x+4, rect.bottom()-4, rect.x+4,rect.y);
			graphics.drawLine(rect.x+4, rect.bottom()-4, rect.right()-4,rect.bottom()-4);
			graphics.drawLine(rect.x+4, rect.y, rect.right()-4,rect.y);
			
		if(valid)
			graphics.setBackgroundColor( ColorConstants.lightBlue);
		else
			graphics.setBackgroundColor( ColorConstants.red);
		Rectangle rec=new Rectangle();
		rec.setLocation( getBounds().getLocation().x+4,getBounds().getLocation().y);
		rec.setSize(new Dimension(getBounds().getSize().width-8,(int)(0.27*(double)getBounds().getSize().height)));
		graphics.fillRectangle( rec.getShrinked( new Insets(1,1,1,1) ) );

		Rectangle rec2=new Rectangle();
		rec2.setLocation( getBounds().getLocation().x+4,getBounds().getLocation().y);
		rec2.setSize(new Dimension(getBounds().getSize().width-8,(int)(double)getBounds().getSize().height-4));
		graphics.drawRectangle( rec2.getShrinked( new Insets(1,1,1,1) ) );

		classLabel.setLocation(new Point((int)(this.getLocation().x+0.25*(double)this.getSize().width)
				,(int)(this.getLocation().y+0.05*(double)this.getSize().height)));

		classLabel.setSize((int)(0.50*(double)this.getSize().width),(int)((double)this.getSize().height*0.15));//new Dimension((int)((double)getBounds().getSize().width/2.0),(int)(0.25*(double)getBounds().getSize().height)));
		openGUIImage.setLocation(
				new Point((int)(this.getLocation().x+0.75*(double)this.getSize().width),
				(int)(this.getLocation().y+0.05*(double)this.getSize().height)));
		openGUIImage.setSize((int)(0.10*(double)this.getSize().width),(int)((double)this.getSize().height*0.15));//new Dimension((int)((double)getBounds().getSize().width/2.0),(int)(0.25*(double)getBounds().getSize().height)));
		closeGUIImage.setLocation(new Point((int)(this.getLocation().x+0.75*(double)this.getSize().width),(int)(this.getLocation().y+0.05*(double)this.getSize().height)));
		closeGUIImage.setSize((int)(0.10*(double)this.getSize().width),(int)((double)this.getSize().height*0.15));//new Dimension((int)((double)getBounds().getSize().width/2.0),(int)(0.25*(double)getBounds().getSize().height)));
		
		graphics.setBackgroundColor( ColorConstants.lightBlue);
		
		super.paintFigure(graphics);
		
	}
	public Label getComponentLabel() {
		return classLabel;

	}
	public void validate() {
		if(isValid()) return;
		componentBorder.invalidateConnectors(this.getBounds().getCopy());
		super.validate();
	}

	private void setEnabled(boolean enabled,Figure enabledFigure,Figure disabledFigure){
		if(enabled){
			enabledFigure.setVisible(false);
			disabledFigure.setVisible(true);
		}else{
			disabledFigure.setVisible(false);
			enabledFigure.setVisible(true);
		}
		this.invalidate();
	}

	public void setUIEnabled(boolean enabled){
		setEnabled(enabled,openGUIImage,closeGUIImage);
	}

	
	
	/**
	 * @param location
	 * @return
	 */
	private int hitTest(Point location,Figure enabledFigure,Figure disabledFigure) {
		if(enabledFigure!=null&&disabledFigure!=null){
			if(enabledFigure.isVisible()){
				if(hitTestForObject(enabledFigure,location))
					return 1;
			}else if(disabledFigure.isVisible()){
				if(hitTestForObject(disabledFigure,location))
					return 2;
			}
		}
		return 0;
	}

	/**
	 * hit test for viz buttons
	 * 
	 * @param location
	 * @return 0 not hit, 1 enable viz button, 2 disable viz button 
	 */
	public int setUIEnabledHitTest(Point location) {
		return hitTest(location,openGUIImage,closeGUIImage);
	}

	/**
	 * hit test for figures
	 * @param location
	 */
	private boolean hitTestForObject(Figure figure,Point location) {
		Rectangle rc;
		rc = new PrecisionRectangle(figure.getBounds());
		figure.translateToAbsolute(rc);
		return  (rc.contains(location));
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point pt) {
		return componentBorder.getTargetConnectionAnchorAt(pt);
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point pt) {
		return componentBorder.getSourceConnectionAnchorAt(pt);
	}

	public ConnectionAnchor getConnectionAnchor(Port port) {
		return componentBorder.getConnectionAnchor(port);
	}

	public Port getModelForAnchor(PortAnchor ctor) {
		return componentBorder.getModelForAnchor(ctor);
	}

	/**
	 * mark ports which are compatible with the source port
	 * @param port
	 */
	public void markCompatibleTargets(Port port) {
		componentBorder.markCompatibleTargets(port);
		this.invalidate();
		this.repaint();
	}




	public void unmarkCompatibleTargets() {
		componentBorder.unmarkCompatibleTargets();
		this.invalidate();
		this.repaint();
	}


	public void setLink(boolean isLink) {
		componentBorder.setLink(isLink);
	}




	public void setCCAValid(boolean valid) {
		this.valid=valid;
	}




	public void setZoom(double newZoom) {
		zoom=newZoom;
	}
	/**
	 * @return the zoom
	 */
	public double getZoom() {
		return zoom;
	}

}
