package de.tum.ascodt.plugin.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.repository.Target;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * A wizard which helps to create new cca components
 * @author Atanas Atanasov
 *
 */
public class NewComponentWizard extends Wizard implements INewWizard {
	
	public static String ID=NewComponentWizard.class.getCanonicalName();
	
	/**
	 * the main page of the wizard
	 */
	private NewComponentWizardPage _page;
	
	/**
	 * the initial project identifier
	 */
	private String _initialProjectIdentifier;
	public NewComponentWizard() {
		setWindowTitle(Messages.NewComponentWizard);
	}

	/**
	 * insert the pages of the new component wizard
	 */
	@Override
	public void addPages() {
		super.addPages();
		_page=new NewComponentWizardPage(Messages.NewComponentWizard);
		_page.setTitle(Messages.CustomComponentNewWizard_2);
		_page.setDescription(Messages.ComponentFromScratch);
		addPage(_page);
		_page.setInitialProjectIdentifier(_initialProjectIdentifier);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		de.tum.ascodt.plugin.utils.tracing.Trace trace = new de.tum.ascodt.plugin.utils.tracing.Trace(getClass().getName());
		trace.in( "performFinish()" );
		try {
			String projectIdentifier=_page.getProjectName();
	    String componentName = _page.getComponentName();
	    String componentNamespace=_page.getComponentNamespace(); 
	    Target componentTarget = _page.getComponentTarget();
	    ProjectBuilder.getInstance().getProject(projectIdentifier).createComponentSIDLSourceFile(componentName, componentNamespace,componentTarget);
	    
		} catch (ASCoDTException e) {
      ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()",  "Cannot create new component", e );
      trace.out( "performFinish()", false );
      return false;
    }
		trace.out( "performFinish()" );
		return true;
	}

	/**
	 * Setter for the initial project identifier
	 * @param initialProjectIdentifier
	 */
	public void setInitialProjectIdentifier(String initialProjectIdentifier) {
		_initialProjectIdentifier=initialProjectIdentifier;
	}

}
