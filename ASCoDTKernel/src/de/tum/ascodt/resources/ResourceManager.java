package de.tum.ascodt.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import de.tum.ascodt.plugin.ASCoDTKernel;

/**
 * through this class the plugin has access to the internal resource file like icons, templates, etc.. 
 * @author atanasoa
 *
 */
public class ResourceManager {
	
	/**
	 * returns a resource located inside of the plugin 
	 * @param pathToResource relative path to the resource to be found (example: de/tum/ascodt/resources/)
	 * @return
	 * @throws IOException
	 */
	public static InputStream getResourceAsStream(String pathToResource,String plugin) throws IOException{
		return ResourceManager.class.getResourceAsStream(pathToResource);
		
	}
	public static URL getResourceAsPath(String pathToResource,String plugin) throws IOException{
		URL url=null;
		boolean hasBinFolder=false;
		if(Platform.getBundle(plugin)!=null){
			if(FileLocator.find(Platform.getBundle(plugin), new Path("debug"),null)!=null)
				hasBinFolder=true;
			url = FileLocator.toFileURL(FileLocator.find(Platform.getBundle(plugin), new Path(((hasBinFolder)?"debug/":"")+pathToResource),null));
			
		}
		return url;
	}
}
