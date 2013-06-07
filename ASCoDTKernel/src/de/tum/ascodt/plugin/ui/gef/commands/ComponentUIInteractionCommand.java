package de.tum.ascodt.plugin.ui.gef.commands;

public class ComponentUIInteractionCommand extends ComponentInteractionCommand{
	public void execute(){
		this.source.setUIEnable((mode==1));
	}
}
