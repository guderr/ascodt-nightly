package de.tum.ascodt.plugin.ui.editors.gef;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

//import de.tum.ascodt.plugin.ui.tabs.ProgramArgsTab;;

/**
 * Provides context menu actions for the ShapesEditor.
 * 
 */
public class ASCoDTContextMenuProvider extends ContextMenuProvider {

	/** The editor's action registry. */
	private ActionRegistry actionRegistry;
	
	/**
	 * Instantiate a new menu context provider for the specified EditPartViewer 
	 * and ActionRegistry.
	 * @param viewer	the editor's graphical viewer
	 * @param registry	the editor's action registry
	 * @throws IllegalArgumentException if registry is <tt>null</tt>. 
	 */
	public ASCoDTContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		if (registry == null) {
			throw new IllegalArgumentException();
		}
		actionRegistry = registry;
		
		IAction properties=new Action("Properties"){
			public void run()
			{
				//ProgramArgsTab tab=new SocketClientAppsTab("Local application setup", AppsViewContainer.ID);
				//tab.setVisible(true);
			}
		};
		
		properties.setId(ActionFactory.PROPERTIES.getId());
		actionRegistry.registerAction(properties);
	}

	/**
	 * Called when the context menu is about to show. Actions, 
	 * whose state is enabled, will appear in the context menu.
	 * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		
		// Add actions to the menu
		menu.appendToGroup(
				GEFActionConstants.GROUP_UNDO, // target group id
				getAction(ActionFactory.UNDO.getId())); // action to add
		menu.appendToGroup(
				GEFActionConstants.GROUP_UNDO, // target group id
				getAction(ActionFactory.UNDO.getId()));
		menu.appendToGroup(
				GEFActionConstants.GROUP_UNDO, 
				getAction(ActionFactory.REDO.getId()));

		menu.appendToGroup(
				GEFActionConstants.GROUP_EDIT,
				getAction(ActionFactory.DELETE.getId()));			
		menu.appendToGroup(
				GEFActionConstants.GROUP_VIEW,
				getAction(ActionFactory.PROPERTIES.getId()));		

	}
	
	private IAction getAction(String actionId) {
		return actionRegistry.getAction(actionId);
	}
}

