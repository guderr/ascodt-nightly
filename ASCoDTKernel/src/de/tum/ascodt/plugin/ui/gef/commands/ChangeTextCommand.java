package de.tum.ascodt.plugin.ui.gef.commands;

import org.eclipse.gef.commands.Command;

import de.tum.ascodt.plugin.ui.gef.model.StickyNote;


public class ChangeTextCommand extends Command{

	private StickyNote note;
	private String newText, oldText;

	public ChangeTextCommand(StickyNote note, String newText) {
		super("Edit Comment");
		this.note = note;
		this.newText = newText;					
	}

	public boolean canExecute() {
		return note != null && newText != null;
	}

	public void execute() {
		oldText = note.getText();
		note.setText(newText);	
		note.changeText(oldText, newText);
	}

	public void undo() {
		note.setText(oldText);
	}

}
