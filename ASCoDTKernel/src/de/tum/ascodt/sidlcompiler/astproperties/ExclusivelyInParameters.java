package de.tum.ascodt.sidlcompiler.astproperties;

import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayOutParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterOutParameter;

/**
 * Are there solely out parameters
 * 
 * Instantiate this class and let it run over (parts of) your AST. Then, you 
 * can ask it whether all arguments are out parameters.
 * 
 * @author Tobias Weinzierl
 */
public class ExclusivelyInParameters extends DepthFirstAdapter {
  private boolean _allParametersAreIn;
  
  public ExclusivelyInParameters() {
    _allParametersAreIn = true;
  }
  
  public boolean areAllParametersInParameters() {
    return _allParametersAreIn;
  }
  
  public void inAParameterOutParameter(AParameterOutParameter node) {
    _allParametersAreIn = false;
  }
  
  public void inAParameterArrayOutParameter(AParameterArrayOutParameter node) {
    _allParametersAreIn = false;
  }

}
