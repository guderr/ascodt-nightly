package de.tum.ascodt.plugin.project.builders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;



import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import de.tum.ascodt.plugin.project.Project;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.backend.CreateComponentsAndInterfaces;
import de.tum.ascodt.sidlcompiler.frontend.lexer.LexerException;
import de.tum.ascodt.sidlcompiler.frontend.node.Start;
import de.tum.ascodt.sidlcompiler.frontend.parser.ParserException;
import de.tum.ascodt.sidlcompiler.symboltable.ASTValidator;
import de.tum.ascodt.sidlcompiler.symboltable.BuildSymbolTable;
import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * The project builder has the task to invoke the sidl compiler when the sidl 
 * resources are changed
 * @author atanasoa
 *
 */
public class ProjectBuilder extends IncrementalProjectBuilder {

	public static String ID=ProjectBuilder.class.getCanonicalName();
	private Trace _trace = new Trace(ProjectBuilder.class.getCanonicalName());

	public ProjectBuilder(){
		super();
		_trace.in("Constructor");

		_trace.out("Constructor");
	}
	/**
	 * 
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor)
	throws CoreException {
		_trace.in("build(..)","kind:"+kind);
		if (kind==IncrementalProjectBuilder.AUTO_BUILD){
			IResourceDelta delta = getDelta(getProject());
			_trace.debug("build()", "starting incremental build");
			ProjectResourceDeltaListener deltaListener=new ProjectResourceDeltaListener();
			if(delta!=null)
				delta.accept(deltaListener);

		}
		_trace.out("build(..)");
		return null;

	}

	/**
	 * A routine to validate the global symbol table against specific sidl file
	 * @param startNode the starting node of the sidl resource
	 * @param symbolTable to validate with
	 * @return a error string or "" if valid
	 */
	public static String  validateSymbolTableForSIDLResource(Start startNode,String resourceLocation,SymbolTable symbolTable){
		ASTValidator validator = new ASTValidator(symbolTable,resourceLocation);
		startNode.apply(validator);
		return validator.getErrorMessages();

	}

	/**
	 * Compile an added or modified resource file. We start by parsing the corresponding sidl file and filling
	 * up the parsed entries in the project symbol table. After this step we make sure that there are no conflicts
	 * in the table through the ast validator. Then we can start with the generation of the java/c++ source files
	 * using CreateComponentsAndInterfaces class. The newly generated source files are added unter <project>/<components>/<target-language>
	 * folder
	 * @param resourceLocation the resource file to be compiled
	 * @param eclipseProject handle of the eclipse project
	 * @throws ASCoDTException
	 */
	public static de.tum.ascodt.sidlcompiler.frontend.node.Start buildStartSymbolsForSIDLResource(String resourceLocation) throws ASCoDTException{
	  try {
			return buildStartSymbolsForSIDLResource(new java.io.FileInputStream(resourceLocation));
		} catch (FileNotFoundException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "buildComponentsAndInterfaces()",e.getMessage(), e);
		} 
	}
	
	/**
	 * Compile an added or modified resource file. We start by parsing the corresponding sidl file and filling
	 * up the parsed entries in the project symbol table. After this step we make sure that there are no conflicts
	 * in the table through the ast validator. Then we can start with the generation of the java/c++ source files
	 * using CreateComponentsAndInterfaces class. The newly generated source files are added unter <project>/<components>/<target-language>
	 * folder
	 * @param resourceLocation the resource file to be compiled
	 * @param eclipseProject handle of the eclipse project
	 * @throws ASCoDTException
	 */
	public static de.tum.ascodt.sidlcompiler.frontend.node.Start buildStartSymbolsForSIDLResource(java.io.InputStream stream) throws ASCoDTException{
		de.tum.ascodt.sidlcompiler.frontend.node.Start result=null;
		try {
			java.io.InputStreamReader fileReader=new java.io.InputStreamReader( stream);
			de.tum.ascodt.sidlcompiler.frontend.parser.Parser parser = new de.tum.ascodt.sidlcompiler.frontend.parser.Parser(
					new de.tum.ascodt.sidlcompiler.frontend.lexer.Lexer(
							new java.io.PushbackReader(
									fileReader
							)
					)
			);
			result = parser.parse();
			
		} catch (ParserException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "buildComponentsAndInterfaces()",e.getMessage(), e);
		} catch (LexerException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "buildComponentsAndInterfaces()",e.getMessage(), e);
		} catch (IOException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "buildComponentsAndInterfaces()",e.getMessage(), e);
		}
		return result;
	}
	
	public static void extendSymbolTable(Start startNode,SymbolTable symbolTable, String resourceLocation){
		
		BuildSymbolTable symbolTableBuilderForResource=new BuildSymbolTable(symbolTable,resourceLocation);
		startNode.apply(symbolTableBuilderForResource);

	}

	/**
	 * Executes the ascodt compiler to generate the blueprints for given symbol table
	 * @param eclipseProject the current eclipse project
	 * @throws ASCoDTException
	 */
	public static void generateBlueprints(IProject eclipseProject) throws ASCoDTException {
		Project project = de.tum.ascodt.plugin.project.ProjectBuilder.getInstance().getProject(eclipseProject);
		assert(project.getSymbolTable()!=null);
		
		CreateComponentsAndInterfaces interfaces= new CreateComponentsAndInterfaces(project.getSymbolTable());
		try {
			interfaces.create(

					new File(eclipseProject.getLocation().toPortableString()+project.getJavaProxiesFolder()).toURI().toURL(),
					new File(eclipseProject.getLocation().toPortableString()+project.getJavaSourcesFolder()).toURI().toURL(),
					new File(eclipseProject.getLocation().toPortableString()+project.getNativeFolder()).toURI().toURL()

			);


			eclipseProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (MalformedURLException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "generateBlueprints()","wrong blueprint url:"+e.getLocalizedMessage(), e);
		} catch (CoreException e) {
			throw new ASCoDTException(ProjectBuilder.class.getName(), "generateBlueprints()","eclipse core exception:"+e.getLocalizedMessage(), e);
		}
	}


}
