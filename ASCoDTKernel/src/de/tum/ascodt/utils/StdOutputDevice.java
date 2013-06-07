/**
 * 
 */
package de.tum.ascodt.utils;

/**
 * @author Atanas Atanasov
 * A standard output device which forwards println calls to the sysmtem.out 
 *
 */
public class StdOutputDevice implements OutputDevice {

	/* (non-Javadoc)
	 * @see de.tum.ascodt.utils.OutputDevice#println(java.lang.String)
	 */
	@Override
	public void println(String line) {
		System.out.println(line);
	}

}
