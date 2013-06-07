package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.ui.tabs.SocketClientAppsTab;
import de.tum.ascodt.plugin.ui.tabs.SocketServerAppsTab;
import de.tum.ascodt.plugin.ui.views.AppsViewContainer;
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
public class CreateJava2SocketServer extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      _trace = new Trace(CreateJava2SocketServer.class.getCanonicalName());
	private java.util.Stack< TemplateFile >   _templateFilesForAbstractComponent;
	private Stack<TemplateFile> _templateFilesForJavaBasisImplementation;
	private Stack<TemplateFile> _templateFilesForJavaImplementation;
	private URL 							  							_userImplementationsDestinationDirectory;
	private URL 							  							_generatedFilesDirectory;
	
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;

	/**
	 * Helper
	 * 
	 * @see inAClassPackageElement()
	 */
	private boolean                           _generateProvidesMethods;
	private String _fullQualifiedName;
	private HashMap<String,Integer> _operationMap;
	private String _fullQualifiedPortName;
	
	
	CreateJava2SocketServer(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory,URL nativeDirectory, String[] namespace,HashMap<String,Integer> operationMap) {
		_templateFilesForAbstractComponent  = new java.util.Stack< TemplateFile >();
		_templateFilesForJavaBasisImplementation = new java.util.Stack< TemplateFile >();
		_templateFilesForJavaImplementation  = new java.util.Stack< TemplateFile >();

		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		_generatedFilesDirectory  = generatedFilesDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_operationMap=operationMap;
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", "open new port interface" );
		try {
			String  componentName              = node.getName().getText();
		String fullQualifiedNameOfJava2AppAdapter = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "AbstractJavaImplementation";
		String fullQualifiedNameOfJavaBasisImplementation = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "BasisJavaImplementation";
		String fullQualifiedNameOfJavaImplementation = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "JavaImplementation";
		
		_fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;
		String  templateFileForJava2AppAdapter       = "java-remote-server-component-implementation.template";
		String  templateFileForJavaBasisImplementaion      = "basis-java-implementation.template";
		String  templateFileForJavaImplementaion      = "java-component-java-implementation.template";
		String  destiationForJava2AppAdapter    = _generatedFilesDirectory.toString() + File.separatorChar + fullQualifiedNameOfJava2AppAdapter.replaceAll("[.]", "/") + ".java";
		String  destiationForJavaBasisImplementation    = _userImplementationsDestinationDirectory.toString() + File.separatorChar + fullQualifiedNameOfJavaBasisImplementation.replaceAll("[.]", "/") + ".java";
		String  destiationForJavaImplementation    = _userImplementationsDestinationDirectory.toString() + File.separatorChar + fullQualifiedNameOfJavaImplementation.replaceAll("[.]", "/") + ".java";
		
		_templateFilesForAbstractComponent.push( 
					new TemplateFile( templateFileForJava2AppAdapter, destiationForJava2AppAdapter, _namespace, TemplateFile.getLanguageConfigurationForJava(),true)
					);
		_templateFilesForJavaBasisImplementation.push( 
				new TemplateFile( templateFileForJavaBasisImplementaion, destiationForJavaBasisImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava(),true)
				);
		_templateFilesForJavaImplementation.push( 
				new TemplateFile( templateFileForJavaImplementaion, destiationForJavaImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava(),false)
				);
		_templateFilesForAbstractComponent.peek().addMapping( "__COMPONENT_NAME__", componentName );
		_templateFilesForAbstractComponent.peek().addMapping("__SOCKET_SERVER_UI__",SocketServerAppsTab.class.getCanonicalName());
		_templateFilesForAbstractComponent.peek().addMapping("__APPS_CONTAINER__",AppsViewContainer.class.getCanonicalName());
		_templateFilesForJavaBasisImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
		_templateFilesForJavaImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
		_templateFilesForJavaBasisImplementation.peek().addMapping("__FULL_QUALIFIED_COMPONENT_NAME__",_fullQualifiedName);
		_templateFilesForAbstractComponent.peek().open();
		_templateFilesForJavaBasisImplementation.peek().open();
		_templateFilesForJavaImplementation.peek().open();
			_generateProvidesMethods = true;
			for ( PUserDefinedType definedType: node.getProvides() ) {
				definedType.apply(this);
			}
			_generateProvidesMethods = false;
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAClassPackageElement(...)", e);
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
		return _fullQualifiedName+"JavaImplementation";
	}


	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesForAbstractComponent.size()==1 );
		Assert.isTrue(_templateFilesForJavaBasisImplementation.size()==1);
		Assert.isTrue(_templateFilesForJavaImplementation.size()==1);
		try {
			_templateFilesForAbstractComponent.peek().close();
			_templateFilesForJavaImplementation.peek().close();
			_templateFilesForJavaBasisImplementation.peek().close();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesForAbstractComponent.pop();
		_templateFilesForJavaBasisImplementation.pop();
		_templateFilesForJavaImplementation.pop();
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
			String templateFile    = "java-remote-client-server-component-implementation-uses-port.template";
			TemplateFile template = new TemplateFile( _templateFilesForAbstractComponent.peek(), templateFile );
			template.addMapping( "__USES_PORT_AS__",   portName );
			template.addMapping( "__USES_PORT_TYPE__", portType );
			template.addMapping("__CREATE_PORT_ID__",""+_operationMap.get(_fullQualifiedPortName+"createPort"));
			template.addMapping("__CONNECT_DISPATCHER_PORT_ID__",""+_operationMap.get(_fullQualifiedPortName+"connectPort"));
			template.addMapping("__DISCONNECT_DISPATCHER_PORT_ID__",""+_operationMap.get(_fullQualifiedPortName+"disconnectPort"));
			//template.addMapping("__CONNECT_PORT_ID__",""+_operationId++);
			//template.addMapping("__DISCONNECT_PORT_ID__",""+_operationId++);
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
			String templateJavaImplementationFile    = "java-remote-client-server-component-implementation-provides-port.template";
			TemplateFile javaImplementationTemplate = new TemplateFile( _templateFilesForAbstractComponent.peek(), templateJavaImplementationFile );
			
			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			javaImplementationTemplate.addMapping("__OPERATION_NAME__", node.getName().getText());
			javaImplementationTemplate.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			javaImplementationTemplate.addMapping( "__OPERATION_ID__",""+_operationMap.get(_fullQualifiedPortName+node.getName().getText()));
			javaImplementationTemplate.addMapping("__SOCKET_PULL__",parameterList.pullOutFromSocketForJava());
			javaImplementationTemplate.addMapping("__SOCKET_PUSH__",parameterList.pushInToSocketForJava());
			javaImplementationTemplate.open();
			javaImplementationTemplate.close();
			
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}

	public void inAUserDefinedType(AUserDefinedType node) {
		String fullQualifiedSymbol = Scope.getSymbol(node);
		AInterfacePackageElement interfaceNode=_symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
		String portName                      = interfaceNode.getName().getText();
		_fullQualifiedPortName = _symbolTable.getScope(interfaceNode).getFullQualifiedName(portName);
		if (_generateProvidesMethods) {
			if(interfaceNode!=null)
				interfaceNode.apply(this);
		}
	}
}