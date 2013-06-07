package de.tum.ascodt.plugin.ui.gef.commands;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;


import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Connection;
import de.tum.ascodt.plugin.ui.gef.model.Port;

public class ConnectionCommand 	extends Command{
	protected Component oldSource;
	protected Port oldSourcePort;
	protected Component oldTarget;
	protected Port oldTargetPort;
	protected Component source;
	protected Port sourcePort;
	protected Component target; 
	protected Port targetPort; 
	protected Connection connection;
	public ConnectionCommand(){
		setLabel("connection creation");
	}
	public boolean canExecute(){
		if (target != null) {
			if(target.equals(source))
				return false;
			Vector<Connection> conns = target.getConnections();
			Iterator<Connection> i = conns.iterator();

			while (i.hasNext()) {
				Connection conn = i.next();
				if (targetPort != null && conn.getTargetPort() != null)
					if (conn.getTargetPort().equals(targetPort) 
							&& conn.getTarget().equals(target))
						return false;
			}
		}
		return true;
	}
	public void execute() {
		if (source != null){
			connection.detachSource();
			connection.setSource(source);
			connection.setSourcePort(sourcePort);
			connection.attachSource();
		}
		if (target != null) {
			connection.detachTarget();
			connection.setTarget(target);
			connection.setTargetPort(targetPort); 
			connection.attachTarget();
		}
		if (source == null && target == null){
			connection.detachSource();
			connection.detachTarget();
			connection.setTarget(null);
			connection.setSource(null);
		}
	}
	
	public void setTarget(Component componentModel) {
		target=componentModel;
	}

	public void setSource(Component componentModel) {
		source=componentModel;
	}

	public void setTargetPort(Port model) {
		targetPort=model;
	}

	public void setSourcePort(Port model) {
		sourcePort=model;

	}
	
	public Object getSourcePort() {
		return sourcePort;
	}
	
	public void setConnection(Connection conn) {
		connection = conn;
		oldSource = conn.getSource();
		oldTarget = conn.getTarget();
		oldSourcePort = conn.getSourcePort();
		oldTargetPort = conn.getTargetPort();	
	}

	public void undo() {
		source = connection.getSource();
		target = connection.getTarget();
		sourcePort = connection.getSourcePort();
		targetPort = connection.getTargetPort();

		connection.detachSource();
		connection.detachTarget();

		connection.setSource(oldSource);
		connection.setTarget(oldTarget);
		connection.setSourcePort(oldSourcePort);
		connection.setTargetPort(oldTargetPort);

		connection.attachSource();
		connection.attachTarget();
	}
	

}
