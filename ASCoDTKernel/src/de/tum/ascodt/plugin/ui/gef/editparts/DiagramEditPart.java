package de.tum.ascodt.plugin.ui.gef.editparts;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;
import de.tum.ascodt.plugin.ui.gef.model.ModelElement;
import de.tum.ascodt.plugin.ui.gef.model.Port;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.XYLayouetEditPolicy;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

/**
 * EditPart for the a ASCoDTDiagramEditPart instance.
 * <p>This edit part server as the main diagram container, the white area where
 * everything else is in. Also responsible for the container's layout (the
 * way the container rearanges is contents) and the container's capabilities
 * (edit policies).
 * </p>
 * <p>This edit part must implement the PropertyChangeListener interface, 
 * so it can be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @autho atanasoa
 */
public class DiagramEditPart extends AbstractGraphicalEditPart 
implements PropertyChangeListener  {

	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);

		}
	}
	
	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getCastedModel().removePropertyChangeListener(this);
		}
	}
	
	/** (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List<?> getModelChildren() {
		return getCastedModel().getChildren(); 
	}
	
	/** (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		// these properties are fired when Shapes are added into or removed from 
		// the ASCoDTDiagram instance and must cause a call of refreshChildren()
		// to update the diagram's contents.
		if (Diagram.CHILD_ADDED_PROP.equals(prop)
				|| Diagram.CHILD_REMOVED_PROP.equals(prop)) {
			refreshChildren();
		}
	}
	
	/**
	 * creates the view for this editpart
	 * @see 
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());
		
		//f.setForegroundColor(ColorConstants.white);
		//f.setBackgroundColor(ColorConstants.white);
		// Create the static router for the connection layer
		ConnectionLayer connLayer = (ConnectionLayer)getLayer(LayerConstants.CONNECTION_LAYER);
		AutomaticRouter router = new FanRouter();
		router.setNextRouter(new ManhattanConnectionRouter());
		connLayer.setConnectionRouter(router);
		f.setVisible(true);
		return f;
	}

	@Override
	protected void createEditPolicies() {
		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayouetEditPolicy());
		
		//prevents the root part from providing selection feedback when the user 
		
		//clicks on the area of the diagram corresponding to the root of the model
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		installEditPolicy("Snap Feedback", new SnapFeedbackPolicy());
	}
	
	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter == SnapToHelper.class) {
			List snapStrategies = new ArrayList();
			Boolean val = (Boolean)getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGuides(this));
			val = (Boolean)getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGeometry(this));
			val = (Boolean)getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGrid(this));
			
			if (snapStrategies.size() == 0)
				return null;
			if (snapStrategies.size() == 1)
				return snapStrategies.get(0);

			SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
			for (int i = 0; i < snapStrategies.size(); i++)
				ss[i] = (SnapToHelper)snapStrategies.get(i);
			return new CompoundSnapToHelper(ss);
		}
		return super.getAdapter(adapter);
	}

	public DragTracker getDragTracker(Request req){
		if (req instanceof SelectionRequest 
			&& ((SelectionRequest)req).getLastButtonPressed() == 3)
				return new DeselectAllTracker(this);
		return new MarqueeDragTracker();
	}
	
	/**
	 * 
	 * @return the model of the edit part
	 */
	private Diagram getCastedModel() {
		return (Diagram) getModel();
	}

	public void markCompatibleTargets(
			Port port,ComponentEditPart component) {
		for(Object part:this.getChildren())
			if(part instanceof ComponentEditPart && !part.equals(component))
				((ComponentEditPart) part).markCompatibleTargets(port);
		
	}

	public void unmarkCompatibleTargets(Request request) {
		for(Object part:this.getChildren())
			if(part instanceof ComponentEditPart )
				((ComponentEditPart) part).unmarkCompatibleTargets();	
	}

//	public CCAClasspathRepository getClasspathRepository() {
//		return this.getCastedModel().getEditor().getRepository().getClasspathRepository();
//	}
	
}
