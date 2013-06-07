package de.tum.ascodt.plugin.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.repository.RepositoryListener;

public class NewComponentUIWizardPage extends WizardPage implements RepositoryListener {
	private String _initialProjectIdentifier;
	private Project _lastProject;
	protected NewComponentUIWizardPage(String pageName) {
		super(pageName);
		_initialProjectIdentifier=null;
		setPageComplete(false);
	}
	
	@Override
	public void dispose(){
		if(_lastProject!=null)
			_lastProject.getStaticRepository().removeListener(NewComponentUIWizardPage.this);
		
	}
	
	protected void  finalize(){
		if(_lastProject!=null)
			_lastProject.getStaticRepository().removeListener(NewComponentUIWizardPage.this);
		
	}
	/**
	 * a listener which tracks for changes on the defined controls
	 */
	private Listener _fieldModifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);

		}
	};
	private Combo _projectsComboBox;
	private Combo _componentInterfacesComboBox;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);


		initializeDialogUnits(parent);

		

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createComponentUICreationGroup(composite);

		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	/**
	 * Creates the component ui specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createComponentUICreationGroup(Composite parent) {
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

		Label componentInterfaceLabel = new Label(projectGroup, SWT.NONE);
		componentInterfaceLabel.setText("Component interface:");
		componentInterfaceLabel.setFont(parent.getFont());

		_componentInterfacesComboBox = new Combo(projectGroup, SWT.NONE);
		int selectionIndex=0,counter=0;
		for(String projectId: ProjectBuilder.getInstance().getProjectsIdentifiers()){
			_projectsComboBox.add(projectId);
			if(_initialProjectIdentifier!=null&&projectId.equals(_initialProjectIdentifier))
				selectionIndex=counter;
			counter++;
		}
		_projectsComboBox.select(selectionIndex);
		_projectsComboBox.setFont(parent.getFont());
		_projectsComboBox.addListener(SWT.Modify, _fieldModifyListener);
		_projectsComboBox.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				onProjectSelected();
			
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		onProjectSelected();
		
		
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		
		String projectNameFieldContents = getProjectNameFieldValue();
		if (projectNameFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("Invalid project name:"+projectNameFieldContents);
			return false;
		}
		
		String componentInterfacesFieldContents = getComponentInterfacesFieldValue();
		if (componentInterfacesFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage("Invalid component interface");
			return false;
		}

		return true;
	}

	/**
	 * @param _initialProjectIdentifier the initialProjectIdentifier to set
	 */
	public void setInitialProjectIdentifier(String initialProjectIdentifier) {
		this._initialProjectIdentifier = initialProjectIdentifier;
	}
	
	

	public String getComponentInterface() {
		return getComponentInterfacesFieldValue();
	}
	
	public String getProjectName(){
		if (_projectsComboBox == null) {
			return _initialProjectIdentifier;
		}

		return getProjectNameFieldValue();
	}

	private String getComponentInterfacesFieldValue() {
		if (_componentInterfacesComboBox == null) {
			return ""; //$NON-NLS-1$
		}

		return _componentInterfacesComboBox.getText().trim();
	}
	
	private String getProjectNameFieldValue() {
		if (_projectsComboBox == null) {
			return ""; //$NON-NLS-1$
		}

		return _projectsComboBox.getText().trim();
	}

	@Override
	public void begin() {
		_componentInterfacesComboBox.removeAll();
	}

	@Override
	public void end() {
			
	}

	@Override
	public void notify(String componentInterface, String target) {
		_componentInterfacesComboBox.add(componentInterface);
		
	}

	/**
	 * 
	 */
	public void onProjectSelected() {
		if(_lastProject!=null)
			_lastProject.getStaticRepository().removeListener(NewComponentUIWizardPage.this);
		_lastProject=ProjectBuilder.getInstance().getProject(_projectsComboBox.getItem(_projectsComboBox.getSelectionIndex()));
		_lastProject.getStaticRepository().addListener(NewComponentUIWizardPage.this);
		_lastProject.getStaticRepository().informListenersAboutChangedComponents();
		if(_componentInterfacesComboBox.getItemCount()>0)
			_componentInterfacesComboBox.select(0);
	}
}
