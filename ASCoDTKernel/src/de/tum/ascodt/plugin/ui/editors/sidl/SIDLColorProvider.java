package de.tum.ascodt.plugin.ui.editors.sidl;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the SIDL editor
 */
public class SIDLColorProvider {

	public static final RGB MULTI_LINE_COMMENT= new RGB(128, 0, 0);
	public static final RGB SINGLE_LINE_COMMENT= new RGB(128, 128, 0);
	public static final RGB KEYWORD= new RGB(128, 0, 128);
	public static final RGB TYPE= new RGB(0, 0, 255);
	public static final RGB STRING= new RGB(0, 128, 0);
	public static final RGB DEFAULT= new RGB(0, 0, 0);
	public static final RGB JAVADOC_KEYWORD= new RGB(0, 128, 0);
	public static final RGB JAVADOC_TAG= new RGB(128, 128, 128);
	public static final RGB JAVADOC_LINK= new RGB(128, 128, 128);
	public static final RGB JAVADOC_DEFAULT= new RGB(0, 128, 128);

	protected Map<RGB,Color> fColorTable= new HashMap<RGB,Color>(10);

	/**
	 * Release all of the color resources held onto by the receiver.
	 */
	public void dispose() {
		Iterator<Color> e= fColorTable.values().iterator();
		while (e.hasNext())
			 ( e.next()).dispose();
	}

	/**
	 * Return the color that is stored in the color table under the given RGB
	 * value.
	 *
	 * @param rgb the RGB value
	 * @return the color stored in the color table for the given RGB value
	 */
	public Color getColor(RGB rgb) {
		Color color=  fColorTable.get(rgb);
		if (color == null) {
			color= new Color(Display.getDefault(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}

