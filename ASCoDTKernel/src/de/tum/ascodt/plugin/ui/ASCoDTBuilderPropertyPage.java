package de.tum.ascodt.plugin.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * 
 * @author atanasoa
 *
 *A property page allowing the easy configuration of the ascodt compiler
 *The following options of the compiler can be configured:
 *->includes: dependet sidl files
 */
public class ASCoDTBuilderPropertyPage extends PropertyPage implements
IWorkbenchPropertyPage {
	
	/**
	 * holds all included sidl files
	 */
	private List _includes;

	/**
	 * a flag if the compiler configuration has changed
	 */
	private boolean _isDirty;
	
	private int _compiledSIDLFiles; 
	
	private IProject _project;
	public ASCoDTBuilderPropertyPage() {
		_includes=null;
		_isDirty=false;
		_compiledSIDLFiles=0;
		_project=null;
	}

	
	/**
	 * creates all controls for the compiler property page
	 */
	@Override
	protected Control createContents(Composite parent) {
		if(getElement() instanceof IResource)
			_project=((IResource) getElement()).getProject();
	
			
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		Label includesLabel = new Label(composite, SWT.FILL|SWT.SEPARATOR | SWT.HORIZONTAL);
		includesLabel.setText("Included sidl files:");
		_includes = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		try {
			for(String dep:ProjectBuilder.getInstance().getProject(_project).getSIDLDependencies())
				_includes.add(dep);
		} catch (CoreException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "createContents()",  "Compiler not configured correctly", e );
		}
		Button importSidlButton = new Button(composite, SWT.BORDER );
		importSidlButton.setText("Import SIDL");
		importSidlButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog fileDialog =new FileDialog(shell,SWT.MULTI);
				fileDialog.setFilterExtensions(new String[]{"*.sidl"});
				String res=fileDialog.open();
				if(res!=null){
					for(String file:fileDialog.getFileNames()){
						includeSidlFile(fileDialog.getFilterPath()+"/"+file);
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

		});
		return composite;
	}
	
	/**
	 * a helper function to add new sidl files in the includes list
	 * @param path
	 */
	private void includeSidlFile(String path){
		boolean exists=false;
		for(String include:_includes.getItems())
			if(include.equals(path))
				exists=true;
		if(!exists){
			_isDirty=true;
			_includes.add(path);
		}
	}
	
	/**
	 * this function is called after accepting the changes done on the property page
	 */
	public boolean performOk() {
		if(_isDirty){
			for(int i=_compiledSIDLFiles;i<_includes.getItemCount();i++){
				try {
					ProjectBuilder.getInstance().getProject(_project).addSIDLDependency(_includes.getItem(i));
				} catch (CoreException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(), "performOk()",  "Compiler not configured correctly", e );
					return false;
				} catch (ASCoDTException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(), "performOk()",  "Compiler not configured correctly", e );
					return false;
				}
			}
			_compiledSIDLFiles=_includes.getItemCount();
			_isDirty=false;
		}
		return true;
	}
	
}
