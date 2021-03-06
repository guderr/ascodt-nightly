//
// ASCoDT - Advanced Scientific Computing Development Toolkit
//
// This file was generated by ASCoDT's simplified SIDL compiler.
//
// Authors: Tobias Weinzierl, Atanas Atanasov   
//
package grs;


public class QuaboxJavaImplementation extends QuaboxBasisJavaImplementation {
  private QuaboxUI _ui;

public QuaboxJavaImplementation( String identifier ) {
    super(identifier);
    _ui= new QuaboxUI(this);
  }
  
  
  public boolean isValid() {
    // @todo Insert your code here
    return true;
  }


  public boolean hasGUI() {
    // @todo Insert your code here
    return true;
  }
  
  
  public void openGUI() {
	  _ui.setVisible(true);
  }
  
  
  public void closeGUI() {
	  _ui.setVisible(false);
  }

  public void destroy(){
     super.destroy();
     _ui.dispose();
  }

}
 


