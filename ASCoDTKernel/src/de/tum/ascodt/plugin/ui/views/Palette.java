package de.tum.ascodt.plugin.ui.views;


import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.wizards.IWizardDescriptor;

import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.project.ProjectBuilder;
import de.tum.ascodt.plugin.ui.palette.ContextMenuProvider;
import de.tum.ascodt.plugin.ui.palette.PaletteFactory;
import de.tum.ascodt.plugin.ui.wizards.NewComponentWizard;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.repository.RepositoryListener;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

import org.eclipse.jface.viewers.LabelProvider;
/**
 * 
 * The palette view: This a View class for the static ASCoDT model. The view is a listener
 * to the static model.
 * @author Atanas Atanasov
 * 
 *
 */
public class Palette extends ViewPart implements RepositoryListener {
	public static String ID = Palette.class.getCanonicalName();

	/**
	 * reference to the gef palette viewer component
	 */
	private PaletteViewer _paletteViewer;

	/**
	 * the palette model reference
	 */
	private PaletteRoot _palette_model;

	
	

	private ComboViewer combo;
	private Project _lastSelection;

	/**
	 * creates the user interface for the palette view
	 */
	@Override
	public void createPartControl(Composite parent) {

		_paletteViewer = new PaletteViewer();
		_paletteViewer.createControl(parent);
		
		configurePaletteViewer();
		initializePaletteViewer();
		createToolbar();
		ProjectBuilder.getInstance().setPalette(this);
	}


	private void initializePaletteViewer() {
		_paletteViewer.addDragSourceListener(
				new TemplateTransferDragSourceListener(_paletteViewer));
	}


	/**
	 * Create toolbar with some buttons to create, insert or remove components.
	 */
	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		ControlContribution projectsControl = new ControlContribution("Projects:"){
			
			@Override
			protected Control createControl(Composite parent) {
				combo = new ComboViewer(parent,SWT.None);
				ProjectContentProvider provider=(ProjectContentProvider) ProjectBuilder.getInstance().getProjectsContentProvider();
				provider.setViewer(combo);
				combo.setContentProvider(provider);
				combo.setInput(ProjectBuilder.getInstance().getProjects());
				combo.setLabelProvider(new LabelProvider(){
					@Override
					public String getText(Object element) {
						if (element instanceof Project) {
							Project project = (Project) element;
							return project.getName();
						}
						return super.getText(element);
					}
				});
				combo.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
						if(selection.getFirstElement() instanceof Project){
							if(_lastSelection!=null)
								_lastSelection.getStaticRepository().removeListener(Palette.this);
							_lastSelection=(Project) selection.getFirstElement();
							_lastSelection.getStaticRepository().addListener(Palette.this);
							_lastSelection.getStaticRepository().informListenersAboutChangedComponents();
						}
					}
				});
				if(ProjectBuilder.getInstance().getProjects().size()>0)
					combo.setSelection(new StructuredSelection(ProjectBuilder.getInstance().getProjects().iterator().next()));
				return combo.getControl();
			}

		};
		mgr.add(projectsControl);
		mgr.add(new Action("New component..."){
			public void run() {
				IWizardDescriptor descriptor = PlatformUI.getWorkbench()
				.getNewWizardRegistry().findWizard(NewComponentWizard.ID);
				try {
					// Then if we have a wizard, open it.
					if (descriptor != null) {
						IWizard wizard = descriptor.createWizard();
						WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
						wd.setTitle(wizard.getWindowTitle());
						wd.open();
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * - >this method adds some of the default tools and menus to the palette:
	 * ->selection tools, connection tools, etc..
	 * ->standart action menus and ASCoDT menus
	 */
	protected void configurePaletteViewer(){
		PaletteFactory paletteFactory = new PaletteFactory(); 
		_palette_model = paletteFactory.createPalette();
		_paletteViewer.setPaletteRoot(_palette_model);
		//setup the context menus
		_paletteViewer.setContextMenu(new ContextMenuProvider(this));
		GEFActionConstants.addStandardActionGroups( _paletteViewer.getContextMenu() );
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return
	 */
	private PaletteContainer getComponentsGroupFromPalette() {
		for(Object item:_palette_model.getChildren())
			if(item instanceof PaletteContainer&&((PaletteContainer)item).getLabel().equals("Available components"))
				return ((PaletteContainer)item);
		return null;
	}

	/**
	 * here the view should remove all entries from the palette
	 */
	@Override
	public void begin() {

		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				Vector<PaletteEntry> entries=new Vector<PaletteEntry>();
				for(Object paletteEntry:getComponentsGroupFromPalette().getChildren()){
					if(paletteEntry instanceof PaletteEntry)
						entries.add((PaletteEntry)paletteEntry);
				}
				for(PaletteEntry entry:entries)
					getComponentsGroupFromPalette().remove(entry);
			}
		});

	}


	@Override
	public void end() {
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				_paletteViewer.getControl().redraw();
			}

		});
	}

	public void deleteSelectedItem(){
		if(_paletteViewer.getSelection() instanceof StructuredSelection &&
				((StructuredSelection)_paletteViewer.getSelection()).getFirstElement() instanceof ToolEntryEditPart
				&& 	((ToolEntryEditPart)((StructuredSelection)_paletteViewer.getSelection()).getFirstElement()).getModel() instanceof CombinedTemplateCreationEntry){
			getProject().getStaticRepository().removeComponent(
					((CombinedTemplateCreationEntry)	((ToolEntryEditPart)((StructuredSelection)_paletteViewer.getSelection()).getFirstElement()).getModel()).getLabel()
					);
			getProject().getStaticRepository().informListenersAboutChangedComponents();
		}
	}
	/**
	 * the view is notified for an existing configuration
	 */
	@Override
	public void notify(final String componentInterface,final String target) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				try {
					getComponentsGroupFromPalette().add(new CombinedTemplateCreationEntry(componentInterface,"",
							ProjectBuilder.getInstance().getNewInstanceFactory(Palette.this.getProject(),componentInterface,target),
							null,null));
				} catch (ASCoDTException e) {
					ErrorWriterDevice.getInstance().showError( getClass().getName(), "notify()",  "Cannot add component to palette", e );
		      
				}
			}

		});


	}

	

	public void setProject(Project project){
		if(project!=null)
			combo.setSelection(new StructuredSelection(project));
		
	}
	
	/**
	 * Getter for the current palette project
	 * @return
	 */
	public Project getProject(){
		return (Project)((StructuredSelection)combo.getSelection()).getFirstElement();
	}
	
	public PaletteViewer getViewer() {
		return _paletteViewer;
	}
	
	
}
