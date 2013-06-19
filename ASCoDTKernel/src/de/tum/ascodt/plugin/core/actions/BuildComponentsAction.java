package de.tum.ascodt.plugin.core.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.project.natures.ASCoDTNature;
import de.tum.ascodt.plugin.ui.gef.editparts.DiagramEditPart;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

public class BuildComponentsAction implements IWorkbenchWindowActionDelegate {

	private HashSet<IProject> selection;
	private Shell shell;

	@Override
	public void run(IAction action) {
		for(IProject project:selection){
		
			MessageBox dialog = 
					  new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
					dialog.setText("Warning");
					dialog.setMessage("Rebuilding the current project needs to reinitiate the project workbench files! Are your sure?");

			
			if(dialog.open()==SWT.OK){
				Vector<IFile> files=ProjectBuilder.getInstance().getProject(project).closeRunningWorkbenchInstances();
				ProjectBuilder.getInstance().getProject(project).compileComponents();
				try {
					ProjectBuilder.getInstance().getProject(project).openWorkbenchEditors(files);
				} catch (ASCoDTException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(),e.getLocalizedMessage(), e );
				}
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection.clear();
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			for (
					Iterator<Object> iterator = structuredSelection.iterator();
					iterator.hasNext();
					){

				Object element = iterator.next();

				try {
					if(element instanceof DiagramEditPart){
						IEditorPart part = 
								((DefaultEditDomain)((DiagramEditPart)element).getViewer().getEditDomain()).getEditorPart();
						
						IFile file_element=null;
						if(part!=null&&part.getEditorInput() instanceof  IFileEditorInput){
							file_element =((IFileEditorInput)part.getEditorInput()).getFile();

							try {
								if(file_element!=null && file_element instanceof IResource&&((IResource) file_element).getProject().hasNature(ASCoDTNature.ID))
									this.selection.add(((IResource) file_element).getProject());
							} catch (CoreException e) {
								ErrorWriterDevice.getInstance().showError( getClass().getName(),e.getLocalizedMessage(), e );
							}
						}
					}
					if(element instanceof Project)
						this.selection.add(((Project) element).getEclipseProjectHandle());
					if(element instanceof IProject)
						this.selection.add(((IProject) element));
					if(element instanceof IResource&&((IResource) element).getProject().hasNature(ASCoDTNature.ID))
						this.selection.add(((IResource) element).getProject());
				} catch (CoreException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(),e.getLocalizedMessage(), e );
				}
			}

		}
		
		if (selection instanceof TextSelection) {
			 IEditorPart activeEditor = PlatformUI
	                    .getWorkbench()
	                    .getActiveWorkbenchWindow()
	                    .getActivePage()
	                    .getActiveEditor();
	        
			IFile element=null;
			if(activeEditor!=null&&activeEditor.getEditorInput() instanceof  IFileEditorInput)
				element =((IFileEditorInput)activeEditor.getEditorInput()).getFile();

				try {
					if(element!=null && element instanceof IResource&&((IResource) element).getProject().hasNature(ASCoDTNature.ID))
						this.selection.add(((IResource) element).getProject());
				} catch (CoreException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(),e.getLocalizedMessage(), e );
				}
			}

		
	}

	@Override
	public void dispose() {
		selection.clear();
		selection=null;

	}

	@Override
	public void init(IWorkbenchWindow window) {
		shell=window.getShell();
		selection = new HashSet<IProject>();

	}

}
