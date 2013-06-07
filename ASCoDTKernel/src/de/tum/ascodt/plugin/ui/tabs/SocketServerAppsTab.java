package de.tum.ascodt.plugin.ui.tabs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.tum.ascodt.repository.entities.SocketComponent;

public class SocketServerAppsTab extends ProgramArgsTab{

	
	private Text textPort;

	private SocketComponent component;
	
	public SocketServerAppsTab(
			String label,
			SocketComponent component,
			String containerId) {
		super(label, containerId);
		this.component=component;
		
	}
	protected void createControlGroup(ExpandBar bar){
		createClientUIGroup(bar);
		super.createControlGroup(bar);
	}
	
	private void createClientUIGroup(ExpandBar bar) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns=2;

		Composite argsComp = new Composite(bar,SWT.NONE);
		argsComp.setLayout(gridLayout);
		GridData textGridData = new GridData();
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.grabExcessHorizontalSpace = true;
		
		
		
		Label labelPort=new Label(argsComp,SWT.LEFT);
		labelPort.setText("Port:");
		labelPort.setLayoutData(textGridData);
		
		textPort = new Text(argsComp,SWT.RIGHT);
		textPort.setText(""+component.getPort());
		textPort.setLayoutData(textGridData);
		
		final ExpandItem itemData = new ExpandItem(bar, SWT.NONE, 0);
		bar.setSize(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				SWT.DEFAULT);
		itemData.setText("Server settings");
		itemData.setHeight(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		itemData.setControl(argsComp);
		itemData.setExpanded(true);
	}
	@Override
	public String getCommandForExecution() {
		return textProgramExecutable.getText()+" "+textProgramArguments.getText();
	}
	public void onStart(){
		super.onStart();
		this.component.open();
	}
	public String[] getEnv(){
		return new String[]{
				this.label.toUpperCase()+"_PORT="+textPort.getText()
		};
	}
	
	@Override 
	public void dispose(){
		
		super.dispose();
		this.component=null;
	}
}
