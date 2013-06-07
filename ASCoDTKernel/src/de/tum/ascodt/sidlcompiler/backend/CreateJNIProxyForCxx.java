package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
import de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.frontend.node.AUses;
import de.tum.ascodt.sidlcompiler.frontend.node.PUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

public class CreateJNIProxyForCxx extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      _trace = new Trace(CreateJNIProxyForCxx.class.getCanonicalName());

	private java.util.Stack< TemplateFile >   _templateFilesOfJNIProxyHeader;
	private java.util.Stack< TemplateFile >   _templateFilesOfJNIProxyImplementation;
	private java.util.Stack< TemplateFile >   _templateFilesUsesPorts;
	private java.util.Stack< TemplateFile >   _templateFilesProvidesPorts;
	
	private URL                               _userImplementationsDestinationDirectory;
	private URL 															_generatedFilesDirectory;
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;
	private String _fullQualifiedName;
	private boolean                           _generateProvidesMethods;
	private String 														_usesPortsIncludes; 
	CreateJNIProxyForCxx(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory, String[] namespace) {
		_templateFilesOfJNIProxyHeader				  = new java.util.Stack< TemplateFile >();
		_templateFilesOfJNIProxyImplementation  = new java.util.Stack< TemplateFile >();
		_templateFilesUsesPorts  = new java.util.Stack< TemplateFile >();
		_templateFilesProvidesPorts = new java.util.Stack< TemplateFile >();
		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		_generatedFilesDirectory  = generatedFilesDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_usesPortsIncludes		= "";
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", "open new port interface" );
		try {
			String  componentName              = node.getName().getText();
			_fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;
			String  templateFileForAbstractJNIProxyComponentHeader       = "native-component-jniproxy-cxx-header.template";
			String  templateFileForAbstractJNIProxyComponentImplemention = "native-component-jniproxy-cxx-implementation.template";
			String  destinationFileForJNIProxyHeader 										 = _generatedFilesDirectory.toString() + File.separatorChar + _fullQualifiedName.replaceAll("[.]", "/") + "JNIProxy.h";
			String  destinationFileForJNIProxyImplementation 						 = _generatedFilesDirectory.toString() + File.separatorChar + _fullQualifiedName.replaceAll("[.]", "/") + "JNIProxy.cpp";
			
			_templateFilesOfJNIProxyHeader.push(
					new TemplateFile( templateFileForAbstractJNIProxyComponentHeader, destinationFileForJNIProxyHeader, _namespace, TemplateFile.getLanguageConfigurationForJNI() ,true)
					);
			_templateFilesOfJNIProxyImplementation.push(
					new TemplateFile( templateFileForAbstractJNIProxyComponentImplemention, destinationFileForJNIProxyImplementation, _namespace, TemplateFile.getLanguageConfigurationForJNI() ,true)
					);
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfJNIProxyHeader.peek().addMapping( "__INCLUDE_GUARD_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "_").toUpperCase());
			_templateFilesOfJNIProxyHeader.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "_"));
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__JNI_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "_"));
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__CXX_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "::"));
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "/"));
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__GENERATED_OUTPUT__", _generatedFilesDirectory.getPath().toString() );
			_templateFilesOfJNIProxyImplementation.peek().addMapping( "__NATIVE_OUTPUT__", _userImplementationsDestinationDirectory.getPath().toString() );
			_templateFilesOfJNIProxyHeader.peek().open();
			_generateProvidesMethods = true;
			for ( PUserDefinedType definedType: node.getProvides() ) {
				definedType.apply(this);
			}
			_generateProvidesMethods = false;
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAClassPackageElement(...)", "open new port interface" );
	}
	
	public void inAUses(AUses node) {
		_trace.in( "inAUses(AUses)", node.toString() );
		try {
			GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
			node.apply( getPorts );
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters(); 
			node.apply( onlyInParameters );

			String portType = getPorts.getUsesPorts("", "::");
			String portName = node.getAs().getText();
			String portTypePath = getPorts.getUsesPorts("", "/");
			
			String templateFileHeader    = "native-component-jniproxy-cxx-header-uses-port.template";
			String templateFileImlementation    = "native-component-jniproxy-cxx-implementation-uses-port.template";


			TemplateFile templateHeader = new TemplateFile( _templateFilesOfJNIProxyHeader.peek(), templateFileHeader );
			templateHeader.addMapping( "__USES_PORT_AS__",   portName );
			//templateHeader.addMapping( "__USES_PORT_TYPE__", portType );
			_usesPortsIncludes+="#include \""+portTypePath+".h\"\n";


			TemplateFile templateImplementation = new TemplateFile( _templateFilesOfJNIProxyImplementation.peek(), templateFileImlementation );
			templateImplementation.addMapping( "__USES_PORT_AS__",   portName );
			templateImplementation.addMapping( "__USES_PORT_TYPE__", portType );
			_templateFilesUsesPorts.add(templateImplementation);
			templateHeader.open();
			templateHeader.close();
			
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
		_trace.out( "inAUses(AUses)" );
	}
	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesOfJNIProxyHeader.size()==1 );
		Assert.isTrue( _templateFilesOfJNIProxyImplementation.size()==1 );
		try {
			_templateFilesOfJNIProxyHeader.peek().close();
			_templateFilesOfJNIProxyImplementation.peek().addMapping("__INCLUDE_USES_PORTS__", _usesPortsIncludes);
			
			_templateFilesOfJNIProxyImplementation.peek().open();
			while(!_templateFilesUsesPorts.isEmpty()){
				_templateFilesUsesPorts.peek().open();
				_templateFilesUsesPorts.peek().close();
				_templateFilesUsesPorts.pop();
			}
			while(!_templateFilesProvidesPorts.isEmpty()){
				_templateFilesProvidesPorts.peek().open();
				_templateFilesProvidesPorts.peek().close();
				_templateFilesProvidesPorts.pop();
			}
			_templateFilesOfJNIProxyImplementation.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
		_templateFilesOfJNIProxyHeader.pop();
		_templateFilesOfJNIProxyImplementation.pop();
	}
	
	public void inAOperation(AOperation node) {
		Assert.isTrue( _generateProvidesMethods );
		try {        
			String templateJNIProxyImplementationFile    = "native-component-jniproxy-cxx-implementation-provides-port.template";
			String templateJNIProxyImplementationHeaderFile    = "native-component-jniproxy-cxx-header-provides-port.template";
			
			TemplateFile jniProxyImplementationHeaderTemplate = new TemplateFile( _templateFilesOfJNIProxyHeader.peek(), templateJNIProxyImplementationHeaderFile );
			TemplateFile jniProxyImplementationTemplate = new TemplateFile( _templateFilesOfJNIProxyImplementation.peek(), templateJNIProxyImplementationFile );
			
			
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			jniProxyImplementationHeaderTemplate.addMapping("__OPERATION_NAME__", node.getName().getText());
			jniProxyImplementationTemplate.addMapping("__OPERATION_NAME__", node.getName().getText());
			jniProxyImplementationHeaderTemplate.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInJNI(onlyInParameters.areAllParametersInParameters()) );
			jniProxyImplementationTemplate.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInJNI(onlyInParameters.areAllParametersInParameters()) );
			jniProxyImplementationHeaderTemplate.addMapping( "__JNI_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "_"));
			jniProxyImplementationTemplate.addMapping( "__JNI_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "_"));
			jniProxyImplementationTemplate.addMapping("__CXX_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "::"));
			jniProxyImplementationTemplate.addMapping("__START_METHOD_CALL__",parameterList.prepareJNIParametersForCxxCall());
			jniProxyImplementationTemplate.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__",parameterList.getFunctionCallListInJNI2Cxx());
			jniProxyImplementationTemplate.addMapping("__END_METHOD_CALL__", parameterList.writeCxxParamatersFromJNIProvideCall());
			
			_templateFilesProvidesPorts.add(jniProxyImplementationTemplate);
			jniProxyImplementationHeaderTemplate.open();
			jniProxyImplementationHeaderTemplate.close();
			
			
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}
	
	public void inAUserDefinedType(AUserDefinedType node) {
		if (_generateProvidesMethods) {
			String fullQualifiedSymbol = Scope.getSymbol(node);
			AInterfacePackageElement interfaceNode=_symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
			if(interfaceNode!=null)
				interfaceNode.apply(this);
		}
	}
}