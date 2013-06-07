package de.tum.ascodt.plugin.ui.gef.model;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * This models a container for the ASCoDTComponent. It holds the component
 * and the ports as children
 * @author atanasoa
 *
 */
public class ComponentContainer extends Geometry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ComponentContainer(){
		size=new Dimension(220,100);
	}

}
