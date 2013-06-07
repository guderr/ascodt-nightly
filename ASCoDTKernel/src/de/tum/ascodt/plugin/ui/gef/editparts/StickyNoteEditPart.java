package de.tum.ascodt.plugin.ui.gef.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.tools.DirectEditManager;
import de.tum.ascodt.plugin.ui.gef.commands.ChangeTextCommand;
import de.tum.ascodt.plugin.ui.gef.commands.LinkCreateCommand;
import de.tum.ascodt.plugin.ui.gef.directedit.LabelCellEditorLocator;
import de.tum.ascodt.plugin.ui.gef.directedit.LabelDirectEditManager;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.StickyNoteComponentEditPolicy;
import de.tum.ascodt.plugin.ui.gef.editparts.policies.StickyNoteFlowEditPolicy;
import de.tum.ascodt.plugin.ui.gef.figures.StickyNoteFigure;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.ModelElement;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;

/**
 * The sticky note editpart controller
 * @author atanasoa
 *
 */
public class StickyNoteEditPart extends AbstractGraphicalEditPart 
implements PropertyChangeListener, NodeEditPart{

	protected DirectEditManager manager;

	private Dimension size;

	/**
	 * The connection anchor of the current port
	 */
	private ConnectionAnchor anchor;

	public StickyNoteEditPart(){		
	}	

	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);

		}
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart arg0) {
		return getConnectionAnchor();
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request arg0) {
		return getConnectionAnchor();
	}

	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			if (getModel() instanceof StickyNote)
				anchor = new ChopboxAnchor(getFigure());
			else
				throw new IllegalArgumentException("unexpected model");
		}		
		return anchor;
	}

	@Override
	protected IFigure createFigure() {
		return new StickyNoteFigure();
	}

	@Override
	protected void createEditPolicies() {
		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new StickyNoteComponentEditPolicy());
		// allow  moving of the associated model element
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new StickyNoteFlowEditPolicy());
		// allow editing of the associated model element		
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new DirectEditPolicy() {
			protected Command getDirectEditCommand(DirectEditRequest request) {				
				return new ChangeTextCommand((StickyNote)getModel(), 
						(String)request.getCellEditor().getValue());
			}

			protected void showCurrentEditValue(DirectEditRequest request) {
				((StickyNoteFigure)getFigure()).setText(
						(String)request.getCellEditor().getValue());
				getFigure().getUpdateManager().performUpdate();			
			}
		});
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new GraphicalNodeEditPolicy() {

			@Override
			protected Command getReconnectTargetCommand(ReconnectRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Command getReconnectSourceCommand(ReconnectRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override

			protected Command getConnectionCompleteCommand(
					CreateConnectionRequest request) {
				if (request.getStartCommand() instanceof LinkCreateCommand){
					LinkCreateCommand cmd = (LinkCreateCommand)request.getStartCommand();
					cmd.setTarget((StickyNote)getHost().getModel());
					return cmd;
				}
				return null;
			}
		});
	}

	private StickyNote getCastedModel() {
		return (StickyNote) getModel();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		String prop = evt.getPropertyName();
		//System.out.println("prop="+prop);
		if ((StickyNote.SIZE_PROP.equals(prop)) || (StickyNote.LOCATION_PROP.equals(prop))){			
			size = getCastedModel().getSize();
			refreshVisuals();
		}
		if (StickyNote.TEXT_PROP.equals(prop)) {
			size = ((StickyNoteFigure)getFigure()).getTextSize();
			size.height += 20;
			size.width += 20;
			refreshVisuals();
		}
		if (StickyNote.TARGET_LINKS_PROP.equals(prop)) {			
			refreshTargetConnections();
		}

	}

	@Override
	protected Vector<Link> getModelTargetConnections() {		
		return getCastedModel().getTargetLinks();		
	}

	protected void performDirectEdit() {
		if(manager == null)
			manager = new LabelDirectEditManager(this,
					new LabelCellEditorLocator(getDirectEditFigure()));
		manager.show();
	}

	public void performRequest(Request request){
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT){
			performDirectEdit();
			refreshVisuals();
			//getEditPolicy(EditPolicy.DIRECT_EDIT_ROLE).getCommand(request).execute();
		}
		else
			super.performRequest(request);
	}

	protected void refreshVisuals() {		
		if (size == null){
			size = getCastedModel().getSize();
		}
		Rectangle bounds = new Rectangle(getCastedModel().getLocation(), size);		
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
		((StickyNoteFigure)getFigure()).setText(getCastedModel().getText());
		((StickyNoteFigure)getFigure()).repaint();
		super.refreshVisuals();		
	}

	public IFigure getDirectEditFigure() {
		return getFigure();
	}

	public String getDirectEditText() {
		return getCastedModel().getText();
	}

}
