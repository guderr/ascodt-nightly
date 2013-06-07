/**
 * 
 */
package de.tum.ascodt.plugin.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * @author atanasoa
 *
 */
public class NewWorkbenchWizard extends Wizard implements INewWizard {
public static String ID=NewWorkbenchWizard.class.getCanonicalName();
	
	/**
	 * the main page of the wizard
	 */
	private NewWorkbenchWizardPage _page;
	
	/**
	 * the initial project identifier
	 */
	private String _initialProjectIdentifier;
	public NewWorkbenchWizard() {
		setWindowTitle(Messages.NewWorkbenchWizard);
	}

	/**
	 * insert the pages of the new component wizard
	 */
	@Override
	public void addPages() {
		super.addPages();
		_page=new NewWorkbenchWizardPage(Messages.NewWorkbenchWizard);
		_page.setTitle(Messages.NewWorkbenchWizard_1);
		_page.setDescription(Messages.NewWorkbenchWizard_2);
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
	    String workbenchName = _page.getWorkbenchName();
	    ProjectBuilder.getInstance().getProject(projectIdentifier).createWorkbench(workbenchName);
	    
      
		} catch (ASCoDTException e) {
      ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()",  "Cannot create new workbench", e );
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
