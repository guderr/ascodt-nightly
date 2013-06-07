package de.tum.ascodt.plugin.ui.gef.editparts.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import de.tum.ascodt.plugin.ui.gef.commands.ComponentDeleteCommand;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

/**
 * component edit policy: For delete commands
 * @author atanasoa
 *
 */
public class ComponentEditPolicy extends org.eclipse.gef.editpolicies.ComponentEditPolicy {
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		Object childModel=getHost().getModel();
		Object parentModel=getHost().getParent().getModel();
		if (childModel instanceof Component && parentModel instanceof Diagram) {
			ComponentDeleteCommand cmd=new ComponentDeleteCommand();
			cmd.setParent((Diagram) parentModel);
			cmd.setChild((Component) childModel);
			return cmd;
		}
		return super.createDeleteCommand(deleteRequest);
	}
	

}
