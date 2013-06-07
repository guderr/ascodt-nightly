package de.tum.ascodt.utils.exceptions;


/**
 * ASCoDT Exception
 * 
 * Whenever we encounter an exception that could be handled locally to some 
 * extend but causes major problems, we catch this exception and wrap it 
 * into an ASCoDT Exception.   
 * 
 * @author Tobias Weinzierl
 *
 */
public class ASCoDTException extends java.lang.Exception {
  private String     _className;
  private String     _methodName;
  private Throwable  _cause;
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param className   Class name of the first class that has catched the orginal exception. Usually, you can simply write down getClass().getName().
   * @param methodName  Method name of the first class that has catched the orginal exception.
   * @param message     Short description, in particular of the implication of the error, i.e. something like 'cannot create project'
   * @param cause       Exceptions wrapped by this exception if there is any. Otherwise null.
   */
  public ASCoDTException(String className, String methodName, String message, Throwable cause ) {
    super( message );
    _className  = className;
    _methodName = methodName;
    _cause      = cause;
  }
  
  public Throwable getCause() {
    return _cause;
  }

  public String getClassName() {
    return _className;
  }
  
  public String getMethodName() {
    return _methodName;
  }
}
