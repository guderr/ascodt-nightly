/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *******************************************************************************/

package de.tum.ascodt.plugin.ui.gef.commands;



import java.util.Iterator;


import org.eclipse.draw2d.Graphics;
import org.eclipse.gef.commands.Command;

import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;


/**
 * A command to create a connection between two shapes.
 * The command can be undone or redone.
 * <p>
 * This command is designed to be used together with a GraphicalNodeEditPolicy.
 * To use this command properly, following steps are necessary:
 * </p>
 * <ol>
 * <li>Create a subclass of GraphicalNodeEditPolicy.</li>
 * <li>Override the <tt>getConnectionCreateCommand(...)</tt> method, 
 * to create a new instance of this class and put it into the CreateConnectionRequest.</li>
 * <li>Override the <tt>getConnectionCompleteCommand(...)</tt>  method,
 * to obtain the Command from the ConnectionRequest, call setTarget(...) to set the
 * target endpoint of the connection and return this command instance.</li>
 * </ol>
 * @see org.eclipse.gef.examples.shapes.parts.ShapeEditPart#createEditPolicies() for an
 * 			 example of the above procedure.
 * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy
 * 
 */
public class LinkCreateCommand extends Command {
	/** The connection instance. */
	private Link link;
	
	/** Start endpoint for the connection. */
	private Component source;
	/** Target endpoint for the connection. */
	private StickyNote target;

	/**
	 *	Instantiate a command that can create a connection between two shapes.
	 * @param source the source endpoint (a non-null Shape instance)
	 * @throws IllegalArgumentException if source is null
	 * @see Link#setLineStyle(int)
	 */
	public LinkCreateCommand(Component source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		setLabel("link creation");

		this.source = source;		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		// disallow source -> source links
		if (source.equals(target)) {
			return false;
		}
		// return false, if the source -> target link exists already
		for (Iterator<Link> iter = source.getSourceLinks().iterator(); iter.hasNext();) {
			Link link = iter.next();
			if (link.getTarget().equals(target)) {
				return false;
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		// create a new link between source and target
		link = new Link(source, target);
		// use the supplied line style
		link.setLineStyle(Graphics.LINE_DASH);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		link.reconnect();
	}

	/**
	 * Set the target endpoint for the link.
	 * @param target that target endpoint (a non-null Shape instance)
	 * @throws IllegalArgumentException if target is null
	 */
	public void setTarget(StickyNote target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		link.disconnect();
	}
}
