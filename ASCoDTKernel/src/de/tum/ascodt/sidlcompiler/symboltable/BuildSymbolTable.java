// Copyright (C) 2009 Technische Universitaet Muenchen
// This file is part of the ASCoDT project. For conditions of distribution and
// use, please see the copyright notice at www5.in.tum.de/ascodt
package de.tum.ascodt.sidlcompiler.symboltable;


import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AEnumDeclarationPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.APackage;
import de.tum.ascodt.sidlcompiler.frontend.node.Node;
import de.tum.ascodt.sidlcompiler.frontend.node.Start;


/**
 * Create the symbol table.
 * 
 * @author Tobias Weinzierl
 */
public class BuildSymbolTable extends DepthFirstAdapter {
	public enum Mode{
		APPEND,
		MODIFIED,
		REMOVE
	};
	/**
   * Symbol table reference.
   */
  SymbolTable _table;
  
  
  private String _inputFileName;
 
 
  public BuildSymbolTable(
      SymbolTable  table,
      String       inputFileName
    ) {
      _table         = table;    
      _inputFileName = inputFileName;
      
      
    }
  
  public void inStart(Start node) {
    _table.setScope(node, _table.getGlobalScope() );    
  }
  

  public void inAPackage(APackage node) {
    String identifier = node.getName().getText();
    Scope  superScope = _table.getScope(node.parent());

    Scope  subScope;
    if ( superScope.containsSubScope(identifier) ) {
      subScope = superScope.getSubScope(identifier);
    }
    else {
      subScope = new Scope( identifier,_inputFileName,superScope );
    }
    _table.setScope(
      node,
      subScope
    );
  }
  
  
  public void inAClassPackageElement(AClassPackageElement node) {
    defaultIn(node);
    _table.getScope(node).addSymbol(node, _inputFileName);
    
  }
  
  public void inAEnumDeclarationPackageElement(AEnumDeclarationPackageElement node){
  	 defaultIn(node);
  	 _table.getScope(node).addSymbol(node, _inputFileName);
  	
  }
  
  
  public void inAInterfacePackageElement(AInterfacePackageElement node) {
    defaultIn(node);
    _table.getScope(node).addSymbol(node, _inputFileName);
   
  }
  
  
  public void defaultIn(Node node) {
    if( _table.getScope(node.parent())!=null)
	  	_table.setScope(
	      node, 
	      _table.getScope(node.parent())
	    );
  }
}
