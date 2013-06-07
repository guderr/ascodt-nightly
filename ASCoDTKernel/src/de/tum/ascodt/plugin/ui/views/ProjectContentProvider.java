package de.tum.ascodt.plugin.ui.views;

import java.util.Collection;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class ProjectContentProvider implements IStructuredContentProvider {
	ComboViewer _viewer;
	@Override
	public void dispose() {
		_viewer=null;
	}

	@Override
	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				if(viewer!=null&&!Display.getDefault().isDisposed())
					viewer.refresh();
			}
		});
		
	}

	/**
   * Returns the elements in the input, which must be either an array or a
   * <code>Collection</code>. 
   */
  @SuppressWarnings("rawtypes")
	public Object[] getElements(Object inputElement) {
      if (inputElement instanceof  Object[])
          return (Object[]) inputElement;
      if (inputElement instanceof  Collection)
          return ((Collection) inputElement).toArray();
      return new Object[0];
  }

	public void setViewer(ComboViewer combo) {
		_viewer=combo;
	}
	
	public ComboViewer getViewer(){
		return _viewer;
	}
	

}
