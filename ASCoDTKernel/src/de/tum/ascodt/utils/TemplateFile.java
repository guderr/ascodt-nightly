package de.tum.ascodt.utils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.ASCoDTKernel;
import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.resources.ResourceManager;
import de.tum.ascodt.utils.exceptions.ASCoDTException;


/**
 * Represents a template file
 *
 * The template file holds one template file and a map from strings to other 
 * strings. This way, it is basically a text replacement system, i.e. it can 
 * take one template file, replace all strings within this file, and write it 
 * to another file.
 * 
 * There's some very simple extensions to a standard text replacement system: 
 * You can explicitly give  
 *   
 * @author Tobias Weinzierl
 */
public class TemplateFile {
	private static final String KeywordInsertHere = "-- INSERT HERE --";

	private static final String KeywordClosePackage = "__CLOSE_PACKAGE__";

	private static final String KeywordOpenPackage  = "__OPEN_PACKAGE__";

	static public class LanguageConfiguration {
		public String   _namespaceSeparator;
		public String   _openNamespaceIdentifier;
		public boolean  _openNamespaceHierarchically;
		public boolean  _useCloseIdentifier;
		public String _closeNamespaceIdentifier;
		

		/**
		 * Do not instantiate yourself but use static getters instead
		 */
		private LanguageConfiguration() {}
	}

	 static public LanguageConfiguration getLanguageConfigurationForFortran() {
			LanguageConfiguration result 				= new LanguageConfiguration();
			result._namespaceSeparator          = "_";
			result._openNamespaceIdentifier     = "module";
			result._closeNamespaceIdentifier	  = "end module ";
			result._openNamespaceHierarchically = false;
			result._useCloseIdentifier 				  = true;
		return result;
	} 
	static public LanguageConfiguration getLanguageConfigurationForJava() {
		LanguageConfiguration result = new LanguageConfiguration();
		result._namespaceSeparator          = ".";
		result._openNamespaceIdentifier     = "package";
		result._closeNamespaceIdentifier	  = "";
		result._openNamespaceHierarchically = false;
		result._useCloseIdentifier 				  = false;
		return result;
	}

	static public LanguageConfiguration getLanguageConfigurationForSIDL() {
		LanguageConfiguration result = new LanguageConfiguration();
		result._namespaceSeparator          = ".";
		result._openNamespaceIdentifier     = "package";
		result._closeNamespaceIdentifier	  = "";
		result._openNamespaceHierarchically = true;
		return result;
	}

	static public LanguageConfiguration getLanguageConfigurationForCPP() {
		LanguageConfiguration result = new LanguageConfiguration();
		result._namespaceSeparator          = "::";
		result._openNamespaceIdentifier     = "namespace";
		result._openNamespaceHierarchically = true;
		result._closeNamespaceIdentifier	  = "";
		result._useCloseIdentifier 				  = false;
		return result;
	}

	static public LanguageConfiguration getLanguageConfigurationForJNI() {
		LanguageConfiguration result = new LanguageConfiguration();
		result._namespaceSeparator          = "_";
		result._openNamespaceIdentifier     = "namespace";
		result._openNamespaceHierarchically = false;
		result._closeNamespaceIdentifier	  = "";
		result._useCloseIdentifier 				  = false;
		return result;
	}

	private static Trace                       _trace = new Trace( "de.tum.ascodt.utils.TemplateFile" ); 

	private java.io.OutputStreamWriter         _destinationFileWriter;
	private java.io.BufferedReader             _templateFileReader;

	private boolean                            _hasToCloseOutputStream;


	/**
	 * All the mappings from keywords to concrete names.
	 */
	private java.util.HashMap<String, String>  _mapping;

	/**
	 * All conditional regions identifier
	 */
	private java.util.HashSet<String> _conditions;

	/**
	 * TemplateFile instances can be built on top of each other, i.e. you create 
	 * one template file and then a second on top of it that inserts additional 
	 * data into the output stream. In this case, this attribute points to the 
	 * parent. Otherwise, it is null.
	 * 
	 * When we do the text replacement, also the parent replacements are 
	 * performed.
	 */
	private TemplateFile                       _parentTemplateFile;

	/**
	 * Namespace of generated artifacts. Always is kept separately, as it 
	 * deserves special treatment. 
	 */
	private String[]                           _namespace;

	private LanguageConfiguration              _languageConfiguration;

	private boolean _overwrite;
	private boolean _readable;
	/**
	 * 
	 * @param templateFile
	 * @param overwriteExistingFile
	 * @param namespace
	 * @param namespaceSeparator      For Java and SIDl, use ".", for C++ use "::".
	 * @param namespaceIdentifier     For C++ use "namespace", for SIDL use "
	 */
	public TemplateFile(
			String                 templateFile, 
			String                 destinationFile,
			String[]               namespace, 
			LanguageConfiguration  languageConfiguration,
			boolean overwrite
			) throws ASCoDTException {
		try {
			_trace.in( "TemplateFile(...)", templateFile, destinationFile, namespace);

			destinationFile = destinationFile.replaceAll("\\\\", "/");
			templateFile    = templateFile.replaceAll("\\\\", "/");



			_mapping               = new java.util.HashMap<String, String>();
			_conditions 					 = new java.util.HashSet<String>();
			_namespace             = namespace;
			_languageConfiguration = languageConfiguration;

			_hasToCloseOutputStream = true;
			_parentTemplateFile     = null;
			_overwrite=overwrite;
			_readable=true;
			openTemplateStream( ResourceManager.getResourceAsStream(templateFile,ASCoDTKernel.ID) );
			openOutputStream( new URL(destinationFile) );
			_trace.out( "TemplateFile(...)" );
		}
		catch (MalformedURLException e) {
			throw new ASCoDTException(getClass().getName(), "TemplateFile(...)", "invalid URL", e);
		} catch (IOException e) {
			throw new ASCoDTException(getClass().getName(), "TemplateFile(...)", "invalid template stream", e);
		}
	}


	public TemplateFile(
			InputStream                    templateStream, 
			URL                    destinationFile, 
			String[]               namespace, 
			LanguageConfiguration  languageConfiguration,
			boolean overwrite
			) throws ASCoDTException {
		_trace.in( "TemplateFile(...)",  destinationFile.toString(), namespace);



		_mapping               = new java.util.HashMap<String, String>();
		_namespace             = namespace;
		_languageConfiguration = languageConfiguration;

		_hasToCloseOutputStream = true;
		_parentTemplateFile     = null;
		_overwrite=overwrite;
		_readable=true;
		openTemplateStream( templateStream );
		openOutputStream( destinationFile );
		_trace.out( "TemplateFile(...)" );
	}

//
//	/**
//	 * Kind of a copy constructor. Takes another template file and inserts data 
//	 * into it.
//	 * 
//	 * @param copy
//	 */
//	public TemplateFile(
//			TemplateFile  copy,
//			InputStream           templateStream
//			) throws ASCoDTException {
//		_trace.in( "TemplateFile(...)");
//
//		openTemplateStream( templateStream );
//
//		_destinationFileWriter = copy._destinationFileWriter;
//		_mapping               = new java.util.HashMap<String, String>();
//		_namespace             = copy._namespace;
//		_languageConfiguration = copy._languageConfiguration;
//
//		_hasToCloseOutputStream = false;
//		_parentTemplateFile     = copy;
//		_overwrite=copy._overwrite;
//		_readable=copy._readable;
//		_trace.out( "TemplateFile(...)" );
//	}


	/**
	 * Kind of a copy constructor. Takes another template file and inserts data 
	 * into it.
	 * 
	 * @param copy
	 */
	public TemplateFile(
			TemplateFile  copy,
			String        templateFile
			) throws ASCoDTException {
		_trace.in( "TemplateFile(...)" );

		
		try {
			openTemplateStream( ResourceManager.getResourceAsStream(templateFile,ASCoDTKernel.ID));
		}
		catch (Exception e ) {
			throw new ASCoDTException(getClass().getName(), "TemplateFile(...)", "cannot open stream of file " , e);
		}

		_destinationFileWriter = copy._destinationFileWriter;
		_mapping               = new java.util.HashMap<String, String>();
		_namespace             = copy._namespace;
		_languageConfiguration = copy._languageConfiguration;

		_hasToCloseOutputStream = false;
		_parentTemplateFile     = copy;
		_overwrite=copy._overwrite;
		_readable=copy._readable;
		_trace.out( "TemplateFile(...)" );
	}


	/**
	 * Add a new mapping. Keywords typically are started with two underscores and 
	 * end with two underscores.
	 * 
	 * @param keyword
	 * @param replacement
	 */
	public void addMapping( String keyword, String replacement ) {
		Assert.isTrue( !_mapping.containsKey(keyword) );
		Assert.isTrue( keyword != KeywordOpenPackage );
		Assert.isTrue( keyword != KeywordClosePackage );
		_mapping.put( keyword, replacement );
	}

	public void addCondition(String conditionVariable){
		_conditions.add(conditionVariable);
	}

	/**
	 * Replace all the keywords.
	 * 
	 * @param line
	 * @return
	 */
	private String replaceKeywords(String line) {
		String text = line;
		if(text.contains("__COND")){
			text=text.replaceAll("__COND", "_");
			if(_conditions.contains(text.trim())){
				_readable=true;
				
			}else{
				
				_readable=false;
			}
			text="";
		}else if(text.contains("__END_COND__")){
			_readable=true;
			text="";
		}
		if(_readable){
			for (String p : _mapping.keySet()) {
				text = text.replaceAll(p, _mapping.get(p));
			}

			if (line.contains( KeywordOpenPackage )) {
				if (_languageConfiguration._openNamespaceHierarchically) {
					for( int indent=0; indent<_namespace.length; indent++) {
						text = text.replaceFirst(KeywordOpenPackage, _languageConfiguration._openNamespaceIdentifier+" " + _namespace[indent] + " { \n" );
						for (int i=0; i<indent; i++) {
							text += "  ";
						}
						text = text + KeywordOpenPackage;
					}
				}
				else {
					text = text.replaceFirst(
							KeywordOpenPackage, 
							_languageConfiguration._openNamespaceIdentifier 
							+ " "
							+ _namespace[0]
									+ "__OPEN_PACKAGE__" );
					for( int i=1; i<_namespace.length; i++) {
						text=text.replaceFirst(KeywordOpenPackage,  _languageConfiguration._namespaceSeparator + _namespace[i] + KeywordOpenPackage );
					}
				}
				text=text.replaceFirst(KeywordOpenPackage,  "" );
			}
			else if (line.contains( KeywordClosePackage ) ) {
				if (_languageConfiguration._openNamespaceHierarchically) {
					int indent=0;
					for(; indent<_namespace.length-1; indent++) {
						for (int i=0; i<indent; i++) {
							text += "  ";
						}
						text=text.replaceFirst(KeywordClosePackage, "} \n" + KeywordClosePackage );
					}
					if( indent<_namespace.length)
						text=text.replaceFirst(KeywordClosePackage, "}" );
				}
				else {
					if(!_languageConfiguration._useCloseIdentifier)
						text=text.replaceAll(KeywordClosePackage, _languageConfiguration._closeNamespaceIdentifier);
					else{
						text = text.replaceFirst(
								KeywordClosePackage, 
								_languageConfiguration._closeNamespaceIdentifier 
								+ " "
								+ _namespace[0]
										+ KeywordClosePackage );
						for( int i=1; i<_namespace.length; i++) {
							text=text.replaceFirst(KeywordClosePackage,  _languageConfiguration._namespaceSeparator + _namespace[i] + KeywordClosePackage );
						}
					}
				}
				text=text.replaceFirst(KeywordClosePackage,  "" );
					
				
						
			}
		}else
			text="";
		if (_parentTemplateFile!=null) {
			return _parentTemplateFile.replaceKeywords(text);
		}
		else {
			return text;
		}
	}


	private void openTemplateStream(InputStream io) throws ASCoDTException {
		try {
			_templateFileReader = new java.io.BufferedReader(
					new InputStreamReader(io));
		} catch (Exception e) {
			throw new ASCoDTException(getClass().getName(), "openTemplateStream(URL)", "open file failed for file " , e);
		}
	}


	/**
	 * Open the output stream
	 * 
	 * First, we strip the path from the file name. Here, we may not use \\, 
	 * i.e. File.separatorChar. This is due to a simple reason: The path 
	 * comes from an URL and consequently does already contain only slashes.  
	 * 
	 * @param file
	 * @throws ASCoDTException
	 */
	private void openOutputStream(URL file) throws ASCoDTException {
		_trace.in( "openOutputStream(URL)", file.toString() );

		try {
			String pathOfFile = file.getPath().toString();
			String dirPath = pathOfFile.substring(0,pathOfFile.lastIndexOf( "/" ));
			File   path       = new File(pathOfFile);
			File dir=new File(dirPath);
			if((!_overwrite&&!path.exists())||_overwrite){
				if (!dir.exists()) {
					_trace.debug( "openOutputStream(URL)", "path of " + path.toString() + " does not exist. create");
					dir.mkdirs();
				}
				else {
					_trace.debug( "openOutputStream(URL)", "path of " + path.toString() + " already exists");
				}

				_destinationFileWriter = new java.io.OutputStreamWriter(
						new java.io.FileOutputStream(file.getFile()));
			}
		} catch (Exception e) {
			throw new ASCoDTException(getClass().getName(), "openTemplateStream(URL)", "write file failed for file " + file.getFile(), e);
		}

		_trace.out( "openOutputStream(URL)" );
	}


	private void processTemplateStream() throws ASCoDTException {
		try {
			String text = _templateFileReader.readLine();

			while ( text != null && !text.trim().equals( KeywordInsertHere )) {
				text = replaceKeywords(text);
				if(_destinationFileWriter!=null){
					_destinationFileWriter.write(text);
					_destinationFileWriter.write("\n");
				}
				text = _templateFileReader.readLine();
			}
		} catch (Exception e) {
			throw new ASCoDTException(getClass().getName(), "openTemplateStream(URL)", "write file failed", e);
		}
	}


	/**
	 * This operation does two things: It first processes the remaining part of 
	 * the template file. Then, it closes all the streams and returns. Be careful: 
	 * The output stream is closed if and only if this class also was responsible 
	 * to open it.
	 *  
	 * @throws ASCoDTException
	 */
	public void close() throws ASCoDTException {
		_trace.in( "close()" );

		processTemplateStream();
		closeStreams();

		_trace.out( "close()" );    
	}


	private void closeStreams() throws ASCoDTException {
		try {
			if (_hasToCloseOutputStream) {
				if(_destinationFileWriter!=null){
					_destinationFileWriter.flush();
					_destinationFileWriter.close();
				}
			}
			_templateFileReader.close();
		} catch (IOException e) {
			throw new ASCoDTException(getClass().getName(), "closeStreams(URL)", "write file failed", e);
		}
	}

	/**
	 * You always have to call close() after open().
	 */
	public void open() throws ASCoDTException {
		_trace.in( "open()" );

		processTemplateStream();

		_trace.out( "open()" );
	}

	 
}
