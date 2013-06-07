package de.tum.ascodt.plugin.ui.editors.gef;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import de.tum.ascodt.plugin.ui.gef.editparts.ComponentEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.ConnectionEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.DiagramEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.LinkEditPart;
import de.tum.ascodt.plugin.ui.gef.editparts.StickyNoteEditPart;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Connection;
import de.tum.ascodt.plugin.ui.gef.model.Link;
import de.tum.ascodt.plugin.ui.gef.model.StickyNote;

/**
 * This factory creates the edit parts(controllers) for the gef model 
 * classes
 * @author Atanas Atanasov
 *
 */
public class EditPartsFactory implements EditPartFactory {


	/**
	 * creates an edit part for given model
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		assert(model!=null);
		// get EditPart for model element
		EditPart part = getPartForElement(model);
		// store model element in EditPart
		part.setModel(model);
		return part;
	}
	/**
	 * Maps an object to an EditPart. 
	 * @throws RuntimeException if no match was found (programming error)
	 */
	private EditPart getPartForElement(Object modelElement) {		
		if (modelElement instanceof Diagram) {
			return new DiagramEditPart();
		}
		if(modelElement instanceof Component)
			return new ComponentEditPart();
		if(modelElement instanceof Connection)
			return new ConnectionEditPart();
		if(modelElement instanceof StickyNote)
			return new StickyNoteEditPart();
		if(modelElement instanceof Link)
			return new LinkEditPart();
		return null;
	}

}
