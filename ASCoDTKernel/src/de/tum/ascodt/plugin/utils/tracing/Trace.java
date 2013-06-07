package de.tum.ascodt.plugin.utils.tracing;

import java.io.Serializable;

public class Trace implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String  _className;

	/**
	 * 
	 * @param Typically, just insert getClass().getName() here.
	 */
	public Trace( String className ) {
		_className = className;
	}

	public void in( String methodName ) {
		TraceDevice.getInstance().traceIn(_className, methodName, "" );
	}

	public void in( String methodName, String[] parameter ) {
		String message  = "parameter=String[" + parameter.length + "]{ ";
		for( int i=0; i<parameter.length; i++) message += parameter[i] + " ";
		message += "}";
		TraceDevice.getInstance().traceIn(_className, methodName, message );
	}

	public void in( String methodName, String parameter ) {
		TraceDevice.getInstance().traceIn(_className, methodName, "parameter=" + parameter );
	}

	public void in( String methodName, String parameter0, String parameter1 ) {
		TraceDevice.getInstance().traceIn(_className, methodName, "parameter0=" + parameter0 +", parameter1=" + parameter1 );
	}

	public void in( String methodName, String parameter0, String[] parameter1 ) {
		String message  = "parameter0=" + parameter0 ;
		message += ", parameter1=String[" + parameter1.length + "]{ ";
		for( int i=0; i<parameter1.length; i++) message += parameter1[i] + " ";
		message += "}";
		TraceDevice.getInstance().traceIn(_className, methodName, message );
	}

	public void in( String methodName, String parameter0, String parameter1, String parameter2 ) {
		TraceDevice.getInstance().traceIn(_className, methodName, "parameter0=" + parameter0 +", parameter1=" + parameter1 +", parameter2=" + parameter2 );
	}

	public void in( String methodName, String parameter0, String parameter1, String[] parameter2 ) {
		String message  = "parameter0=" + parameter0 +", parameter1=" + parameter1;
		message += ", parameter2=String[" + parameter2.length + "]{ ";
		for( int i=0; i<parameter2.length; i++) message += parameter2[i] + " ";
		message += "}";
		TraceDevice.getInstance().traceIn(_className, methodName, message );
	}

	public void out( String methodName ) {
		TraceDevice.getInstance().traceOut(_className, methodName, "" );
	}

	public void out( String methodName, String result ) {
		TraceDevice.getInstance().traceOut(_className, methodName, result );
	}

	public void out( String methodName, boolean result ) {
		TraceDevice.getInstance().traceOut(_className, methodName, Boolean.toString(result) );
	}

	public void debug( String methodName, String message ) {
		TraceDevice.getInstance().debug(_className, methodName, message );
	}

}
