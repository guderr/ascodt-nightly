/**
 * 
 */
package de.tum.ascodt.plugin.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;

import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * @author Atanas Atanasov
 * 
 * The main import utility used to import zipped components
 * into the pallete.
 *
 */
public class Importer {

	/**
	 * import a component into the palette
	 * @param sourcePath the path of the component
	 * @param project the current project
	 * @throws ASCoDTException 
	 */
	public static void importBinary(String sourcePath,IProject project) throws ASCoDTException{
		extract(sourcePath,project.getLocation().toPortableString()+"/imports",project);
		compileSIDLFiles(sourcePath,project.getLocation().toPortableString()+"/imports",project);
	}
	
	
	/**
	 * extract the content of the ascodt-component archive in the imports folder of the project
	 * @param sourcePath the path of component to be imported
	 * @param targetPath the path where to extract the component
	 * @param project the current project
	 * @throws IOException 
	 */
	private static void extract(String sourcePath,String targetPath,IProject project) throws ASCoDTException{
		File f=new File(sourcePath);
		ZipFile zipFile=null;
		if(f.exists()){
			try {
				zipFile=new ZipFile(f);
				Enumeration<? extends ZipEntry> entries=zipFile.entries();

				File destinationDirectory=new File(targetPath+File.separatorChar+f.getName().substring(0,f.getName().lastIndexOf(".")));
				File nativeDirectory=new File(project.getLocation().toPortableString()+"/native");
				if(destinationDirectory.exists()){
					//TODO deleteDirectory(directory);
					destinationDirectory.mkdirs();
				}

				while(entries.hasMoreElements()){
					final ZipEntry entry = (ZipEntry)entries.nextElement();
					final File file = new File(destinationDirectory, entry.getName());
					final File native_file = new File(nativeDirectory, entry.getName());
					
					if (entry.isDirectory()) {
						file.mkdirs();
					} else {
						file.getParentFile().mkdirs();
						if(entry.getName().endsWith(".so")||entry.getName().endsWith(".dll"))
							copyInputStream(zipFile.getInputStream(entry),
									new BufferedOutputStream(new FileOutputStream(native_file)));
						else	
							copyInputStream(zipFile.getInputStream(entry),
								new BufferedOutputStream(new FileOutputStream(file)));


					}

				}
				zipFile.close();
			} catch (FileNotFoundException e) {
				throw new ASCoDTException(Importer.class.getCanonicalName(),"extract()","Importer File not found error:"+e.getMessage(),e);
			} catch (IOException e) {
				throw new ASCoDTException(Importer.class.getCanonicalName(),"extract()","Importer IO error:"+e.getMessage(),e);
			}
		}
	}

	/**
	 * copies an input stream on given output stream
	 * @param inputStream the source input stream
	 * @param bufferedOutputStream the output stream
	 * @throws IOException
	 */
	private static void copyInputStream(InputStream inputStream,
			BufferedOutputStream bufferedOutputStream) throws IOException{
		byte[] buffer = new byte[1024];
		int len;

		while((len = inputStream.read(buffer)) >0)
			bufferedOutputStream.write(buffer, 0, len);

		inputStream.close();
		bufferedOutputStream.close();
	}

	/**
	 * A recursive function to scan a folder for all sidl resources and compile them
	 * @param currentFile
	 * @param project
	 * @throws ASCoDTException 
	 * @throws  
	 */
	private static void compileSIDLFiles(File currentFile,IProject project) throws ASCoDTException{
		if(currentFile.isDirectory())
			for(File child:currentFile.listFiles())
				compileSIDLFiles(child,project);
		else if(currentFile.getName().contains(".sidl")){
			//de.tum.ascodt.plugin.project.builders.ProjectBuilder.buildSymbolTableForSIDLResource(currentFile.getAbsolutePath(),project);
		
		}	
			
	}
	/**
	 * Compiles all sidl files and tests the validity against the project symbol table
	 * @param sourcePath  path to the component file to be imported
	 * @param destination the destination folder
	 * @throws ASCoDTException 
	 */
	private static void compileSIDLFiles(String sourcePath,String destination,IProject project) throws ASCoDTException{
		File f=new File(sourcePath);
		File directory=new File(destination+File.separatorChar+f.getName().substring(0,f.getName().lastIndexOf(".")));
		File includesDirectory=new File(destination+File.separatorChar+f.getName().substring(0,f.getName().lastIndexOf("."))+File.separatorChar+"includes");
		if(directory.exists()){
				if(includesDirectory.exists())
			compileSIDLFiles(includesDirectory,project);
			for(File child:directory.listFiles())
				if(child.getName().contains(".sidl"))
					compileSIDLFiles(child,project);
			de.tum.ascodt.plugin.project.Project proj=de.tum.ascodt.plugin.project.ProjectBuilder.getInstance().getProject(project);
			proj.compileComponents();
			de.tum.ascodt.plugin.project.ProjectBuilder.getInstance().notifyProjectChangedListeners();
			
		}
		
	}


}
