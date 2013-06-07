package de.tum.ascodt.plugin.repository;


import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.gef.requests.CreationFactory;

import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.ui.gef.model.Component;
import de.tum.ascodt.plugin.ui.gef.model.Port;
import de.tum.ascodt.plugin.ui.gef.model.ProvidePort;
import de.tum.ascodt.plugin.ui.gef.model.UsePort;
import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.repository.Target;
import de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent;
import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * The instance factory is invoked by gef after dropping a component from the palette
 * to the workbench. It should create instances of the java clases needed to instantiate
 * the component and instances of the GEF model classes.
 * @author Atanas Atanasov
 *
 */
public class InstanceFactory implements CreationFactory{

	private Trace _trace= new Trace(InstanceFactory.class.getCanonicalName());
	
	/**
	 * reference to the current project
	 */
	private Project _project;
	
	/**
	 * the component to be created
	 */
	private String _componentInterface;
	
	private String _target;
	
	/**
	 * Constructor
	 * @param project the project related to this factory
	 * @param componentInterface name of the component
	 * @param target 
	 * @throws ASCoDTException
	 */
	public InstanceFactory(Project project,String componentInterface, String target) throws ASCoDTException{
		_project=project;
		_componentInterface=componentInterface;
		_target=target;
	}
	
	/**
	 * loads additional variables of the serialized component. This includes:
	 * 1. reinstantiate the current application.
	 * 2. call the loadFromFile method of the application.
	 */
	public void loadObject(Component gefComponent){
		_trace.in("loadObject");
		String classToLoad = getTargetMainClass();	
		Class<?> componentClass;
		try {
			componentClass =_project.getClasspathRepository().loadClass(classToLoad);
			de.tum.ascodt.repository.entities.Component application = (de.tum.ascodt.repository.entities.Component)
			componentClass.getConstructor(String.class).newInstance(gefComponent.getReference());
			gefComponent.setCCAComponent(application);
		} catch (ClassNotFoundException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot load class "+classToLoad, e );
		} catch (InstantiationException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot instantiate class "+classToLoad, e );
		} catch (IllegalAccessException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot access class instance "+classToLoad, e );
		} catch (IllegalArgumentException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot load class "+classToLoad, e );
		} catch (SecurityException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot load class "+classToLoad, e );
		} catch (InvocationTargetException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot load class "+classToLoad, e );
		} catch (NoSuchMethodException e) {
			ErrorWriterDevice.getInstance().showError( getClass().getName(), "loadObject()",  "Cannot load class "+classToLoad, e );
		} 
		_trace.out("loadObject");
	}

	/**
	 * @return
	 */
	public String getTargetMainClass() {
		String classToLoad=_componentInterface+"JavaImplementation";
		/*if(Target.isJavaLocal(_target))
			classToLoad=_componentInterface+"JavaImplementation";
		else if(Target.isCxxNative(_target)||Target.isFortranNative(_target))
			classToLoad=_componentInterface+"JavaNativeImplementation";
		else if(Target.isReverseFortranRemoteSocket(_target)||Target.isReverseCxxRemoteSocket(_target))
			classToLoad=_componentInterface+"JavaRemoteClientImplementation";
		else if(Target.isFortranRemoteSocket(_target)||Target.isCxxRemoteSocket(_target))
			classToLoad=_componentInterface+"JavaRemoteServerImplementation";*/
		return classToLoad;
	}
	/**
	 * the main factory method
	 * 1. get a new component identifier from the dynamic repository
	 * 2. create the gef model of the component
	 * 3. create the cca model of the component
	 * 
	 */
	@Override
	public Object getNewObject() {
		
		if(_project.getClasspathRepository()!=null){
			_trace.in("getNewObject");
			String classToLoad = getTargetMainClass();	
			try {
				String reference="oid"+_project.getClasspathRepository().getNewId();
				Class<?> componentClass=_project.getClasspathRepository().loadClass(classToLoad);
				de.tum.ascodt.repository.entities.Component application=(de.tum.ascodt.repository.entities.Component)componentClass.getConstructor(String.class).newInstance(reference);
				de.tum.ascodt.plugin.ui.gef.model.Component component = new de.tum.ascodt.plugin.ui.gef.model.Component();
				component.setReference(reference);
				component.setClassName(_componentInterface);
				component.setTarget(_target);
				SymbolTable symbolTable=_project.getSymbolTable();
				
				AClassPackageElement node=symbolTable.getGlobalScope().getClassDefinition(_componentInterface);
				assert(node!=null);
				GetProvidesAndUsesPortsOfComponent getPorts = new GetProvidesAndUsesPortsOfComponent();
				node.apply( getPorts );
				component.setProvidePorts(createProvidePorts(getPorts));
				component.setUsePorts(createUsePorts(getPorts));
				
				component.setCCAComponent(application);
				_trace.out("getNewObject");
				return component;
			} catch (ClassNotFoundException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
			} catch (InstantiationException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot instantiate class "+classToLoad, e );
			} catch (IllegalAccessException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot access class instance "+classToLoad, e );
			} catch (IllegalArgumentException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
			} catch (SecurityException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
			} catch (InvocationTargetException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
			} catch (NoSuchMethodException e) {
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
			} catch (Error e){
				ErrorWriterDevice.getInstance().showError( getClass().getName(), "getNewObject()",  "Cannot load class "+classToLoad, e );
				e.printStackTrace();
				
			}
		}
	
		return null;
	}

	/**
	 * Creates all gef models of the use ports 
	 * @param getPorts
	 * @return
	 */
	private Vector<Port> createUsePorts(GetProvidesAndUsesPortsOfComponent getPorts) {
		Vector<Port> usePorts=new Vector<Port>();
		String ports_as_string = getPorts.getUsesPortsAndAsIdentifiers(",", ".");
		if(!ports_as_string.equals(""))
			if(ports_as_string.contains(",")){
				String[]usePortsArray=ports_as_string.split(",");
				for(int i=0;i<usePortsArray.length;i+=2)
					usePorts.add(new UsePort(usePortsArray[i],usePortsArray[i+1],i/2));
			}
				
		return usePorts;
	}
	
	/**
	 * Creates all gef models of the provide ports 
	 * @param getPorts
	 * @return
	 */
	private Vector<Port> createProvidePorts(GetProvidesAndUsesPortsOfComponent ports) {
		Vector<Port> providePorts=new Vector<Port>();
		String ports_as_string=ports.getProvidesPorts(",",".");
		int index=0;
		if(!ports_as_string.equals(""))
			if(ports_as_string.contains(","))
				for(String port:ports_as_string.split(","))
					providePorts.add(new ProvidePort(port,index++));
			else
				providePorts.add(new ProvidePort(ports_as_string,index++));
		return providePorts;
	}
	/**
	 * @return the class of the created objects
	 */
	@Override
	public Object getObjectType() {
		return Component.class;
	}

}
