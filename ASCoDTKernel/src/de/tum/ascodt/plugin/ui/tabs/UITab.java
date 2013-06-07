package de.tum.ascodt.plugin.ui.tabs;

import de.tum.ascodt.plugin.ui.views.UIViewContainer;




/**
 * To use the ASCoDT_UIService one should implement a subclass of the tab.
 * Each instance corresponds to new tab int ASCoDT-UIView. To fill the tab 
 * with content the 
 * @author Atanas Atanasov
 *
 */
public abstract class UITab extends ContainerTab {

	/**
	 * holds an instance of the component
	 */
	protected de.tum.ascodt.repository.entities.Component _component;
	/**
	 * Constructor
	 * @param implementation reference of the component implementation
	 */
	public UITab(de.tum.ascodt.repository.entities.Component implementation) {
		super(":"+implementation.getClass().getCanonicalName(),UIViewContainer.ID);
		_component=implementation;
	}
	
	/* (non-Javadoc)
	 * @see de.tum.ascodt.plugin.ui.views.ContainerTab#createControlGroup()
	 */
	@Override
	abstract protected void createControlGroup();
	
	@Override
	public void dispose() {
		_component=null;
		super.dispose();
	}
	
}
