package de.tum.ascodt.plugin.ui.gef.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;

import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Diagram;

/**
 * Command for the creation of the Gef Component representation
 * @author atanasoa
 *
 */
public class CreateComponentCommand extends Command {

	/** The new component. */ 
	private Component _component;
	/** The parent of the component-diagram*/
	private Diagram _parent;
	private Rectangle _bounds;
	private CreateRequest _request;
	public CreateComponentCommand(Component componentGefModel,
			Diagram parent, Rectangle bounds) {
		this._component = componentGefModel;
		this._parent = parent;
		this._bounds = bounds;
	}

	public CreateComponentCommand(CreateRequest request, Diagram parent,
			Rectangle bounds) {
		this._request=request;
		this._parent = parent;
		this._bounds = bounds;
		setLabel("component creation");
	}

	/**
	 * Can execute if all the necessary information has been provided. 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		return _request != null && _parent != null && _bounds != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		_component=(Component)_request.getNewObject();
		if(_component!=null){
			_component.setLocation(_bounds.getLocation());
			_request=null;

			Dimension size = _bounds.getSize();
			if (size.width > 0 && size.height > 0)
				_component.setSize(size);
			redo();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		_parent.addChild(_component);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		_parent.removeChild(_component);
	}

}
