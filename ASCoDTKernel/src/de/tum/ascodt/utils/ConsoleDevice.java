package de.tum.ascodt.utils;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import de.tum.ascodt.plugin.utils.ConsoleStream;

public class ConsoleDevice {
	private ConsoleDevice() {
	}

	private static ConsoleDevice _singleton = new ConsoleDevice();

	public static ConsoleDevice getInstance() {
		return _singleton;
	}

	/**
	 * Find a console with given name. If the console doesn't exist a new 
	 * instance is created.
	 * 
	 * @param name
	 * @return
	 */
	public OutputDevice getConsole(String name) {
		if(ConsolePlugin.getDefault()!=null){	 
			IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] existing = conMan.getConsoles();
			for (int i = 0; i < existing.length; i++)
				if (name.equals(existing[i].getName()) && existing[i] instanceof MessageConsole){
					return (ConsoleStream) existing[i];
				}
		
			//no console found, so create a new one
			final ConsoleStream myConsole = new ConsoleStream(name);
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					myConsole.setWaterMarks(80000,100000);
				}
				
			});
			
			conMan.addConsoles(new IConsole[]{myConsole});
			return myConsole;
		}else
			return new StdOutputDevice();
	}
}
