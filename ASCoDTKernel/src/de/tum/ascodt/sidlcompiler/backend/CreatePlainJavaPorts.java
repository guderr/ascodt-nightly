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
public class CreatePlainJavaPorts extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace( CreatePlainJavaPorts.class.getCanonicalName() );

	private java.util.Stack< TemplateFile >   _templateFiles;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;

	private SymbolTable                       _symbolTable;
	private boolean _generateSuperport;

	public CreatePlainJavaPorts(SymbolTable symbolTable,  URL destinationDirectory, String[] namespace) {
		_templateFiles        = new java.util.Stack< TemplateFile >();
		_destinationDirectory = destinationDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_generateSuperport=false;
	}


	public void inAInterfacePackageElement(AInterfacePackageElement node) {
		_trace.in( "inAInterfacePackageElement(...)", "open new port interface" );
		try {
			if(!_generateSuperport){
				String portName                      = node.getName().getText();
				String templateFile                  = "java-port-plain-port.template";
				String fullQualifiedComponentName    = _symbolTable.getScope(node).getFullQualifiedName(portName);
				String destinationFile               = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + "PlainJavaPort.java";

				_templateFiles.push( 
						new TemplateFile( templateFile, destinationFile, _namespace, TemplateFile.getLanguageConfigurationForJava() ,true)
						);

				_templateFiles.peek().addMapping("__PORT_NAME__", portName);
				_templateFiles.peek().open();
			}
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAInterfacePackageElement(...)", "open new port interface" );
	}


	public void outAInterfacePackageElement(AInterfacePackageElement node) {
		Assert.isTrue( _templateFiles.size()==1 );
		if(!_generateSuperport){
			try {

				_templateFiles.peek().close();
			}
			catch (ASCoDTException  e ) {
				ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
			}

			_templateFiles.pop();
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

			String templateFile;

			//      if (onlyInParameters.areAllParametersInParameters()) {
			//        templateFile = _templateDirectory.toString() + File.separatorChar + "java-port-operation-plain-java-implementation-only-in-parameters.template";
			//      }
			//      else {
			templateFile = "java-port-operation-plain-java-implementation.template";
			//}
			TemplateFile template = new TemplateFile( _templateFiles.peek(), templateFile );

			GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
			node.apply( parameterList );

			template.addMapping( "__OPERATION_NAME__" , node.getName().getText() );
			template.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );
			template.addMapping( "__FUNCTION_CALL_PARAMETERS_LIST__" , parameterList.getFunctionCallListInJava() );

			template.open();
			template.close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAOperation(...)", e);
		}

		_trace.out( "inAOperation(...)" );
	}

}
