package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
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

public class CreateSocketServerProxyForCxx extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      _trace = new Trace(CreateSocketServerProxyForCxx.class.getCanonicalName());

	private java.util.Stack< TemplateFile >   _templateFilesOfC2CxxProxyImplementation;
	private java.util.Stack< TemplateFile >   _templateFilesProvidesPorts;
  private java.util.Stack< String > 			  _invokers;
	private URL 															_generatedFilesDirectory;
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;
	private String _fullQualifiedName;
	private boolean                           _generateProvidesMethods;
	private int 															_provides;
	private HashMap<AInterfacePackageElement, Integer> _offset_map;

	private String _fullQualifiedPortName;
	public CreateSocketServerProxyForCxx(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory, String[] namespace, HashMap<AInterfacePackageElement, Integer> offset_map) {
		_templateFilesOfC2CxxProxyImplementation  = new java.util.Stack< TemplateFile >();
		_templateFilesProvidesPorts = new java.util.Stack< TemplateFile >();
		_invokers = new java.util.Stack< String >();
		_provides = 0;
		
		_generatedFilesDirectory  = generatedFilesDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_offset_map					  = offset_map;
		
		
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", "open new port interface" );
		try {
			String  componentName              = node.getName().getText();
			_fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;
			String  templateFileForC2CxxProxyComponentImplemention = "native-component-c2cxxproxy-implementation.template";
			String  destinationFileForC2CxxProxyImplementation 						 = _generatedFilesDirectory.toString() + File.separatorChar + _fullQualifiedName.replaceAll("[.]", "/") + "C2CxxProxy.cpp";
			
			_templateFilesOfC2CxxProxyImplementation.push(
					new TemplateFile( templateFileForC2CxxProxyComponentImplemention, destinationFileForC2CxxProxyImplementation, _namespace, TemplateFile.getLanguageConfigurationForJNI() ,true)
					);
			//			
			

			_templateFilesOfC2CxxProxyImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfC2CxxProxyImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__", _fullQualifiedName.replaceAll("[.]", "_") );
			_templateFilesOfC2CxxProxyImplementation.peek().addMapping( "__CXX_FULL_QUALIFIED_NAME__", _fullQualifiedName.replaceAll("[.]", "::") );
			_templateFilesOfC2CxxProxyImplementation.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",_fullQualifiedName.replaceAll("[.]", "/"));
		
			
			//_templateFilesOfC2CxxProxyImplementation.peek().open();
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
		
	}
	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue(_templateFilesOfC2CxxProxyImplementation.size()==1);
		try {
			//_templateFilesOfJNIProxyHeader.peek().close();
			String invokers="";
			while(!_invokers.isEmpty()){
				invokers+=_invokers.peek();
				_invokers.pop();
			}
			_templateFilesOfC2CxxProxyImplementation.peek().addMapping("__SET_INVOKERS__", invokers);
			_templateFilesOfC2CxxProxyImplementation.peek().addMapping("__METHODS__", ""+(2+_provides));
			_templateFilesOfC2CxxProxyImplementation.peek().open();
			while(!_templateFilesProvidesPorts.isEmpty()){
				_templateFilesProvidesPorts.peek().open();
				_templateFilesProvidesPorts.peek().close();
				_templateFilesProvidesPorts.pop();
			}
			_templateFilesOfC2CxxProxyImplementation.peek().close();
		
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
		_templateFilesOfC2CxxProxyImplementation.pop();
	}

	public void inAOperation(AOperation node) {
		Assert.isTrue( _generateProvidesMethods );
		try {
			_provides++;
			_invokers.push("invokers["+_offset_map.get(_fullQualifiedPortName+node.getName().getText())+"]=invoker_"+node.getName().getText()+";\n");
			String templateC2FProxyImplementationFile    = "native-component-c2cxxproxy-implementation-provides-port.template";
			
			TemplateFile c2CxxProxyImplementationTemplate = new TemplateFile( _templateFilesOfC2CxxProxyImplementation.peek(), templateC2FProxyImplementationFile );
			

			ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
			node.apply( onlyInParameters );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );
			c2CxxProxyImplementationTemplate.addMapping("__SOCKET_PULL__", parameterList.pullInFromSocketForCxx());

			c2CxxProxyImplementationTemplate.addMapping("__SOCKET_PUSH__", parameterList.pushOutToSocketForCxx());
			c2CxxProxyImplementationTemplate.addMapping("__OPERATION_PARAMETERS_LIST__",parameterList.getParameterListInJNI(onlyInParameters.areAllParametersInParameters()) );
			c2CxxProxyImplementationTemplate.addMapping("__OPERATION_NAME__", node.getName().getText());
			c2CxxProxyImplementationTemplate.addMapping("__F_OPERATION_NAME__", node.getName().getText().toLowerCase());
			c2CxxProxyImplementationTemplate.addMapping("__OPERATION_PARAMETERS_LIST_C2F__",parameterList.getParameterListInC2F());
			String funcCallParamters= parameterList.getFunctionCallListInCxx();
			//if(!funcCallParamters.equals(""))
			//	funcCallParamters=funcCallParamters;
			c2CxxProxyImplementationTemplate.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__",funcCallParamters);
					
		
			_templateFilesProvidesPorts.add(c2CxxProxyImplementationTemplate);

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