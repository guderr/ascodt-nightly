package de.tum.ascodt.plugin.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.tum.ascodt.plugin.ui.wizards.messages"; //$NON-NLS-1$
	public static String NewWorkbenchWizard_1;
	public static String NewWorkbenchWizard_2;
	public static String NewWorkbenchWizard;
	public static String CustomComponentNewWizard_2;
	public static String CustomProjectNewWizard_0;
	public static String CustomProjectNewWizard_1;
	public static String CustomProjectNewWizard_2;
	public static String CustomProjectNewWizard_3;
	public static String NewComponentWizard;
	public static String ComponentFromScratch;
	public static String NewComponentUIWizard;
	public static String ComponentUIFromScratch;
	public static String CustomComponentUINewWizard_2;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
