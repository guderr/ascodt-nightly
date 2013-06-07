package de.tum.ascodt.repository;

/**
 * 
 * @author Tobias Weinzierl
 */
public interface RepositoryListener {
  /**
   * Tell the listener that there will now be a couple of notify calls (one per 
   * component description). A palette view, e.g., should erase the 
   * visualisation on this call and then add sucessively new components.  
   */
  public void begin();
  
  /**
   * O.k. this have been all notify() calls.
   */
  public void end();
  public void notify(String componentInterface,String target);
}
