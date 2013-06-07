package de.tum.ascodt.plugin.ui.palette;

import java.io.File;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import de.tum.ascodt.plugin.ui.views.Palette;
import de.tum.ascodt.plugin.utils.Exporter;
import de.tum.ascodt.plugin.utils.Importer;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.utils.exceptions.ASCoDTException;



/**
 * Specifies the context menu of the palette. The following
 * actions are supported:
 * 1. exportComponent
 * 2. exportSource
 * 
 * @author Atanas Atanasov
 *
 */
public class ContextMenuProvider extends PaletteContextMenuProvider {

	/**
	 * reference to the palette view
	 */
	private Palette _palette;

	public ContextMenuProvider(Palette palette) {
		super(palette.getViewer());
		_palette=palette;
	}

	/**
	 * This is the method that builds the context menu.
	 * 
	 * @param menu
	 *            The IMenuManager to which actions for the palette's context
	 *            menu can be added.
	 * @see ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		createExportComponentAction(menu);
		createImportComponentAction(menu);
		createDeleteComponentAction(menu);
	}

	/**
	 * creates export component action
	 * @param menu
	 */
	private void createExportComponentAction(IMenuManager menu) {
		Action actionExportComponent =
				new Action(
						Actions.EXPORT_COMPONENT){	
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog fileDialog =new FileDialog(shell,SWT.SAVE);
				fileDialog.setText("Export ASCoDT component");
				fileDialog.setFilterExtensions(new String[]{"*.ascodt-component"});
				for(Object item:getPaletteViewer().getSelectedEditParts()){
					if(item instanceof EditPart&&((EditPart)item).getModel() instanceof CombinedTemplateCreationEntry ){
						CombinedTemplateCreationEntry selectedEntry=((CombinedTemplateCreationEntry) ((EditPart)item).getModel());
						fileDialog.setFileName(selectedEntry.getLabel()+".ascodt-component");
						String destination=fileDialog.open();
						int rc = SWT.OK;
						if(destination!=null){
							if(new File(destination).exists()){
								int style = SWT.ICON_INFORMATION |SWT.OK | SWT.CANCEL;


								MessageBox messageBox = new MessageBox(shell, style);
								messageBox.setMessage("A file named \""+destination+"\" already exists.  Do you want to replace it?");
								rc = messageBox.open();
							}
							if(rc==SWT.OK){
								try {
									Exporter.exportBinary(selectedEntry.getLabel(), destination, _palette.getProject().getEclipseProjectHandle());
								} catch (ASCoDTException e) {
									ErrorWriterDevice.getInstance().showError( getClass().getName(), "createExportComponentAction()",  "Cannot export component binaries", e );

								}

							}
						}
						break;
					}
				}
			}
		};

		ActionContributionItem itemExport = new ActionContributionItem(actionExportComponent);
		menu.add(itemExport);

	}

	/**
	 * creates export component action
	 * @param menu
	 */
	private void createImportComponentAction(IMenuManager menu) {
		Action actionExportComponent =
				new Action(
						Actions.IMPORT_COMPONENT){	
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog fileDialog =new FileDialog(shell,SWT.OPEN);
				fileDialog.setText("Import ASCoDT component");
				fileDialog.setFilterExtensions(new String[]{"*.ascodt-component"});
				String source=fileDialog.open();
				if(source!=null){
					try {
						Importer.importBinary(source, _palette.getProject().getEclipseProjectHandle());
					} catch (ASCoDTException e) {
						ErrorWriterDevice.getInstance().showError( getClass().getName(), "createExportComponentAction()",  "Cannot export component binaries", e );

					}
				}
			}
		};

		ActionContributionItem itemImport = new ActionContributionItem(actionExportComponent);
		menu.add(itemImport);

	}

	/* deletes component action
	 * @param menu
	 */
	private void createDeleteComponentAction(IMenuManager menu) {
		Action actionDeleteComponent =
				new Action(
						Actions.DELETE_COMPONENT){	
			public void run() {
				_palette.deleteSelectedItem();
			}
		};

		ActionContributionItem itemDelete= new ActionContributionItem(actionDeleteComponent);
		menu.add(itemDelete);
		
	}
}
