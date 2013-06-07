package de.tum.ascodt.plugin.ui.gef.editparts.policies;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import de.tum.ascodt.plugin.ui.gef.editparts.DiagramEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.StickyNoteEditPart;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;

public class StickyNoteFlowEditPolicy extends org.eclipse.gef.editpolicies.FlowLayoutEditPolicy
{

	/**
	 * Override to return the <code>Command</code> to perform an {@link
	 * RequestConstants#REQ_CLONE CLONE}. By default, <code>null</code> is
	 * returned.
	 * @param request the Clone Request
	 * @return A command to perform the Clone.
	 */


	public Command getCommand(Request r){
		if(r instanceof ChangeBoundsRequest){
			ChangeBoundsRequest re= new ChangeBoundsRequest();
			
			EditPart host=getHost();
			while(!(host instanceof StickyNoteEditPart))
				host=host.getParent();			
			re.setEditParts(host);
			re.setMoveDelta(((ChangeBoundsRequest) r).getMoveDelta());
			int x = 0, y = 0;
			if (host instanceof StickyNoteEditPart){
				re.setType(REQ_MOVE);
				x=((ChangeBoundsRequest) r).getLocation().x-((ChangeBoundsRequest) r).getLocation().x-((StickyNote)host.getModel()).getLocation().x;
				y=((ChangeBoundsRequest) r).getLocation().y-((ChangeBoundsRequest) r).getLocation().y-((StickyNote)host.getModel()).getLocation().y;			
			}
			re.setLocation(new Point(x,y));
			return ((DiagramEditPart)host.getParent()).getEditPolicy(EditPolicy.LAYOUT_ROLE).getCommand(re);
		}
		return null;
	}
	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	protected EditPolicy createChildEditPolicy(EditPart child) {

		StickyNoteResizableEditPolicy policy = new StickyNoteResizableEditPolicy();
		policy.setResizeDirections(0);
		return policy;
	}



	@Override
	protected Command createAddCommand(EditPart child, EditPart after) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected Command createMoveChildCommand(EditPart child, EditPart after) {
		// TODO Auto-generated method stub
		return null;
	}

}
