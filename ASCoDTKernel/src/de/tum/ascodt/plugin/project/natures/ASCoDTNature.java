package de.tum.ascodt.plugin.project.natures;


import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

//TODO import de.tum.ascodt.plugin.project.builders.ASCoDTProjectBuilder;


/**
 * This class implements the ASCoDT Project nature. In eclipse one project can different natures. A nature is a function,
 * which maps a project to compiler(builder). In the case of ASCoDT the ASCoDT nature tells eclipse to use the internal 
 * ASCoDT builder to be able to compile the project. The life-cycle of the ASCoDT nature is the following:
 * 1. After load:
 * 		-Configure the nature: reads the project description file(.project), which holds the state of the project and loads the 
 * serialized parameters for the compiler(builder) and installs the compiler for this project
 * 2. Destroy:
 * 		-remove all parameters from the project description and removes the installed compilers
 * Important functionality:
 * 	- the nature acts as an access point to the compiler arguments. We can access them through getArguments() and setArguments
 * @author atanasoa
 *
 */
public class ASCoDTNature  implements IProjectNature{
	
	
	public static final String ID = ASCoDTNature.class.getCanonicalName();
	/**
	 * reference to the project
	 */
	private IProject project;
	
	
	
	/**
	 * 
	 * @return the compiler arguments for the current ASCoDT project
	 * @throws CoreException
	 */
	public Map<String,String> getArguments() throws CoreException{
		//IProjectDescription desc = project.getDescription();
		//ICommand[] commands = desc.getBuildSpec();
		
		
//		for (int i = 0; i < commands.length; ++i) {
//			if (commands[i].getBuilderName().equals(ASCoDTProjectBuilder.ID)) {
//				return commands[i].getArguments();
//			}
//		}
		return null;
	}
	
	/**
	 * sets the compiler arguments for the current project
	 * @param args compiler arguments for the ASCoDT project
	 * @throws CoreException
	 */
	public void setArguments(Map<String,String> args) throws CoreException{
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		
//		for (int i = 0; i < commands.length; ++i) {
//			if (commands[i].getBuilderName().equals(ASCoDTProjectBuilder.ID)) {
//				commands[i].setArguments(args);
//			}
//		}
		desc.setBuildSpec(commands);
		project.setDescription(desc, IResource.FORCE, null);
		
	}
	
	
	/**
	 * install the ASCoDT builder 
	 */
	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(de.tum.ascodt.plugin.project.builders.ProjectBuilder.ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[1];
		//System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(de.tum.ascodt.plugin.project.builders.ProjectBuilder.ID);
		//TODO Map<String,String> args = new HashMap<String,String>();
		//		args.put("output", "components");
		//		if(args!=null)
		//			command.setArguments(args);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
		
		
	}

	/**
	 * uninstall the ASCoDT project builder
	 */
	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(de.tum.ascodt.plugin.project.builders.ProjectBuilder.ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);			
				return;
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override
	public IProject getProject() {
		return project;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
