/**
 * 
 */
package de.tum.ascodt.sidlcompiler;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;
import de.tum.ascodt.plugin.ASCoDTKernel;
import de.tum.ascodt.resources.ResourceManager;
import de.tum.ascodt.sidlcompiler.backend.CreateComponentsAndInterfaces;
import de.tum.ascodt.sidlcompiler.frontend.node.Start;
import de.tum.ascodt.sidlcompiler.symboltable.ASTValidator;
import de.tum.ascodt.utils.exceptions.ASCoDTException;

/**
 * @author Atanas Atanasov
 * A class used for the stand alone execution of the sidl compiler
 */
public class Compiler {

	/**
	 * Full command line arguments.
	 */
	private static final String CommandLineParameterInclude              = "include";
	private static final String CommandLineParameterOutputDirectoryForUser      = "output-user";

	private static final String CommandLineParameterOutputDirectoryForStubs      = "output-stubs";
	private static final String CommandLineParameterVerbose              = "verbose";
	private static final String CommandLineParameterAuthors              = "authors";


	/**
	 * The short version of command line arguments.
	 */
	private static final char   CommandLineParameterVerboseShort         = 'v';
	private static final char   CommandLineParameterOutputDirectoryUserShort = 'o';
	private static final char   CommandLineParameterOutputDirectoryStubsShort = 's';
	private static final char   CommandLineParameterIncludeShort         = 'i';
	private static final char   CommandLineParameterAuthorsShort         = 'a';


	/**
	 * Write the program header. This is done at the very beginning of the main
	 * operation.
	 */
	static void header() {
		System.out.println( "A Simplified SIDL Compiler" );
		System.out.println( "(C) Tobias Weinzierl" );
		System.out.println( "(C) Atanas Atanasov" );
	}

	/**
	 * prints the actual usage of the compiler
	 */
	static void usage() {
		System.out.println( "Usage: java de.tum.in.ascodt.sidl.Compiler [options] inputfile" );
		System.out.println( "Options: " );

		System.out.println( 
				"-"  + CommandLineParameterIncludeShort + " <file> | " +
				"--" + CommandLineParameterInclude      + " <file> "
		);
		System.out.println( "    Read types from a SIDL file, but don't generate code" );
		System.out.println( "    for them. Multiple include options are allowed." );

		System.out.println( 
				"-"  + CommandLineParameterOutputDirectoryUserShort + " <dir>  | " +
				"--" + CommandLineParameterOutputDirectoryForUser      + " <dir>" 
		);
		System.out.println( 
				"-"  + CommandLineParameterOutputDirectoryStubsShort + " <dir>  | " +
				"--" + CommandLineParameterOutputDirectoryForStubs      + " <dir>" 
		);
		System.out.println( "    Set output directory ('.' default)." );

		System.out.println( 
				"-"  + CommandLineParameterAuthorsShort + "        | " +
				"--" + CommandLineParameterAuthors 
		);
		System.out.println( "    Set authors of component. Multiple authors allowed. " );

		System.out.println( 
				"-"  + CommandLineParameterVerboseShort + "        | " +
				"--" + CommandLineParameterVerbose 
		);
		System.out.println( "    Verbose output." );

	}

	/**
	 * a verbose flag
	 */
	private boolean _verbose;

	/**
	 * Output directory, i.e. where all user implementation files are written to.
	 */
	private String  _outputDirectoryForUserImplementation;

	/**
	 * Output directory, i.e. where all user implementation files are written to.
	 */
	private String  _outputDirectoryForStubs;

	/**
	 * Name of the SIDL file which is to be read and translated.
	 */
	private String  _sourceSIDLFile;

	//TODO private java.util.Vector<String>  _authors;
	private java.util.Vector<String>         _includedSIDLFiles;

	/**
	 * Symbol table of the compile run.
	 */
	private de.tum.ascodt.sidlcompiler.symboltable.SymbolTable  _symbolTable;

	private boolean _isValid;

	
	Compiler(){
		_symbolTable=new de.tum.ascodt.sidlcompiler.symboltable.SymbolTable();
	}
	/**
	 * Parse the command line arguments, and perform some validation afterwards.
	 * 
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void parseCommandLineArguments(String[] args) throws ASCoDTException{
		jargs.gnu.CmdLineParser commandLineParser = new jargs.gnu.CmdLineParser();
		try {
			
			jargs.gnu.CmdLineParser.Option commandLineOptionInclude        = commandLineParser.addStringOption ( CommandLineParameterIncludeShort,         CommandLineParameterInclude );
			jargs.gnu.CmdLineParser.Option commandLineOptionOutputUserDir      = commandLineParser.addStringOption ( CommandLineParameterOutputDirectoryUserShort, CommandLineParameterOutputDirectoryForUser );
			jargs.gnu.CmdLineParser.Option commandLineOptionOutputStubsDir      = commandLineParser.addStringOption ( CommandLineParameterOutputDirectoryStubsShort, CommandLineParameterOutputDirectoryForStubs );

			jargs.gnu.CmdLineParser.Option commandLineOptionVerbose        = commandLineParser.addBooleanOption( CommandLineParameterVerboseShort,         CommandLineParameterVerbose );

			commandLineParser.parse(args);
			_verbose          = ((Boolean)commandLineParser.getOptionValue(commandLineOptionVerbose, false)).booleanValue();
			_outputDirectoryForUserImplementation  = (String)commandLineParser.getOptionValue(commandLineOptionOutputUserDir,"./");
			_outputDirectoryForStubs  = (String)commandLineParser.getOptionValue(commandLineOptionOutputStubsDir,"./");

			_includedSIDLFiles = (java.util.Vector<String>)commandLineParser.getOptionValues(commandLineOptionInclude);
			_sourceSIDLFile = (commandLineParser.getRemainingArgs())[0];
			if(!_sourceSIDLFile.contains(".sidl")){
				_isValid=false;
				throw new ASCoDTException(Compiler.class.getCanonicalName(), "parseCommandLineArguments()", "Compiler error invalid sidl source file",null);
			}
		} catch (IllegalOptionValueException e) {
			_isValid=false;
			throw new ASCoDTException(Compiler.class.getCanonicalName(), "parseCommandLineArguments()", "Compiler error illegal value:"+e.getMessage(), e);

		} catch (UnknownOptionException e) {
			_isValid=false;
			throw new ASCoDTException(Compiler.class.getCanonicalName(), "parseCommandLineArguments()", "Compiler error unknown argument:"+e.getMessage(), e);
		} catch (ArrayIndexOutOfBoundsException e){
			_isValid=false;
			throw new ASCoDTException(Compiler.class.getCanonicalName(), "parseCommandLineArguments()", "Compiler error wrong number of arguments:"+e.getMessage(), e);
		
		}

	}

	/**
	 * @return Is the compiler state valid, i.e. was the compiler workflow 
	 *         successful.
	 */
	boolean isValid() {
		return _isValid;
	}

	/**
	 * Entry point for the compiler if it is instantiated on the command line.
	 */
	public static void main(String[] args) {
		header();
		Compiler compiler = new Compiler();
		try{
			compiler.parseCommandLineArguments(args);
			compiler.processDependencies();
			Start startSymbol=compiler.processSource( compiler._sourceSIDLFile );
			compiler.buildSymbolTable(startSymbol, compiler._sourceSIDLFile);
			compiler.validate(startSymbol, compiler._sourceSIDLFile);
			compiler.generateBluePrints();
			if (compiler._verbose) {
				System.out.println( "finished successfully" );
			}

		}catch(ASCoDTException e){
			usage();
			e.printStackTrace();


		}
	}


	private void generateBluePrints() throws ASCoDTException {
		CreateComponentsAndInterfaces interfaces= new CreateComponentsAndInterfaces(_symbolTable);
		try {
			interfaces.create(
					new File(_outputDirectoryForStubs).toURI().toURL(),
					new File(_outputDirectoryForUserImplementation).toURI().toURL(),
					new File(_outputDirectoryForStubs).toURI().toURL()
			);
		} catch (Exception e) {
			_isValid=false;
			throw new ASCoDTException(Compiler.class.getCanonicalName(), "generateBluePrints()",e.getLocalizedMessage(),e); 
		}

	}

	/**
	 * validates a given symbol table
	 * @param startSymbol starting symbol of a sidl file. 
	 * @throws ASCoDTException 
	 */
	private void validate(Start startSymbol,String resourceLocation) throws ASCoDTException {
		ASTValidator validator = new ASTValidator(_symbolTable,resourceLocation);
		startSymbol.apply(validator);
		if(!validator.isValid())
			throw new ASCoDTException(Compiler.class.getName(), "validate()", "AST not valid! AST error message:"+validator.getErrorMessages(),null);

	}

	/**
	 * reads a sidl file, constructs a parser for it and returns the start symbol 
	 * of the file
	 * @param fileName the file to be parsed
	 * @return
	 * @throws ASCoDTException
	 */
	private de.tum.ascodt.sidlcompiler.frontend.node.Start processSource( String fileName ) throws ASCoDTException {
		de.tum.ascodt.sidlcompiler.frontend.node.Start start = null;
		if (_verbose) {
			System.out.println( "read input file " + fileName + " ... ");
		}
		FileReader reader;
		try {
			reader = new java.io.FileReader( fileName );

			de.tum.ascodt.sidlcompiler.frontend.parser.Parser parser = new de.tum.ascodt.sidlcompiler.frontend.parser.Parser(
					new de.tum.ascodt.sidlcompiler.frontend.lexer.Lexer(
							new java.io.PushbackReader(
									reader
							)
					)
			);
			start = parser.parse();
			reader.close();
		} catch (Exception e) {
			throw new ASCoDTException(Compiler.class.getCanonicalName(),"readSIDLFile()",e.getMessage(),e);
		}
		return start;
	}



	/**
	 * extends an existing symbol table with the symbols included in 
	 * specific sidl file passed as start symbol
	 * @param astRootNode the start symbol of the sidl file to be parsed
	 * @throws Exception
	 */
	private void buildSymbolTable(
			de.tum.ascodt.sidlcompiler.frontend.node.Start astRootNode,
			String fileName
	) {
		de.tum.ascodt.sidlcompiler.symboltable.BuildSymbolTable adapter = 
			new de.tum.ascodt.sidlcompiler.symboltable.BuildSymbolTable(_symbolTable, fileName);
		astRootNode.apply( adapter );
	}

	/**
	 * process all sidl dependencies
	 * @throws ASCoDTException 
	 */
	private void processDependencies() throws ASCoDTException {
		for (String includeFileName: _includedSIDLFiles ){
			Start startSymbol=processSource( includeFileName );
			buildSymbolTable(startSymbol,includeFileName);
			validate(startSymbol,includeFileName);
			
		}

	}




}
