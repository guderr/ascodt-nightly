package de.tum.ascodt.plugin.ui.gef.commands;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import de.tum.ascodt.plugin.ui.gef.model.Diagram;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;

public class StickyNoteDeleteCommand extends Command {
		
	/** Diagram to remove from. */
	private final Diagram parent;
	/** Note to remove. */
	private final StickyNote child;
	/** Holds a copy of the incoming links of child. */
	private List<Link> targetLinks;

	/** True, if child was removed from its parent. */
	private boolean wasRemoved;

	/**
	 * Create a command that will remove the shape from its parent.
	 * @param parent the Diagram containing the child
	 * @param child    the Note to remove
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public StickyNoteDeleteCommand(Diagram parent, StickyNote child) {
		if (parent == null) {
			throw new IllegalArgumentException();
		}
		setLabel("note deletion");
		this.parent = parent;		
		this.child = child;
	}

	
	/**
	 * Reconnects a List of Links with their previous endpoints.
	 * @param links a non-null List of links
	 */
	private void addLinks(List<Link> links) {
		for (Iterator<Link> iter = links.iterator(); iter.hasNext();) {
			Link link = iter.next();
			link.reconnect();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	public boolean canUndo() {
		return wasRemoved;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// store a copy of incoming & outgoing connections before proceeding 
		targetLinks = child.getTargetLinks();
		redo();		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		// remove the child and disconnect its connections
		wasRemoved = parent.removeChild(child);
		if (wasRemoved) {
			removeLinks(targetLinks);
		}
	}
	
	/**
	 * Disconnects a List of Links from their endpoints.
	 * @param links a non-null List of links
	 */
	private void removeLinks(List<Link> links) {
		for (Iterator<Link> iter = links.iterator(); iter.hasNext();) {
			Link link =  iter.next();
			link.targetRemoved();
			//link.disconnect();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		// add the child and reconnect its connections
		if (parent.addChild(child)) {
			addLinks(targetLinks);
		}
	}
	}