package de.tum.ascodt.plugin.ui.gef.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;


/**
 * This class implements the figures for ports
 * @author atanasoa
 *
 */
public class PortAnchor extends AbstractConnectionAnchor{
	private Point location;
	private ComponentBorder border;
	public PortAnchor(ComponentBorder border,IFigure owner,Point location) {
		super(owner);
		this.location=location;
		this.border=border;
	}

	@Override
	public Point getLocation(Point reference) {
		Point p=location.getCopy();
		getOwner().translateToAbsolute(p);
		return p;
	}

	public void setLocation(int x, int y) {
		location.x=x;
		location.y=y;
	}

	public void ancestorMoved(IFigure figure) {
		if (!(figure instanceof ComponentFigure))
			return;
		
		border.invalidateConnectors(figure.getBounds().getCopy());
		super.ancestorMoved(figure);
	}


}
