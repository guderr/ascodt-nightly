package de.tum.ascodt.plugin.ui.gef.directedit;

import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

/**
 * A CellEditorLocator for a specified label
 * 
 * @author Phil Zoio
 */
public class LabelCellEditorLocator implements CellEditorLocator
{

	private Label label;

	private IFigure fig;	

	/**
	 * Creates a new CellEditorLocator for the given Label
	 * 
	 * @param label
	 *            the Label
	 */
	public LabelCellEditorLocator(Label label)
	{
		setLabel(label);
	}

	public LabelCellEditorLocator(IFigure figure) {
		fig = figure;
	}

	/**
	 * expands the size of the control by 1 pixel in each direction
	 */
	public void relocate(CellEditor celleditor)
	{
		Text text = (Text) celleditor.getControl();

		Point pref = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (label!=null){
			Rectangle rect = label.getTextBounds().getCopy();
			label.translateToAbsolute(rect);
			if (text.getCharCount() > 1)
				text.setBounds(rect.x - 1, rect.y - 1, pref.x + 1, pref.y + 1);
			else
				text.setBounds(rect.x - 1, rect.y - 1, pref.y + 1, pref.y + 1);
		}

		if (fig!=null){
			Rectangle rect = fig.getClientArea(Rectangle.SINGLETON);
			if (fig instanceof Label)
				rect = ((Label)fig).getTextBounds().intersect(rect);
			fig.translateToAbsolute(rect);

			org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(0, 0, 0, 0);
			rect.translate(trim.x, trim.y);
			rect.width += trim.width;
			rect.height += trim.height;

			text.setBounds(rect.x, rect.y, rect.width, rect.height);
		}

	}

	/**
	 * Returns the Label figure.
	 * 
	 * @return the Label
	 */
	protected Label getLabel()
	{
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * @param label
	 *            The label to set
	 */
	protected void setLabel(Label label)
	{
		this.label = label;
	}

}

