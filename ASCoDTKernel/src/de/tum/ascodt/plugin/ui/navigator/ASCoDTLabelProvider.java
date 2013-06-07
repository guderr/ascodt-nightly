package de.tum.ascodt.plugin.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.tum.ascodt.plugin.project.Project;

/**
 * This class defines the way to show the labels and icons for the ASCoDT navigator
 * @author atanasoa
 *
 */
public class ASCoDTLabelProvider extends LabelProvider implements ILabelProvider {

	/**
	 * @return the image for the given element
	 */
	@Override
	public Image getImage(Object element) {
		if(element instanceof Project)
			return PlatformUI.getWorkbench().getSharedImages()
      .getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
		else if(element instanceof IFolder){
			return PlatformUI.getWorkbench().getSharedImages()
      .getImage(ISharedImages.IMG_OBJ_FOLDER);
		}else if(element instanceof IFile){
			return PlatformUI.getWorkbench().getSharedImages()
      .getImage(ISharedImages.IMG_OBJ_FILE);
		}
		return null;
	}

	/**
	 * @return the label for the given element
	 */
	@Override
	public String getText(Object element) {
		if(element instanceof Project)
			return ((Project)element).getName();
		else if (element instanceof IResource){
			return ((IResource)element).getName();
		}
		return null;
	}

}
