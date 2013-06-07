package de.tum.ascodt.plugin.ui.gef.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.IPropertySource;

import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.repository.Target;


/**
 * Gef model representing the component. The model is bridged with cca instances.
 * @author atanasoa
 *
 */
public class Component extends Geometry implements IPropertySource{
	private Trace _trace = new Trace(Component.class.getCanonicalName());

	public static final String REFERENCE_PROP = "Component.Reference";
	public static final String GUI_PROP = "Component.GUI";
	public static final String LINKS_PROP = "Component.Link";
	public static final String INPUT_CONNECTIONS = "Component.Input";
	public static final String OUTPUT_CONNECTIONS = "Component.Output";
	public static final String WRITE_TO_FILE="Component.WriteToFile";
	public static final String LOAD_FROM_FILE="Component.LoadFromFile";
	//public static final Object CHANGE_COMPONENT_REFERENCE = "Component.ChangeComponentReference";

	/**
	 * holds the reference id of the component
	 */
	private String _reference;

	/**
	 * holds the class name of the component
	 */
	private String className;




	/**
	 * holds the remote component flag
	 */
	private boolean isRemote;

	/**
	 * enable the ui for this componet
	 */
	private transient boolean uiEnabled;

	/**
	 * use ports
	 */
	private Vector<Port> usePorts;

	/**
	 * provide ports
	 */
	private Vector<Port> providePorts;

	private Vector<Connection> outputConnections;
	private Vector<Connection> inputConnections;

	private Vector<Link> sourceLinks;

	/**
	 * holds the file name where the state of the component should be stored
	 */
	private String fileName;

	private String _target;
	/**
	 * validity flag
	 */
	private boolean valid;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the cca instance
	 */
	private transient de.tum.ascodt.repository.entities.Component _component;

	private transient Map<Connection, Object> _connections;

	public Component(){
		size=new Dimension(256,128);
		this._reference="";
		this.className="";
		this.isRemote=false;
		uiEnabled=false;
		setUsePorts(new Vector<Port>());
		setProvidePorts(new Vector<Port>());
		setConnections(new Vector<Connection>());
		inputConnections=new Vector<Connection>();
		outputConnections=new Vector<Connection>();
		sourceLinks=new Vector<Link>(); 
		fileName=System.getProperty("java.io.tmpdir")+"/"+this.hashCode()+".asc_state";
		_target="";
		valid=true;

	}

	/**
	 * reconnects all outgoing connections. Needed by serialization
	 */
	public void reconnect(){
		for(Connection con:inputConnections)
			handleConnect(con);
	}

	/**
	 * Setter for the bridged cca component instance
	 * @param component
	 */
	public void setCCAComponent(de.tum.ascodt.repository.entities.Component component){
		_component=component;
		_connections=new HashMap<Connection,Object>();
	} 

	public de.tum.ascodt.repository.entities.Component getCCAComponent(){
		return _component;
	}

	public String getReference() {
		return _reference;
	}

	public void setReference(String reference) {
		String oldValue=this._reference;
		this._reference=reference;
		firePropertyChange(REFERENCE_PROP, oldValue, reference);
	}

	public String getClassName(){
		return className;
	}

	public String getComponentName() {
		return className.substring(className.lastIndexOf(".")+1);
	}

	public void setClassName(String className){
		this.className=className;
	}
	
	public void setTarget(String target) {
		_target=target;
	}
	
	public String getTarget(){
		return _target;
	}

	/**
	 * Checks if the component has gui
	 * @return
	 */
	public boolean hasGUI() {
		Assert.isNotNull(_component);
		return _component.hasGUI();
	}

	public void setUIEnable(boolean value){
		Assert.isNotNull(_component);
		boolean oldValue=uiEnabled;
		uiEnabled=value;
		if(uiEnabled)
			_component.openGUI();
		else
			_component.closeGUI();
		firePropertyChange(GUI_PROP, oldValue, uiEnabled);
	}

	/**
	 * this method is needed to free memory which can't be freed from garbage collector
	 */
	public void destroy(){
		Assert.isNotNull(_component);
		_component.destroy();
	}




	public boolean getUIEnabled() {
		return uiEnabled;
	}

	public Vector<Port> getUsePorts() {
		return usePorts;
	}

	public Vector<Port> getProvidePorts() {
		return providePorts;
	}

	public void setUsePorts(Vector<Port> usePorts) {
		this.usePorts = usePorts;
	}

	public void setProvidePorts(Vector<Port> providePorts) {
		this.providePorts = providePorts;
	}

	public void setConnections(Vector<Connection> connections) {
		this.outputConnections = connections;
	}

	public Vector<Connection> getConnections() {
		return outputConnections;
	}

	@SuppressWarnings("unchecked")
	public Vector<Connection> getSourceConnections() {
		return (Vector<Connection>)outputConnections.clone();
	}

	@SuppressWarnings("unchecked")
	public Vector<Connection> getTargetConnections() {
		return (Vector<Connection>)inputConnections.clone();

	}

	/**
	 * a callback for the connect method
	 * @param connection
	 */
	public void connectInput(Connection connection) {
		if(handleConnect(connection))
			inputConnections.add(connection);
		firePropertyChange(INPUT_CONNECTIONS,true,connection);
	}


	/**
	 * Connect a use port to given provide port
	 * 1. create the port needed based on the destination target
	 * 2. connect the destination application to the created port
	 * 3. connect the created port to the source application
	 * @param connection
	 * @return 
	 */
	public boolean handleConnect(Connection connection) {
		_trace.in("handleConnect");
		Component source=connection.getSource();
		Component target=connection.getTarget();
		UsePort sourcePort = (UsePort) connection.getSourcePort();
		ProvidePort targetPort = (ProvidePort) connection.getTargetPort();
		
		Assert.isNotNull(source);

		Assert.isNotNull(target);

		Assert.isNotNull(sourcePort);

		Assert.isNotNull(targetPort);
		Method createPort;
		try {
			createPort = source.getCCAComponent().getClass().getMethod("createPortFor"+sourcePort.getReference(),Target.class);
			if(createPort!=null&&target.getCCAComponent().getTarget()!=null){
				Object usePort=createPort.invoke(source.getCCAComponent(),target.getCCAComponent().getTarget());
				for(Method m:usePort.getClass().getMethods())
					if(m.getName().equals("connect")&&((m.getModifiers()&Modifier.NATIVE)==0))
						m.invoke(usePort, target.getCCAComponent());
				for(Method m:source.getCCAComponent().getClass().getMethods())
					if(m.getName().equals("connect"+sourcePort.getReference()))
						m.invoke(source.getCCAComponent(), usePort);
				_connections.put(connection,usePort);
			}else{
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "handleConnect()",  "Cannot create connection between "+
						source.getReference()+":"+source.getComponentName()+" and "+target.getReference()+":"+target.getComponentName()+"!\nTarget not specified in sidl", null );
				return false;
			}
		} catch (Exception e){
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "handleConnect()",  "Cannot create connection between "+
					source.getReference()+":"+source.getComponentName()+" and "+target.getReference()+":"+target.getComponentName(), e );
			return false;

		}
		_trace.out("handleConnect");
		return true;
	}

	/**
	 * disconnect a plain port from the component
	 * 1. find the plain port to be disconnected
	 * 2. disconnect the destination application from the plain port
	 * 3. disconnect the plain port from the source
	 * @param connection
	 */
	public boolean handleDisconnect(Connection connection) {
		_trace.in("handleDisconnect");
		Component source=connection.getSource();
		Component target=connection.getTarget();
		UsePort sourcePort = (UsePort) connection.getSourcePort();
		Assert.isNotNull(_connections);
		Assert.isNotNull(_connections.get(connection));
		Object usePort=_connections.get(connection);
		try {
			for(Method m:usePort.getClass().getMethods())
				if(m.getName().equals("disconnect"))
					m.invoke(usePort,target.getCCAComponent());

			for(Method m:source.getCCAComponent().getClass().getMethods())
				if(m.getName().equals("disconnect"+sourcePort.getReference()))
					m.invoke(source.getCCAComponent(), usePort);
			_connections.remove(connection);
		}catch(Exception e){
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "handleDisconnect()",  "Cannot delete connection between "+
					source.getReference()+":"+source.getComponentName()+" and "+target.getReference()+":"+target.getComponentName(), e );
			return false;
		}
		
		_trace.out("handleDisconnect");
		return true;

	}



	public void connectOutput(Connection connection) {
		outputConnections.addElement(connection);
		firePropertyChange(OUTPUT_CONNECTIONS,true, connection);
	}

	public void disconnectInput(Connection connection) {
		if(handleDisconnect(connection))
			inputConnections.remove(connection);
		firePropertyChange(INPUT_CONNECTIONS,false,connection);
	}

	public void disconnectOutput(Connection connection) {
		outputConnections.removeElement(connection);
		firePropertyChange(OUTPUT_CONNECTIONS,false,connection);
	}

	public void writeToFile() {
		firePropertyChange(WRITE_TO_FILE,null,fileName);
	}

	public void loadFromFile() {
		firePropertyChange(LOAD_FROM_FILE,null,fileName);
	}

	public void removeLink(Link link) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		if ((link.getSource() == this) && (sourceLinks.contains(link))) {
			sourceLinks.remove(link);
			firePropertyChange(LINKS_PROP, link, null);	
		} 
	}

	public void addLink(Link link) {
		if (link == null ) {
			throw new IllegalArgumentException();
		}
		if (link.getSource() == this) {
			sourceLinks.add(link);
			firePropertyChange(LINKS_PROP, null, link);
		} 
	}

	@SuppressWarnings("unchecked")
	public Vector<Link> getSourceLinks() {
		return (Vector<Link>)sourceLinks.clone();
	}

	/**
	 * @param isRemote the isRemote to set
	 */
	public void setRemote(boolean isRemote) {
		this.isRemote = isRemote;
	}

	public boolean isRemote() {
		return isRemote;
	}

	/**
	 * Setter for validity
	 * @param valid validity flag for the component
	 */
	public void setIsValid(boolean valid) {
		this.valid=valid;
	}

	/**
	 * Getter for validity
	 * @return
	 */
	public boolean isValid(){
		return valid;
	}
	
	

	

}
