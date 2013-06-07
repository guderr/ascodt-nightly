package de.tum.ascodt.plugin.ui.gef.editparts.policies;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import de.tum.ascodt.plugin.ui.gef.editparts.StickyNoteEditPart;

public class StickyNoteResizableEditPolicy extends ResizableEditPolicy
{
	public	StickyNoteResizableEditPolicy(){
		super();
		this.setDragAllowed(true);
	}
	/**
	 * Creates the figure used for feedback.
	 * @return the new feedback figure
	 */
	protected IFigure createDragSourceFeedbackFigure() {
		IFigure figure = createFigure((GraphicalEditPart)getHost(), null);

		figure.setBounds(getInitialFeedbackBounds());
		addFeedback(figure);
		return figure;
	}

	protected IFigure createFigure(GraphicalEditPart part, IFigure parent) {
		IFigure child = getCustomFeedbackFigure(part.getModel());

		if (parent != null)
			parent.add(child);
		EditPart note_parent=part.getParent();
		while(!(note_parent instanceof StickyNoteEditPart))
			note_parent=note_parent.getParent();
		Rectangle childBounds = null;
		childBounds = ((StickyNoteEditPart)note_parent).getFigure().getBounds().getCopy();

		IFigure walker = part.getFigure().getParent();

		while (walker != ((GraphicalEditPart)note_parent).getFigure()) {
			walker.translateToParent(childBounds);
			walker = walker.getParent();
		}

		child.setBounds(childBounds);

		return child;
	}

	protected IFigure getCustomFeedbackFigure(Object modelPart) {
		IFigure figure; 


		figure = new RectangleFigure();
		((RectangleFigure)figure).setXOR(true);
		((RectangleFigure)figure).setFill(true);
		//figure.setBackgroundColor(LogicColorConstants.ghostFillColor);
		figure.setForegroundColor(ColorConstants.black);


		return figure;
	}

	/**
	 * Returns the layer used for displaying feedback.
	 *  
	 * @return the feedback layer
	 */
	protected IFigure getFeedbackLayer() {
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#getInitialFeedbackBounds()
	 */
	protected Rectangle getInitialFeedbackBounds() {
		EditPart parent=this.getHost().getParent();
		while(!(parent instanceof StickyNoteEditPart))
			parent=parent.getParent();
		return ((AbstractGraphicalEditPart)parent).getFigure().getBounds();	
	}

}

