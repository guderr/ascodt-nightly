package de.tum.ascodt.sidlcompiler.backend;

import java.io.File;
import java.net.URL;

import de.tum.ascodt.plugin.services.SocketService;
import de.tum.ascodt.repository.entities.SocketComponent;
import de.tum.ascodt.utils.TemplateFile;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

public class CreateSocketComponent {


	TemplateFile 														  _templateFileForSocketComponent;
	public CreateSocketComponent(URL generatedFilesDirectory, String[] namespaces) throws ASCoDTException{
		String namespacesPath="";
		for(String namespace:namespaces){
			namespacesPath+= File.separatorChar+namespace;
		}
		String  destinationFileForJavaSocketComponent 						 = generatedFilesDirectory.toString()+namespacesPath+ File.separatorChar + "SocketComponent.java";
		String  templateFileForJavaSocketComponent = "java-abstract-socket-component.template";
		_templateFileForSocketComponent = new TemplateFile(
				templateFileForJavaSocketComponent,
				destinationFileForJavaSocketComponent,
				namespaces,
				TemplateFile.getLanguageConfigurationForJava() ,
				true
	  );
		

	}
	public void apply() throws ASCoDTException{
		_templateFileForSocketComponent.addMapping( "__SOCKET_COMPONENT__",SocketComponent.class.getCanonicalName());
				
		_templateFileForSocketComponent.addMapping( "__SOCKET_SERVICE__", SocketService.class.getCanonicalName() );
		_templateFileForSocketComponent.open();
		_templateFileForSocketComponent.close();
	}
}
