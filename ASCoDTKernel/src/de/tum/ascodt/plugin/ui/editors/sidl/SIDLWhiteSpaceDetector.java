package de.tum.ascodt.plugin.ui.editors.sidl;

import org.eclipse.jface.text.rules.IWhitespaceDetector;


public class SIDLWhiteSpaceDetector implements IWhitespaceDetector {

	/* (non-Javadoc)
	 * Method declared on IWhitespaceDetector
	 */
	public boolean isWhitespace(char character) {
		return Character.isWhitespace(character);
	}
}
