package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.repository.Target;
import de.tum.ascodt.repository.Target.TargetType;
import de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AUses;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * This class creates the base class for all components of a given type.
 * 
 * There might be multiple component implementations, i.e. multiple variants of 
 * a component with different targets. They have in common one base class in 
 * Java that acts as a proxy to the real implementation. This class creates this 
 * common base class with all its abstract files.
 * 
 * @author Tobias Weinzierl
 */
public class CreateJavaComponentInterface extends DepthFirstAdapter {
	private static Trace                      _trace = new Trace(CreateJavaComponentInterface.class.getCanonicalName());

	private java.util.Stack< TemplateFile >   _templateFiles;
	private URL                               _destinationDirectory;
	private String[]                          _namespace;
	private String                            _fullQualifiedNameOfTheComponentImplementation;
	private SymbolTable                       _symbolTable;
	private Target _target;
	public CreateJavaComponentInterface(
			SymbolTable symbolTable,
			URL destinationDirectory,
			String[] namespace,
			String fullQualifiedNameOfTheComponentImplementation,
			Target target) {
		_templateFiles        = new java.util.Stack< TemplateFile >();
		_destinationDirectory = destinationDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_target=target;
		_fullQualifiedNameOfTheComponentImplementation = fullQualifiedNameOfTheComponentImplementation;
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

			String portType = getPorts.getUsesPorts("", ".");
			String portName = node.getAs().getText();
			String templateFile    = "java-component-interface-uses-port.template";

			TemplateFile template = new TemplateFile( _templateFiles.peek(), templateFile );

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


	/**
	 * This is the major plug-in point. We opne a new template file and 
	 * imediately push it on the file writer stack. This stack has been 
	 * empty before. When we leave the class package again, this file 
	 * writer is to be closed. To find out which provides ports are 
	 * implemented, we have to use GetProvidesAndUsesPorts on node. To 
	 * find out what the actual implementation file is, we 
	 */
	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", node.toString() );
		try {
			String componentName   = node.getName().getText();
			String fullQualifiedComponentName = _symbolTable.getScope(node).getFullQualifiedName(componentName);
			String templateFile    = "java-component-interface.template";
			String destinationFile = _destinationDirectory.toString() + File.separatorChar + fullQualifiedComponentName.replaceAll("[.]", "/") + ".java";
			_templateFiles.push( 
					new TemplateFile( templateFile, destinationFile, _namespace, TemplateFile.getLanguageConfigurationForJava(),true )
					);

			_templateFiles.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFiles.peek().addMapping( "__FULL_QUALIFIED_PATH_OF_IMPLEMENTATION_FILE__", _fullQualifiedNameOfTheComponentImplementation );

			GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
			node.apply( getPorts );

			String extendingInterfaces = getPorts.getProvidesPorts(",", ".");
			if (!extendingInterfaces.equals("")) {
				extendingInterfaces += ",";
			}
			switch(_target.getType()){
			case JavaLocal:
				extendingInterfaces +=de.tum.ascodt.repository.entities.Component.class.getCanonicalName();
				break;
			case CxxRemoteSocket:
				extendingInterfaces +=de.tum.ascodt.repository.entities.CxxRemoteSocketComponent.class.getCanonicalName();
				break;
			case JavaNative:
				extendingInterfaces += de.tum.ascodt.repository.entities.NativeComponent.class.getCanonicalName();
				break;
			case ReverseCxxRemoteSocket:
				extendingInterfaces +=de.tum.ascodt.repository.entities.CxxReverseRemoteSocketComponent.class.getCanonicalName();
				break;
			default:
				extendingInterfaces +=de.tum.ascodt.repository.entities.Component.class.getCanonicalName();
				break;
			}
			
			_templateFiles.peek().addMapping( 
					"__LIST_OF_PROVIDES_INTERFACES_AND_STANDARD_COMPONENT_INTERFACE__", 
					extendingInterfaces
					);

			_templateFiles.peek().open();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_trace.out( "inAClassPackageElement(...)" );
	}


	/**
	 * Close output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFiles.size()==1 );

		try {
			_templateFiles.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "outAClassPackageElement(...)", e);
		}

		_templateFiles.pop();
	}

}
