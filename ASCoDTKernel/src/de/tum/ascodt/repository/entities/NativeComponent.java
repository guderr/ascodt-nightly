/**
 * 
 */
package de.tum.ascodt.repository.entities;

/**
 * @author Atanas Atanasov
 *
 */
public interface NativeComponent extends Component {
	/**
	 * 
	 * @return the pointer of the native component
	 */
	public long getNativeReference();
}
