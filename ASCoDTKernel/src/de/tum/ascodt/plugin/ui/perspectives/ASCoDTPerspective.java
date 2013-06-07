package de.tum.ascodt.plugin.ui.perspectives;


import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import de.tum.ascodt.plugin.ui.views.ASCoDTNavigator;
import de.tum.ascodt.plugin.ui.views.Palette;
import de.tum.ascodt.plugin.ui.views.UIViewContainer;


/**
 * This class defines the content of the ASCoDT perspective.
 * 
 * @author Atanas Atanasov, Tobias Weinzierl
 *
 */
public class ASCoDTPerspective implements IPerspectiveFactory {
  
  /**
   * Unique IDE identifying this class. 
   */
	public static final String ID = "de.tum.ascodt.plugin.perspectives.ASCoDTPerspective";
	

	/**
	 * This method creates the layout of the ascodt perspective. We split the IDE in three
	 * region:
	 * *left: here we put different views for the navigation
	 * *right: here we put the container for user defined graphical interfaces a VTK view
	 * *bottom: here we put the console and problem explorer
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout left =
			layout.createFolder("left", IPageLayout.LEFT, (float) 0.2, editorArea);
		IFolderLayout right =
			layout.createFolder("right", IPageLayout.RIGHT, (float) 0.8, editorArea);
		IFolderLayout bottom =
			layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.8, editorArea);

		// add the placeholder on the right for the ASCoDT palette
		left.addView(ASCoDTNavigator.ID);
		left.addView(IPageLayout.ID_PROJECT_EXPLORER );
		right.addView(Palette.ID);
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
    
    // @todo Das sollte automatisch aufgehen

    
    
    //        right.addView(VTKView.ID);
    right.addPlaceholder(UIViewContainer.ID+":*");
//  right.addPlaceholder(VTKView.ID+":*");
//  right.addPlaceholder(ASCoDTRemoteComponentsView.ID+":*");
//  
//  layout.addShowViewShortcut(UIContainer.ID);
//    layout.addShowViewShortcut(VTKView.ID);
//      layout.addShowViewShortcut(PaletteView.ID);
//      left.addView("org.eclipse.jdt.ui.PackageExplorer");
//      left.addView(IPageLayout.ID_PROJECT_EXPLORER );
//      left.addView(IPageLayout.ID_RES_NAV );
//      left.addView(IPageLayout.ID_OUTLINE);
//      bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
//
//      bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
	}
}
