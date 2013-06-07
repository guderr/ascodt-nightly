package de.tum.ascodt.plugin.ui.gef.editparts.policies;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoutingAnimator;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.RequestConstants;

import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.requests.SelectionRequest;

import de.tum.ascodt.plugin.ui.gef.commands.ComponentUIInteractionCommand;
import de.tum.ascodt.plugin.ui.gef.commands.ConnectionCommand;
import de.tum.ascodt.plugin.ui.gef.commands.LinkCreateCommand;
import de.tum.ascodt.plugin.ui.gef.editparts.ComponentEditPart;
import de.tum.ascodt.plugin.ui.gef.figures.ComponentFigure;
import de.tum.ascodt.plugin.ui.gef.figures.PortAnchor;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Connection;
import de.tum.ascodt.plugin.ui.gef.model.Link;

/**
 * A policy specifying the possible connection setups
 * @author atanasoa
 *
 */
public class ComponentConnectionPolicy extends org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy {
	@Override
	protected org.eclipse.draw2d.Connection createDummyConnection(Request req) {
		PolylineConnection conn = createNewWire(null);
		return conn;
	}

	public static PolylineConnection createNewWire(Connection wire){

		PolylineConnection conn = new PolylineConnection();
		conn.addRoutingListener(RoutingAnimator.getDefault());
		PolygonDecoration arrow;

		if (wire == null )
			arrow = null;
		else {
			arrow = new PolygonDecoration();
			arrow.setTemplate(PolygonDecoration.INVERTED_TRIANGLE_TIP);
			arrow.setScale(5,2.5);
		}
		conn.setSourceDecoration(arrow);

		if (wire == null)
			arrow = null;
		else {
			arrow = new PolygonDecoration();
			arrow.setTemplate(PolygonDecoration.INVERTED_TRIANGLE_TIP);
			arrow.setScale(5,2.5);
		}
		conn.setTargetDecoration(arrow);
		return conn;
	}
	public Command getCommand(Request request) {	
		if (understandsRequest(request)) {
			Command cmd=null;
			ComponentEditPart componentEditPart=getComponentEditPart();
			int interactionMode=0;
			if((interactionMode=componentEditPart.setUIEnabledHitTest(((SelectionRequest) request).getLocation().getCopy()))>0){
				cmd=new ComponentUIInteractionCommand();
				((ComponentUIInteractionCommand)cmd).setSource((Component)componentEditPart.getModel());
				((ComponentUIInteractionCommand)cmd).setMode(interactionMode);
			}
			return cmd;
		}
		if(request.getType().equals(RequestConstants.REQ_RECONNECT_SOURCE)){
			System.out.println("start");
		}
		return super.getCommand(request);
	}
	public boolean understandsRequest(Request request) {
		if (request.getType().equals(RequestConstants.REQ_OPEN)) {
			return true;
		}

		return false;
	}

	@Override
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {
		if (request.getNewObjectType() == Link.DASH_CONNECTION){
			return null;
		}else{
			ConnectionCommand command = (ConnectionCommand)request.getStartCommand();
			command.setTarget(getComponentModel());
			ConnectionAnchor ctor = getComponentEditPart().getTargetConnectionAnchor(request);
			if (ctor == null){
				return command;

			}if(!(ctor instanceof PortAnchor))
				return null;
			if(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor).isConnectable()){
				command.setTargetPort(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor));
				return command;
			}
			return null;
		}		
	}
	/**
	 * @return
	 */
	private Component getComponentModel() {
		return (Component)getHost().getModel();		
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (request.getNewObjectType() == Link.DASH_CONNECTION){
			LinkCreateCommand cmd = new LinkCreateCommand(getComponentModel());
			request.setStartCommand(cmd);
			return cmd;	
		}else if (request.getNewObjectType() == Connection.SOLID_CONNECTION){
			ConnectionCommand command = new ConnectionCommand();
			command.setConnection(new Connection());
			command.setSource(getComponentModel());
			ConnectionAnchor ctor = getComponentEditPart().getSourceConnectionAnchor(request);
			if (ctor == null)
				return null;
			if(!(ctor instanceof PortAnchor))
				return null;
			command.setSourcePort(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor));
			request.setStartCommand(command);
			return command;
		}else
			return null;
	}
	/**
	 * @return
	 */
	private ComponentEditPart getComponentEditPart() {
		return ((ComponentEditPart)getHost());
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		ConnectionCommand cmd = new ConnectionCommand();
		cmd.setConnection((Connection)request.getConnectionEditPart().getModel());

		ConnectionAnchor ctor = getComponentEditPart().getTargetConnectionAnchor(request);
		if (ctor == null)
			return null;
		if(!(ctor instanceof PortAnchor))
			return null;
		if(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor).isConnectable()){
			cmd.setTarget(getComponentModel());
			cmd.setTargetPort(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor));
			return cmd;
		}
		return null;
	}
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		ConnectionCommand cmd = new ConnectionCommand();
		cmd.setConnection((Connection)request.getConnectionEditPart().getModel());

		ConnectionAnchor ctor = getComponentEditPart().getSourceConnectionAnchor(request);
		cmd.setSource(getComponentModel());
		cmd.setSourcePort(((ComponentFigure)getComponentEditPart().getFigure()).getModelForAnchor((PortAnchor)ctor));
		return cmd;		
	}

}


