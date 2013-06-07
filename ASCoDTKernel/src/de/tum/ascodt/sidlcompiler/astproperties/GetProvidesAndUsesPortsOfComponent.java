package de.tum.ascodt.sidlcompiler.astproperties;

import java.util.Map;

import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.frontend.node.AUses;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;

public class GetProvidesAndUsesPortsOfComponent extends DepthFirstAdapter {
  private static Trace                      _trace = new Trace( "de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent" );

  /**
   * Entries are separated by .
   */
  private java.util.Set<String>  _providesPorts;

  /**
   * Entries are separated by .
   */
  private java.util.Map<String,String> _usesPorts;
  private boolean  _currentlyInProvidesPort; 
  private String _portName;
  public GetProvidesAndUsesPortsOfComponent() {
    _trace.in( "GetProvidesAndUsesPortsOfComponent(SymbolTable)" );
    _providesPorts = new java.util.HashSet<String>();
    _usesPorts     = new java.util.HashMap<String,String>();
   
    _currentlyInProvidesPort = true;
    _trace.out( "GetProvidesAndUsesPortsOfComponent(SymbolTable)" );
  }

  public void inAUses(AUses node) {
  	 _portName = node.getAs().getText();
    _currentlyInProvidesPort = false;
  }

  public void outAUses(AUses node) {
    _currentlyInProvidesPort = true;
  }
  
  public void inAUserDefinedType(AUserDefinedType node) {
    String portSymbol = Scope.getSymbol(node);
    
    _trace.in( "inAUserDefinedType(...)", Boolean.toString(_currentlyInProvidesPort), portSymbol );
    if (_currentlyInProvidesPort) {
      _providesPorts.add(portSymbol);
    }
    else {
      _usesPorts.put(_portName,portSymbol);
    }
    _trace.out( "inAUserDefinedType(...)" );
  }
  
  private String getListOfPorts( String listSeparator, String namespaceSeparator, java.util.Collection<String> collection ) {
    String result = "";
    
    for(String currentPort: collection) {
      result += currentPort.replaceAll( "[.]", namespaceSeparator);
      result += listSeparator;
    }
    if (result.lastIndexOf(listSeparator)!=-1) {
      result = result.substring(0,result.lastIndexOf(listSeparator));
    }
    return result;
  }
  
	private String getListOfPortsWithAsIdentifiers(String listSeparator,
			String namespaceSeparator, Map<String, String> usePorts) {
		String result = "";
    
    for(java.util.Map.Entry<String,String> currentPort: usePorts.entrySet()) {
      result += currentPort.getKey()+listSeparator+currentPort.getValue().replaceAll( "[.]", namespaceSeparator);
      result += listSeparator;
    }
    if (result.lastIndexOf(listSeparator)!=-1) {
      result = result.substring(0,result.lastIndexOf(listSeparator));
    }
    return result;
	}

  /**
   * 
   * @param listSeparator      This string is used to separate two provide port entries in the result. 
   * @param namespaceSeparator This string is used to separate two namespaces within each entry of the result list.
   * @return  List of provides ports.
   */
  public String getProvidesPorts( String listSeparator, String namespaceSeparator ) {
    _trace.in( "getProvidesPorts(...)", listSeparator, namespaceSeparator, Integer.toString(_providesPorts.size()) );
    String result = getListOfPorts(listSeparator,namespaceSeparator,_providesPorts);
    _trace.out( "getProvidesPorts(...)", result );
    return result;
  }


  public String getUsesPorts( String listSeparator, String namespaceSeparator ) {
    _trace.in( "getUsesPorts(...)", listSeparator, namespaceSeparator, Integer.toString(new java.util.HashSet<String>(_usesPorts.values()).size()) );
    String result = getListOfPorts(listSeparator,namespaceSeparator,new java.util.HashSet<String>(_usesPorts.values()));
    _trace.out( "getUsesPorts(...)", result );
    return result;
  }
  
  public String getUsesPortsAndAsIdentifiers( String listSeparator, String namespaceSeparator ){
  	_trace.in( "getUsesPortsAndAsIdentifiers(...)", listSeparator, namespaceSeparator, Integer.toString(_usesPorts.size()) );
    String result = getListOfPortsWithAsIdentifiers(listSeparator,namespaceSeparator,_usesPorts);
    _trace.out( "getUsesPortsAndAsIdentifiers(...)", result );
    
    return result;
  }


  
}
