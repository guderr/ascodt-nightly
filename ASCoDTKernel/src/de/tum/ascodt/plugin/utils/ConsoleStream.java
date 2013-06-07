package de.tum.ascodt.plugin.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import de.tum.ascodt.utils.OutputDevice;


/**
 * A thread-safe print-stream for the ASCoDT console
 * 
 * Do not use this class directly but instead use the ConsoleDevice.
 * 
 * @author Atanas Atanasov, Tobias Weinzierl 
 */
public class ConsoleStream extends MessageConsole implements OutputDevice{
  /**
   * This is a queue filled with the messages that are to be displayed.
   */
	private ConcurrentLinkedQueue<String>   _consoleMessages;
	
	/**
	 * This is the thread permanently 
	 */
  private ConsolveStreamThread            _printStreamThread;
  
  
  public ConsoleStream(String name) {
    super(name, null, null, true);
    
    _consoleMessages   = new ConcurrentLinkedQueue <String>();
    _printStreamThread = new ConsolveStreamThread(_consoleMessages);
    _printStreamThread.start();
  }

	class ConsolveStreamThread extends Thread  {
		private ConcurrentLinkedQueue<String> _consoleMessages;
		private MessageConsoleStream          _consoleStream;
		 
		public ConsolveStreamThread(ConcurrentLinkedQueue<String> consoleMessages){
			_consoleMessages = consoleMessages;
			_consoleStream   = newMessageStream();
		}
		
		public void run(){
			while(true){
				if(_consoleMessages.isEmpty())
					synchronized(ConsoleStream.this){
						try {
							ConsoleStream.this.wait();
						} catch (InterruptedException e) {
							
						}
					}
				while (!_consoleMessages.isEmpty()){
				  _consoleStream.println(_consoleMessages.poll());
				 
				}
			}
		}
	}


	public void println(String line){
    _consoleMessages.add(line);
    synchronized(ConsoleStream.this){
    	ConsoleStream.this.notifyAll();
    }
	}
}
