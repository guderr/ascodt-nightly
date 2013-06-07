package de.tum.ascodt.plugin.ui.views;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.navigator.CommonNavigator;

import de.tum.ascodt.plugin.project.ProjectBuilder;

/**
 * This class defines a common navigator view for the ascodt project. 
 * As model for the navigator we use the Project class.
 * @author Atanas Atanasov
 *
 */
public class ASCoDTNavigator extends CommonNavigator {
	public static final String ID=ASCoDTNavigator.class.getCanonicalName();
	public ASCoDTNavigator() {
		this.setLinkingEnabled(true);
	
	}
	
	@Override
	protected IAdaptable getInitialInput(){
		return ProjectBuilder.getInstance();
	}

}
