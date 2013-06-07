package de.tum.ascodt.plugin.ui.palette;



import org.eclipse.gef.palette.CombinedTemplateCreationEntry;


import de.tum.ascodt.plugin.repository.InstanceFactory;


/**
 * An Entry for the palette. Such an entry should hold the 
 * correct component name, the factory, which will instantiate the component,
 * an image for the component
 * @author Atanas Atanasov
 *
 */
public class PaletteComponentEntry extends CombinedTemplateCreationEntry {

	/**
	 * 
	 * @param componentName name of the component
	 * @param instanceFactory factory to be used by instantiation
	 */
	public PaletteComponentEntry(String componentName,InstanceFactory instanceFactory) {

		super(componentName,"",
				instanceFactory,
				null,null);
		

	}


	
}
