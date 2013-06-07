package de.tum.ascodt.repository.entities;

import de.tum.ascodt.repository.Target;

/**
 * Represents one component
 * 
 * @author Tobias Weinzierl
 */
public interface Component {
  /**
   * Before anybody invokes an operation on an component object, he has to call 
    lock. Only after lock() returns you may do something with the object. Do 
    not forget to call unlock() afterwards.  
   */
  void lock();
 
  /**
   * Unlock component.
   *
   * This is the counterpart of lock(). 
   * 
   * @see lock() 
   */
  void unlock();
  
  /**
   * Self-explaining. 
   * 
   * @return
   */
  boolean isValid();

  /**
   * If this field is set, the workbench provides an open button for the GUI. 
   * 
   * @return
   */
  boolean hasGUI();
  
  void openGUI();
  void closeGUI();
  
  /**
   * Yields the target object, i.e. a description about the location and mode 
   * in which this component is running.
   * 
   * @return
   */
  Target getTarget();
  
  String getInstanceName();
  
  void setInstanceName(String instanceName);

	void destroy();
}
