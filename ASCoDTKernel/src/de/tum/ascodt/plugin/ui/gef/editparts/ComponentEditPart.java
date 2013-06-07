package de.tum.ascodt.plugin.ui.gef.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import de.tum.ascodt.plugin.ui.gef.directedit.LabelCellEditorLocator;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.ComponentConnectionPolicy;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.ComponentDirectEditPolicy;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.ComponentEditPolicy;
import de.tum.ascodt.plugin.ui.gef.figures.ComponentFigure;
import de.tum.ascodt.plugin.ui.gef.figures.EditableLabel;
import de.tum.ascodt.plugin.ui.gef.figures.PortAnchor;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Connection;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.ModelElement;
import de.tum.ascodt.plugin.ui.gef.model.Port;

/**
 * This class acts as controller for the component representation.
 * @author atanasoa
 *
 */
public class ComponentEditPart extends AbstractGraphicalEditPart implements NodeEditPart, PropertyChangeListener{
	protected DirectEditManager manager;
	private double cachedZoom = -1.0;
	private ZoomListener zoomListener = new ZoomListener() {
		public void zoomChanged(double newZoom) {
			updateScaleFactor(newZoom);
		}
	};

	
	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
			ZoomManager zoomMgr = (ZoomManager)getViewer()
			.getProperty(ZoomManager.class.toString());
			if (zoomMgr != null) {
				// this will force the font to be set
				cachedZoom = -1.0;
				updateScaleFactor(zoomMgr.getZoom());
				zoomMgr.addZoomListener(zoomListener);
			}
		}
	}

	protected void updateScaleFactor(double newZoom) {
		if (cachedZoom == newZoom)
			return;
		else{
			((ComponentFigure)this.getFigure()).setZoom(newZoom);
			cachedZoom = newZoom;
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getCastedModel().removePropertyChangeListener(this);
			ZoomManager zoomMgr = (ZoomManager)getViewer()
			.getProperty(ZoomManager.class.toString());
			if (zoomMgr != null)
				zoomMgr.removeZoomListener(zoomListener);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		String prop = evt.getPropertyName();
		if (Component.SIZE_PROP.equals(prop) || Component.LOCATION_PROP.equals(prop)){ 
			refreshVisuals();
		}else if (Component.REFERENCE_PROP.equals(prop)){
			commitNameChange(evt);
		}else if (Component.GUI_PROP.equals(prop)){
			commitSetUIEnabled(evt);
		}else if (Component.INPUT_CONNECTIONS.equals(prop))
			refreshTargetConnections();
		else if (Component.OUTPUT_CONNECTIONS.equals(prop)){
			refreshSourceConnections();
		}else if (Component.LINKS_PROP.equals(prop))			
			refreshSourceConnections();
	}

	public void performRequest(Request request)
	{		
		if (request.getType().equals(RequestConstants.REQ_DIRECT_EDIT)){
			if (request instanceof DirectEditRequest
					&& !directEditHitTest(((DirectEditRequest) request).getLocation().getCopy()))
				return;
			performDirectEdit();
		}else if (request.getType().equals(RequestConstants.REQ_OPEN)){
			this.getViewer().getEditDomain().getCommandStack().execute(this.getEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE).getCommand(request));

		}else if(request.getType().equals(RequestConstants.REQ_CONNECTION_START)){

		}

	}

	@Override
	public void showSourceFeedback(Request request){
		if(request instanceof CreateConnectionRequest){
			ConnectionAnchor ctor =getSourceConnectionAnchor(request);
			if(ctor!=null&&ctor instanceof PortAnchor)
				((DiagramEditPart)this.getParent()).markCompatibleTargets(((ComponentFigure)getFigure()).getModelForAnchor((PortAnchor)ctor),this);
			//((ASCoDTDiagramEditPart)this.getParent()).get
		}
		super.showSourceFeedback(request);
	}

	@Override
	public void eraseSourceFeedback(Request request){
		((DiagramEditPart)this.getParent()).unmarkCompatibleTargets(request);
		super.eraseSourceFeedback(request);
	}

	/**
	 * Returns a list of connections for which this is the 
	 * source.
	 *
	 * @return List of connections.
	 */
	protected List<?> getModelSourceConnections(){
		Vector<ModelElement> sourceConections = new Vector<ModelElement>();
		sourceConections.addAll(this.getCastedModel().getSourceConnections());
		sourceConections.addAll(this.getCastedModel().getSourceLinks());
		return sourceConections;
	}

	/**
	 * Returns a list of connections for which this is the 
	 * target.
	 *
	 * @return  List of connections.
	 */
	protected List<?> getModelTargetConnections(){
		return this.getCastedModel().getTargetConnections();
	}


	

	/**
	 * Determines if the ui buttons are pressed
	 * @param location interatcion location
	 * @return
	 */
	public int setUIEnabledHitTest(Point location) {
		ComponentFigure componentFigure=(ComponentFigure)getFigure();
		return componentFigure.setUIEnabledHitTest(location);
	}

	private boolean directEditHitTest(Point requestLoc){
		EditableLabel figure = getComponentLabel();
		figure.translateToRelative(requestLoc);
		if (figure.containsPoint(requestLoc))
			return true;
		return false;
	}

	protected void performDirectEdit(){
		if (manager == null){


			Label l = getComponentLabel();
			manager = new ExtendedDirectEditManager(this, TextCellEditor.class, new LabelCellEditorLocator(l), l);
		}
		manager.show();
	}

	@Override
	protected IFigure createFigure() {
		return new ComponentFigure(this.getCastedModel().hasGUI(),
				this.getCastedModel().isRemote(),this.getCastedModel().getReference(),this.getCastedModel().getComponentName(),null,
				this.getCastedModel().getUsePorts(),this.getCastedModel().getProvidePorts());
	}

	/**
	 * policies
	 * 1. delete
	 * 2. direct edit for reference id
	 * 3. open for gui open
	 */
	@Override
	protected void createEditPolicies() {
		//
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy());

		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ComponentDirectEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,new ComponentConnectionPolicy());
	}

	protected void refreshVisuals() {
		// notify parent container of changed position & location
		// if this line is removed, the XYLayoutManager used by the parent container 
		// (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
		// and will not draw it correctly.
		((ComponentFigure)getFigure()).setCCAValid(this.getCastedModel().isValid());
		((ComponentFigure)getFigure()).repaint();
		Rectangle bounds = new Rectangle(getCastedModel().getLocation(),
				getCastedModel().getSize());
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
		super.refreshVisuals();
	}

	private Component getCastedModel() {

		return (Component)getModel();
	}

	/**
	 * Handler for the change of referenceid
	 * @param value
	 */
	public void handleReferenceChange(String value) {
		EditableLabel label = getComponentLabel();
		label.setVisible(false);
		setSelected(EditPart.SELECTED_NONE);
		label.revalidate();
	}

	/**
	 * @return
	 */
	private EditableLabel getComponentLabel() {
		ComponentFigure componentFigure = (ComponentFigure) getFigure();
		EditableLabel label = (EditableLabel) componentFigure.getComponentLabel();
		return label;
	}

	/**
	 * Sets the width of the line when selected
	 */
	public void setSelected(int value)
	{
		super.setSelected(value);
		EditableLabel componentLabel = getComponentLabel();
		if (value != EditPart.SELECTED_NONE)
			componentLabel.setSelected(true);
		else
			componentLabel.setSelected(false);
		componentLabel.repaint();
	}

	public void revertReferenceChange(String oldValue) {
		EditableLabel componentLabel = getComponentLabel();
		componentLabel.setVisible(true);
		setSelected(EditPart.SELECTED_PRIMARY);
		componentLabel.revalidate();
	}

	/**
	 * Handles when successfully applying direct edit
	 */
	private void commitNameChange(PropertyChangeEvent evt)
	{
		EditableLabel componentLabel = getComponentLabel();
		componentLabel.setText(this.getCastedModel().getReference()+":"+this.getCastedModel().getComponentName());
		setSelected(EditPart.SELECTED_PRIMARY);
		componentLabel.revalidate();
	}

	
	private void commitSetUIEnabled(PropertyChangeEvent evt) {
		ComponentFigure figure= (ComponentFigure)getFigure();
		figure.setUIEnabled(this.getCastedModel().getUIEnabled());
	}

	/**
	 * Returns the connection anchor for the given 
	 * ConnectionEditPart's target.
	 *
	 * @return  ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
		Connection conn = (Connection) connEditPart.getModel();
		return getComponentFigure().getConnectionAnchor(conn.getTargetPort());
	}

	/**
	 * Returns the connection anchor of a terget connection which
	 * is at the given point.
	 *
	 * @return  ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest)request).getLocation());
		return getComponentFigure().getTargetConnectionAnchorAt(pt);
	}

	/**
	 * Returns the connection anchor for the given
	 * ConnectionEditPart's source. 
	 *
	 * @return  ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(final ConnectionEditPart connEditPart) {
		if (connEditPart instanceof LinkEditPart){
			getComponentFigure().setLink(true);
			return  new ChopboxAnchor(getComponentFigure());
		}else{
			getComponentFigure().setLink(false);
			Connection conn = (Connection) connEditPart.getModel();
			return  getComponentFigure().getConnectionAnchor(conn.getSourcePort());
		}
	}

	/**
	 * Returns the connection anchor of a source connection which
	 * is at the given point.
	 * 
	 * @return  ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if ((request instanceof CreateConnectionRequest) &&
				(((CreateConnectionRequest)request)).getNewObjectType().
				equals(Link.DASH_CONNECTION)){
			getComponentFigure().setLink(true);
			return  new ChopboxAnchor(getComponentFigure());
		}
		else{
			getComponentFigure().setLink(false);
			Point pt = new Point(((DropRequest)request).getLocation());
			return getComponentFigure().getSourceConnectionAnchorAt(pt);
		}
	}

	/**
	 * @return
	 */
	private ComponentFigure getComponentFigure() {
		return ((ComponentFigure)getFigure());
	}

	public void markCompatibleTargets(Port port) {
		((ComponentFigure)getFigure()).markCompatibleTargets(port);
	}

	public void unmarkCompatibleTargets() {
		((ComponentFigure)getFigure()).unmarkCompatibleTargets();
	}

}
