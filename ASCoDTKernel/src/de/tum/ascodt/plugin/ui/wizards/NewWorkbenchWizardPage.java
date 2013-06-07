package de.tum.ascodt.plugin.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

import de.tum.ascodt.plugin.project.ProjectBuilder;


/**
 * A wizard class which helps with the creation of a new workbench. The workbench
 * represents the dynamic repository in ASCoDT, where the user can use his compiled
 * components and create new applications. 
 * @author Atanas Atanasov
 *
 */
@SuppressWarnings("restriction")
public class NewWorkbenchWizardPage extends WizardPage{

	/**
	 * holds the name of the workbench
	 */
	private Text _workbenchNameField;
  
	/**
	 * a combobox with all ascodt projects
	 */
	private Combo _projectsComboBox;

	
	/**
   * initial value to store
   */
	private String _initialWorkbenchFieldValue="MyWorkbench";
	
	/**
	 * initial project id or null
	 */
	private String _initialProjectIdentifier;
 

	//constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	
	/**
	 * a listener which tracks for changes on the defined controls
	 */
	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);

		}
	};
	
	
	protected NewWorkbenchWizardPage(String pageName) {
		super(pageName);
		_initialProjectIdentifier=null;
		setPageComplete(false);
	}
	
	/**
	 * creates the user interface
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);


		initializeDialogUnits(parent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createComponentCreationGroup(composite);
		
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		
	}

	/**
	 * Creates the component specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createComponentCreationGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	  // project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText("Project:");
		projectLabel.setFont(parent.getFont());
		
		_projectsComboBox = new Combo(projectGroup, SWT.NONE);
		int selectionIndex=0,counter=0;
		
		for(String projectId: ProjectBuilder.getInstance().getProjectsIdentifiers()){
			_projectsComboBox.add(projectId);
			if(_initialProjectIdentifier!=null&&projectId.equals(_initialProjectIdentifier))
				selectionIndex=counter;
			counter++;
		}
		_projectsComboBox.select(selectionIndex);
		_projectsComboBox.setFont(parent.getFont());
		_projectsComboBox.addListener(SWT.Modify, nameModifyListener);
	
		// workbench name label
		Label workbenchLabel = new Label(projectGroup, SWT.NONE);
		workbenchLabel.setText("Workbench name:");
		workbenchLabel.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		// new component name entry field
		_workbenchNameField = new Text(projectGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		_workbenchNameField.setLayoutData(data);
		_workbenchNameField.setFont(parent.getFont());

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (_initialWorkbenchFieldValue != null) {
			_workbenchNameField.setText(_initialWorkbenchFieldValue);
		}
		_workbenchNameField.addListener(SWT.Modify, nameModifyListener);
	}
	
	
	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

		String projectNameFieldContents = getProjectNameFieldValue();
		if (projectNameFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("Invalid project name:"+projectNameFieldContents);
			return false;
		}
		
		String workbenchNameFieldContents = getWorkbenchNameFieldValue();
		if (workbenchNameFieldContents.equals("")
				||ProjectBuilder.getInstance().getProject(projectNameFieldContents).hasWorkbench(workbenchNameFieldContents)) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("Invalid workbench name");
			return false;
		}

		
		IStatus nameStatus = workspace.validateName(projectNameFieldContents,
				IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	/**
	 * Returns the current component name as entered by the user, or its anticipated
	 * initial value.
	 *
	 * @return the component name, its anticipated initial value, or <code>null</code>
	 *   if no component name is known
	 */
	public String getWorkbenchName() {
		if (_workbenchNameField == null) {
			return _initialWorkbenchFieldValue;
		}

		return getWorkbenchNameFieldValue();
	}

	/**
	 * Returns the project name, where the new component should be created
	 * @return
	 */
	public String getProjectName(){
		return getProjectNameFieldValue();
	}
	
	
	/**
	 * Returns the value of the project name combo
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project name in the combobox
	 */
	private String getProjectNameFieldValue(){
		if (_projectsComboBox == null) {
			return ""; //$NON-NLS-1$
		}

		return _projectsComboBox.getText().trim();
	}
	
	/**
	 * Returns the value of the workbench name field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the component name in the field
	 */
	private String getWorkbenchNameFieldValue() {
		if (_workbenchNameField == null) {
			return "default"; //$NON-NLS-1$
		}

		return _workbenchNameField.getText().trim();
	}
	
	

	 /**
	 * @param _initialProjectIdentifier the _initialProjectIdentifier to set
	 */
	public void setInitialProjectIdentifier(String initialProjectIdentifier) {
		this._initialProjectIdentifier = initialProjectIdentifier;
	}
	
}
