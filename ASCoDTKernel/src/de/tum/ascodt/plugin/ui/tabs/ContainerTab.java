package de.tum.ascodt.plugin.ui.tabs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;

import de.tum.ascodt.plugin.ASCoDTKernel;


/**
 * A container tab class. An abstract class allowing to add tabs to tab folder control
 * @author Atanas Atanasov
 *
 */


public abstract class ContainerTab {
	/**
	 * the page which should be used as parent for the user UIs
	 */
	protected Composite tabFolderPage;
	protected ScrolledComposite tabFolderPageScrolled;
	
	//protected CCAComponentInstance implementation;
	
	/**
	 * the actual tab item
	 */
	private TabItem tabItem;
	private String _parentContainerID;
	protected String label;
	private boolean _contentCreated;
	private boolean _isDisposed;
	
	/**
	 * Constructor
	 * @param implementation
	 * @param parent
	 */
	protected ContainerTab(String label, String containerId) {
	
		this.label=label;
		_contentCreated=false;
		_parentContainerID=containerId;
		this.setVisible(false);
		_isDisposed=false;
	}
  
	void createContent(){
		
		tabFolderPageScrolled = new ScrolledComposite (ASCoDTKernel.getDefault().getUIservice().getContainer(_parentContainerID),SWT.V_SCROLL|SWT.H_SCROLL);
		tabFolderPage=new  Composite(tabFolderPageScrolled,SWT.NONE);
		
		// which goes as content to the scrolled composite
		createControlGroup();
		
		tabFolderPageScrolled.setContent(tabFolderPage);
		tabFolderPageScrolled.setExpandVertical(true);
		tabFolderPageScrolled.setExpandHorizontal(true);
		tabFolderPageScrolled.setMinHeight(tabFolderPage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		tabFolderPageScrolled.addControlListener(new ControlAdapter(){
			public void controlResized( ControlEvent e ) {
			// recalculate height in case the resize makes texts
//			// wrap or things happen that require it
				tabFolderPageScrolled.setMinHeight(tabFolderPage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			}
		});
		// create user content
	
		ASCoDTKernel.getDefault().getUIservice().getContainer(_parentContainerID).setLayout(new FillLayout());
		_contentCreated=true;
		redraw();
	}
	/**
	 * should be overwritten by the user
	 */
	abstract protected void createControlGroup();
	
	/**
	 * redraw the user interface
	 */
	public void redraw(){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if(_contentCreated){
					tabFolderPage.redraw();	
				}
			}
		});
	}
	/**
	 * enables and disables the tab
	 * @param visibility
	 */
	public void setVisible(final boolean visibility){
		class CustomThread extends Thread{
			public void run() {
				if(tabItem!=null && !visibility){
					tabItem.dispose();
					tabItem=null;
				}
				if(tabItem==null && visibility){
					if(!_contentCreated)
						createContent();
					tabItem=new TabItem(ASCoDTKernel.getDefault().getUIservice().getContainer(_parentContainerID),SWT.FILL);
					tabItem.setText(label);
					tabItem.setControl(tabFolderPageScrolled);
				}
			}
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new CustomThread());

	}
	public synchronized boolean isDisposed(){
		return _isDisposed;
	}
//	protected void finalize () {
//		dispose();
//	}

	public void dispose() {
		class CustomThread extends Thread{
			public void run() {
				if(tabItem!=null){
					tabItem.dispose();
					tabItem=null;
				}
				if(tabFolderPage!=null){
					tabFolderPage.dispose();
					tabFolderPage=null;
				}
				if(tabFolderPageScrolled!=null){
					tabFolderPageScrolled.dispose();
					tabFolderPageScrolled=null;
				}
			}
		}
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new CustomThread());
		synchronized(this){
			_isDisposed=true;
		}
	}


}
