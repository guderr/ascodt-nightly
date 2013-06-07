package de.tum.ascodt.plugin.ui.palette;

import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;

import de.tum.ascodt.plugin.ASCoDTKernel;
import de.tum.ascodt.plugin.ui.gef.model.Connection;

/**
 * A factory class to create the palette model of the ASCoDTComponent Editor 
 * @author atanasoa
 *
 */
public class PaletteFactory {

	//private static final String CONNECTION_TOOL = "Port connection";
	//private static final String LINK_TOOL = "Sticky Note link";
	//private static final String STICKY_NOTE_TOOL = "Sticky Note";
	private static final String CONNECTION_TOOL = "Port connection";
	public PaletteRoot createPalette() {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroup(palette));
		palette.add(createComponentsGroup());
		return palette;
	}
	
	/**
	 *  Create the "Tools" group. 
	 *  
	 *  @todo Doku und vor allem ausmisten
	 **/
	private PaletteContainer createToolsGroup(PaletteRoot palette) {
		PaletteToolbar toolbar = new PaletteToolbar("Tools");
		// Add a selection tool to the group
		ToolEntry tool = new PanningSelectionToolEntry();
		toolbar.add(tool);
		palette.setDefaultEntry(tool);
		// Add a marquee tool to the group
		toolbar.add(new MarqueeToolEntry());
		// Add an icon for file browsing
		// Add (Plain-port) connection tool 
		tool = new ConnectionCreationToolEntry(
				CONNECTION_TOOL,
				"Create a port connection",
				new CreationFactory(){
					@Override
					public Object getNewObject() {
						return null;
					}

					@Override
					public Object getObjectType() {
						return Connection.SOLID_CONNECTION;						
					}					
				},
				ImageDescriptor.createFromFile(ASCoDTKernel.class, "ui/resources/connection_s16.gif"),
				ImageDescriptor.createFromFile(ASCoDTKernel.class, "ui/resources/connection_s24.gif"));
		toolbar.add(tool);
//		
//		// Add comment link tool
//		tool = new ConnectionCreationToolEntry(
//				LINK_TOOL, "Associate a component with a sticky note",
//				new CreationFactory(){
//					@Override
//					public Object getNewObject() {
//						return null;
//					}
//
//					@Override
//					public Object getObjectType() {
//						return Link.DASH_CONNECTION;						
//					}					
//				}, 
//				ImageDescriptor.createFromFile(ASCoDTPlugin.class, "ui/resources/link.gif"),
//				ImageDescriptor.createFromFile(ASCoDTPlugin.class, "ui/resources/link.gif"));
//		toolbar.add(tool);
//		
//		// Add link to comment tool
//		tool = new CreationToolEntry(
//				STICKY_NOTE_TOOL, 
//				"Add comments to the diagram", 
//
//				new CreationFactory() {
//					public Object getNewObject() {
//						return new StickyNote();						
//					}
//					public Object getObjectType() {
//						return StickyNote.class;
//					}
//				}, 
//				ImageDescriptor.createFromFile(ASCoDTPlugin.class,"ui/resources/note.gif"),
//				ImageDescriptor.createFromFile(ASCoDTPlugin.class,"ui/resources/note.gif"));
//		toolbar.add(tool);
		
		return toolbar;
	}
	
	/**
	 * Creates the group for the components
	 * @return
	 */
	private PaletteContainer createComponentsGroup() {
		PaletteDrawer componentsDrawer = new PaletteDrawer("Available components");
		return componentsDrawer;
	}
}
