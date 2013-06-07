package de.tum.ascodt.plugin.utils.exceptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.widgets.Display;

import de.tum.ascodt.utils.ConsoleDevice;

/**
 * Error Writer Device
 * 
 * The error writer device basically is a singleton (single point of contact) 
 * to write error messages. It forwards error messages to an ASCoDT console, 
 * and open a message box displaying the error. Furthermore, it logs the 
 * message into an output file.
 *  
 * @author Atanas Atanasov, Tobias Weinzierl
 */
public class ErrorWriterDevice {
	private static ErrorWriterDevice  _singleton = new ErrorWriterDevice();

	private FileWriter      _fileWriter;
	private BufferedWriter  _errorLog;
	private boolean 						_isTest;

	private ErrorWriterDevice() {
		try {
			File errorLogFile = new File("ErrorLog.log");
			if(errorLogFile.exists()) {
				errorLogFile.delete();
			}
			errorLogFile.createNewFile();
			_fileWriter  = new FileWriter(errorLogFile,true);
			_errorLog   = new BufferedWriter(_fileWriter);
			_isTest = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void enableTesting(){
		_isTest=true;
	}

	public void disableTesting(){
		_isTest=false;
	}

	private static String now() {
		final String DateFormat = "yyyy.MM.dd G 'at' hh:mm:ss z";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
		return sdf.format(cal.getTime());
	}


	static public ErrorWriterDevice getInstance() {
		return _singleton;
	}


	/**
	 * 
	 * @param fullQualifiedClassName Just insert getClass().getName() here 
	 * @param methodName
	 * @param e
	 */
	public void showError(String fullQualifiedClassName, String methodName, Throwable e ){
		displayErrorMessage( fullQualifiedClassName, methodName, ((e!=null)?e.getMessage() +"\n"+e.getCause():""));
		PrintWriter writer = new PrintWriter( _fileWriter );
		if(e!=null){
			e.printStackTrace( writer );
			e.printStackTrace();
		}
	}


	public void showError(String fullQualifiedClassName, String methodName, String message, Throwable e ){
		displayErrorMessage( fullQualifiedClassName, methodName, message + "\n" + ((e!=null)?e.getMessage() +"\n"+e.getCause():""));
		PrintWriter writer = new PrintWriter( _fileWriter );
		if(e!=null){
			e.printStackTrace( writer );
			e.printStackTrace();
		}
	}


	public void showError(String fullQualifiedClassName, String methodName, String message ){
		displayErrorMessage( fullQualifiedClassName, methodName, message );
	}


	private void displayErrorMessage(final String fullQualifiedClassName, final String methodName, final String errorMessage ){
		if(!_isTest)
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					final String outputText = fullQualifiedClassName + "." + methodName + "\n" + errorMessage ;
					org.eclipse.jface.dialogs.MessageDialog.openError(Display.getDefault().getActiveShell(),"ASCoDT Error" , outputText );

					try {
						_errorLog.write( now() + "\n" );
						_errorLog.write( outputText + "\n" );
						_errorLog.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}

					ConsoleDevice.getInstance().getConsole( "ASCoDT Error" ).println( outputText );
				}

			});

	}
}