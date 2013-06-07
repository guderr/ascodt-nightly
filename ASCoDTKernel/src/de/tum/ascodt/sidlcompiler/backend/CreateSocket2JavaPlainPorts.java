/**
 * 
 */
package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.repository.entities.NativeComponent;
import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * @author Atanas Atanasov
 *
 */
public class CreateSocket2JavaPlainPorts extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace(CreateSocket2JavaPlainPorts.class.getCanonicalName() );
	
	private java.util.Stack< TemplateFile >   _templateJava;
	private java.util.Stack< TemplateFile >   _templateJavaOperations;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;

	private boolean _generateSuperport;

	private String _operation_switch;

	private HashMap<String,Integer>			  _operationsMap;

	private String _fullQualifiedPortName;
	public CreateSocket2JavaPlainPorts(
			SymbolTable symbolTable,
			URL destinationDirectory,
			String[] namespace,
			HashMap<String,Integer>	operationsMap){
		_templateJava         		   = new java.util.Stack< TemplateFile >();
		_templateJavaOperations 		 = new java.util.Stack< TemplateFile >();
		_destinationDirectory        = destinationDirectory;
		_namespace                   = namespace;
		_symbolTable                 = symbolTable;
		_generateSuperport=false;
		_operation_switch="";
		_operationsMap = operationsMap ;
		
	}
	
	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperport){
				String  portName              = node.getName().getText();
				
				_fullQualifiedPortName = _symbolTable.getScope(node).getFullQualifiedName(portName);
				
				String templateFileJava                  = "java-port-socket2java-plain-port.template";
				
				String fullQualifiedComponentName    = _symbolTable.getScope(node).getFullQualifiedName(portName);
				String destinationFileJava           = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + "Socket2JavaPlainPort.java";
				
				_templateJava.push( 
						new TemplateFile( templateFileJava, destinationFileJava, _namespace, TemplateFile.getLanguageConfigurationForJava() ,true)
						);
				
				addMappingsJava(portName);
				
				//_templateJava.peek().open();
				
			}
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}

	

	/**
	 * @param portName
	 */
	public void addMappingsJava(String portName) {
		_templateJava.peek().addMapping("__PORT_NAME__", portName);
		_templateJava.peek().addMapping("__NATIVE_COMPONENT__", NativeComponent.class.getCanonicalName());
	}
	
	
	
	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateJava.size()==1 );
		if(!_generateSuperport){
			try {
				_templateJava.peek().addMapping("__METHODS_SWITCH__", _operation_switch);
				_templateJava.peek().open();
				while(!_templateJavaOperations.isEmpty()){
					_templateJavaOperations.peek().open();
					_templateJavaOperations.peek().close();
					
					_templateJavaOperations.pop();
					
				}
				_templateJava.peek().close();
			}
			catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}

			_templateJava.pop();
		}
	}
	
	public void inAUserDefinedType(AUserDefinedType node) {

		String fullQualifiedSymbol = Scope.getSymbol(node);
		AInterfacePackageElement interfaceDefintion=_symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
		if(interfaceDefintion!=null){
			_generateSuperport=true;
			interfaceDefintion.apply(this);
			_generateSuperport=false;
		}
	}
	/**
	 * We create the one operation belonging to this port operation.
	 */
	public void inAOperation(AOperation node) {
		_trace.in( "inAOperation(...)" );
		try {
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );

			

			String templateFile = "java-port-socket2java-operation-plain-java-implementation.template";
				
			TemplateFile templateJava = new TemplateFile( _templateJava.peek(), templateFile );
			
			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );
			templateJava.addMapping("__SOCKET_PULL__", parameterList.pullInFromSocketForJava());
			templateJava.addMapping("__SOCKET_PUSH__", parameterList.pushOutToSocketFromJava2Cxx());
			templateJava.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			_operation_switch+="if(methodId=="+(+_operationsMap.get(_fullQualifiedPortName+""+node.getName().getText()))+")\n";
			_operation_switch+="\t\t\tinvoke_"+node.getName().getText()+"();\n";			
			templateJava.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			templateJava.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJava() );
			
			_templateJavaOperations.add(templateJava);
					  
			//templateJava.open();
			//templateJava.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}


