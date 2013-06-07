// Copyright (C) 2009 Technische Universitaet Muenchen
// This file is part of the ASCoDT project. For conditions of distribution and
// use, please see the copyright notice at www5.in.tum.de/ascodt
package de.tum.ascodt.sidlcompiler.symboltable;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;


/**
 * Symbol Table
 * 
 * The symbol table is a simple wrapper for a hash map mapping every ast node
 * to a scope. It is also a singleton.
 * 
 * !!! Access any identifier
 * 
 * To work with an identifier always follows the same strategy. One takes the 
 * node of the AST and passes it to getScope(). The returned scope then is 
 * able to transfer the identifier into several representations. 
 * 
 * @author Tobias Weinzierl
 */
public class SymbolTable {  
  /**
   * Map from the nodes to the scopes.
   */
  private java.util.Map<String, Scope > _mapping;
  
  /**
   * Global scope, i.e. the scope corresponding to the forrest of AST root 
   * nodes.
   */
  private Scope                                                   _globalScope;
    
  public SymbolTable() {
    _mapping     = new java.util.HashMap<String, Scope >();
    _globalScope = new Scope();
  }  
  
  public SymbolTable(SymbolTable old){
	  this._mapping=new HashMap<String,Scope>(old._mapping);
	  this._globalScope=new Scope(old._globalScope);
  }

  /**
   * Returns the scope belonging to a node. 
   * 
   * @param node
   * @return
   */
  public Scope getScope(de.tum.ascodt.sidlcompiler.frontend.node.Node node) {
    Scope result = _mapping.get(Scope.getSymbol(node));
    return result;
  }
  
  public void setScope(de.tum.ascodt.sidlcompiler.frontend.node.Node node,Scope scope) {
    _mapping.put(Scope.getSymbol(node),scope);
  }
  
  public Scope getGlobalScope() {
    return _globalScope;
  }
}
