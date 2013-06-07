package de.tum.ascodt.repository;

import java.io.IOException;


/**
 * Represents target information. Currently ASCoDT supports
 * three targets : a local java component, a local native component and
 * a remote native component connected through sockets.
 * 
 * 
 * @author Tobias Weinzierl
 */
public class Target {
	public enum TargetType {
		JavaLocal{
			public String toString(){
				return "java_local";
			}
		},
		JavaNative{
			public String toString(){
				return "java_native";
			}
		},
		
		ReverseCxxRemoteSocket{
			public String toString(){
				return "reverse_cxx_remote_socket";
			}
		},
		CxxRemoteSocket{
			public String toString(){
				return "cxx_remote_socket";
			}
		},
		ReverseFortranRemoteSocket{
			public String toString(){
				return "reverse_fortran_remote_socket";
			}
		},
		FortranRemoteSocket{
			public String toString(){
				return "fortran_remote_socket";
			}
		},
		
		FortranNative{
			public String toString(){
				return "fortran_native";
			}
		}
		
	}

	/**
	 * 
	 * @return all possible targets
	 */
	public static String[] getAllTargetTypes(){
		String [] res=new String[TargetType.values().length];
		for(int i=0;i<res.length;i++)
			res[i]=TargetType.values()[i].toString();
		return res; 
	}



	private TargetType   _targetType;
  private Object _descriptionObject;

	private Target() {
	}


	public TargetType getType() {
		return _targetType;
	}

	public Object getDescription(){
		return _descriptionObject;
	}

	/**
	 * This method verifies validity of targets
	 * @param identifier
	 * @return
	 */
	public static boolean isValidTarget( String identifier ) {
		return isJavaLocal(identifier)||isCxxNative(identifier)
				||isReverseCxxRemoteSocket(identifier)||isCxxRemoteSocket(identifier)
				||isFortranNative(identifier); // add with || further targets    
	}


	public static boolean isJavaLocal(String identifier) {
		return identifier.equals( TargetType.JavaLocal.toString() );
	}

	public static boolean isCxxNative(String identifier) {
		return identifier.equals( TargetType.JavaNative.toString() );
	}

	public static boolean isReverseCxxRemoteSocket(String identifier){
		return identifier.equals( TargetType.ReverseCxxRemoteSocket.toString() );
	}
	
	public static boolean isCxxRemoteSocket(String identifier){
		return identifier.equals( TargetType.CxxRemoteSocket.toString() );
	}
	
	public static boolean isFortranNative(String identifier){
		return identifier.equals( TargetType.FortranNative.toString() );
	}
	
	public static boolean isReverseFortranRemoteSocket(String identifier){
		return identifier.equals( TargetType.ReverseFortranRemoteSocket.toString() );
	}
	
	public static boolean isFortranRemoteSocket(String identifier){
		return identifier.equals( TargetType.FortranRemoteSocket.toString() );
	}
	

	/**
	 * Factory mechanism
	 */
	public static Target createJavaLocalTarget() {
		Target result = new Target();
		result._targetType = TargetType.JavaLocal;
		return result;
	}

	public static Target createCxxNativeTarget(){
		Target result = new Target();
		result._targetType = TargetType.JavaNative;
		return result;
	}
	
	public static Target createCxxRemoteSocketTarget(){
		Target result = new Target();
		result._targetType = TargetType.CxxRemoteSocket;
		return result;
	}
	
	public static Target createReverseCxxRemoteSocketTarget(){
		Target result = new Target();
		result._targetType = TargetType.ReverseCxxRemoteSocket;
		return result;
	}

	public static Target createFortranRemoteSocketTarget(){
		Target result = new Target();
		result._targetType = TargetType.FortranRemoteSocket;
		return result;
	}
	
	public static Target createReverseFortranRemoteSocketTarget(){
		Target result = new Target();
		result._targetType = TargetType.ReverseFortranRemoteSocket;
		return result;
	}
	
	public static Target createFortranNativeTarget() {
		Target result = new Target();
		result._targetType = TargetType.FortranNative;
		return result;
	}
	
	public static Target createTarget(String identifier){
		Target result=null;
		if(identifier.equals( TargetType.JavaLocal.toString())){
			result = createJavaLocalTarget();
		}else if(identifier.equals( TargetType.JavaNative.toString())){
			result = createCxxNativeTarget();
		}else if(identifier.equals( TargetType.CxxRemoteSocket.toString())){
			result = createCxxRemoteSocketTarget();
		}else if(identifier.equals( TargetType.ReverseCxxRemoteSocket.toString())){
			result = createReverseCxxRemoteSocketTarget();
		}else if(identifier.equals(TargetType.FortranNative.toString())){
			result = createFortranNativeTarget();
		}else if(identifier.equals( TargetType.FortranRemoteSocket.toString())){
			result = createFortranRemoteSocketTarget();
		}else if(identifier.equals( TargetType.ReverseFortranRemoteSocket.toString())){
			result = createReverseFortranRemoteSocketTarget();
		}
		return result;
	}


	

}
