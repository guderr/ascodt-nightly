package de.tum.ascodt.plugin.repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

//import de.tum.ascodt.plugin.ASCoDTKernel;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;

/**
 * A classloader for the current project. It helps to load
 * the classes of the generated components. 
 * @author atanasoa
 *
 */
public class ClasspathRepository extends URLClassLoader {
	private IProject _project;
	private int _idCounter;
	//private HashMap<URL,Integer> _urls;
	public ClasspathRepository(IProject project, ClassLoader parent_loader){
		super(new URL[]{},parent_loader);
		this._project=project;
		this._idCounter=0;
		//this._urls=new HashMap<URL,Integer>();
		
	}

	protected void finalize(){
		System.out.println("removed classpath repository");
		//ProjectBuilder.getInstance().getProject(_project).setLoaderFlag(false);
	}

	public void clear(){
		//ProjectBuilder.getInstance().getProject(_project).setLoaderFlag(true);
	}
	/**
	 * Returns a new id
	 * @return 
	 */
	public int getNewId() {
		_idCounter++;
		return _idCounter-1;
	}

	/**
	 * adds a new url to the classloader
	 */
	public void addURL(URL newUrl){
		boolean included=false;
		for(URL url:getURLs()){
			if(url.equals(newUrl))
				included=true;
		}
		if(!included)
			super.addURL(newUrl);
	}

	/**
	 * searches for a given class in the collection of loaded classes
	 */
	@Override
	protected Class<?> findClass(String className){
		IJavaProject jProject=JavaCore.create(_project);

		try {
			for(IClasspathEntry entry:jProject.getRawClasspath()){
				if(entry.getEntryKind()==IClasspathEntry.CPE_LIBRARY)
					if(entry.getPath().toString().startsWith("/"+_project.getName()))
					{
						URL urlToAdd=new Path(_project.getLocation()+entry.getPath().toString().substring(_project.getName().length()+1)).toFile().toURI().toURL();
						//if(!_urls.containsKey(urlToAdd)){
							this.addURL(urlToAdd);
						//	_urls.put(urlToAdd, 0);
						//}
					}
					else if(!entry.getPath().toFile().toURI().toString().contains("vtk.jar")){
						URL urlToAdd=entry.getPath().toFile().toURI().toURL();
						//if(!_urls.containsKey(urlToAdd)){
							this.addURL(urlToAdd);
						//	_urls.put(urlToAdd, 0);
						//}
					}

			}
		} catch (JavaModelException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName() , "performFinish()", e);
		} catch (MalformedURLException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName() , "performFinish()", e);
		}
		Class<?> cl=null; 
		try {
			if((cl=super.findClass(className))!=null)
				return cl;


		} catch (Exception e) {

			ErrorWriterDevice.getInstance().showError( getClass().getName() , "findClass()", e);
		}
		return cl;
	}
	public boolean findCCAClass(final String name){
		return (this.findLoadedClass(name)!=null);
	}

}
