package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
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
 * Create the interface representing a port. This class is always to be invoked
 * directly on an interface node, i.e. never on the whole tree.
 * 
 * @author Atanas Atanasov
 */
public class CreateNative2JavaPlainPorts extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace(CreateNative2JavaPlainPorts.class.getCanonicalName() );
	private java.util.Stack< TemplateFile >   _templateFilesJava;
	private java.util.Stack< TemplateFile >   _templateFilesHeader;
	private java.util.Stack< TemplateFile >   _templateFilesImplementation;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;

	private boolean _generateSuperport;


	public CreateNative2JavaPlainPorts(SymbolTable symbolTable, URL destinationDirectory, String[] namespace) {
		_templateFilesJava		= new java.util.Stack< TemplateFile >();		
		_templateFilesHeader  = new java.util.Stack< TemplateFile >();
		_templateFilesImplementation= new java.util.Stack< TemplateFile >();
		_destinationDirectory = destinationDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_generateSuperport=false;
	}


	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperport){
				String portType                      = node.getName().getText();
				String templateFileJava                  = "java-port-native2java-plain-port.template";
				String templateFileHeader                  = "cxx-port-native2java-plain-port-header.template";
				String templateFileForImplementation = "cxx-port-native2java-plain-port-implementation.template";
				String fullQualifiedPortName    = _symbolTable.getScope(node).getFullQualifiedName(portType);
				String destinationFileJava  = _destinationDirectory.toString() + File.separatorChar + fullQualifiedPortName.replaceAll("[.]", "/") + "Native2JavaPlainPort.java";
				String destinationFileHeader               = _destinationDirectory.toString() + File.separatorChar + fullQualifiedPortName.replaceAll("[.]", "/") + "Native2JavaPlainPort.h";
				String destinationFileImplementation = _destinationDirectory.toString() + File.separatorChar + fullQualifiedPortName.replaceAll("[.]", "/") + "Native2JavaPlainPort.cpp";

				_templateFilesJava.push( 
						new TemplateFile( templateFileJava, destinationFileJava, _namespace, TemplateFile.getLanguageConfigurationForJava() ,true)
				);
				
				_templateFilesHeader.push( 
						new TemplateFile( templateFileHeader, destinationFileHeader, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
				);
				_templateFilesImplementation.push( 
						new TemplateFile( templateFileForImplementation, destinationFileImplementation, _namespace, TemplateFile.getLanguageConfigurationForCPP() ,true)
				);
				
				addJavaMappings(portType);
				addHeaderMappings(portType, fullQualifiedPortName);
				addCxxImplementationMappings(portType, fullQualifiedPortName);
				
				_templateFilesJava.peek().open();
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
	 * @param portType
	 */
	public void addJavaMappings(String portType) {
		_templateFilesJava.peek().addMapping("__PORT_NAME__", portType);
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


	/**
	 * @param portType
	 * @param fullQualifiedPortName
	 */
	public void addHeaderMappings(String portType, String fullQualifiedPortName) {
		_templateFilesHeader.peek().addMapping("__PORT_NAME__", portType);
		_templateFilesHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_").toUpperCase());
		_templateFilesHeader.peek().addMapping( "__FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "::"));
		_templateFilesHeader.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "/"));
		_templateFilesHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",fullQualifiedPortName.replaceAll("[.]", "_"));
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

	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		if(!_generateSuperport){
			Assert.isTrue( _templateFilesHeader.size()==1 );

			try {
				_templateFilesJava.peek().close();
				_templateFilesHeader.peek().close();
				_templateFilesImplementation.peek().close();
				
			}
			catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}
			_templateFilesJava.pop();
			_templateFilesHeader.pop();
			_templateFilesImplementation.pop();
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

			String templateFileHeader = "cxx-port-native-operation-plain-header.template";
			String templateFileImplementation = "cxx-port-native2java-operation-plain-implementation.template";
			String templateFileJava = "java-port-operation-plain-java-implementation.template";
			
			TemplateFile templateHeader = new TemplateFile( _templateFilesHeader.peek(), templateFileHeader );
			TemplateFile templateImplementation = new TemplateFile( _templateFilesImplementation.peek(), templateFileImplementation );
			TemplateFile templateJava = new TemplateFile( _templateFilesJava.peek(), templateFileJava );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			templateJava.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateJava.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			templateJava.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJava() );

			
			templateHeader.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateHeader.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			
			templateImplementation.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateImplementation.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInCxx());
			templateImplementation.addMapping( "__JNI_OPERATION_PARAMETER_TYPES_LIST__",parameterList.getParameterListInJNITypes());
			templateImplementation.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInCxx2JNI() );
			templateImplementation.addMapping("__START_METHOD_CALL__", parameterList.prepareCxxParametersForJNICall());
			templateImplementation.addMapping("__END_METHOD_CALL__", parameterList.writeCxxParamatersFromJNIUseCall());
			
			templateJava.open();
			templateJava.close();
			
			templateHeader.open();
			templateHeader.close();
			templateImplementation.open();
			templateImplementation.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}
