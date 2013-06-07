/**
 * 
 */
package de.tum.ascodt.plugin.extensions;

import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;

import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * @author Atanas Atanasov
 * A simple interface for extending existing ascodt project
 */
public interface Project {
	public static String ID=Project.class.getCanonicalName();
	
	/**
	 * use the method to add addiotion classpath entries to the project
	 * @param classpathEntries
	 */
	void addClasspathEntries(Set<IClasspathEntry> classpathEntries) throws ASCoDTException;
}
