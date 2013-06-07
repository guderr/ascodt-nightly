/**
 * 
 */
package de.tum.ascodt.plugin.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.ILinkHelper;

import de.tum.ascodt.plugin.utils.tracing.Trace;

/**
 * @author Atanas Atanasov
 * Here we specify links between elements in the navigator view and editors. We want 
 * to have a link between a sidl resource and the sidl editor, worbench-resource and
 * the component-editor
 *
 */
public class ASCoDTNavigatorLinkHelper implements ILinkHelper {
	
	private Trace _trace = new Trace(ASCoDTNavigatorLinkHelper.class.getCanonicalName());
	public ASCoDTNavigatorLinkHelper(){
		_trace.in("Constructor(..)");
		_trace.out("Constructor(..)");
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#findSelection(org.eclipse.ui.IEditorInput)
	 */
	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		_trace.in("findSelection(..)");
		_trace.out("findSelection(..)");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#activateEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void activateEditor(IWorkbenchPage aPage,
			IStructuredSelection aSelection) {
		// TODO Auto-generated method stub
		
		_trace.in("activateEditor(..)",	aSelection.getFirstElement().toString());
	 if(aSelection.getFirstElement() instanceof IFile){
		 try {
			IDE.openEditor(aPage, ((IFile)aSelection.getFirstElement()));
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
		_trace.out("activateEditor(..)");
	}

}
