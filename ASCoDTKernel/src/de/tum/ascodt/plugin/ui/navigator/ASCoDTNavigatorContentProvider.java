package de.tum.ascodt.plugin.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.project.ProjectChangedListener;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;

/**
 * A provider for the ASCoDT navigation view. The provider class queries the ProjectBuilder
 * for newly added, removed or changed ASCoDT projects. The ASCoDT projects are then displays
 * in the navigation view.
 * @author Atanas Atanasov
 *
 */
public class ASCoDTNavigatorContentProvider implements ITreeContentProvider, ProjectChangedListener {
	/**
	 * the swt viewer component
	 */
	private Viewer _viewer;
	
	/**
	 * Costructor: here we register the provider as changed lister to the projectbuilder
	 */
	public ASCoDTNavigatorContentProvider(){
		ProjectBuilder.getInstance().registerProjectChangedListener(this);
	}
	
	@Override
	public void dispose() {
		ProjectBuilder.getInstance().removeProjectChangedListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		_viewer=viewer;
	}

	/**
	 * return all elements, which can be shown 
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @return the children of given object or nil if the object is unknown
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[]children=null;
		try{
			if(parentElement instanceof ProjectBuilder){
				children=((ProjectBuilder) parentElement).getProjects().toArray();
			}else if(parentElement instanceof Project){
				children=((Project)parentElement).getFolders();
			}else if(parentElement instanceof IFolder){
				children=((IFolder)parentElement).members();
			}
		}catch(CoreException e){
			 ErrorWriterDevice.getInstance().showError( getClass().getName(), "getChildren()",  "Cannot get children for an object", e );
		}
		return children;
	}

	/**
	 * @return the parent of given object or nil if the object is unknown
	 */
	@Override
	public Object getParent(Object element) {
		if(element instanceof Project)
			return ProjectBuilder.getInstance();
		else if(element instanceof IFolder){
			return ProjectBuilder.getInstance().getProject(((IFolder)element).getProject());
		}else if(element instanceof IFile){
			return ((IFile)element).getParent();
		}
		return null;
	}

	/**
	 * returns if the navigator node should have children 
	 */
	@Override
	public boolean hasChildren(Object element) {
		try{
			return (element instanceof ProjectBuilder && ProjectBuilder.getInstance().getProjects().size()>0)
			|| (element instanceof Project && ((Project) element).getFolders().length>0)
			|| (element instanceof IFolder && ((IFolder)element).members().length>0);
		}catch(CoreException e){
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "hasChildren()",  "Cannot get children for an object", e );
		}
		return false;
	}
	
	

	@Override
	public void begin() {
		
	}

	/**
	 * refresh the viewer after event processing has finished
	 */
	@Override
	public void end() {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				if(_viewer!=null)
					_viewer.refresh();
			}
			
		});
	
	}

	@Override
	public void notify(Project project) {
		
	}
	

}
