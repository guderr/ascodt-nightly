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
 * This generator is used to create the cxx code for native components.
 * @author Atanas Atanasov
 *
 */
public class CreateFortranComponent extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter{
	private Trace                      _trace = new Trace(CreateFortranComponent.class.getCanonicalName());

	private java.util.Stack< TemplateFile >   _templateFilesOfFortranImplementation;
	private java.util.Stack< TemplateFile >   _templateFilesOfAbstractFortranImplementation;
	private java.util.Stack< TemplateFile >   _templateFilesForAbstractUsesPorts;
	private URL                               _userImplementationsDestinationDirectory;
	private URL 															_generatedFilesDirectory;
	private String[]                          _namespace;

	private String                            _fullQualifiedName;
	private SymbolTable                       _symbolTable;
	private boolean                           _generateProvidesMethods;
  private String _provideOperations;
  private String _usesImports;
  private String _usesVariables;
  private java.util.Stack< TemplateFile > _subTemplates;

	private String _usesMethods;

	private String _usesVariablesValidity;
	public CreateFortranComponent(SymbolTable symbolTable, URL userImplementationsDestinationDirectory
			,URL generatedFilesDirectory, String[] namespace){
		_templateFilesOfFortranImplementation  = new java.util.Stack< TemplateFile >();
		_templateFilesOfAbstractFortranImplementation = new java.util.Stack< TemplateFile >();
		_subTemplates = new java.util.Stack< TemplateFile >();
		_templateFilesForAbstractUsesPorts = new java.util.Stack< TemplateFile >();
		_userImplementationsDestinationDirectory = userImplementationsDestinationDirectory;
		
		_generatedFilesDirectory  = generatedFilesDirectory;
		_namespace            = namespace;
		_symbolTable          = symbolTable;
		_provideOperations = "";
		_usesImports = "";
		_usesVariables = "";
		_usesMethods = "";
		_usesVariablesValidity="";
		
	}
	public void inAClassPackageElement(AClassPackageElement node) {
		_trace.in( "inAClassPackageElement(...)", "open new port interface" );
		try {
			String  componentName              = node.getName().getText();
			_fullQualifiedName													 = _symbolTable.getScope(node).getFullQualifiedName(componentName) ;

			String  templateFileForFortranComponentImplementation      		 = "native-component-fortran-implementation.template";
			String  destinationFileForFortranImplementation 						     = _userImplementationsDestinationDirectory.toString()  +File.separatorChar+ _fullQualifiedName.replaceAll("[.]", "/") + "Implementation.f90";
			String  templateFileForFortranAbstractComponentImplementation      		 = "native-component-fortran-abstract-implementation.template";
			String  destinationFileForFortranAbstractImplementation 						     = _generatedFilesDirectory.toString()  +File.separatorChar+ _fullQualifiedName.replaceAll("[.]", "/") + "AbstractImplementation.f90";


			_templateFilesOfFortranImplementation.push(
					new TemplateFile( templateFileForFortranComponentImplementation, destinationFileForFortranImplementation, _namespace, TemplateFile.getLanguageConfigurationForFortran(),false)
					);
			_templateFilesOfAbstractFortranImplementation.push(
					new TemplateFile( templateFileForFortranAbstractComponentImplementation, destinationFileForFortranAbstractImplementation, _namespace, TemplateFile.getLanguageConfigurationForFortran(),true)
					);
			
			_templateFilesOfFortranImplementation.peek().addMapping( "__FULL_QUALIFIED_NAME__", _fullQualifiedName.replaceAll("\\.","_").toLowerCase() );
			_templateFilesOfFortranImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
			_templateFilesOfAbstractFortranImplementation.peek().addMapping( "__COMPONENT_NAME__",componentName);
			

			_generateProvidesMethods = true;
			
		
			
			if(node.getProvides().size()>0)
				//				_templateFilesOfAbstractCXXHeader.peek().addMapping("__IMPLEMENTS__",": public");
				//			else
				//
				//				_templateFilesOfAbstractCXXHeader.peek().addMapping("__IMPLEMENTS__","");
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
	 * Close the output streams.
	 */
	public void outAClassPackageElement(AClassPackageElement node) {
		Assert.isTrue( _templateFilesOfFortranImplementation.size()==1 );
		Assert.isTrue( _templateFilesOfAbstractFortranImplementation.size()==1);
		try {
			
			_templateFilesOfFortranImplementation.peek().addMapping( "__PROVIDE_PORTS__", _provideOperations );
			_templateFilesOfFortranImplementation.peek().open();
			_templateFilesOfAbstractFortranImplementation.peek().addMapping("__USE_PORTS_VARIABLES__", _usesVariables);
			_templateFilesOfAbstractFortranImplementation.peek().addMapping("__USE_PORTS_IMPORTS__", _usesImports);
			_templateFilesOfAbstractFortranImplementation.peek().addMapping("__USE_PORTS_CONN_METHODS__",_usesMethods);
			if(_usesVariablesValidity.equals(""))
				_usesVariablesValidity=".True.";
			_templateFilesOfAbstractFortranImplementation.peek().addMapping("__USES_PORTS_VARIABLES_VALIDITY__",_usesVariablesValidity.replaceFirst(".and.", ""));
			_templateFilesOfAbstractFortranImplementation.peek().open();
			while(!_subTemplates.isEmpty()){
				_subTemplates.peek().open();
				_subTemplates.peek().close();
				_subTemplates.pop(); 
			}
			while (!_templateFilesForAbstractUsesPorts.isEmpty()){
				_templateFilesForAbstractUsesPorts.peek().open();
				_templateFilesForAbstractUsesPorts.peek().close();
				_templateFilesForAbstractUsesPorts.pop(); 
			}
			_templateFilesOfFortranImplementation.peek().close();
			_templateFilesOfAbstractFortranImplementation.peek().close();
		}
		catch (ASCoDTException  e ) {
			ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		}

		_templateFilesOfFortranImplementation.pop();
		_templateFilesOfAbstractFortranImplementation.pop();

	}

	public void inAUses(AUses node) {
		_trace.in( "inAUses(AUses)", node.toString() );
		//	try {
		GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
		node.apply( getPorts );
		ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters(); 
		node.apply( onlyInParameters );
	  String portName = node.getAs().getText();
    String fullQualifiedpPortType = getPorts.getUsesPorts("", ".");
  	String portType=fullQualifiedpPortType.substring(fullQualifiedpPortType.lastIndexOf(".")+1);
    
    _usesImports+="use "+fullQualifiedpPortType.replaceAll("\\.","_").toLowerCase()+"FNativeSocketDispatcher\n";
    _usesVariables+= "type("+portType.replaceAll("\\.","_")+"NativeSocketDispatcher)::"+portName+"\n";
    _usesVariables+= "logical:: v_is_connected_"+portName+"\n";
    _usesMethods+="procedure,public::connect_"+ portName.toLowerCase()+"\n";
    _usesMethods+="\tprocedure,public::disconnect_"+ portName.toLowerCase()+"\n";
    _usesMethods+="\tprocedure,public::is_connected_"+ portName.toLowerCase()+"\n";
    _usesVariablesValidity+= ".and.this%v_is_connected_"+portName+"";
    String templateFile    = "native-component-fortran-abstract-implementation-uses-port.template";
    try {
			TemplateFile template = new TemplateFile( _templateFilesOfAbstractFortranImplementation.peek(), templateFile );
			template.addMapping("__FULL_QUALIFIED_USES_PORT_TYPE__",fullQualifiedpPortType.replaceAll("\\.","_").toLowerCase());
			template.addMapping("__USES_PORT_AS__",portName);
			template.addMapping("__USES_PORT_TYPE__", portType.replaceAll("\\.","_"));
			_templateFilesForAbstractUsesPorts.add(template);
    } catch (ASCoDTException e) {
			 ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
		} 
		_trace.out( "inAUses(AUses)" );
	}

	public void inAOperation(AOperation node) {
		 Assert.isTrue( _generateProvidesMethods );
	    try {        
	      String templateFile    = "native-component-fortran-implementation-provides-port.template";
	      TemplateFile template = new TemplateFile( _templateFilesOfFortranImplementation.peek(), templateFile );

	      ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
	      node.apply( onlyInParameters );

	      GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
	      node.apply( parameterList );
	      template.addMapping("__PARAMETER_LIST_TYPES_INTENTS__",parameterList.getParameterListTypesForF(false));
	      template.addMapping("__OPERATION_NAME__", node.getName().getText());
	      _provideOperations+= "procedure,public::"+ node.getName().getText()+"\n";
	      template.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInF(onlyInParameters.areAllParametersInParameters()) );
	      _subTemplates.push(template);
	     
	    }
	    catch (ASCoDTException  e ) {
	      ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
	    }
	}

	 public void inAUserDefinedType(AUserDefinedType node) {
	    if (_generateProvidesMethods) {
	      String fullQualifiedSymbol = Scope.getSymbol(node);
	      AInterfacePackageElement interfaceDefintion=_symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
	      if(interfaceDefintion!=null)
	      	interfaceDefintion.apply(this);
	    }
	  }
}