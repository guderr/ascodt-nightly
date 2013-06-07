package de.tum.ascodt.plugin.ui.editors.sidl;



import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * This class implements an editor for the sidl files .
 * @author Atanas Atanasov
 *
 */

public class SIDLEditor extends TextEditor {
    public final static String SIDL_PARTITIONING= "__sidl_partitioning";
	private static SIDLEditor editor;
	private SIDLPartitionScanner fPartitionScanner;
	private SIDLCodeScanner fCodeScanner;
	private SIDLColorProvider fColorProvider;
	private SIDLDocumentationScanner fDocScanner;
	public SIDLEditor() {
		super();
		editor=this;
		this.setDocumentProvider(new SIDLDocumentProvider());
		this.setSourceViewerConfiguration(new SIDLSourceViewerConfiguration());
		
		
	}
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		

	}
	

	public void dispose() {
		super.dispose();
	}

	public static SIDLEditor getDefault() {
		if(editor==null)
			editor= new SIDLEditor();
		return editor;
	}
	
	
	
	/**
	 * Return a scanner for creating SIDL partitions.
	 *
	 * @return a scanner for creating SIDL partitions
	 */
	 public SIDLPartitionScanner getSIDLPartitionScanner() {
		if (fPartitionScanner == null)
			fPartitionScanner= new SIDLPartitionScanner();
		return fPartitionScanner;
	}

	/**
	 * Returns the singleton SIDL code scanner.
	 *
	 * @return the singleton SIDL code scanner
	 */
	 public RuleBasedScanner getSIDLCodeScanner() {
	 	if (fCodeScanner == null)
			fCodeScanner= new SIDLCodeScanner(getSIDLColorProvider());
		return fCodeScanner;
	}

	/**
	 * Returns the singleton Java color provider.
	 *
	 * @return the singleton Java color provider
	 */
	 public SIDLColorProvider getSIDLColorProvider() {
	 	if (fColorProvider == null)
			fColorProvider= new SIDLColorProvider();
		return fColorProvider;
	}

	/**
	 * Returns the singleton SIDL-doc scanner.
	 *
	 * @return the singleton SIDL-doc scanner
	 */
	 public RuleBasedScanner getSIDLDocScanner() {
	 	if (fDocScanner == null)
			fDocScanner= new SIDLDocumentationScanner(fColorProvider);
		return fDocScanner;
	}
	 
	public void setStatus(String message) {
		super.setStatusLineMessage(message);
		
	}

}
