package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
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
public class CreateBuildScripts extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private java.util.Stack< TemplateFile >   _templateFilesOfFortranMakefile;
	private java.util.Stack< TemplateFile >   _templateFilesOfFortranCMakefile;
	private URL                               _userImplementationsDestinationDirectory;
	private URL 							  							_generatedFilesDirectory;
	
	private String[]                          _namespace;
	private SymbolTable                       _symbolTable;


	CreateBuildScripts(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory,URL nativeDirectory, String[] namespace){
		_templateFilesOfFortranMakefile  = new java.util.Stack< TemplateFile >();
		_templateFilesOfFortranCMakefile = new java.util.Stack< TemplateFile >();
		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		_generatedFilesDirectory  = generatedFilesDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
	}

	public void inAClassPackageElement(AClassPackageElement node) {
		try {
			String  componentName              = node.getName().getText();

			String  templateFileForFortranMakefile 						     = "makefile-fortran.template";
			String  destinationFileForFortranMakefile							 = _userImplementationsDestinationDirectory.toString() + File.separatorChar + "Makefile."+componentName;
			String  templateFileForFortranCMakefile 						     = "cmakefile-fortran.template";
			String  destinationFileForFortranCMakefile							 = _userImplementationsDestinationDirectory.toString() + File.separatorChar + "CMakeLists."+componentName+".txt";
			
			String fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;

			_templateFilesOfFortranMakefile.push(
					new TemplateFile( templateFileForFortranMakefile, destinationFileForFortranMakefile, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true)
					);
			_templateFilesOfFortranCMakefile.push(
					new TemplateFile( templateFileForFortranCMakefile, destinationFileForFortranCMakefile, _namespace, TemplateFile.getLanguageConfigurationForCPP(),true)
					);
			createComponentMappings(componentName, fullQualifiedName,_templateFilesOfFortranMakefile);
			createComponentMappings(componentName, fullQualifiedName,_templateFilesOfFortranCMakefile);
			
			_templateFilesOfFortranMakefile.peek().addMapping( "__TAB__","\t");
			_templateFilesOfFortranMakefile.peek().open();
			_templateFilesOfFortranCMakefile.peek().open();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}

	private void createComponentMappings(String componentName,
			String fullQualifiedName,
			java.util.Stack< TemplateFile > template) {
		template.peek().addMapping( "__COMPONENT_NAME__", componentName );
		template.peek().addMapping( "__PATH_FULL_QUALIFIED_NAME__",fullQualifiedName.replaceAll("[.]", "/"));
		template.peek().addMapping( "__GENERATED_OUTPUT__", _generatedFilesDirectory.getPath().toString() );
		template.peek().addMapping( "__SRC_OUTPUT__", _userImplementationsDestinationDirectory.getPath().toString() );
	}

	/**
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesOfFortranMakefile.size()==1 );
		Assert.isTrue( _templateFilesOfFortranCMakefile.size()==1 );

		try {
			_templateFilesOfFortranMakefile.peek().close();
			_templateFilesOfFortranCMakefile.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesOfFortranMakefile.pop();
		_templateFilesOfFortranCMakefile.pop();
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

			TemplateFile templateMakefile = new TemplateFile( _templateFilesOfFortranMakefile.peek(), templateMakefileName );
			templateMakefile.addMapping("__USES_PORT_PATH__",portTypePath);
			templateMakefile.open();
			templateMakefile.close();
		}catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}
	}
}
