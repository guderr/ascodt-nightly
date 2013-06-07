package de.tum.ascodt.plugin.ui.gef.commands;

import org.eclipse.gef.commands.Command;

import de.tum.ascodt.plugin.ui.gef.model.Component;

public class ResetReferenceForComponentCommand extends Command {

	private Component source;
	private String reference, oldReference;
	

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute()
	{
		source.setReference(reference);
		
	}

	/**
	 * @return whether we can apply changes
	 */
	public boolean canExecute()
	{
		if (reference != null)
		{
			return true;
		}
		else
		{
			reference = oldReference;
			return false;
		}
	}

	/**
	 * Sets the new reference name
	 * 
	 * @param string
	 *            the new name
	 */
	public void setReference(String string)
	{
		
		if (string != null)
		{
			int colonIndex = string.indexOf(':');
			if (colonIndex >= 0)
			{
				reference = string.substring(0, colonIndex);
				
			}
		}
		
	}

	/**
	 * Sets the old reference name
	 * 
	 * @param string
	 *            the old name
	 */
	public void setOldReference(String string)
	{
		oldReference = string;
	}

	/**
	 * Sets the source Component
	 * 
	 * @param column
	 *            the source Componet
	 */
	public void setSource(Component component)
	{
		source = component;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo()
	{
		source.setReference(oldReference);
		
	}

}
