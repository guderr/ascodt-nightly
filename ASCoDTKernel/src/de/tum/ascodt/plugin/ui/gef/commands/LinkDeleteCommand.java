package de.tum.ascodt.plugin.ui.gef.commands;

import org.eclipse.gef.commands.Command;
import de.tum.ascodt.plugin.ui.gef.model.Link;

public class LinkDeleteCommand extends Command{

	/** Connection instance to disconnect. */
	private final Link link;

	/** 
	 * Create a command that will disconnect a link from its endpoints.
	 * @param link the link instance to disconnect (non-null)
	 * @throws IllegalArgumentException if link is null
	 */ 
	public LinkDeleteCommand(Link link) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		setLabel("link deletion");
		this.link = link;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		link.disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		link.reconnect();
	}
}
