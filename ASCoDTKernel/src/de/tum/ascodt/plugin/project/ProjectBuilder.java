package de.tum.ascodt.plugin.project;


import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import de.tum.ascodt.plugin.project.natures.ASCoDTNature;
import de.tum.ascodt.plugin.repository.InstanceFactory;
import de.tum.ascodt.plugin.ui.editors.gef.WorkbenchEditor;
import de.tum.ascodt.plugin.ui.perspectives.ASCoDTPerspective;
import de.tum.ascodt.plugin.ui.views.Palette;
import de.tum.ascodt.plugin.ui.views.ProjectContentProvider;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.utils.exceptions.ASCoDTException;



/**
 * This class is responsible for the project construction and project management
 * 
 * It creates the basic files of the project and add the project natures and builders.
 *  
 * @image html ../../../../../architecture_packages-and-important-classes.png
 * 
 * @todo Atanas Doku! Ich schreibe jetzt, was das neue Version macht
 * 
 * 
 * !!! Jobs
 * 
 * - The project builder is a singleton managing all instances of Project.
 * - Other components can create new projects due to this manager (factory mechanism).
 * - Other components can gain access to the project due to this manager with the 
 *   project name.
 * 
 * @author Atanas Atanasov
 */
public class ProjectBuilder extends PlatformObject{


	public static final String ASCODT_FILE_EXTENSION = ".ascodt";

	static private ProjectBuilder  _singleton = new ProjectBuilder();
	/**
	 * a map with all children projects
	 */
	protected java.util.Map<String, Project> _nameToProjectEntitiesMap;
	private java.util.Map<org.eclipse.core.resources.IProject, Project>  _eclipseProjectToProjectMap;
	private ArrayList<Project>  _projects;
	private java.util.Set<ProjectChangedListener> _listeners;
	private ProjectContentProvider _projectsContentProvider;

	private Palette _palette;

	private WorkbenchEditor _workbench;

	/**
	 * @return Reference to singleton
	 */
	static public ProjectBuilder getInstance() {
		return _singleton;
	}


	/**
	 * Class is a singleton. Thus, you are not allowed to create instances of it. 
	 */
	private ProjectBuilder() {
		_eclipseProjectToProjectMap = new java.util.HashMap<org.eclipse.core.resources.IProject, Project>();
		_nameToProjectEntitiesMap           = new java.util.HashMap<String, Project>();
		_listeners = new java.util.HashSet<ProjectChangedListener>();
		_projects=new ArrayList<Project>();
		_projectsContentProvider=new ProjectContentProvider();
	}



	/**
	 * id of the problem markers of ASCoDT. The problems markers are used to denote
	 * errors inside of the SIDL files. Such kind of errors can occur by the compilation of the file.
	 * They show the position in the file, where the error has occurred.
	 */
	public static final String PROBLEM_MARKER_ID = "de.tum.ascodt.plugin.project.markers.ASCoDTProblemMarker";


	/**
	 * @todo Atanas Doku!
	 * 
	 * @param resource
	 * @return true if the resource lies in the source folder of the project
	 */
	public static boolean isProjectSource(String resourceLocation,IProject project,String extension){

		return (resourceLocation.indexOf((project.getLocation().toPortableString()+""))>=0&&
				resourceLocation.endsWith(extension)) ;

	}

	/**
	 * 
	 * @return if the project builder has the project
	 */
	public boolean hasProject(String projectName){
		return _nameToProjectEntitiesMap.get(projectName)!=null;
	}

	/**
	 * Get a project. 
	 * 
	 * Precondition: The project with this identifier must exist.
	 * 
	 * @param projectName
	 * @return
	 */
	public Project getProject( String projectName ) {
		Assert.isTrue( _nameToProjectEntitiesMap.containsKey(projectName) );
		return (Project)_nameToProjectEntitiesMap.get(projectName);
	}


	/**
	 * Get a project. 
	 * 
	 * Precondition: The project with this Eclipse ID must exist.
	 * 
	 * @param projectName
	 * @return
	 */
	public Project getProject( IProject projectIdentifier ) {
		Assert.isTrue( _eclipseProjectToProjectMap.containsKey(projectIdentifier) );
		return _eclipseProjectToProjectMap.get(projectIdentifier);
	}

	/**
	 * Creates a project representation object for existing eclipse project with the ASCoDT nature
	 * The method is used by starting of eclipse when we have only eclipse projects but no ascodt
	 * projects
	 * @param projectName the name of the project
	 * @param eclipseProject eclipse project reference
	 * @throws ASCoDTException
	 * @throws MalformedURLException 
	 */
	public void createProject(IProject eclipseProject) throws ASCoDTException, MalformedURLException{
		Trace trace = new Trace( getClass().getName() );
		trace.in( "createProject(...)", eclipseProject.getName() );
		Project  newProject     = new Project(eclipseProject);

		_nameToProjectEntitiesMap.put(eclipseProject.getName(), newProject);
		_eclipseProjectToProjectMap.put(eclipseProject, newProject);
		newProject.buildProjectSources();

		_projects.add(newProject);
		_projectsContentProvider.inputChanged(_projectsContentProvider.getViewer(), null, null);

		notifyProjectChangedListeners();
		trace.out( "createProject(...)" );
	}

	/**
	 * constructs a new instance factory for the given project
	 * @param project reference to the project
	 * @param componentInterface
	 * @param target 
	 * @return
	 * @throws ASCoDTException 
	 */
	public InstanceFactory getNewInstanceFactory(Project project,String componentInterface, String target) throws ASCoDTException{
		return new InstanceFactory(project,componentInterface,target);
	}

	/**
	 * This method creates an ASCoDT project, and setups the main file structure and natures setup of the project.
	 * @param projectName name of the project to be created
	 * @param location location where to create the project
	 * @return the internal eclipse object representing the project
	 * @throws MalformedURLException 
	 */
	public void createProject(String projectName, URI location) throws ASCoDTException, CoreException, MalformedURLException {
		Trace trace = new Trace( getClass().getName() );
		trace.in( "createProject(...)", projectName );

		Assert.isNotNull(projectName);
		Assert.isTrue(projectName.trim().length() > 0);
		Assert.isTrue(!_nameToProjectEntitiesMap.containsKey(projectName));

		IProject eclipseProject = createBaseProject(projectName, location);
		addNature(eclipseProject);
		createProject(eclipseProject);



		// make sure that the ascodt perspective is opened
		PlatformUI.getWorkbench().showPerspective(ASCoDTPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		// make sure that the ascodt palette is opened
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = activeWindow.getActivePage();
		if (activeWindow!=null&&page != null) {
			page.showView(Palette.ID);
		}

		trace.out( "createProject(...)" );
	}




	/**
	 * Just do the basics: create a basic project.
	 *
	 * @param location
	 * @param projectName
	 * @throws CoreException 
	 */
	private static IProject createBaseProject(String projectName, URI location) throws CoreException {
		// it is acceptable to use the ResourcesPlugin class
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}

			desc.setLocationURI(projectLocation);
			newProject.create(desc, null);
			if (!newProject.isOpen()) {
				newProject.open(null);
			}

		}

		return newProject;
	}

	/**
	 * removes the project from the project builder
	 * @param project
	 */
	public void removeProject(IProject project){
		if(_eclipseProjectToProjectMap.get(project)!=null){
			_eclipseProjectToProjectMap.get(project).getStaticRepository().removeListener(_palette);
			_projects.remove(_eclipseProjectToProjectMap.get(project));

			if(_nameToProjectEntitiesMap.get(project.getName())!=null)
				_nameToProjectEntitiesMap.remove(project.getName());

			_eclipseProjectToProjectMap.remove(project);
			_projectsContentProvider.inputChanged(_projectsContentProvider.getViewer(), null, null);

			notifyProjectChangedListeners();
		}
	}


	/**
	 * setup the project nature, so that the ascodt project can be identified as such one. We add an additiona
	 * java nature to be able to use the build-in java compiler.
	 * @param project
	 * @throws CoreException
	 */
	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ASCoDTNature.ID)){
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 2];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ASCoDTNature.ID;
			newNatures[prevNatures.length+1] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

			ICommand[] newCommands = new ICommand[1];
			ICommand command = description.newCommand();
			command.setBuilderName(de.tum.ascodt.plugin.project.builders.ProjectBuilder.ID);
			newCommands[newCommands.length - 1] = command;
			description.setBuildSpec(newCommands);
			project.setDescription(description, null);


		}
	}


	public void registerProjectChangedListener(
			ProjectChangedListener listener) {
		_listeners.add(listener);

	}

	public void removeProjectChangedListener(
			ProjectChangedListener listener) {
		_listeners.remove(listener);

	}

	/**
	 * 
	 * @return all projects
	 */
	public Collection<Project> getProjects() {
		Assert.isNotNull(_projects);
		return _projects;
	}


	/**
	 * 
	 * @return a list of the projects identifiers
	 */
	public Set<String> getProjectsIdentifiers() {
		return _nameToProjectEntitiesMap.keySet();
	}

	/**
	 * notify all views that the project model has changed
	 */
	public void notifyProjectChangedListeners(){
		for(ProjectChangedListener listener:_listeners){
			listener.begin();
			for(Project project:this._nameToProjectEntitiesMap.values())
				listener.notify(project);
			listener.end();
		}
	}


	public IContentProvider getProjectsContentProvider() {
		return _projectsContentProvider;
	}


	public void setWorkbench(WorkbenchEditor workbenchEditor) {
		if(_workbench!=null&& workbenchEditor.equals(_workbench))
			return;
		_workbench=workbenchEditor;
		if(_palette!=null){
			_workbench.connectToPalette(_palette);
		}
	}


	public void setPalette(Palette palette) {
		_palette=palette;
		if(_workbench!=null){
			_workbench.connectToPalette(_palette);
		}
	}





}
