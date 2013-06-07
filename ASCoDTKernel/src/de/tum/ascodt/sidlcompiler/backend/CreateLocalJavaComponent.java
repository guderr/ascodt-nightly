package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;
import java.util.Vector;

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
 * This generator is used to create a pure java component. 
 *  
 * @author Tobias Weinzierl
 *
 */
public class CreateLocalJavaComponent extends de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter {
  private static Trace                      _trace = new Trace(CreateLocalJavaComponent.class.getCanonicalName());
  
  private java.util.Stack< TemplateFile >   _templateFilesOfAbstractImplementation;

  private java.util.Stack< TemplateFile >   _templateFilesOfBasisImplementation;
  private java.util.Stack< TemplateFile >   _templateFilesOfPlainImplementation;
  private URL 															_userDirectory;
  private URL                               _generatedDirectory;
  
  private String[]                          _namespace;
  
  private String                            _fullQualifiedNameOfTheComponentImplementation;
  private SymbolTable                       _symbolTable;
  
  private Vector<String>									  _conditions;
  /**
   * Helper
   * 
   * @see inAClassPackageElement()
   */
  private boolean                           _generateProvidesMethods;

  public CreateLocalJavaComponent(
  		SymbolTable symbolTable,
  		URL destinationDirectory,
  		URL userDirectory,
  		String[] namespace,
  		String... conditions) {
    _templateFilesOfAbstractImplementation  = new java.util.Stack< TemplateFile >();
    _templateFilesOfBasisImplementation  = new java.util.Stack< TemplateFile >();
    _templateFilesOfPlainImplementation     = new java.util.Stack< TemplateFile >();
    
    _generatedDirectory = destinationDirectory;
    _userDirectory = userDirectory;
    _namespace            = namespace;
    _symbolTable          = symbolTable;
    _conditions=new Vector<String>();
    for(String condition:conditions)
    	_conditions.add(condition);
  }
   
  
  public void inAClassPackageElement(AClassPackageElement node) {
    _trace.in( "inAClassPackageElement(...)", "open new port interface" );
    try {
      String  componentName              = node.getName().getText();
      String fullQualifiedNameOfTheAbstractComponentImplementation = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "AbstractJavaImplementation";
      String fullQualifiedNameOfTheBasisComponentImplementation = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "BasisJavaImplementation";
      
      _fullQualifiedNameOfTheComponentImplementation               = _symbolTable.getScope(node).getFullQualifiedName(componentName) + "JavaImplementation";
      String  templateFileForAbstractComponentImplementation       = "java-component-abstract-java-implementation.template";
      String  templateFileForComponentImplementation               = "java-component-java-implementation.template";
      String  templateFileForBasisComponentImplementation          = "basis-java-implementation.template";
      
      String  destinationFileForAbstractComponentImplementation    = _generatedDirectory.toString() + File.separatorChar + fullQualifiedNameOfTheAbstractComponentImplementation.replaceAll("[.]", "/") + ".java";
      String  destinationFileForComponentImplementation            = _userDirectory.toString() + File.separatorChar + _fullQualifiedNameOfTheComponentImplementation.replaceAll("[.]", "/")  + ".java";
      String  destinationFileForBasisComponentImplementation            = _userDirectory.toString() + File.separatorChar + fullQualifiedNameOfTheBasisComponentImplementation.replaceAll("[.]", "/") + ".java";
            
      _templateFilesOfAbstractImplementation.push( 
        new TemplateFile( templateFileForAbstractComponentImplementation, destinationFileForAbstractComponentImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava() ,true)
      );
      _templateFilesOfPlainImplementation.push(
        new TemplateFile( templateFileForComponentImplementation, destinationFileForComponentImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava() ,false)
      );
      _templateFilesOfBasisImplementation.push(
          new TemplateFile( templateFileForBasisComponentImplementation, destinationFileForBasisComponentImplementation, _namespace, TemplateFile.getLanguageConfigurationForJava() ,false)
        );
      _templateFilesOfAbstractImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
      _templateFilesOfPlainImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );
      _templateFilesOfBasisImplementation.peek().addMapping( "__COMPONENT_NAME__", componentName );

      _templateFilesOfBasisImplementation.peek().addMapping("__FULL_QUALIFIED_COMPONENT_NAME__",_symbolTable.getScope(node).getFullQualifiedName(componentName));
      _templateFilesOfAbstractImplementation.peek().open();
      _templateFilesOfPlainImplementation.peek().open();
      _templateFilesOfBasisImplementation.peek().open();
      _generateProvidesMethods = true;
      for ( PUserDefinedType definedType: node.getProvides() ) {
        definedType.apply(this);
      }
      _generateProvidesMethods = false;
    }
    catch (ASCoDTException  e ) {
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
    return _fullQualifiedNameOfTheComponentImplementation;
  }
 
  
  /**
   * Close the output streams.
   */
  public void outAClassPackageElement(AClassPackageElement node) {
    Assert.isTrue( _templateFilesOfAbstractImplementation.size()==1 );
    Assert.isTrue(_templateFilesOfBasisImplementation.size()==1 );
    Assert.isTrue(_templateFilesOfPlainImplementation.size()==1 );
    
    try {
      _templateFilesOfAbstractImplementation.peek().close();
      _templateFilesOfPlainImplementation.peek().close();
      _templateFilesOfBasisImplementation.peek().close();
    }
    catch (ASCoDTException  e ) {
      ErrorWriterDevice.getInstance().showError(getClass().getName(), "inAInterfacePackageElement(...)", e);
    }

    _templateFilesOfAbstractImplementation.pop();
    _templateFilesOfBasisImplementation.pop();
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
      String templateFile    ="java-component-abstract-java-implementation-uses-port.template";
     
      
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
      String templateFile    = "java-components-java-implementation-provides-port.template";
      TemplateFile template = new TemplateFile( _templateFilesOfBasisImplementation.peek(), templateFile );

      ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
      node.apply( onlyInParameters );

      GetParameterList parameterList = new GetParameterList(_symbolTable.getScope(node));
      node.apply( parameterList );

      template.addMapping("__OPERATION_NAME__", node.getName().getText());
      template.addMapping( "__OPERATION_PARAMETERS_LIST__" , parameterList.getParameterListInJava(onlyInParameters.areAllParametersInParameters()) );

      template.open();
      template.close();
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
