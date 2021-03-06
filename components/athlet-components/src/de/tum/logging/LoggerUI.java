//
// ASCoDT - Advanced Scientific Computing Development Toolkit
//
// This file was generated by ASCoDT's simplified SIDL compiler.
//
// Authors: Tobias Weinzierl, Atanas Atanasov   
//
package de.tum.logging;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoggerUI extends de.tum.ascodt.plugin.ui.tabs.UITab {
     
	Text textBoxForConfiguration;
	public LoggerUI(Logger component){
		super(component);
	}
	/**
	 * here you need to instantiate your own controls
	 * use _tabFolderPage as parent
	 */
	@Override
	protected void createControlGroup() {
		GridLayout layout= new GridLayout();
		layout.numColumns=1;

		super.tabFolderPage.setLayout(layout);

		final ExpandBar bar = new org.eclipse.swt.widgets.ExpandBar(super.tabFolderPage, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;	
		gridData.grabExcessVerticalSpace = true;
		bar.setLayoutData(gridData);
		createControlGroup(bar);
	}

	private void createControlGroup(ExpandBar bar) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns=2;

		Composite argsComp = new Composite(bar,SWT.NONE);
		argsComp.setLayout(gridLayout);
		GridData textGridData = new GridData();
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.grabExcessHorizontalSpace = true;


		Label loggingConfigurationFile = new Label(argsComp,SWT.LEFT);
		loggingConfigurationFile.setText("Log4j configuration file:");
		loggingConfigurationFile.setLayoutData(textGridData);
		textBoxForConfiguration = new Text(argsComp,SWT.RIGHT);
		textBoxForConfiguration.setLayoutData(textGridData);
		
		Button browseBtn = new Button(argsComp,SWT.LEFT);
		browseBtn.setText("Setup");
		browseBtn.setLayoutData(textGridData);

		browseBtn.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = Display.getDefault().getActiveShell();
				FileDialog fileDialog =new FileDialog(shell,SWT.OPEN);
				fileDialog.setText("Path to xml configurration");
				fileDialog.setFilterExtensions(new String[]{"*.xml"});
				String configuration = fileDialog.open();
				getImplementation().setXMLConfiguration(configuration);
				textBoxForConfiguration.setText(configuration);
			}


		});


		final ExpandItem itemData = new ExpandItem(bar, SWT.NONE, 0);
		bar.setSize(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				SWT.DEFAULT);
		itemData.setText("Simulation controler");
		itemData.setHeight(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		itemData.setControl(argsComp);
		itemData.setExpanded(true);
	}
	private LoggerJavaImplementation getImplementation(){
		if(_component instanceof LoggerJavaImplementation){	
			return (LoggerJavaImplementation)_component;
		}
		return null;
	}
     
     private Logger getCastedComponent(){
          if(_component instanceof Logger){
               return (Logger) _component;
          }
          return null;
     }
}
