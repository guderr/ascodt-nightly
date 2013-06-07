package de.tum.ascodt.plugin.ui.gef.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Abstract prototype of a geometry.
 * Has a size (width and height), a location (x and y position) and a list of incoming
 * and outgoing connections. Use subclasses to instantiate a specific geometry.
 * 
 */
public abstract class Geometry extends ModelElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Property ID to use when a child is added to this geometry. */
	public static final String CHILD_ADDED_PROP = "Geometry.ChildAdded";
	/** Property ID to use when a child is removed from this geometry. */
	public static final String CHILD_REMOVED_PROP = "Geometry.ChildRemoved";
	/** 
	 * A static array of property descriptors.
	 * There is one IPropertyDescriptor entry per editable property.
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	//private static IPropertyDescriptor[] descriptors;
	/** ID for the Height property value (used for by the corresponding property descriptor). */
	private static final String HEIGHT_PROP = "Geometry.Height";
	/** Property ID to use when the location of this geometry is modified. */
	public static final String LOCATION_PROP = "Geometry.Location";
	/** Property ID to use then the size of this geometry is modified. */
	public static final String SIZE_PROP = "Geometry.Size";
	/** ID for the Width property value (used for by the corresponding property descriptor). */
	private static final String WIDTH_PROP = "Geometry.Width";
	/** ID for the X property value (used for by the corresponding property descriptor).  */
	private static final String XPOS_PROP = "Geometry.xPos";
	/** ID for the Y property value (used for by the corresponding property descriptor).  */
	private static final String YPOS_PROP = "Geometry.yPos";
	
	/**
	 * The parent geometry
	 */
	protected Geometry parent;
	/** Location of this shape. */
	protected Point location = new Point(0, 0);
	/** Size of this shape. */
	protected Dimension size = new Dimension(200, 100);
	
	/**
	 * Adds a child in the container
	 * @param g the new child
	 * @return
	 */
	public boolean addChild(Geometry g) {
		if (g != null && children.add(g)) {
			firePropertyChange(CHILD_ADDED_PROP, null, g);
			return true;
		}
		return false;
	}
	
	/**
	 * Remove a shape from this diagram.
	 * @param s a non-null shape instance;
	 * @return true, if the shape was removed, false otherwise
	 */
	public boolean removeChild(Geometry g) {
		if (g != null && children.remove(g)) {
			firePropertyChange(CHILD_REMOVED_PROP, null, g);
			return true;
		}
		return false;
	}
	
	/**
	 * Return the Location of this shape.
	 * @return a non-null location instance
	 */
	public Point getLocation() {
		return location.getCopy();
	}
	
	/**
	 * Set the Location of this shape.
	 * @param newLocation a non-null Point instance
	 * @throws IllegalArgumentException if the parameter is null
	 */
	public void setLocation(Point newLocation) {
		if (newLocation == null) {
			throw new IllegalArgumentException();
		}
		location.setLocation(newLocation);
		firePropertyChange(LOCATION_PROP, null, location);
	}
	
	/**
	 * Return the Size of this shape.
	 * @return a non-null Dimension instance
	 */
	public Dimension getSize() {
		return size.getCopy();
	}
	
	/**
	 * Set the Size of this shape.
	 * Will not modify the size if newSize is null.
	 * @param newSize a non-null Dimension instance or null
	 */
	public void setSize(Dimension newSize) {
		if (newSize != null) {
			size.setSize(newSize);
			firePropertyChange(SIZE_PROP, null, size);
		}
	}
	
	/**
	 * Return the property value for the given propertyId, or null.
	 * <p>The property view uses the IDs from the IPropertyDescriptors array 
	 * to obtain the value of the corresponding properties.</p>
	 * @see #descriptors
	 * @see #getPropertyDescriptors()
	 */
	public Object getPropertyValue(Object propertyId) {
		if (XPOS_PROP.equals(propertyId)) {
			return Integer.toString(location.x);
		}
		if (YPOS_PROP.equals(propertyId)) {
			return Integer.toString(location.y);
		}
		if (HEIGHT_PROP.equals(propertyId)) {
			return Integer.toString(size.height);
		}
		if (WIDTH_PROP.equals(propertyId)) {
			return Integer.toString(size.width);
		}
		return super.getPropertyValue(propertyId);
	}

	/**
	 * Set the property value for the given property id.
	 * If no matching id is found, the call is forwarded to the superclass.
	 * <p>The property view uses the IDs from the IPropertyDescriptors array to set the values
	 * of the corresponding properties.</p>
	 * @see #descriptors
	 * @see #getPropertyDescriptors()
	 */
	public void setPropertyValue(Object propertyId, Object value) {
		//TODO add VTK property
		if (XPOS_PROP.equals(propertyId)) {
			int x = Integer.parseInt((String) value);
			setLocation(new Point(x, location.y));
		} else if (YPOS_PROP.equals(propertyId)) {
			int y = Integer.parseInt((String) value);
			setLocation(new Point(location.x, y));
		} else if (HEIGHT_PROP.equals(propertyId)) {
			int height = Integer.parseInt((String) value);
			setSize(new Dimension(size.width, height));
		} else if (WIDTH_PROP.equals(propertyId)) {
			int width = Integer.parseInt((String) value);
			setSize(new Dimension(width, size.height));
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}
	
	/**
	 * Returns an array of IPropertyDescriptors for this shape.
	 * <p>The returned array is used to fill the property view, when the edit-part corresponding
	 * to this model element is selected.</p>
	 * @see #descriptors
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
//	public IPropertyDescriptor[] getPropertyDescriptors() {
//		return descriptors;
//	}
	
}
