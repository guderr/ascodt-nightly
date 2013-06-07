package de.tum.ascodt.plugin.ui.gef.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * 
 * @author Mihai
 * link to a comment/note
 *
 */
public class Link extends ModelElement{


	private static final long serialVersionUID = 1L;

	/** 
	 * Used for indicating that a Link with dotted line style should be created.
	 */
	public static final String DASH_CONNECTION = "Dash";

	/** Property ID to use when the line style of this link is modified. */
	public enum LinkType
	{
		DASH_CONNECTION
	}
	public static final String LINESTYLE_PROP = "LineStyle";
	protected static final IPropertyDescriptor[] descriptors = new IPropertyDescriptor[1];
	private static final String DASH_STR = "Dash Node";

	/** True, if the link is attached to its point. */ 
	private boolean isConnected;
	/** Line drawing style for this link. */
	private int lineStyle = Graphics.LINE_DASH;
	/** Link's source */
	private Component source;
	/** Link's target */
	private StickyNote target;
	
	private Class<?> type;
	static {
		descriptors[0] = new ComboBoxPropertyDescriptor(LINESTYLE_PROP, LINESTYLE_PROP, 
				new String[] {DASH_STR});
	}

	/** 
	 * Create a link between a component and a sticky note.
	 * @param source a source endpoint for this link (non null)
	 * @param target a target endpoint for this link (non null)
	 * @throws IllegalArgumentException if any of the parameters are null or source == target
	 * @see #setLineStyle(int) 
	 */
	public Link(Component source, StickyNote target) {
		reconnect(source, target);

	}
	public Class<?> getType(){
		return type;
	}

	/**
	 * Returns the line drawing style of this connection.
	 * @return an int value (Graphics.LINE_DOT) 
	 */
	public int getLineStyle() {
		return lineStyle;
	}

	/**
	 * Returns the descriptor for the lineStyle property
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	/**
	 * Returns the lineStyle as String for the Property Sheet
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
	 */
	public Object getPropertyValue(Object id) {
		if (id.equals(LINESTYLE_PROP)) {
			if (getLineStyle() == Graphics.LINE_DASH)
				return "Dash";
			return "";
		}
		return super.getPropertyValue(id);
	}
	
	/**
	 * Returns the source of this link.
	 * @return a non-null Component instance
	 */
	public Component getSource() {
		return source;
	}

	/**
	 * Returns the target of this link.
	 * @return a non-null StickyNote instance
	 */
	public StickyNote getTarget() {
		return target;
	}
	
	/** 
	 * Target was deleted, disconnect this link from the source component it is attached to.
	 */
	public void targetRemoved(){
		target = null;
		if (source != null){
			source.removeLink(this);
			isConnected = false;
		}
	}
	
	/** 
	 * Source was deleted, disconnect this link from the target note it is attached to.
	 */
	public void sourceRemoved(){
		source = null;
		if (target != null){
			target.removeLink(this);
			isConnected = false;
		}
	}
	/** 
	 * Disconnect this link from the components it is attached to.
	 */
	public void disconnect() {
		if (isConnected) {			
			source.removeLink(this);
			target.removeLink(this);
			isConnected = false;
		}
	}

	/** 
	 * Reconnect this link. 
	 * The link will reconnect with the component/sticky note it was previously attached to.
	 */  
	public void reconnect() {
		if (!isConnected) {
			source.addLink(this);
			target.addLink(this);
			isConnected = true;			
		}
	}
	
	/**
	 * Reconnect to a different source component/ target sticky note.
	 * The link will disconnect from its current attachments and reconnect to 
	 * the new source and target. 
	 * @param newSource a new source endpoint for this link (non null)
	 * @param newTarget a new target endpoint for this link (non null)
	 * @throws IllegalArgumentException if any of the parameters are null or newSource == newTarget
	 */
	public void reconnect(Component newSource, StickyNote newTarget) {
		if (newSource == null || newTarget == null) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.source = newSource;
		this.target = newTarget;
		reconnect();		
	}

	/**
	 * Set the line drawing style of this connection.
	 * @param lineStyle one of following values: Graphics.LINE_DOT
	 * @see Graphics#LINE_DOT
	 * @throws IllegalArgumentException if lineStyle does not have the above value
	 */
	public void setLineStyle(int lineStyle) {
		if (lineStyle != Graphics.LINE_DASH) {
			throw new IllegalArgumentException();
		}
		this.lineStyle = lineStyle;
		firePropertyChange(LINESTYLE_PROP, null, new Integer(this.lineStyle));
	}

	/**
	 * Sets the lineStyle based on the String provided by the PropertySheet
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(LINESTYLE_PROP))
			setLineStyle(Graphics.LINE_DASH);
		else
			super.setPropertyValue(id, value);
	}
	
}

