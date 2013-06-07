package de.tum.ascodt.plugin.ui.wizards;


import java.net.MalformedURLException;
import java.net.URI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.ui.perspectives.ASCoDTPerspective;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * The new Project wizard class for ASCoDT.
 * 
 * The class creates the ASCoDT Project Wizards and initializes its pages.
 * The performFinish method creates the project files and opens the project. 
 * 
 * @author Atanas Atanasov, Tobias Weinzierl
 */
public class NewProjectWizard extends Wizard implements INewWizard {

	private NewProjectWizardPage   _page;
	
	
	public NewProjectWizard() {
		setWindowTitle(Messages.CustomProjectNewWizard_0);
	}

	
	/**
	 * insert the pages of the new project wizard
	 */
	@Override
	public void addPages() {
		super.addPages();

		_page = new NewProjectWizardPage(Messages.CustomProjectNewWizard_1);
		_page.setTitle(Messages.CustomProjectNewWizard_2);
		_page.setDescription(Messages.CustomProjectNewWizard_3);

		addPage(_page);
	}
	
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}


    // @todo Der Editor muss natuerlich schon geoeffnet werden, aber nicht fuer das File, sondern fuer einen Teil des Repositories
//    final String ASCFile = NameFactory.getFullQualifiedWorkbenchStateFile();
//    trace.debug( "performFinish()", "access " + ASCFile );
//
//    final IFile f= pr.getFile(ASCFile);
//
//    
//    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
//      public void run() {
//        IWorkbenchPage page =
//          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//        try {
//
//          IDE.openEditor(page,f, true);
//        } catch (PartInitException e) {
//          ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()", e);
//        }
//      }
//    });
  
	/**
	 * The core of this UI element is the call to Project.createProject(). 
	 * Afterwards, the right windows/views are opened. While the former is just a 
	 * call to the corresponding operation, the latter is a process triggered 
	 * indirectly: We tell Eclipse to switch to the ASCoDT perspective which in 
	 * turn holds the information which views to open.
	 * 
	 * @see ASCoDTPerspective
	 */
	@Override
	public boolean performFinish() {
	  de.tum.ascodt.plugin.utils.tracing.Trace trace = new de.tum.ascodt.plugin.utils.tracing.Trace(getClass().getName());
	  trace.in( "performFinish()" );
	  
		try {
	    String name = _page.getProjectName();
	    URI location = null;
	    if (!_page.useDefaults()) {
	      location = _page.getLocationURI();
	    } 
	    ProjectBuilder.getInstance().createProject(name, location);
	    
      IWorkbench             workbench   = PlatformUI.getWorkbench();
      IWorkbenchWindow       window      = workbench.getActiveWorkbenchWindow();
      IWorkbenchPage         page        = window.getActivePage();
      IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry().findPerspectiveWithId(ASCoDTPerspective.ID);
      page.setPerspective(perspective);
		} catch (CoreException e) {
      ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()", "Cannot create new project due to " + e.getCause(), e );
      trace.out( "performFinish()", false );
      return false;
		} catch (ASCoDTException e) {
      ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()",  "Cannot create new project", e );
      trace.out( "performFinish()", false );
      return false;
    } catch (MalformedURLException e) {
    	ErrorWriterDevice.getInstance().showError( getClass().getName(), "performFinish()",  "Cannot create new project due to "+ e.getCause(), e );
        trace.out( "performFinish()", false );
	}

    trace.out( "performFinish()", true );
		return true;
	}
}
