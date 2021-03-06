package de.tum.ascodt.plugin.core.commands;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.project.natures.ASCoDTNature;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


public class BuildComponentsHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection=HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
			if(selection!=null && selection instanceof IStructuredSelection){
				IStructuredSelection strucSelection = (IStructuredSelection)selection;
				for (@SuppressWarnings("unchecked")
				Iterator<Object> iterator = strucSelection.iterator(); iterator.hasNext();) {
					Object element = iterator.next();

					if(element instanceof IResource&&((IResource) element).getProject().hasNature(ASCoDTNature.ID))
						ProjectBuilder.getInstance().getProject(((IResource) element).getProject()).compileComponents();

				}
			}
		} catch (CoreException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "execute()",  "Cannot compile components", e );
		} 
		return null;

	}


}
