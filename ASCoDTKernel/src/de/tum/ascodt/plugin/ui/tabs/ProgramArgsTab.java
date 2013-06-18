package de.tum.ascodt.plugin.ui.tabs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.tum.ascodt.plugin.utils.Importer;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;
abstract class ProgramArgsTab extends ContainerTab{

	protected Text textProgramArguments;
	protected Text textProgramExecutable;
	private java.io.File applicationSettings;
	private boolean _isStarted;
	
	protected ProgramArgsTab(String label, String containerId) {
		super(label, containerId);
		applicationSettings = new java.io.File(label+".settings");
		_isStarted=false;
	}
 
	protected void createControlGroup(){
		
		
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
		loadStorageFiles();
		//super.createControlGroup(bar);
	}
	public boolean hasApplicationSettings(){
		return applicationSettings.exists();
	}
	private void loadStorageFiles() {
		if(hasApplicationSettings())
		{
			try {
				BufferedReader reader= new BufferedReader(new FileReader(applicationSettings));
				String line =""; 
				int counter=0;
				while((line=reader.readLine())!=null){
					if(counter==0)
						textProgramExecutable.setText(line);
					else
						textProgramArguments.setText(line);
					counter++;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
	}

	protected void createControlGroup(ExpandBar bar){
		
		createArgsCotrolItems(bar);
	}
	
	public void onStart(){
		saveProgramSettings();
		synchronized(this){
			_isStarted=true;
		}
	}
	private void saveProgramSettings() {
		FileWriter fwriter;
		try {
			fwriter = new FileWriter(applicationSettings,false);
			BufferedWriter writer = new BufferedWriter(
					fwriter
					);
			writer.write(textProgramExecutable.getText()+"\n");
			writer.write(textProgramArguments.getText()+"\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void createArgsCotrolItems(ExpandBar bar) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns=2;

		Composite argsComp = new Composite(bar,SWT.NONE);
		argsComp.setLayout(gridLayout);
		GridData textGridData = new GridData();
		textGridData.horizontalAlignment = GridData.FILL;
		textGridData.grabExcessHorizontalSpace = true;
		
		
		Label labelProgramExecutable=new Label(argsComp,SWT.LEFT);
		labelProgramExecutable.setText("Executable:");
		labelProgramExecutable.setLayoutData(textGridData);
		
		textProgramExecutable=new Text(argsComp,SWT.RIGHT);
		textProgramExecutable.setText("/work_fast/atanasoa/Programme/runtime-HelloSocketFortran/AthletCoupling/native/Athlet");
		textProgramExecutable.setLayoutData(textGridData);
		
		Label labelProgramArgumets=new Label(argsComp,SWT.LEFT);
		labelProgramArgumets.setText("Arguments:");
		labelProgramArgumets.setLayoutData(textGridData);
		
		textProgramArguments = new Text(argsComp,SWT.RIGHT);
		textProgramArguments.setText("");
		textProgramArguments.setLayoutData(textGridData);
		
		Button browseBtn = new Button(argsComp,SWT.LEFT);
		browseBtn.setText("Browse..");
		browseBtn.setLayoutData(textGridData);
		
		browseBtn.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog fileDialog =new FileDialog(shell,SWT.OPEN);
				fileDialog.setText("Path to executable");
				//fileDialog.setFilterExtensions(new String[]{"*.ascodt-component"});
				textProgramExecutable.setText(fileDialog.open());
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		Button startBtn = new Button(argsComp,SWT.RIGHT);
		startBtn.setText("Start");
		startBtn.setLayoutData(textGridData);
		startBtn.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				execute();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		final ExpandItem itemData = new ExpandItem(bar, SWT.NONE, 0);
		bar.setSize(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				SWT.DEFAULT);
		itemData.setText("Domain settings");
		itemData.setHeight(argsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		itemData.setControl(argsComp);
		itemData.setExpanded(true);
	}
	
	
	public abstract String getCommandForExecution();
//	public String[] getCmd(){
//		String[] tokenizedArgs=  textProgramArguments.getText().split(" ");
//		String[] res = new String[tokenizedArgs.length+1];
//		res[0]= textProgramExecutable.getText();
//		for(int i=0;i<tokenizedArgs.length;i++)
//			res[i+1]=tokenizedArgs[i];
//		return res;
//	}
	public String[] getEnv(){
		return new String[]{};
	}
	public synchronized boolean isStarted(){
		return _isStarted;
	}

	/**
	 * 
	 */
	public void execute() {
		try {
			String cmd=getCommandForExecution();
			ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
			Map<String, String> env = pb.environment();
			if (env != null)
				for(String envVar: getEnv()){
					String[] envPair=envVar.split("=");
					env.put(envPair[0],envPair[1]);
				}
			final Process p = pb.start();
			
			//final Process p=Runtime.getRuntime().exec(cmd,getEnv());
			ExecutorService exService=java.util.concurrent.Executors.newCachedThreadPool();
			exService.execute(new Runnable(){

				@Override
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line="";
					try {
						while ((line=reader.readLine())!=null)
								System.out.println(line);
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			});
			exService.execute(new Runnable(){

				@Override
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line="";
					try {
						while ((line=reader.readLine())!=null)
								System.err.println(line);
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			});
			onStart();
			
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
