package de.tum.ascodt.plugin.ui.gef.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * 
 * @author mihai
 *	This class represents a sticky note for comments
 */

public class StickyNote extends Geometry{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Property ID to use when the list of incoming links is modified. */
	public static final String TARGET_LINKS_PROP = "StickyNote.TargetLink";
	/** Property ID to use when the text of a note is modified. */	
	public static final String TEXT_PROP = "StickyNote.ChangeText";	
	
	protected Vector<Link> targetLinks = new Vector<Link>();
		
	private String text = "Comment:";
	
	/** initial size of this note. */
	protected Dimension noteSize = new Dimension(100, 50);
		
	public StickyNote(){		
	}
		
	public Vector<Link> getTargetLinks() {
		return targetLinks;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;		
	}

	
	/**
	 * signals that text has changed, so that figure is updated once
	 * editing of StickyNote is done
	 * @param oldText previous text of the StickyNote
	 * @param newText next text of the StickyNote
	 */
	public void changeText(String oldText, String newText){		
		firePropertyChange(TEXT_PROP, oldText, newText);	
	}
		
	/**
	 * Return the Size of this note.
	 * @return a non-null Dimension instance
	 */
	public Dimension getSize() {
		return noteSize.getCopy();
	}
	
	/**
	 * Set the Size of this shape.
	 * Will not modify the size if newSize is null.
	 * @param newSize a non-null Dimension instance or null
	 */
	public void setSize(Dimension newSize) {
		if (newSize != null) {
			noteSize.setSize(newSize);
			firePropertyChange(SIZE_PROP, null, size);
		}
	}
	
	public void addLink(Link link) {
		
		if (link == null) {
			throw new IllegalArgumentException();
		}
		if ((link.getTarget() instanceof StickyNote) 
				&& (((StickyNote)link.getTarget()) == this)) {			
			targetLinks.add(link);
			firePropertyChange(TARGET_LINKS_PROP, null, link);
		} 		
	}
	
	public void removeLink(Link link) {		
		if (link == null) {
			throw new IllegalArgumentException();
		}
		if ((link.getTarget() instanceof StickyNote) 
				&& (((StickyNote)link.getTarget()) == this)
				&& (targetLinks.contains(link))) {
			targetLinks.remove(link);
			firePropertyChange(TARGET_LINKS_PROP, link, null);	
		} 		
	}
}
