package de.tum.ascodt.plugin.ui.gef.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;

import de.tum.ascodt.plugin.ui.gef.model.StickyNote;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

public class StickyNoteCreateCommand extends Command 
{

	/** The new note. */ 
	private StickyNote newNote;
	/** Diagram to add to. */
	private final Diagram _parent;
	/** The bounds of the new sticky note. */
	private Rectangle bounds;
    /**
     * creation Request 
     */
	private CreateRequest request;
	/**
	 * Create a command that will add a new Note to a Diagram.
	 * @param request the creation request
	 * @param parent the Diagram that will hold the new element
	 * @param bounds the bounds of the new note; the size can be (-1, -1) if not known
	 * @throws IllegalArgumentException if any parameter is null, or the request
	 * 						  does not provide a new Shape instance
	 */
	public StickyNoteCreateCommand(CreateRequest request, Diagram parent, Rectangle bounds) {
		this.request = request;
		this._parent = parent;
		this.bounds = bounds;
		setLabel("note creation");
	}

	/**
	 * Can execute if all the necessary information has been provided. 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		return request != null && _parent != null && bounds != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		newNote=(StickyNote) request.getNewObject();
		newNote.setLocation(bounds.getLocation());
		Dimension size = bounds.getSize();
		if (size.width > 0 && size.height > 0)
			newNote.setSize(size);
		redo();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		_parent.addChild(newNote);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		_parent.removeChild(newNote);
	}

}
