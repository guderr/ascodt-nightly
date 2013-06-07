package de.tum.ascodt.plugin;


import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.tum.ascodt.plugin.services.SocketService;
import de.tum.ascodt.plugin.services.UIService;
import de.tum.ascodt.plugin.utils.ConsoleStream;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.project.natures.ASCoDTNature;
import de.tum.ascodt.utils.ConsoleDevice;
import de.tum.ascodt.utils.OutputDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * Singleton for the plugin instance. 
 * @author atanasoa
 *
 */

@SuppressWarnings("restriction")
public class ASCoDTKernel extends AbstractUIPlugin {
	public static final String ID = ASCoDTKernel.class.getCanonicalName();
	private static ASCoDTKernel singleton;

	/**
	 * holds reference to the error manager service
	 */
	private de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice errorManager;

	/**
	 * the service responsible for user defined graphical interfaces
	 */
	private UIService _uiService;

	private SocketService _socketService;

	public static ASCoDTKernel getDefault(){
		if(singleton==null)
			return new ASCoDTKernel();
		return singleton;
	}


	public void start(BundleContext context) throws Exception {
		super.start(context);
		initializeSwing();
		initialiseServices();

		initializeProjects();
		initializeProjectLifeCycle();
		initializeOutputDevice();
	}

	/**
	 * This function setups the ascodt project by invoking the project builder to create
	 * the project representation object
	 * @param project the current project
	 * @throws JavaModelException
	 * @throws IOException
	 */
	private void prepareProject(IProject project){
		try {
			ProjectBuilder.getInstance().createProject(project);
		} catch (ASCoDTException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "prepareProject()", "Cannot create project representation object due to " + e.getCause(), e );

		} catch (MalformedURLException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "prepareProject()", "Cannot create project representation object due to " + e.getCause(), e );
		}	
	}


	/**
	 * 
	 * !!! Jobs of the constructor
	 * 
	 * - Create a listener to the Eclipse workbench that reacts to project open and project call calls. 
	 * - Initialise services
	 * - t.b.d.
	 * 
	 */
	public ASCoDTKernel() {
		singleton=this;
	  // for uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				ErrorWriterDevice.getInstance().showError(UncaughtExceptionHandler.class.getCanonicalName(), "uncaughtException(Thread,Throwable", e);

			}

		});


	}



	private void initializeOutputDevice() {
		OutputDevice output=ConsoleDevice.getInstance().getConsole( "ASCoDT Components Output" );
		OutputDevice error_output=ConsoleDevice.getInstance().getConsole( "ASCoDT Error Output" );
		if(output!=null && output instanceof ConsoleStream && 
				error_output!=null && error_output instanceof ConsoleStream){
			System.setOut(new PrintStream(((ConsoleStream)output).newMessageStream()));
	    MessageConsoleStream errorStream=((ConsoleStream)error_output).newMessageStream();
	    errorStream.setColor(new org.eclipse.swt.graphics.Color(Display.getDefault(),255,0,0));
			System.setErr(	new PrintStream(errorStream));
		}
	}

	/**
	 * adds a project lifecycle listener to the workspace. We need to create the project
	 * representation objects for newly opened ascodt projects
	 */
	private void initializeProjectLifeCycle() {
		((Workspace) ResourcesPlugin.getWorkspace()).addLifecycleListener(new ILifecycleListener(){

			@Override
			public void handleEvent(LifecycleEvent event) throws CoreException {

				if(event.resource instanceof IProject && ((IProject)event.resource).isAccessible()  &&
						((IProject)event.resource).isNatureEnabled(ASCoDTNature.ID)){

					if(event.kind==LifecycleEvent.PRE_PROJECT_OPEN){
						prepareProject((IProject)event.resource);
					}else if(event.kind==LifecycleEvent.PRE_PROJECT_CLOSE){
						ProjectBuilder.getInstance().removeProject((IProject)event.resource);
					}else if(event.kind==LifecycleEvent.PRE_PROJECT_DELETE){
						ProjectBuilder.getInstance().removeProject((IProject)event.resource);
					}

				}
			}

		});
	}

	/**
	 * This method loops through all ascodt projects and setups their project objects
	 */
	public void initializeProjects(){
		Job job = new Job("Projects initialisation") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for(IProject project:((Workspace) ResourcesPlugin.getWorkspace()).getRoot().getProjects()){

						if(project.isOpen()&&project.isNatureEnabled(ASCoDTNature.ID))
							prepareProject(project);
					}
					 return Status.OK_STATUS;
				} catch (CoreException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(), "initializeProjects()", "Cannot initialize an existing project:" + e.getCause(), e );
					 return Status.CANCEL_STATUS;
				}
			}

		};
		job.schedule();

	}



	/**
	 * initialises all services 
	 * @throws IOException 
	 */
	public void initialiseServices() {
		_uiService = new UIService();
		_socketService = new SocketService();
		//		vtkService = new VTKService();
		//		pvService=new ParaViewService();
		//		coRepService=new ComponentRegistryService();
		//		remoteCompService= new RemoteComponentService();
	}






	void initializeSwing() {
		/*
		 * Feature in GTK.  The default X error handler
		 * for GTK calls exit() after printing the X error.
		 * Normally, this isn't that big a problem for SWT
		 * applications because they don't cause X errors.
		 * However, sometimes X errors are generated by AWT
		 * that make SWT exit.  The fix is to hide all X
		 * errors when AWT is running.
		 */

		try {
			/* Initialize the default focus traversal policy */
			Class<?>[] emptyClass = new Class[0];
			Object[] emptyObject = new Object[0];
			Class<?> clazz = Class.forName("javax.swing.UIManager");
			Method method = clazz.getMethod("getDefaults", emptyClass);
			if (method != null) method.invoke(clazz, emptyObject);
		} catch (Throwable e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName() , "performFinish()", e);
		}

	}
	public String getLocation(){
		boolean hasBinFolder=false;
		if(FileLocator.find(Platform.getBundle(ASCoDTKernel.ID), new Path("bin"),null)!=null)
			hasBinFolder=true;
		try {
			return FileLocator.toFileURL(FileLocator.find(Platform.getBundle(ASCoDTKernel.ID), new Path((hasBinFolder?"bin":"")),null)).getPath();
		} catch (IOException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName() , "performFinish()", e);
		}
		return "";
	}
	/**
	 * 
	 * @return the error manager service
	 */
	public de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice getErrorManager(){
		return errorManager;
	}
	//
	//
	//
	//
	//	public CCARemoteComponentSftpFileInstantiator getSftpFileInstantiatorFactory(){
	//		return sftpFileInstantiator;
	//	}
	//	//list of all services
	//

	//A Getter for the ui service
	public UIService getUIservice(){
		return _uiService;
	}

	public SocketService getSocketService(){
		return _socketService;
	}
	//A Getter for stand alone applications service

	//
	//	//getter for the vtk service
	//	public VTKService getVTKService(){
	//		return vtkService;
	//	}
	//
	//	//getter for the component registry service
	//	public ComponentRegistryService getComponetRegistryService(){
	//		return coRepService;
	//	}



	//	/**
	//	 * getter for the paraview service
	//	 * @return
	//	 */
	//	public ParaViewService getPVService() {
	//		return pvService;
	//	}
	//
	//
	//	/**
	//	 * @return the remoteCompService
	//	 */
	//	public RemoteComponentService getRemoteComponentService() {
	//		return remoteCompService;
	//	}
}
