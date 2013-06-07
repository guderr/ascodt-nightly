package de.tum.ascodt.sidlcompiler.astproperties;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
import de.tum.ascodt.sidlcompiler.frontend.node.APackage;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;


/**
 * Compares two ASTs.
 * The idea of this compare is pretty simple: the depth-first traversal is done
 * two times. The first dfs streams the tokens of interest into a queue. The 
 * second dfs ran on the second AST compares the traversal actions to this 
 * queue.   
 * 
 * @author Tobias Weinzierl
 */
public class ASTEquals extends DepthFirstAdapter {
  private java.util.Queue< String >  _queue;
  
  private boolean                    _isInCompareMode;
  
  private String                     _differenceDescription;
  
  
  public ASTEquals() {
    _queue                 = new java.util.LinkedList< String >();
    _differenceDescription = "";
    _isInCompareMode       = false;
  }
  
  
  public boolean ASTsAreEqual() {
    return _isInCompareMode && _differenceDescription.equals("") && _queue.isEmpty();
  }
  
  
  public String getDifferenceDescription() {
    return _differenceDescription;
  }  
  
  public void switchToCompareMode() {
    Assert.isTrue( !_isInCompareMode );
    _isInCompareMode = true;
  }
  
  
  public void inAClassPackageElement(AClassPackageElement node) {
    if (_isInCompareMode) {
      String classIdentifier  = _queue.poll();
      String className        = _queue.poll();
      String numberOfProvides = _queue.poll(); 
      String numberOfUses     = _queue.poll();
      String target           = _queue.poll();
      if (classIdentifier==null || !classIdentifier.equals( "class" ) ) {
        _differenceDescription += "expected class but got " + classIdentifier + "\n";
      }
      if (className==null || !className.equals( node.getName().getText() )) {
        _differenceDescription += "expected class " + className + " but got " + node.getName() + "\n";
      }
      if (numberOfProvides==null ||Integer.parseInt(numberOfProvides)!=node.getProvides().size()) {
      	
        _differenceDescription += "expected " + numberOfProvides + " provides ports but got " + node.getProvides().size() + "\n";
      }
      if (numberOfUses==null || Integer.parseInt(numberOfUses)!=node.getUses().size()) {
        _differenceDescription += "expected " + numberOfUses + " uses ports but got " + node.getUses().size() + "\n";
      }
      if (node.getTarget()!=null) {
        if (!node.getTarget().getText().equals(target))
          _differenceDescription += "expected " + target + " uses ports but got " + node.getTarget().getText() + "\n";
      }
      else if (!target.equals("no-target")){
        _differenceDescription += "expected no target for class but got " + target + "\n";
      }

    }
    else {
      _queue.add( "class" );
      _queue.add( node.getName().getText() );
      _queue.add( Integer.toString(node.getProvides().size()) );
      _queue.add( Integer.toString(node.getUses().size()) );
      if (node.getTarget()!=null) {
        _queue.add( node.getTarget().getText() );
      }
      else {
        _queue.add( "no-target" );
      }
    }
  }
  
  public void inAInterfacePackageElement(AInterfacePackageElement node) {
    if (_isInCompareMode) {
      String classIdentifier    = _queue.poll();
      String className          = _queue.poll();
      String numberOfSuperTypes = _queue.poll(); 
      if (classIdentifier==null || !classIdentifier.equals( "interface" ) ) {
        _differenceDescription += "expected interface but got " + classIdentifier + "\n";
      }
      if (className==null || !className.equals( node.getName().getText() )) {
        _differenceDescription += "expected interface " + className + " but got " + node.getName() + "\n";
      }
      if (numberOfSuperTypes==null || Integer.parseInt( numberOfSuperTypes)!=node.getSupertype().size()) {
        _differenceDescription += "expected " + numberOfSuperTypes + " super types but got " + node.getSupertype().size() + "\n";
      }
    }
    else {
      _queue.add( "interface" );
      _queue.add( node.getName().getText() );
      _queue.add( Integer.toString(node.getSupertype().size()) );
    }
  }
  
  
  public void inAUserDefinedType(AUserDefinedType node) {
    if (_isInCompareMode) {
      String extendsIdentifier = _queue.poll();
      String fullQualifiedName = _queue.poll();
      if (extendsIdentifier==null || !extendsIdentifier.equals( "user-defined-type" ) ) {
        _differenceDescription += "expected used defined type but got " + extendsIdentifier + "\n";
      }
      if (fullQualifiedName==null || !fullQualifiedName.equals( Scope.getSymbol(node)  )) {
        _differenceDescription += "expected type " + fullQualifiedName + " but got " + Scope.getSymbol(node)  + "\n";
      }
    }
    else {
      _queue.add( "user-defined-type" );
      _queue.add( Scope.getSymbol(node) );
    }
  }

  
  public void inAOperation(AOperation node) {
    if (_isInCompareMode) {
      String operationIdentifier = _queue.poll();
      String name                = _queue.poll();
      String numberOfArgumentsAsString = _queue.poll();
      if (operationIdentifier==null || !operationIdentifier.equals( "operation" ) ) {
        _differenceDescription += "expected operation but got " + operationIdentifier + "\n";
      }
      if (name==null || !name.equals( node.getName().getText() )) {
        _differenceDescription += "expected operation name " + name + " but got " + node.getName().getText()   + "\n";
      }
      if (numberOfArgumentsAsString==null || Integer.parseInt(numberOfArgumentsAsString)!=node.getParameter().size()) {
        _differenceDescription += "expected " + numberOfArgumentsAsString+ " parameters but got " + node.getParameter().size() + "\n";
      }
    }
    else {
      _queue.add( "operation" );
      _queue.add( node.getName().getText() );
      _queue.add( Integer.toString(node.getParameter().size()) );
    }
  }
  

  public void inAPackage(APackage node) {
    _differenceDescription += "got a subpackage, but operation AST equals is not defined on subpackages \n";
  }
  
  
  public void inAParameterInParameter(AParameterInParameter node) {
    if (_isInCompareMode) {
      String parameterIdentifier = _queue.poll();
      String name                = _queue.poll();
      String type                = _queue.poll(); 
      if (parameterIdentifier==null ||!parameterIdentifier.equals( "in-parameter" ) ) {
        _differenceDescription += "expected in parameter but got " + parameterIdentifier + "\n";
      }
      if (name==null || !name.equals( node.getName().getText() )) {
        _differenceDescription += "expected parameter with name " + name + " but got " + node.getName().getText()   + "\n";
      }
      if (type==null || !type.equals(node.getType().getClass().getCanonicalName()) ) {
        _differenceDescription += "expected parameter type " + type+ " but got " + node.getType().toString() + "\n";
      }
    }
    else {
      _queue.add( "in-parameter" );
      _queue.add( node.getName().getText() );
      _queue.add( node.getType().getClass().getCanonicalName());
    }
  }
  

  public void inAParameterOutParameter(AParameterInParameter node) {
    if (_isInCompareMode) {
      String parameterIdentifier = _queue.poll();
      String name                = _queue.poll();
      String type                = _queue.poll(); 
      if (parameterIdentifier==null || !parameterIdentifier.equals( "out-parameter" ) ) {
        _differenceDescription += "expected out parameter but got " + parameterIdentifier + "\n";
      }
      if (name==null || !name.equals( node.getName().getText() )) {
        _differenceDescription += "expected parameter with name " + name + " but got " + node.getName().getText()   + "\n";
      }
      if (type==null || !type.equals(node.getType().toString()) ) {
        _differenceDescription += "expected parameter type " + type+ " but got " + node.getType().toString() + "\n";
      }
    }
    else {
      _queue.add( "out-parameter" );
      _queue.add( node.getName().getText() );
      _queue.add( node.getType().toString() );
    }
  }
  
  
  public void inAParameterArrayInParameter(AParameterArrayInParameter node) {
    if (_isInCompareMode) {
      String parameterIdentifier = _queue.poll();
      String name                = _queue.poll();
      String type                = _queue.poll(); 
      if (parameterIdentifier==null || !parameterIdentifier.equals( "in-array-parameter" ) ) {
        _differenceDescription += "expected in array parameter but got " + parameterIdentifier + "\n";
      }
      if (name==null ||  !name.equals( node.getName().getText() )) {
        _differenceDescription += "expected parameter with name " + name + " but got " + node.getName().getText()   + "\n";
      }
      if (type==null || !type.equals(node.getType().toString()) ) {
        _differenceDescription += "expected parameter type " + type+ " but got " + node.getType().toString() + "\n";
      }
    }
    else {
      _queue.add( "in-array-parameter" );
      _queue.add( node.getName().getText() );
      _queue.add( node.getType().toString() );
    }
  }

  
  public void inAParameterArrayOutParameter(AParameterArrayInParameter node) {
    if (_isInCompareMode) {
      String parameterIdentifier = _queue.poll();
      String name                = _queue.poll();
      String type                = _queue.poll(); 
      if (parameterIdentifier==null || !parameterIdentifier.equals( "out-array" ) ) {
        _differenceDescription += "expected out array parameter but got " + parameterIdentifier + "\n";
      }
      if (name==null || !name.equals( node.getName().getText() )) {
        _differenceDescription += "expected parameter with name " + name + " but got " + node.getName().getText()   + "\n";
      }
      if (type==null || !type.equals(node.getType().toString()) ) {
        _differenceDescription += "expected parameter type " + type+ " but got " + node.getType().toString() + "\n";
      }
    }
    else {
      _queue.add( "out-array-parameter" );
      _queue.add( node.getName().getText() );
      _queue.add( node.getType().toString() );
    }
  }
}

