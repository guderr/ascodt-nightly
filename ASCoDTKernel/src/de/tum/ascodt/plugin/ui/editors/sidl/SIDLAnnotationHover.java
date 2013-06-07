package de.tum.ascodt.plugin.ui.editors.sidl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;


public class SIDLAnnotationHover implements IAnnotationHover {

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IDocument document= sourceViewer.getDocument();

		try {
			IRegion info= document.getLineInformation(lineNumber);
			return document.get(info.getOffset(), info.getLength());
		} catch (BadLocationException x) {
		}

		return null;
	}

}
