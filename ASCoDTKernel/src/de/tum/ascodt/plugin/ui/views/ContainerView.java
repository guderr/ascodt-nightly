package de.tum.ascodt.plugin.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.part.ViewPart;

/**
 * A view proving a tab folder to integrate multiple tabs
 * @author Atanas Atanasov
 *
 */
public class ContainerView extends ViewPart{
	private TabFolder folder;
	@Override
	public void createPartControl(Composite parent) {
		folder=new TabFolder(parent,SWT.FILL);
	}

	
	
	/**
	 * A getter for tab folder for this container
	 * @return
	 */
	public TabFolder getUITabFolder() {
		return folder;
	}



	@Override
	public void setFocus() {
		folder.setFocus();
	}

}
