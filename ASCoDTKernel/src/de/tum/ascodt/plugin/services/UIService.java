package de.tum.ascodt.plugin.services;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.tum.ascodt.plugin.ui.views.ContainerView;
import de.tum.ascodt.plugin.ui.views.UIViewContainer;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;


/**
 * A service to access the container for user defined UIs.
 * A user defined graphical interface should extend the UITab class.
 * @author Atanas Atanasov
 *
 */
public class UIService {

	public TabFolder getContainer(final String containerID){
		class CustomThread extends Thread{
			private IViewPart uiContainer;
			public void run() {
				IWorkbenchWindow dwindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = dwindow.getActivePage();

				if (dwindow!=null&&page != null) {
					boolean found=false;
					if(page.getViewReferences()!=null)
						for(IViewReference ref:page.getViewReferences())
							if(ref.getId().equals(containerID)){
								found=true;
								uiContainer = ref.getView(true);
							}
					if(!found)
						try {
							uiContainer = page.showView(containerID);
						} catch (PartInitException e) {
				      ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()", e);
						};
						
				}
			}
			public IViewPart getUIContainer(){
				return uiContainer;
			}
		}
		ContainerView containerView=null;

		CustomThread t=new CustomThread(); 
		PlatformUI.getWorkbench().getDisplay().syncExec(t);
		containerView=(ContainerView) t.getUIContainer();

		return containerView.getUITabFolder();
	}

}
