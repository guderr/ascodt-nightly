package de.tum.ascodt.plugin.ui.gef.editparts.policies;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import de.tum.ascodt.plugin.ui.gef.commands.ComponentSetConstraintCommand;
import de.tum.ascodt.plugin.ui.gef.commands.CreateComponentCommand;
import de.tum.ascodt.plugin.ui.gef.commands.StickyNoteCreateCommand;
import de.tum.ascodt.plugin.ui.gef.commands.StickyNoteSetConstraintCommand;
import de.tum.ascodt.plugin.ui.gef.editparts.ComponentEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.StickyNoteEditPart;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

/**
 * EditPolicy for the Figure used by this edit part.
 * Children of XYLayoutEditPolicy can be used in Figures with XYLayout.
 * @author atanasoa
 */
public class XYLayouetEditPolicy extends XYLayoutEditPolicy{
	/* (non-Javadoc)
	 * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(ChangeBoundsRequest, EditPart, Object)
	 */
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
			EditPart child, Object constraint) {
		if (child instanceof ComponentEditPart && constraint instanceof Rectangle) {
			// return a command that can move and/or resize a Component
			return new ComponentSetConstraintCommand(
					(Component) child.getModel(), request, (Rectangle) constraint);
		}else if (child instanceof StickyNoteEditPart && constraint instanceof Rectangle) {
			// return a command that can move and/or resize a StickyNote
			return new StickyNoteSetConstraintCommand(
					(StickyNote) child.getModel(), request, (Rectangle) constraint);
		}
		return super.createChangeConstraintCommand(request, child, constraint);
	}
	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		return null;
	}
	
	/**
	 * creates a command for the creation of components or sticky notes
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object childClass = request.getNewObjectType();
		Command command=null;
		if (childClass == StickyNote.class){
			command=new StickyNoteCreateCommand(request,(Diagram)getHost().getModel(), 
					(Rectangle)getConstraintFor(request));
			return command;
		}		
		if (childClass == Component.class) {
			command=new CreateComponentCommand(request, 
					(Diagram)getHost().getModel(), (Rectangle)getConstraintFor(request));
			return command;
		}
		return null;
	}

}
