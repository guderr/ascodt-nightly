package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AUses;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * Compiler Adapter for generating fortran makefiles
 * @author Atanas Atanasov
 *
 */
public class CreateFortranMakefile extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      				_trace = new Trace(CreateFortranMakefile.class.getCanonicalName());
	private java.util.Stack< TemplateFile >   _templateFilesOfMakefile;
	private URL                               _userImplementationsDestinationDirectory;
	private URL 							  							_generatedFilesDirectory;
	private URL                               _nativeDirectory;

	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;


	CreateFortranMakefile(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory,URL nativeDirectory, String[] namespace){
		_templateFilesOfMakefile  = new java.util.Stack< TemplateFile >();
		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		_generatedFilesDirectory  = generatedFilesDirectory;
		_nativeDirectory=nativeDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		try {
			String  componentName              = node.getName().getText();

			String  templateFileForMakefile 						     = "makefile-fortran.template";
			String  destinationFileForMakefile							 = _userImplementationsDestinationDirectory.toString() + File.separatorChar + "Makefile."+componentName;
			String fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;

			_templateFilesOfMakefile.push(
					new TemplateFile( templateFileForMakefile, destinationFileForMakefile, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true)
					);
			_templateFilesOfMakefile.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfMakefile.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "/"));
			_templateFilesOfMakefile.peek().addMapping( "__GENERATED_OUTPUT__", _generatedFilesDirectory.getPath().toString() );
			_templateFilesOfMakefile.peek().addMapping( "__SRC_OUTPUT__", _userImplementationsDestinationDirectory.getPath().toString() );
			_templateFilesOfMakefile.peek().addMapping( "__TAB__","\t");
			_templateFilesOfMakefile.peek().open();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}

	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesOfMakefile.size()==1 );

		try {
			_templateFilesOfMakefile.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesOfMakefile.pop();
	}

	/**
	 * For each uses relation, we have to generate all the connection 
	 * operations.
	 */
	public void inAUses(AUses node) {
		try {
			GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
			node.apply( getPorts );
			String templateMakefileName = "makefile-fortran-uses-port.template";

			String portTypePath = getPorts.getUsesPorts("", "/");

			TemplateFile templateMakefile = new TemplateFile( _templateFilesOfMakefile.peek(), templateMakefileName );
			templateMakefile.addMapping("__USES_PORT_PATH__",portTypePath);
			templateMakefile.open();
			templateMakefile.close();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}
}
