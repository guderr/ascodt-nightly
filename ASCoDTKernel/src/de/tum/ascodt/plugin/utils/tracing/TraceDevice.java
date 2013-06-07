package de.tum.ascodt.plugin.utils.tracing;


import de.tum.ascodt.utils.ConsoleDevice;


/**
 * 
 * @author Tobias Weinzierl
 */
public class TraceDevice {
  private static TraceDevice  _singleton = new TraceDevice();
  private static String       _traceConsolveIdentifier = "ASCoDT Trace";
  
  protected  static TraceDevice getInstance() {
    return _singleton;
  }
  
  private TraceDevice() {
  }
  
  private boolean shallTrace(String className, String methodName) {
    return true;
  }
  
  protected void traceIn( String className, String methodName, String message ) {
    if (shallTrace(className, methodName)) {
      writeStream("in", className, methodName, message );
    }
  }

  protected void traceOut( String className, String methodName, String message ) {
    if (shallTrace(className, methodName)) {
      writeStream("out", className, methodName, message );
    }
  }

  protected void debug( String className, String methodName, String message ) {
    if (shallTrace(className, methodName)) {
      writeStream("debug", className, methodName, message );
    }
  }

  private void writeStream( String prefix, String className, String methodName, String message ) {
    ConsoleDevice.getInstance().getConsole( _traceConsolveIdentifier ).println( prefix + "\t" + className + "\t" + methodName + "\t" + message);
  }
}
