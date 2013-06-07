package de.tum.ascodt.sidlcompiler.astproperties;

import java.util.HashSet;

import org.eclipse.core.runtime.Assert;

import de.tum.ascodt.plugin.utils.tracing.Trace;
import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
import de.tum.ascodt.sidlcompiler.frontend.node.ABoolBuiltInType;
import de.tum.ascodt.sidlcompiler.frontend.node.ADoubleBuiltInType;
import de.tum.ascodt.sidlcompiler.frontend.node.AEnumDeclarationPackageElement;
import de.tum.ascodt.sidlcompiler.frontend.node.AIntBuiltInType;
import de.tum.ascodt.sidlcompiler.frontend.node.AOpaqueBuiltInType;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayInEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayOutEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterArrayOutParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterInEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterInParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterOutEnumParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AParameterOutParameter;
import de.tum.ascodt.sidlcompiler.frontend.node.AStringBuiltInType;
import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
import de.tum.ascodt.sidlcompiler.symboltable.Scope;

/**
 * Get the parameter list
 * 
 * Very simple class extracting the parameter list from the AST. It also 
 * provides a couple of operations to convert these lists into Java and 
 * C++ code. This is not a strict separation of concerns AST/backend, but 
 * I found it simpler this way.
 * 
 * @author Tobias Weinzierl
 */
public class GetParameterList extends DepthFirstAdapter {
	private static Trace _trace = new Trace( "de.tum.ascodt.sidlcompiler.astproperties.GetParameterList" );

	static class Parameter {
		public enum Type {
			Integer, Double, Boolean, String, UserDefined, Opaque
		}

		public String  name;

		public Type    type;

		public String  userDefinedTypeIdentifier;
		/**
		 * Set if the parameter is out or inout.
		 */
		public boolean isOut;

		public boolean isArray;
	}

	private java.util.List< Parameter >   _parameters;
	private boolean _hasEnums;
	private HashSet<String> _enumTypes;
	private Scope _scope;
	public String getFunctionCallListInJava() {
		String result = "";  

		for (Parameter parameter: _parameters) {
			result += ",";
			result += parameter.name;
		}    

		result = result.replaceFirst(",", "");
		return result;
	}

	public String getFunctionCallListInJNI2Java() {
		String result = "";  

		for (Parameter parameter: _parameters) {
			result += ",";
			if(parameter.type!=Parameter.Type.UserDefined)
				result += parameter.name;
			else if(parameter.type==Parameter.Type.UserDefined&&!parameter.isArray&&!parameter.isOut)
				result+=parameter.userDefinedTypeIdentifier+".values()["+parameter.name+"]";
			else if(parameter.type==Parameter.Type.UserDefined&&(parameter.isArray||parameter.isOut))
				result += parameter.name+"_as_enum";
		}    

		result = result.replaceFirst(",", "");
		return result;
	}
	public String getFunctionCallListInJava2JNI() {
		String result = "";  

		for (Parameter parameter: _parameters) {
			result += ",";

			result += parameter.name;
			if(parameter.type==Parameter.Type.UserDefined&&!parameter.isArray&&!parameter.isOut){
				result+=".ordinal()";
			}else if(parameter.type==Parameter.Type.UserDefined){
				result+="_as_int";
			}
		}    

		result = result.replaceFirst(",", "");
		return result;
	}
	public String getFunctionCallListInJNI2Cxx() {
		String result = "";  

		for (Parameter parameter: _parameters) {
			result += ",";
			if(!parameter.isArray&&!parameter.isOut&&parameter.type==Parameter.Type.Opaque)
				result+="(void*)";
			else if(parameter.type==Parameter.Type.Opaque&!parameter.isArray&&parameter.isOut)
				result+="(void*&)";
			else if(parameter.type==Parameter.Type.Opaque&parameter.isArray)
				result+="(void**)";
			if(!parameter.isArray&&!parameter.isOut&&parameter.type==Parameter.Type.UserDefined){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result+="("+fullQuualifiedTypeName.replaceAll("[.]", "::")+")";
			}else if(parameter.type==Parameter.Type.UserDefined&(!parameter.isArray&&parameter.isOut)){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result+="("+fullQuualifiedTypeName.replaceAll("[.]", "::")+"&)";
			}else if(parameter.type==Parameter.Type.UserDefined&(parameter.isArray)){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result+="("+fullQuualifiedTypeName.replaceAll("[.]", "::")+"*)";
			}

			result += parameter.name;
			if((parameter.type!=Parameter.Type.Boolean)&&(parameter.isArray||parameter.isOut||
					(!parameter.isArray&&!parameter.isOut&&parameter.type==Parameter.Type.String)))
				result +="_jni"; 
			if(parameter.type==Parameter.Type.Boolean)
				result +="_b"; 
			if(!parameter.isArray&&parameter.isOut&&parameter.type!=Parameter.Type.Boolean)
				result += "[0]";

			if(parameter.isArray)
				result += ","+parameter.name+"_jni_len";

		}    


		result = result.replaceFirst(",", "");
		return result;
	}

	public String getFunctionCallListInF2Cxx() {
		String result = "";  
		String delim="";
		for (Parameter parameter: _parameters) {
			result += delim;
			if(!parameter.isArray&&(parameter.type!=Parameter.Type.String))
				result += "*";
			result += parameter.name+"";
			if(parameter.type==Parameter.Type.String)
				result += "_str";
			if(parameter.isArray)
				result+=",*"+parameter.name+"_len";
			delim=",";
		}    


		return result;
	}
	public String getFunctionCallListInCxx() {
		String result = "";  
		String delim="";
		for (Parameter parameter: _parameters) {

			result += delim+parameter.name+"";
			if(parameter.isArray)
				result+=","+parameter.name+"_len";
			delim=",";
		}    


		return result;
	}

	public String getFunctionCallListInCxx2JNI() {
		String result = "";  

		for (Parameter parameter: _parameters) {
			result += ",";
			result += parameter.name+"_jni";
		}    


		return result;
	}

	public String prepareJavaEnumParametersForJNI2JavaCall(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
				result+=parameter.userDefinedTypeIdentifier+"[] "+parameter.name+"_as_enum=new "+parameter.userDefinedTypeIdentifier+"["+parameter.name+".length];\n";
				result+="for(int i=0;i<"+parameter.name+".length;i++)\n";
				result+="\t"+parameter.name+"_as_enum[i]="+parameter.userDefinedTypeIdentifier+".values()["+parameter.name+"[i]];\n";
			}else if(parameter.type==Parameter.Type.UserDefined&&parameter.isOut){
				result+=parameter.userDefinedTypeIdentifier+"[] "+parameter.name+"_as_enum=new "+parameter.userDefinedTypeIdentifier+"[1];\n";
				result+=parameter.name+"_as_enum[0]="+parameter.userDefinedTypeIdentifier+".values()["+parameter.name+"[0]];\n";
			}
		}
		return result;
	}

	public String prepareJavaEnumParametersForJava2JNICall(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
				result+="int[] "+parameter.name+"_as_int=new int["+parameter.name+".length];\n";
				result+="for(int i=0;i<"+parameter.name+".length;i++)\n";
				result+="\t"+parameter.name+"_as_int[i]="+parameter.name+"[i].ordinal();\n";
			}else if(parameter.type==Parameter.Type.UserDefined&&parameter.isOut){
				result+="int[] "+parameter.name+"_as_int=new int[1];\n";
				result+=parameter.name+"_as_int[0]="+parameter.name+"[0].ordinal();\n";
			}
		}
		return result;
	}

	public String writeJavaEnumParametersAfterJava2JNICall(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.type==Parameter.Type.UserDefined && parameter.isOut&&parameter.isArray){
				result+="for(int i=0;i<"+parameter.name+".length;i++)\n";
				result+="\t"+parameter.name+"[i]="+parameter.userDefinedTypeIdentifier+".values()["+parameter.name+"_as_int[i]];\n";
			}else if(parameter.type==Parameter.Type.UserDefined&&parameter.isOut){
				result+=parameter.name+"[0]="+parameter.userDefinedTypeIdentifier+".values()["+parameter.name+"_as_int[0]];\n";
			}
		}
		return result;
	}

	public String writeJavaEnumParametersAfterJNI2JavaCall(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.type==Parameter.Type.UserDefined && parameter.isOut&&parameter.isArray){
				result+="for(int i=0;i<"+parameter.name+".length;i++)\n";
				result+="\t"+parameter.name+"[i]="+parameter.name+"_as_enum[i].ordinal();\n";
			}else if(parameter.type==Parameter.Type.UserDefined&&parameter.isOut){
				result+=parameter.name+"[0]="+parameter.name+"_as_enum[0].ordinal();\n";
			}
		}
		return result;
	}

	public String prepareJNIParametersForCxxCall(){
		_trace.in( "prepareJNIParametersForCxxCall()"  );
		String result = "";
		for (Parameter parameter: _parameters) {



			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray)) {
				result += "jboolean* "+parameter.name+"_jni = env->GetBooleanArrayElements(" + parameter.name + ",0);\n";
				result += "bool* "+parameter.name+"_b = new bool[env->GetArrayLength(" + parameter.name + ")];\n";
				result += "for(int i=0;i<env->GetArrayLength(" + parameter.name + ");i++)\n";
				result += ""+parameter.name+"_b=("+parameter.name+"_jni[i])?true:false;\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";

			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result += "jdouble* "+parameter.name+"_jni = env->GetDoubleArrayElements(" + parameter.name + ",0);\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result += "jint* "+parameter.name+"_jni = env->GetIntArrayElements(" + parameter.name + ",0);\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result+="std::string* "+parameter.name+"_jni = new std::string[env->GetArrayLength("+parameter.name+")];\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";
				result+="for(int i=0;i<"+parameter.name+"_jni_len;i++)\n";
				result+="\t"+parameter.name+"_jni[i]=std::string(env->GetStringUTFChars((jstring)env->GetObjectArrayElement("+parameter.name+",i),0));\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {
				result += "jint* "+parameter.name+"_jni = env->GetIntArrayElements(" + parameter.name + ",0);\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {
				result += "jlong* "+parameter.name+"_jni = env->GetLongArrayElements(" + parameter.name + ",0);\n";
				result += "int "+parameter.name+"_jni_len = env->GetArrayLength(" + parameter.name + ");\n";
			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray&&parameter.isOut)) {
				result += "jboolean* "+parameter.name+"_jni = env->GetBooleanArrayElements(" + parameter.name + ",0);\n";
				result += "bool "+parameter.name+"_b=("+parameter.name+"_jni[0])?true:false;\n";


			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray&&parameter.isOut)) {
				result += "jdouble* "+parameter.name+"_jni = env->GetDoubleArrayElements(" + parameter.name + ",0);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray&&parameter.isOut)) {
				result += "jint* "+parameter.name+"_jni = env->GetIntArrayElements(" + parameter.name + ",0);\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray&&parameter.isOut )) {
				result+="std::string* "+parameter.name+"_jni = new std::string[1];\n";
				result+=parameter.name+"_jni[i]=std::string(env->GetStringUTFChars((jstring)env->GetObjectArrayElement("+parameter.name+",0),0));\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray&&parameter.isOut)) {
				result += "jint* "+parameter.name+"_jni = env->GetIntArrayElements(" + parameter.name + ",0);\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray&&parameter.isOut)) {
				result += "jlong* "+parameter.name+"_jni = env->GetLongArrayElements(" + parameter.name + ",0);\n";
			}

			if (parameter.type==Parameter.Type.String && (!parameter.isArray&&!parameter.isOut )) {
				result+="std::string "+parameter.name+"_jni = std::string(env->GetStringUTFChars((jstring)"+parameter.name+",0));\n";
			}


		}


		_trace.out( "prepareJNIParametersForCxxCall()"  );
		return result;
	}
	
	public String pushInToSocketForCxx() {
		String result = "";
		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="sendData((char*)"+parameter.name+",sizeof(bool)*"+parameter.name+"_len,_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="sendData((char*)"+parameter.name+",sizeof(double)*"+parameter.name+"_len,_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="sendData((char*)"+parameter.name+",sizeof(int)*"+parameter.name+"_len,_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
				result+="\tint data_size="+parameter.name+"[i].size();\n";
				result+="\tsendData((char*)&data_size,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="\tsendData((char*)"+parameter.name+"[i].c_str(),"+parameter.name+"[i].size()<255?"+parameter.name+"[i].size():255,_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="}\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
				result+="sendData((char*)&"+parameter.name+",sizeof(bool),_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+",sizeof(double),_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
				result+="sendData((char*)&"+parameter.name+",sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
				result+="int "+parameter.name+"_data_size="+parameter.name+".size();\n";
				
				result+="sendData((char*)&"+parameter.name+"_data_size,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
				result+="sendData((char*)"+parameter.name+".c_str(),"+parameter.name+".size(),_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
			}

		}
		return result;
	}
	
	public String pushInToSocketForJava() {
		String result = "";
		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
				result+="sendIntData("+parameter.name+".length);\n";
				result+="sendBooleanData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result+="sendIntData("+parameter.name+".length);\n";
				result+="sendDoubleData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result+="sendIntData("+parameter.name+".length);\n";
				result+="sendIntData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result+="sendIntData("+parameter.name+".length);\n";
				result+="sendStringData("+parameter.name+");\n";
//				result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
//				result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
//				result+="\tint data_size="+parameter.name+"[i].size();\n";
//				result+="\tsendData((char*)&data_size,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
//				result+="\tsendData((char*)"+parameter.name+"[i].c_str(),"+parameter.name+"[i].size()<255?"+parameter.name+"[i].size():255,_sendBuffer,_newsockfd,_buffer_size);\n";
//				result+="}\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
				result+="sendBooleanData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
				result+="sendDoubleData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
				result+="sendIntData("+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
				result+="sendStringData("+parameter.name+");\n";
//				result+="int "+parameter.name+"_data_size="+parameter.name+".size();\n";
//				
//				result+="sendData((char*)&"+parameter.name+"_data_size,sizeof(int),_sendBuffer,_newsockfd,_buffer_size);\n";
//				result+="sendData((char*)"+parameter.name+".c_str(),sizeof("+parameter.name+".size()),_sendBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
			}

		}
		return result;
	}
  public String pullInFromSocketForJava(){
  	String result = "";
		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=readIntData();\n";
				
				result+="boolean []"+parameter.name+"=new bool["+parameter.name+"_len];\n";

				result+="readBooleanData("+parameter.name+","+parameter.name+"_len);\n";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=readIntData();\n";
				result+="double []"+parameter.name+"=new double["+parameter.name+"_len];\n";
				result+="readDoubleData("+parameter.name+","+parameter.name+"_len);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=readIntData();\n";
				result+="int []"+parameter.name+"=new int["+parameter.name+"_len];\n";
				result+="readIntData("+parameter.name+","+parameter.name+"_len);\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=readIntData();\n";
				result+="String []"+parameter.name+"=new String["+parameter.name+"_len];\n";
				result+="readStringData("+parameter.name+","+parameter.name+"_len);\n";

			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray) && !parameter.isOut) {
				result+="boolean "+parameter.name+"=readBooleanData();\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray ) && !parameter.isOut) {
				result+="double "+parameter.name+"=readDoubleData();\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray ) && !parameter.isOut) {
				result+="int "+parameter.name+"=readIntData();\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray )&& !parameter.isOut ) {
				result+="String "+parameter.name+"=readStringData();\n";
//				result+="int "+parameter.name+"_str_len=0;\n";
//				result+="readData((char*)&"+parameter.name+"_str_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
//				result+="char* "+parameter.name+"=new char["+parameter.name+"_str_len];\n";  
//				result+="readData((char*)"+parameter.name+","+parameter.name+"_str_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray ) && !parameter.isOut) {
				//result+="readData((char*)"+parameter.name+",sizeof(bool),_rcvBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray) && !parameter.isOut ) {
			}
			
			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray) && parameter.isOut) {
				result+="boolean [] "+parameter.name+"= new boolean[1];\n";
				result+="readBooleanData("+parameter.name+",1);\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray ) && parameter.isOut) {
				result+="double [] "+parameter.name+"= new double[1];\n";
				result+="readDoubleData("+parameter.name+",1);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray ) && parameter.isOut) {
				result+="int [] "+parameter.name+"= new int[1];\n";
				result+="readIntData("+parameter.name+",1);\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray )&& parameter.isOut ) {
				result+="String [] "+parameter.name+"= new String[1];\n";
				result+="readStringData("+parameter.name+",1);\n";
//				result+="int "+parameter.name+"_str_len=0;\n";
//				result+="readData((char*)&"+parameter.name+"_str_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
//				result+="char* "+parameter.name+"=new char["+parameter.name+"_str_len];\n";  
//				result+="readData((char*)"+parameter.name+","+parameter.name+"_str_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray ) && parameter.isOut) {
				//result+="readData((char*)"+parameter.name+",sizeof(bool),_rcvBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray) && parameter.isOut ) {
			}

		}
		return result;
  }
	 
	public String pullInFromSocketForCxx(){
		String result = "";
		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=0;\n";
				result+="readData((char*)&"+parameter.name+"_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				
				result+="bool* "+parameter.name+"=new bool["+parameter.name+"_len];\n";

				result+="readData((char*)"+parameter.name+",sizeof(bool)*"+parameter.name+"_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=0;\n";
				result+="readData((char*)&"+parameter.name+"_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				
				result+="double* "+parameter.name+"=new double["+parameter.name+"_len];\n";
				result+="readData((char*)"+parameter.name+",sizeof(double)*"+parameter.name+"_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=0;\n";
				result+="readData((char*)&"+parameter.name+"_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				
				result+="int* "+parameter.name+"=new int["+parameter.name+"_len];\n";

				result+="readData((char*)"+parameter.name+",sizeof(int)*"+parameter.name+"_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result+="int "+parameter.name+"_len=0;\n";
				result+="readData((char*)&"+parameter.name+"_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				result+="char (* "+parameter.name+")[255]=new char["+parameter.name+"_len][255];\n";

				result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
				result+="\tint "+parameter.name+"_data_len=0;\n";
				result+="\treadData((char*)&"+parameter.name+"_data_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				result+="\treadData((char*)"+parameter.name+"[i],"+parameter.name+"_data_len<255?"+parameter.name+"_data_len:255,rcvBuffer,newsockfd,buffer_size);\n";
				result+="}\n";

			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
				result+="bool "+parameter.name+";\n";
				result+="readData((char*)&"+parameter.name+",sizeof(bool),rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
				result+="double "+parameter.name+";\n";
				result+="readData((char*)&"+parameter.name+",sizeof(double),rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
				result+="int "+parameter.name+";\n";
				result+="readData((char*)&"+parameter.name+",sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
				result+="int "+parameter.name+"_str_len=0;\n";
				result+="readData((char*)&"+parameter.name+"_str_len,sizeof(int),rcvBuffer,newsockfd,buffer_size);\n";
				result+="char* "+parameter.name+"=new char["+parameter.name+"_str_len];\n";  
				result+="readData((char*)"+parameter.name+","+parameter.name+"_str_len,rcvBuffer,newsockfd,buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
				//result+="readData((char*)"+parameter.name+",sizeof(bool),_rcvBuffer,_newsockfd,_buffer_size);\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
			}

		}
		return result;
	}
	public String pushOutToSocketForCxx(){
		String result = "";
		for (Parameter parameter: _parameters) {
			if(parameter.isOut){
				if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(bool)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(double)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(int)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
					result+="\tint data_size="+parameter.name+"[i].size();\n";
					result+="\tsendData((char*)&data_size,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="\tsendData((char*)"+parameter.name+"[i].c_str(),"+parameter.name+"[i].size(),sendBuffer,newsockfd,buffer_size);\n";
					result+="}\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

				}
				if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

				}

				if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
					result+="sendData((char*)&"+parameter.name+",sizeof(bool),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+",sizeof(double),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+",sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+".size(),sizeof(std::string::size_type),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+".c_str(),"+parameter.name+".size(),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
				}
				if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
				}
			}
		}
		return result;
	}
	
	public String pushOutToSocketFromJava2Cxx(){
		String result = "";
		for (Parameter parameter: _parameters) {
			if(parameter.isOut){
				if (parameter.type==Parameter.Type.Boolean ) {
					result+="sendBooleanData("+parameter.name+");\n";
				}
				if (parameter.type==Parameter.Type.Double) {
					result+="sendDoubleData("+parameter.name+");\n";
				}
				if (parameter.type==Parameter.Type.Integer) {
					result+="sendIntData("+parameter.name+");\n";
				}
				if (parameter.type==Parameter.Type.String ) {
					//TODOresult+=socketPrefix+"sendBooleanData("+parameter.name+");\n";
				}
				if (parameter.type==Parameter.Type.UserDefined ) {

				}
				if (parameter.type==Parameter.Type.Opaque ) {

				}

			}
		}
		return result;
	}
	public String pushOutToSocketForJava(){
		String result = "";
		for (Parameter parameter: _parameters) {
			if(parameter.isOut){
				if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(bool)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(double)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+",sizeof(int)*"+parameter.name+"_len,sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+"_len,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
					result+="\tint data_size="+parameter.name+"[i].size();\n";
					result+="\tsendData((char*)&data_size,sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
					result+="\tsendData((char*)"+parameter.name+"[i].c_str(),"+parameter.name+"[i].size(),sendBuffer,newsockfd,buffer_size);\n";
					result+="}\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

				}
				if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

				}

				if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
					result+="sendData((char*)&"+parameter.name+",sizeof(bool),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+",sizeof(double),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+",sizeof(int),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
					result+="sendData((char*)&"+parameter.name+".size(),sizeof(std::string::size_type),sendBuffer,newsockfd,buffer_size);\n";
					result+="sendData((char*)"+parameter.name+".c_str(),sizeof("+parameter.name+".size()),sendBuffer,newsockfd,buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
				}
				if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
				}
			}
		}
		return result;
	}
	
	public String pullOutFromSocketForJava(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.isOut){
				if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="bool* "+parameter.name+"=new bool["+parameter.name+"_data_len];\n";
					result+="int "+parameter.name+"_len;\n";
					result+=parameter.name+"_len=readIntData();\n";
					result+="readBooleanData("+parameter.name+","+parameter.name+"_len);\n";
				}
				if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="double* "+parameter.name+"=new double["+parameter.name+"_data_len];\n";
					result+="int "+parameter.name+"_len;\n";
					result+=parameter.name+"_len=readIntData();\n";
					result+="readDoubleData("+parameter.name+","+parameter.name+"_len);\n";;
				}
				if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="int* "+parameter.name+"=new int["+parameter.name+"_len];\n";

					result+="int "+parameter.name+"_len;\n";
					result+=parameter.name+"_len=readIntData();\n";
					result+="readIntData("+parameter.name+","+parameter.name+"_len);\n";
				}
				if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
					result+="int "+parameter.name+"_len;\n";
					result+=parameter.name+"_len=readIntData();\n";
					result+="readStringData("+parameter.name+","+parameter.name+"_len);\n";

				}
				if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

				}
				if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

				}

				if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray&&parameter.isOut)) {
					result+="readBooleanData("+parameter.name+",1);\n";
				}
				if (parameter.type==Parameter.Type.Double && (!parameter.isArray&&parameter.isOut)) {
					result+="readDoubleData("+parameter.name+",1);\n";;
					
				}
				if (parameter.type==Parameter.Type.Integer && (!parameter.isArray&&parameter.isOut)) {
					result+="readIntData("+parameter.name+",1);\n";
					
				}
				if (parameter.type==Parameter.Type.String && (!parameter.isArray&&parameter.isOut)) {
					result+="readStringData("+parameter.name+",1);\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray &&parameter.isOut)) {
					//result+="readData((char*)"+parameter.name+",sizeof(bool),__rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray&&parameter.isOut)) {
				}
				
				if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray&&!parameter.isOut)) {
					result+=parameter.name+"=readBooleanData();\n";
				}
				if (parameter.type==Parameter.Type.Double && (!parameter.isArray&&!parameter.isOut)) {
					result+=parameter.name+"=readDoubleData();\n";
					
				}
				if (parameter.type==Parameter.Type.Integer && (!parameter.isArray&&!parameter.isOut)) {
					result+=parameter.name+"=readIntData();\n";
					
				}
				if (parameter.type==Parameter.Type.String && (!parameter.isArray&&!parameter.isOut)) {
					result+=parameter.name+"=readStringData();\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray&&!parameter.isOut)) {
					//result+="readData((char*)"+parameter.name+",sizeof(bool),__rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray&&!parameter.isOut)) {
				}
			}
		}
		return result;
	}
	
	public String pullOutFromSocketForCxx(){
		String result="";
		for (Parameter parameter: _parameters) {
			if(parameter.isOut){
				if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="bool* "+parameter.name+"=new bool["+parameter.name+"_data_len];\n";

					result+="readData((char*)&"+parameter.name+"_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					result+="readData((char*)"+parameter.name+",sizeof(bool)*"+parameter.name+"_len,_rcvBuffer,newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="double* "+parameter.name+"=new double["+parameter.name+"_data_len];\n";
					result+="readData((char*)&"+parameter.name+"_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					result+="readData((char*)"+parameter.name+",sizeof(double)*"+parameter.name+"_len,_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					//result+="int* "+parameter.name+"=new int["+parameter.name+"_len];\n";

					result+="readData((char*)&"+parameter.name+"_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					result+="readData((char*)"+parameter.name+",sizeof(int)*"+parameter.name+"_len,_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
					//result+="int "+parameter.name+"_len=0;\n";
					result+="readData((char*)&"+parameter.name+"_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					//result+="char (* "+parameter.name+")[255]=new char["+parameter.name+"_len][255];\n";

					result+="for(int i=0;i<"+parameter.name+"_len;i++){\n";
					result+="\tint "+parameter.name+"_data_len=0;\n";
					result+="\treadData((char*)&"+parameter.name+"_data_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					result+="\treadData((char*)"+parameter.name+"[i],"+parameter.name+"_data_len<255?"+parameter.name+"_data_len:255,_rcvBuffer,_newsockfd,_buffer_size);\n";
					result+="}\n";

				}
				if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {

				}
				if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {

				}

				if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray)) {
					//result+="bool "+parameter.name+";\n";
					result+="readData((char*)&"+parameter.name+",sizeof(bool),_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Double && (!parameter.isArray )) {
					//result+="double "+parameter.name+";\n";
					result+="readData((char*)&"+parameter.name+",sizeof(double),_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Integer && (!parameter.isArray )) {
					//result+="int "+parameter.name+";\n";
					result+="readData((char*)&"+parameter.name+",sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.String && (!parameter.isArray )) {
					//result+="int "+parameter.name+"_str_len=0;\n";
					result+="readData((char*)&"+parameter.name+"_str_len,sizeof(int),_rcvBuffer,_newsockfd,_buffer_size);\n";
					//result+="char* "+parameter.name+"_data=new char["+parameter.name+"_str_len];\n";  
					result+="readData((char*)"+parameter.name+"_data,"+parameter.name+"_str_len,_rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray )) {
					//result+="readData((char*)"+parameter.name+",sizeof(bool),__rcvBuffer,_newsockfd,_buffer_size);\n";
				}
				if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray)) {
				}
			}
		}
		return result;
	}
	public String prepareCxxParametersForJNICall(){
		String result = "";

		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray )) {
				result += "jbooleanArray " + parameter.name + "_jni=env->NewBooleanArray("+parameter.name+"_len);\n";
				result += "env->SetBooleanArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jboolean*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray )) {
				result += "jdoubleArray " + parameter.name + "_jni=env->NewDoubleArray("+parameter.name+"_len);\n";
				result += "env->SetDoubleArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jdouble*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray )) {
				result += "jintArray " + parameter.name + "_jni=env->NewIntArray("+parameter.name+"_len);\n";
				result += "env->SetIntArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jint*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray )) {
				result += "jobjectArray " + parameter.name + "_jni=env->NewObjectArray("+parameter.name+"_len,env->FindClass(\"Ljava/lang/String;\"),0);\n";
				result += "for(int i=0;i<"+parameter.name+"_len;i++)\n";
				result += "\tenv->SetObjectArrayElement("+parameter.name+"_jni,i, env->NewStringUTF("+parameter.name+"[i].c_str()));\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray )) {
				result += "jintArray " + parameter.name + "_jni=env->NewIntArray("+parameter.name+"_len);\n";
				result += "env->SetIntArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jint*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray )) {
				result += "jlongArray " + parameter.name + "_jni=env->NewLongArray("+parameter.name+"_len);\n";
				result += "env->SetLongArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jlong*)"+parameter.name+");\n";
			}

			if (parameter.type==Parameter.Type.Boolean && (!parameter.isArray && parameter.isOut)) {
				result += "jbooleanArray " + parameter.name + "_jni=env->NewBooleanArray(1);\n";
				result += "env->SetBooleanArrayRegion("+parameter.name+"_jni,0,1,(jboolean*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double && (!parameter.isArray && parameter.isOut)) {
				result += "jdoubleArray " + parameter.name + "_jni=env->NewDoubleArray(1);\n";
				result += "env->SetDoubleArrayRegion("+parameter.name+"_jni,0,1,(jdouble*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer && (!parameter.isArray && parameter.isOut)) {
				result += "jintArray " + parameter.name + "_jni=env->NewIntArray(1);\n";
				result += "env->SetIntArrayRegion("+parameter.name+"_jni,0,1,(jint*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String && (!parameter.isArray && parameter.isOut)) {
				result += "jobjectArray " + parameter.name + "_jni=env->NewObjectArray(1,env->FindClass(\"Ljava/lang/String;\"),0);\n";
				result += "env->SetObjectArrayElement("+parameter.name+"_jni,0, env->NewStringUTF("+parameter.name+".c_str()));\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && (!parameter.isArray && parameter.isOut)) {
				result += "jintArray " + parameter.name + "_jni=env->NewIntArray(1);\n";
				result += "env->SetIntArrayRegion("+parameter.name+"_jni,0,1,(jint*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Opaque && (!parameter.isArray && parameter.isOut)) {
				result += "jlongArray " + parameter.name + "_jni=env->NewLongArray(1);\n";
				result += "env->SetLongArrayRegion("+parameter.name+"_jni,0,1,(jlong*)&"+parameter.name+");\n";
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "jboolean " + parameter.name+"_jni="+parameter.name+";\n";
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "jdouble " + parameter.name+"_jni="+parameter.name+";\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "jint " + parameter.name+"_jni="+parameter.name+";\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "jobject " + parameter.name+"_jni=env->NewStringUTF("+parameter.name+".c_str());\n";
			}
			if (parameter.type==Parameter.Type.UserDefined && !parameter.isArray && !parameter.isOut) {
				result += "jint " + parameter.name+"_jni="+parameter.name+";\n";
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "jlong " + parameter.name+"_jni="+parameter.name+";\n";
			}
		}
		return result;
	}

	public String writeCxxParamatersFromJNIUseCall(){
		String result = "";

		for (Parameter parameter: _parameters) {

			if (parameter.type==Parameter.Type.Boolean &&  parameter.isOut&& parameter.isArray){

				result +="env->GetBooleanArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jboolean*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double &&  parameter.isOut&& parameter.isArray){
				result +="env->GetDoubleArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jdouble*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer &&  parameter.isOut&& parameter.isArray){
				result +="env->GetIntArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jint*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String &&  parameter.isOut&& parameter.isArray){
				//TODO
			}
			if (parameter.type==Parameter.Type.UserDefined &&  parameter.isOut&& parameter.isArray){
				result +="env->GetIntArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jint*)"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Opaque &&  parameter.isOut&& parameter.isArray){
				result +="env->GetLongArrayRegion("+parameter.name+"_jni,0,"+parameter.name+"_len,(jlong*)"+parameter.name+");\n";
			}

			if (parameter.type==Parameter.Type.Boolean &&  parameter.isOut&& !parameter.isArray){

				result +="env->GetBooleanArrayRegion("+parameter.name+"_jni,0,1,(jboolean*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Double &&  parameter.isOut&&! parameter.isArray){
				result +="env->GetDoubleArrayRegion("+parameter.name+"_jni,0,1,(jdouble*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Integer &&  parameter.isOut&& !parameter.isArray){
				result +="env->GetIntArrayRegion("+parameter.name+"_jni,0,1,(jint*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.String &&  parameter.isOut&& !parameter.isArray){
				//TODO
			}
			if (parameter.type==Parameter.Type.UserDefined &&  parameter.isOut&& !parameter.isArray){
				result +="env->GetIntArrayRegion("+parameter.name+"_jni,0,1,(jint*)&"+parameter.name+");\n";
			}
			if (parameter.type==Parameter.Type.Opaque &&  parameter.isOut&& !parameter.isArray){
				result +="env->GetLongArrayRegion("+parameter.name+"_jni,0,1,(jlong*)&"+parameter.name+");\n";
			}
		}
		return result;
	}

	public String writeCxxParamatersFromJNIProvideCall(){
		String result = "";

		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean &&  !parameter.isOut && parameter.isArray){
				result +="env->ReleaseBooleanArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_ABORT);\n";
			}
			if (parameter.type==Parameter.Type.Double &&  !parameter.isOut && parameter.isArray){
				result +="env->ReleaseDoubleArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_ABORT);\n";
			}
			if (parameter.type==Parameter.Type.Integer &&  !parameter.isOut && parameter.isArray){
				result +="env->ReleaseIntArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_ABORT);\n";
			}
			if (parameter.type==Parameter.Type.String &&  !parameter.isOut && parameter.isArray){
				//TODO
			}
			if (parameter.type==Parameter.Type.UserDefined &&  !parameter.isOut && parameter.isArray){
				result +="env->ReleaseIntArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_ABORT);\n";
			}
			if (parameter.type==Parameter.Type.Opaque &&  !parameter.isOut && parameter.isArray){
				result +="env->ReleaseLongArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_ABORT);\n";
			}

			if (parameter.type==Parameter.Type.Boolean &&  parameter.isOut){
				if(parameter.isArray){
					result += "for(int i=0;i<env->GetArrayLenght("+parameter.name+");i++)\n";
					result += ""+parameter.name+"_jni[i]=("+parameter.name+"_b[i])?true:false;\n";
				}else
					result +=parameter.name+"_jni[0]="+parameter.name+"_b;\n";

				result +="env->ReleaseBooleanArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_COMMIT);\n";
			}
			if (parameter.type==Parameter.Type.Double &&  parameter.isOut){
				result +="env->ReleaseDoubleArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_COMMIT);\n";
			}
			if (parameter.type==Parameter.Type.Integer &&  parameter.isOut){
				result +="env->ReleaseIntArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_COMMIT);\n";
			}
			if (parameter.type==Parameter.Type.String &&  parameter.isOut){
				//TODO
			}
			if (parameter.type==Parameter.Type.UserDefined &&  parameter.isOut){
				result +="env->ReleaseIntArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_COMMIT);\n";
			}
			if (parameter.type==Parameter.Type.Opaque &&  parameter.isOut){
				result +="env->ReleaseLongArrayElements("+parameter.name+","+parameter.name+"_jni,JNI_COMMIT);\n";
			}
		}
		return result;
	}

	public String getParameterListInJavaWithIntEnums(boolean withKeywordFinal) {
		_trace.in( "getParameterListInJava()"  );
		String result = "";

		for (Parameter parameter: _parameters) {
			result += ",";
			if (withKeywordFinal) {
				result += "final ";
			}

			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray || parameter.isOut)) {
				result += "boolean " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray || parameter.isOut)) {
				result += "double " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray || parameter.isOut)) {
				result += "int " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray || parameter.isOut)) {
				result += "String " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray || parameter.isOut) ){
				result+= "int " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray || parameter.isOut) ){
				result+= "long " + parameter.name + "[]";
			}
			//					if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
			//						result += "BooleanWrapper " + parameter.name;
			//					}
			//					if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
			//						result += "DoubleWrapper " + parameter.name;
			//					}
			//					if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
			//						result += "IntegerWrapper " + parameter.name;
			//					}
			//					if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
			//						result += "StringWrapper " + parameter.name;
			//					}
			//					if (parameter.type==Parameter.Type.Boolean && parameter.isArray && !parameter.isOut) {
			//						result += "boolean " + parameter.name + "[]";
			//					}
			//					if (parameter.type==Parameter.Type.Double && parameter.isArray && !parameter.isOut) {
			//						result += "double " + parameter.name + "[]";
			//					}
			//					if (parameter.type==Parameter.Type.Integer && parameter.isArray && !parameter.isOut) {
			//						result += "int " + parameter.name + "[]";
			//					}
			//					if (parameter.type==Parameter.Type.String && parameter.isArray && !parameter.isOut) {
			//						result += "String " + parameter.name + "[]";
			//					}
			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "boolean " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "double " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "int " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "String " + parameter.name;
			}
			if (parameter.type==Parameter.Type.UserDefined && !parameter.isArray && !parameter.isOut) {
				result += "int " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "long " + parameter.name;
			}
		}

		result = result.replaceFirst(",", "");
		_trace.out( "getParameterListInJava()", result );
		return result;
	}

	public String getParameterListInC2F() {
		_trace.in( "getParameterListInC2F()"  );
		String result = "";

		for (Parameter parameter: _parameters) {
			result += ",";
			result += (!parameter.isOut)?"const ":"";

			if (parameter.type==Parameter.Type.Boolean && parameter.isArray) {
				result += "bool* " + parameter.name + ", const int& "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				result += "double* " + parameter.name + "" + ", const int& "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray) {
				result += "int* " + parameter.name + "" + ", const int& "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray) {
				result += "char(* " + parameter.name + ")[255], const int& "+parameter.name+"_len";
			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int& "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				result += "void** " + parameter.name + "" + ", const int& "+parameter.name+"_len";
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				result += "bool& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				result += "double& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				result += "int& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				result += "char* " + parameter.name;
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "bool& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "double& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "int& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "char* " + parameter.name;
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);

				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "void* " + parameter.name;
			}
		}

		_trace.out( "getParameterListInC2F()"  );
		return result;
	}
	public String getParameterListInJava(boolean withKeywordFinal) {
		_trace.in( "getParameterListInJava()"  );
		String result = "";

		for (Parameter parameter: _parameters) {
			result += ",";
			if (withKeywordFinal) {
				result += "final ";
			}

			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray || parameter.isOut)) {
				result += "boolean " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray || parameter.isOut)) {
				result += "double " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray || parameter.isOut)) {
				result += "int " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray || parameter.isOut)) {
				result += "String " + parameter.name + "[]";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray || parameter.isOut) ){
				result+= parameter.userDefinedTypeIdentifier+" " + parameter.name + "[]";
			}
			if(parameter.type==Parameter.Type.Opaque && (parameter.isArray || parameter.isOut) ){
				result+= "long " + parameter.name + "[]";
			}
			//			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
			//				result += "BooleanWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
			//				result += "DoubleWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
			//				result += "IntegerWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
			//				result += "StringWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Boolean && parameter.isArray && !parameter.isOut) {
			//				result += "boolean " + parameter.name + "[]";
			//			}
			//			if (parameter.type==Parameter.Type.Double && parameter.isArray && !parameter.isOut) {
			//				result += "double " + parameter.name + "[]";
			//			}
			//			if (parameter.type==Parameter.Type.Integer && parameter.isArray && !parameter.isOut) {
			//				result += "int " + parameter.name + "[]";
			//			}
			//			if (parameter.type==Parameter.Type.String && parameter.isArray && !parameter.isOut) {
			//				result += "String " + parameter.name + "[]";
			//			}
			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "boolean " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "double " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "int " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "String " + parameter.name;
			}
			if (parameter.type==Parameter.Type.UserDefined && !parameter.isArray && !parameter.isOut) {
				result += parameter.userDefinedTypeIdentifier+" " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "long " + parameter.name;
			}
		}

		result = result.replaceFirst(",", "");
		_trace.out( "getParameterListInJava()", result );
		return result;
	}


	public String getParameterListInJNI(boolean withKeywordFinal) {
		_trace.in( "getParameterListInJNI()"  );
		String result = "";
		for (Parameter parameter: _parameters) {
			result += ",";
			if (withKeywordFinal) {
				result += "";
			}

			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray || parameter.isOut)) {
				result += "jbooleanArray " + parameter.name + "";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray || parameter.isOut)) {
				result += "jdoubleArray " + parameter.name + "";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray || parameter.isOut)) {
				result += "jintArray " + parameter.name + "";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray || parameter.isOut)) {
				result += "jObjectArray " + parameter.name + "";
			}

			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray || parameter.isOut)) {
				result += "jintArray " + parameter.name + "";
			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray || parameter.isOut)) {
				result += "jlongArray " + parameter.name + "";
			}
			//			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
			//				result += "BooleanWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
			//				result += "DoubleWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
			//				result += "IntegerWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
			//				result += "StringWrapper " + parameter.name;
			//			}


			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "jboolean " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "jdouble " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "jint " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "jstring " + parameter.name;
			}
			if (parameter.type==Parameter.Type.UserDefined && !parameter.isArray && !parameter.isOut) {
				result += "jint " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "jlong " + parameter.name;
			}
		}

		//result = result.replaceFirst(",", "");
		_trace.out( "getParameterListInJNI()"  );
		return result;
	}
	public String getParameterListInJNITypes() {
		_trace.in( "getParameterListInJNITypes()"  );
		String result="";
		for (Parameter parameter: _parameters) {


			if (parameter.type==Parameter.Type.Boolean && (parameter.isArray || parameter.isOut)) {
				result += "[B";
			}
			if (parameter.type==Parameter.Type.Double && (parameter.isArray || parameter.isOut)) {
				result += "[D";
			}
			if (parameter.type==Parameter.Type.Integer && (parameter.isArray || parameter.isOut)) {
				result += "[I";
			}
			if (parameter.type==Parameter.Type.String && (parameter.isArray|| parameter.isOut)) {
				result += "[Ljava/lang/String;";
			}
			if (parameter.type==Parameter.Type.UserDefined && (parameter.isArray || parameter.isOut)) {
				result += "[I";
			}
			if (parameter.type==Parameter.Type.Opaque && (parameter.isArray || parameter.isOut)) {
				result += "[J";
			}
			//			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
			//				result += "BooleanWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
			//				result += "DoubleWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
			//				result += "IntegerWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
			//				result += "StringWrapper " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Boolean && parameter.isArray || parameter.isOut) {
			//				result += "[B";
			//			}
			//			if (parameter.type==Parameter.Type.Double && parameter.isArray || parameter.isOut) {
			//				result += "[D";
			//			}
			//			if (parameter.type==Parameter.Type.Integer && parameter.isArray || parameter.isOut) {
			//				result += "[I";
			//			}
			//			if (parameter.type==Parameter.Type.String && parameter.isArray || parameter.isOut) {
			//				result += "[Ljava/lang/String;";
			//			}
			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "B";
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "D";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "I";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "Ljava/lang/String;";
			}
			if (parameter.type==Parameter.Type.UserDefined && !parameter.isArray && !parameter.isOut) {
				result += "I";
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "J";
			}
		}

		_trace.out( "getParameterListInJNITypes()"  );
		return result;
	}

	public String getParameterListInF2Cxx(){
		_trace.in( "getParameterListInF2Cxx()"  );

		String result = "";
		for (Parameter parameter: _parameters) {
			result += ",";
			//result += (!parameter.isOut)?"const ":"";

			if (parameter.type==Parameter.Type.Boolean && parameter.isArray) {
				result += "bool* " + parameter.name + ", int* "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				result += "double* " + parameter.name + "" + ", int* "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray) {
				result += "int* " + parameter.name + "" + ",int* "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray) {
				result += "char** " + parameter.name + "" + ",int* "+parameter.name+"_len";
			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ",int* "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				result += "void** " + parameter.name + "" + ",int* "+parameter.name+"_len";
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				result += "bool* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				result += "double* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				result += "int* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				result += "char* " + parameter.name;
				
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "bool* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "double* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "int* " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "char* " + parameter.name;
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);

				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "void* " + parameter.name;
			}
		}
	   result = result.replaceFirst(",", "");
		_trace.out( "getParameterListInF2Cxx()"  );
		return result;
	}
	public String getParameterListInCxx() {
		_trace.in( "getParameterListInCxx()"  );

		String result = "";

		for (Parameter parameter: _parameters) {
			result += ",";
			result += (!parameter.isOut)?"const ":"";

			if (parameter.type==Parameter.Type.Boolean && parameter.isArray) {
				result += "bool* " + parameter.name + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				result += "double* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray) {
				result += "int* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray) {
				result += "std::string* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				result += "void** " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				result += "bool& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				result += "double& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				result += "int& " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				result += "std::string& " + parameter.name;
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result += "bool " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result += "double " + parameter.name;
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result += "int " + parameter.name;
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result += "std::string " + parameter.name;
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				assert(enumDecl!=null);
				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);

				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				result += "void* " + parameter.name;
			}
		}

		result = result.replaceFirst(",", "");
		_trace.out( "getParameterListInCxx()"  );
		return result;
	}

	public GetParameterList(Scope scope) {
		_trace.in( "GetParameterList()"  );
		_parameters = new java.util.LinkedList< Parameter >();
		_hasEnums=false;
		_enumTypes=new HashSet<String>();
		_scope=scope;
		_trace.out( "GetParameterList()"  );

	}

	java.util.List< Parameter > getParameters() {
		return _parameters;
	}

	public void inAParameterArrayInParameter(AParameterArrayInParameter node) {
		_trace.in( "inAParameterArrayInParameter(...)"  );
		Parameter newParameter = new Parameter();

		newParameter.isArray = true;
		newParameter.isOut   = false;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_trace.out( "inAParameterArrayInParameter(...)" );
	}

	public void inAParameterArrayOutParameter(AParameterArrayOutParameter node) {
		_trace.in( "inAParameterArrayOutParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = true;
		newParameter.isOut   = true;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_trace.out( "inAParameterArrayOutParameter(...)" );
	}

	public void inAParameterInParameter(AParameterInParameter node) {
		_trace.in( "inAParameterInParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = false;
		newParameter.isOut   = false;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_trace.out( "inAParameterInParameter(...)" );
	}

	public void inAParameterOutParameter(AParameterOutParameter node) {
		_trace.in( "inAParameterOutParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = false;
		newParameter.isOut   = true;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_trace.out( "inAParameterOutParameter(...)" );
	}

	@Override
	public void inAParameterInEnumParameter(AParameterInEnumParameter node){
		_trace.in( "inAParameterInEnumParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = false;
		newParameter.isOut   = false;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_hasEnums=true;
		_trace.out( "inAParameterInEnumParameter(...)" );
	}
	@Override
	public void inAParameterArrayOutEnumParameter(AParameterArrayOutEnumParameter node) {
		_trace.in( "inAParameterArrayOutEnumParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = true;
		newParameter.isOut   = true;
		newParameter.name    = node.getName().getText();
		_hasEnums=true;
		_parameters.add(newParameter);
		_trace.out( "inAParameterArrayOutEnumParameter(...)" );
	}
	@Override
	public void inAParameterOutEnumParameter(AParameterOutEnumParameter node){
		_trace.in( "inAParameterOutEnumParameter(...)" );
		Parameter newParameter = new Parameter();

		newParameter.isArray = false;
		newParameter.isOut   = true;
		newParameter.name    = node.getName().getText();

		_parameters.add(newParameter);
		_hasEnums=true;
		_trace.out( "inAParameterOutEnumParameter(...)" );
	}

	@Override
	public void inAParameterArrayInEnumParameter(AParameterArrayInEnumParameter node) {
		_trace.in( "inAParameterArrayInEnumParameter(...)"  );
		Parameter newParameter = new Parameter();

		newParameter.isArray = true;
		newParameter.isOut   = false;
		newParameter.name    = node.getName().getText();
		_hasEnums=true;
		_parameters.add(newParameter);
		_trace.out( "inAParameterArrayInEnumParameter(...)" );
	}


	public void inAUserDefinedType(AUserDefinedType node){
		String nodeSymbol = Scope.getSymbol(node);
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.UserDefined;
		_parameters.get( _parameters.size()-1 ).userDefinedTypeIdentifier = nodeSymbol;
		_enumTypes.add(nodeSymbol);
	}
	public void inAIntBuiltInType(AIntBuiltInType node) {
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.Integer;
	}

	public void inADoubleBuiltInType(ADoubleBuiltInType node) {
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.Double;
	}

	public void inABoolBuiltInType(ABoolBuiltInType node) {
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.Boolean;
	}

	public void inAOpaqueBuiltInType(AOpaqueBuiltInType node){
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.Opaque;
	}

	public void inAStringBuiltInType(AStringBuiltInType node) {
		Assert.isTrue( _parameters.size()>0 );
		_parameters.get( _parameters.size()-1 ).type = Parameter.Type.String;
	}

	public int size() {
		return _parameters.size();
	}

	public boolean hasEnums() {
		return _hasEnums;
	}

	public HashSet<String> getEnumTypes(){
		return _enumTypes;
	}
	public String getParameterListTypesForFCBindedToC(boolean toC) {
		String result = "";

		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && parameter.isArray ) {
				result+="\tlogical(kind=c_bool),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";

			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				result+="\treal(kind=c_double),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray ) {
				result+="\tinteger(kind=c_int),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray) {
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
				result+="\ttype(c_ptr),dimension(*),intent(in)::"+parameter.name+"\n";
			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				//TODO result += "void** " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}



			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				result+="\tlogical(kind=c_bool),intent(inout)::"+parameter.name+"\n";

			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				result+="\treal(kind=c_double),intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				result+="\tinteger(kind=c_int),intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				if(!toC)
					result+="\tcharacter(kind=c_char),dimension(*),intent(inout)::"+parameter.name+"\n";
				else{
					result+="\ttype(c_ptr),intent(inout)::"+parameter.name+"\n";
				}
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				//TODO result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result+="\tlogical(kind=c_bool),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result+="\treal(kind=c_double),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				if(!toC){
					result+="\tcharacter(kind=c_char),dimension(*),intent(in)::"+parameter.name+"\n";
				}else{
					result+="\ttype(c_ptr),intent(in)::"+parameter.name+"\n";
				}
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				//TODO result += "void* " + parameter.name;
			}
		}
		return result;
	}
	public String getParameterListTypesForFCBindedFromC() {
		String result = "";

		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && parameter.isArray ) {
				result+="\tlogical(kind=c_bool),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";

			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				result+="\treal(kind=c_double),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray ) {
				result+="\tinteger(kind=c_int),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray) {
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"_len\n";
				result+="\tcharacter(255),dimension(*),intent(in)::"+parameter.name+"\n";
			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				//TODO result += "void** " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}



			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				result+="\tlogical(kind=c_bool),intent(inout)::"+parameter.name+"\n";

			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				result+="\treal(kind=c_double),intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				result+="\tinteger(kind=c_int),intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				result+="\tcharacter(255),intent(inout)::"+parameter.name+"\n";
				
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				//TODO result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				result+="\tlogical(kind=c_bool),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				result+="\treal(kind=c_double),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				result+="\tinteger(kind=c_int),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				result+="\tcharacter(255),intent(in)::"+parameter.name+"\n";
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				//TODO result += "void* " + parameter.name;
			}
		}
		return result;
	}
	public String getParameterListTypesForF(boolean fromCl){
		String typesResult = "";
		String stmtResult = "";

		for (Parameter parameter: _parameters) {
			if (parameter.type==Parameter.Type.Boolean && parameter.isArray ) {
				typesResult+="\tlogical,intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				typesResult+="\tinteger,intent(in)::"+parameter.name+"_len\n";

			}
			if (parameter.type==Parameter.Type.Double && parameter.isArray ) {
				typesResult+="\treal(8),intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				typesResult+="\tinteger,intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.Integer && parameter.isArray ) {
				typesResult+="\tinteger,intent("+((parameter.isOut)?"inout":"in")+"),dimension(*)::"+parameter.name+"\n";
				typesResult+="\tinteger,intent(in)::"+parameter.name+"_len\n";
			}
			if (parameter.type==Parameter.Type.String && parameter.isArray ) {
				typesResult+="\tcharacter(*),intent(in),dimension(*)::"+parameter.name+"\n";
				typesResult+="\tinteger,intent(in)::"+parameter.name+"_len\n";
				if(fromCl){
					typesResult+="\ttype(c_ptr),dimension("+parameter.name+"_len) :: "+parameter.name+"PtrArray\n";
					typesResult+="\tinteger::"+parameter.name+"_ns\n";
					typesResult+="\tcharacter(255), dimension("+parameter.name+"_len), target :: "+parameter.name+"FSArray\n";
					stmtResult+="\tdo "+parameter.name+"_ns = 1, "+parameter.name+"_len\n";
					stmtResult+="\t\t"+parameter.name+"FSArray("+parameter.name+"_ns) = "+parameter.name+"("+parameter.name+"_ns)// C_NULL_CHAR\n";
					stmtResult+="\t\t"+parameter.name+"PtrArray("+parameter.name+"_ns) = C_LOC("+parameter.name+"FSArray("+parameter.name+"_ns))\n";
					stmtResult+="\tend do\n";

				}



			}
			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}
			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
				//TODO result += "void** " + parameter.name + "" + ", const int "+parameter.name+"_len";
			}



			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
				typesResult+="\tlogical,intent(inout)::"+parameter.name+"\n";

			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
				typesResult+="\treal(8),intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
				typesResult+="\tinteger,intent(inout)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
				typesResult+="\tcharacter(*),intent(inout)::"+parameter.name+"\n";
			}
			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
				//TODO result += "void*& " + parameter.name;
			}

			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
				typesResult+="\tlogical,intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
				typesResult+="\treal(8),intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
				typesResult+="\tinteger,intent(in)::"+parameter.name+"\n";
			}
			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
				typesResult+="\tcharacter(*),intent(in)::"+parameter.name+"\n";
			}

			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
				//TODO				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
				//				assert(enumDecl!=null);
				//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
				//
				//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			}
			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
				//TODO result += "void* " + parameter.name;
			}
		}
		return typesResult+stmtResult;
	}
	public String getParameterListInF(boolean areAllParametersInParameters) {

		String result = ",";

		for (Parameter parameter: _parameters) {
			result += ",&\n";

			if ( parameter.type==Parameter.Type.Boolean||
					parameter.type==Parameter.Type.Double||
					parameter.type==Parameter.Type.Integer||
					parameter.type==Parameter.Type.String) {
				result +="\t"+parameter.name; 
				if(parameter.isArray)
					result +=","+parameter.name+"_len";
			}

			//TODO			if (parameter.type==Parameter.Type.String && parameter.isArray) {
			//				result += "std::string* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			//			}
			//			if(parameter.type==Parameter.Type.UserDefined && parameter.isArray){
			//				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
			//				assert(enumDecl!=null);
			//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
			//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"* " + parameter.name + "" + ", const int "+parameter.name+"_len";
			//			}
			//			if (parameter.type==Parameter.Type.Opaque && parameter.isArray) {
			//				result += "void** " + parameter.name + "" + ", const int "+parameter.name+"_len";
			//			}
			//			
			//			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && parameter.isOut) {
			//				result += "bool& " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Double && !parameter.isArray && parameter.isOut) {
			//				result += "double& " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && parameter.isOut) {
			//				result += "int& " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.String && !parameter.isArray && parameter.isOut) {
			//				result += "std::string& " + parameter.name;
			//			}
			//			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray && parameter.isOut ){
			//				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
			//				assert(enumDecl!=null);
			//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
			//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+"& " + parameter.name ;
			//			}
			//			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && parameter.isOut) {
			//				result += "void*& " + parameter.name;
			//			}
			//
			//			if (parameter.type==Parameter.Type.Boolean && !parameter.isArray && !parameter.isOut) {
			//				result += "bool " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Double && !parameter.isArray && !parameter.isOut) {
			//				result += "double " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.Integer && !parameter.isArray && !parameter.isOut) {
			//				result += "int " + parameter.name;
			//			}
			//			if (parameter.type==Parameter.Type.String && !parameter.isArray && !parameter.isOut) {
			//				result += "std::string " + parameter.name;
			//			}
			//
			//			if(parameter.type==Parameter.Type.UserDefined && !parameter.isArray &&! parameter.isOut ){
			//				AEnumDeclarationPackageElement enumDecl=_scope.getEnumerationDefinition(parameter.userDefinedTypeIdentifier);
			//				assert(enumDecl!=null);
			//				String fullQuualifiedTypeName=_scope.getFullQualifiedName(parameter.userDefinedTypeIdentifier);
			//
			//				result +=fullQuualifiedTypeName.replaceAll("[.]", "::")+" " + parameter.name ;
			//			}
			//			if (parameter.type==Parameter.Type.Opaque && !parameter.isArray && !parameter.isOut) {
			//				result += "void* " + parameter.name;
			//			}
		}

		result = result.replaceFirst(",", "");
		return result;
	}

	public String convertCharsToString() {
		String result="";
		for (Parameter parameter: _parameters) {


			if( parameter.type== Parameter.Type.String ){
				if(parameter.isArray){
					result += "std::string* "+parameter.name+"_str=new std::string[*"+parameter.name+"_len];\n"; 
					result += "for(int i=0;i<*"+parameter.name+"_len;i++)\n"; 
					result +=	 parameter.name+"_str[i]="+parameter.name+"[i];\n";
				}else{
					
					result += "std::string "+parameter.name+"_str("+parameter.name+");\n"; 
				}
			}
		}
		return result;
	}

	public String getFunctionCallListInFClient(boolean appendCNULLCHAR) {
		String result="";
		for (Parameter parameter: _parameters) {
			result += ",&\n";

			if ( parameter.type==Parameter.Type.Boolean||
					parameter.type==Parameter.Type.Double||
					parameter.type==Parameter.Type.Integer
					) {
				result += parameter.name; 
				if(parameter.isArray)
					result +=","+parameter.name+"_len";
			}
			if( parameter.type== Parameter.Type.String ){
				result += parameter.name;
				if(!parameter.isArray&&appendCNULLCHAR)
					result += "//c_null_char";
				if(parameter.isArray&&appendCNULLCHAR)
					result +="PtrArray,"+parameter.name+"_len";
				if(parameter.isArray&&!appendCNULLCHAR)
					result +=","+parameter.name+"_len";
			}
		}
		return (appendCNULLCHAR)?result.replaceFirst(",", ""):result;
	}

	public String getFunctionCallListInFServer() {
		String result="";
		for (Parameter parameter: _parameters) {
			result += ",&\n";

			if ( parameter.type==Parameter.Type.Boolean||
					parameter.type==Parameter.Type.Double||
					parameter.type==Parameter.Type.Integer||
					parameter.type== Parameter.Type.String
					) {
				result += parameter.name; 
				if(parameter.isArray)
					result +=","+parameter.name+"_len";
			}

		}
		return result;
	}





}
