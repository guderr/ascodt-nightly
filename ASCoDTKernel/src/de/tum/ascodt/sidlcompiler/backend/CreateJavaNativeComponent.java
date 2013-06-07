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

/**
 * This generator is used to create the java code for native components.
 * @author Atanas Atanasov
 *
 */
public class CreateJavaNativeComponent extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      _trace = new Trace(CreateJavaNativeComponent.class.getCanonicalName());
	private java.util.Stack< TemplateFile >   _templateFilesOfAbstractImplementation;
	private java.util.Stack< TemplateFile >   _templateFilesOfPlainImplementation;
	private URL                               _userImplementationsDestinationDirectory;
	private URL 							  							_generatedFilesDirectory;
	private URL                               _nativeDirectory;
	
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;

	/**
	 * Helper
	 * 
	 * @see inAClassPackageElement()
	 */
	private boolean                           _generateProvidesMethods;
	private String _fullQualifiedName;

	CreateJavaNativeComponent(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory,URL nativeDirectory, String[] namespace) {
		_templateFilesOfAbstractImplementation  = new java.util.Stack< TemplateFile >();
		_templateFilesOfPlainImplementation     = new java.util.Stack< TemplateFile >();
		
		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		_generatedFilesDirectory  = generatedFilesDirectory;
		_nativeDirectory=nativeDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", "open new port interface" );
		try {
			String  componentName              = node.getName().getText();
			String fullQualifiedNameOfTheAbstractComponentImplementation = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "AbstractJavaNativeImplementation";
			_fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;
			String  templateFileForAbstractComponentImplementation       = "java-native-component-abstract-java-implementation.template";
			String  templateFileForComponentImplementation               = "java-native-component-java-implementation.template";
		
			String  destinationFileForAbstractComponentImplementation    = _userImplementationsDestinationDirectory.toString() + File.separatorChar + fullQualifiedNameOfTheAbstractComponentImplementation.replaceAll("[.]", "/") + ".java";
			String  destinationFileForComponentImplementation            = _userImplementationsDestinationDirectory.toString() + File.separatorChar + _fullQualifiedName.replaceAll("[.]", "/") + "JavaNativeImplementation.java";
			_templateFilesOfAbstractImplementation.push( 
					new TemplateFile( templateFileForAbstractComponentImplementation, destinationFileForAbstractComponentImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava(),true)
					);
			_templateFilesOfPlainImplementation.push(
					new TemplateFile( templateFileForComponentImplementation, destinationFileForComponentImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava(),false)
					);
			
			
			
			_templateFilesOfAbstractImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfPlainImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfAbstractImplementation.peek().addMapping( "__NATIVE_OUTPUT__", _nativeDirectory.getPath().toString() );
			_templateFilesOfAbstractImplementation.peek().open();
			_templateFilesOfPlainImplementation.peek().open();
			
			
		
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

	/**
	 * Tell the asking method the name of the component instantiation that 
	 * ASCoDT shall instantiate later on.
	 * 
	 * @return
	 */
	public String getFullQualifiedNameOfTheComponentImplementation() {
		return _fullQualifiedName+"JavaNativeImplementation";
	}


	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesOfAbstractImplementation.size()==1 );

		try {
			_templateFilesOfAbstractImplementation.peek().close();
			_templateFilesOfPlainImplementation.peek().close();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesOfAbstractImplementation.pop();
		_templateFilesOfPlainImplementation.pop();
	}



	/**
	 * For each uses relation, we have to generate all the connection 
	 * operations.
	 */
	public void inAUses(AUses node) {
		_trace.in( "inAUses(AUses)", node.toString() );
		try {
			GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
			node.apply( getPorts );
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters(); 
			node.apply( onlyInParameters );

			String portType = getPorts.getUsesPorts("", ".");
			String portName = node.getAs().getText();
			String templateFile    = "java-native-component-abstract-java-implementation-uses-port.template";
			TemplateFile template = new TemplateFile( _templateFilesOfAbstractImplementation.peek(), templateFile );
			template.addMapping( "__USES_PORT_AS__",   portName );
			template.addMapping( "__USES_PORT_TYPE__", portType );
		
			
			template.open();
			template.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
		_trace.out( "inAUses(AUses)" );
	}

	public void inAOperation(AOperation node) {
		Assert.isTrue( _generateProvidesMethods );
		try {        
			String templateJavaImplementationFile    = "java-native-component-java-implementation-provides-port.template";
			TemplateFile javaImplementationTemplate = new TemplateFile( _templateFilesOfPlainImplementation.peek(), templateJavaImplementationFile );
			
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			javaImplementationTemplate.addMapping("__OPERATION_NAME__", node.getName().getText());
			if(parameterList.size()>0)
				javaImplementationTemplate.addMapping("__SEPARATOR__", ",");
			else
				javaImplementationTemplate.addMapping("__SEPARATOR__", "");
			javaImplementationTemplate.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			javaImplementationTemplate.addMapping( "__JNI_OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJavaWithIntEnums(onlyInParameters.areAllParametersInParameters()) );
			javaImplementationTemplate.addMapping("__PREPARE_ENUMS__", parameterList.prepareJavaEnumParametersForJava2JNICall());
			javaImplementationTemplate.addMapping("__JNI_FUNCTION_CALL_PARAMETERS_LIST__",parameterList.getFunctionCallListInJava2JNI());
			javaImplementationTemplate.addMapping("__WRITE_ENUMS__", parameterList.writeJavaEnumParametersAfterJava2JNICall());
			javaImplementationTemplate.open();
			javaImplementationTemplate.close();
			
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