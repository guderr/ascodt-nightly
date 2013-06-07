package de.tum.ascodt.plugin.ui.gef.editparts.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import de.tum.ascodt.plugin.ui.gef.commands.StickyNoteDeleteCommand;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

public class StickyNoteComponentEditPolicy extends ComponentEditPolicy {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		Object parent = getHost().getParent().getModel();
		Object child = getHost().getModel();
		if (parent instanceof Diagram && child instanceof StickyNote) {
			return new StickyNoteDeleteCommand((Diagram) parent, (StickyNote) child);
		}
		return super.createDeleteCommand(deleteRequest);
	}
	}
