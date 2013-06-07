package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.PUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * Create the interface representing a port. This class is always to be invoked
 * directly on an interface node, i.e. never on the whole tree.
 * 
 * @author Atanas Atanasov
 */
public class CreateNativePortInterface extends DepthFirstAdapter{
	private static Trace                      _trace = new Trace(CreateNativePortInterface.class.getCanonicalName());

	private java.util.Stack< TemplateFile >   _templateFilesInterface;
	private java.util.Stack< TemplateFile >   _templateFilesDispatcherHeader;
	private java.util.Stack< TemplateFile >   _templateFilesDispatcherImplementation;

	private java.util.Stack< TemplateFile >   _templateFilesSocketDispatcherHeader;
	private java.util.Stack< TemplateFile >   _templateFilesSocketDispatcherImplementation;

	private java.util.Stack< TemplateFile >   _templateFilesFortranSocketDispatcher;
	private java.util.Stack< TemplateFile >   _templateFilesFortranProxy4SocketDispatcher;
	private java.util.Stack< TemplateFile >   _templateFilesCProxy4SocketDispatcher;
	
	private java.util.Stack< TemplateFile >   _cxxOperationsTemplateFiles;
	private java.util.Stack< TemplateFile >   _fortranOperationsTemplateFiles;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;
	private String _enumerationIncludes;
	private HashSet<String> _enums;

	private String _operations;
	public CreateNativePortInterface(SymbolTable symbolTable, URL destinationDirectory, String[] namespace) {
		_templateFilesInterface                = new java.util.Stack< TemplateFile >();
		_templateFilesDispatcherHeader			   = new java.util.Stack< TemplateFile >();
		_templateFilesDispatcherImplementation = new java.util.Stack< TemplateFile >();
		_templateFilesSocketDispatcherHeader			   = new java.util.Stack< TemplateFile >();
		_templateFilesSocketDispatcherImplementation = new java.util.Stack< TemplateFile >();
		_templateFilesFortranSocketDispatcher = new java.util.Stack< TemplateFile >();
		_templateFilesFortranProxy4SocketDispatcher = new java.util.Stack< TemplateFile >();
		_templateFilesCProxy4SocketDispatcher = new java.util.Stack< TemplateFile >();
		_cxxOperationsTemplateFiles = new java.util.Stack< TemplateFile >();
		_fortranOperationsTemplateFiles = new java.util.Stack< TemplateFile >();
		_destinationDirectory = destinationDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_enumerationIncludes  = "";
		_enums= new HashSet<String>();
		_operations="";
	}


	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			String portName                      = node.getName().getText();
			String templateFileOfInterface       = "cxx-port-interface.template";
			String templateFileOfDispatcherHeaderPort    = "cxx-port-dispatcher-header.template";
			String templateFileOfDispatcherImplementationPort    = "cxx-port-dispatcher-implementation.template";
			String templateFileOfSocketDispatcherHeaderPort    = "cxx-port-socket-dispatcher-header.template";
			String templateFileOfSocketDispatcherImplementationPort    = "cxx-port-socket-dispatcher-implementation.template";
			String templateFileOfFortranSocketDispatcherImplementationPort    = "fortran-port-socket-dispatcher-implementation.template";
			String templateFileOfFortranProxy4SocketDispatcherImplementationPort    = "fortran-port-socket-dispatcher-proxy-implementation.template";
			String templateFileOfCProxy4SocketDispatcherImplementationPort    = "c-port-socket-dispatcher-proxy-implementation.template";
			
			String fullQualifiedName    = _symbolTable.getScope(node).getFullQualifiedName(portName);
			String destinationFileOfInterface    = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + ".h";
			String destinationFileOfDispatcherPortHeader = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "NativeDispatcher.h";
			String destinationFileOfDispatcherPortImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "NativeDispatcher.cpp";
			String destinationFileOfSocketDispatcherPortHeader = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "NativeSocketDispatcher.h";
			String destinationFileOfSocketDispatcherPortImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "NativeSocketDispatcher.cpp";
			String destinationFileOfFortranSocketDispatcherPortImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "FNativeSocketDispatcher.f90";
			String destinationFileOfFortranProxy4SocketDispatcherPortImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "FProxyNativeSocketDispatcher.f90";
			String destinationFileOfCProxy4SocketDispatcherPortImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedName.replaceAll("[.]", "/") + "CProxyNativeSocketDispatcher.cpp";

			_templateFilesInterface.push( 
					new TemplateFile( templateFileOfInterface, destinationFileOfInterface, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			_templateFilesDispatcherHeader.push( 
					new TemplateFile( templateFileOfDispatcherHeaderPort, destinationFileOfDispatcherPortHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			_templateFilesDispatcherImplementation.push( 
					new TemplateFile( templateFileOfDispatcherImplementationPort, destinationFileOfDispatcherPortImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			_templateFilesSocketDispatcherHeader.push( 
					new TemplateFile( templateFileOfSocketDispatcherHeaderPort, destinationFileOfSocketDispatcherPortHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			_templateFilesSocketDispatcherImplementation.push( 
					new TemplateFile( templateFileOfSocketDispatcherImplementationPort, destinationFileOfSocketDispatcherPortImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			_templateFilesFortranSocketDispatcher.push( 
					new TemplateFile( templateFileOfFortranSocketDispatcherImplementationPort, destinationFileOfFortranSocketDispatcherPortImplementation, _namespace, TemplateFile.getLanguageConfigurationForFortran(),true )
					);
			_templateFilesFortranProxy4SocketDispatcher.push( 
					new TemplateFile( templateFileOfFortranProxy4SocketDispatcherImplementationPort, destinationFileOfFortranProxy4SocketDispatcherPortImplementation, _namespace, TemplateFile.getLanguageConfigurationForFortran(),true )
					);
			_templateFilesCProxy4SocketDispatcher.push( 
					new TemplateFile( templateFileOfCProxy4SocketDispatcherImplementationPort, destinationFileOfCProxy4SocketDispatcherPortImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true )
					);
			String interfaceExtensions="";
			String interfaceExtensionsIncludes="";
			String delim=": public ";
			for(PUserDefinedType superInterface:node.getSupertype()){
				String usesTypeName = superInterface.toString().trim();
				interfaceExtensions+=delim+usesTypeName.replaceAll(" ", "::");
				interfaceExtensionsIncludes+="#include \""+usesTypeName.replaceAll(" ","/")+".h\"\n";
				delim=", public ";
				

			}

			addMappingsInterface(
					portName, 
					fullQualifiedName,
					interfaceExtensions,
					interfaceExtensionsIncludes);
			
			addMappingsHeader(portName,fullQualifiedName);
			addMappingsImplementation(portName, fullQualifiedName);
			
			_templateFilesDispatcherHeader.peek().open();
			_templateFilesDispatcherImplementation.peek().open();
			_templateFilesSocketDispatcherHeader.peek().open();
			_templateFilesSocketDispatcherImplementation.peek().open();
			_templateFilesFortranProxy4SocketDispatcher.peek().open();
			_templateFilesCProxy4SocketDispatcher.peek().open();
			//_templateFilesFortranSocketDispatcher.peek().open();
				}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}


	/**
	 * @param portName
	 * @param fullQualifiedName
	 */
	public void addMappingsImplementation(String portName,
			String fullQualifiedName) {
		_templateFilesDispatcherImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "::"));
		_templateFilesDispatcherImplementation.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesDispatcherImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "/"));
		_templateFilesDispatcherImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_"));
		
		_templateFilesSocketDispatcherImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "::"));
		_templateFilesSocketDispatcherImplementation.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesSocketDispatcherImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "/"));
		_templateFilesSocketDispatcherImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_"));
		
		_templateFilesFortranSocketDispatcher.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesFortranSocketDispatcher.peek().addMapping("__C_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_").toLowerCase());
		_templateFilesFortranProxy4SocketDispatcher.peek().addMapping("__C_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_").toLowerCase());
	
		_templateFilesCProxy4SocketDispatcher.peek().addMapping("__C_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_").toLowerCase());
		_templateFilesCProxy4SocketDispatcher.peek().addMapping("__CXX_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "::"));
		_templateFilesCProxy4SocketDispatcher.peek().addMapping("__PATH_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "/"));
	}

	/**
	 * @param portName
	 */
	public void addMappingsHeader(String portName, String fullQualifiedPortName) {
		_templateFilesDispatcherHeader.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesDispatcherHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateFilesDispatcherHeader.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateFilesDispatcherHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateFilesDispatcherHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
		_templateFilesSocketDispatcherHeader.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesSocketDispatcherHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateFilesSocketDispatcherHeader.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateFilesSocketDispatcherHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateFilesSocketDispatcherHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));

	}


	/**
	 * @param portName
	 * @param fullQualifiedName
	 * @param interfaceExtensions
	 * @param interfaceExtensionsIncludes
	 */
	public void addMappingsInterface(String portName, String fullQualifiedName,
			String interfaceExtensions, String interfaceExtensionsIncludes) {
		_templateFilesInterface.peek().addMapping("__SUPER_PORTS_INCLUDES__", interfaceExtensionsIncludes);
		_templateFilesInterface.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesInterface.peek().addMapping("__SUPER_TYPES__", interfaceExtensions);
		_templateFilesInterface.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "_").toUpperCase());
		_templateFilesInterface.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "::"));
	}


	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateFilesInterface.size()==1 );
		generateEnumIncludes();
		_templateFilesInterface.peek().addMapping( "__ENUM_INCLUDES__",_enumerationIncludes);
		_templateFilesFortranSocketDispatcher.peek().addMapping("__OPERATIONS__",_operations);
		
		try {
			_templateFilesFortranSocketDispatcher.peek().open();
			_templateFilesInterface.peek().open();
			
			while(!_cxxOperationsTemplateFiles.isEmpty()){
				TemplateFile operationTemplate=_cxxOperationsTemplateFiles.peek();
				operationTemplate.open();
				operationTemplate.close();
				_cxxOperationsTemplateFiles.pop();
			}
			while(!_fortranOperationsTemplateFiles.isEmpty()){
				TemplateFile operationTemplate=_fortranOperationsTemplateFiles.peek();
				operationTemplate.open();
				operationTemplate.close();
				_fortranOperationsTemplateFiles.pop();
			}

			_templateFilesInterface.peek().close();
			_templateFilesDispatcherHeader.peek().close();
			_templateFilesDispatcherImplementation.peek().close();

			_templateFilesSocketDispatcherHeader.peek().close();
			_templateFilesSocketDispatcherImplementation.peek().close();
			_templateFilesFortranSocketDispatcher.peek().close();
			_templateFilesFortranProxy4SocketDispatcher.peek().close();
			_templateFilesCProxy4SocketDispatcher.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesInterface.pop();
		_templateFilesDispatcherHeader.pop();
		_templateFilesDispatcherImplementation.pop();
		_templateFilesSocketDispatcherHeader.pop();
		_templateFilesSocketDispatcherImplementation.pop();
		_templateFilesFortranSocketDispatcher.pop();
		_templateFilesFortranProxy4SocketDispatcher.pop();
		_templateFilesCProxy4SocketDispatcher.pop();
	}



	/**
	 * We create the one operation belonging to this port operation.
	 */
	public void inAOperation(AOperation node) {
		_trace.in( "inAOperation(...)" );
		try {
			String templateFileInterface    = "cxx-port-operation-interface.template";
			String templateFileHeader    = "cxx-port-native-operation-plain-header.template";
			String templateFileImplementation    = "cxx-port-native-operation-dispatcher-implementation.template";
			String templateFileFortranOperationImplementation    = "fortran-port-socket-dispatcher-operation-implementation.template";
			String templateFileFortranProxyOperationImplementation    = "fortran-port-socket-dispatcher-proxy-operation-implementation.template";
			String templateFileCProxyOperationImplementation    = "c-port-socket-dispatcher-operation-implementation.template";
			
			TemplateFile templateInterface = new TemplateFile( _templateFilesInterface.peek(), templateFileInterface );
			TemplateFile templateDispatcherHeader = new TemplateFile( _templateFilesDispatcherHeader.peek(), templateFileHeader );
			TemplateFile templateDispatcherImplementation = new TemplateFile( _templateFilesDispatcherImplementation.peek(), templateFileImplementation );
			TemplateFile templateFortranOperation4DispatcherImplementation = new TemplateFile(
					_templateFilesFortranSocketDispatcher.peek(),
					templateFileFortranOperationImplementation
			);
			TemplateFile templateFortranProxyOperation4DispatcherImplementation = new TemplateFile(
					_templateFilesFortranProxy4SocketDispatcher.peek(),
					templateFileFortranProxyOperationImplementation
			);
			TemplateFile templateCProxyOperation4DispatcherImplementation = new TemplateFile(
					_templateFilesCProxy4SocketDispatcher.peek(),
					templateFileCProxyOperationImplementation
			);
			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );
			if(parameterList.hasEnums()){
				HashSet<String> enums=parameterList.getEnumTypes();
				_enums.addAll(enums);
			}
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );
			
			templateInterface.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateInterface.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			
			templateFortranOperation4DispatcherImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateFortranProxyOperation4DispatcherImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			
			templateFortranOperation4DispatcherImplementation.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInF(true));
			templateFortranOperation4DispatcherImplementation.addMapping("__OPERATION_PARAMETERS_TYPES_LIST_FOR_C__", parameterList.getParameterListTypesForFCBindedToC(false));
			templateFortranOperation4DispatcherImplementation.addMapping("__OPERATION_PARAMETERS_TYPES_LIST__", parameterList.getParameterListTypesForF(true));
			
			templateFortranProxyOperation4DispatcherImplementation.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInF(true));
			templateFortranProxyOperation4DispatcherImplementation.addMapping("__OPERATION_PARAMETERS_TYPES_LIST__", parameterList.getParameterListTypesForFCBindedToC(true));
			
			templateFortranOperation4DispatcherImplementation.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__", parameterList.getFunctionCallListInFClient(true));
			templateFortranOperation4DispatcherImplementation.addMapping("__FUNCTION_CALL_PARAMETERS_LIST_FOR_C__", parameterList.getFunctionCallListInFClient(false));
			
			templateDispatcherHeader.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateDispatcherHeader.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());

			templateDispatcherImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateDispatcherImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			templateDispatcherImplementation.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__", parameterList.getFunctionCallListInCxx());
			String parameters=parameterList.getParameterListInF2Cxx();
			if(!parameters.equals(""))
				parameters=","+parameters;	
			templateCProxyOperation4DispatcherImplementation.addMapping("__PREPARE__STRING_ARGS__",parameterList.convertCharsToString());
			templateCProxyOperation4DispatcherImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText().toLowerCase());
			templateCProxyOperation4DispatcherImplementation.addMapping( "__CXX_OPERATION_NAME__", node.getName().getText());
			templateCProxyOperation4DispatcherImplementation.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInF2Cxx());
			templateCProxyOperation4DispatcherImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameters);
			
			_cxxOperationsTemplateFiles.push(templateInterface);
			_operations+="\tprocedure,public::"+node.getName().getText()+"\n";
			_operations+="\tprocedure,private::"+node.getName().getText()+"_internal\n";
			templateDispatcherHeader.open();
			templateDispatcherHeader.close();
			
			templateDispatcherImplementation.open();
			templateDispatcherImplementation.close();
			templateFortranProxyOperation4DispatcherImplementation.open();
			templateFortranProxyOperation4DispatcherImplementation.close();
			templateCProxyOperation4DispatcherImplementation.open();
			templateCProxyOperation4DispatcherImplementation.close();
			_fortranOperationsTemplateFiles.add(templateFortranOperation4DispatcherImplementation);
			//_fortranOperationsTemplateFiles
			
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}


	/**
	 * @param node
	 * @param enums
	 */
	private void generateEnumIncludes() {
		for(de.tum.ascodt.sidlcompiler.frontend.node.AEnumDeclarationPackageElement globalEnumeration:
			_symbolTable.getGlobalScope().getFlattenedEnumsElements()){
			for(String localEnumeration:_enums)
				
				if(globalEnumeration.getName().getText().contains(localEnumeration.substring(localEnumeration.lastIndexOf(".")+1))){
					String fullQualifiedName=_symbolTable.getScope(globalEnumeration).getFullQualifiedName(globalEnumeration.getName().getText());
					_enumerationIncludes+="#include \""+_destinationDirectory.getPath().toString()  +fullQualifiedName.replaceAll("[.]", "/")+".h\"\n";
				}
		}
	}

}
