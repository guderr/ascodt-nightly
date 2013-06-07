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
public class CreateNative2SocketPlainPorts extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace(CreateNative2SocketPlainPorts.class.getCanonicalName() );
	
	private java.util.Stack< TemplateFile >   _templateCxxFilesHeader;
	private java.util.Stack< TemplateFile >   _templateCxxFilesImplementation;
	private java.util.Stack< TemplateFile >   _templateCFilesHeader;
	private java.util.Stack< TemplateFile >   _templateCFilesImplementation;
	private java.util.Stack< TemplateFile >   _templateFPort;
	private java.util.Stack< TemplateFile >   _templateFPortProxy;
	private java.util.Stack< TemplateFile >   _templateFPortOperations;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;

	private boolean _generateSuperport;
	
	private String _operations;

	private HashMap<String, Integer> _operationsMap;

	private String _fullQualifiedComponentName;

	
	public CreateNative2SocketPlainPorts(
			SymbolTable symbolTable,
			URL destinationDirectory,
			String[] namespace,
			HashMap<String,Integer> operationsMap){
		_templateCxxFilesHeader         = new java.util.Stack< TemplateFile >();
		_templateCxxFilesImplementation = new java.util.Stack< TemplateFile >();
		_templateCFilesHeader         = new java.util.Stack< TemplateFile >();
		_templateCFilesImplementation = new java.util.Stack< TemplateFile >();
		_templateFPort = new java.util.Stack< TemplateFile >();
		_templateFPortProxy = new java.util.Stack< TemplateFile >();
		_templateFPortOperations = new java.util.Stack< TemplateFile >();
		_destinationDirectory        = destinationDirectory;
		_namespace                   = namespace;
		_symbolTable                 = symbolTable;
		_generateSuperport=false;
		_operations="";
		_operationsMap=operationsMap;
	}
	
	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperport){
				String  portName              = node.getName().getText();
				
				String fullQualifiedPortName													 = _symbolTable.getScope(node).getFullQualifiedName(portName) ;
				
				String templateCxxFileHeader					   = "cxx-port-native2socket-plain-header.template";
				String templateCxxFileImplementation	   = "cxx-port-native2socket-plain-implementation.template";
				String templateCFileHeader					   = "c-port-native2socket-plain-header.template";
				String templateCFileImplementation	   = "c-port-native2socket-plain-implementation.template";
				String templateFPort					   = "fortran-port-f2native-plain-port-implementation.template";
				String templateFPortProxy	   = "fortran-port-f2native-proxy-plain-port-implementation.template";
				
				_fullQualifiedComponentName = _symbolTable.getScope(node).getFullQualifiedName(portName);
				String destinationCxxFileHeader         = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "Cxx2SocketPlainPort.h";
				String destinationCxxFileImplementation = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "Cxx2SocketPlainPort.cpp";
				String destinationCFileHeader         = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "C2CxxSocketPlainPort.h";
				String destinationCFileImplementation = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "C2CxxSocketPlainPort.cpp";
				String destinationFPort         = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "2SocketPlainPort.f90";
				String destinationFProtProxy = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "2SocketPlainPortProxy.f90";
				
				_templateCxxFilesHeader.push( 
						new TemplateFile( templateCxxFileHeader, destinationCxxFileHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				_templateCxxFilesImplementation.push( 
						new TemplateFile( templateCxxFileImplementation, destinationCxxFileImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				_templateCFilesHeader.push( 
						new TemplateFile( templateCFileHeader, destinationCFileHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				_templateCFilesImplementation.push( 
						new TemplateFile( templateCFileImplementation, destinationCFileImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				
				_templateFPort.push( 
						new TemplateFile( templateFPort, destinationFPort, _namespace, TemplateFile.getLanguageConfigurationForFortran() ,true)
						);
				_templateFPortProxy.push( 
						new TemplateFile( templateFPortProxy, destinationFProtProxy, _namespace, TemplateFile.getLanguageConfigurationForFortran() ,true)
						);
				
				addMappingCxxHeader(portName, fullQualifiedPortName);
				addMappingCxxImplementation(portName, fullQualifiedPortName);
				addMappingCHeader(portName, fullQualifiedPortName);
				addMappingCImplementation(portName, fullQualifiedPortName);
				addMappingFPort(portName, fullQualifiedPortName);
				addMappingFPortProxy(portName, fullQualifiedPortName);
				_templateCxxFilesHeader.peek().open();
				_templateCxxFilesImplementation.peek().open();
				_templateCFilesHeader.peek().open();
				_templateCFilesImplementation.peek().open();
				
				_templateFPortProxy.peek().open();
			}
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}

	private void addMappingFPortProxy(String portName,
			String fullQualifiedPortName) {
		_templateFPort.peek().addMapping("__PORT_NAME__", portName);
		_templateFPort.peek().addMapping("__C_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toLowerCase());
	}

	private void addMappingFPort(String portName, String fullQualifiedPortName) {
		_templateFPortProxy.peek().addMapping("__C_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toLowerCase());
	}

	/**
	 * @param portName
	 * @param fullQualifiedPortName
	 * @throws ASCoDTException
	 */
	public void addMappingCxxHeader(String portName, String fullQualifiedPortName)
			throws ASCoDTException {
		_templateCxxFilesHeader.peek().addMapping("__PORT_NAME__", portName);
		_templateCxxFilesHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateCxxFilesHeader.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateCxxFilesHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateCxxFilesHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
	}
	
	/**
	 * @param portName
	 * @param fullQualifiedPortName
	 * @throws ASCoDTException
	 */
	public void addMappingCHeader(String portName, String fullQualifiedPortName)
			throws ASCoDTException {
		//_templateCFilesHeader.peek().addMapping("__PORT_NAME__", portName);
		_templateCFilesHeader.peek().addMapping( "__INCLUDE_GUARD_C_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateCFilesHeader.peek().addMapping( "__C_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toLowerCase());
		//_templateCFilesHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		
	}

	
	/**
	 * @param portType
	 * @param fullQualifiedPortName
	 */
	public void addMappingCxxImplementation(String portType,
			String fullQualifiedPortName) {
		_templateCxxFilesImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateCxxFilesImplementation.peek().addMapping("__PORT_NAME__", portType);
		_templateCxxFilesImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		//_templateCxxFilesImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
	}

	
	/**
	 * @param portType
	 * @param fullQualifiedPortName
	 */
	public void addMappingCImplementation(String portType,
			String fullQualifiedPortName) {
		//TODO static 
		_templateCFilesImplementation.peek().addMapping( "__C_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toLowerCase());
		_templateCFilesImplementation.peek().addMapping( "__CXX_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateCFilesImplementation.peek().addMapping("__PORT_NAME__", portType);
		_templateCFilesImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		
		//_templateCFilesImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
	}

	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateCxxFilesHeader.size()==1 );
		if(!_generateSuperport){
			try {

				_templateCxxFilesHeader.peek().close();
				_templateCxxFilesImplementation.peek().close();

				_templateCFilesHeader.peek().close();
				_templateCFilesImplementation.peek().close();
				_templateFPort.peek().addMapping("__OPERATIONS__",_operations);
				_templateFPort.peek().open();
				while(!_templateFPortOperations.isEmpty()){
					_templateFPortOperations.peek().open();
					_templateFPortOperations.peek().close();
					_templateFPortOperations.pop();
				}
				_templateFPort.peek().close();
				_templateFPortProxy.peek().close();
			}
			catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}

			_templateCxxFilesHeader.pop();
			_templateCxxFilesImplementation.pop();

			_templateCFilesHeader.pop();
			_templateCFilesImplementation.pop();
			_templateFPort.pop();
			_templateFPortProxy.pop();
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
//
//			
//
			String templateCxxFileHeader = "cxx-port-native-operation-plain-header.template";
			String templateCxxFileImplementation = "cxx-port-native2socket-operation-plain-implementation.template";
			String templateCFileHeader = "c-port-native-operation-plain-header.template";
			String templateCFileImplementation = "c-port-native2socket-operation-plain-implementation.template";
			String templateFPortOperationFileName = "fortran-port-f2native-plain-port-operation-implementation.template";
			String templateFPortProxyOperationFileName = "fortran-port-f2native-proxy-plain-port-operation-implementation.template";
			
//			
//			
			TemplateFile templateCxxOperationHeader = new TemplateFile( _templateCxxFilesHeader.peek(), templateCxxFileHeader );
			TemplateFile templateCxxImplementation = new TemplateFile( _templateCxxFilesImplementation.peek(), templateCxxFileImplementation );
			TemplateFile templateCOperationHeader = new TemplateFile( _templateCFilesHeader.peek(), templateCFileHeader );
			TemplateFile templateCImplementation = new TemplateFile( _templateCFilesImplementation.peek(), templateCFileImplementation );
			TemplateFile templateFPortOperation= new TemplateFile( _templateFPort.peek(), templateFPortOperationFileName);
			TemplateFile templateFPortProxyOperation= new TemplateFile( _templateFPortProxy.peek(), templateFPortProxyOperationFileName );
			_templateFPortOperations.add(templateFPortOperation);
			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );
//
			templateCxxOperationHeader.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateCxxOperationHeader.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			templateFPortOperation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateFPortProxyOperation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateFPortOperation.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInF(true));
			templateFPortOperation.addMapping("__OPERATION_PARAMETERS_TYPES_LIST__", parameterList.getParameterListTypesForF(true));
			templateFPortProxyOperation.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInF(true));
			templateFPortProxyOperation.addMapping("__OPERATION_PARAMETERS_TYPES_LIST__", parameterList.getParameterListTypesForFCBindedToC(true));
			templateFPortOperation.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__", parameterList.getFunctionCallListInFClient(false));
			
			templateCOperationHeader.addMapping( "__OPERATION_NAME__" , node.getName().getText().toLowerCase() );
			String parameters=parameterList.getParameterListInF2Cxx();
			if(!parameters.equals(""))
				parameters=","+parameters;	
//			
			templateCOperationHeader.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameters);
			templateCxxImplementation.addMapping("__OPERATION_ID__",""+_operationsMap.get(_fullQualifiedComponentName+ node.getName().getText()));
			templateCxxImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateCxxImplementation.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInCxx());
			templateCxxImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			String pullOut=parameterList.pullOutFromSocketForCxx();
			String switchSyncAsync="";
			if(pullOut.equals(""))
			{
				switchSyncAsync+="int flags;\n";
				switchSyncAsync+="flags = fcntl(_newsockfd, F_GETFL, 0);\n";
				switchSyncAsync+="flags |= O_NONBLOCK;\n";
				switchSyncAsync+="fcntl(_newsockfd, F_SETFL, flags);\n";
				templateCxxImplementation.addMapping("__SWITCH_SYNC_ASYNC__",switchSyncAsync);
			}else{
				switchSyncAsync+="int flags;\n";
				switchSyncAsync+="flags = fcntl(_newsockfd, F_GETFL, 0);\n";
				switchSyncAsync+="flags ^= O_NONBLOCK;\n";
				switchSyncAsync+="fcntl(_newsockfd, F_SETFL, flags);\n";
				templateCxxImplementation.addMapping("__SWITCH_SYNC_ASYNC__",switchSyncAsync);
			}
				
			templateCxxImplementation.addMapping("__SOCKET_PUSH__", parameterList.pushInToSocketForCxx());
			
			templateCxxImplementation.addMapping("__SOCKET_PULL__", parameterList.pullOutFromSocketForCxx());
			templateCImplementation.addMapping("__PREPARE__STRING_ARGS__",parameterList.convertCharsToString());
			templateCImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText().toLowerCase());
			templateCImplementation.addMapping( "__CXX_OPERATION_NAME__", node.getName().getText());
			templateCImplementation.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInF2Cxx());
			templateCImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameters);
			_operations+="procedure,public::"+node.getName().getText()+"\n";
//			
		
			templateCxxOperationHeader.open();
			templateCxxOperationHeader.close();
      templateCxxImplementation.open();
			templateCxxImplementation.close();
			templateCOperationHeader.open();
			templateCOperationHeader.close();
			templateCImplementation.open();
			templateCImplementation.close();
			templateFPortProxyOperation.open();
			templateFPortProxyOperation.close();
//
//		  
//			templateJava.open();
//			templateJava.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}


