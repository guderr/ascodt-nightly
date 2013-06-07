package de.tum.ascodt.plugin.ui.gef.commands;


import java.util.Vector;
import org.eclipse.gef.commands.Command;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Connection;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

public class ComponentDeleteCommand extends Command {
	private static final String COMPONENT_DELETION = "component deletion";
	private Diagram parent;
	private Component child;
	private Vector<Connection> sourceConnections = new Vector<Connection> ();
	private Vector<Connection>  targetConnections = new Vector<Connection> ();
	private Vector<Link> sourceLinks = new Vector<Link>();
	/**
	 * Create a command that will remove the component from its parent.
	 * @param parent the ASCoDTDiagram containing the child
	 * @param child    the Shape to remove
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public ComponentDeleteCommand() {
		setLabel(COMPONENT_DELETION);
		this.parent = null;
		this.child = null;
	}
	
	public void setChild (Component comp) {
		child = comp;
	}

	public void setParent(Diagram diagram) {
		parent = diagram;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		redo();
	}
	
	private void deleteConnections(Component part) {
		sourceConnections.addAll(part.getSourceConnections());
		for (int i = 0; i < sourceConnections.size(); i++) {
			Connection conn = sourceConnections.get(i);
			conn.detachSource();
			conn.detachTarget();
		}
		sourceConnections.clear();
		targetConnections.addAll(part.getTargetConnections());
		for (int i = 0; i < targetConnections.size(); i++) {
			Connection conn = targetConnections.get(i);
			conn.detachSource();
			conn.detachTarget();
		}
		targetConnections.clear();
		sourceLinks.addAll(part.getSourceLinks());
		for (int i = 0; i < sourceLinks.size(); i++){
			Link link = sourceLinks.get(i);
			link.disconnect();
		}
		sourceLinks.clear();
	}
	
	public void redo() {
		deleteConnections(child);
		parent.removeChild(child);
		child.destroy();
		child.setCCAComponent(null);
		System.gc();
	}
	
	public void undo() {
		parent.addChild(child);
		restoreConnections();
		
	}
	
	private void restoreConnections() {
		for (int i = 0; i < sourceConnections.size(); i++) {
			Connection conn = sourceConnections.get(i);
			conn.attachSource();
			conn.attachTarget();
		}
		sourceConnections.clear();
		for (int i = 0; i < targetConnections.size(); i++) {
			Connection conn = targetConnections.get(i);
			conn.attachSource();
			conn.attachTarget();
		}
		targetConnections.clear();
	}
}
