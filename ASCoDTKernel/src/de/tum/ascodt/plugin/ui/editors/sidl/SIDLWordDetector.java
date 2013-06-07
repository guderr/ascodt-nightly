package de.tum.ascodt.plugin.ui.editors.sidl;

import org.eclipse.jface.text.rules.IWordDetector;


public class SIDLWordDetector implements IWordDetector {

	/* (non-Javadoc)
	 * Method declared on IWordDetector.
	 */
	public boolean isWordPart(char character) {
		return Character.isJavaIdentifierPart(character)||('-'==character);
	}

	/* (non-Javadoc)
	 * Method declared on IWordDetector.
	 */
	public boolean isWordStart(char character) {
		return Character.isJavaIdentifierStart(character);
	}


}
