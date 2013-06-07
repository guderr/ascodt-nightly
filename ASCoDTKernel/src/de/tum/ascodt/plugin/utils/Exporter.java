package de.tum.ascodt.plugin.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * 
 * @author Atanas Atanasov
 *
 * Implements a utility for exporting of compiled components. The utility takes all
 * classes, sidl files and resources relevant for the component and packs them into 
 * tarball archive. The archive can be imported later by ASCoDT though the import 
 * utility
 */
public class Exporter {
	
	
	/**
	 * packs the founded classes in jar file. The file paths are made relative to the project bin folder
	 * @param classes
	 * @param library
	 * @param projectLocation
	 * @throws IOException 
	 */
	private static void packToJar(Vector<File> classes, String componentInterface, String projectLocation) throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		File libraryFile=new File(componentInterface+".jar");
		if(!libraryFile.exists())
			libraryFile.createNewFile();
		JarOutputStream target = new JarOutputStream(new FileOutputStream(componentInterface+".jar"), manifest);
		for(File classFile:classes){
			String name=null;
			if(new Path(classFile.getPath()).toPortableString().contains(projectLocation))
				name=new Path(classFile.getPath()).toPortableString().substring(projectLocation.length()+1);
			
			JarEntry entry=new JarEntry(name);
			entry.setTime(classFile.lastModified());

			target.putNextEntry(entry);

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(classFile));
			writeEntry(target, in);
			in.close();
			target.closeEntry();
		}
		target.close();
	}
	
	/**
	 * writes an input stream to given outputstream
	 * @param out the ouputstream of the entry
	 * @param in inputstream of the file
	 * @throws IOException
	 */
	private static void writeEntry(OutputStream out, BufferedInputStream in)
	throws IOException {
		byte[] buffer = new byte[1024];
		while (true)
		{
			int count = in.read(buffer);
			if (count == -1)
				break;
			out.write(buffer, 0, count);
		}
	}
	
	
	/**
	 * writes an entry in zip file
	 * @param out
	 * @param file
	 * @param entryName 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void writeEntryToZipFile(ZipOutputStream out, File file, String entryName)
	throws IOException, FileNotFoundException {
		out.putNextEntry(new ZipEntry(entryName));
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		writeEntry(out, in);
		in.close();
		out.closeEntry();
	}

	
	/**
	 * export a bunch of files as zip archive
	 * @param files collection of files to be exported
	 * @param dependencies list of dependent sidl files
	 * @param destination path of the destination
	 * @param destination the destination of the zip file
	 * @throws IOException
	 */
	private static void exportAsZipArchive(
			Vector<File> classes,
			Vector<File> dependencies,
			String componentInterface,
			String destination,
			IProject project) throws IOException {
		
		packToJar(classes,componentInterface,project.getLocation().toPortableString());
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
		File jarLibrary=new File(componentInterface+".jar");
		File componentSourceFile = new File(project.getLocation().toPortableString()+"/sidl/"+componentInterface.substring(componentInterface.lastIndexOf(".")+1)+".sidl"); 
		
		writeEntryToZipFile(out, componentSourceFile,componentSourceFile.getName());
		writeEntryToZipFile(out, jarLibrary,jarLibrary.getName());
		for(File dependency:dependencies)
			writeEntryToZipFile(out, dependency,"includes/"+dependency.getName());
		out.close();
		jarLibrary.delete();
	}
	
	/**
	 * main method of the export utility. Exports the given component
	 * as archive 
	 * 
	 * @param componentInterface name of the component interface
	 * @param destination path where to store the exported binary
	 * @param project the project where the component was defined
	 * @throws ASCoDTException  
	 */
	public static void exportBinary(String componentInterface,String destination,IProject project) throws ASCoDTException {
		//gather all resources for this component
		//Vector<String> sidlFiles;
		Vector<File> classes=new Vector<File>();
		Vector<File> sidlDependencies=new Vector<File>();
		try {
			retrieveClasses(classes,componentInterface,project.getLocation().toPortableString());
			retrieveSIDLDependencies(ProjectBuilder.getInstance().getProject(project).getSIDLDependencies(),sidlDependencies);
			
			exportAsZipArchive(classes,sidlDependencies,componentInterface,destination,project);
		} catch (CoreException e) {
			throw new ASCoDTException(Exporter.class.getCanonicalName(),"exportBinary()",e.getLocalizedMessage(),e);
		} catch (IOException e) {
			throw new ASCoDTException(Exporter.class.getCanonicalName(),"exportBinary()",e.getLocalizedMessage(),e);
		}
	}
	
	/**
	 * retrieves the classes of one component:
	 * 1.implementation
	 * 2.abstract class
	 * 3.third-party classes
	 * @param classes the collection where the found classes are going to be stored
	 * @param componentName the name of the component 
	 * @param projectPath path to the project
	 */
	private static void retrieveClasses(Vector<File> classes, String componentName,String projectPath) {
		File srcDir=new File(projectPath+new Path("/bin").toPortableString());
		if(srcDir.exists())
			for(File file:srcDir.listFiles())
				retrieveFiles(projectPath,"class",classes,file,componentName);
	}
	
	/**
	 * retrieve a list of the dependent sidl files
	 * @param paths2SIDLDependencies collection with path of the sidl deps
	 * @param sidlDependencies file collection where to put the found files
	 */
	private static void retrieveSIDLDependencies(String [] paths2SIDLDependencies,Vector<File> sidlDependencies) {
		for(String path:paths2SIDLDependencies)
			sidlDependencies.add(new File(path));
	}

	/**
	 * a helper function which executes a match on namespace level
	 * @param file the file to be matched
	 * @param componentInterface the component to match with
	 * @return boolean value of the match
	 */
	private static boolean checkForNamespaceMatch(File file, String componentInterace){
		return new Path(file.getAbsolutePath()).toPortableString().replaceAll("/",".").contains(componentInterace.substring(0, componentInterace.lastIndexOf(".")-1));
		
	}
	/**
	 * retrieves files with given extension
	 * @param path the path where to search for files
	 * @param ext the extension of the file to match
	 * @param classes collection where to put the files
	 * @param file the current file or directory
	 * @param componentInterface name of the component
	 */
	private static void retrieveFiles(String path,String ext, Vector<File> classes,File file, String componentInterface){
		if(file.isFile()&&file.getName().contains("."+ext)&& checkForNamespaceMatch(file,componentInterface)){
			classes.add(file);
		}else if(file.isDirectory()){
			for(File child:file.listFiles())
				retrieveFiles(path,ext,classes,child,componentInterface);
		}
	}
	
	
	public void exportSource(String componentInterface){
		
	}
	
}
