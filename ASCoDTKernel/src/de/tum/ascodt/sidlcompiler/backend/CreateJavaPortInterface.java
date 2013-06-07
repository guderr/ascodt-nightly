package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.frontend.node.PUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * Create the interface representing a port. This class is always to be invoked
 * directly on an interface node, i.e. never on the whole tree.
 * 
 * @author Tobias Weinzierl
 */
public class CreateJavaPortInterface extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace( CreateJavaPortInterface.class.getCanonicalName() );

	private java.util.Stack< TemplateFile >   _templateInterfaceFiles;
	private java.util.Stack< TemplateFile >   _templateDispatcherFiles;
	private java.util.Stack< TemplateFile >   _templateDispatcherNativeFiles;
	private java.util.Stack< TemplateFile >   _templateDispatcherSocketFiles;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;
	private HashMap<String,Integer>					  _portsHashmap;
	private boolean _generateSuperPort;

	private String _fullQualifiedComponentName;

	public CreateJavaPortInterface(
			SymbolTable symbolTable,
			URL destinationDirectory,
			String[] namespace,
			HashMap<String,Integer>	portsHashmap) {
		_templateInterfaceFiles         = new java.util.Stack< TemplateFile >();
		_templateDispatcherFiles        = new java.util.Stack< TemplateFile >();
		_templateDispatcherNativeFiles  = new java.util.Stack< TemplateFile >();
		_templateDispatcherSocketFiles  = new java.util.Stack< TemplateFile >();
		_destinationDirectory = destinationDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_portsHashmap = portsHashmap;
		_generateSuperPort=false;
	}


	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperPort){
				String portName                      = node.getName().getText();
				String templateFileOfInterface       = "java-port-interface.template";
				String templateFileOfDispatcherPort    = "java-port-dispatcher.template";
				String templateFileOfDispatcherNativePort    = "java-port-dispatcher-native.template";
				String templateFileOfDispatcherSocketPort    = "java-port-dispatcher-socket.template";
				
				String templateFileOfAbstractPort    = "java-port-abstract-port.template";
				String templateFileOfAbstractSocketPort    = "java-port-abstract-socket-port.template";

				_fullQualifiedComponentName = _symbolTable.getScope(node).getFullQualifiedName(portName);
				String destinationFileOfInterface    = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + ".java";
				String destinationFileOfDispatcherPort = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "Dispatcher.java";
				String destinationFileOfAbstractPort = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "AbstractPort.java";
				String destinationFileOfAbstractSocketPort = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "AbstractSocketPort.java";
				
				String destinationFileOfDispatcherNativePort = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "NativeDispatcher.java";
				String destinationFileOfDispatcherSocketPort = _destinationDirectory.toString() + File.separatorChar + _fullQualifiedComponentName.replaceAll("[.]", "/") + "SocketDispatcher.java";
				
				_portsHashmap.put(_fullQualifiedComponentName+"createPort", 2+_portsHashmap.size());
				_portsHashmap.put(_fullQualifiedComponentName+"connectPort", 2+_portsHashmap.size());
				_portsHashmap.put(_fullQualifiedComponentName+"disconnectPort", 2+_portsHashmap.size());
				
				System.out.println("Key1:"+_fullQualifiedComponentName+"createPort:"+	_portsHashmap.get(_fullQualifiedComponentName+"createPort"));
				
				_templateInterfaceFiles.push( 
						new TemplateFile( templateFileOfInterface, destinationFileOfInterface, _namespace, TemplateFile.getLanguageConfigurationForJava(),true)
						);
				_templateDispatcherFiles.push(
						new TemplateFile( templateFileOfDispatcherPort, destinationFileOfDispatcherPort, _namespace, TemplateFile.getLanguageConfigurationForJava(),true )
						);
				_templateDispatcherNativeFiles.push(
						new TemplateFile( templateFileOfDispatcherNativePort, destinationFileOfDispatcherNativePort, _namespace, TemplateFile.getLanguageConfigurationForJava(),true )
						);
				_templateDispatcherSocketFiles.push(
						new TemplateFile( templateFileOfDispatcherSocketPort, destinationFileOfDispatcherSocketPort, _namespace, TemplateFile.getLanguageConfigurationForJava(),true )
						);
				TemplateFile templateAbstractPort =  new TemplateFile( templateFileOfAbstractPort, destinationFileOfAbstractPort, _namespace, TemplateFile.getLanguageConfigurationForJava(),true );
				TemplateFile templateAbstractSocketPort =  new TemplateFile( templateFileOfAbstractSocketPort, destinationFileOfAbstractSocketPort, _namespace, TemplateFile.getLanguageConfigurationForJava(),true );
				
				_templateInterfaceFiles.peek().addMapping("__PORT_NAME__", portName);
				_templateDispatcherFiles.peek().addMapping("__PORT_NAME__", portName);
				_templateDispatcherNativeFiles.peek().addMapping("__PORT_NAME__", portName);
				_templateDispatcherSocketFiles.peek().addMapping("__PORT_NAME__", portName);
				String interfaceExtensions="";
				String delim="extends ";
				for(PUserDefinedType superInterface:node.getSupertype()){
					String usesTypeName = superInterface.toString().trim();
					usesTypeName = usesTypeName.replace(' ', '.');
					interfaceExtensions+=delim+usesTypeName;
					delim=",";


				}
				templateAbstractPort.addMapping("__PORT_NAME__", portName);
				templateAbstractSocketPort.addMapping("__PORT_NAME__", portName);
				_templateInterfaceFiles.peek().addMapping("__SUPER_TYPES__", interfaceExtensions);
				templateAbstractPort.open();
				templateAbstractPort.close();
				templateAbstractSocketPort.open();
				templateAbstractSocketPort.close();
				_templateInterfaceFiles.peek().open();
				_templateDispatcherFiles.peek().open();
				_templateDispatcherNativeFiles.peek().open();
				_templateDispatcherSocketFiles.peek().open();
			}
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}


	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateInterfaceFiles.size()==1 );
		if(!_generateSuperPort){
			try {
				_templateInterfaceFiles.peek().close();
				_templateDispatcherFiles.peek().close();
				_templateDispatcherNativeFiles.peek().close();
				_templateDispatcherSocketFiles.peek().close();
			}catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}
			_templateDispatcherFiles.pop();
			_templateInterfaceFiles.pop();
			_templateDispatcherNativeFiles.pop();
			_templateDispatcherSocketFiles.pop();
		}
	}


	public void inAUserDefinedType(AUserDefinedType node) {

		String fullQualifiedSymbol = Scope.getSymbol(node);
		AInterfacePackageElement interfaceDefintion=_symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
		if(interfaceDefintion!=null){
			_generateSuperPort=true;
			interfaceDefintion.apply(this);
			_generateSuperPort=false;
		}
	}

	/**
	 * We create the one operation belonging to this port operation.
	 */
	public void inAOperation(AOperation node) {
		_trace.in( "inAOperation(...)" );
		try {
			String templateFile    = "java-port-operation-interface.template";
			TemplateFile template = new TemplateFile( _templateInterfaceFiles.peek(), templateFile );

			String templateDispatcherOpearationFile    = "java-port-dispatcher-operation.template";
			
			TemplateFile templateDispatcherOpertion = new TemplateFile( _templateDispatcherFiles.peek(), templateDispatcherOpearationFile );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );
			_portsHashmap.put(_fullQualifiedComponentName+node.getName().getText(), 2+_portsHashmap.size());
			template.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			template.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			templateDispatcherOpertion.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			templateDispatcherOpertion.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			templateDispatcherOpertion.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJava() );
			templateDispatcherOpertion.addMapping( "__INT_ENUM_OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJavaWithIntEnums(onlyInParameters.areAllParametersInParameters()) );
			templateDispatcherOpertion.addMapping( "__INT_ENUM_FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJNI2Java() );
			templateDispatcherOpertion.addMapping("__PREPARE_ENUMS__", parameterList.prepareJavaEnumParametersForJNI2JavaCall());
			templateDispatcherOpertion.addMapping("__WRITE_ENUMS__", parameterList.writeJavaEnumParametersAfterJNI2JavaCall());
			
			template.open();
			template.close();
			templateDispatcherOpertion.open();
			templateDispatcherOpertion.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}
