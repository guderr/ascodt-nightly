/**
 * 
 */
package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

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
public class CreateNative2NativePlainPorts extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace(CreateNative2NativePlainPorts.class.getCanonicalName() );
	
	private java.util.Stack< TemplateFile >   _templateJava;
	private java.util.Stack< TemplateFile >   _templateFilesHeader;
	private java.util.Stack< TemplateFile >   _templateFilesImplementation;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;

	private boolean _generateSuperport;

	
	public CreateNative2NativePlainPorts(
			SymbolTable symbolTable,
			URL destinationDirectory,
			String[] namespace){
		_templateJava         		   = new java.util.Stack< TemplateFile >();
		_templateFilesHeader         = new java.util.Stack< TemplateFile >();
		_templateFilesImplementation = new java.util.Stack< TemplateFile >();
		_destinationDirectory        = destinationDirectory;
		_namespace                   = namespace;
		_symbolTable                 = symbolTable;
		_generateSuperport=false;
	}
	
	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperport){
				String  portName              = node.getName().getText();
				
				String fullQualifiedPortName													 = _symbolTable.getScope(node).getFullQualifiedName(portName) ;
				
				String templateFileJava                  = "java-port-native2native-plain-port.template";
				String templateFileHeader					   = "cxx-port-native2native-plain-port-header.template";
				String templateFileImplementation	   = "cxx-port-native2native-plain-port-implementation.template";
				
				String fullQualifiedComponentName    = _symbolTable.getScope(node).getFullQualifiedName(portName);
				String destinationFileJava           = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + "Native2NativePlainPort.java";
				String destinationFileHeader         = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + "Native2NativePlainPort.h";
				String destinationFileImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + "Native2NativePlainPort.cpp";
				
				_templateJava.push( 
						new TemplateFile( templateFileJava, destinationFileJava, _namespace, TemplateFile.getLanguageConfigurationForJava() ,true)
						);
				_templateFilesHeader.push( 
						new TemplateFile( templateFileHeader, destinationFileHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				_templateFilesImplementation.push( 
						new TemplateFile( templateFileImplementation, destinationFileImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
						);
				
				addMappingsJava(portName);
				addMappingHeader(portName, fullQualifiedPortName);
				addCxxImplementationMappings(portName, fullQualifiedPortName);
				
				_templateJava.peek().open();
				_templateFilesHeader.peek().open();
				_templateFilesImplementation.peek().open();
			}
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}

	/**
	 * @param portName
	 * @param fullQualifiedPortName
	 * @throws ASCoDTException
	 */
	public void addMappingHeader(String portName, String fullQualifiedPortName)
			throws ASCoDTException {
		_templateFilesHeader.peek().addMapping("__PORT_NAME__", portName);
		_templateFilesHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateFilesHeader.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateFilesHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateFilesHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
	}

	/**
	 * @param portName
	 */
	public void addMappingsJava(String portName) {
		_templateJava.peek().addMapping("__PORT_NAME__", portName);
		_templateJava.peek().addMapping("__NATIVE_COMPONENT__", NativeComponent.class.getCanonicalName());
	}
	
	/**
	 * @param portType
	 * @param fullQualifiedPortName
	 */
	public void addCxxImplementationMappings(String portType,
			String fullQualifiedPortName) {
		_templateFilesImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateFilesImplementation.peek().addMapping("__PORT_NAME__", portType);
		_templateFilesImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateFilesImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
		
	}

	
	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateJava.size()==1 );
		if(!_generateSuperport){
			try {

				_templateJava.peek().close();
				_templateFilesHeader.peek().close();
				_templateFilesImplementation.peek().close();
			}
			catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}

			_templateJava.pop();
			_templateFilesHeader.pop();
			_templateFilesImplementation.pop();
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

			

			String templateFile = "java-port-native2native-operation-plain-java-implementation.template";
			String templateFileHeader = "cxx-port-native-operation-plain-header.template";
			String templateFileImplementation = "cxx-port-native2native-operation-plain-implementation.template";
			
			
			TemplateFile templateJava = new TemplateFile( _templateJava.peek(), templateFile );
			TemplateFile templateHeader = new TemplateFile( _templateFilesHeader.peek(), templateFileHeader );
			TemplateFile templateImplementation = new TemplateFile( _templateFilesImplementation.peek(), templateFileImplementation );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			templateHeader.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateHeader.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			
			templateImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateImplementation.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInCxx());
			templateImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			
			templateJava.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateJava.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			templateJava.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJava() );
			
			
			templateHeader.open();
			templateHeader.close();
      templateImplementation.open();
			templateImplementation.close();

		  
			templateJava.open();
			templateJava.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}


