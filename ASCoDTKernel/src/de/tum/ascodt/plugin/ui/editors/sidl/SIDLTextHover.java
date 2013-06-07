package de.tum.ascodt.plugin.ui.editors.sidl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;


public class SIDLTextHover  implements ITextHover {

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated As of 3.4, replaced by
	 *             {@link ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)}
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				if (hoverRegion.getLength() > -1)
					return textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
			} catch (BadLocationException x) {
			}
		}
		return SIDLEditorMessages.getString("JavaTextHover.emptySelection"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on ITextHover
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

	
}
